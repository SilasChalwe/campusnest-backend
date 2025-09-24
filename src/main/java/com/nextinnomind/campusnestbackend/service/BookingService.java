
package com.nextinnomind.campusnestbackend.service;

import com.nextinnomind.campusnestbackend.dto.booking.*;
import com.nextinnomind.campusnestbackend.entity.BookingRequest;
import com.nextinnomind.campusnestbackend.entity.Property;
import com.nextinnomind.campusnestbackend.entity.Unit;
import com.nextinnomind.campusnestbackend.entity.User;
import com.nextinnomind.campusnestbackend.enums.BookingStatus;
import com.nextinnomind.campusnestbackend.exception.BadRequestException;
import com.nextinnomind.campusnestbackend.exception.ResourceNotFoundException;
import com.nextinnomind.campusnestbackend.repository.BookingRequestRepository;
import com.nextinnomind.campusnestbackend.repository.PropertyRepository;
import com.nextinnomind.campusnestbackend.repository.UnitRepository;
import com.nextinnomind.campusnestbackend.repository.UserRepository;
import com.nextinnomind.campusnestbackend.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BookingService {

    private final BookingRequestRepository bookingRepository;
    private final PropertyRepository propertyRepository;
    private final UnitRepository unitRepository;
    private final UserRepository userRepository;
    private final ChatService chatService;
    private final EmailService emailService;

    public BookingResponse createBooking(CreateBookingRequest request, Long studentId) {
        // Validate dates
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new BadRequestException("Start date must be before end date");
        }

        Property property = propertyRepository.findById(request.getPropertyId())
                .orElseThrow(() -> new ResourceNotFoundException("Property not found"));

        Unit unit = unitRepository.findById(request.getUnitId())
                .orElseThrow(() -> new ResourceNotFoundException("Unit not found"));

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check if unit belongs to property
        if (!unit.getProperty().getId().equals(property.getId())) {
            throw new BadRequestException("Unit does not belong to this property");
        }

        // Check if unit is available
        if (!unit.getAvailable()) {
            throw new BadRequestException("Unit is not available");
        }

        // Check for conflicting bookings
        List<BookingRequest> conflicts = bookingRepository.findConflictingBookings(
                unit.getId(), request.getStartDate(), request.getEndDate());
        if (!conflicts.isEmpty()) {
            throw new BadRequestException("Unit is already booked for the selected dates");
        }

        BookingRequest booking = BookingRequest.builder()
                .property(property)
                .unit(unit)
                .student(student)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .studentMessage(request.getStudentMessage())
                .build();

        BookingRequest savedBooking = bookingRepository.save(booking);

        // Send notification to landlord
        emailService.sendBookingNotification(property.getOwner().getEmail(),
                property.getOwner().getFullName(), savedBooking);

        // Start chat conversation
        chatService.createBookingConversation(savedBooking);

        log.info("Created booking request: {} for property: {}", savedBooking.getId(), property.getId());

        return convertToResponse(savedBooking);
    }

    @Transactional(readOnly = true)
    public Page<BookingResponse> getUserBookings(Long userId, Pageable pageable) {
        return bookingRepository.findByStudentId(userId, pageable)
                .map(this::convertToResponse);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getPropertyBookings(Long propertyId, Long landlordId) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found"));

        if (!property.getOwner().getId().equals(landlordId)) {
            throw new BadRequestException("You can only view bookings for your own properties");
        }

        return bookingRepository.findByPropertyId(propertyId).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BookingResponse getBookingById(Long id, UserPrincipal userPrincipal) {
        BookingRequest booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        // Check access permissions
        boolean isStudent = booking.getStudent().getId().equals(userPrincipal.getId());
        boolean isLandlord = booking.getProperty().getOwner().getId().equals(userPrincipal.getId());
        boolean isAdmin = userPrincipal.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        if (!isStudent && !isLandlord && !isAdmin) {
            throw new BadRequestException("You don't have permission to view this booking");
        }

        return convertToResponse(booking);
    }

    public BookingResponse approveBooking(Long id, BookingResponseRequest response, Long landlordId) {
        BookingRequest booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (!booking.getProperty().getOwner().getId().equals(landlordId)) {
            throw new BadRequestException("You can only approve bookings for your own properties");
        }

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new BadRequestException("Only pending bookings can be approved");
        }

        booking.setStatus(BookingStatus.APPROVED);
        booking.setLandlordResponse(response != null ? response.getMessage() : null);
        booking.setRespondedAt(LocalDateTime.now());

        // Mark unit as unavailable for the booking period
        booking.getUnit().setAvailable(false);
        unitRepository.save(booking.getUnit());

        BookingRequest savedBooking = bookingRepository.save(booking);

        // Send notification to student
        emailService.sendBookingApprovalNotification(
                booking.getStudent().getEmail(),
                booking.getStudent().getFullName(),
                savedBooking);

        log.info("Approved booking: {} by landlord: {}", id, landlordId);

        return convertToResponse(savedBooking);
    }

    public BookingResponse rejectBooking(Long id, BookingResponseRequest response, Long landlordId) {
        BookingRequest booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (!booking.getProperty().getOwner().getId().equals(landlordId)) {
            throw new BadRequestException("You can only reject bookings for your own properties");
        }

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new BadRequestException("Only pending bookings can be rejected");
        }

        booking.setStatus(BookingStatus.REJECTED);
        booking.setLandlordResponse(response.getMessage());
        booking.setRespondedAt(LocalDateTime.now());

        BookingRequest savedBooking = bookingRepository.save(booking);

        // Send notification to student
        emailService.sendBookingRejectionNotification(
                booking.getStudent().getEmail(),
                booking.getStudent().getFullName(),
                savedBooking);

        log.info("Rejected booking: {} by landlord: {}", id, landlordId);

        return convertToResponse(savedBooking);
    }

    public BookingResponse cancelBooking(Long id, Long studentId) {
        BookingRequest booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (!booking.getStudent().getId().equals(studentId)) {
            throw new BadRequestException("You can only cancel your own bookings");
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BadRequestException("Booking is already cancelled");
        }

        booking.setStatus(BookingStatus.CANCELLED);

        // Make unit available again if it was approved
        if (booking.getStatus() == BookingStatus.APPROVED) {
            booking.getUnit().setAvailable(true);
            unitRepository.save(booking.getUnit());
        }

        BookingRequest savedBooking = bookingRepository.save(booking);

        log.info("Cancelled booking: {} by student: {}", id, studentId);

        return convertToResponse(savedBooking);
    }

    private BookingResponse convertToResponse(BookingRequest booking) {
        return BookingResponse.builder()
                .id(booking.getId())
                .property(BookingResponse.PropertyInfo.builder()
                        .id(booking.getProperty().getId())
                        .title(booking.getProperty().getTitle())
                        .address(booking.getProperty().getAddress())
                        .build())
                .unit(BookingResponse.UnitInfo.builder()
                        .id(booking.getUnit().getId())
                        .name(booking.getUnit().getName())
                        .type(booking.getUnit().getType().name())
                        .build())
                .student(BookingResponse.StudentInfo.builder()
                        .id(booking.getStudent().getId())
                        .fullName(booking.getStudent().getFullName())
                        .email(booking.getStudent().getEmail())
                        .phone(booking.getStudent().getPhone())
                        .build())
                .status(booking.getStatus())
                .startDate(booking.getStartDate())
                .endDate(booking.getEndDate())
                .studentMessage(booking.getStudentMessage())
                .landlordResponse(booking.getLandlordResponse())
                .respondedAt(booking.getRespondedAt())
                .createdAt(booking.getCreatedAt())
                .build();
    }
}


package com.nextinnomind.campusnestbackend.service;

import com.nextinnomind.campusnestbackend.dto.payment.*;
import com.nextinnomind.campusnestbackend.entity.BookingRequest;
import com.nextinnomind.campusnestbackend.entity.Payment;
import com.nextinnomind.campusnestbackend.entity.User;
import com.nextinnomind.campusnestbackend.exception.BadRequestException;
import com.nextinnomind.campusnestbackend.exception.ResourceNotFoundException;
import com.nextinnomind.campusnestbackend.repository.BookingRequestRepository;
import com.nextinnomind.campusnestbackend.repository.PaymentRepository;
import com.nextinnomind.campusnestbackend.repository.UserRepository;
import com.nextinnomind.campusnestbackend.security.UserPrincipal;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRequestRepository bookingRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Value("${app.payment.stripe.secret-key}")
    private String stripeSecretKey;

    @Value("${app.payment.webhook-secret}")
    private String webhookSecret;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    public PaymentResponse createPayment(CreatePaymentRequest request, Long studentId) {
        BookingRequest booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!booking.getStudent().getId().equals(studentId)) {
            throw new BadRequestException("You can only create payments for your own bookings");
        }

        try {
            // Create Stripe PaymentIntent
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(request.getAmount().multiply(BigDecimal.valueOf(100)).longValue()) // Convert to cents
                    .setCurrency(request.getCurrency().toLowerCase())
                    .putMetadata("bookingId", booking.getId().toString())
                    .putMetadata("studentId", studentId.toString())
                    .build();

            PaymentIntent intent = PaymentIntent.create(params);

            // Create payment record
            Payment payment = Payment.builder()
                    .booking(booking)
                    .student(student)
                    .amount(request.getAmount())
                    .currency(request.getCurrency())
                    .paymentMethod(request.getPaymentMethod())
                    .providerTransactionId(intent.getId())
                    .description(request.getDescription())
                    .build();

            Payment savedPayment = paymentRepository.save(payment);

            log.info("Created payment: {} for booking: {}", savedPayment.getId(), booking.getId());

            return PaymentResponse.builder()
                    .id(savedPayment.getId())
                    .amount(savedPayment.getAmount())
                    .currency(savedPayment.getCurrency())
                    .status(savedPayment.getStatus().name())
                    .paymentMethod(savedPayment.getPaymentMethod())
                    .clientSecret(intent.getClientSecret())
                    .createdAt(savedPayment.getCreatedAt())
                    .build();

        } catch (StripeException e) {
            log.error("Failed to create payment intent");
            throw new BadRequestException("Failed to create payment: " + e.getMessage());
        }
    }

    public void handleWebhook(String payload, String signature) {
        // Handle Stripe webhook events
        try {
            // Verify webhook signature and process events
            log.info("Processing payment webhook");

            // Parse the payload and update payment status accordingly
            // This is a simplified implementation

        } catch (Exception e) {
            log.error("Failed to process webhook", e);
            throw new BadRequestException("Failed to process webhook");
        }
    }

    @Transactional(readOnly = true)
    public Page<PaymentResponse> getUserPayments(Long userId, Pageable pageable) {
        return paymentRepository.findByStudentId(userId, pageable)
                .map(this::convertToResponse);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(Long id, UserPrincipal userPrincipal) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        // Check access permissions
        boolean isStudent = payment.getStudent().getId().equals(userPrincipal.getId());
        boolean isLandlord = payment.getBooking().getProperty().getOwner().getId().equals(userPrincipal.getId());
        boolean isAdmin = userPrincipal.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        if (!isStudent && !isLandlord && !isAdmin) {
            throw new BadRequestException("You don't have permission to view this payment");
        }

        return convertToResponse(payment);
    }

    @Transactional(readOnly = true)
    public EarningsResponse getLandlordEarnings(Long landlordId, String period) {
        LocalDateTime startDate;
        LocalDateTime endDate = LocalDateTime.now();

        startDate = switch (period != null ? period.toLowerCase() : "month") {
            case "week" -> endDate.minusDays(7);
            case "year" -> endDate.minusYears(1);
            default -> // month
                    endDate.minusMonths(1);
        };

        BigDecimal totalEarnings = paymentRepository.getTotalEarningsByLandlord(
                landlordId, startDate, endDate);

        Page<Payment> payments = paymentRepository.findCompletedPaymentsByLandlord(
                landlordId, Pageable.unpaged());

        return EarningsResponse.builder()
                .totalEarnings(totalEarnings != null ? totalEarnings : BigDecimal.ZERO)
                .period(period)
                .paymentCount(payments.getTotalElements())
                .build();
    }

    private PaymentResponse convertToResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus().name())
                .paymentMethod(payment.getPaymentMethod())
                .description(payment.getDescription())
                .paidAt(payment.getPaidAt())
                .createdAt(payment.getCreatedAt())
                .build();
    }

    public PaymentResponse confirmPayment(Long id, String transactionId, Long id1) {
        return null;
    }
}
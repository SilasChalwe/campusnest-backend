package com.nextinnomind.campusnestbackend.dto.booking;

import com.nextinnomind.campusnestbackend.enums.BookingStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class BookingResponse {
    private Long id;
    private PropertyInfo property;
    private UnitInfo unit;
    private StudentInfo student;
    private BookingStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private String studentMessage;
    private String landlordResponse;
    private LocalDateTime respondedAt;
    private LocalDateTime createdAt;

    @Data
    @Builder
    public static class PropertyInfo {
        private Long id;
        private String title;
        private String address;
    }

    @Data
    @Builder
    public static class UnitInfo {
        private Long id;
        private String name;
        private String type;
    }

    @Data
    @Builder
    public static class StudentInfo {
        private Long id;
        private String fullName;
        private String email;
        private String phone;
    }
}

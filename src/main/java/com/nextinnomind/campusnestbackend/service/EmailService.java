//
//
//package com.nextinnomind.campusnestbackend.service;
//
//import com.nextinnomind.campusnestbackend.entity.BookingRequest;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.mail.SimpleMailMessage;
//import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.stereotype.Service;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class EmailService {
//
//    //@Autowired
//    private final JavaMailSender mailSender;
//
//    public void sendVerificationEmail(String to, String fullName, String verificationCode) {
//        try {
//            SimpleMailMessage message = new SimpleMailMessage();
//            message.setTo(to);
//            message.setSubject("CampusNest - Email Verification");
//            message.setText(String.format(
//                    "Hi %s,\n\n" +
//                            "Welcome to CampusNest! Please verify your email address using the code below:\n\n" +
//                            "Verification Code: %s\n\n" +
//                            "This code will expire in 24 hours.\n\n" +
//                            "Best regards,\n" +
//                            "The CampusNest Team",
//                    fullName, verificationCode
//            ));
//
//            mailSender.send(message);
//            log.info("Verification email sent to: {}", to);
//        } catch (Exception e) {
//            log.error("Failed to send verification email to: {}", to, e);
//        }
//    }
//
//    public void sendPasswordResetEmail(String to, String fullName, String resetToken) {
//        try {
//            SimpleMailMessage message = new SimpleMailMessage();
//            message.setTo(to);
//            message.setSubject("CampusNest - Password Reset");
//            message.setText(String.format(
//                    "Hi %s,\n\n" +
//                            "You requested a password reset for your CampusNest account.\n\n" +
//                            "Reset Code: %s\n\n" +
//                            "This code will expire in 1 hour.\n\n" +
//                            "If you didn't request this, please ignore this email.\n\n" +
//                            "Best regards,\n" +
//                            "The CampusNest Team",
//                    fullName, resetToken
//            ));
//
//            mailSender.send(message);
//            log.info("Password reset email sent to: {}", to);
//        } catch (Exception e) {
//            log.error("Failed to send password reset email to: {}", to, e);
//        }
//    }
//
//    public void sendBookingNotification(String email, String fullName, BookingRequest savedBooking) {
//    }
//
//    public void sendBookingRejectionNotification(String email, String fullName, BookingRequest savedBooking) {
//    }
//
//    public void sendBookingApprovalNotification(String email, String fullName, BookingRequest savedBooking) {
//
//    }
//}


package com.nextinnomind.campusnestbackend.service;

import com.nextinnomind.campusnestbackend.entity.BookingRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    // ---------------- Verification Email ----------------
    public void sendVerificationEmail(String to, String fullName, String verificationCode) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");

            String htmlMsg = "<!DOCTYPE html>"
                    + "<html><body style='font-family: Arial, sans-serif;'>"
                    + "<h2 style='color:#2E86C1;'>Welcome to CampusNest!</h2>"
                    + "<p>Hi " + fullName + ",</p>"
                    + "<p>Please verify your email address using the code below:</p>"
                    + "<h3 style='background-color:#F4F4F4; display:inline-block; padding:10px;'>"
                    + verificationCode + "</h3>"
                    + "<p style='color:red;'>This code will expire in 24 hours.</p>"
                    + "<br>"
                    + "<p>Best regards,<br>The CampusNest Team</p>"
                    + "</body></html>";

            helper.setTo(to);
            helper.setSubject("CampusNest - Email Verification");
            helper.setText(htmlMsg, true); // true = HTML
            helper.setFrom("CampusNest <info@campusnest.com>");

            mailSender.send(message);
            log.info("Verification email sent to: {}", to);

        } catch (MessagingException e) {
            log.error("Failed to send verification email to: {}", to, e);
        }
    }

    // ---------------- Password Reset Email ----------------
    public void sendPasswordResetEmail(String to, String fullName, String resetToken) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");

            String htmlMsg = "<!DOCTYPE html>"
                    + "<html><body style='font-family: Arial, sans-serif;'>"
                    + "<h2 style='color:#C0392B;'>Password Reset Request</h2>"
                    + "<p>Hi " + fullName + ",</p>"
                    + "<p>You requested a password reset for your CampusNest account.</p>"
                    + "<h3 style='background-color:#F4F4F4; display:inline-block; padding:10px;'>"
                    + resetToken + "</h3>"
                    + "<p style='color:red;'>This code will expire in 1 hour.</p>"
                    + "<p>If you didn't request this, please ignore this email.</p>"
                    + "<br>"
                    + "<p>Best regards,<br>The CampusNest Team</p>"
                    + "</body></html>";

            helper.setTo(to);
            helper.setSubject("CampusNest - Password Reset");
            helper.setText(htmlMsg, true);
            helper.setFrom("CampusNest <info@campusnest.com>");

            mailSender.send(message);
            log.info("Password reset email sent to: {}", to);

        } catch (MessagingException e) {
            log.error("Failed to send password reset email to: {}", to, e);
        }
    }

    // ---------------- Booking Notifications ----------------
    public void sendBookingNotification(String email, String fullName, BookingRequest savedBooking) {
        // TODO: Implement HTML notification for booking request
    }

    public void sendBookingRejectionNotification(String email, String fullName, BookingRequest savedBooking) {
        // TODO: Implement HTML notification for booking rejection
    }

    public void sendBookingApprovalNotification(String email, String fullName, BookingRequest savedBooking) {
        // TODO: Implement HTML notification for booking approval
    }
}

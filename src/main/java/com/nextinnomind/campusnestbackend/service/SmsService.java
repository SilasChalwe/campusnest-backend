
package com.nextinnomind.campusnestbackend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SmsService {

    public void sendVerificationSms(String phoneNumber, String verificationCode) {
        try {
            // Implement SMS sending logic using your preferred SMS provider
            // Example: Twilio, AWS SNS, etc.

            String message = String.format(
                    "CampusNest verification code: %s. This code expires in 1 hour.",
                    verificationCode
            );

            // TODO: Integrate with SMS provider
            log.info("SMS verification code sent to: {}", phoneNumber);
        } catch (Exception e) {
            log.error("Failed to send SMS to: {}", phoneNumber, e);
        }
    }

    public void sendBookingNotificationSms(String phoneNumber, String message) {
        try {
            // TODO: Implement SMS sending for booking notifications
            log.info("Booking notification SMS sent to: {}", phoneNumber);
        } catch (Exception e) {
            log.error("Failed to send booking notification SMS to: {}", phoneNumber, e);
        }
    }
}
package com.nextinnomind.campusnestbackend.service;

import com.nextinnomind.campusnestbackend.dto.auth.*;
import com.nextinnomind.campusnestbackend.entity.RefreshToken;
import com.nextinnomind.campusnestbackend.entity.User;
import com.nextinnomind.campusnestbackend.entity.VerificationToken;
import com.nextinnomind.campusnestbackend.exception.BadRequestException;
import com.nextinnomind.campusnestbackend.exception.UnauthorizedException;
import com.nextinnomind.campusnestbackend.repository.RefreshTokenRepository;
import com.nextinnomind.campusnestbackend.repository.UserRepository;
import com.nextinnomind.campusnestbackend.repository.VerificationTokenRepository;
import com.nextinnomind.campusnestbackend.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final SmsService smsService;

    @Value("${app.jwt.refresh-expiration}")
    private int refreshTokenExpirationMs;

    // Modified register method - returns a special response indicating verification needed
    public Map<String, Object> register(RegisterRequest request) {
        // Check if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email address already in use");
        }

        if (userRepository.existsByPhone(request.getPhone())) {
            throw new BadRequestException("Phone number already in use");
        }

        // Create new user with unverified email, phone verification optional
        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .emailVerified(false)
                .phoneVerified(true) // Phone is optional, so mark as verified by default
                .build();

        user = userRepository.save(user);
        log.info("User registered with ID: {}", user.getId());

        // Send only email verification
        sendEmailVerification(user);

        // Return registration response WITHOUT JWT tokens - indicate email verification needed
        Map<String, Object> response = new HashMap<>();
        response.put("userId", user.getId());
        response.put("message", "Registration successful! Please check your email for verification code.");
        response.put("emailSent", true);
        response.put("smsSent", false);
        response.put("verificationRequired", true);

        return response;
    }

    // Modified login method - enforce verification before allowing login
    public AuthResponse login(LoginRequest request) {
        // Authenticate user credentials first
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        // Check verification status before allowing login - only email required
        if (!user.getEmailVerified()) {
            throw new BadRequestException("Email verification required. Please verify your email address before logging in.");
        }

        // Phone verification is optional - no need to check

        // Update last login time
        userRepository.updateLastLoginTime(user.getId(), LocalDateTime.now());

        // Revoke existing refresh tokens
        refreshTokenRepository.revokeAllUserTokens(user.getId());

        // Generate new tokens ONLY if fully verified
        String accessToken = tokenProvider.generateAccessToken(
                user.getId(), user.getEmail(), user.getRole().name()
        );
        String refreshToken = createRefreshToken(user);

        log.info("User {} logged in successfully", user.getEmail());
        return buildAuthResponse(user, accessToken, refreshToken);
    }

    // Keep existing method signature but add verification logic
    public void verifyEmail(VerificationRequest request) {
        VerificationToken token = verificationTokenRepository
                .findByUserIdAndTypeAndUsedFalse(request.getUserId(), "EMAIL_VERIFICATION")
                .orElseThrow(() -> new BadRequestException("Invalid or expired verification token"));

        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Verification code has expired. Please request a new one.");
        }

        if (!token.getToken().equals(request.getCode())) {
            throw new BadRequestException("Invalid verification code");
        }

        // Mark email as verified
        userRepository.markEmailAsVerified(request.getUserId());
        token.setUsed(true);
        verificationTokenRepository.save(token);

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new BadRequestException("User not found"));

        log.info("Email verified for user: {}", user.getEmail());
    }

    // Keep existing method signature but add verification logic
    public void verifyPhone(VerificationRequest request) {
        VerificationToken token = verificationTokenRepository
                .findByUserIdAndTypeAndUsedFalse(request.getUserId(), "PHONE_VERIFICATION")
                .orElseThrow(() -> new BadRequestException("Invalid or expired verification token"));

        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Verification code has expired. Please request a new one.");
        }

        if (!token.getToken().equals(request.getCode())) {
            throw new BadRequestException("Invalid verification code");
        }

        // Mark phone as verified
        userRepository.markPhoneAsVerified(request.getUserId());
        token.setUsed(true);
        verificationTokenRepository.save(token);

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new BadRequestException("User not found"));

        log.info("Phone verified for user: {}", user.getEmail());
    }

    // Add method to check verification status (email only required)
    public Map<String, Object> getVerificationStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));

        Map<String, Object> status = new HashMap<>();
        status.put("userId", userId);
        status.put("emailVerified", user.getEmailVerified());
        status.put("phoneVerified", user.getPhoneVerified());
        status.put("allVerified", user.getEmailVerified()); // Only email required for login

        return status;
    }

    // Add method to resend email verification
    public void resendEmailVerification(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));

        if (user.getEmailVerified()) {
            throw new BadRequestException("Email is already verified");
        }

        // Invalidate existing email verification tokens
        verificationTokenRepository.invalidateUserTokensByType(userId, "EMAIL_VERIFICATION");

        // Send new verification code
        sendEmailVerification(user);
        log.info("Email verification code resent for user: {}", user.getEmail());
    }

    // Add method to resend phone verification
    public void resendPhoneVerification(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));

        if (user.getPhoneVerified()) {
            throw new BadRequestException("Phone is already verified");
        }

        // Invalidate existing phone verification tokens
        verificationTokenRepository.invalidateUserTokensByType(userId, "PHONE_VERIFICATION");

        // Send new verification code
        sendPhoneVerification(user);
        log.info("Phone verification code resent for user: {}", user.getEmail());
    }

    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if (refreshToken.getRevoked() || refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new UnauthorizedException("Refresh token expired");
        }

        User user = refreshToken.getUser();

        // Generate new access token
        String accessToken = tokenProvider.generateAccessToken(
                user.getId(), user.getEmail(), user.getRole().name()
        );

        // Generate new refresh token
        String newRefreshToken = createRefreshToken(user);
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        return buildAuthResponse(user, accessToken, newRefreshToken);
    }

    private String createRefreshToken(User user) {
        String token = UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now().plusSeconds(refreshTokenExpirationMs / 1000);

        RefreshToken refreshToken = RefreshToken.builder()
                .token(token)
                .user(user)
                .expiresAt(expiryDate)
                .build();

        refreshTokenRepository.save(refreshToken);
        return token;
    }

    private void sendEmailVerification(User user) {
        String code = generateVerificationCode();

        VerificationToken token = VerificationToken.builder()
                .token(code)
                .type("EMAIL_VERIFICATION")
                .user(user)
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build();

        verificationTokenRepository.save(token);

        try {
            emailService.sendVerificationEmail(user.getEmail(), user.getFullName(), code);
            log.info("Email verification code sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send email verification to: {}", user.getEmail(), e);
            throw new BadRequestException("Failed to send verification email");
        }
    }

    private void sendPhoneVerification(User user) {
        String code = generateVerificationCode();

        VerificationToken token = VerificationToken.builder()
                .token(code)
                .type("PHONE_VERIFICATION")
                .user(user)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .build();

        verificationTokenRepository.save(token);

        try {
            smsService.sendVerificationSms(user.getPhone(), code);
            log.info("SMS verification code sent to: {}", user.getPhone());
        } catch (Exception e) {
            log.error("Failed to send SMS verification to: {}", user.getPhone(), e);
            throw new BadRequestException("Failed to send verification SMS");
        }
    }

    private String generateVerificationCode() {
        return String.format("%06d", new Random().nextInt(999999));
    }

    private AuthResponse buildAuthResponse(User user, String accessToken, String refreshToken) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(tokenProvider.getExpirationDateFromToken(accessToken).getTime())
                .user(AuthResponse.UserInfo.builder()
                        .id(user.getId())
                        .fullName(user.getFullName())
                        .email(user.getEmail())
                        .phone(user.getPhone())
                        .role(user.getRole().name())
                        .emailVerified(user.getEmailVerified())
                        .phoneVerified(user.getPhoneVerified())
                        .profilePictureUrl(user.getProfilePictureUrl())
                        .build())
                .build();
    }
}
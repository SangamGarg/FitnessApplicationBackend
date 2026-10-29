package com.fitnessapp.appAuth.service;

import com.fitnessapp.appAuth.model.dtos.requestDto.OtpRequestDto;
import com.fitnessapp.appAuth.model.dtos.requestDto.ResetPasswordWithOtpRequestDto;
import com.fitnessapp.appAuth.model.dtos.requestDto.VerifyEmailOtpRequestDto;
import com.fitnessapp.appAuth.model.dtos.responseDto.OtpActionResponseDto;
import com.fitnessapp.appAuth.model.dtos.responseDto.UserRegisterResponseDto;
import com.fitnessapp.appAuth.model.entity.OtpEntity;
import com.fitnessapp.appAuth.repo.OtpRepo;
import com.fitnessapp.appAuth.repo.UserRepo;
import com.fitnessapp.utils.appUtil.AppResponseStatus;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Locale;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppOtpService {
    private static final String SIGNUP = "SIGNUP";
    private static final String FORGOT_PASSWORD = "FORGOT_PASSWORD";
    private static final int OTP_DIGITS = 6;
    private static final int MAX_VERIFY_ATTEMPTS = 5;
    private static final int MAX_SENDS_PER_WINDOW = 5;
    private static final Duration SEND_WINDOW = Duration.ofHours(1);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final OtpRepo otpRepo;
    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final AppAuthEmailService appAuthEmailService;

    @Value("${app.auth.otp.expiry-seconds:600}")
    private long otpExpirySeconds;

    @Value("${app.auth.otp.resend-cooldown-seconds:60}")
    private long resendCooldownSeconds;

    @Value("${app.auth.otp.reset-token-expiry-seconds:600}")
    private long resetTokenExpirySeconds;

    @Transactional
    public ResponseEntity<OtpActionResponseDto> sendSignupOtp(OtpRequestDto request) {
        String email = normalizeEmail(request.getEmail());
        if (userRepo.existsByEmailIgnoreCase(email)) {
            return response(HttpStatus.CONFLICT, false, "EMAIL_ALREADY_REGISTERED",
                    "An account with this email already exists.", null, null);
        }
        return sendOtp(email, SIGNUP, "Verify your Muscle Play email", "Use this code to verify your email address and continue creating your account.");
    }

    @Transactional
    public ResponseEntity<OtpActionResponseDto> verifySignupOtp(VerifyEmailOtpRequestDto request) {
        String email = normalizeEmail(request.getEmail());
        OtpEntity challenge = otpRepo.findForUpdate(email, SIGNUP).orElse(null);
        ResponseEntity<OtpActionResponseDto> invalid = validateOtpChallenge(challenge, request.getOtp());
        if (invalid != null) {
            return invalid;
        }
        Objects.requireNonNull(challenge).setVerifiedAt(Instant.now());
        challenge.setOtpHash(null);
        otpRepo.save(challenge);
        return response(HttpStatus.OK, true, "EMAIL_VERIFIED", "Email verified successfully. You can now continue sign-up.", null, null);
    }

    @Transactional
    public ResponseEntity<OtpActionResponseDto> sendForgotPasswordOtp(OtpRequestDto request) {
        String email = normalizeEmail(request.getEmail());
        // Deliberately returns the requested explicit result for the mobile UX.
        if (!userRepo.existsByEmailIgnoreCase(email)) {
            return response(HttpStatus.NOT_FOUND, false, "USER_NOT_FOUND", "User does not exist.", null, null);
        }
        return sendOtp(email, FORGOT_PASSWORD, "Reset your Muscle Play password", "Use this code to verify your identity before resetting your password.");
    }

    @Transactional
    public ResponseEntity<OtpActionResponseDto> verifyForgotPasswordOtp(VerifyEmailOtpRequestDto request) {
        String email = normalizeEmail(request.getEmail());
        if (!userRepo.existsByEmailIgnoreCase(email)) {
            return response(HttpStatus.NOT_FOUND, false, "USER_NOT_FOUND", "User does not exist.", null, null);
        }

        OtpEntity challenge = otpRepo.findForUpdate(email, FORGOT_PASSWORD).orElse(null);
        ResponseEntity<OtpActionResponseDto> invalid = validateOtpChallenge(challenge, request.getOtp());
        if (invalid != null) {
            return invalid;
        }

        String resetToken = generateVerificationToken();
        Objects.requireNonNull(challenge).setVerifiedAt(Instant.now());
        challenge.setOtpHash(null);
        challenge.setVerificationTokenHash(passwordEncoder.encode(resetToken));
        challenge.setVerificationTokenExpiresAt(Instant.now().plusSeconds(resetTokenExpirySeconds));
        challenge.setConsumedAt(null);
        otpRepo.save(challenge);
        return response(HttpStatus.OK, true, "OTP_VERIFIED", "OTP verified. Use the verification token to set a new password.", resetTokenExpirySeconds, resetToken);
    }

    @Transactional
    public ResponseEntity<OtpActionResponseDto> resetPassword(ResetPasswordWithOtpRequestDto request) {
        String email = normalizeEmail(request.getEmail());
        if (!userRepo.existsByEmailIgnoreCase(email)) {
            return response(HttpStatus.NOT_FOUND, false, "USER_NOT_FOUND", "User does not exist.", null, null);
        }

        OtpEntity challenge = otpRepo.findForUpdate(email, FORGOT_PASSWORD).orElse(null);
        Instant now = Instant.now();
        if (challenge == null || challenge.getVerificationTokenHash() == null
                || challenge.getVerificationTokenExpiresAt() == null
                || !challenge.getVerificationTokenExpiresAt().isAfter(now)
                || challenge.getConsumedAt() != null
                || !passwordEncoder.matches(request.getVerificationToken(), challenge.getVerificationTokenHash())) {
            return response(HttpStatus.UNAUTHORIZED, false, "INVALID_RESET_TOKEN", "The password reset verification token is invalid or expired.", null, null);
        }

        try {
            UserRecord firebaseUser = FirebaseAuth.getInstance().getUserByEmail(email);
            FirebaseAuth.getInstance().updateUser(new UserRecord.UpdateRequest(firebaseUser.getUid())
                    .setPassword(request.getNewPassword()));
            FirebaseAuth.getInstance().revokeRefreshTokens(firebaseUser.getUid());
            challenge.setConsumedAt(now);
            challenge.setVerificationTokenHash(null);
            challenge.setVerificationTokenExpiresAt(null);
            otpRepo.saveAndFlush(challenge);
            return response(HttpStatus.OK, true, "PASSWORD_RESET", "Password reset successfully. Please sign in with your new password.", null, null);
        } catch (FirebaseAuthException ex) {
            // Do not expose Firebase's internal error details or log user credentials.
            log.warn("Firebase password reset failed for a verified account");
            return response(HttpStatus.BAD_GATEWAY, false, "PASSWORD_RESET_FAILED", "Unable to reset the password right now. Please try again.", null, null);
        }
    }

    /** Used by the Firebase token-exchange flow before creating a new backend user. */
    @Transactional
    public boolean hasVerifiedSignupEmail(String emailAddress) {
        String email = normalizeEmail(emailAddress);
        OtpEntity challenge = otpRepo.findForUpdate(email, SIGNUP).orElse(null);
        return challenge != null
                && challenge.getVerifiedAt() != null
                && challenge.getConsumedAt() == null
                && challenge.getExpiresAt() != null
                && challenge.getExpiresAt().isAfter(Instant.now());
    }

    /** Consume the signup proof after the backend user row has been persisted. */
    @Transactional
    public boolean consumeSignupVerification(String emailAddress) {
        String email = normalizeEmail(emailAddress);
        OtpEntity challenge = otpRepo.findForUpdate(email, SIGNUP).orElse(null);
        Instant now = Instant.now();
        if (challenge == null || challenge.getVerifiedAt() == null || challenge.getConsumedAt() != null
                || challenge.getExpiresAt() == null || !challenge.getExpiresAt().isAfter(now)) {
            return false;
        }
        challenge.setConsumedAt(now);
        otpRepo.save(challenge);
        return true;
    }

    private ResponseEntity<OtpActionResponseDto> sendOtp(String email, String purpose, String subject, String explanation) {
        Instant now = Instant.now();
        OtpEntity challenge = otpRepo.findForUpdate(email, purpose).orElseGet(() -> {
            OtpEntity created = new OtpEntity();
            created.setEmail(email);
            created.setPurpose(purpose);
            created.setAttemptCount(0);
            created.setSendCount(0);
            return created;
        });

        if (challenge.getLastSentAt() != null && challenge.getLastSentAt().plusSeconds(resendCooldownSeconds).isAfter(now)) {
            long retryAfter = Math.max(1, Duration.between(now, challenge.getLastSentAt().plusSeconds(resendCooldownSeconds)).toSeconds());
            return response(HttpStatus.TOO_MANY_REQUESTS, false, "OTP_RESEND_COOLDOWN",
                    "Please wait before requesting another OTP.", retryAfter, null);
        }

        if (challenge.getWindowStartedAt() == null || !challenge.getWindowStartedAt().plus(SEND_WINDOW).isAfter(now)) {
            challenge.setWindowStartedAt(now);
            challenge.setSendCount(0);
        }
        if (challenge.getSendCount() >= MAX_SENDS_PER_WINDOW) {
            return response(HttpStatus.TOO_MANY_REQUESTS, false, "OTP_RATE_LIMITED",
                    "Too many OTP requests. Please try again later.", SEND_WINDOW.toSeconds(), null);
        }

        String otp = generateOtp();
        challenge.setOtpHash(passwordEncoder.encode(otp));
        challenge.setExpiresAt(now.plusSeconds(otpExpirySeconds));
        challenge.setAttemptCount(0);
        challenge.setLastSentAt(now);
        challenge.setSendCount(challenge.getSendCount() + 1);
        challenge.setVerifiedAt(null);
        challenge.setVerificationTokenHash(null);
        challenge.setVerificationTokenExpiresAt(null);
        challenge.setConsumedAt(null);
        otpRepo.saveAndFlush(challenge);

        try {
            appAuthEmailService.sendOtpMail(email, subject, explanation, otp, otpExpirySeconds);
        } catch (MessagingException | RuntimeException ex) {
            // Invalidate a code that may not have been delivered.
            challenge.setOtpHash(null);
            challenge.setExpiresAt(now);
            otpRepo.save(challenge);
            log.error("OTP email delivery failed; recipient details and OTP were not logged : {}", ex.getMessage());
            return response(HttpStatus.SERVICE_UNAVAILABLE, false, "OTP_EMAIL_UNAVAILABLE",
                    "Unable to send the verification email right now. Please try again later.", null, null);
        }

        return response(HttpStatus.OK, true, "OTP_SENT", "A verification code has been sent to your email address.", otpExpirySeconds, null);
    }

    private ResponseEntity<OtpActionResponseDto> validateOtpChallenge(OtpEntity challenge, String submittedOtp) {
        Instant now = Instant.now();
        if (challenge == null || challenge.getOtpHash() == null || challenge.getExpiresAt() == null
                || !challenge.getExpiresAt().isAfter(now) || challenge.getConsumedAt() != null) {
            return response(HttpStatus.BAD_REQUEST, false, "OTP_INVALID_OR_EXPIRED", "The OTP is invalid or has expired. Request a new one.", null, null);
        }
        if (challenge.getAttemptCount() >= MAX_VERIFY_ATTEMPTS) {
            return response(HttpStatus.TOO_MANY_REQUESTS, false, "OTP_ATTEMPTS_EXCEEDED", "Too many incorrect attempts. Request a new OTP.", null, null);
        }
        if (!passwordEncoder.matches(submittedOtp, challenge.getOtpHash())) {
            challenge.setAttemptCount(challenge.getAttemptCount() + 1);
            otpRepo.save(challenge);
            if (challenge.getAttemptCount() >= MAX_VERIFY_ATTEMPTS) {
                return response(HttpStatus.TOO_MANY_REQUESTS, false, "OTP_ATTEMPTS_EXCEEDED", "Too many incorrect attempts. Request a new OTP.", null, null);
            }
            return response(HttpStatus.BAD_REQUEST, false, "OTP_INCORRECT", "The OTP you entered is incorrect.", null, null);
        }
        return null;
    }


    private String generateOtp() {
        int value = SECURE_RANDOM.nextInt((int) Math.pow(10, OTP_DIGITS));
        return String.format(Locale.ROOT, "%06d", value);
    }

    private String generateVerificationToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private ResponseEntity<OtpActionResponseDto> response(HttpStatus statusCode, boolean success, String code,
                                                          String message, Long expiresInSeconds, String verificationToken) {
        return ResponseEntity.status(statusCode).body(OtpActionResponseDto.builder().status(success ? AppResponseStatus.SUCCESS.name() : AppResponseStatus.FAILED.name()).statusCode(statusCode.value()).code(code)
                .message(message)
                .expiresInSeconds(expiresInSeconds)
                .verificationToken(verificationToken)
                .build());
    }

}

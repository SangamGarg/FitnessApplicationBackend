package com.fitnessapp.appAuth.service;

import com.fitnessapp.appAuth.model.dtos.UserDetailsDto;
import com.fitnessapp.appAuth.model.dtos.requestDto.RefreshTokenRequestDto;
import com.fitnessapp.appAuth.model.dtos.requestDto.UserPatchRequestDto;
import com.fitnessapp.appAuth.model.dtos.responseDto.RefreshTokenResponseDto;
import com.fitnessapp.appAuth.model.dtos.responseDto.UserDetailsResponseDto;
import com.fitnessapp.appAuth.model.dtos.responseDto.UserRegisterResponseDto;
import com.fitnessapp.appAuth.model.entity.*;
import com.fitnessapp.appAuth.model.enums.MobileAccountStatus;
import com.fitnessapp.appAuth.repo.RefreshTokenRepo;
import com.fitnessapp.appAuth.repo.UserDetailsRepo;
import com.fitnessapp.appAuth.repo.UserRepo;
import com.fitnessapp.appAuth.utils.AppAuthUtils;
import com.fitnessapp.calculator.service.CalculatorService;
import com.fitnessapp.config.springSecurityConfig.jwt.jwtService.JwtTokenService;
import com.fitnessapp.utils.appUtil.AppResponseStatus;
import com.google.firebase.auth.AuthErrorCode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Collections;
import java.util.HexFormat;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppAuthServiceImpl implements AppAuthService {

    private static final String DEFAULT_MAIL_NAME = "Fitness Freak";

    private final UserRepo userRepo;
    private final UserDetailsRepo userDetailsRepo;
    private final JwtTokenService jwtTokenService;
    private final CalculatorService calculatorService;
    private final RefreshTokenRepo refreshTokenRepo;
    private final AppAuthEmailService appAuthEmailService;
    private final AppOtpService appOtpService;

    // ------------------------------------------------------------------ login / register

    @Override
    public ResponseEntity<UserRegisterResponseDto> loginOrRegisterUser(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ") || authorizationHeader.substring(7).isBlank()) {
            log.warn("Authorization header is missing, invalid, or empty");
            return errorResponseRegister(HttpStatus.UNAUTHORIZED, "Verification failed: Kindly Try Again.");
        }
        final FirebaseToken decodedToken;
        try {
            decodedToken = FirebaseAuth.getInstance().verifyIdToken(authorizationHeader.substring(7).trim());
        } catch (FirebaseAuthException | IllegalArgumentException ex) {
            // Never log credentials or token contents.
            log.warn("Firebase ID token verification failed: Unable to Decode {}", ex.getMessage());
            return errorResponseRegister(HttpStatus.UNAUTHORIZED, "Verification failed: Kindly Try Again.");
        }

        String uid = decodedToken.getUid();
        String email = decodedToken.getEmail();
        if (uid == null || uid.isBlank() || email == null || email.isBlank()) {
            log.warn("Firebase token verification failed: Missing UID or email claim");
            return errorResponseRegister(HttpStatus.UNAUTHORIZED, "Verification failed: Kindly Try Again.");
        }

        UserEntity user;
        boolean isNewUser = false;
        try {
            user = userRepo.findById(uid).orElse(null);
            if (user == null) {
                String normalizedEmail = email.trim().toLowerCase(java.util.Locale.ROOT);
                // New backend accounts may only be created after the email OTP was verified.

//make sure your Signup flow calls these endpoints in the correct order:
//1. POST /api/v1/app/auth/otp/signup/send
//2. POST /api/v1/app/auth/otp/signup/verify
//3. Complete Firebase signup, obtain the Firebase ID token, and call POST /loginRegister.

                if (!appOtpService.hasVerifiedSignupEmail(normalizedEmail)) {
                    return errorResponseRegister(HttpStatus.FORBIDDEN, "Please verify your email first or signup");
                }

                UserEntity appUser = new UserEntity();
                appUser.setId(uid); // Firebase UID is the stable primary key.
                appUser.setEmail(normalizedEmail);
                appUser.setAccountStatus(MobileAccountStatus.ACTIVE);
                try {
                    user = userRepo.saveAndFlush(appUser); // persist before issuing tokens
                    isNewUser = true;
                    if (!appOtpService.consumeSignupVerification(normalizedEmail)) {
                        // If the verification expired or was consumed concurrently, don't leave a new user behind.
                        userRepo.deleteById(uid);
                        userRepo.flush();
                        return errorResponseRegister(HttpStatus.FORBIDDEN, "Verification Failed: Kindly sign up again");
                    }
                } catch (DataIntegrityViolationException concurrentSignup) {
                    // A concurrent request may have inserted the same Firebase UID or email.
                    user = userRepo.findById(uid).orElse(null);
                    if (user == null) {
                        log.warn("Signup uniqueness conflict could not be resolved for Firebase UID");
                        return errorResponseRegister(HttpStatus.CONFLICT, "Signup uniqueness conflict could not be resolved for UID");
                    }
                }
            }
        } catch (DataIntegrityViolationException conflict) {
            return errorResponseRegister(HttpStatus.CONFLICT, "User Already Exists");
        } catch (Exception ex) {
            log.error("Could not persist user during Firebase token exchange", ex);
            return errorResponseRegister(HttpStatus.INTERNAL_SERVER_ERROR, "Something Went Wrong : Could not persist user during Firebase token exchange");
        }

        if (user.getAccountStatus() != MobileAccountStatus.ACTIVE) {
            return errorResponseRegister(HttpStatus.FORBIDDEN, "Account is Suspended or deactivated");
        }

        try {
            UserDetailsEntity details = userDetailsRepo.findByUserId(uid);
            String accessToken = jwtTokenService.generateToken(uid, user.getEmail());
            String refreshToken = jwtTokenService.generateRefreshToken(uid, user.getEmail());
            String familyId = UUID.randomUUID().toString();
            persistRefreshToken(uid, refreshToken, familyId);

            if (isNewUser) {
                // Async mail failures must never fail the token exchange.
                String mailName = decodedToken.getName();
                appAuthEmailService.sendWelcomeEmail((mailName == null || mailName.isBlank()) ? DEFAULT_MAIL_NAME : mailName, user.getEmail());
            }

            return ResponseEntity.ok(UserRegisterResponseDto.builder().status(AppResponseStatus.SUCCESS.name()).statusCode(HttpStatus.OK.value()).message("Authentication Successful").accessToken(accessToken).refreshToken(refreshToken).isNewUser(isNewUser).profileComplete(details != null).userDetailsDto(details == null ? null : AppAuthUtils.userDetailsEntityToUserDetailsDto(details)).build());
        } catch (Exception ex) {
            log.error("Token exchange failed after user persistence", ex);
            return errorResponseRegister(HttpStatus.INTERNAL_SERVER_ERROR, "Something Went Wrong : Token exchange failed");
        }
    }

    // ------------------------------------------------------------------ refresh token rotation

    @Override
    @Transactional
    public ResponseEntity<RefreshTokenResponseDto> refreshToken(RefreshTokenRequestDto request) {
        String rawToken = request == null ? null : request.getRefreshToken();
        if (rawToken == null || rawToken.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            String uid = jwtTokenService.extractId(rawToken);
            String tokenHash = sha256(rawToken);
            RefreshTokenEntity stored = refreshTokenRepo.findByTokenHashForUpdate(tokenHash).orElse(null);
            if (stored == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            Instant now = Instant.now();
            if (stored.getRevokedAt() != null || stored.getConsumedAt() != null) {
                // Reuse of a previously rotated token revokes all tokens in that family.
                refreshTokenRepo.findByFamilyId(stored.getFamilyId()).forEach(t -> t.setRevokedAt(now));
                refreshTokenRepo.flush();
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            if (!stored.getUserId().equals(uid) || !stored.getExpiresAt().isAfter(now)) {
                stored.setRevokedAt(now);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            UserEntity user = userRepo.findById(uid).orElse(null);
            if (user == null || user.getAccountStatus() != MobileAccountStatus.ACTIVE) {
                stored.setRevokedAt(now);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            UserDetails principal = new org.springframework.security.core.userdetails.User(user.getId(), "", true, true, true, true, Collections.emptyList());
            if (!jwtTokenService.isRefreshTokenValid(rawToken, principal)) {
                stored.setRevokedAt(now);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            String newAccess = jwtTokenService.generateToken(uid, user.getEmail());
            String newRefresh = jwtTokenService.generateRefreshToken(uid, user.getEmail());
            String newHash = sha256(newRefresh);
            stored.setConsumedAt(now);
            stored.setReplacedByHash(newHash);
            refreshTokenRepo.save(stored);
            persistRefreshToken(uid, newRefresh, stored.getFamilyId());

            return ResponseEntity.ok(RefreshTokenResponseDto.builder().status(AppResponseStatus.SUCCESS.name()).statusCode(HttpStatus.CREATED.value()).message("Refresh Token Send Successful").accessToken(newAccess).refreshToken(newRefresh).build());
        } catch (JwtException | IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(RefreshTokenResponseDto.builder().status(AppResponseStatus.FAILED.name()).statusCode(HttpStatus.UNAUTHORIZED.value()).message(ex.getLocalizedMessage()).accessToken(null).refreshToken(null).build());
        } catch (Exception ex) {
            log.error("Refresh token rotation failed", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(RefreshTokenResponseDto.builder().status(AppResponseStatus.FAILED.name()).statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value()).message("Internal Server Error: Refresh token rotation failed").accessToken(null).refreshToken(null).build());
        }
    }

    @Override
    @Transactional
    public ResponseEntity<Void> logout(RefreshTokenRequestDto request, boolean logoutEverywhere) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null || "anonymousUser".equals(authentication.getName()) || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String uid = authentication.getName();
        Instant now = Instant.now();

        try {
            if (logoutEverywhere) {
                // Revoke all currently active refresh-token records for this user.
                refreshTokenRepo.findActiveByUserIdForUpdate(uid).forEach(token -> token.setRevokedAt(now));
                refreshTokenRepo.flush();
                return ResponseEntity.noContent().build();
            }

            String rawToken = request == null ? null : request.getRefreshToken();
            if (rawToken == null || rawToken.isBlank()) {
                // Do not silently turn a single-device logout into logout-everywhere.
                return ResponseEntity.badRequest().build();
            }

            String tokenHash = sha256(rawToken);
            RefreshTokenEntity stored = refreshTokenRepo.findByTokenHashForUpdate(tokenHash).orElse(null);

            // Idempotent response: don't disclose whether a token exists or belongs to
            // another account. Only revoke it when it belongs to the authenticated user.
            if (stored != null && uid.equals(stored.getUserId())) {
                // A refresh-token family represents one app session/device. Revoke every
                // token in that family, including consumed predecessors and the latest token.
                refreshTokenRepo.findByFamilyIdForUpdate(stored.getFamilyId()).forEach(token -> {
                    if (token.getRevokedAt() == null) {
                        token.setRevokedAt(now);
                    }
                });
                refreshTokenRepo.flush();
            }

            return ResponseEntity.noContent().build();
        } catch (Exception ex) {
            log.error("Logout refresh-token revocation failed for authenticated user");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private void persistRefreshToken(String userId, String rawToken, String familyId) {
        Instant now = Instant.now();
        refreshTokenRepo.saveAndFlush(RefreshTokenEntity.builder().userId(userId).tokenHash(sha256(rawToken)).familyId(familyId).createdAt(now).expiresAt(now.plusMillis(jwtRefreshTtlMillis())).build());
    }

    private long jwtRefreshTtlMillis() {
        // Refresh token expiration is validated by JwtTokenService; keep DB expiry conservative.
        return jwtTokenService.getRefreshExpirationMs();
    }

    private String sha256(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256 is unavailable", impossible);
        }
    }

    // ------------------------------------------------------------------ user details

    @Override
    public ResponseEntity<UserDetailsResponseDto> getUserDetails() {
        try {
            String id = AppAuthUtils.getUserIdFromTokenInHeader();
            UserEntity userEntity = userRepo.findById(id).orElse(null);
            if (userEntity == null) {
                return errorResponseDetails(HttpStatus.NOT_FOUND, "User Not Found with given id");
            }
            UserDetailsEntity user = userDetailsRepo.findByUserId(id);
            if (user != null) {
                UserDetailsDto userDetailsDto = AppAuthUtils.userDetailsEntityToUserDetailsDto(user);

                UserDetailsResponseDto response = UserDetailsResponseDto.builder().status(AppResponseStatus.SUCCESS.name()).statusCode(HttpStatus.OK.value()).message("Details Updated Successfully").userDetailsDto(userDetailsDto).build();

                return ResponseEntity.ok(response);

            } else {
                return errorResponseDetails(HttpStatus.NOT_FOUND, "User Not Found with given id");
            }
        } catch (Exception e) {
            return errorResponseDetails(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error: " + e.getLocalizedMessage());
        }
    }

    @Transactional
    @Override
    public ResponseEntity<UserDetailsResponseDto> postUserDetails(UserDetailsDto dto) {
        try {
            String id = AppAuthUtils.getUserIdFromTokenInHeader();
            // Fetch user from DB
            UserEntity user = userRepo.findById(id).orElse(null);
            if (user == null) {
                return errorResponseDetails(HttpStatus.NOT_FOUND, "User Not Found with given id");
            }
            // Fetch existing user details
            UserDetailsEntity existingUserDetails = userDetailsRepo.findByUserId(id);

            if (existingUserDetails != null && existingUserDetails.getUser().getId().equals(user.getId())) {
                log.warn("User details already exist");
                return errorResponseDetails(HttpStatus.CONFLICT, "User details already exist");
            }


            // Update user details
            UserDetailsEntity userEntity = UserDetailsEntity.builder().name(dto.getName()).email(user.getEmail()).gender(dto.getGender()).age(dto.getAge()).profileImageUrl(dto.getProfileImageUrl()).heightCm(dto.getHeightCm()).weightKg(dto.getWeightKg()).activityLevel(dto.getActivityLevel()).goal(dto.getGoal()).user(user)
//                    .hipCm(dto.getHipCm()).neckCm(dto.getNeckCm()).waistCm(dto.getWaistCm())
                    .build();

            UserDetailsEntity saved = userDetailsRepo.save(userEntity);


            log.info("User details created for user {}", id);

            dto.setEmail(user.getEmail());

            UserDetailsResponseDto response = UserDetailsResponseDto.builder().status(AppResponseStatus.SUCCESS.name()).statusCode(HttpStatus.OK.value()).message("Details Updated Successfully").userDetailsDto(dto).build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return errorResponseDetails(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error: " + e.getLocalizedMessage());
        }
    }

    @Transactional
    @Override
    public ResponseEntity<UserDetailsResponseDto> patchUserDetails(UserPatchRequestDto userDetailsDto) {
        try {
            String id = AppAuthUtils.getUserIdFromTokenInHeader();

            // Fetch existing user details
            UserDetailsEntity existingUserDetails = userDetailsRepo.findByUserId(id);

            if (existingUserDetails == null) {
                log.warn("User Not Found with given id");
                return errorResponseDetails(HttpStatus.NOT_FOUND, "User Not Found with given id");
            }

            UserDetailsEntity saved = userDetailsRepo.save(Objects.requireNonNull(AppAuthUtils.patchLogic(userDetailsDto, existingUserDetails)));


            log.info("User details updated for user {}", id);

            // Map back updated data to DTO
            UserDetailsDto updatedDto = AppAuthUtils.userDetailsEntityToUserDetailsDto(saved);

            UserDetailsResponseDto response = UserDetailsResponseDto.builder().status(AppResponseStatus.SUCCESS.name()).statusCode(HttpStatus.OK.value()).message("Details Updated Successfully").userDetailsDto(updatedDto).build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.warn("Internal Server Error: {}", e.getLocalizedMessage());
            return errorResponseDetails(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error: " + e.getLocalizedMessage());
        }
    }

    // ------------------------------------------------------------------ delete account

    @Override
    @Transactional
    public ResponseEntity<Void> deleteUser() {
        String uid = AppAuthUtils.getUserIdFromTokenInHeader();
        UserEntity user = userRepo.findById(uid).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        // Remove application rows in the transaction first, then delete the external Firebase
        // identity last. If Firebase deletion fails, throwing rolls back the database transaction
        // so the client can retry with its still-valid identity.
        refreshTokenRepo.deleteByUserId(uid);
        userRepo.delete(user);
        userRepo.flush();
        try {
            FirebaseAuth.getInstance().deleteUser(uid);
        } catch (FirebaseAuthException ex) {
            if (ex.getAuthErrorCode() != AuthErrorCode.USER_NOT_FOUND) {
                log.error("Firebase account deletion failed; database transaction will roll back");
//                throw new IllegalStateException("Account deletion could not be completed; retry later");
            }
        }

        log.info("Mobile account deleted");
        return ResponseEntity.noContent().build();
    }

    // ------------------------------------------------------------------ helpers

    private ResponseEntity<UserRegisterResponseDto> errorResponseRegister(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(UserRegisterResponseDto.builder().status(AppResponseStatus.FAILED.name()).statusCode(status.value()).message(message).build());
    }

    private ResponseEntity<UserDetailsResponseDto> errorResponseDetails(HttpStatus status, String message) {
        log.warn(message);
        return ResponseEntity.status(status).body(UserDetailsResponseDto.builder().status(AppResponseStatus.FAILED.name()).statusCode(status.value()).message(message).build());
    }
}
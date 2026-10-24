package com.fitnessapp.userDetailsAndAuthService.services.userService.userServiceImpl;

import com.fitnessapp.config.springSecurityConfig.jwt.jwtService.JwtTokenService;
import com.fitnessapp.calculator.service.CalculatorService;
import com.fitnessapp.userDetailsAndAuthService.models.dtos.ErrorDtoUserDetailsAndAuth;
import com.fitnessapp.userDetailsAndAuthService.models.dtos.UserDetailsDto;
import com.fitnessapp.userDetailsAndAuthService.models.dtos.requestDtos.RefreshTokenRequestDto;
import com.fitnessapp.userDetailsAndAuthService.models.dtos.requestDtos.UserLoginOrRegisterRequestDto;
import com.fitnessapp.userDetailsAndAuthService.models.dtos.requestDtos.UserPatchRequestDto;
import com.fitnessapp.userDetailsAndAuthService.models.dtos.responseDtos.RefreshTokenResponseDto;
import com.fitnessapp.userDetailsAndAuthService.models.dtos.responseDtos.UserDetailsResponseDto;
import com.fitnessapp.userDetailsAndAuthService.models.dtos.responseDtos.UserRegisterResponseDto;
import com.fitnessapp.userDetailsAndAuthService.models.entitites.UserBodyDetails;
import com.fitnessapp.userDetailsAndAuthService.models.entitites.UserDetailsEntity;
import com.fitnessapp.userDetailsAndAuthService.models.entitites.UserEntity;
import com.fitnessapp.userDetailsAndAuthService.repository.UserDetailAndAuthRepository;
import com.fitnessapp.userDetailsAndAuthService.repository.UserRepository;
import com.fitnessapp.userDetailsAndAuthService.services.userService.UserDetailAndAuthService;
import com.fitnessapp.userDetailsAndAuthService.userDetailsAndAuthServiceUtilities.AppConstantsUserAndAuthService;
import com.google.firebase.auth.AuthErrorCode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Slf4j
@Service
public class UserDetailAndAuthServiceImpl implements UserDetailAndAuthService {

    private static final String DEFAULT_MAIL_NAME = "Fitness Freak";

    private final UserRepository userRepository;
    private final UserDetailAndAuthRepository userDetailRepository;
    private final JwtTokenService jwtTokenService;
    private final CalculatorService calculatorService;
    private final JavaMailSender mailSender;

    public UserDetailAndAuthServiceImpl(UserRepository userRepository, UserDetailAndAuthRepository userDetailRepository, JwtTokenService jwtTokenService, CalculatorService calculatorService, JavaMailSender mailSender) {
        this.userRepository = userRepository;
        this.userDetailRepository = userDetailRepository;
        this.jwtTokenService = jwtTokenService;
        this.calculatorService = calculatorService;
        this.mailSender = mailSender;
    }

    // ------------------------------------------------------------------ login / register

    @Override
    public ResponseEntity<?> loginOrRegisterUser(UserLoginOrRegisterRequestDto registerRequestDto) {

        FirebaseToken decodedToken;
        try {
            decodedToken = FirebaseAuth.getInstance().verifyIdToken(registerRequestDto.getFirebaseIdToken());
        } catch (FirebaseAuthException | IllegalArgumentException e) {
            log.warn("Firebase ID token rejected: {}", e.getMessage());
            return error(HttpStatus.UNAUTHORIZED, "Invalid Firebase token");
        }

        String id = decodedToken.getUid();
        String email = decodedToken.getEmail();

        if (id == null || id.isBlank()) {
            return error(HttpStatus.BAD_REQUEST, "Firebase user id is missing");
        }
        // Phone / anonymous Firebase accounts have no email; the app stores users by email, so reject them cleanly.
        if (email == null || email.isBlank()) {
            log.warn("Login rejected for user {}: Firebase account has no email", id);
            return error(HttpStatus.BAD_REQUEST, "This account has no email address. Please sign in with an email-based method.");
        }

        try {
            log.info("Login/register for user id {}", id);
            String jwtToken = jwtTokenService.generateToken(id, email);
            String refreshToken = jwtTokenService.generateRefreshToken(id, email);
            if (!userRepository.existsById(id)) {
                return registerUser(id, email, decodedToken.getName(), jwtToken, refreshToken);
            } else {
                return loginUser(id, jwtToken, refreshToken);
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(AppConstantsUserAndAuthService.errorDtoUserDetailAndAuth(e));
        }
    }

    private ResponseEntity<?> loginUser(String id, String jwtToken, String refreshToken) {
        try {
            UserDetailsEntity details = userDetailRepository.findByUserId(id);

            if (details == null) {
                log.info("User {} logged in but has not completed onboarding yet", id);
                // The account exists but the user never finished the "add details" screens.
                // Give them tokens (same response as a fresh registration) so they can continue onboarding.
                return ResponseEntity.ok(UserRegisterResponseDto
                        .builder()
                        .status(AppConstantsUserAndAuthService.SUCCESS_API_STATUS)
                        .statusCode(200)
                        .jwtToken(jwtToken)
                        .refreshToken(refreshToken)
                        .build());
            }

            log.info("User {} logged in", id);
            return ResponseEntity.ok(UserDetailsResponseDto
                    .builder()
                    .status(AppConstantsUserAndAuthService.SUCCESS_API_STATUS)
                    .statusCode(200)
                    .jwtToken(jwtToken)
                    .refreshToken(refreshToken)
                    .userDetailsDto(UserDetailAndAuthServiceUtilities.userDetailsEntityToUserDetailsDto(details))
                    .build());

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(AppConstantsUserAndAuthService.errorDtoUserDetailAndAuth(e));
        }
    }

    private ResponseEntity<?> registerUser(String id, String email, String firebaseName, String jwtToken, String refreshToken) {
        try {
            UserEntity userEntity = new UserEntity();
            userEntity.setId(id);
            userEntity.setEmail(email);
            userRepository.save(userEntity); // repository save is already transactional
            log.info("Registered user {}", userEntity);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(AppConstantsUserAndAuthService.errorDtoUserDetailAndAuth(e));
        }

        // The welcome mail must never fail the registration: the user is already saved at this point.
        try {
            String mailName = (firebaseName == null || firebaseName.isBlank()) ? DEFAULT_MAIL_NAME : firebaseName;
            UserDetailAndAuthServiceUtilities.sendSuccessfulRegistrationMailToUser(mailSender, mailName, email);
        } catch (Exception e) {
            log.warn("Welcome mail could not be sent to user {}: {}", id, e.getLocalizedMessage());
        }

        return ResponseEntity.ok(UserRegisterResponseDto
                .builder()
                .status(AppConstantsUserAndAuthService.SUCCESS_API_STATUS)
                .statusCode(200)
                .jwtToken(jwtToken)
                .refreshToken(refreshToken)
                .build());
    }

    // ------------------------------------------------------------------ refresh token

    @Override
    public ResponseEntity<?> refreshToken(RefreshTokenRequestDto refreshTokenRequestDto) {
        String refreshToken = refreshTokenRequestDto.getRefreshToken();
        try {
            // Throws JwtException when the token is malformed, tampered with or expired.
            String id = jwtTokenService.extractId(refreshToken);
            if (id == null) {
                return unauthorizedString("Invalid refresh token");
            }

            UserEntity user = userRepository.findById(id).orElse(null);
            if (user == null) {
                return unauthorizedString("Invalid refresh token");
            }

            UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                    user.getId(), "", Collections.emptyList());

            // Only a REFRESH token is accepted here, an access token is rejected.
            if (!jwtTokenService.isRefreshTokenValid(refreshToken, userDetails)) {
                return unauthorizedString("Invalid refresh token");
            }

            String newAccessToken = jwtTokenService.generateToken(user.getId(), user.getEmail());
            log.debug("Access token refreshed for user {}", user.getId());
            return ResponseEntity.ok(RefreshTokenResponseDto.builder()
                    .accessToken(newAccessToken)
                    .build());

        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Refresh token rejected: {}", e.getMessage());
            return unauthorizedString("Invalid refresh token");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(AppConstantsUserAndAuthService.errorDtoUserDetailAndAuth(e));
        }
    }

    // ------------------------------------------------------------------ user details

    @Override
    public ResponseEntity<?> getUserDetails() {
        try {
            String id = UserDetailAndAuthServiceUtilities.getUserIdFromTokenInHeader();
            UserEntity userEntity = userRepository.findById(id).orElse(null);
            if (userEntity == null) {
                return ResponseEntity.status(404).body("User not found");
            }
            UserDetailsEntity user = userDetailRepository.findByUserId(id);
            if (user != null) {

                UserDetailsResponseDto loginResponseDto = UserDetailsResponseDto
                        .builder()
                        .status(AppConstantsUserAndAuthService.SUCCESS_API_STATUS)
                        .statusCode(200)
                        .userDetailsDto(UserDetailAndAuthServiceUtilities.userDetailsEntityToUserDetailsDto(user))
                        .build();

                return ResponseEntity.ok(loginResponseDto);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User Details Not Found with given id");
            }
        } catch (Exception e) {

            return ResponseEntity.internalServerError().body(AppConstantsUserAndAuthService.errorDtoUserDetailAndAuth(e));

        }
    }

    @Transactional
    @Override
    public ResponseEntity<?> postUserDetails(UserDetailsDto dto) {
        try {
            String id = UserDetailAndAuthServiceUtilities.getUserIdFromTokenInHeader();
            // Fetch user from DB
            UserEntity user = userRepository.findById(id).orElse(null);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            }
            // Fetch existing user details
            UserDetailsEntity existingUserDetails = userDetailRepository.findByUserId(id);

            if (existingUserDetails != null && existingUserDetails.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User Details With Given Id Already Exists");
            }

            float bmi = UserDetailAndAuthServiceUtilities.getBmi(calculatorService, dto);
            float dailyCalories = UserDetailAndAuthServiceUtilities.getDailyCalories(calculatorService, dto);
            float bodyFat = UserDetailAndAuthServiceUtilities.getBodyFat(calculatorService, dto);
            float idealWeight = UserDetailAndAuthServiceUtilities.getIdealWeight(calculatorService, dto);
            // Update user details
            UserDetailsEntity userEntity = UserDetailsEntity
                    .builder()
                    .name(dto.getName())
                    .email(user.getEmail())
                    .firebaseCloudMessagingToken(dto.getFirebaseCloudMessagingToken())
                    .gender(dto.getGender())
                    .age(dto.getAge())
                    .profileImageUrl(dto.getProfileImageUrl())
                    .heightCm(dto.getHeightCm())
                    .weightKg(dto.getWeightKg())
                    .activityLevel(dto.getActivityLevel())
                    .goal(dto.getGoal())
                    .hipCm(dto.getHipCm())
                    .neckCm(dto.getNeckCm())
                    .waistCm(dto.getWaistCm())
                    .user(user)
                    .userBodyDetails(UserBodyDetails.builder()
                            .bmi(bmi)
                            .dailyCalories(dailyCalories)
                            .bodyFat(bodyFat)
                            .idealWeight(idealWeight)
                            .build())
                    .build();

            UserDetailsEntity saved = userDetailRepository.save(userEntity);

            // A device token belongs to one account only: take it away from any other user.
            if (dto.getFirebaseCloudMessagingToken() != null && !dto.getFirebaseCloudMessagingToken().isEmpty()) {
                userDetailRepository.removeTokensFromOtherUsers(dto.getFirebaseCloudMessagingToken(), saved.getId());
            }

            log.info("User details created for user {}", id);

            dto.setEmail(user.getEmail());
            dto.setBmi(bmi);
            dto.setBodyFat(bodyFat);
            dto.setDailyCalories(dailyCalories);
            dto.setIdealWeight(idealWeight);

            UserDetailsResponseDto responseDto = UserDetailsResponseDto
                    .builder()
                    .status(AppConstantsUserAndAuthService.SUCCESS_API_STATUS)
                    .statusCode(200)
                    .userDetailsDto(dto)
                    .build();

            return ResponseEntity.ok(responseDto);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(AppConstantsUserAndAuthService.errorDtoUserDetailAndAuth(e));
        }
    }

    @Transactional
    @Override
    public ResponseEntity<?> patchUserDetails(UserPatchRequestDto userDetailsDto) {
        try {
            String id = UserDetailAndAuthServiceUtilities.getUserIdFromTokenInHeader();

            // Fetch existing user details
            UserDetailsEntity existingUserDetails = userDetailRepository.findByUserId(id);

            if (existingUserDetails == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User Not Found with given id");
            }

            UserDetailsEntity saved = userDetailRepository.save(UserDetailAndAuthServiceUtilities.patchLogic(userDetailsDto, existingUserDetails));

            if (userDetailsDto.getFirebaseCloudMessagingToken() != null && !userDetailsDto.getFirebaseCloudMessagingToken().isEmpty()) {
                userDetailRepository.removeTokensFromOtherUsers(userDetailsDto.getFirebaseCloudMessagingToken(), saved.getId());
            }

            log.info("User details updated for user {}", id);

            // Map back updated data to DTO
            UserDetailsDto updatedDto = UserDetailAndAuthServiceUtilities.userDetailsEntityToUserDetailsDto(saved);

            UserDetailsResponseDto response = UserDetailsResponseDto.builder()
                    .status(AppConstantsUserAndAuthService.SUCCESS_API_STATUS)
                    .statusCode(200)
                    .userDetailsDto(updatedDto)
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(AppConstantsUserAndAuthService.errorDtoUserDetailAndAuth(e));
        }
    }

    // ------------------------------------------------------------------ delete account

    @Override
    public ResponseEntity<?> deleteUser() {
        String id;
        try {
            id = UserDetailAndAuthServiceUtilities.getUserIdFromTokenInHeader();
            if (!userRepository.existsById(id)) {
                return ResponseEntity.status(404).body("User not found");
            }
            // Cascades to the details row and FCM tokens. Committed before we touch Firebase.
            userRepository.deleteById(id);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(AppConstantsUserAndAuthService.errorDtoUserDetailAndAuth(e));
        }

        // Also remove the Firebase Authentication account (best effort; the app data is already gone).
        try {
            FirebaseAuth.getInstance().deleteUser(id);
        } catch (FirebaseAuthException e) {
            if (e.getAuthErrorCode() != AuthErrorCode.USER_NOT_FOUND) {
                log.warn("Could not delete Firebase account {}: {}", id, e.getLocalizedMessage());
            }
        }

        log.info("User {} deleted", id);
        return ResponseEntity.ok("User With Id : " + id + " Deleted Successfully");
    }

    // ------------------------------------------------------------------ helpers

    private ResponseEntity<?> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(
                ErrorDtoUserDetailsAndAuth.builder()
                        .status(AppConstantsUserAndAuthService.ERROR_API_STATUS)
                        .statusCode(status.value())
                        .errorImageUrl("")
                        .errorMessage(message)
                        .build());
    }

    private ResponseEntity<?> unauthorizedString(String message) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(message);
    }
}
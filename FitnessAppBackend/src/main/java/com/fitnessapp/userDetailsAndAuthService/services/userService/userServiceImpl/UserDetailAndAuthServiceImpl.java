package com.fitnessapp.userDetailsAndAuthService.services.userService.userServiceImpl;

import com.fitnessapp.fitnessCalculatorsService.services.CalculatorService;
import com.fitnessapp.userDetailsAndAuthService.models.dtos.requestDtos.RefreshTokenRequestDto;
import com.fitnessapp.userDetailsAndAuthService.models.dtos.requestDtos.UserLoginOrRegisterRequestDto;
import com.fitnessapp.userDetailsAndAuthService.models.dtos.requestDtos.UserPatchRequestDto;
import com.fitnessapp.userDetailsAndAuthService.models.dtos.responseDtos.RefreshTokenResponseDto;
import com.fitnessapp.userDetailsAndAuthService.models.dtos.responseDtos.UserDetailsResponseDto;
import com.fitnessapp.userDetailsAndAuthService.models.dtos.responseDtos.UserRegisterResponseDto;
import com.fitnessapp.userDetailsAndAuthService.models.entitites.UserBodyDetails;
import com.fitnessapp.userDetailsAndAuthService.repository.UserDetailAndAuthRepository;
import com.fitnessapp.userDetailsAndAuthService.services.userService.UserDetailAndAuthService;
import com.fitnessapp.userDetailsAndAuthService.userDetailsAndAuthServiceUtilities.AppConstantsUserAndAuthService;
import com.fitnessapp.userDetailsAndAuthService.models.dtos.*;
import com.fitnessapp.userDetailsAndAuthService.models.entitites.UserDetailsEntity;
import com.fitnessapp.userDetailsAndAuthService.models.entitites.UserEntity;
import com.fitnessapp.userDetailsAndAuthService.repository.UserRepository;
import com.fitnessapp.DeveloperAndTestingService.configAppScope.springSecurityConfig.jwt.jwtService.JwtService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserDetailAndAuthServiceImpl implements UserDetailAndAuthService {

    private final UserRepository userRepository;
    private final UserDetailAndAuthRepository userDetailRepository;
    private final JwtService jwtService;
    private final CalculatorService calculatorService;
    private final JavaMailSender mailSender;

    public UserDetailAndAuthServiceImpl(UserRepository userRepository, UserDetailAndAuthRepository userDetailRepository, JwtService jwtService, CalculatorService calculatorService, JavaMailSender mailSender) {
        this.userRepository = userRepository;
        this.userDetailRepository = userDetailRepository;
        this.jwtService = jwtService;
        this.calculatorService = calculatorService;
        this.mailSender = mailSender;
    }

    @Override
    public ResponseEntity<?> loginOrRegisterUser(UserLoginOrRegisterRequestDto registerRequestDto) {

        String id = "";
        String email = "";
        try {
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(registerRequestDto.getFirebaseIdToken());
            id = decodedToken.getUid();
            email = decodedToken.getEmail();
        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ErrorDtoUserDetailsAndAuth.builder()
                            .status(AppConstantsUserAndAuthService.ERROR_API_STATUS)
                            .errorMessage("Invalid Firebase token")
                            .statusCode(401)
                            .build()
            );
        }

        if (!id.isBlank() && !email.isBlank()) {
            try {
                log.info("ID of user{}Email of user{}", id, email);
                String jwtToken = jwtService.generateToken(id, email);
                String refreshToken = jwtService.generateRefreshToken(id, email);
                if (userRepository.findById(id).isEmpty()) {
                    return registerUser(id, email, jwtToken, refreshToken);
                } else {
                    return loginUser(id, jwtToken, refreshToken);
                }
            } catch (Exception e) {
                return ResponseEntity.internalServerError().body(AppConstantsUserAndAuthService.errorDtoUserDetailAndAuth(e));
            }
        } else {
            return ResponseEntity.badRequest().body(AppConstantsUserAndAuthService.errorDtoUserDetailAndAuth(new Exception("Id or Email is Blank")));
        }
    }


    @Transactional
    private ResponseEntity<?> loginUser(String id, String jwtToken, String refreshToken) {
        try {
            UserDetailsEntity user = userDetailRepository.findByUserId(id);
            if (user != null) {

                UserDetailsResponseDto loginResponseDto = UserDetailsResponseDto
                        .builder()
                        .status(AppConstantsUserAndAuthService.SUCCESS_API_STATUS)
                        .statusCode(200)
                        .jwtToken(jwtToken)
                        .refreshToken(refreshToken)
                        .userDetailsDto(UserDetailAndAuthServiceUtilities.userDetailsEntityToUserDetailsDto(user))
                        .build();

                return ResponseEntity.ok(loginResponseDto);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User Not Found with given id");
            }

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(AppConstantsUserAndAuthService.errorDtoUserDetailAndAuth(e));
        }
    }


    @Override
    public ResponseEntity<?> refreshToken(RefreshTokenRequestDto refreshTokenRequestDto) {
        try {
            String refreshToken = refreshTokenRequestDto.getRefreshToken();
            String id = jwtService.extractId(refreshToken);
            if (id == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid refresh token");
            }

            UserDetails userDetails = userRepository.findById(id)
                    .map(user -> org.springframework.security.core.userdetails.User
                            .withUsername(user.getId())
//                            .password("") // or set to "N/A" if not needed
//                            .authorities("USER") // or based on role
                            .build())
                    .orElse(null);

            if (userDetails != null && jwtService.isRefreshTokenValid(refreshToken, userDetails)) {
                String newAccessToken = jwtService.generateToken(id, userDetails.getUsername());


                RefreshTokenResponseDto tokenResponse = RefreshTokenResponseDto.builder()
                        .accessToken(newAccessToken)
                        .build();

                return ResponseEntity.ok(tokenResponse);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid refresh token");
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(AppConstantsUserAndAuthService.errorDtoUserDetailAndAuth(e));
        }
    }

    @Transactional
    private ResponseEntity<?> registerUser(String id, String email, String jwtToken, String refreshToken) {
        try {
            UserEntity userEntity = new UserEntity();
            userEntity.setId(id);
            userEntity.setEmail(email);
            log.info("Response Register User Entity {}", userEntity);
            userRepository.save(userEntity);
            //This method sends the mail to the user after successful registration.
            UserDetailAndAuthServiceUtilities.sendSuccessfulRegistrationMailToUser(mailSender, "Fitness Freak", email);
            return ResponseEntity.ok(UserRegisterResponseDto
                    .builder()
                    .status(AppConstantsUserAndAuthService.SUCCESS_API_STATUS)
                    .statusCode(200)
                    .jwtToken(jwtToken)
                    .refreshToken(refreshToken)
                    .build()
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(AppConstantsUserAndAuthService.errorDtoUserDetailAndAuth(e));
        }
    }

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

            userDetailRepository.save(userEntity);


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
            String id = UserDetailAndAuthServiceUtilities.getUserIdFromTokenInHeader(); // should be userId (Long or String)

            // Fetch existing user details
            UserDetailsEntity existingUserDetails = userDetailRepository.findByUserId(id);

            if (existingUserDetails == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User Not Found with given id");
            }

            userDetailRepository.save(UserDetailAndAuthServiceUtilities.patchLogic(userDetailsDto, existingUserDetails));

            // Map back updated data to DTO (optional: map from entity to DTO)
            UserDetailsDto updatedDto = UserDetailAndAuthServiceUtilities.userDetailsEntityToUserDetailsDto(existingUserDetails);

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

    @Override
    public ResponseEntity<?> deleteUser() {
        try {
            String id = UserDetailAndAuthServiceUtilities.getUserIdFromTokenInHeader();
            UserEntity userEntity = userRepository.findById(id).orElse(null);
            if (userEntity == null) {
                return ResponseEntity.status(404).body("User not found");
            }
            userRepository.deleteById(id);

            return ResponseEntity.ok("User With Id : " + id + " Deleted Successfully");

        } catch (Exception e) {

            return ResponseEntity.internalServerError().body(AppConstantsUserAndAuthService.errorDtoUserDetailAndAuth(e));

        }
    }


}

package com.fitnessapp.userDetailsAndAuthService.services.userService;

import com.fitnessapp.userDetailsAndAuthService.models.dtos.requestDtos.UserLoginOrRegisterRequestDto;
import com.fitnessapp.userDetailsAndAuthService.models.dtos.requestDtos.UserPatchRequestDto;
import com.fitnessapp.userDetailsAndAuthService.models.dtos.responseDtos.UserDetailsResponseDto;
import com.fitnessapp.userDetailsAndAuthService.models.dtos.responseDtos.UserRegisterResponseDto;
import com.fitnessapp.userDetailsAndAuthService.repository.UserDetailRepository;
import com.fitnessapp.userDetailsAndAuthService.userDetailsAndAuthServiceUtilities.AppConstantsUserAndAuthService;
import com.fitnessapp.userDetailsAndAuthService.models.dtos.*;
import com.fitnessapp.userDetailsAndAuthService.models.entitites.UserDetailsEntity;
import com.fitnessapp.userDetailsAndAuthService.models.entitites.UserEntity;
import com.fitnessapp.userDetailsAndAuthService.repository.UserRepository;
import com.fitnessapp.userDetailsAndAuthService.services.firebaseService.FirebaseService;
import com.fitnessapp.userDetailsAndAuthService.services.jwtService.JwtService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserDetailRepository userDetailRepository;
    private final JwtService jwtService;

    public UserServiceImpl(UserRepository userRepository, UserDetailRepository userDetailRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.userDetailRepository = userDetailRepository;
        this.jwtService = jwtService;
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
                    ErrorDto.builder()
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
                if (userRepository.findById(id).isEmpty()) {
                    return registerUser(id, email, jwtToken);
                } else {
                    return loginUser(id, jwtToken);
                }
            } catch (Exception e) {
                return ResponseEntity.internalServerError().body(AppConstantsUserAndAuthService.errorDto(e));
            }
        } else {
            return ResponseEntity.internalServerError().body(AppConstantsUserAndAuthService.errorDto(new Exception("Id or Email is Blank")));
        }
    }

    @Transactional
    private ResponseEntity<?> loginUser(String id, String jwtToken) {
        try {
            UserDetailsEntity user = userDetailRepository.findByUserId(id);
            if (user != null) {

                UserDetailsResponseDto loginResponseDto = UserDetailsResponseDto
                        .builder()
                        .status(AppConstantsUserAndAuthService.SUCCESS_API_STATUS)
                        .statusCode(200)
                        .jwtToken(jwtToken)
                        .userDetailsDto(UserDetailsDto.builder()
                                .name(user.getName())
                                .email(user.getEmail())
                                .gender(user.getGender())
                                .age(user.getAge())
                                .profileImageUrl(user.getProfileImageUrl())
                                .heightCm(user.getHeight())
                                .weightKg(user.getWeight())
                                .activityLevel(user.getActivityLevel())
                                .goal(user.getGoal())
                                .hip(user.getHip())
                                .neck(user.getNeck())
                                .waist(user.getWaist())
                                .build())
                        .build();

                return ResponseEntity.ok(loginResponseDto);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User Not Found with given id");
            }

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(AppConstantsUserAndAuthService.errorDto(e));
        }
    }

    @Transactional
    private ResponseEntity<?> registerUser(String id, String email, String jwtToken) {
        try {
            UserEntity userEntity = new UserEntity();
            userEntity.setId(id);
            userEntity.setEmail(email);
            log.info("Response Register User Entity {}", userEntity);
            userRepository.save(userEntity);
            return ResponseEntity.ok(UserRegisterResponseDto
                    .builder()
                    .status(AppConstantsUserAndAuthService.SUCCESS_API_STATUS)
                    .statusCode(200)
                    .jwtToken(jwtToken)
                    .build()
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(AppConstantsUserAndAuthService.errorDto(e));
        }

    }

    @Override
    public ResponseEntity<?> getUserDetails() {
        try {
            String id = UserServiceUtilities.getUserIdFromTokenInHeader();
            log.info("TOKEN11 {}", id);
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
                        .userDetailsDto(UserDetailsDto.builder()
                                .name(user.getName())
                                .email(user.getEmail())
                                .gender(user.getGender())
                                .age(user.getAge())
                                .profileImageUrl(user.getProfileImageUrl())
                                .heightCm(user.getHeight())
                                .weightKg(user.getWeight())
                                .activityLevel(user.getActivityLevel())
                                .goal(user.getGoal())
                                .hip(user.getHip())
                                .neck(user.getNeck())
                                .waist(user.getWaist())
                                .build())
                        .build();

                return ResponseEntity.ok(loginResponseDto);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User Details Not Found with given id");
            }
        } catch (Exception e) {

            return ResponseEntity.internalServerError().body(AppConstantsUserAndAuthService.errorDto(e));

        }
    }

    @Transactional
    @Override
    public ResponseEntity<?> fillUserDetails(UserDetailsDto userDetailsDto) {
        try {
            String id = UserServiceUtilities.getUserIdFromTokenInHeader();
            log.info("TOKEN222 {}", id);
            // Fetch user from DB
            UserEntity user = userRepository.findById(id).orElse(null);
            if (user == null) {
                return ResponseEntity.status(404).body("User not found");
            }
            // Fetch existing user details
            UserDetailsEntity existingUserDetails = userDetailRepository.findByUserId(id);

            if (existingUserDetails != null && existingUserDetails.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User Details With Given Id Already Exists");
            }
            // Update user details
            UserDetailsEntity userEntity = UserDetailsEntity
                    .builder()
                    .name(userDetailsDto.getName())
                    .email(user.getEmail())
                    .firebaseCloudMessagingToken(userDetailsDto.getFirebaseCloudMessagingToken())
                    .gender(userDetailsDto.getGender())
                    .age(userDetailsDto.getAge())
                    .profileImageUrl(userDetailsDto.getProfileImageUrl())
                    .height(userDetailsDto.getHeightCm())
                    .weight(userDetailsDto.getWeightKg())
                    .activityLevel(userDetailsDto.getActivityLevel())
                    .goal(userDetailsDto.getGoal())
                    .hip(userDetailsDto.getHip())
                    .neck(userDetailsDto.getNeck())
                    .waist(userDetailsDto.getWaist())
                    .user(user)
                    .build();

            userDetailRepository.save(userEntity);

            UserDetailsResponseDto loginResponseDto = UserDetailsResponseDto
                    .builder()
                    .status(AppConstantsUserAndAuthService.SUCCESS_API_STATUS)
                    .statusCode(200)
                    .userDetailsDto(userDetailsDto)
                    .build();

            return ResponseEntity.ok(loginResponseDto);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(AppConstantsUserAndAuthService.errorDto(e));
        }
    }

    @Transactional
    @Override
    public ResponseEntity<?> patchUserDetails(UserPatchRequestDto userDetailsDto) {
        try {
            String id = UserServiceUtilities.getUserIdFromTokenInHeader(); // should be userId (Long or String)
            log.info("TOKEN333 {}", id);

            // Fetch existing user details
            UserDetailsEntity existingUserDetails = userDetailRepository.findByUserId(id);

            if (existingUserDetails == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User Not Found with given id");
            }

            // PATCH: Update only non-null / non-empty fields
            if (userDetailsDto.getName() != null) existingUserDetails.setName(userDetailsDto.getName());
            if (userDetailsDto.getFirebaseCloudMessagingToken() != null && !userDetailsDto.getFirebaseCloudMessagingToken().isEmpty())
                existingUserDetails.setFirebaseCloudMessagingToken(userDetailsDto.getFirebaseCloudMessagingToken());
            if (userDetailsDto.getGender() != null) existingUserDetails.setGender(userDetailsDto.getGender());
            if (userDetailsDto.getAge() != null) existingUserDetails.setAge(userDetailsDto.getAge());
            if (userDetailsDto.getProfileImageUrl() != null)
                existingUserDetails.setProfileImageUrl(userDetailsDto.getProfileImageUrl());
            if (userDetailsDto.getHeightCm() > 0) existingUserDetails.setHeight(userDetailsDto.getHeightCm());
            if (userDetailsDto.getWeightKg() > 0) existingUserDetails.setWeight(userDetailsDto.getWeightKg());
            if (userDetailsDto.getActivityLevel() != null)
                existingUserDetails.setActivityLevel(userDetailsDto.getActivityLevel());
            if (userDetailsDto.getGoal() != null) existingUserDetails.setGoal(userDetailsDto.getGoal());
            if (userDetailsDto.getHip() >= 0) existingUserDetails.setHip(userDetailsDto.getHip());
            if (userDetailsDto.getNeck() >= 0) existingUserDetails.setNeck(userDetailsDto.getNeck());
            if (userDetailsDto.getWaist() >= 0) existingUserDetails.setWaist(userDetailsDto.getWaist());

            userDetailRepository.save(existingUserDetails);

            // Map back updated data to DTO (optional: map from entity to DTO)
            UserDetailsDto updatedDto = UserDetailsDto.builder()
                    .name(existingUserDetails.getName())
                    .email(existingUserDetails.getEmail())
                    .firebaseCloudMessagingToken(existingUserDetails.getFirebaseCloudMessagingToken())
                    .gender(existingUserDetails.getGender())
                    .age(existingUserDetails.getAge())
                    .profileImageUrl(existingUserDetails.getProfileImageUrl())
                    .heightCm(existingUserDetails.getHeight())
                    .weightKg(existingUserDetails.getWeight())
                    .activityLevel(existingUserDetails.getActivityLevel())
                    .goal(existingUserDetails.getGoal())
                    .hip(existingUserDetails.getHip())
                    .neck(existingUserDetails.getNeck())
                    .waist(existingUserDetails.getWaist())
                    .build();

            UserDetailsResponseDto response = UserDetailsResponseDto.builder()
                    .status(AppConstantsUserAndAuthService.SUCCESS_API_STATUS)
                    .statusCode(200)
                    .userDetailsDto(updatedDto)
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(AppConstantsUserAndAuthService.errorDto(e));
        }
    }

    @Override
    public ResponseEntity<?> deleteUser() {
        try {
            String id = UserServiceUtilities.getUserIdFromTokenInHeader();
            UserEntity userEntity = userRepository.findById(id).orElse(null);
            if (userEntity == null) {
                return ResponseEntity.status(404).body("User not found");
            }
            userRepository.deleteById(id);

            return ResponseEntity.ok("User With Id : " + id + " Deleted Successfully");

        } catch (Exception e) {

            return ResponseEntity.internalServerError().body(AppConstantsUserAndAuthService.errorDto(e));

        }
    }


}

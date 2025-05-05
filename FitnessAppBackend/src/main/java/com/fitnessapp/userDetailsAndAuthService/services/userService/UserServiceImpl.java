package com.fitnessapp.userDetailsAndAuthService.services.userService;

import com.fitnessapp.userDetailsAndAuthService.models.dtos.requestDtos.UserLoginOrRegisterRequestDto;
import com.fitnessapp.userDetailsAndAuthService.models.dtos.responseDtos.UserDetailsFilResponseDto;
import com.fitnessapp.userDetailsAndAuthService.models.dtos.responseDtos.UserGetDetailsResponseDto;
import com.fitnessapp.userDetailsAndAuthService.models.dtos.responseDtos.UserLoginResponseDto;
import com.fitnessapp.userDetailsAndAuthService.models.dtos.responseDtos.UserRegisterResponseDto;
import com.fitnessapp.userDetailsAndAuthService.userDetailsAndAuthServiceUtilities.AppConstantsUserAndAuthService;
import com.fitnessapp.userDetailsAndAuthService.models.dtos.*;
import com.fitnessapp.userDetailsAndAuthService.models.entitites.UserDetailsEntity;
import com.fitnessapp.userDetailsAndAuthService.models.entitites.UserEntity;
import com.fitnessapp.userDetailsAndAuthService.models.mapper.UserMapper;
import com.fitnessapp.userDetailsAndAuthService.repository.UserRepository;
import com.fitnessapp.userDetailsAndAuthService.services.firebaseService.FirebaseService;
import com.fitnessapp.userDetailsAndAuthService.services.jwtService.JwtService;
import com.google.firebase.auth.FirebaseToken;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final FirebaseService firebaseService;
    private final JwtService jwtService;
    private final UserMapper userMapper;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, FirebaseService firebaseService, JwtService jwtService, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.firebaseService = firebaseService;
        this.jwtService = jwtService;
        this.userMapper = userMapper;
    }


    @Transactional
    @Override
    public ResponseEntity<?> loginOrRegisterUser(UserLoginOrRegisterRequestDto registerRequestDto) {

        FirebaseToken decodedToken = firebaseService.verifyFirebaseIdToken(registerRequestDto.getFirebaseIdToken());
        String id = decodedToken.getUid();
        String email = decodedToken.getEmail();

        if (!id.isBlank() && !email.isBlank()) {
            try {
                log.info("ID of user{}Email of user{}", id, email);
                UserRegisterResponseDto responseDto = UserRegisterResponseDto
                        .builder()
                        .status(AppConstantsUserAndAuthService.SUCCESS_API_STATUS)
                        .statusCode(200)
                        .jwtToken(jwtService.generateToken(id, email))
                        .build();
                log.info("Response Dto{}", responseDto);


                if (userRepository.findById(id).isEmpty()) {
                    UserEntity userEntity = new UserEntity();
                    userEntity.setId(id);
                    userEntity.setEmail(email);
                    UserDetailsEntity details = new UserDetailsEntity();
// set other details fields...
                    details.setUsers(userEntity); // very important
                    userEntity.setUserDetailsEntity(details);
                    log.info("Response User Entity {}", userEntity);
                    userRepository.save(userEntity);
                } else {
                    UserEntity user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User Not Found"));

                    UserLoginResponseDto loginResponseDto = userMapper.userEntityToLoginResponse(user);
                    loginResponseDto.setStatus(AppConstantsUserAndAuthService.SUCCESS_API_STATUS);
                    loginResponseDto.setStatusCode(200);
                    loginResponseDto.setJwtToken(jwtService.generateToken(id, email));
//                    UserLoginResponseDto loginResponseDto = UserLoginResponseDto
//                            .builder()
//                            .status(AppConstantsUserAndAuthService.SUCCESS_API_STATUS)
//                            .statusCode(200)
//                            .jwtToken(jwtService.generateToken(id, email))
//                            .userDataDto(
//                                    UserDetailsDto.builder()
//                                            .name(user.getUserDetailsList().getName())
//                                            .gender()
//                                            .age()
//                                            .profileImageUrl()
//                                            .height()
//                                            .weight()
//                                            .activityLevel()
//                                            .goal()
//                                            .hip()
//                                            .neck()
//                                            .waist()
//                                            .build());
                    return ResponseEntity.ok(loginResponseDto);
                }

                return ResponseEntity.ok(responseDto);
            } catch (Exception e) {

                return ResponseEntity.internalServerError().body(AppConstantsUserAndAuthService.errorDto(e));

            }

        } else {
            ErrorDto errorDto = ErrorDto
                    .builder()
                    .errorImageUrl("")
                    .status(AppConstantsUserAndAuthService.ERROR_API_STATUS)
                    .errorMessage("Error : Please Try Again")
                    .statusCode(500)
                    .build();

            return ResponseEntity.internalServerError().body(errorDto);
        }
    }

    @Transactional
    @Override
    public ResponseEntity<?> fillUserDetails(UserDetailsDto userDetailsDto) {
        try {
            String id = getUserIdFromTokenInHeader();
            log.info("TOKENNNNN " + id);
            // Fetch user from DB
            UserEntity userEntity = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Update user details
            UserDetailsEntity details = userMapper.userDataDtoTouserDetailsEntity(userDetailsDto);
            userEntity.setUserDetailsEntity(details);
            userRepository.save(userEntity);

            UserDetailsFilResponseDto responseDto = UserDetailsFilResponseDto.builder()
                    .status(AppConstantsUserAndAuthService.SUCCESS_API_STATUS)
                    .statusCode(200)
                    .message("User Details Added Successfully")
                    .build();
            return ResponseEntity.ok(responseDto);

        } catch (Exception e) {

            return ResponseEntity.internalServerError().body(AppConstantsUserAndAuthService.errorDto(e));


        }
    }

    @Override
    public ResponseEntity<?> getUserDetails() {
        try {
            String id = getUserIdFromTokenInHeader();
            log.info("TOKENNNNN11 " + id);
            UserEntity user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User Not Found"));

            UserGetDetailsResponseDto userDetailsResponseDto = userMapper.userEntityToUserDetailsResponseDto(user);
            userDetailsResponseDto.setStatus(AppConstantsUserAndAuthService.SUCCESS_API_STATUS);
            userDetailsResponseDto.setStatusCode(200);

            return ResponseEntity.ok(userDetailsResponseDto);
        } catch (Exception e) {

            return ResponseEntity.internalServerError().body(AppConstantsUserAndAuthService.errorDto(e));

        }
    }

    private static String getUserIdFromTokenInHeader() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName(); // This is the Firebase UID from the token
    }


}

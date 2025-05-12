package com.fitnessapp.userDetailsAndAuthService.services.userService;

import com.fitnessapp.userDetailsAndAuthService.models.dtos.UserDetailsDto;
import com.fitnessapp.userDetailsAndAuthService.models.dtos.requestDtos.RefreshTokenRequestDto;
import com.fitnessapp.userDetailsAndAuthService.models.dtos.requestDtos.UserLoginOrRegisterRequestDto;
import com.fitnessapp.userDetailsAndAuthService.models.dtos.requestDtos.UserPatchRequestDto;
import org.springframework.http.ResponseEntity;

public interface UserService {
    ResponseEntity<?> loginOrRegisterUser(UserLoginOrRegisterRequestDto registerRequestDto);

    ResponseEntity<?> refreshToken(RefreshTokenRequestDto refreshTokenRequestDto);

    ResponseEntity<?> getUserDetails();

    ResponseEntity<?> fillUserDetails(UserDetailsDto userDetailsDto);


    ResponseEntity<?> patchUserDetails(UserPatchRequestDto userDetailsDto);

    ResponseEntity<?> deleteUser();
}

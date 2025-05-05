package com.fitnessapp.userDetailsAndAuthService.services.userService;

import com.fitnessapp.userDetailsAndAuthService.models.dtos.UserDetailsDto;
import com.fitnessapp.userDetailsAndAuthService.models.dtos.requestDtos.UserLoginOrRegisterRequestDto;
import org.springframework.http.ResponseEntity;

public interface UserService {
    ResponseEntity<?> loginOrRegisterUser(UserLoginOrRegisterRequestDto registerRequestDto);

    ResponseEntity<?> fillUserDetails(UserDetailsDto userDetailsDto);

    ResponseEntity<?> getUserDetails();

}

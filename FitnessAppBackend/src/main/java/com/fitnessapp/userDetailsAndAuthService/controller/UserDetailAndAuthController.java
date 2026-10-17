package com.fitnessapp.userDetailsAndAuthService.controller;

import com.fitnessapp.userDetailsAndAuthService.models.dtos.requestDtos.RefreshTokenRequestDto;
import com.fitnessapp.userDetailsAndAuthService.models.dtos.requestDtos.UserPatchRequestDto;
import com.fitnessapp.userDetailsAndAuthService.userDetailsAndAuthServiceUtilities.AppConstantsUserAndAuthService;
import com.fitnessapp.userDetailsAndAuthService.models.dtos.UserDetailsDto;
import com.fitnessapp.userDetailsAndAuthService.models.dtos.requestDtos.UserLoginOrRegisterRequestDto;
import com.fitnessapp.userDetailsAndAuthService.services.userService.UserDetailAndAuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(AppConstantsUserAndAuthService.API_PREFIX)
public class UserDetailAndAuthController {

    private final UserDetailAndAuthService userService;

    public UserDetailAndAuthController(UserDetailAndAuthService userService) {
        this.userService = userService;
    }


    //This POST endpoint logs in an existing user or registers a new one using a Firebase ID token that comes from the mobile phone.
    @PostMapping("/loginRegister")
    public ResponseEntity<?> loginOrRegisterUser(@RequestBody @Valid UserLoginOrRegisterRequestDto registerRequestDto) {
        return userService.loginOrRegisterUser(registerRequestDto);
    }

    //This request adds the users detail (i.e. different screen in the phone) in the database, and the UID will be got from the jwt token to identify the user and fetch its details from the database
    //It returns the UserDetailsResponseDto
    @PostMapping("/add-details")
    public ResponseEntity<?> addDetailsOfUser(@RequestBody @Valid UserDetailsDto userDetailsDto) {
        return userService.postUserDetails(userDetailsDto);
    }

    //This request patch the user details in the database,  and the uid will be got from the jwt token to identify the user and fetch its details from the database
    //It returns the UserDetailsResponseDto
    @PatchMapping("/patch-details")
    public ResponseEntity<?> patchDetailsOfUser(@RequestBody @Valid UserPatchRequestDto userDetailsDto) {
        return userService.patchUserDetails(userDetailsDto);
    }

    // In this particular, user details will be fetched, and the uid will be got from the jwt token to identify the user and fetch its details from the database
    //It returns the UserDetailsResponseDto
    @GetMapping("/get-details")
    public ResponseEntity<?> getUserDetails() {
        return userService.getUserDetails();
    }

    //To delete the particular user, and the uid will be got from the jwt token to identify the user and fetch its details from the database
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteUser() {
        return userService.deleteUser();
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody @Valid RefreshTokenRequestDto refreshTokenRequestDto) {
        return userService.refreshToken(refreshTokenRequestDto);
    }
}
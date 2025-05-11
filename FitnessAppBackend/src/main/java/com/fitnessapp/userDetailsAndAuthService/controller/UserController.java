package com.fitnessapp.userDetailsAndAuthService.controller;

import com.fitnessapp.userDetailsAndAuthService.models.dtos.requestDtos.UserPatchRequestDto;
import com.fitnessapp.userDetailsAndAuthService.userDetailsAndAuthServiceUtilities.AppConstantsUserAndAuthService;
import com.fitnessapp.userDetailsAndAuthService.models.dtos.UserDetailsDto;
import com.fitnessapp.userDetailsAndAuthService.models.dtos.requestDtos.UserLoginOrRegisterRequestDto;
import com.fitnessapp.userDetailsAndAuthService.services.userService.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(AppConstantsUserAndAuthService.API_PREFIX)
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }


    @PostMapping("/loginRegister")
    public ResponseEntity<?> loginOrRegisterUser(@RequestBody @Valid UserLoginOrRegisterRequestDto registerRequestDto) {
        return userService.loginOrRegisterUser(registerRequestDto);
    }

    @PostMapping("/add-details")
    public ResponseEntity<?> addDetailsOfUser(@RequestBody @Valid UserDetailsDto userDetailsDto) {
        return userService.fillUserDetails(userDetailsDto);
    }

    @PatchMapping("/patch-details")
    public ResponseEntity<?> patchDetailsOfUser(@RequestBody @Valid UserPatchRequestDto userDetailsDto) {
        return userService.patchUserDetails(userDetailsDto);
    }

    // in this particular, user details will be fetched, and the uid will be got from the jwt token
    @GetMapping("/get-details")
    public ResponseEntity<?> getUserDetails() {
        return userService.getUserDetails();
    }


    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteUser() {
        return userService.deleteUser();
    }
}
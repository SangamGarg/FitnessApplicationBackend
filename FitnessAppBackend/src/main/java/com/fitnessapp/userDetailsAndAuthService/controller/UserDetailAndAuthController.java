package com.fitnessapp.userDetailsAndAuthService.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
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
@Tag(name = "User Detail And Auth", description = "Endpoints for user detail and auth")
public class UserDetailAndAuthController {

    private final UserDetailAndAuthService userService;

    public UserDetailAndAuthController(UserDetailAndAuthService userService) {
        this.userService = userService;
    }


    //This POST endpoint logs in an existing user or registers a new one using a Firebase ID token that comes from the mobile phone.
    @Operation(summary = "login Or Register User", description = """
        Executes the login or register user operation.
        """)
    @PostMapping("/loginRegister")
    public ResponseEntity<?> loginOrRegisterUser(@RequestBody @Valid UserLoginOrRegisterRequestDto registerRequestDto) {
        return userService.loginOrRegisterUser(registerRequestDto);
    }

    //This request adds the users detail (i.e. different screen in the phone) in the database, and the UID will be got from the jwt token to identify the user and fetch its details from the database
    //It returns the UserDetailsResponseDto
    @Operation(summary = "add Details Of User", description = """
        Executes the add details of user operation.
        """)
    @PostMapping("/add-details")
    public ResponseEntity<?> addDetailsOfUser(@RequestBody @Valid UserDetailsDto userDetailsDto) {
        return userService.postUserDetails(userDetailsDto);
    }

    //This request patch the user details in the database,  and the uid will be got from the jwt token to identify the user and fetch its details from the database
    //It returns the UserDetailsResponseDto
    @Operation(summary = "patch Details Of User", description = """
        Executes the patch details of user operation.
        """)
    @PatchMapping("/patch-details")
    public ResponseEntity<?> patchDetailsOfUser(@RequestBody @Valid UserPatchRequestDto userDetailsDto) {
        return userService.patchUserDetails(userDetailsDto);
    }

    // In this particular, user details will be fetched, and the uid will be got from the jwt token to identify the user and fetch its details from the database
    //It returns the UserDetailsResponseDto
    @Operation(summary = "get User Details", description = """
        Executes the get user details operation.
        """)
    @GetMapping("/get-details")
    public ResponseEntity<?> getUserDetails() {
        return userService.getUserDetails();
    }

    //To delete the particular user, and the uid will be got from the jwt token to identify the user and fetch its details from the database
    @Operation(summary = "delete User", description = """
        Executes the delete user operation.
        """)
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteUser() {
        return userService.deleteUser();
    }

    @Operation(summary = "refresh Token", description = """
        Executes the refresh token operation.
        """)
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody @Valid RefreshTokenRequestDto refreshTokenRequestDto) {
        return userService.refreshToken(refreshTokenRequestDto);
    }
}

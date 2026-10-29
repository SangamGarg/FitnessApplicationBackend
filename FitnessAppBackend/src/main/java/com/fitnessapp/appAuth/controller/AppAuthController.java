package com.fitnessapp.appAuth.controller;

import com.fitnessapp.appAuth.model.dtos.UserDetailsDto;
import com.fitnessapp.appAuth.model.dtos.requestDto.RefreshTokenRequestDto;
import com.fitnessapp.appAuth.model.dtos.requestDto.UserPatchRequestDto;
import com.fitnessapp.appAuth.model.dtos.responseDto.RefreshTokenResponseDto;
import com.fitnessapp.appAuth.model.dtos.responseDto.UserDetailsResponseDto;
import com.fitnessapp.appAuth.model.dtos.responseDto.UserRegisterResponseDto;
import com.fitnessapp.appAuth.service.AppAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.fitnessapp.utils.appUtil.AppUtil.APP_AUTH_ENDPOINT;

@Slf4j
@RestController
@RequestMapping(APP_AUTH_ENDPOINT)
@RequiredArgsConstructor
public class AppAuthController {
    private final AppAuthService authService;

    @PostMapping("/loginRegister")
    public ResponseEntity<UserRegisterResponseDto> loginOrRegisterUser(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        log.info("Received request for user login/registration. Auth header present: {}", authorization != null && !authorization.isBlank());
        ResponseEntity<UserRegisterResponseDto> response = authService.loginOrRegisterUser(authorization);
        log.info("User login/registration completed with status: {}", response.getStatusCode());
        return response;
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<RefreshTokenResponseDto> refreshToken(
            @RequestBody @Valid RefreshTokenRequestDto request) {
        log.info("Received request to refresh token");
        ResponseEntity<RefreshTokenResponseDto> response = authService.refreshToken(request);
        log.info("Token refresh completed with status: {}", response.getStatusCode());
        return response;
    }

    /** Revokes the current refresh-token family. Use logoutEverywhere=true to revoke all sessions. */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestBody(required = false) RefreshTokenRequestDto request,
            @RequestParam(defaultValue = "false") boolean logoutEverywhere) {
        log.info("Received logout request. logoutEverywhere={}", logoutEverywhere);
        ResponseEntity<Void> response = authService.logout(request, logoutEverywhere);
        log.info("Logout completed with status: {}", response.getStatusCode());
        return response;
    }

    @PostMapping("/add-details")
    public ResponseEntity<UserDetailsResponseDto> addDetailsOfUser(@RequestBody @Valid UserDetailsDto dto) {
        log.info("Received request to add user details");
        ResponseEntity<UserDetailsResponseDto> response = authService.postUserDetails(dto);
        log.info("User details added with status: {}", response.getStatusCode());
        return response;
    }

    @PatchMapping("/patch-details")
    public ResponseEntity<UserDetailsResponseDto> patchDetailsOfUser(@RequestBody @Valid UserPatchRequestDto dto) {
        log.info("Received request to patch user details");
        ResponseEntity<UserDetailsResponseDto> response = authService.patchUserDetails(dto);
        log.info("User details patched with status: {}", response.getStatusCode());
        return response;
    }

    @GetMapping("/get-details")
    public ResponseEntity<UserDetailsResponseDto> getUserDetails() {
        log.info("Received request to fetch user details");
        ResponseEntity<UserDetailsResponseDto> response = authService.getUserDetails();
        log.info("User details retrieved with status: {}", response.getStatusCode());
        return response;
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteUser() {
        log.info("Received request to delete user account");
        ResponseEntity<Void> response = authService.deleteUser();
        log.info("User account deletion completed with status: {}", response.getStatusCode());
        return response;
    }
}
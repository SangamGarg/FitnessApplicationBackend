package com.fitnessapp.appAuth.service;

import com.fitnessapp.appAuth.model.dtos.UserDetailsDto;
import com.fitnessapp.appAuth.model.dtos.requestDto.RefreshTokenRequestDto;
import com.fitnessapp.appAuth.model.dtos.requestDto.UserPatchRequestDto;
import com.fitnessapp.appAuth.model.dtos.responseDto.RefreshTokenResponseDto;
import com.fitnessapp.appAuth.model.dtos.responseDto.UserDetailsResponseDto;
import com.fitnessapp.appAuth.model.dtos.responseDto.UserRegisterResponseDto;
import org.springframework.http.ResponseEntity;

public interface AppAuthService {
    ResponseEntity<UserRegisterResponseDto> loginOrRegisterUser(String authorizationHeader);
    ResponseEntity<RefreshTokenResponseDto> refreshToken(RefreshTokenRequestDto request);
    ResponseEntity<Void> logout(RefreshTokenRequestDto request, boolean logoutEverywhere);
    ResponseEntity<UserDetailsResponseDto> getUserDetails();
    ResponseEntity<UserDetailsResponseDto> postUserDetails(UserDetailsDto userDetailsDto);
    ResponseEntity<UserDetailsResponseDto> patchUserDetails(UserPatchRequestDto userDetailsDto);
    ResponseEntity<Void> deleteUser();
}

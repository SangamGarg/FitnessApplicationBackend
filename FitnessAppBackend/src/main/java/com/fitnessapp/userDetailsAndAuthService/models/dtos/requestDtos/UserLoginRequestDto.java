package com.fitnessapp.userDetailsAndAuthService.models.dtos.requestDtos;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UserLoginRequestDto {
    @NotBlank(message = "FirebaseId Token cannot be null or empty")
    private String firebaseIdToken;
}
package com.fitnessapp.userDetailsAndAuthService.models.dtos.responseDtos;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UserRegisterResponseDto {
    private String status;
    private Integer statusCode;
    private String jwtToken;
    private String refreshToken;
}
package com.fitnessapp.appAuth.model.dtos.responseDto;

import lombok.*;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenResponseDto {

    private String status;
    private Integer statusCode;
    private String message;
    @Builder.Default
    private Instant timestamp = Instant.now();
    @Builder.Default
    private String tokenType = "Bearer";
    private String accessToken;
    private String refreshToken;
}

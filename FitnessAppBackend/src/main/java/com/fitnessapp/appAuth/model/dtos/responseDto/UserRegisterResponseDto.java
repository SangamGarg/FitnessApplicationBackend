package com.fitnessapp.appAuth.model.dtos.responseDto;

import com.fitnessapp.appAuth.model.dtos.UserDetailsDto;
import lombok.*;

import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UserRegisterResponseDto {
    private String status;
    private Integer statusCode;
    private String message;
    @Builder.Default
    private Instant timestamp = Instant.now();

    @Builder.Default
    private String tokenType = "Bearer";
    private String accessToken;
    private String refreshToken;
    private boolean isNewUser;
    private boolean profileComplete;
    private UserDetailsDto userDetailsDto;
}

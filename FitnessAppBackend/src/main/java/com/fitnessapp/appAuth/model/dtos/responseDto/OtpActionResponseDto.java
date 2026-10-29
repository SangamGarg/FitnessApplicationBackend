package com.fitnessapp.appAuth.model.dtos.responseDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpActionResponseDto {


    private String status;
    private Integer statusCode;
    private String message;
    @Builder.Default
    private Instant timestamp = Instant.now();

    private String code;
    private Long expiresInSeconds;
    /** Present only after successful forgot-password OTP verification. */
    private String verificationToken;
}

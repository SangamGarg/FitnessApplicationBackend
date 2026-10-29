package com.fitnessapp.appAuth.model.dtos.requestDto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordWithOtpRequestDto {
    @NotBlank
    @Email
    @Size(max = 320)
    private String email;

    @NotBlank
    @Size(min = 32, max = 128)
    private String verificationToken;

    @NotBlank
    @Size(min = 8, max = 64, message = "Password must be between 8 and 128 characters")
    private String newPassword;
}

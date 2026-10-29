package com.fitnessapp.appAuth.model.dtos.requestDto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class OtpRequestDto {
    @NotBlank
    @Email
    @Size(max = 320)
    private String email;
}

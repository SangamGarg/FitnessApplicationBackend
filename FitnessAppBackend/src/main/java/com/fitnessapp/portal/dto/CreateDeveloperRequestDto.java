package com.fitnessapp.portal.dto;

import com.fitnessapp.portal.common.enums.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateDeveloperRequestDto(
    @NotBlank @Size(max = 120) String name,
    @NotBlank @Email @Size(max = 254) String email,
    @Size(max = 25) @Pattern(regexp = "^$|^[+0-9() .-]{7,25}$", message = "Enter a valid mobile number") String mobileNumber,
    Gender gender,
    @Size(max = 500) String address
) {}

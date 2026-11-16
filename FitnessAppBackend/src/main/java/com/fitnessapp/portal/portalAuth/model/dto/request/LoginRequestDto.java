package com.fitnessapp.portal.portalAuth.model.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequestDto(@NotBlank @Email String email, @NotBlank String password) {
}

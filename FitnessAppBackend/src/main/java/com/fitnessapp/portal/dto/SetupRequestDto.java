package com.fitnessapp.portal.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public record SetupRequestDto(@NotBlank String token, @NotBlank @Size(min=12,max=128) String password) {}

package com.fitnessapp.gamification.game.model.dto;
import jakarta.validation.constraints.*;
public record GameCompleteRequestDto(@NotNull @Min(0) @Max(10000) Integer repsDetected,@NotNull @Min(1) @Max(7200) Integer durationSeconds,@NotBlank @Size(max=80) String trackerVersion,@DecimalMin("0.0") @DecimalMax("1.0") Double modelConfidence,@Min(0) @Max(7200) Integer holdSeconds){}

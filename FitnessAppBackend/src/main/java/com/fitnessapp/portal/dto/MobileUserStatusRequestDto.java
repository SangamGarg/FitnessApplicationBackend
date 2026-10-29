package com.fitnessapp.portal.dto;

import com.fitnessapp.appAuth.model.entity.MobileAccountStatus;
import jakarta.validation.constraints.NotNull;

public record MobileUserStatusRequestDto(@NotNull MobileAccountStatus status) {
}

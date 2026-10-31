package com.fitnessapp.portal.dto;

import com.fitnessapp.appAuth.model.enums.MobileAccountStatus;
import jakarta.validation.constraints.NotNull;

public record MobileUserStatusRequestDto(@NotNull MobileAccountStatus status) {
}

package com.fitnessapp.portal.portalAuth.model.dto.request;

import com.fitnessapp.appAuth.model.enums.MobileAccountStatus;
import jakarta.validation.constraints.NotNull;

public record MobileUserStatusRequestDto(@NotNull MobileAccountStatus status) {
}

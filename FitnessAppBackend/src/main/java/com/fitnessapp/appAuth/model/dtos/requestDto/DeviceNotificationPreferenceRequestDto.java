package com.fitnessapp.appAuth.model.dtos.requestDto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DeviceNotificationPreferenceRequestDto {
    @NotNull
    private Boolean enabled;
}

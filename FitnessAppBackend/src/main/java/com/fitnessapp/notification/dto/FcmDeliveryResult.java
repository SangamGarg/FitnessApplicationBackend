package com.fitnessapp.notification.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class FcmDeliveryResult {
    int targetedDevices;
    int attemptedDevices;
    int successCount;
    int failureCount;
    int removedInvalidTokenCount;
}

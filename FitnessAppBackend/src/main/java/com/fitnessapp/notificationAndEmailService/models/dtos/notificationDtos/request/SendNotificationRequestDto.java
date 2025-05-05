package com.fitnessapp.notificationAndEmailService.models.dtos.notificationDtos.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SendNotificationRequestDto {
    /**
     * Use for single or multiple devices.
     * Required if not sending to topic, condition, or device group.
     */
    private List<@NotBlank(message = "Device token cannot be blank") String> deviceTokens;

    /**
     * Use for sending to a device group (notification key).
     * Optional, but one of deviceTokens, topic, condition, or deviceGroupKey must be provided.
     */
    private String deviceGroupKey;

    /**
     * Optional: FCM topic to send notification.
     */
    private String topic;

    /**
     * Optional: Condition string for advanced targeting.
     * Will be validated only if provided.
     */
    private String condition;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Body is required")
    private String body;

    /**
     * Optional: Image URL to be displayed in the notification.
     */
    @URL(message = "Invalid image URL")
    private String imageUrl;

    /**
     * Optional: Custom key-value pairs to include with the notification.
     */
    private Map<@NotBlank(message = "Key is required") String, @NotBlank(message = "Value is required") String> data;
}
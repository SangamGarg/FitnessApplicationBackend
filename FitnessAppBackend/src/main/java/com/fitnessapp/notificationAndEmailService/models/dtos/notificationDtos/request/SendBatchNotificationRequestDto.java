package com.fitnessapp.notificationAndEmailService.models.dtos.notificationDtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SendBatchNotificationRequestDto {
    /**
     * List of individual notification request DTOs. Each item represents a notification to a single device, topic, or condition.
     */
    @NotEmpty(message = "Notifications Cannot be Empty")
    private List<@NotBlank(message = "Each notification request in batch must not be blank") SendNotificationRequestDto> notifications;

}

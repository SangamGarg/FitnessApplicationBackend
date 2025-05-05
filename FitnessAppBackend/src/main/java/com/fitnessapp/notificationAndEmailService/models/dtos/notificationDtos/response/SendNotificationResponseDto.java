package com.fitnessapp.notificationAndEmailService.models.dtos.notificationDtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SendNotificationResponseDto {

    public String status;
    public Integer statusCode;
    /**
     * The target of the notification (Device token, topic, or device group).
     */
    private String target;

    /**
     * Additional information about the notification process.
     */
    private Object message;

}
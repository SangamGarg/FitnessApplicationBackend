package com.fitnessapp.notificationAndEmailService.models.dtos.emailDtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailFromUseResponseDto {
    private String status;
    private Integer statusCode;
    private String message;
    private String imageUrl;
}
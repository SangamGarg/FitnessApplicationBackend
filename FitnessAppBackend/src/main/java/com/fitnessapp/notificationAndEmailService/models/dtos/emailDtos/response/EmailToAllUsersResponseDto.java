package com.fitnessapp.notificationAndEmailService.models.dtos.emailDtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailToAllUsersResponseDto {
    private String status;
    private Integer statusCode;
    private String message;
    private List<String> emails;
    private String subject;
    private String body;
}

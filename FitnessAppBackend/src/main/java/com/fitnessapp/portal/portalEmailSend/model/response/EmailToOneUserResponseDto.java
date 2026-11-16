package com.fitnessapp.portal.portalEmailSend.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailToOneUserResponseDto {
    private String status;
    private Integer statusCode;
    private String message;
    private String name;
    private String email;
    private String subject;
    private String body;
}
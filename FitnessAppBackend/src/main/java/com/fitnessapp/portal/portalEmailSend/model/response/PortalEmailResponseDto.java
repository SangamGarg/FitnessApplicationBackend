package com.fitnessapp.portal.portalEmailSend.model.response;

import com.fitnessapp.portal.portalEmailSend.model.EmailRecipientType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortalEmailResponseDto {
    private String status;
    private Integer statusCode;
    private String message;
    private EmailRecipientType recipientType;
    private Integer recipientCount;
    private String name;
    private List<String> emails;
    private String email;
    private String subject;
    private String body;
}
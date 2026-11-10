package com.fitnessapp.support.service.supportEmail;


import com.fitnessapp.support.models.dtos.request.EmailFromUserRequestDto;
import com.fitnessapp.support.models.dtos.response.EmailFromUserResponseDto;
import com.fitnessapp.support.models.dtos.response.SupportEmailHomeResponseDto;
import com.fitnessapp.support.models.entity.SupportTicketEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface SupportEmailService {

    SupportEmailHomeResponseDto getEmailHome();

    EmailFromUserResponseDto sendEmail(
            EmailFromUserRequestDto request,
            List<MultipartFile> attachments
    );

    void sendTicketConfirmationEmail(SupportTicketEntity ticket);

    void sendTicketResolutionEmail(SupportTicketEntity ticket);
}
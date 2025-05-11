package com.fitnessapp.notificationAndEmailService.controller;

import com.fitnessapp.notificationAndEmailService.models.dtos.emailDtos.request.EmailFromUseRequestDto;
import com.fitnessapp.notificationAndEmailService.models.dtos.emailDtos.request.EmailToAllUsersRequestDto;
import com.fitnessapp.notificationAndEmailService.models.dtos.emailDtos.request.EmailToMoreThanOneUserRequestDto;
import com.fitnessapp.notificationAndEmailService.models.dtos.emailDtos.request.EmailToOneUserRequestDto;
import com.fitnessapp.notificationAndEmailService.notificationAndEmailServiceUtilities.AppConstantsNotificationAndEmailService;
import com.fitnessapp.notificationAndEmailService.services.emailService.EmailService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(AppConstantsNotificationAndEmailService.API_PREFIX_EMAIL)
public class EmailController {

    private final EmailService emailService;

    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("from-user")
    public ResponseEntity<?> getMailFromUser(@RequestBody @Valid EmailFromUseRequestDto emailRequestDto) throws MessagingException {
        return emailService.getMailFromUser(emailRequestDto);
    }
}

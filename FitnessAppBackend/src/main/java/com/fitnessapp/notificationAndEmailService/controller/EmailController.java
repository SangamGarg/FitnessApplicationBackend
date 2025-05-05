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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(AppConstantsNotificationAndEmailService.API_PREFIX_EMAIL)
public class EmailController {

    private final EmailService emailService;

    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @GetMapping("from-user")
    public ResponseEntity<?> getMailFromUser(@RequestBody @Valid EmailFromUseRequestDto emailRequestDto) throws MessagingException {
        return emailService.getMailFromUser(emailRequestDto);
    }

    @GetMapping("send-email-one-user")

    public ResponseEntity<?> sendEmailToOneUser(@RequestBody @Valid EmailToOneUserRequestDto emailToOneUserRequestDto) throws MessagingException{
        return emailService.sendEmailToOneUser(emailToOneUserRequestDto);
    }

    @GetMapping("send-email-all-user")

    public ResponseEntity<?> sendEmailToAllUsers(@RequestBody @Valid EmailToAllUsersRequestDto emailToAllUsersRequestDto) throws MessagingException{
        return emailService.sendEmailToAllUsers(emailToAllUsersRequestDto);

    }

    @GetMapping("send-email-more-user")
    public ResponseEntity<?> sendEmailToMoreThanOneUser(@RequestBody @Valid EmailToMoreThanOneUserRequestDto emailToMoreThanOneUserRequestDto) throws MessagingException{
        return emailService.sendEmailToMoreThanOneUser(emailToMoreThanOneUserRequestDto);

    }
}

package com.fitnessapp.DeveloperAndTestingService.notificationAndEmailDevService;

import com.fitnessapp.DeveloperAndTestingService.devAndTestingServiceUtilities.DevAndTestingServiceUtility;
import com.fitnessapp.notificationAndEmailService.models.dtos.emailDtos.request.EmailFromUseRequestDto;
import com.fitnessapp.notificationAndEmailService.models.dtos.emailDtos.request.EmailToAllUsersRequestDto;
import com.fitnessapp.notificationAndEmailService.models.dtos.emailDtos.request.EmailToMoreThanOneUserRequestDto;
import com.fitnessapp.notificationAndEmailService.models.dtos.emailDtos.request.EmailToOneUserRequestDto;
import com.fitnessapp.notificationAndEmailService.services.emailService.EmailService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(DevAndTestingServiceUtility.API_PREFIX_EMAIL)
@Validated
public class DevEmailController {

    private final EmailService emailService;

    public DevEmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("send-email-one-user")

    public ResponseEntity<?> sendEmailToOneUser(@RequestBody @Valid EmailToOneUserRequestDto emailToOneUserRequestDto) throws MessagingException {
        return emailService.sendEmailToOneUser(emailToOneUserRequestDto);
    }

    @PostMapping("send-email-all-user")

    public ResponseEntity<?> sendEmailToAllUsers(@RequestBody @Valid EmailToAllUsersRequestDto emailToAllUsersRequestDto) throws MessagingException {
        return emailService.sendEmailToAllUsers(emailToAllUsersRequestDto);

    }

    @PostMapping("send-email-more-user")
    public ResponseEntity<?> sendEmailToMoreThanOneUser(@RequestBody @Valid EmailToMoreThanOneUserRequestDto emailToMoreThanOneUserRequestDto) throws MessagingException {
        return emailService.sendEmailToMoreThanOneUser(emailToMoreThanOneUserRequestDto);

    }
}

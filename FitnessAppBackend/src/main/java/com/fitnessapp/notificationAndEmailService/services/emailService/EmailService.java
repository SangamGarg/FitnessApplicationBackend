package com.fitnessapp.notificationAndEmailService.services.emailService;

import com.fitnessapp.notificationAndEmailService.models.dtos.emailDtos.request.EmailFromUseRequestDto;
import com.fitnessapp.notificationAndEmailService.models.dtos.emailDtos.request.EmailToAllUsersRequestDto;
import com.fitnessapp.notificationAndEmailService.models.dtos.emailDtos.request.EmailToMoreThanOneUserRequestDto;
import com.fitnessapp.notificationAndEmailService.models.dtos.emailDtos.request.EmailToOneUserRequestDto;
import org.springframework.http.ResponseEntity;

public interface EmailService {
    ResponseEntity<?> getMailFromUser(EmailFromUseRequestDto emailRequestDto);

    ResponseEntity<?> sendEmailToOneUser(EmailToOneUserRequestDto emailToOneUserRequestDto);

    ResponseEntity<?> sendEmailToAllUsers(EmailToAllUsersRequestDto emailToAllUsersRequestDto);

    ResponseEntity<?> sendEmailToMoreThanOneUser(EmailToMoreThanOneUserRequestDto emailToMoreThanOneUserRequestDto);

}
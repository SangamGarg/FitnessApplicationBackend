package com.fitnessapp.email;

import com.fitnessapp.email.dto.request.EmailFromUseRequestDto;
import com.fitnessapp.email.dto.request.EmailToAllUsersRequestDto;
import com.fitnessapp.email.dto.request.EmailToMoreThanOneUserRequestDto;
import com.fitnessapp.email.dto.request.EmailToOneUserRequestDto;
import org.springframework.http.ResponseEntity;

public interface EmailService {
    ResponseEntity<?> getMailFromUser(EmailFromUseRequestDto emailRequestDto);

    ResponseEntity<?> sendEmailToOneUser(EmailToOneUserRequestDto emailToOneUserRequestDto);

    ResponseEntity<?> sendEmailToAllUsers(EmailToAllUsersRequestDto emailToAllUsersRequestDto);

    ResponseEntity<?> sendEmailToMoreThanOneUser(EmailToMoreThanOneUserRequestDto emailToMoreThanOneUserRequestDto);

}
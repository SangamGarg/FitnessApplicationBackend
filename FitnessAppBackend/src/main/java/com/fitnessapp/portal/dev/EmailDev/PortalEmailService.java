package com.fitnessapp.portal.dev.EmailDev;

import com.fitnessapp.portal.dev.EmailDev.model.EmailToAllUsersRequestDto;
import com.fitnessapp.portal.dev.EmailDev.model.EmailToMoreThanOneUserRequestDto;
import com.fitnessapp.portal.dev.EmailDev.model.EmailToOneUserRequestDto;
import org.springframework.http.ResponseEntity;

public interface PortalEmailService {

    ResponseEntity<?> sendEmailToOneUser(EmailToOneUserRequestDto emailToOneUserRequestDto);

    ResponseEntity<?> sendEmailToAllUsers(EmailToAllUsersRequestDto emailToAllUsersRequestDto);

    ResponseEntity<?> sendEmailToMoreThanOneUser(EmailToMoreThanOneUserRequestDto emailToMoreThanOneUserRequestDto);

}
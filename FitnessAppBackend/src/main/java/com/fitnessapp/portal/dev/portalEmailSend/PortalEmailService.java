package com.fitnessapp.portal.dev.portalEmailSend;

import com.fitnessapp.portal.dev.portalEmailSend.model.EmailToAllUsersRequestDto;
import com.fitnessapp.portal.dev.portalEmailSend.model.EmailToMoreThanOneUserRequestDto;
import com.fitnessapp.portal.dev.portalEmailSend.model.EmailToOneUserRequestDto;
import org.springframework.http.ResponseEntity;

public interface PortalEmailService {

    ResponseEntity<?> sendEmailToOneUser(EmailToOneUserRequestDto emailToOneUserRequestDto);

    ResponseEntity<?> sendEmailToAllUsers(EmailToAllUsersRequestDto emailToAllUsersRequestDto);

    ResponseEntity<?> sendEmailToMoreThanOneUser(EmailToMoreThanOneUserRequestDto emailToMoreThanOneUserRequestDto);

}
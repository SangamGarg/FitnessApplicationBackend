package com.fitnessapp.portal.portalEmailSend.service;

import com.fitnessapp.portal.portalEmailSend.model.request.EmailToAllUsersRequestDto;
import com.fitnessapp.portal.portalEmailSend.model.request.EmailToMoreThanOneUserRequestDto;
import com.fitnessapp.portal.portalEmailSend.model.request.EmailToOneUserRequestDto;
import com.fitnessapp.portal.portalEmailSend.model.response.EmailToAllUsersResponseDto;
import com.fitnessapp.portal.portalEmailSend.model.response.EmailToMoreThanOneUserResponseDto;
import com.fitnessapp.portal.portalEmailSend.model.response.EmailToOneUserResponseDto;
import org.springframework.http.ResponseEntity;

public interface PortalEmailService {

    ResponseEntity<EmailToOneUserResponseDto> sendEmailToOneUser(EmailToOneUserRequestDto emailToOneUserRequestDto);

    ResponseEntity<EmailToAllUsersResponseDto> sendEmailToAllUsers(EmailToAllUsersRequestDto emailToAllUsersRequestDto);

    ResponseEntity<EmailToMoreThanOneUserResponseDto> sendEmailToMoreThanOneUser(EmailToMoreThanOneUserRequestDto emailToMoreThanOneUserRequestDto);

}
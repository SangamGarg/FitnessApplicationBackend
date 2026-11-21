package com.fitnessapp.portal.portalEmailSend.service;

import com.fitnessapp.portal.portalEmailSend.model.request.EmailRequestDto;
import com.fitnessapp.portal.portalEmailSend.model.response.PortalEmailResponseDto;
import org.springframework.http.ResponseEntity;

public interface PortalEmailService {

    ResponseEntity<PortalEmailResponseDto> sendEmail(EmailRequestDto requestDto);

}
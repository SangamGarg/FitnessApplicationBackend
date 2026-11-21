package com.fitnessapp.portal.portalEmailSend.controller;

import com.fitnessapp.portal.portalEmailSend.model.request.EmailRequestDto;
import com.fitnessapp.portal.portalEmailSend.model.response.PortalEmailResponseDto;
import com.fitnessapp.portal.portalEmailSend.service.PortalEmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.fitnessapp.utils.portalUtil.PortalUtil.PORTAL_DEV_EMAIL_ENDPOINT;

@RestController
@RequestMapping(PORTAL_DEV_EMAIL_ENDPOINT)
@Validated
@RequiredArgsConstructor
public class PortalEmailController {

    private final PortalEmailService portalEmailService;

    @PostMapping("/send")
    public ResponseEntity<PortalEmailResponseDto> sendEmail(
            @RequestBody @Valid EmailRequestDto requestDto
    ) {
        return portalEmailService.sendEmail(requestDto);
    }

}

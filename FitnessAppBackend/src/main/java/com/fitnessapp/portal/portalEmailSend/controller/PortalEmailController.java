package com.fitnessapp.portal.portalEmailSend.controller;

import com.fitnessapp.portal.portalEmailSend.model.request.EmailToAllUsersRequestDto;
import com.fitnessapp.portal.portalEmailSend.model.request.EmailToMoreThanOneUserRequestDto;
import com.fitnessapp.portal.portalEmailSend.model.request.EmailToOneUserRequestDto;
import com.fitnessapp.portal.portalEmailSend.model.response.EmailToAllUsersResponseDto;
import com.fitnessapp.portal.portalEmailSend.model.response.EmailToMoreThanOneUserResponseDto;
import com.fitnessapp.portal.portalEmailSend.model.response.EmailToOneUserResponseDto;
import com.fitnessapp.portal.portalEmailSend.service.PortalEmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.fitnessapp.utils.portalUtil.PortalUtil.PORTAL_EMAIL_ENDPOINT;

@RestController
@RequestMapping(PORTAL_EMAIL_ENDPOINT)
@Validated
@RequiredArgsConstructor
public class PortalEmailController {

    private final PortalEmailService portalEmailService;


    @PostMapping("/send-one-user")

    public ResponseEntity<EmailToOneUserResponseDto> sendEmailToOneUser(@RequestBody @Valid EmailToOneUserRequestDto emailToOneUserRequestDto) {
        return portalEmailService.sendEmailToOneUser(emailToOneUserRequestDto);
    }

    @PostMapping("/send-all-user")

    public ResponseEntity<EmailToAllUsersResponseDto> sendEmailToAllUsers(@RequestBody @Valid EmailToAllUsersRequestDto emailToAllUsersRequestDto) {
        return portalEmailService.sendEmailToAllUsers(emailToAllUsersRequestDto);
    }

    @PostMapping("/send-more-user")
    public ResponseEntity<EmailToMoreThanOneUserResponseDto> sendEmailToMoreThanOneUser(@RequestBody @Valid EmailToMoreThanOneUserRequestDto emailToMoreThanOneUserRequestDto) {
        return portalEmailService.sendEmailToMoreThanOneUser(emailToMoreThanOneUserRequestDto);

    }
}

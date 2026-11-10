package com.fitnessapp.portal.dev.EmailDev;

import com.fitnessapp.portal.dev.EmailDev.model.EmailToAllUsersRequestDto;
import com.fitnessapp.portal.dev.EmailDev.model.EmailToMoreThanOneUserRequestDto;
import com.fitnessapp.portal.dev.EmailDev.model.EmailToOneUserRequestDto;
import jakarta.mail.MessagingException;
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


    @PostMapping("send-email-one-user")

    public ResponseEntity<?> sendEmailToOneUser(@RequestBody @Valid EmailToOneUserRequestDto emailToOneUserRequestDto) throws MessagingException {
        return portalEmailService.sendEmailToOneUser(emailToOneUserRequestDto);
    }

    @PostMapping("send-email-all-user")

    public ResponseEntity<?> sendEmailToAllUsers(@RequestBody @Valid EmailToAllUsersRequestDto emailToAllUsersRequestDto) throws MessagingException {
        return portalEmailService.sendEmailToAllUsers(emailToAllUsersRequestDto);
    }

    @PostMapping("send-email-more-user")
    public ResponseEntity<?> sendEmailToMoreThanOneUser(@RequestBody @Valid EmailToMoreThanOneUserRequestDto emailToMoreThanOneUserRequestDto) throws MessagingException {
        return portalEmailService.sendEmailToMoreThanOneUser(emailToMoreThanOneUserRequestDto);

    }
}

package com.fitnessapp.dev.notificationAndEmailDevService;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import com.fitnessapp.dev.devAndTestingServiceUtilities.DevAndTestingServiceUtility;
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
@Tag(name = "Dev Email", description = "Endpoints for dev email")
public class DevEmailController {

    private final EmailService emailService;

    public DevEmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @Operation(summary = "send Email To One User", description = """
        Executes the send email to one user operation.
        """)
    @PostMapping("send-email-one-user")

    public ResponseEntity<?> sendEmailToOneUser(@RequestBody @Valid EmailToOneUserRequestDto emailToOneUserRequestDto) throws MessagingException {
        return emailService.sendEmailToOneUser(emailToOneUserRequestDto);
    }

    @Operation(summary = "send Email To All Users", description = """
        Executes the send email to all users operation.
        """)
    @PostMapping("send-email-all-user")

    public ResponseEntity<?> sendEmailToAllUsers(@RequestBody @Valid EmailToAllUsersRequestDto emailToAllUsersRequestDto) throws MessagingException {
        return emailService.sendEmailToAllUsers(emailToAllUsersRequestDto);

    }

    @Operation(summary = "send Email To More Than One User", description = """
        Executes the send email to more than one user operation.
        """)
    @PostMapping("send-email-more-user")
    public ResponseEntity<?> sendEmailToMoreThanOneUser(@RequestBody @Valid EmailToMoreThanOneUserRequestDto emailToMoreThanOneUserRequestDto) throws MessagingException {
        return emailService.sendEmailToMoreThanOneUser(emailToMoreThanOneUserRequestDto);

    }
}

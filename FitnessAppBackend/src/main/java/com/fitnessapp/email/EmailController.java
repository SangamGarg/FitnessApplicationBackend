package com.fitnessapp.email;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import com.fitnessapp.email.dto.request.EmailFromUseRequestDto;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(AppConstantsEmail.API_PREFIX_EMAIL)
@Tag(name = "Email", description = "Endpoints for email")
public class EmailController {

    private final EmailService emailService;

    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @Operation(summary = "get Mail From User", description = """
        Executes the get mail from user operation.
        """)
    @PostMapping("from-user")
    public ResponseEntity<?> getMailFromUser(@RequestBody @Valid EmailFromUseRequestDto emailRequestDto) throws MessagingException {
        return emailService.getMailFromUser(emailRequestDto);
    }
}

package com.fitnessapp.portal.portalEmailSend.service;

import com.fitnessapp.portal.portalEmailSend.model.request.EmailToAllUsersRequestDto;
import com.fitnessapp.portal.portalEmailSend.model.request.EmailToMoreThanOneUserRequestDto;
import com.fitnessapp.portal.portalEmailSend.model.request.EmailToOneUserRequestDto;
import com.fitnessapp.portal.portalEmailSend.model.response.EmailToAllUsersResponseDto;
import com.fitnessapp.portal.portalEmailSend.model.response.EmailToMoreThanOneUserResponseDto;
import com.fitnessapp.portal.portalEmailSend.model.response.EmailToOneUserResponseDto;
import com.fitnessapp.utils.appUtil.AppResponseStatus;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Year;
import java.util.List;

/**
 * Service implementation for handling outbound and contact-form email communications.
 */
@Slf4j
@Service
public class PortalEmailServiceImpl implements PortalEmailService {

    private static final String DEFAULT_RECIPIENT_NAME = "Fitness Enthusiast";
    private static final int CURRENT_YEAR = Year.now().getValue();
    private final JavaMailSender mailSender;

    public PortalEmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public ResponseEntity<EmailToOneUserResponseDto> sendEmailToOneUser(EmailToOneUserRequestDto requestDto) {
        log.info("Initiating single-user outbound email to: [{}] with subject: [{}]", requestDto.getEmail(), requestDto.getSubject());

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, StandardCharsets.UTF_8.name());

            helper.setTo(requestDto.getEmail());
            helper.setSubject(requestDto.getSubject());
            helper.setText(buildEmailTemplate(requestDto.getName(), requestDto.getHtmlBody()), true);

            mailSender.send(mimeMessage);
            log.info("Successfully sent email to single user: [{}]", requestDto.getEmail());

            return ResponseEntity.ok(EmailToOneUserResponseDto.builder()
                    .status(AppResponseStatus.SUCCESS.name())
                    .statusCode(200)
                    .message("Email Sent Successfully")
                    .email(requestDto.getEmail())
                    .name(requestDto.getName())
                    .subject(requestDto.getSubject())
                    .body(requestDto.getBody())
                    .build());

        } catch (MessagingException e) {
            log.error("Failed to construct or deliver email to [{}]: {}", requestDto.getEmail(), e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        } catch (Exception e) {
            log.error("Unexpected error occurred while dispatching email to [{}]: {}", requestDto.getEmail(), e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Broadcasts an announcement or alert to all registered user email addresses.
     * Uses BCC recipients to preserve user privacy.
     */
    @Override
    public ResponseEntity<EmailToAllUsersResponseDto> sendEmailToAllUsers(EmailToAllUsersRequestDto requestDto) {
        List<String> emails = requestDto.getEmails();
        int recipientCount = emails != null ? emails.size() : 0;
        log.info("Initiating broadcast email to all users. Total recipients: {}, subject: [{}]", recipientCount, requestDto.getSubject());

        if (recipientCount == 0) {
            log.warn("Broadcast aborted: Recipient email list is null or empty");
            return ResponseEntity.badRequest().body(EmailToAllUsersResponseDto.builder()
                    .status(AppResponseStatus.FAILED.name())
                    .statusCode(HttpStatus.BAD_REQUEST.value())
                    .message("Recipient email list cannot be empty")
                    .emails(requestDto.getEmails())
                    .subject(requestDto.getSubject())
                    .body(requestDto.getBody())
                    .build());
        }

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, StandardCharsets.UTF_8.name());

            // Use setBcc to prevent exposing all customer emails in public headers
            helper.setBcc(emails.toArray(new String[0]));
            helper.setSubject(requestDto.getSubject());
            helper.setText(buildEmailTemplate(DEFAULT_RECIPIENT_NAME, requestDto.getHtmlBody()), true);

            mailSender.send(mimeMessage);
            log.info("Successfully dispatched broadcast email to {} recipients", recipientCount);

            return ResponseEntity.ok(EmailToAllUsersResponseDto.builder()
                    .status(AppResponseStatus.SUCCESS.name())
                    .statusCode(200)
                    .message("Email Sent Successfully")
                    .emails(requestDto.getEmails())
                    .subject(requestDto.getSubject())
                    .body(requestDto.getBody())
                    .build());

        } catch (MessagingException e) {
            log.error("Failed to construct or deliver broadcast email to {} recipients: {}", recipientCount, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        } catch (Exception e) {
            log.error("Unexpected error occurred during mass email broadcast: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Sends an email to a targeted subset of multiple users.
     * Uses BCC recipients to safeguard recipient privacy.
     */
    @Override
    public ResponseEntity<EmailToMoreThanOneUserResponseDto> sendEmailToMoreThanOneUser(EmailToMoreThanOneUserRequestDto requestDto) {
        List<String> emails = requestDto.getEmails();
        int recipientCount = emails != null ? emails.size() : 0;
        log.info("Initiating multi-user email to {} recipients with subject: [{}]", recipientCount, requestDto.getSubject());

        if (recipientCount == 0) {
            log.warn("Multi-user email dispatch aborted: Recipient list is empty");
            return ResponseEntity.badRequest().body(EmailToMoreThanOneUserResponseDto.builder()
                    .status(AppResponseStatus.FAILED.name())
                    .statusCode(HttpStatus.BAD_REQUEST.value())
                    .message("Recipient email list cannot be empty")
                    .emails(requestDto.getEmails())
                    .subject(requestDto.getSubject())
                    .body(requestDto.getBody())
                    .build());
        }

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, StandardCharsets.UTF_8.name());

            helper.setBcc(emails.toArray(new String[0]));
            helper.setSubject(requestDto.getSubject());
            helper.setText(buildEmailTemplate(DEFAULT_RECIPIENT_NAME, requestDto.getHtmlBody()), true);

            mailSender.send(mimeMessage);
            log.info("Successfully delivered targeted email to {} users", recipientCount);

            return ResponseEntity.ok(EmailToMoreThanOneUserResponseDto.builder()
                    .status(AppResponseStatus.SUCCESS.name())
                    .statusCode(200)
                    .message("Email Sent Successfully")
                    .emails(requestDto.getEmails())
                    .subject(requestDto.getSubject())
                    .body(requestDto.getBody())
                    .build());

        } catch (MessagingException e) {
            log.error("Failed to construct or deliver multi-user email to {} recipients: {}", recipientCount, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        } catch (Exception e) {
            log.error("Unexpected error during multi-user email dispatch: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }


    private String buildEmailTemplate(String recipientName, String htmlBody) {
        String safeName = (recipientName != null && !recipientName.isBlank()) ? recipientName : DEFAULT_RECIPIENT_NAME;
        String safeContent = (htmlBody != null) ? htmlBody : "";

        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body {
                            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
                            background-color: #f8fafc;
                            padding: 24px;
                            color: #1e293b;
                            line-height: 1.6;
                        }
                        .email-container {
                            background-color: #ffffff;
                            padding: 32px;
                            border-radius: 8px;
                            border: 1px solid #e2e8f0;
                            max-width: 600px;
                            margin: 0 auto;
                        }
                        h2 {
                            color: #0f172a;
                            margin-top: 0;
                            font-size: 20px;
                        }
                        .content {
                            margin-top: 16px;
                            font-size: 15px;
                        }
                        .sign-off {
                            margin-top: 36px;
                            border-top: 1px solid #f1f5f9;
                            padding-top: 18px;
                        }
                        .footer {
                            margin-top: 24px;
                            font-size: 12px;
                            color: #94a3b8;
                            text-align: center;
                        }
                    </style>
                </head>
                <body>
                    <div class="email-container">
                        <h2>Hello %s,</h2>
                        <div class="content">
                            %s
                        </div>
                        <div class="sign-off">
                            <p>Regards,<br/><strong>Fitness App Team</strong></p>
                        </div>
                    </div>
                    <div class="footer">
                        &copy; %d Fitness App. All rights reserved.
                    </div>
                </body>
                </html>
                """.formatted(safeName, safeContent, CURRENT_YEAR);
    }
}
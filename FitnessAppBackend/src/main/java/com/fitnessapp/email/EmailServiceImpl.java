package com.fitnessapp.email;

import com.fitnessapp.email.dto.request.EmailFromUseRequestDto;
import com.fitnessapp.email.dto.request.EmailToAllUsersRequestDto;
import com.fitnessapp.email.dto.request.EmailToMoreThanOneUserRequestDto;
import com.fitnessapp.email.dto.request.EmailToOneUserRequestDto;
import com.fitnessapp.email.dto.response.EmailFromUseResponseDto;
import com.fitnessapp.email.dto.response.EmailToAllUsersResponseDto;
import com.fitnessapp.email.dto.response.EmailToMoreThanOneUserResponseDto;
import com.fitnessapp.email.dto.response.EmailToOneUserResponseDto;
import com.fitnessapp.portal.dev.assetUploadDev.model.AppMediaAssetEntity;
import com.fitnessapp.portal.dev.assetUploadDev.repo.AppMediaAssetRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
public class EmailServiceImpl implements EmailService {

    private static final String DEFAULT_RECIPIENT_NAME = "Fitness Enthusiast";
    private static final int CURRENT_YEAR = Year.now().getValue();

    private final JavaMailSender mailSender;
    private final AppMediaAssetRepository imageAndAboutRepository;

    //here set the receiver like (contact.fitnessapp@app.in) this email receives the email send by user
    @Value("${app.mail.support-email:sangamgarg17@gmail.com}")
    private String supportEmail;

    public EmailServiceImpl(JavaMailSender mailSender, AppMediaAssetRepository imageAndAboutRepository) {
        this.mailSender = mailSender;
        this.imageAndAboutRepository = imageAndAboutRepository;
    }

    /**
     * Receives feedback/inquiries submitted by a user and forwards them to the internal support desk.
     */
    @Override
    public ResponseEntity<?> getMailFromUser(EmailFromUseRequestDto requestDto) {
        log.info("Processing inbound query email from user: [{}] with subject: [{}]", requestDto.getEmail(), requestDto.getSubject());

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, StandardCharsets.UTF_8.name());

            helper.setTo(supportEmail);
            helper.setReplyTo(requestDto.getEmail());
            helper.setSubject("Query From User Fitness Application: " + requestDto.getSubject());
            helper.setText(buildUserQueryTemplate(requestDto), true);

            mailSender.send(mimeMessage);
            log.info("Successfully delivered user query from [{}] to support inbox [{}]", requestDto.getEmail(), supportEmail);

            return ResponseEntity.ok(EmailFromUseResponseDto.builder()
                    .status(AppConstantsEmail.SUCCESS_API_STATUS)
                    .statusCode(200)
                    .message("We have received your email and will contact you soon")
                    .imageUrl(getImageUrlAndAbout().getImageUrl())
                    .build());

        } catch (MessagingException e) {
            log.error("Failed to construct or deliver user query email from [{}]: {}", requestDto.getEmail(), e.getMessage(), e);
            return ResponseEntity.internalServerError().body(AppConstantsEmail.errorDto(e));
        } catch (Exception e) {
            log.error("Unexpected error occurred while processing user query from [{}]: {}", requestDto.getEmail(), e.getMessage(), e);
            return ResponseEntity.internalServerError().body(AppConstantsEmail.errorDto(e));
        }
    }

    /**
     * Sends an individual transactional or notification email to a single user.
     */
    @Override
    public ResponseEntity<?> sendEmailToOneUser(EmailToOneUserRequestDto requestDto) {
        log.info("Initiating single-user outbound email to: [{}] with subject: [{}]", requestDto.getEmail(), requestDto.getSubject());

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, StandardCharsets.UTF_8.name());

            helper.setTo(requestDto.getEmail());
            helper.setSubject(requestDto.getSubject());
            helper.setText(buildUserNotificationTemplate(requestDto.getName(), requestDto.getHtmlBody()), true);

            mailSender.send(mimeMessage);
            log.info("Successfully sent email to single user: [{}]", requestDto.getEmail());

            return ResponseEntity.ok(EmailToOneUserResponseDto.builder()
                    .status(AppConstantsEmail.SUCCESS_API_STATUS)
                    .statusCode(200)
                    .message("Email Sent Successfully")
                    .email(requestDto.getEmail())
                    .name(requestDto.getName())
                    .subject(requestDto.getSubject())
                    .body(requestDto.getBody())
                    .build());

        } catch (MessagingException e) {
            log.error("Failed to construct or deliver email to [{}]: {}", requestDto.getEmail(), e.getMessage(), e);
            return ResponseEntity.internalServerError().body(AppConstantsEmail.errorDto(e));
        } catch (Exception e) {
            log.error("Unexpected error occurred while dispatching email to [{}]: {}", requestDto.getEmail(), e.getMessage(), e);
            return ResponseEntity.internalServerError().body(AppConstantsEmail.errorDto(e));
        }
    }

    /**
     * Broadcasts an announcement or alert to all registered user email addresses.
     * Uses BCC recipients to preserve user privacy.
     */
    @Override
    public ResponseEntity<?> sendEmailToAllUsers(EmailToAllUsersRequestDto requestDto) {
        List<String> emails = requestDto.getEmails();
        int recipientCount = emails != null ? emails.size() : 0;
        log.info("Initiating broadcast email to all users. Total recipients: {}, subject: [{}]", recipientCount, requestDto.getSubject());

        if (recipientCount == 0) {
            log.warn("Broadcast aborted: Recipient email list is null or empty");
            return ResponseEntity.badRequest().body("Recipient email list cannot be empty");
        }

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, StandardCharsets.UTF_8.name());

            // Use setBcc to prevent exposing all customer emails in public headers
            helper.setBcc(emails.toArray(new String[0]));
            helper.setSubject(requestDto.getSubject());
            helper.setText(buildUserNotificationTemplate(DEFAULT_RECIPIENT_NAME, requestDto.getHtmlBody()), true);

            mailSender.send(mimeMessage);
            log.info("Successfully dispatched broadcast email to {} recipients", recipientCount);

            return ResponseEntity.ok(EmailToAllUsersResponseDto.builder()
                    .status(AppConstantsEmail.SUCCESS_API_STATUS)
                    .statusCode(200)
                    .message("Email Sent Successfully")
                    .emails(requestDto.getEmails())
                    .subject(requestDto.getSubject())
                    .body(requestDto.getBody())
                    .build());

        } catch (MessagingException e) {
            log.error("Failed to construct or deliver broadcast email to {} recipients: {}", recipientCount, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(AppConstantsEmail.errorDto(e));
        } catch (Exception e) {
            log.error("Unexpected error occurred during mass email broadcast: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(AppConstantsEmail.errorDto(e));
        }
    }

    /**
     * Sends an email to a targeted subset of multiple users.
     * Uses BCC recipients to safeguard recipient privacy.
     */
    @Override
    public ResponseEntity<?> sendEmailToMoreThanOneUser(EmailToMoreThanOneUserRequestDto requestDto) {
        List<String> emails = requestDto.getEmails();
        int recipientCount = emails != null ? emails.size() : 0;
        log.info("Initiating multi-user email to {} recipients with subject: [{}]", recipientCount, requestDto.getSubject());

        if (recipientCount == 0) {
            log.warn("Multi-user email dispatch aborted: Recipient list is empty");
            return ResponseEntity.badRequest().body("Recipient email list cannot be empty");
        }

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, StandardCharsets.UTF_8.name());

            helper.setBcc(emails.toArray(new String[0]));
            helper.setSubject(requestDto.getSubject());
            helper.setText(buildUserNotificationTemplate(DEFAULT_RECIPIENT_NAME, requestDto.getHtmlBody()), true);

            mailSender.send(mimeMessage);
            log.info("Successfully delivered targeted email to {} users", recipientCount);

            return ResponseEntity.ok(EmailToMoreThanOneUserResponseDto.builder()
                    .status(AppConstantsEmail.SUCCESS_API_STATUS)
                    .statusCode(200)
                    .message("Email Sent Successfully")
                    .emails(requestDto.getEmails())
                    .subject(requestDto.getSubject())
                    .body(requestDto.getBody())
                    .build());

        } catch (MessagingException e) {
            log.error("Failed to construct or deliver multi-user email to {} recipients: {}", recipientCount, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(AppConstantsEmail.errorDto(e));
        } catch (Exception e) {
            log.error("Unexpected error during multi-user email dispatch: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(AppConstantsEmail.errorDto(e));
        }
    }

    /**
     * Retrieves application branding image and metadata, providing a fallback on failure.
     */
    private AppMediaAssetEntity getImageUrlAndAbout() {
//        try {
//            log.trace("Fetching email branding image metadata from repository");
//            return imageAndAboutRepository.findByAssetKeyAndIsActiveTrue("email")
//                    .orElseGet(() -> {
//                        log.debug("No branding entity found with name 'email'; using default fallback");
//                        return AppMediaAssetEntity.builder().imageUrl("Not Found").build();
//                    });
//        } catch (Exception e) {
//            log.warn("Could not retrieve branding image metadata from DB: {}", e.getMessage());
//            return new AppMediaAssetEntity(0L, "Error Getting Name", "Error Getting Image", "Error Getting About");
//        }

        return null;
    }

    // =========================================================================
    // HTML Template Generators
    // =========================================================================

    private String buildUserQueryTemplate(EmailFromUseRequestDto dto) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body {
                            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                            background-color: #f4f6f8;
                            padding: 20px;
                            color: #333333;
                            line-height: 1.6;
                        }
                        .email-container {
                            background-color: #ffffff;
                            padding: 30px;
                            border-radius: 8px;
                            border: 1px solid #e1e4e8;
                            max-width: 650px;
                            margin: 0 auto;
                        }
                        h2 {
                            color: #007bff;
                            margin-top: 0;
                            border-bottom: 2px solid #007bff;
                            padding-bottom: 8px;
                        }
                        .info-row {
                            margin: 12px 0;
                        }
                        .info-label {
                            font-weight: 600;
                            color: #555555;
                            min-width: 80px;
                            display: inline-block;
                        }
                        .message-box {
                            background-color: #f8f9fa;
                            border-left: 4px solid #007bff;
                            padding: 14px;
                            margin-top: 14px;
                            border-radius: 4px;
                        }
                        .footer {
                            margin-top: 30px;
                            font-size: 12px;
                            color: #888888;
                            text-align: center;
                        }
                    </style>
                </head>
                <body>
                    <div class="email-container">
                        <h2>Inbound User Inquiry</h2>
                        <div class="info-row"><span class="info-label">Sender:</span> %s</div>
                        <div class="info-row"><span class="info-label">Email:</span> %s</div>
                        <div class="info-row"><span class="info-label">Subject:</span> %s</div>
                        <div class="message-box">
                            <span class="info-label">Message:</span><br/>
                            %s
                        </div>
                        <p style="margin-top: 30px;">Regards,<br/><strong>Fitness App Platform</strong></p>
                    </div>
                    <div class="footer">
                        &copy; %d Fitness App. Internal support communication.
                    </div>
                </body>
                </html>
                """.formatted(
                dto.getName() != null ? dto.getName() : "Anonymous",
                dto.getEmail(),
                dto.getSubject(),
                dto.getBody(),
                CURRENT_YEAR
        );
    }

    private String buildUserNotificationTemplate(String recipientName, String htmlBody) {
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
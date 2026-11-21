package com.fitnessapp.portal.portalEmailSend.service;

import com.fitnessapp.appAuth.model.enums.MobileAccountStatus;
import com.fitnessapp.appAuth.repo.UserRepo;
import com.fitnessapp.portal.portalEmailSend.model.EmailRecipientType;
import com.fitnessapp.portal.portalEmailSend.model.request.EmailRequestDto;
import com.fitnessapp.portal.portalEmailSend.model.response.PortalEmailResponseDto;
import com.fitnessapp.utils.appUtil.AppResponseStatus;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.time.Year;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
public class PortalEmailServiceImpl implements PortalEmailService {

    private static final String DEFAULT_RECIPIENT_NAME = "Fitness Enthusiast";
    private static final int MAX_BCC_RECIPIENTS = 50;
    private static final int MAX_RECIPIENTS_PER_REQUEST = 5_000;
    private static final int CURRENT_YEAR = Year.now().getValue();

    private final JavaMailSender mailSender;
    private final UserRepo userRepo;
    private final String senderEmail;

    public PortalEmailServiceImpl(
            JavaMailSender mailSender,
            UserRepo userRepo,
            @Value("${spring.mail.username}") String senderEmail
    ) {
        this.mailSender = mailSender;
        this.userRepo = userRepo;
        this.senderEmail = senderEmail;
    }

    @Override
    public ResponseEntity<PortalEmailResponseDto> sendEmail(EmailRequestDto requestDto) {

        validateRequest(requestDto);
        EmailRecipientType recipientType = requestDto.getRecipientType();

        if (recipientType == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "recipientType is required (SINGLE, SELECTED, or ALL)"
            );
        }
        validateRecipientFields(requestDto, recipientType);

        int recipientCount;
        List<String> responseEmails = null;
        String responseEmail = null;
        String responseName = null;

        switch (recipientType) {
            case SINGLE -> {
                responseEmail = requestDto.getEmail().trim();
                responseName = normalizeName(requestDto.getName());
                dispatchBatch(
                        List.of(responseEmail),
                        requestDto.getSubject().trim(),
                        responseName,
                        requestDto.getBody(),
                        requestDto.getHtmlBody(),
                        true
                );
                recipientCount = 1;
            }
            case SELECTED -> {
                responseEmails = normalizeRecipients(requestDto.getEmails());
                dispatchInBatches(
                        responseEmails,
                        requestDto.getSubject().trim(),
                        requestDto.getBody(),
                        requestDto.getHtmlBody()
                );
                recipientCount = responseEmails.size();
            }
            case ALL -> recipientCount = dispatchToAllActiveUsers(requestDto);
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported recipient type");
        }

        log.info("Portal email send completed. recipientType={}, recipientCount={}",
                recipientType, recipientCount);
        return ResponseEntity.ok(PortalEmailResponseDto.builder()
                .status(AppResponseStatus.SUCCESS.name())
                .statusCode(HttpStatus.OK.value())
                .message("Email sent successfully to " + recipientCount + " recipient(s)")
                .recipientType(recipientType)
                .recipientCount(recipientCount)
                .name(responseName)
                .email(responseEmail)
                .emails(responseEmails)
                .subject(requestDto.getSubject().trim())
                .body(requestDto.getBody())
                .build());
    }


    private void validateRecipientFields(EmailRequestDto requestDto, EmailRecipientType type) {
        boolean hasEmail = StringUtils.hasText(requestDto.getEmail());
        boolean hasEmails = requestDto.getEmails() != null && !requestDto.getEmails().isEmpty();
        switch (type) {
            case SINGLE -> {
                if (!hasEmail) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "email is required for SINGLE recipient type");
                }
                if (hasEmails) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "emails must not be provided for SINGLE recipient type");
                }
                if (requestDto.getEmail().length() > 320) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "email must be 320 characters or fewer");
                }
            }
            case SELECTED -> {
                if (hasEmail) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Use emails, not email, for SELECTED recipient type");
                }
                if (!hasEmails) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "emails are required for SELECTED recipient type");
                }
                if (requestDto.getEmails().size() > MAX_RECIPIENTS_PER_REQUEST) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "No more than " + MAX_RECIPIENTS_PER_REQUEST + " recipients can be included"
                    );
                }
            }
            case ALL -> {
                if (hasEmail || hasEmails) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Do not provide email or emails for ALL recipient type; recipients are loaded from active app users"
                    );
                }
            }
        }
    }

    private int dispatchToAllActiveUsers(EmailRequestDto requestDto) {
        int pageNumber = 0;
        int batchNumber = 0;
        int recipientCount = 0;
        Set<String> seenAddresses = new HashSet<>();

        while (true) {
            List<String> addresses = userRepo.findEmailsByAccountStatus(
                    MobileAccountStatus.ACTIVE,
                    PageRequest.of(pageNumber++, MAX_BCC_RECIPIENTS)
            );
            if (addresses.isEmpty()) {
                break;
            }

            List<String> batch = uniqueStoredAddresses(addresses, seenAddresses);
            if (!batch.isEmpty()) {
                batchNumber++;
                try {
                    dispatchBatch(
                            batch,
                            requestDto.getSubject().trim(),
                            DEFAULT_RECIPIENT_NAME,
                            requestDto.getBody(),
                            requestDto.getHtmlBody(),
                            false
                    );
                } catch (ResponseStatusException exception) {
                    String message = batchNumber == 1
                            ? "Email delivery failed in batch 1"
                            : "Email delivery failed in batch " + batchNumber
                            + "; earlier batches may already have been delivered";
                    throw new ResponseStatusException(exception.getStatusCode(), message);
                }
                recipientCount = Math.addExact(recipientCount, batch.size());
            }

            if (addresses.size() < MAX_BCC_RECIPIENTS) {
                break;
            }
        }

        if (recipientCount == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No active users with email addresses were found");
        }
        return recipientCount;
    }

    private List<String> uniqueStoredAddresses(List<String> addresses, Set<String> seenAddresses) {
        List<String> unique = new ArrayList<>(addresses.size());
        for (String address : addresses) {
            if (!StringUtils.hasText(address)) {
                continue;
            }
            String trimmed = address.trim();
            String normalized = trimmed.toLowerCase(Locale.ROOT);
            if (seenAddresses.add(normalized)) {
                try {
                    new InternetAddress(trimmed, true).validate();
                } catch (jakarta.mail.internet.AddressException exception) {
                    log.error("An active account contains an invalid email address; refusing ALL-user send");
                    throw new ResponseStatusException(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "An active user has an invalid email address"
                    );
                }
                unique.add(trimmed);
            }
        }
        return unique;
    }

    private List<String> normalizeRecipients(List<String> emails) {
        Map<String, String> uniqueEmails = new LinkedHashMap<>();
        for (String email : emails) {
            if (!StringUtils.hasText(email)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Recipient emails cannot be blank");
            }
            String trimmed = email.trim();
            if (trimmed.length() > 320) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Recipient emails must be 320 characters or fewer");
            }
            String normalized = trimmed.toLowerCase(Locale.ROOT);
            uniqueEmails.putIfAbsent(normalized, trimmed);
        }
        if (uniqueEmails.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one recipient email is required");
        }
        return new ArrayList<>(uniqueEmails.values());
    }

    private void dispatchInBatches(
            List<String> recipients,
            String subject,
            String plainText,
            String htmlBody
    ) {
        int batchCount = (recipients.size() + MAX_BCC_RECIPIENTS - 1) / MAX_BCC_RECIPIENTS;
        for (int batchIndex = 0; batchIndex < batchCount; batchIndex++) {
            int fromIndex = batchIndex * MAX_BCC_RECIPIENTS;
            int toIndex = Math.min(fromIndex + MAX_BCC_RECIPIENTS, recipients.size());
            try {
                dispatchBatch(
                        recipients.subList(fromIndex, toIndex),
                        subject,
                        DEFAULT_RECIPIENT_NAME,
                        plainText,
                        htmlBody,
                        false
                );
            } catch (ResponseStatusException exception) {
                String message = batchIndex == 0
                        ? "Email delivery failed in batch 1 of " + batchCount
                        : "Email delivery failed in batch " + (batchIndex + 1) + " of " + batchCount
                        + "; earlier batches may already have been delivered";
                throw new ResponseStatusException(exception.getStatusCode(), message);
            }
        }
    }

    private void dispatchBatch(
            List<String> recipients,
            String subject,
            String recipientName,
            String plainText,
            String htmlBody,
            boolean directRecipient
    ) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    true,
                    StandardCharsets.UTF_8.name()
            );
            helper.setFrom(senderEmail);
            helper.setSubject(subject);
            helper.setText(buildPlainTextBody(plainText), buildHtmlTemplate(recipientName, htmlBody));
            if (directRecipient) {
                helper.setTo(recipients.getFirst());
            } else {
                helper.setBcc(recipients.toArray(String[]::new));
            }
            mailSender.send(message);
        } catch (MessagingException | MailException exception) {
            log.error("Failed to send portal email batch for {} recipients", recipients.size(), exception);
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Email provider rejected or failed to deliver the message"
            );
        }
    }

    private void validateRequest(EmailRequestDto requestDto) {
        if (requestDto == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email request is required");
        }
        String subject = requestDto.getSubject();
        String body = requestDto.getBody();
        String htmlBody = requestDto.getHtmlBody();

        if (!StringUtils.hasText(subject) || subject.length() > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Subject is required and must be 100 characters or fewer");
        }
        if (subject.indexOf('\r') >= 0 || subject.indexOf('\n') >= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Subject cannot contain line breaks");
        }
        if (!StringUtils.hasText(body) || body.length() > 1000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Body is required and must be 1000 characters or fewer");
        }
        if (!StringUtils.hasText(htmlBody) || htmlBody.length() > 50_000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "HTML body is required and must be 50000 characters or fewer");
        }
        if (requestDto.getName() != null && requestDto.getName().length() > 50) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name must be 50 characters or fewer");
        }
    }

    private String normalizeName(String name) {
        return StringUtils.hasText(name) ? name.trim() : DEFAULT_RECIPIENT_NAME;
    }

    private String buildPlainTextBody(String body) {
        return body + "\n\nRegards,\nFitness App Team\n\nCopyright (c) "
                + CURRENT_YEAR + " Fitness App. All rights reserved.";
    }

    private String buildHtmlTemplate(String recipientName, String htmlBody) {
        String safeName = escapeHtml(StringUtils.hasText(recipientName)
                ? recipientName.trim()
                : DEFAULT_RECIPIENT_NAME);

        return """
                <!DOCTYPE html>
                <html>
                <head><meta charset="UTF-8"></head>
                <body style="margin:0;padding:24px;background-color:#f8fafc;color:#1e293b;font-family:Arial,Helvetica,sans-serif;line-height:1.6">
                  <div style="max-width:600px;margin:0 auto;padding:32px;background:#ffffff;border:1px solid #e2e8f0;border-radius:8px">
                    <h2 style="margin-top:0;color:#0f172a">Hello %s,</h2>
                    <div style="margin-top:16px;font-size:15px">%s</div>
                    <div style="margin-top:36px;padding-top:18px;border-top:1px solid #f1f5f9">
                      Regards,<br><strong>Fitness App Team</strong>
                    </div>
                  </div>
                  <div style="max-width:600px;margin:24px auto 0;color:#64748b;font-size:12px;text-align:center">
                    &copy; %d Fitness App. All rights reserved.
                  </div>
                </body>
                </html>
                """.formatted(safeName, htmlBody, CURRENT_YEAR);
    }

    private String escapeHtml(String value) {
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}

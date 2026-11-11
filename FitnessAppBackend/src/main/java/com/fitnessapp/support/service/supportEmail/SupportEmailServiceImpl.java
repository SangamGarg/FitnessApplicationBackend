package com.fitnessapp.support.service.supportEmail;

import com.fitnessapp.appAuth.model.entity.UserEntity;
import com.fitnessapp.appAuth.repo.UserRepo;
import com.fitnessapp.support.models.dtos.SupportEmailAttachment;
import com.fitnessapp.support.models.dtos.SupportValidationDto;
import com.fitnessapp.support.models.dtos.SupportIssueCategoryDto;
import com.fitnessapp.support.models.dtos.request.EmailFromUserRequestDto;
import com.fitnessapp.support.models.dtos.response.EmailFromUserResponseDto;
import com.fitnessapp.support.models.dtos.response.SupportEmailHomeResponseDto;
import com.fitnessapp.support.models.entity.SupportTicketEntity;
import com.fitnessapp.support.models.enums.SupportIssueCategory;
import com.fitnessapp.support.utils.SupportAttachmentValidator;
import com.fitnessapp.utils.appUtil.AppResponseStatus;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SupportEmailServiceImpl implements SupportEmailService {
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;
    private final UserRepo userRepo;
    private final SupportAttachmentValidator attachmentValidator;
    private final SupportEmailDispatcher emailDispatcher;

    @Override
    @Transactional(readOnly = true)
    public SupportEmailHomeResponseDto getEmailHome() {

        List<SupportIssueCategoryDto> categories =
                Arrays.stream(SupportIssueCategory.values())
                        .map(category ->
                                SupportIssueCategoryDto.builder()
                                        .key(category.name())
                                        .label(category.getLabel())
                                        .build())
                        .toList();

        SupportValidationDto validation =
                SupportValidationDto.builder()
                        .subjectMinLength(5)
                        .subjectMaxLength(100)
                        .messageMinLength(10)
                        .messageMaxLength(2000)
                        .maxAttachments(3)
                        .maxAttachmentSizeBytes(5L * 1024 * 1024)
                        .maxTotalAttachmentSizeBytes(10L * 1024 * 1024)
                        .allowedMimeTypes(List.of(
                                "image/jpeg",
                                "image/png",
                                "image/webp"
                        ))
                        .build();

        return SupportEmailHomeResponseDto.builder()
                .title("Email Support Desk")
                .subtitle("Replies within ~12 business hours")
                .description(
                        "Tell us about your issue and our support team will help you."
                )
                .categories(categories)
                .validation(validation)
                .diagnosticsEnabled(true)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public EmailFromUserResponseDto sendEmail(
            EmailFromUserRequestDto request,
            List<MultipartFile> attachments) {

        String userId = getAuthenticatedUserId();

        UserEntity user = userRepo.findById(userId)
                .orElseThrow(() ->
                        new EntityNotFoundException("User account not found"));

        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new IllegalStateException(
                    "Registered user email is not available"
            );
        }

        if (request.getCategory() == SupportIssueCategory.OTHER) {
            // If you add an otherDescription field to the DTO,
            // validate that it is present and nonblank here.
        }

        attachmentValidator.validate(attachments);

        List<SupportEmailAttachment> emailAttachments =
                prepareAttachments(attachments);

        log.info(
                "Accepting support email request. userId={}, category={}, attachmentCount={}",
                userId,
                request.getCategory(),
                emailAttachments.size()
        );

        emailDispatcher.dispatchSupportEmail(
                user.getEmail(),
                userId,
                request,
                emailAttachments
        );

        return EmailFromUserResponseDto.builder()
                .status(AppResponseStatus.SUCCESS.name())
                .statusCode(202)
                .message(
                        "Your message has been submitted successfully. Our support team will contact you."
                )
                .build();
    }

    private List<SupportEmailAttachment> prepareAttachments(
            List<MultipartFile> files) {

        if (files == null || files.isEmpty()) {
            return List.of();
        }

        return files.stream()
                .map(file -> {
                    try {
                        String extension = getSafeExtension(
                                Objects.requireNonNull(file.getContentType())
                        );

                        String filename = "support-attachment-" +
                                UUID.randomUUID() + extension;

                        return new SupportEmailAttachment(
                                filename,
                                file.getContentType(),
                                file.getBytes()
                        );

                    } catch (IOException e) {
                        throw new IllegalArgumentException(
                                "Unable to process attachment"
                        );
                    }
                })
                .toList();
    }

    private String getSafeExtension(String contentType) {

        return switch (contentType.toLowerCase()) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> throw new IllegalArgumentException(
                    "Unsupported attachment type"
            );
        };
    }

    private String getAuthenticatedUserId() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getPrincipal() == null
                || "anonymousUser".equals(authentication.getPrincipal())) {

            throw new AccessDeniedException(
                    "User authentication is required"
            );
        }

        return authentication.getName();
    }

    @Async
    @Override
    public void sendTicketConfirmationEmail(SupportTicketEntity ticket) {
        log.info("Sending asynchronous ticket confirmation email to [{}] for ticket [{}]",
                ticket.getUserEmail(), ticket.getTicketNumber());

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );

            Context context = new Context();
            context.setVariable("ticketNumber", ticket.getTicketNumber());
            context.setVariable("subject", ticket.getSubject());
            context.setVariable("category", ticket.getCategory().name());
            context.setVariable("description", ticket.getDescription());
            context.setVariable("createdAt", ticket.getCreatedAt());

            String htmlContent = templateEngine.process("mail/ticket-confirmation-email", context);

            helper.setTo(ticket.getUserEmail());
            helper.setFrom(fromEmail, "FitnessApp Support Team");
            helper.setSubject("We received your request - [" + ticket.getTicketNumber() + "]");
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Ticket confirmation email dispatched successfully for [{}]", ticket.getTicketNumber());

        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            log.error("Failed to deliver ticket confirmation email for ticket [{}]: {}",
                    ticket.getTicketNumber(), e.getMessage(), e);
        }
    }

    @Async
    @Override
    public void sendTicketResolutionEmail(SupportTicketEntity ticket) {
        log.info("Sending resolution email to [{}] for ticket [{}]", ticket.getUserEmail(), ticket.getTicketNumber());

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, StandardCharsets.UTF_8.name());

            Context context = new Context();
            context.setVariable("ticketNumber", ticket.getTicketNumber());
            context.setVariable("resolutionNotes", ticket.getResolutionNotes());

            String htmlContent = templateEngine.process("mail/ticket-resolved-email", context);

            helper.setTo(ticket.getUserEmail());
            helper.setFrom(fromEmail, "FitnessApp Support Team");
            helper.setSubject("Ticket Resolved - [" + ticket.getTicketNumber() + "]");
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Resolution email dispatched successfully for [{}]", ticket.getTicketNumber());

        } catch (Exception e) {
            log.error("Failed to send resolution email for [{}]: {}", ticket.getTicketNumber(), e.getMessage(), e);
        }
    }
}
package com.fitnessapp.support.service;

import com.fitnessapp.support.models.SupportTicketEntity;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class SupportEmailServiceImpl implements SupportEmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username:support@fitnessapp.com}")
    private String fromEmail;

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

            String htmlContent = templateEngine.process("mail/ticket-confirmation", context);

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

            String htmlContent = templateEngine.process("mail/ticket-resolved", context);

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
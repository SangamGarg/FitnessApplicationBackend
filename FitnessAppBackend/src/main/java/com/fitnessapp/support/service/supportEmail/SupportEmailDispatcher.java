package com.fitnessapp.support.service.supportEmail;

import com.fitnessapp.support.models.dtos.SupportEmailAttachment;
import com.fitnessapp.support.models.dtos.request.EmailFromUserRequestDto;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SupportEmailDispatcher {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.mail.support-email}")
    private String supportEmail;

    @Async("supportMailExecutor")
    public void dispatchSupportEmail(
            String userEmail,
            String userId,
            EmailFromUserRequestDto request,
            List<SupportEmailAttachment> attachments) {

        log.info(
                "Dispatching support email for userId={}, category={}",
                userId,
                request.getCategory()
        );

        try {
            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    true,
                    StandardCharsets.UTF_8.name()
            );

            Context context = new Context();

            // context.setVariable("userName", userName);
            context.setVariable("userEmail", userEmail);
            context.setVariable("userId", userId);
            context.setVariable("category", request.getCategory().getLabel());
            context.setVariable("subject", request.getSubject());
            context.setVariable("message", request.getMessage());
            context.setVariable("diagnostics", request.getDiagnostics());

            String html = templateEngine.process(
                    "mail/user-support-email",
                    context
            );

            helper.setFrom(fromEmail, "FitnessApp Support");
            helper.setTo(supportEmail);
            helper.setReplyTo(userEmail);
            helper.setSubject(
                    "[App Support] [" + request.getCategory().name() +
                            "] " + request.getSubject()
            );
            helper.setText(html, true);

            for (SupportEmailAttachment attachment : attachments) {

                ByteArrayResource resource =
                        new ByteArrayResource(attachment.content());

                helper.addAttachment(
                        attachment.filename(),
                        resource,
                        attachment.contentType()
                );
            }

            mailSender.send(message);

            log.info(
                    "Support email successfully dispatched for userId={}",
                    userId
            );

        } catch (Exception e) {

            log.error(
                    "Support email delivery failed for userId={}, category={}",
                    userId,
                    request.getCategory(),
                    e
            );

            // In a production system, record this failure for retry.
        }
    }
}
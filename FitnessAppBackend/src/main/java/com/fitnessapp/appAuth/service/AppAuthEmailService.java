package com.fitnessapp.appAuth.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.util.HtmlUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.fitnessapp.utils.appUtil.AppUtil.APP_NAME;
import static com.fitnessapp.utils.appUtil.AppUtil.APP_URL;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppAuthEmailService {


    private static final String WELCOME_TEMPLATE = "templates/mail/app-welcome-email.html";
    private static final String OTP_TEMPLATE = "templates/mail/app-otp-email.html";
    private static final Map<String, String> TEMPLATE_CACHE = new ConcurrentHashMap<>();
    @Value("${spring.mail.username:}")
    private String mailFrom;

    private static String loadTemplate(String path) {
        return TEMPLATE_CACHE.computeIfAbsent(path, p -> {
            try (InputStream in = new ClassPathResource(p).getInputStream()) {
                return StreamUtils.copyToString(in, StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new IllegalStateException("Could not load email template: " + p, e);
            }
        });
    }

    private final JavaMailSender mailSender;

    @Async
    public void sendWelcomeEmail(String name, String email) {
        try {
            appWelcomeEmail(mailSender, name, email);
        } catch (Exception ex) {
            // Mail delivery must never fail or roll back account creation.
            log.warn("Welcome email delivery failed; signup remains successful: {}", ex.getMessage());
        }
    }

    private void appWelcomeEmail(JavaMailSender mailSender, String name, String email) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

        helper.setTo(email);
        helper.setSubject("Welcome to " + APP_NAME + " 🔥 Let's get moving");

        String displayName = (name == null || name.isBlank()) ? "there" : name;

        String htmlContent = loadTemplate(WELCOME_TEMPLATE).replace("{{app}}", APP_NAME).replace("{{appUrl}}", APP_URL).replace("{{year}}", String.valueOf(java.time.Year.now().getValue())).replace("{{name}}", HtmlUtils.htmlEscape(displayName)); // user input last

        String plainText = """
                Hey %s, welcome to %s!
                
                Your registration was successful and your account is ready to use.
                
                What's waiting for you:
                - Exercise Catalogue: browse by muscle group, equipment and difficulty
                - AI Camera Tracking: real-time pose tracking to perfect your form
                - Progress Tracking: watch your improvements add up
                
                Start your first workout: %s
                
                Stay active,
                The %s Team
                """.formatted(displayName, APP_NAME, APP_URL, APP_NAME);

        helper.setText(plainText, htmlContent);
        mailSender.send(mimeMessage);
    }


    public void sendOtpMail(
            String email,
            String subject,
            String explanation,
            String otp,
            long expirySeconds
    ) throws MessagingException {

        MimeMessage message = mailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(
                message,
                MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                StandardCharsets.UTF_8.name()
        );

        helper.setTo(email);

        if (mailFrom != null && !mailFrom.isBlank()) {
            helper.setFrom(mailFrom);
        }

        helper.setSubject(subject);

        long expiryMinutes = Math.max(1, (expirySeconds + 59) / 60);

        String safeExplanation =
                (explanation != null && !explanation.isBlank())
                        ? HtmlUtils.htmlEscape(explanation)
                        : "Use the code below to complete your verification.";

        String safeOtp = HtmlUtils.htmlEscape(otp);

        // Load the cached HTML template and replace its placeholders.
        String htmlContent = loadTemplate(OTP_TEMPLATE)
                .replace("{{explanation}}", safeExplanation)
                .replace("{{otp}}", safeOtp)
                .replace("{{expiryMinutes}}", String.valueOf(expiryMinutes));

        String plainText = "Your MusclePlay verification code is " + otp + ". "
                + (explanation != null ? explanation : "Complete your verification.")
                + " This code expires in " + expiryMinutes
                + " minutes. If you did not request this, you can ignore this email.";

        helper.setText(plainText, htmlContent);

        mailSender.send(message);
    }
}
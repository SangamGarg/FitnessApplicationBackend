package com.fitnessapp.DeveloperAndTestingService.devUserAndAuthDevService;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class DevUserServiceImpl implements DevUserService {

    private final DevUserRepository devUserRepository;
    private final JavaMailSender mailSender;

    public DevUserServiceImpl(DevUserRepository userTestingRepository, JavaMailSender mailSender) {
        this.devUserRepository = userTestingRepository;
        this.mailSender = mailSender;
    }

    @Transactional
    @Override
    public ResponseEntity<?> saveUserForDevAndTest(String devName, String devEmail) {
        try {
            String randomStr = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 8);
            String key = devName.replace(" ", "").concat(randomStr);
            String hashedKey = new BCryptPasswordEncoder(10).encode(key); //We don't save the actual key, we save the hashed key for security'
            DevUserEntity user = devUserRepository.findByDevNameIgnoreCase(devName);
            if (user != null) {
                return ResponseEntity.badRequest().body("User with name : " + devName + " already exist");
            }
            LocalDateTime dateTime = LocalDateTime.now(); // 2025-04-10T14:23:45.123
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
            String dateAndTime = dateTime.format(formatter);
            devUserRepository.save(DevUserEntity.builder()
                    .devName(devName)
                    .devEmail(devEmail)
                    .key(hashedKey)
                    .createdAt(dateAndTime)
                    .build());
            //send email to the user of key
            sendMailToDevUser(devName, devEmail, key);

            return ResponseEntity.ok("User For Dev And Testing Registered with name : " + devName + "\nKey : " + key);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getLocalizedMessage());
        }
    }

    private void sendMailToDevUser(String name, String email, String key) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

        helper.setTo(email);
        helper.setSubject("Welcome Aboard! You’ve Successfully Registered and Assigned as Developer");

        String htmlContent = """
                <html>
                    <head>
                        <style>
                            body {
                                font-family: Arial, sans-serif;
                                background-color: #f9f9f9;
                                padding: 20px;
                                color: #333;
                            }
                            .email-container {
                                background-color: #ffffff;
                                padding: 30px;
                                border-radius: 8px;
                                box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
                                max-width: 600px;
                                margin: auto;
                                text-align: left;
                            }
                            h2 {
                                color: #4CAF50;
                            }
                            .highlight-box {
                                background-color: #e8f5e9;
                                padding: 15px;
                                border-left: 4px solid #4CAF50;
                                margin: 20px 0;
                                font-size: 16px;
                                font-weight: bold;
                            }
                            .footer {
                                margin-top: 30px;
                                font-size: 14px;
                                color: #777;
                                text-align: center;
                            }
                        </style>
                    </head>
                    <body>
                        <div class="email-container">
                            <h2>Welcome, %s!</h2>
                            <p>You have been successfully registered in our system and assigned the role of <strong>Developer</strong>.</p>
                
                            <p>Your unique access key is:</p>
                            <div class="highlight-box">%s</div>
                
                            <p>Please keep this key safe as it is required for accessing developer resources.</p>
                            <p>Please remember this key.</p>
                            <p>If you forget this key you have to contact the lead.</p>
                
                            <p style="margin-top: 40px;">Regards,<br/><strong>Fitness App Team</strong></p>
                        </div>
                        <div class="footer">
                            &copy; 2025 Fitness App. All rights reserved.
                            &copy; Fitness App Confidential.
                        </div>
                    </body>
                </html>
                """.formatted(name, key);


        helper.setText(htmlContent, true); // true = isHtml

        mailSender.send(mimeMessage);
    }

    @Transactional
    @Override
    public ResponseEntity<?> deleteUserForDevAndTest(String devEmail) {
        try {
            DevUserEntity user = devUserRepository.findByDevNameIgnoreCase(devEmail);
            if (user == null) {
                return ResponseEntity.badRequest().body("User Not Found With Given Name : " + devEmail);
            }
            devUserRepository.delete(user);
            return ResponseEntity.ok("User For Dev And Testing Deleted with name : " + devEmail);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getLocalizedMessage());
        }
    }

    @Override
    public ResponseEntity<?> getUsersForDevAndTest() {
        try {
            return ResponseEntity.ok(devUserRepository.findAll());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getLocalizedMessage());
        }
    }

    @Override
    public List<DevUserEntity> getUsersForDevAndTestForInterceptor() {
        try {
            return devUserRepository.findAll();
        } catch (Exception e) {
            return null;
        }
    }
}

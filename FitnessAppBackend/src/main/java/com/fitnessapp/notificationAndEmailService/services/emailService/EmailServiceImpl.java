package com.fitnessapp.notificationAndEmailService.services.emailService;

import com.fitnessapp.fitnessCalculatorsService.models.entities.ImageAndAboutEntity;
import com.fitnessapp.fitnessCalculatorsService.repositories.ImageAndAboutRepository;
import com.fitnessapp.notificationAndEmailService.models.dtos.emailDtos.request.EmailFromUseRequestDto;
import com.fitnessapp.notificationAndEmailService.models.dtos.emailDtos.request.EmailToAllUsersRequestDto;
import com.fitnessapp.notificationAndEmailService.models.dtos.emailDtos.request.EmailToMoreThanOneUserRequestDto;
import com.fitnessapp.notificationAndEmailService.models.dtos.emailDtos.request.EmailToOneUserRequestDto;
import com.fitnessapp.notificationAndEmailService.models.dtos.emailDtos.response.EmailFromUseResponseDto;
import com.fitnessapp.notificationAndEmailService.models.dtos.emailDtos.response.EmailToAllUsersResponseDto;
import com.fitnessapp.notificationAndEmailService.models.dtos.emailDtos.response.EmailToMoreThanOneUserResponseDto;
import com.fitnessapp.notificationAndEmailService.models.dtos.emailDtos.response.EmailToOneUserResponseDto;
import com.fitnessapp.notificationAndEmailService.notificationAndEmailServiceUtilities.AppConstantsNotificationAndEmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;


@Service
public class EmailServiceImpl implements EmailService {

    /// //
    /// /
    /// /This I have to do
    /// /Add HTML Template And also add attachment functionality
    /// /
    /// /

    private final JavaMailSender mailSender;
    private final ImageAndAboutRepository imageAndAboutRepository;


    public EmailServiceImpl(JavaMailSender mailSender, ImageAndAboutRepository imageAndAboutRepository) {
        this.mailSender = mailSender;
        this.imageAndAboutRepository = imageAndAboutRepository;
    }

    @Override
    public ResponseEntity<?> getMailFromUser(EmailFromUseRequestDto emailRequestDto) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo("sangamgarg17@gmail.com"); // here set the receiver like (contact.fitnessapp@app.in) this email receives the email send by user
            helper.setSubject("Query From User Fitness Application: " + emailRequestDto.getSubject());

            String htmlContent = """
        <html>
            <head>
                <style>
                    body {
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                        background-color: #f4f6f8;
                        padding: 20px;
                        color: #333;
                    }
                    .email-container {
                        background-color: #ffffff;
                        padding: 30px;
                        border-radius: 10px;
                        box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
                        max-width: 700px;
                        margin: auto;
                    }
                    h2 {
                        color: #007bff;
                    }
                    .info-label {
                        font-weight: bold;
                        margin-top: 10px;
                    }
                    .footer {
                        margin-top: 40px;
                        font-size: 13px;
                        color: #888;
                        text-align: center;
                    }
                </style>
            </head>
            <body>
                <div class="email-container">
                    <h2>User Query from Fitness App</h2>
                    <p><span class="info-label">Name:</span> %s</p>
                    <p><span class="info-label">Email:</span> %s</p>
                    <p><span class="info-label">Subject:</span> %s</p>
                    <p><span class="info-label">Message:</span> %s</p>
                    <br/>
                    <p>Regards,<br/><strong>Fitness App Team</strong></p>
                </div>
                <div class="footer">
                    &copy; 2025 Fitness App. All rights reserved.
                </div>
            </body>
        </html>
        """.formatted(
                    emailRequestDto.getName(),
                    emailRequestDto.getEmail(),
                    emailRequestDto.getSubject(),
                    emailRequestDto.getBody()
            );


            helper.setText(htmlContent, true); // true = isHtml

//            if (emailToOneUserRequestDto.getAttachment() != null) {
//                helper.addAttachment(emailToOneUserRequestDto.getAttachment().getName(), emailToOneUserRequestDto.getAttachment());
//            }

            mailSender.send(mimeMessage);

            return ResponseEntity.ok(EmailFromUseResponseDto.builder()
                    .status(AppConstantsNotificationAndEmailService.SUCCESS_API_STATUS)
                    .statusCode(200)
                    .message("We have received your email and will contact you soon")
                    .imageUrl(getImageUrlAndAbout().getImageUrl())
                    .build());

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(AppConstantsNotificationAndEmailService.errorDto(e));
        }


    }

    @Override
    public ResponseEntity<?> sendEmailToOneUser(EmailToOneUserRequestDto emailToOneUserRequestDto) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(emailToOneUserRequestDto.getEmail());
            helper.setSubject(emailToOneUserRequestDto.getSubject());

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
                                    padding: 20px;
                                    border-radius: 8px;
                                    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
                                    max-width: 600px;
                                    margin: auto;
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
                                <h2>Dear %s,</h2>
                                <p>%s</p>
                                <p style="margin-top: 40px;">Regards,<br/><strong>Fitness App Team</strong></p>
                            </div>
                            <div class="footer">
                                &copy; 2025 Fitness App. All rights reserved.
                            </div>
                        </body>
                    </html>
                    """.formatted(emailToOneUserRequestDto.getName(), emailToOneUserRequestDto.getHtmlBody());

            helper.setText(htmlContent, true); // true = isHtml

//            if (emailToOneUserRequestDto.getAttachment() != null) {
//                helper.addAttachment(emailToOneUserRequestDto.getAttachment().getName(), emailToOneUserRequestDto.getAttachment());
//            }

            mailSender.send(mimeMessage);

            return ResponseEntity.ok(EmailToOneUserResponseDto.builder()
                    .status(AppConstantsNotificationAndEmailService.SUCCESS_API_STATUS)
                    .statusCode(200)
                    .message("Email Sent Successfully")
                    .email(emailToOneUserRequestDto.getEmail())
                    .name(emailToOneUserRequestDto.getName())
                    .subject(emailToOneUserRequestDto.getSubject())
                    .body(emailToOneUserRequestDto.getBody())
                    .build());

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(AppConstantsNotificationAndEmailService.errorDto(e));
        }
    }

    @Override
    public ResponseEntity<?> sendEmailToAllUsers(EmailToAllUsersRequestDto emailToAllUsersRequestDto) {
        try {
            String[] emails = emailToAllUsersRequestDto.getEmails().toArray(new String[0]);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(emails);
            helper.setSubject(emailToAllUsersRequestDto.getSubject());

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
                                    padding: 20px;
                                    border-radius: 8px;
                                    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
                                    max-width: 600px;
                                    margin: auto;
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
                                <h2>Dear %s,</h2>
                                <p>%s</p>
                                <p style="margin-top: 40px;">Regards,<br/><strong>Fitness App Team</strong></p>
                            </div>
                            <div class="footer">
                                &copy; 2025 Fitness App. All rights reserved.
                            </div>
                        </body>
                    </html>
                    """.formatted("User", emailToAllUsersRequestDto.getHtmlBody());

            helper.setText(htmlContent, true); // true = isHtml

//            if (emailToOneUserRequestDto.getAttachment() != null) {
//                helper.addAttachment(emailToOneUserRequestDto.getAttachment().getName(), emailToOneUserRequestDto.getAttachment());
//            }

            mailSender.send(mimeMessage);


            return ResponseEntity.ok(EmailToAllUsersResponseDto.builder()
                    .status(AppConstantsNotificationAndEmailService.SUCCESS_API_STATUS)
                    .statusCode(200)
                    .message("Email Sent Successfully")
                    .emails(emailToAllUsersRequestDto.getEmails())
                    .subject(emailToAllUsersRequestDto.getSubject())
                    .body(emailToAllUsersRequestDto.getBody())
                    .build());

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(AppConstantsNotificationAndEmailService.errorDto(e));
        }
    }

    @Override
    public ResponseEntity<?> sendEmailToMoreThanOneUser(EmailToMoreThanOneUserRequestDto emailToMoreThanOneUserRequestDto) {
        try {

            String[] emails = emailToMoreThanOneUserRequestDto.getEmails().toArray(new String[0]);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(emails);
            helper.setSubject(emailToMoreThanOneUserRequestDto.getSubject());

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
                                    padding: 20px;
                                    border-radius: 8px;
                                    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
                                    max-width: 600px;
                                    margin: auto;
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
                                <h2>Dear %s,</h2>
                                <p>%s</p>
                                <p style="margin-top: 40px;">Regards,<br/><strong>Fitness App Team</strong></p>
                            </div>
                            <div class="footer">
                                &copy; 2025 Fitness App. All rights reserved.
                            </div>
                        </body>
                    </html>
                    """.formatted("User", emailToMoreThanOneUserRequestDto.getHtmlBody());

            helper.setText(htmlContent, true); // true = isHtml

//            if (emailToOneUserRequestDto.getAttachment() != null) {
//                helper.addAttachment(emailToOneUserRequestDto.getAttachment().getName(), emailToOneUserRequestDto.getAttachment());
//            }

            mailSender.send(mimeMessage);



            return ResponseEntity.ok(EmailToMoreThanOneUserResponseDto.builder()
                    .status(AppConstantsNotificationAndEmailService.SUCCESS_API_STATUS)
                    .statusCode(200)
                    .message("Email Sent Successfully")
                    .emails(emailToMoreThanOneUserRequestDto.getEmails())
                    .subject(emailToMoreThanOneUserRequestDto.getSubject())
                    .body(emailToMoreThanOneUserRequestDto.getBody())
                    .build());

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(AppConstantsNotificationAndEmailService.errorDto(e));
        }
    }

    private ImageAndAboutEntity getImageUrlAndAbout() {
        try {
            return imageAndAboutRepository.findByNameIgnoreCase("email").orElse(ImageAndAboutEntity.builder().imageUrl("Not Found").build());
        } catch (Exception e) {
            return new ImageAndAboutEntity(0L, "Error Getting Name", "Error Getting Image", "Error Getting About");
        }
    }

}

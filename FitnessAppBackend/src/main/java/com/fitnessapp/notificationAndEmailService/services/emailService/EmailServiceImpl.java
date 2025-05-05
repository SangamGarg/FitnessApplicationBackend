package com.fitnessapp.notificationAndEmailService.services.emailService;

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
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;


@Service
public class EmailServiceImpl implements EmailService {

    ///
    /// //
    /// /
    /// /This I have to do
    /// /Add HTML Template And also add attachment functionality
    /// /
    /// /
    ///

    private final JavaMailSender mailSender;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public ResponseEntity<?> getMailFromUser(EmailFromUseRequestDto emailRequestDto) {
//        LocalDateTime myDateObj = LocalDateTime.now();
//        DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
//        String formattedDate = myDateObj.format(myFormatObj);
//        Date date = Date.from(myDateObj.atZone(ZoneId.systemDefault()).toInstant());
//
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            String body = String.format("""
                    Email: %s
                    Name: %s
                    Subject: %s
                    Message: %s
                    """, emailRequestDto.getEmail(), emailRequestDto.getName(), emailRequestDto.getSubject(), emailRequestDto.getBody());
            //  message.setSentDate(date);
            message.setTo("sangamgarg17@gmail.com"); // here set the receiver like (contact.fitnessapp@app.in) this email receives the email send by user
            message.setSubject("Query From User: " + emailRequestDto.getSubject());
            message.setText(body);
            mailSender.send(message);

            return ResponseEntity.ok(EmailFromUseResponseDto.builder()
                    .message("We have received your email and will contact you soon")
                    .imageUrl("")
                    .build());

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(AppConstantsNotificationAndEmailService.errorDto(e));
        }


    }

    @Override
    public ResponseEntity<?> sendEmailToOneUser(EmailToOneUserRequestDto emailToOneUserRequestDto) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();

            message.setTo(emailToOneUserRequestDto.getEmail());
            message.setSubject(emailToOneUserRequestDto.getSubject());
            message.setText(emailToOneUserRequestDto.getBody());


            mailSender.send(message);

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
            SimpleMailMessage message = new SimpleMailMessage();

            message.setTo(emails);
            message.setSubject(emailToAllUsersRequestDto.getSubject());
            message.setText(emailToAllUsersRequestDto.getBody());
            mailSender.send(message);

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
            SimpleMailMessage message = new SimpleMailMessage();

            message.setTo(emails);
            message.setSubject(emailToMoreThanOneUserRequestDto.getSubject());
            message.setText(emailToMoreThanOneUserRequestDto.getBody());
            mailSender.send(message);

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
}

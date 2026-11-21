package com.fitnessapp.portal.portalEmailSend.model.request;

import com.fitnessapp.portal.portalEmailSend.model.EmailRecipientType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailRequestDto {
    private EmailRecipientType recipientType;

    @Size(max = 50, message = "Name should not exceed 50 characters")
    private String name;

    @Email(message = "Invalid email format")
    private String email;

    @Size(max = 5000, message = "No more than 5000 recipients can be included")
    private List<@Email(message = "Invalid email format") @NotBlank(message = "Email cannot be blank") String> emails;

    @NotBlank(message = "Subject is required")
    @Size(max = 100, message = "Subject should not exceed 100 characters")
    private String subject;

    @NotBlank(message = "Body is required")
    @Size(max = 1000, message = "Body should not exceed 1000 characters")
    private String body;

    @NotBlank(message = "Html Body is required")
    @Size(max = 50000, message = "Html Body must not exceed 50000 characters")
    @Pattern(
            regexp = "(?s).*<[^>]+>.*",
            message = "Html Body must contain at least one HTML tag"
    )
    private String htmlBody;
}

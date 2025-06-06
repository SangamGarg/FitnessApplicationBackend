package com.fitnessapp.notificationAndEmailService.models.dtos.emailDtos.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailFromUseRequestDto {
    @NotBlank(message = "Name is required")
    @Size(max = 50, message = "Name should not exceed 50 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Subject is required")
    @Size(max = 100, message = "Subject should not exceed 100 characters")
    private String subject;

    @NotBlank(message = "Body is required")
    @Size(max = 1000, message = "Body should not exceed 1000 characters")
    private String body;
}

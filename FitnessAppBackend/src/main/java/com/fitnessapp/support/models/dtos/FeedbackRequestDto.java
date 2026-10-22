package com.fitnessapp.support.models.dtos;

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
public class FeedbackRequestDto {


    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "User ID is required")
    private String userId;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    @NotBlank(message = "Feedback cannot be empty")
    @Size(max = 500, message = "Feedback cannot exceed 500 characters")
    private String feedback;

}
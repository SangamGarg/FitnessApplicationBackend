package com.fitnessapp.feedback.model;

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
    @NotBlank(message = "Feedback cannot be empty")
    @Size(max = 500, message = "Feedback cannot exceed 500 characters")
    private String feedback;

}
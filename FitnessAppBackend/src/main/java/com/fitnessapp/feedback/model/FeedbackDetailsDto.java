package com.fitnessapp.feedback.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackDetailsDto {

    private Long id;
    private String feedback;
    private String name;
    private String email;
    private String userId;
    private LocalDateTime createdAt;
}
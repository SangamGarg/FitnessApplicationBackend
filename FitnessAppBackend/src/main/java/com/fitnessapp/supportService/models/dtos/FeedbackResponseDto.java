package com.fitnessapp.supportService.models.dtos;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedbackResponseDto {
    private String status;
    private Integer statusCode;
    private String message;
    private String imageUrl;
}
package com.fitnessapp.support.portalSupport.models.dto.response;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppInfoFaqsResponseDto {
    private Long id;
    private String question;
    private String answer;
}
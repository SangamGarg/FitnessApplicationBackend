package com.fitnessapp.DeveloperAndTestingService.appInfoDevService.models.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class AppInfoFaqsRequestDto {

    @NotBlank(message = "Question cannot be blank or null")
    private String question;
    @NotBlank(message = "Answer cannot be blank or null")
    private String answer;


}
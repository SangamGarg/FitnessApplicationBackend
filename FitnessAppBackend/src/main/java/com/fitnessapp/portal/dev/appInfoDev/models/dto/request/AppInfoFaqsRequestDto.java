package com.fitnessapp.portal.dev.appInfoDev.models.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppInfoFaqsRequestDto {

    @NotBlank(message = "Question cannot be blank")
    @Schema(description = "FAQ Question", example = "How to reset my password?")
    private String question;

    @NotBlank(message = "Answer cannot be blank")
    @Schema(description = "FAQ Answer", example = "Navigate to profile settings and click reset password.")
    private String answer;
}
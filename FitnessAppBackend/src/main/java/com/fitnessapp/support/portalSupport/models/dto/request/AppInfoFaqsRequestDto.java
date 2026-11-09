package com.fitnessapp.support.portalSupport.models.dto.request;

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
    @Schema(description = "FAQ Question",
            example = "How to reset my password?")
    private String question;

    @NotBlank(message = "Answer cannot be blank")
    @Schema(description = "FAQ Answer",
            example = "Navigate to profile settings and click reset password.")
    private String answer;

    @NotBlank(message = "Category key cannot be blank")
    @Schema(description = "FAQ category key",
            example = "ACCOUNT")
    private String categoryKey;

    @Schema(description = "Display order", example = "1")
    private Integer sortOrder;

    @Schema(description = "Whether this FAQ is popular", example = "false")
    private Boolean popular;

    @Schema(description = "Whether this FAQ is active", example = "true")
    private Boolean active;
}
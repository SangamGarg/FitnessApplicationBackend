package com.fitnessapp.support.portalSupport.models.dto.request;

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
    private String question;
    @NotBlank(message = "Answer cannot be blank")
    private String answer;
    @NotBlank(message = "Category key cannot be blank")
    private String categoryKey;
    private Integer sortOrder;
    private Boolean popular;
    private Boolean active;
}
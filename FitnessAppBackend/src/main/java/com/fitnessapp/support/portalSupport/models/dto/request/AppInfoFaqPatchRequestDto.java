package com.fitnessapp.support.portalSupport.models.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppInfoFaqPatchRequestDto {

    @Size(min = 1, max = 500,
            message = "Question must contain between 1 and 500 characters")
    private String question;

    @Size(min = 1, max = 10000,
            message = "Answer must contain between 1 and 10000 characters")
    private String answer;

    private String categoryKey;

    @Min(value = 0, message = "Sort order cannot be negative")
    private Integer sortOrder;

    private Boolean popular;

    private Boolean active;
}
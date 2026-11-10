package com.fitnessapp.support.models.dtos.request;

import com.fitnessapp.support.models.dtos.DeviceDiagnosticInfo;
import com.fitnessapp.support.models.enums.SupportIssueCategory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailFromUserRequestDto {
    @NotNull(message = "Issue category is required")
    private SupportIssueCategory category;

    @NotBlank(message = "Subject is required")
    @Size(min = 5, max = 100,
            message = "Subject must be between 5 and 100 characters")
    private String subject;

    @NotBlank(message = "Message is required")
    @Size(min = 10, max = 2000,
            message = "Message must be between 10 and 2000 characters")
    private String message;

    @Valid
    @NotNull(message = "Device diagnostics are required")
    private DeviceDiagnosticInfo diagnostics;
}

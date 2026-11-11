package com.fitnessapp.support.models.dtos.request;

import com.fitnessapp.support.models.dtos.DeviceDiagnosticInfo;
import com.fitnessapp.support.models.enums.SupportIssueCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class  CreateSupportTicketRequestDTO {

    @NotNull(message = "Category is required")
    private SupportIssueCategory category;

    @NotBlank(message = "Subject cannot be blank")
    @Size(min = 5, max = 100, message = "Subject cannot exceed 100 characters")
    private String subject;

    @NotBlank(message = "Description cannot be blank")
    @Size(min = 10, max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;

    // Autofilled by the Android client
    private DeviceDiagnosticInfo diagnosticInfo;

    // Optional URLs if uploaded prior to ticket creation
    private List<String> attachmentUrls;
}
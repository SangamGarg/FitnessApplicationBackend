package com.fitnessapp.support.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class CreateSupportTicketRequestDTO {

    @NotNull(message = "Category is required")
    private TicketCategory category;

    @NotBlank(message = "Subject cannot be blank")
    @Size(max = 200, message = "Subject cannot exceed 200 characters")
    private String subject;

    @NotBlank(message = "Description cannot be blank")
    @Size(max = 3000, message = "Description cannot exceed 3000 characters")
    private String description;

    // Autofilled by the Android client
    private DeviceDiagnosticInfo diagnosticInfo;

    // Optional URLs if uploaded prior to ticket creation
    private List<String> attachmentUrls;
}
package com.fitnessapp.support.models.dtos.response;

import com.fitnessapp.support.models.dtos.DeviceDiagnosticInfo;
import com.fitnessapp.support.models.enums.SupportIssueCategory;
import com.fitnessapp.support.models.enums.TicketPriority;
import com.fitnessapp.support.models.enums.TicketStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class SupportTicketResponseDTO {
    private String ticketNumber;
    private String userId;
    private String userEmail;
    private SupportIssueCategory category;
    private String subject;
    private String description;
    private TicketStatus status;
    private TicketPriority priority;
    private DeviceDiagnosticInfo diagnosticInfo;
    private List<String> attachmentUrls;
    private String resolutionNotes;
    private String assignedToEmail;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
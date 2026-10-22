package com.fitnessapp.support.models;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class SupportTicketResponseDTO {
    private String ticketNumber;
    private Long userId;
    private String userEmail;
    private TicketCategory category;
    private String subject;
    private String description;
    private TicketStatus status;
    private TicketPriority priority;
    private DeviceDiagnosticInfo diagnosticInfo;
    private List<String> attachmentUrls;
    private String resolutionNotes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
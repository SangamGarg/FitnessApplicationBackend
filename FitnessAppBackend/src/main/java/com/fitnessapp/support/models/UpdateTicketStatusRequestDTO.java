package com.fitnessapp.support.models;

import lombok.Data;

@Data
public class UpdateTicketStatusRequestDTO {
    private TicketStatus status;
    private TicketPriority priority;
    private String resolutionNotes;
}
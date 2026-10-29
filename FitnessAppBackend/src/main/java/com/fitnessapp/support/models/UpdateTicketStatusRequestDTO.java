package com.fitnessapp.support.models;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateTicketStatusRequestDTO {
    private TicketStatus status;
    private TicketPriority priority;
    @Email
    @Size(max = 254)
    private String assignedToEmail;
    @Size(max = 5000)
    private String resolutionNotes;
}
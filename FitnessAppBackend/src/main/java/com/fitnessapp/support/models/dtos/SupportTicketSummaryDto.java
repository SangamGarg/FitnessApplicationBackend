package com.fitnessapp.support.models.dtos;
import lombok.*;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SupportTicketSummaryDto { private long total; private long open; private long inProgress; private long resolved; private long closed; }

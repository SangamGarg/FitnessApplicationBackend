package com.fitnessapp.support.models.dtos;
import lombok.*; import java.util.List;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SupportHomeResponseDto { private List<SupportFaqCategoryDto> categories; private List<SupportFaqDto> popularFaqs; private SupportTicketSummaryDto ticketSummary; }

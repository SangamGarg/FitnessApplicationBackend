package com.fitnessapp.support.controller;

import com.fitnessapp.support.models.SupportTicketResponseDTO;
import com.fitnessapp.support.service.SupportTicketService;
import com.fitnessapp.support.models.TicketCategory;
import com.fitnessapp.support.models.TicketStatus;
import com.fitnessapp.support.models.UpdateTicketStatusRequestDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/dev/support")
@RequiredArgsConstructor
@Tag(name = "Portal Support Admin", description = "Administrative endpoints to triage, update, and resolve support tickets")
public class PortalSupportController {

    private final SupportTicketService supportTicketService;

    @Operation(summary = "List and search support tickets with pagination")
    @GetMapping("/tickets")
    public ResponseEntity<Page<SupportTicketResponseDTO>> getAllTickets(
            @RequestParam(required = false) TicketStatus status,
            @RequestParam(required = false) TicketCategory category,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        log.info("REST request to list support tickets with filters [status={}, category={}]", status, category);
        return ResponseEntity.ok(supportTicketService.getAllTickets(status, category, pageable));
    }

    @Operation(summary = "Update ticket status, priority, or resolution notes")
    @PatchMapping("/tickets/{ticketNumber}/status")
    public ResponseEntity<SupportTicketResponseDTO> updateTicketStatus(
            @PathVariable String ticketNumber,
            @RequestBody UpdateTicketStatusRequestDTO request) {
        log.info("REST request to update status of ticket [{}]", ticketNumber);
        return ResponseEntity.ok(supportTicketService.updateTicketStatus(ticketNumber, request));
    }
}
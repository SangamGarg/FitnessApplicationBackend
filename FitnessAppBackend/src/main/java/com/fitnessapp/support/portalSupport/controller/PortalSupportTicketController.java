package com.fitnessapp.support.portalSupport.controller;

import com.fitnessapp.portal.common.CurrentUserService;
import com.fitnessapp.support.models.dtos.request.SupportAdminMessageRequestDTO;
import com.fitnessapp.support.models.dtos.response.SupportTicketMessageResponseDTO;
import com.fitnessapp.support.models.dtos.response.SupportTicketResponseDTO;
import com.fitnessapp.support.models.enums.SupportIssueCategory;
import com.fitnessapp.support.service.SupportTicketService;
import com.fitnessapp.support.models.enums.TicketStatus;
import com.fitnessapp.support.models.dtos.request.UpdateTicketStatusRequestDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.fitnessapp.utils.portalUtil.PortalUtil.PORTAL_SUPPORT_ENDPOINT;

@Slf4j
@RestController
@RequestMapping(PORTAL_SUPPORT_ENDPOINT)
@RequiredArgsConstructor
public class PortalSupportTicketController {

    private final SupportTicketService supportTicketService;
    private final CurrentUserService currentUser;

    @GetMapping("/tickets")
    public ResponseEntity<Page<SupportTicketResponseDTO>> getAllTickets(
            @RequestParam(required = false) TicketStatus status,
            @RequestParam(required = false) SupportIssueCategory category,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Page must be non-negative and size must be between 1 and 100");
        }
        log.info("REST request to list support tickets with filters [status={}, category={}]", status, category);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(supportTicketService.searchTickets(status, category, query, pageable));
    }

    @GetMapping("/tickets/{ticketNumber}")
    public ResponseEntity<SupportTicketResponseDTO> getTicket(@PathVariable String ticketNumber) {
        return ResponseEntity.ok(supportTicketService.getTicketForAdmin(ticketNumber));
    }

    @PatchMapping("/tickets/{ticketNumber}/status")
    public ResponseEntity<SupportTicketResponseDTO> updateTicketStatus(
            @PathVariable String ticketNumber,
            @Valid @RequestBody UpdateTicketStatusRequestDTO request) {
        log.info("REST request to update status of ticket [{}]", ticketNumber);
        return ResponseEntity.ok(supportTicketService.updateTicketStatus(ticketNumber, request));
    }

    @GetMapping("/tickets/{ticketNumber}/messages")
    public ResponseEntity<List<SupportTicketMessageResponseDTO>> getMessages(@PathVariable String ticketNumber) {
        return ResponseEntity.ok(supportTicketService.getTicketMessagesForAdmin(ticketNumber));
    }

    @PostMapping("/tickets/{ticketNumber}/messages")
    public ResponseEntity<SupportTicketMessageResponseDTO> addMessage(
            @PathVariable String ticketNumber,
            @Valid @RequestBody SupportAdminMessageRequestDTO request
    ) {
        SupportTicketMessageResponseDTO message = supportTicketService.addAdminMessage(
                currentUser.getEmail(),
                ticketNumber,
                request.message(),
                request.internalNote()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(message);
    }
}
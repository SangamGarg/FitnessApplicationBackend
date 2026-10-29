package com.fitnessapp.portal.support.controller;

import com.fitnessapp.support.models.CreateSupportTicketRequestDTO;
import com.fitnessapp.support.models.SupportTicketMessageRequestDTO;
import com.fitnessapp.support.models.SupportTicketMessageResponseDTO;
import com.fitnessapp.support.models.SupportTicketResponseDTO;
import com.fitnessapp.support.service.SupportTicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/app/support")
@RequiredArgsConstructor
@Tag(name = "Client Support", description = "In-app support ticket submission, attachments, and history")
public class ClientSupportController {

    private final SupportTicketService supportTicketService;

    @Operation(
            summary = "Submit support ticket with file attachments",
            description = "Accepts multipart form-data containing the ticket DTO as JSON and optional image/video files."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Ticket created and email confirmation dispatched",
                    content = @Content(schema = @Schema(implementation = SupportTicketResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request attributes", content = @Content)
    })
    @PostMapping(value = "/tickets", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SupportTicketResponseDTO> createTicket(
            @RequestAttribute("userId") Long userId,
            @RequestAttribute("userEmail") String userEmail,

            @Parameter(description = "Support ticket metadata payload", required = true)
            @Valid @RequestPart("ticket") CreateSupportTicketRequestDTO request,

            @Parameter(description = "Optional screenshots or screen recording attachments")
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {

        log.info("REST multipart request from user [{}] to create support ticket", userId);
        SupportTicketResponseDTO response = supportTicketService.createTicket(userId, userEmail, request, files);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get user ticket history")
    @GetMapping("/tickets")
    public ResponseEntity<List<SupportTicketResponseDTO>> getUserTickets(
            @RequestAttribute("userId") Long userId) {
        log.info("REST request to fetch ticket history for user [{}]", userId);
        return ResponseEntity.ok(supportTicketService.getUserTickets(userId));
    }

    @Operation(summary = "Get single ticket details")
    @GetMapping("/tickets/{ticketNumber}")
    public ResponseEntity<SupportTicketResponseDTO> getTicketDetails(
            @RequestAttribute("userId") Long userId,
            @PathVariable String ticketNumber) {
        log.info("REST request for ticket [{}] by user [{}]", ticketNumber, userId);
        return ResponseEntity.ok(supportTicketService.getTicketByNumber(userId, ticketNumber));
    }

    @Operation(summary = "Get user-visible support ticket conversation")
    @GetMapping("/tickets/{ticketNumber}/messages")
    public ResponseEntity<List<SupportTicketMessageResponseDTO>> getTicketMessages(
            @RequestAttribute("userId") Long userId,
            @PathVariable String ticketNumber) {
        return ResponseEntity.ok(supportTicketService.getTicketMessages(userId, ticketNumber));
    }

    @Operation(summary = "Reply to a support ticket")
    @PostMapping("/tickets/{ticketNumber}/messages")
    public ResponseEntity<SupportTicketMessageResponseDTO> addTicketMessage(
            @RequestAttribute("userId") Long userId,
            @RequestAttribute("userEmail") String userEmail,
            @PathVariable String ticketNumber,
            @Valid @RequestBody SupportTicketMessageRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                supportTicketService.addUserMessage(userId, userEmail, ticketNumber, request.message())
        );
    }
}
package com.fitnessapp.support.controller;

import com.fitnessapp.support.models.dtos.request.CreateSupportTicketRequestDTO;
import com.fitnessapp.support.models.dtos.request.SupportTicketMessageRequestDTO;
import com.fitnessapp.support.models.dtos.response.SupportTicketMessageResponseDTO;
import com.fitnessapp.support.models.dtos.response.SupportTicketResponseDTO;
import com.fitnessapp.support.service.SupportTicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.fitnessapp.utils.appUtil.AppUtilEndpoint.APP_SUPPORT_ENDPOINT;

@Slf4j
@RestController
@RequestMapping(APP_SUPPORT_ENDPOINT)
@RequiredArgsConstructor
public class SupportTicketController {

    private final SupportTicketService supportTicketService;

    @PostMapping(value = "/tickets", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SupportTicketResponseDTO> createTicket(
            @RequestAttribute("userId") Long userId,
            @RequestAttribute("userEmail") String userEmail,
            @Valid @RequestPart("ticket") CreateSupportTicketRequestDTO request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {

        log.info("REST multipart request from user [{}] to create support ticket", userId);
        SupportTicketResponseDTO response = supportTicketService.createTicket(userId, userEmail, request, files);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/tickets")
    public ResponseEntity<List<SupportTicketResponseDTO>> getUserTickets(
            @RequestAttribute("userId") Long userId) {
        log.info("REST request to fetch ticket history for user [{}]", userId);
        return ResponseEntity.ok(supportTicketService.getUserTickets(userId));
    }

    @GetMapping("/tickets/{ticketNumber}")
    public ResponseEntity<SupportTicketResponseDTO> getTicketDetails(
            @RequestAttribute("userId") Long userId,
            @PathVariable String ticketNumber) {
        log.info("REST request for ticket [{}] by user [{}]", ticketNumber, userId);
        return ResponseEntity.ok(supportTicketService.getTicketByNumber(userId, ticketNumber));
    }

    @GetMapping("/tickets/{ticketNumber}/messages")
    public ResponseEntity<List<SupportTicketMessageResponseDTO>> getTicketMessages(
            @RequestAttribute("userId") Long userId,
            @PathVariable String ticketNumber) {
        return ResponseEntity.ok(supportTicketService.getTicketMessages(userId, ticketNumber));
    }

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
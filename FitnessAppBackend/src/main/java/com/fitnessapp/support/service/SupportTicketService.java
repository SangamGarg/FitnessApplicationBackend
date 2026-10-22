package com.fitnessapp.support.service;

import com.fitnessapp.support.models.CreateSupportTicketRequestDTO;
import com.fitnessapp.support.models.SupportTicketResponseDTO;
import com.fitnessapp.support.models.TicketCategory;
import com.fitnessapp.support.models.TicketStatus;
import com.fitnessapp.support.models.UpdateTicketStatusRequestDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface SupportTicketService {

    // =========================================================================
    // Client / Mobile Actions
    // =========================================================================

    /**
     * Creates a new support ticket with optional multipart file attachments (screenshots/recordings).
     *
     * @param userId      ID of the authenticated user.
     * @param userEmail   Email address of the user for notifications.
     * @param request     Ticket metadata (subject, category, description, diagnostic info).
     * @param attachments Optional list of media files (PNG, JPG, MP4) up to allowable limit.
     * @return Persisted support ticket details.
     */
    SupportTicketResponseDTO createTicket(
            Long userId,
            String userEmail,
            CreateSupportTicketRequestDTO request,
            List<MultipartFile> attachments
    );

    /**
     * Retrieves all support tickets filed by a specific user.
     *
     * @param userId ID of the user.
     * @return List of tickets ordered by creation date descending.
     */
    List<SupportTicketResponseDTO> getUserTickets(Long userId);

    /**
     * Retrieves details of a single ticket by its reference number with user ownership validation.
     *
     * @param userId       ID of the requesting user.
     * @param ticketNumber Unique human-readable ticket identifier (e.g., TCK-20261022-8491).
     * @return Support ticket details.
     */
    SupportTicketResponseDTO getTicketByNumber(Long userId, String ticketNumber);

    // =========================================================================
    // Admin / Portal Actions
    // =========================================================================

    /**
     * Retrieves a paginated list of tickets with optional status and category filters.
     *
     * @param status   Optional status filter (e.g., OPEN, IN_PROGRESS, RESOLVED).
     * @param category Optional category filter.
     * @param pageable Pagination and sorting metadata.
     * @return Page of matching tickets.
     */
    Page<SupportTicketResponseDTO> getAllTickets(TicketStatus status, TicketCategory category, Pageable pageable);

    /**
     * Updates status, priority, or resolution notes for an existing ticket.
     *
     * @param ticketNumber Unique ticket identifier.
     * @param request      Updated status, priority, and resolution notes.
     * @return Updated support ticket details.
     */
    SupportTicketResponseDTO updateTicketStatus(String ticketNumber, UpdateTicketStatusRequestDTO request);
}
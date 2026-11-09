package com.fitnessapp.support.service;

import com.fitnessapp.support.models.dtos.request.CreateSupportTicketRequestDTO;
import com.fitnessapp.support.models.dtos.response.SupportTicketResponseDTO;
import com.fitnessapp.support.models.dtos.response.SupportTicketMessageResponseDTO;
import com.fitnessapp.support.models.enums.TicketCategory;
import com.fitnessapp.support.models.enums.TicketStatus;
import com.fitnessapp.support.models.dtos.request.UpdateTicketStatusRequestDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface SupportTicketService {


    SupportTicketResponseDTO createTicket(
            Long userId,
            String userEmail,
            CreateSupportTicketRequestDTO request,
            List<MultipartFile> attachments
    );

    List<SupportTicketResponseDTO> getUserTickets(Long userId);


    SupportTicketResponseDTO getTicketByNumber(Long userId, String ticketNumber);


    Page<SupportTicketResponseDTO> getAllTickets(TicketStatus status, TicketCategory category, Pageable pageable);

    Page<SupportTicketResponseDTO> searchTickets(
            TicketStatus status,
            TicketCategory category,
            String query,
            Pageable pageable
    );

    SupportTicketResponseDTO getTicketForAdmin(String ticketNumber);

    List<SupportTicketMessageResponseDTO> getTicketMessages(Long userId, String ticketNumber);

    List<SupportTicketMessageResponseDTO> getTicketMessagesForAdmin(String ticketNumber);

    SupportTicketMessageResponseDTO addUserMessage(Long userId, String userEmail, String ticketNumber, String message);

    SupportTicketMessageResponseDTO addAdminMessage(String actorEmail, String ticketNumber, String message, boolean internalNote);

    SupportTicketResponseDTO updateTicketStatus(String ticketNumber, UpdateTicketStatusRequestDTO request);
}
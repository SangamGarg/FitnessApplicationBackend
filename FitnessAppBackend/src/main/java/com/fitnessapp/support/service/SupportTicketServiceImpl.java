package com.fitnessapp.support.service;

import com.fitnessapp.portal.dev.assetUploadDev.service.FileStorageService;
import com.fitnessapp.support.models.SupportTicketResponseDTO;
import com.fitnessapp.support.models.*;
import com.fitnessapp.support.repository.SupportTicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SupportTicketServiceImpl implements SupportTicketService {

    private final SupportTicketRepository ticketRepository;
    private final FileStorageService fileStorageService;
    private final SupportEmailService emailService;

    @Override
    @Transactional
    public SupportTicketResponseDTO createTicket(
            Long userId,
            String userEmail,
            CreateSupportTicketRequestDTO request,
            List<MultipartFile> attachments) {

        log.info("Initiating ticket creation for user ID [{}] with {} attachment(s)",
                userId, (attachments != null ? attachments.size() : 0));

        // 1. Process and upload attachments if present
        List<String> uploadedAttachmentUrls = new ArrayList<>();
        if (attachments != null && !attachments.isEmpty()) {
            for (MultipartFile file : attachments) {
                if (!file.isEmpty()) {
                    String uploadedUrl = fileStorageService.uploadFile(file, "support-attachments");
                    uploadedAttachmentUrls.add(uploadedUrl);
                }
            }
            log.info("Persisted {} ticket attachment(s) to cloud storage", uploadedAttachmentUrls.size());
        }

        // 2. Derive ticket priority
        TicketPriority assignedPriority = (request.getCategory() == TicketCategory.BILLING_SUBSCRIPTION)
                ? TicketPriority.HIGH
                : TicketPriority.MEDIUM;

        // 3. Build & save ticket entity
        SupportTicketEntity entity = SupportTicketEntity.builder()
                .ticketNumber(generateUniqueTicketNumber())
                .userId(userId)
                .userEmail(userEmail)
                .category(request.getCategory())
                .subject(request.getSubject().trim())
                .description(request.getDescription().trim())
                .status(TicketStatus.OPEN)
                .priority(assignedPriority)
                .diagnosticInfo(request.getDiagnosticInfo())
                .attachmentUrls(uploadedAttachmentUrls)
                .build();

        SupportTicketEntity saved = ticketRepository.save(entity);
        log.info("Ticket [{}] persisted successfully in database", saved.getTicketNumber());

        // 4. Trigger asynchronous email confirmation
        emailService.sendTicketConfirmationEmail(saved);

        return toResponseDTO(saved);
    }

    @Override
    public List<SupportTicketResponseDTO> getUserTickets(Long userId) {
        log.debug("Fetching support ticket history for user ID [{}]", userId);
        return ticketRepository.findAllByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Override
    public SupportTicketResponseDTO getTicketByNumber(Long userId, String ticketNumber) {
        log.debug("Fetching ticket [{}] for user ID [{}]", ticketNumber, userId);
        SupportTicketEntity ticket = ticketRepository.findByTicketNumber(ticketNumber)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + ticketNumber));

        if (!ticket.getUserId().equals(userId)) {
            log.warn("Unauthorized ticket access attempt: user ID [{}] attempted reading ticket [{}]", userId, ticketNumber);
            throw new SecurityException("You do not have permission to view this ticket.");
        }

        return toResponseDTO(ticket);
    }

    @Override
    public Page<SupportTicketResponseDTO> getAllTickets(TicketStatus status, TicketCategory category, Pageable pageable) {
        if (status != null) {
            return ticketRepository.findAllByStatus(status, pageable).map(this::toResponseDTO);
        }
        if (category != null) {
            return ticketRepository.findAllByCategory(category, pageable).map(this::toResponseDTO);
        }
        return ticketRepository.findAll(pageable).map(this::toResponseDTO);
    }

    @Override
    @Transactional
    public SupportTicketResponseDTO updateTicketStatus(String ticketNumber, UpdateTicketStatusRequestDTO request) {
        log.info("Updating ticket status for [{}] to [{}]", ticketNumber, request.getStatus());

        SupportTicketEntity ticket = ticketRepository.findByTicketNumber(ticketNumber)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + ticketNumber));

        if (request.getStatus() != null) {
            ticket.setStatus(request.getStatus());
        }
        if (request.getPriority() != null) {
            ticket.setPriority(request.getPriority());
        }
        if (request.getResolutionNotes() != null) {
            ticket.setResolutionNotes(request.getResolutionNotes());
        }

        SupportTicketEntity updated = ticketRepository.save(ticket);

        // Notify user if ticket is marked RESOLVED
        if (updated.getStatus() == TicketStatus.RESOLVED) {
            emailService.sendTicketResolutionEmail(updated);
        }

        return toResponseDTO(updated);
    }

    private String generateUniqueTicketNumber() {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int randomSuffix = ThreadLocalRandom.current().nextInt(1000, 9999);
        return "TCK-" + datePart + "-" + randomSuffix;
    }

    private SupportTicketResponseDTO toResponseDTO(SupportTicketEntity entity) {
        return SupportTicketResponseDTO.builder()
                .ticketNumber(entity.getTicketNumber())
                .userId(entity.getUserId())
                .userEmail(entity.getUserEmail())
                .category(entity.getCategory())
                .subject(entity.getSubject())
                .description(entity.getDescription())
                .status(entity.getStatus())
                .priority(entity.getPriority())
                .diagnosticInfo(entity.getDiagnosticInfo())
                .attachmentUrls(entity.getAttachmentUrls())
                .resolutionNotes(entity.getResolutionNotes())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
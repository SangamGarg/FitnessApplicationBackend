package com.fitnessapp.support.service;

import com.fitnessapp.portal.dev.assetUploadDev.service.FileStorageService;
import com.fitnessapp.support.models.SupportTicketResponseDTO;
import com.fitnessapp.support.models.*;
import com.fitnessapp.support.repository.SupportTicketRepository;
import com.fitnessapp.support.repository.SupportTicketMessageRepository;
import com.fitnessapp.support.models.SupportTicketMessageEntity;
import com.fitnessapp.support.models.SupportTicketMessageResponseDTO;
import com.fitnessapp.portal.audit.PortalAuditService;
import com.fitnessapp.portal.common.enums.UserRole;
import com.fitnessapp.portal.repository.PortalAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

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
    private final SupportTicketMessageRepository messageRepository;
    private final PortalAccountRepository portalAccounts;
    private final FileStorageService fileStorageService;
    private final SupportEmailService emailService;
    private final PortalAuditService audit;

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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found"));

        if (!ticket.getUserId().equals(userId)) {
            log.warn("Unauthorized ticket access attempt: user ID [{}] attempted reading ticket [{}]", userId, ticketNumber);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to view this ticket");
        }

        return toResponseDTO(ticket);
    }

    @Override
    public Page<SupportTicketResponseDTO> getAllTickets(TicketStatus status, TicketCategory category, Pageable pageable) {
        return searchTickets(status, category, null, pageable);
    }

    @Override
    public Page<SupportTicketResponseDTO> searchTickets(
            TicketStatus status,
            TicketCategory category,
            String query,
            Pageable pageable
    ) {
        String normalizedQuery = query == null || query.isBlank() ? null : query.trim();
        return ticketRepository.searchForAdmin(status, category, normalizedQuery, pageable).map(this::toResponseDTO);
    }

    @Override
    public SupportTicketResponseDTO getTicketForAdmin(String ticketNumber) {
        return toResponseDTO(findTicket(ticketNumber));
    }

    @Override
    public List<SupportTicketMessageResponseDTO> getTicketMessages(Long userId, String ticketNumber) {
        SupportTicketEntity ticket = findTicket(ticketNumber);
        if (!ticket.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to view this ticket");
        }
        return messageRepository.findAllByTicketTicketNumberAndInternalNoteFalseOrderByCreatedAtAsc(ticketNumber)
                .stream()
                .map(this::toMessageResponse)
                .toList();
    }

    @Override
    public List<SupportTicketMessageResponseDTO> getTicketMessagesForAdmin(String ticketNumber) {
        findTicket(ticketNumber);
        return messageRepository.findAllByTicketTicketNumberOrderByCreatedAtAsc(ticketNumber)
                .stream()
                .map(this::toMessageResponse)
                .toList();
    }

    @Override
    @Transactional
    public SupportTicketMessageResponseDTO addUserMessage(
            Long userId,
            String userEmail,
            String ticketNumber,
            String message
    ) {
        SupportTicketEntity ticket = findTicket(ticketNumber);
        if (!ticket.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to reply to this ticket");
        }
        if (ticket.getStatus() == TicketStatus.CLOSED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A closed ticket cannot receive replies");
        }
        return toMessageResponse(messageRepository.save(
                new SupportTicketMessageEntity(ticket, userEmail, message.trim(), false)
        ));
    }

    @Override
    @Transactional
    public SupportTicketMessageResponseDTO addAdminMessage(
            String actorEmail,
            String ticketNumber,
            String message,
            boolean internalNote
    ) {
        SupportTicketEntity ticket = findTicket(ticketNumber);
        SupportTicketMessageEntity saved = messageRepository.save(
                new SupportTicketMessageEntity(ticket, actorEmail, message.trim(), internalNote)
        );
        audit.record(
                internalNote ? "SUPPORT_INTERNAL_NOTE_ADDED" : "SUPPORT_PUBLIC_REPLY_ADDED",
                "SUPPORT_TICKET",
                ticketNumber,
                internalNote ? "Internal support note added" : "Public support reply added"
        );
        return toMessageResponse(saved);
    }

    @Override
    @Transactional
    public SupportTicketResponseDTO updateTicketStatus(String ticketNumber, UpdateTicketStatusRequestDTO request) {
        log.info("Updating ticket status for [{}] to [{}]", ticketNumber, request.getStatus());

        SupportTicketEntity ticket = ticketRepository.findByTicketNumber(ticketNumber)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + ticketNumber));

        if (request.getStatus() != null && !isValidTransition(ticket.getStatus(), request.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ticket status cannot transition from " + ticket.getStatus() + " to " + request.getStatus()
            );
        }
        TicketStatus previousStatus = ticket.getStatus();
        if (request.getStatus() != null) {
            ticket.setStatus(request.getStatus());
        }
        if (request.getPriority() != null) {
            ticket.setPriority(request.getPriority());
        }
        if (request.getAssignedToEmail() != null) {
            String assignee = request.getAssignedToEmail().trim().toLowerCase(java.util.Locale.ROOT);
            if (!assignee.isBlank()) {
                var account = portalAccounts.findByEmailIgnoreCase(assignee)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Assignee is not an active portal account"));
                if (!account.isEnabled() || (account.getRole() != UserRole.ADMIN && account.getRole() != UserRole.DEVELOPER)) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Assignee is not an active portal account");
                }
            }
            ticket.setAssignedToEmail(assignee.isBlank() ? null : assignee);
        }
        if (request.getResolutionNotes() != null) {
            ticket.setResolutionNotes(request.getResolutionNotes());
        }

        SupportTicketEntity updated = ticketRepository.save(ticket);
        audit.record(
                "SUPPORT_TICKET_UPDATED",
                "SUPPORT_TICKET",
                ticketNumber,
                "Status changed from " + previousStatus + " to " + updated.getStatus()
                        + "; assignedTo=" + updated.getAssignedToEmail()
        );

        // Notify user if ticket is marked RESOLVED
        if (updated.getStatus() == TicketStatus.RESOLVED) {
            emailService.sendTicketResolutionEmail(updated);
        }

        return toResponseDTO(updated);
    }

    private SupportTicketEntity findTicket(String ticketNumber) {
        return ticketRepository.findByTicketNumber(ticketNumber)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found"));
    }

    private boolean isValidTransition(TicketStatus current, TicketStatus next) {
        if (current == next) {
            return true;
        }
        return switch (current) {
            case OPEN -> next == TicketStatus.IN_PROGRESS || next == TicketStatus.RESOLVED || next == TicketStatus.CLOSED;
            case IN_PROGRESS -> next == TicketStatus.RESOLVED || next == TicketStatus.CLOSED;
            case RESOLVED -> next == TicketStatus.IN_PROGRESS || next == TicketStatus.CLOSED;
            case CLOSED -> false;
        };
    }

    private SupportTicketMessageResponseDTO toMessageResponse(SupportTicketMessageEntity entity) {
        return new SupportTicketMessageResponseDTO(
                entity.getId(),
                entity.getActorEmail(),
                entity.getMessage(),
                entity.isInternalNote(),
                entity.getCreatedAt()
        );
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
                .assignedToEmail(entity.getAssignedToEmail())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
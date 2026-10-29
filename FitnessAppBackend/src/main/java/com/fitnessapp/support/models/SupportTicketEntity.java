package com.fitnessapp.support.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "support_tickets", indexes = {
        @Index(name = "idx_ticket_number", columnList = "ticket_number", unique = true),
        @Index(name = "idx_ticket_user_id", columnList = "user_id"),
        @Index(name = "idx_ticket_status", columnList = "status"),
        @Index(name = "idx_ticket_category", columnList = "category")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupportTicketEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Human-readable reference number (e.g., "TICK-20261022-8491")
    @Column(name = "ticket_number", nullable = false, unique = true, length = 32)
    private String ticketNumber;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "user_email", nullable = false, length = 150)
    private String userEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TicketCategory category;

    @Column(nullable = false, length = 200)
    private String subject;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TicketStatus status = TicketStatus.OPEN;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TicketPriority priority = TicketPriority.MEDIUM;

    // Auto-captured mobile device parameters
    @Embedded
    private DeviceDiagnosticInfo diagnosticInfo;

    // Supabase / S3 attachment URLs (screenshots, screen recordings)
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "support_ticket_attachments", joinColumns = @JoinColumn(name = "ticket_id"))
    @Column(name = "attachment_url", length = 500)
    @Builder.Default
    private List<String> attachmentUrls = new ArrayList<>();

    // Administrative notes / agent resolution explanation
    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    private String resolutionNotes;

    @Column(name = "assigned_to_email", length = 254)
    private String assignedToEmail;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
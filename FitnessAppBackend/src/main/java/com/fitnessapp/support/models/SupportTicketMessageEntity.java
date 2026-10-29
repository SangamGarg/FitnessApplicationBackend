package com.fitnessapp.support.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "support_ticket_messages", indexes = {
        @Index(name = "idx_support_message_ticket_created", columnList = "ticket_id, created_at")
})
public class SupportTicketMessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_id", nullable = false, foreignKey = @ForeignKey(name = "fk_support_message_ticket"))
    private SupportTicketEntity ticket;

    @Column(name = "actor_email", nullable = false, length = 254)
    private String actorEmail;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "internal_note", nullable = false)
    private boolean internalNote;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public SupportTicketMessageEntity(
            SupportTicketEntity ticket,
            String actorEmail,
            String message,
            boolean internalNote
    ) {
        this.ticket = ticket;
        this.actorEmail = actorEmail;
        this.message = message;
        this.internalNote = internalNote;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}

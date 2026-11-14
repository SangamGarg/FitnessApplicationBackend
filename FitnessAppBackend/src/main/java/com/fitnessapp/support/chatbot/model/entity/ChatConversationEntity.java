
package com.fitnessapp.support.chatbot.model.entity;

import com.fitnessapp.support.chatbot.model.enums.ChatConversationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "app_support_chat_conversations",
        indexes = {
                @Index(
                        name = "idx_chat_conversation_user_updated",
                        columnList = "user_id, updated_at"
                ),
                @Index(
                        name = "idx_chat_conversation_status",
                        columnList = "status"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatConversationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false, length = 128)
    private String userId;

    @Column(length = 200)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ChatConversationStatus status =
            ChatConversationStatus.ACTIVE;

    @Column(name = "linked_ticket_number", length = 100)
    private String linkedTicketNumber;

    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;

        if (lastMessageAt == null) {
            lastMessageAt = now;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

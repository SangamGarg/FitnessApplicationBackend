
package com.fitnessapp.support.chatbot.model.entity;

import com.fitnessapp.support.chatbot.model.enums.ChatSenderType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "app_support_chat_messages",
        indexes = {
                @Index(
                        name = "idx_chat_message_conversation_created",
                        columnList = "conversation_id, created_at"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "conversation_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_chat_message_conversation")
    )
    private ChatConversationEntity conversation;

    @Enumerated(EnumType.STRING)
    @Column(name = "sender_type", nullable = false, length = 20)
    private ChatSenderType senderType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

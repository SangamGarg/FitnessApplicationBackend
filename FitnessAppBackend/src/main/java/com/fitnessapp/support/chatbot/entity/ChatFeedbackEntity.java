
package com.fitnessapp.support.chatbot.entity;

import com.fitnessapp.support.chatbot.enums.ChatFeedbackRating;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "app_support_chat_feedback",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_chat_feedback_message_user",
                        columnNames = {"message_id", "user_id"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_chat_feedback_user",
                        columnList = "user_id"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatFeedbackEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "message_id", nullable = false)
    private ChatMessageEntity message;

    @Column(name = "user_id", nullable = false, length = 128)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ChatFeedbackRating rating;

    @Column(name = "feedback_text", length = 1000)
    private String feedbackText;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

package com.fitnessapp.feedback.model;


import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "app_user_feedbacks",
        indexes = {
                @Index(name = "idx_feedback_user_id", columnList = "user_id"),
                @Index(name = "idx_feedback_created_at", columnList = "created_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class FeedbackEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String feedback;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "user_id", nullable = false, length = 150)
    private String userId;

    @Column(nullable = false, length = 254)
    private String email;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
package com.fitnessapp.analytics.model.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "app_user_body_measurements",
        indexes = {
                @Index(
                        name = "idx_body_measurements_user_date",
                        columnList = "user_id, measured_at"
                )
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BodyMeasurementEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "weight_kg", precision = 6, scale = 2)
    private BigDecimal weightKg;

    @Column(name = "body_fat_percentage", precision = 5, scale = 2)
    private BigDecimal bodyFatPercentage;

    @Column(name = "measured_at", nullable = false)
    private LocalDateTime measuredAt;
}

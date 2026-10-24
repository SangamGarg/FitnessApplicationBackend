package com.fitnessapp.support.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "support_faq_categories", indexes = @Index(name="idx_support_faq_category_active_sort", columnList="active, sort_order"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SupportFaqCategoryEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name="category_key", nullable=false, unique=true, length=60) private String categoryKey;
    @Column(nullable=false, length=100) private String name;
    @Column(length=300) private String description;
    @Column(name="icon_key", length=60) private String iconKey;
    @Column(nullable=false) @Builder.Default private boolean active = true;
    @Column(name="sort_order", nullable=false) @Builder.Default private int sortOrder = 0;
    @Column(name="created_at", nullable=false, updatable=false) @Builder.Default private LocalDateTime createdAt = LocalDateTime.now();
    @PrePersist void prePersist(){ if(createdAt == null) createdAt = LocalDateTime.now(); }
}

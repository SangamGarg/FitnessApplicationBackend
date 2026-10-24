package com.fitnessapp.support.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name="support_faq_articles", indexes={
 @Index(name="idx_support_faq_active_popular", columnList="active, popular, sort_order"),
 @Index(name="idx_support_faq_category", columnList="category_id")})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SupportFaqEntity {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="category_id", nullable=false) private SupportFaqCategoryEntity category;
 @Column(nullable=false, length=240) private String question;
 @Column(nullable=false, columnDefinition="TEXT") private String answer;
 @Column(nullable=false) @Builder.Default private boolean popular=false;
 @Column(nullable=false) @Builder.Default private boolean active=true;
 @Column(name="sort_order", nullable=false) @Builder.Default private int sortOrder=0;
 @UpdateTimestamp @Column(name="updated_at", nullable=false) private LocalDateTime updatedAt;
}

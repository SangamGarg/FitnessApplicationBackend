package com.fitnessapp.support.models.dtos;
import lombok.*; import java.time.LocalDateTime;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SupportFaqDto { private Long id; private String categoryKey; private String categoryName; private String question; private String answer; private boolean popular; private LocalDateTime updatedAt; }

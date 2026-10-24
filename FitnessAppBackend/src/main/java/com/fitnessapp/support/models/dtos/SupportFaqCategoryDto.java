package com.fitnessapp.support.models.dtos;
import lombok.*;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SupportFaqCategoryDto { private Long id; private String categoryKey; private String name; private String description; private String iconKey; private int sortOrder; }

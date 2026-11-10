package com.fitnessapp.support.portalSupport.models.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupportFaqCategoryRequestDto {

    @NotBlank(message = "Category key cannot be blank")
    @Size(max = 60, message = "Category key cannot exceed 60 characters")
    private String categoryKey;

    @NotBlank(message = "Category name cannot be blank")
    @Size(max = 100, message = "Category name cannot exceed 100 characters")
    private String name;

    @Size(max = 300, message = "Description cannot exceed 300 characters")
    private String description;

    @Size(max = 60, message = "Icon key cannot exceed 60 characters")
    private String iconKey;

    private Boolean active;

    @Min(value = 0, message = "Sort order cannot be negative")
    private Integer sortOrder;
}
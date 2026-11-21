package com.fitnessapp.profile.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BadgeAddRequestDto {
    @NotBlank
    @Size(max = 100)
    private String name;

    @NotBlank
    @Size(max = 1000)
    private String description;

    @Size(max = 2048)
    private String imageUrl;

    @Size(max = 50)
    private String category;

    @Size(max = 4000)
    private String criteriaMetadata;

    @Builder.Default
    private boolean active = true;
}

package com.fitnessapp.followUsLinks.model;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FollowUsLinksPatchRequestDto {

    @Size(max = 120, message = "Title cannot exceed 120 characters")
    private String title;

    @Size(max = 60, message = "Sub Title cannot exceed 60 characters")
    private String subtitle;

    @Size(max = 500, message = "URL cannot exceed 500 characters")
    private String url;
}
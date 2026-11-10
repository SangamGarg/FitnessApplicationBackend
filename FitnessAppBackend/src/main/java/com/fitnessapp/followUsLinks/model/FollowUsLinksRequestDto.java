package com.fitnessapp.followUsLinks.model;

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
public class FollowUsLinksRequestDto {

    @NotBlank(message = "Title cannot be blank")
    @Size(max = 120, message = "Title cannot exceed 120 characters")
    private String title;

    @NotBlank(message = "Sub Title cannot be blank")
    @Size(max = 60, message = "Sub Title cannot exceed 60 characters")
    private String subtitle;

    @NotBlank(message = "Icon Key cannot be blank")
    @Size(max = 10, message = "Icon Key cannot exceed 60 characters")
    private IconKeyFollowUs iconKey;

    @NotBlank(message = "URL cannot be blank")
    @Size(max = 500, message = "URL cannot exceed 500 characters")
    private String url;
}
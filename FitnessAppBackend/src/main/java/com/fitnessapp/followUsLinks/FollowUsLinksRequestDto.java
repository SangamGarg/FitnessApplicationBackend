package com.fitnessapp.followUsLinks;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
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
    @Schema(description = "Title of the link", example = "Play Store")
    private String title;

    @NotBlank(message = "URL cannot be blank")
    @Schema(description = "Destination URL", example = "https://play.google.com/store/apps/details?id=...")
    private String url;
}
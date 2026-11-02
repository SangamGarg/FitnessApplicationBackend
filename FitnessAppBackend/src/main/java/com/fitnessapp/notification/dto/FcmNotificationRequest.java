package com.fitnessapp.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FcmNotificationRequest {
    @NotBlank
    @Size(max = 200)
    private String title;

    @NotBlank
    @Size(max = 2000)
    private String body;

    @Size(max = 2048)
    @Pattern(regexp = "https?://.*", message = "imageUrl must be an HTTP(S) URL")
    private String imageUrl;

    /** FCM data payload values must be strings. Do not put secrets or personal data here. */
    @Size(max = 50)
    private Map<@NotBlank @Size(max = 128) String, @NotBlank @Size(max = 2048) String> data;
}

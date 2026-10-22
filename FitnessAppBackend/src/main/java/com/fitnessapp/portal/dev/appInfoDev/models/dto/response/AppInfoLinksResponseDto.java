package com.fitnessapp.portal.dev.appInfoDev.models.dto.response;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppInfoLinksResponseDto {
    private Long id;
    private String title;
    private String url;
}
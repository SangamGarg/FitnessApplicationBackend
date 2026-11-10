package com.fitnessapp.followUsLinks.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FollowUsLinksResponseDto {
    private Long id;
    private String title;
    private String iconKey;
    private String subtitle;
    private String url;
}
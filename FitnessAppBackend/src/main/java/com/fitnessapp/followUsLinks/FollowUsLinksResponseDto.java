package com.fitnessapp.followUsLinks;
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
    private String url;
}
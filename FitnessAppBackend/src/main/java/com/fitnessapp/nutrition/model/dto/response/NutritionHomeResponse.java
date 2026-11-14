package com.fitnessapp.nutrition.model.dto.response;


import lombok.*;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NutritionHomeResponse {
    private String title;
    private String subTitle;
    private String description;
}
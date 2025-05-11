package com.fitnessapp.userDetailsAndAuthService.models.entitites;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Embeddable
public class UserBodyDetails {
    private Float bmi;
    private Integer dailyCalories;
    private Float idealWeight;
    private Float bodyFat;
}

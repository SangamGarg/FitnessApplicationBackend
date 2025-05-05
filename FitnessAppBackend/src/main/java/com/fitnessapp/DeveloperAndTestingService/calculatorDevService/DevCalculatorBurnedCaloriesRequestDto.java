package com.fitnessapp.DeveloperAndTestingService.calculatorDevService;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DevCalculatorBurnedCaloriesRequestDto {

    @NotBlank(message = "Name must not be blank")
    private String name;

    @Positive(message = "MET value must be greater than zero")
    private Double metValue;

    @NotBlank(message = "ImageUrl must not be blank")
    @URL(message = "Not valid url")
    private String imageUrl;

}

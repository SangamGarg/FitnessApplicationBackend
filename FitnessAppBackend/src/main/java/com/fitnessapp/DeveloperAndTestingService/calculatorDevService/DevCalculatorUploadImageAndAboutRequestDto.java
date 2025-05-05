package com.fitnessapp.DeveloperAndTestingService.calculatorDevService;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DevCalculatorUploadImageAndAboutRequestDto {

    @NotBlank(message = "Name cannot be blank")
    @Size(max = 50, message = "Name cannot exceed 50 characters")
    private String name;

    @NotBlank(message = "Image URL cannot be blank")
    @URL(message = "Url is not in proper format")
    private String imageUrl;

    @NotBlank(message = "About cannot be blank")
    @Size(max = 500, message = "About cannot exceed 500 characters")
    private String about;

}
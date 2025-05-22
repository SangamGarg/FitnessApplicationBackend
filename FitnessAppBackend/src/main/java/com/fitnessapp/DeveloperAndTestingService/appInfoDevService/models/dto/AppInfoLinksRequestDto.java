package com.fitnessapp.DeveloperAndTestingService.appInfoDevService.models.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class AppInfoLinksRequestDto {

    @NotBlank(message = "Title cannot be blank or null")
    private String title;
    @NotBlank(message = "Url cannot be blank or null")
    private String url;


}
package com.fitnessapp.calculator.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalculatorCatalogResponseDto {
    private String status;
    private Integer statusCode;
    private String message;
    @Builder.Default
    private Instant timestamp = Instant.now();
}

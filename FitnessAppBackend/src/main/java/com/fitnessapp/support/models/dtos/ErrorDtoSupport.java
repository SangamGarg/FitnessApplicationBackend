package com.fitnessapp.support.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class ErrorDtoSupport {
    private String status;
    private Integer statusCode;
    private String errorImageUrl;
    private String errorMessage;
}


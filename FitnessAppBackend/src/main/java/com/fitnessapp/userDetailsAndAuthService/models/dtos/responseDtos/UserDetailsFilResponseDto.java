package com.fitnessapp.userDetailsAndAuthService.models.dtos.responseDtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailsFilResponseDto {

    private String status;
    private Integer statusCode;
    private String message;
}
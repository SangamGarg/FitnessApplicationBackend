package com.fitnessapp.userDetailsAndAuthService.models.dtos.responseDtos;


import com.fitnessapp.userDetailsAndAuthService.models.dtos.UserDetailsDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDetailsResponseDto {
    private String status;
    private Integer statusCode;
    private String jwtToken;
    private String refreshToken;
    private UserDetailsDto userDetailsDto;
}

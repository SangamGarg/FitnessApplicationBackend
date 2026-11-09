package com.fitnessapp.appAuth.model.dtos.responseDto;


import com.fitnessapp.appAuth.model.dtos.UserDetailsDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDetailsResponseDto {

    private String status;
    private Integer statusCode;
    private String message;
    @Builder.Default
    private Instant timestamp = Instant.now();
    private UserDetailsDto userDetailsDto;
}

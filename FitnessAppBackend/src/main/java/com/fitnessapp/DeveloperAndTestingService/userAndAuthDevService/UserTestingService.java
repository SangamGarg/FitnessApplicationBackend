package com.fitnessapp.DeveloperAndTestingService.userAndAuthDevService;

import org.springframework.http.ResponseEntity;

public interface UserTestingService {
    ResponseEntity<?> getJwtToken(String apiKey, String id, String email);
}

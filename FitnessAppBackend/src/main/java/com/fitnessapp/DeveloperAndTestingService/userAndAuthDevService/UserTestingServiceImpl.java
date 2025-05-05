package com.fitnessapp.DeveloperAndTestingService.userAndAuthDevService;

import com.fitnessapp.userDetailsAndAuthService.services.jwtService.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class UserTestingServiceImpl implements UserTestingService {
    private JwtService jwtService;
    @Value("${jwt.creation.secret.key}")
    private String jwtCreationKeyTest;

    public UserTestingServiceImpl(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public ResponseEntity<?> getJwtToken(String apiKey, String id, String email) {

        if (apiKey.equals(jwtCreationKeyTest)) {

            Map<String, String> map = Map.of("TestingJwtToken", jwtService.generateToken(id, email));
            return ResponseEntity.ok(map);
        } else {
            return ResponseEntity.badRequest().body("Wrong Api Key");
        }

    }
}

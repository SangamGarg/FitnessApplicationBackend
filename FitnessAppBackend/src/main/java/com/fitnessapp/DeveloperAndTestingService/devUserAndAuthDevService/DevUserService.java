package com.fitnessapp.DeveloperAndTestingService.devUserAndAuthDevService;

import org.springframework.http.ResponseEntity;

import java.util.List;

public interface DevUserService {
    ResponseEntity<?> saveUserForDevAndTest(String devName, String devEmail);

    ResponseEntity<?> deleteUserForDevAndTest(String devEmail);

    ResponseEntity<?> getUsersForDevAndTest();

    List<DevUserEntity> getUsersForDevAndTestForInterceptor();
}

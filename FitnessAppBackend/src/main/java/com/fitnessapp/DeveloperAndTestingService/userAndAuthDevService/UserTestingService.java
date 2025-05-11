package com.fitnessapp.DeveloperAndTestingService.userAndAuthDevService;

import org.springframework.http.ResponseEntity;

import java.util.List;

public interface UserTestingService {
    ResponseEntity<?> saveUserForDevAndTest(String name);

    ResponseEntity<?> deleteUserForDevAndTest(String name);

    List<UserTestingEntity> getUsersForDevAndTest();
}

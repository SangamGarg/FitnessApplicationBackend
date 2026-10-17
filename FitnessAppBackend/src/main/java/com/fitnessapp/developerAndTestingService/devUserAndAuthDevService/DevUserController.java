package com.fitnessapp.developerAndTestingService.devUserAndAuthDevService;

import com.fitnessapp.developerAndTestingService.devAndTestingServiceUtilities.DevAndTestingServiceUtility;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(DevAndTestingServiceUtility.API_PREFIX_USER)
@Validated
public class DevUserController {

    private final DevUserService userTestingService;

    public DevUserController(DevUserService userTestingService) {
        this.userTestingService = userTestingService;
    }

    @GetMapping("/save")
    public ResponseEntity<?> saveUserForDevAndTest(@RequestParam @NotBlank(message = "Name Cannot be blank or null") String devName, @RequestParam @NotBlank(message = "Email Cannot be blank or null") String devEmail) {
        return userTestingService.saveUserForDevAndTest(devName, devEmail);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteUserForDevAndTest(@RequestParam @NotBlank(message = "Name Cannot be blank or null") String devEmail) {
        return userTestingService.deleteUserForDevAndTest(devEmail);
    }

    @GetMapping("/all-users")
    public ResponseEntity<?> getAllUserForDevAndTest() {
        return userTestingService.getUsersForDevAndTest();
    }
}

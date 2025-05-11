package com.fitnessapp.DeveloperAndTestingService.userAndAuthDevService;

import com.fitnessapp.DeveloperAndTestingService.devAndTestingServiceUtilities.DevAndTestingServiceUtility;
import com.fitnessapp.userDetailsAndAuthService.userDetailsAndAuthServiceUtilities.AppConstantsUserAndAuthService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(DevAndTestingServiceUtility.API_PREFIX_USER)
@Validated
public class UserTestingController {

    private final UserTestingService userTestingService;

    public UserTestingController(UserTestingService userTestingService) {
        this.userTestingService = userTestingService;
    }

    @GetMapping("/save")
    public ResponseEntity<?> saveUserForDevAndTest(@RequestParam @NotBlank(message = "Name Cannot be blank or null") String name) {
        return userTestingService.saveUserForDevAndTest(name);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteUserForDevAndTest(@RequestParam @NotBlank(message = "Name Cannot be blank or null") String name) {
        return userTestingService.deleteUserForDevAndTest(name);
    }
}

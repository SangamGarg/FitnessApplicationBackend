package com.fitnessapp.dev.devUserAndAuthDevService;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import com.fitnessapp.dev.devAndTestingServiceUtilities.DevAndTestingServiceUtility;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(DevAndTestingServiceUtility.API_PREFIX_USER)
@Validated
@Tag(name = "Dev User", description = "Endpoints for dev user")
public class DevUserController {

    private final DevUserService userTestingService;

    public DevUserController(DevUserService userTestingService) {
        this.userTestingService = userTestingService;
    }

    @Operation(summary = "save User For Dev And Test", description = """
        Executes the save user for dev and test operation.
        """)
    @GetMapping("/save")
    public ResponseEntity<?> saveUserForDevAndTest(@RequestParam @NotBlank(message = "Name Cannot be blank or null") String devName, @RequestParam @NotBlank(message = "Email Cannot be blank or null") String devEmail) {
        return userTestingService.saveUserForDevAndTest(devName, devEmail);
    }

    @Operation(summary = "delete User For Dev And Test", description = """
        Executes the delete user for dev and test operation.
        """)
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteUserForDevAndTest(@RequestParam @NotBlank(message = "Name Cannot be blank or null") String devEmail) {
        return userTestingService.deleteUserForDevAndTest(devEmail);
    }

    @Operation(summary = "get All User For Dev And Test", description = """
        Executes the get all user for dev and test operation.
        """)
    @GetMapping("/all-users")
    public ResponseEntity<?> getAllUserForDevAndTest() {
        return userTestingService.getUsersForDevAndTest();
    }
}

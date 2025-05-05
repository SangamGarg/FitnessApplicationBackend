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

    private UserTestingService userTestingService;

    public UserTestingController(UserTestingService userTestingService) {
        this.userTestingService = userTestingService;
    }

    @GetMapping("/test-getjwt")
    public ResponseEntity<?> getTestJwt(@RequestHeader(name = "APIKEY")
                                        @NotBlank(message = "ApiKey cannot be blank")
                                        String apiKey,
                                        @NotBlank(message = "Id cannot be blank")
                                        @RequestParam String id,
                                        @Email(message = "Not valid email")
                                        @NotBlank(message = "Email cannot be blank")
                                        @RequestParam String email) {
        return userTestingService.getJwtToken(apiKey, id, email);
    }
}

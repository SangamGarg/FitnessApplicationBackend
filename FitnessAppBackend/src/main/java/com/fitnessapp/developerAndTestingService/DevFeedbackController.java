package com.fitnessapp.developerAndTestingService;

import com.fitnessapp.developerAndTestingService.devAndTestingServiceUtilities.DevAndTestingServiceUtility;
import com.fitnessapp.supportService.service.SupportService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Admin-only feedback endpoints. Everything under /api/v1/dev/** is protected by DevApiInterceptor
 * (X-NAME + X-API-KEY headers), so regular app users can no longer read or delete feedback.
 */
@RestController
@Validated
@RequestMapping(DevAndTestingServiceUtility.API_PREFIX_FEEDBACK)
public class DevFeedbackController {

    private final SupportService supportService;

    public DevFeedbackController(SupportService supportService) {
        this.supportService = supportService;
    }

    @GetMapping
    public ResponseEntity<?> getFeedback(@RequestParam @NotBlank(message = "Name Cannot be Blank or Null") String name) {
        return supportService.getFeedback(name);
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllFeedback() {
        return supportService.getAllFeedback();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFeedback(@PathVariable Long id) {
        return supportService.deleteFeedback(id);
    }
}
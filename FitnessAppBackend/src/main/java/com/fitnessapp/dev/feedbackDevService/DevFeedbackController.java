package com.fitnessapp.dev.feedbackDevService;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import com.fitnessapp.dev.devAndTestingServiceUtilities.DevAndTestingServiceUtility;
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
@Tag(name = "Dev Feedback", description = "Endpoints for dev feedback")
public class DevFeedbackController {

    private final SupportService supportService;

    public DevFeedbackController(SupportService supportService) {
        this.supportService = supportService;
    }

    @Operation(summary = "get Feedback", description = """
        Executes the get feedback operation.
        """)
    @GetMapping
    public ResponseEntity<?> getFeedback(@RequestParam @NotBlank(message = "Name Cannot be Blank or Null") String name) {
        return supportService.getFeedback(name);
    }

    @Operation(summary = "get All Feedback", description = """
        Executes the get all feedback operation.
        """)
    @GetMapping("/all")
    public ResponseEntity<?> getAllFeedback() {
        return supportService.getAllFeedback();
    }

    @Operation(summary = "delete Feedback", description = """
        Executes the delete feedback operation.
        """)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFeedback(@PathVariable Long id) {
        return supportService.deleteFeedback(id);
    }
}

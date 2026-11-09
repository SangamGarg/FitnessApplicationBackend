package com.fitnessapp.feedback;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.fitnessapp.utils.portalUtil.PortalUtil.PORTAL_FEEDBACK_ENDPOINT;


@RestController
@Validated
@RequestMapping(PORTAL_FEEDBACK_ENDPOINT)
@RequiredArgsConstructor
public class PortalFeedbackController {

    private final FeedbackService feedbackService;

    @GetMapping
    public ResponseEntity<?> getFeedback(@RequestParam @NotBlank(message = "Name Cannot be Blank or Null") String name) {
        return feedbackService.getFeedback(name);
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllFeedback() {
        return feedbackService.getAllFeedback();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFeedback(@PathVariable Long id) {
        return feedbackService.deleteFeedback(id);
    }
}

package com.fitnessapp.feedback.controller;

import com.fitnessapp.feedback.model.FeedbackDetailsDto;
import com.fitnessapp.feedback.service.FeedbackService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.fitnessapp.utils.portalUtil.PortalUtil.PORTAL_FEEDBACK_ENDPOINT;


@RestController
@RequiredArgsConstructor
@RequestMapping(PORTAL_FEEDBACK_ENDPOINT)
public class PortalFeedbackController {

    private final FeedbackService feedbackService;

    @GetMapping
    public ResponseEntity<Page<FeedbackDetailsDto>> getAllFeedback(
            @PageableDefault(
                    size = 20,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable) {

        return ResponseEntity.ok(
                feedbackService.getAllFeedback(pageable)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<FeedbackDetailsDto> getFeedback(
            @PathVariable Long id) {

        return ResponseEntity.ok(feedbackService.getFeedback(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<FeedbackDetailsDto>> getUserFeedback(
            @PathVariable String userId, @PageableDefault(
                    size = 20,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable) {

        return ResponseEntity.ok(feedbackService.getUserFeedback(userId, pageable));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFeedback(
            @PathVariable Long id) {

        feedbackService.deleteFeedback(id);
        return ResponseEntity.noContent().build();
    }
}
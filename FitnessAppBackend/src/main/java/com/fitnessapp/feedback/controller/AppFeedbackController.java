package com.fitnessapp.feedback.controller;

import com.fitnessapp.feedback.model.FeedbackRequestDto;
import com.fitnessapp.feedback.model.FeedbackResponseDto;
import com.fitnessapp.feedback.service.FeedbackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.fitnessapp.utils.appUtil.AppUtilEndpoint.APP_FEEDBACK_ENDPOINT;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(APP_FEEDBACK_ENDPOINT)
@Validated
public class AppFeedbackController {

    private final FeedbackService feedbackService;

    @PostMapping("/submit")
    public ResponseEntity<FeedbackResponseDto> postFeedback(
            @RequestBody @Valid FeedbackRequestDto request) {
        log.info("Feedback submission request received");
        return ResponseEntity.ok().body(feedbackService.postFeedback(request));
    }
}

package com.fitnessapp.feedback;

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
public class FeedbackController {

    private final FeedbackService feedbackService;

    @PostMapping("/feedback")
    public ResponseEntity<FeedbackResponseDto> postFeedback(
            @RequestBody @Valid FeedbackRequestDto feedbackRequestDto) {

        log.info("Feedback submission request received");

        ResponseEntity<FeedbackResponseDto> response = feedbackService.postFeedback(feedbackRequestDto);

        log.info("Feedback submission completed with status={}", response.getStatusCode());
        return response;
    }
}

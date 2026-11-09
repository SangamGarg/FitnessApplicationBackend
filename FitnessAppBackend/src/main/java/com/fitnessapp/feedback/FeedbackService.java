package com.fitnessapp.feedback;

import org.springframework.http.ResponseEntity;

public interface FeedbackService {


    ResponseEntity<FeedbackResponseDto> postFeedback(FeedbackRequestDto feedbackRequestDto);

    ResponseEntity<?> getAllFeedback();

    ResponseEntity<?> getFeedback(String name);

    ResponseEntity<?> deleteFeedback(Long id);
}

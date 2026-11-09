package com.fitnessapp.support.service;

import com.fitnessapp.support.models.dtos.request.FeedbackRequestDto;
import com.fitnessapp.support.models.dtos.response.FeedbackResponseDto;
import org.springframework.http.ResponseEntity;

public interface SupportService {

    ResponseEntity<?> getAppInfo();


    ResponseEntity<FeedbackResponseDto> postFeedback(FeedbackRequestDto feedbackRequestDto);

    ResponseEntity<?> getAllFeedback();

    ResponseEntity<?> getFeedback(String name);

    ResponseEntity<?> deleteFeedback(Long id);
}

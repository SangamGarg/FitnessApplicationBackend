package com.fitnessapp.supportService.service;

import com.fitnessapp.supportService.models.dtos.FeedbackRequestDto;
import org.springframework.http.ResponseEntity;

public interface SupportService {

    ResponseEntity<?> getAppInfo();


    ResponseEntity<?> postFeedback(FeedbackRequestDto feedbackRequestDto);

    ResponseEntity<?> getAllFeedback();

    ResponseEntity<?> getFeedback(String name);

    ResponseEntity<?> deleteFeedback(Long id);
}

package com.fitnessapp.feedback.service;

import com.fitnessapp.feedback.model.FeedbackDetailsDto;
import com.fitnessapp.feedback.model.FeedbackRequestDto;
import com.fitnessapp.feedback.model.FeedbackResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FeedbackService {

    FeedbackResponseDto postFeedback(
            FeedbackRequestDto request
    );

    Page<FeedbackDetailsDto> getAllFeedback(Pageable pageable);

    FeedbackDetailsDto getFeedback(Long id);

    Page<FeedbackDetailsDto> getUserFeedback(String id, Pageable pageable);

    void deleteFeedback(Long id);
}


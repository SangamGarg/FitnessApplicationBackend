package com.fitnessapp.feedback.service;


import com.fitnessapp.feedback.model.FeedbackDetailsDto;
import com.fitnessapp.feedback.model.FeedbackEntity;
import com.fitnessapp.feedback.model.FeedbackRequestDto;
import com.fitnessapp.feedback.model.FeedbackResponseDto;
import com.fitnessapp.feedback.repo.FeedbackRepository;
import com.fitnessapp.utils.appUtil.AppResponseStatus;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeedbackServiceImpl implements FeedbackService {

    private final FeedbackRepository feedbackRepository;

    @Override
    @Transactional
    public FeedbackResponseDto postFeedback(FeedbackRequestDto request) {

        String userId = currentUserId();

        FeedbackEntity entity = FeedbackEntity.builder()
                .feedback(request.getFeedback().trim())
                .name(request.getName())
                .email(request.getEmail())
                .userId(userId)
                .build();

        FeedbackEntity saved = feedbackRepository.save(entity);

        return FeedbackResponseDto.builder()
                .status(AppResponseStatus.SUCCESS.name())
                .statusCode(201)
                .message("Feedback submitted successfully")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FeedbackDetailsDto> getAllFeedback(Pageable pageable) {

        return feedbackRepository
                .findAllByOrderByCreatedAtDesc(pageable)
                .map(this::mapToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public FeedbackDetailsDto getFeedback(Long id) {

        FeedbackEntity entity = feedbackRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("Feedback not found"));

        return mapToDto(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FeedbackDetailsDto> getUserFeedback(String userId, Pageable pageable) {
        return feedbackRepository
                .findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::mapToDto);
    }

    @Override
    @Transactional
    public void deleteFeedback(Long id) {

        FeedbackEntity entity = feedbackRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("Feedback not found"));

        feedbackRepository.delete(entity);
    }

    private FeedbackDetailsDto mapToDto(FeedbackEntity entity) {

        return FeedbackDetailsDto.builder()
                .id(entity.getId())
                .feedback(entity.getFeedback())
                .name(entity.getName())
                .email(entity.getEmail())
                .userId(entity.getUserId())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    private FeedbackResponseDto errorResponseFeedback(HttpStatus status, String message) {
        log.warn(message);
        return FeedbackResponseDto.builder().status(AppResponseStatus.FAILED.name()).statusCode(status.value()).message(message).build();
    }

    private String currentUserId() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null
                || "anonymousUser".equals(authentication.getName())) {

            throw new AccessDeniedException("Authentication required");
        }

        return authentication.getName();
    }
}
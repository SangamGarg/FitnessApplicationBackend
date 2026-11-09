package com.fitnessapp.support.service;

import com.fitnessapp.support.portalSupport.repo.AppInfoRepository;
import com.fitnessapp.support.portalSupport.models.entity.AppInfoEntity;
import com.fitnessapp.portal.dev.assetUploadDev.model.AppMediaAssetEntity;
import com.fitnessapp.support.models.dtos.request.FeedbackRequestDto;
import com.fitnessapp.support.models.dtos.response.FeedbackResponseDto;
import com.fitnessapp.support.models.entity.FeedbackEntity;
import com.fitnessapp.support.repo.FeedbackRepository;
import com.fitnessapp.utils.appUtil.AppResponseStatus;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
@Slf4j
public class SupportServiceImpl implements SupportService {
    private final FeedbackRepository feedbackRepository;
    private final AppInfoRepository appInfoRepository;

    public SupportServiceImpl(FeedbackRepository feedbackRepository, AppInfoRepository appInfoRepository) {
        this.feedbackRepository = feedbackRepository;
        this.appInfoRepository = appInfoRepository;
    }

    @Override
    public ResponseEntity<?> getAppInfo() {
        try {
            Optional<AppInfoEntity> appInfo = appInfoRepository.findById(1L);
            if (appInfo.isPresent()) {
                return ResponseEntity.ok(appInfo);
            } else {
                return ResponseEntity.internalServerError().body("Not Found App Info");
            }


        } catch (Exception e) {
            log.error("Request failed: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("SupportUtils.errorDtoSupportService(e)");
        }
    }

    @Override
    @Transactional
    public ResponseEntity<FeedbackResponseDto> postFeedback(FeedbackRequestDto feedbackRequestDto) {
        try {

            feedbackRepository.save(FeedbackEntity.builder()
                    .feedback(feedbackRequestDto.getFeedback())
                    .email(feedbackRequestDto.getEmail())
                    .userId(feedbackRequestDto.getUserId())
                    .name(feedbackRequestDto.getName())
                    .build());

            FeedbackResponseDto responseDto = FeedbackResponseDto
                    .builder()
                    .status(AppResponseStatus.SUCCESS.name())
                    .message("Feedback Submitted")
                    .imageUrl(getImageUrlAndAbout().getImageUrl())
                    .statusCode(200)
                    .build();

            return ResponseEntity.ok(responseDto);

        } catch (Exception e) {
            log.error("Request failed: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Override
    public ResponseEntity<?> getAllFeedback() {
        try {
            return ResponseEntity.ok(feedbackRepository.findAll()
            );

        } catch (Exception e) {
            log.error("Request failed: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("SupportUtils.errorDtoSupportService(e)");
        }
    }

    @Override
    public ResponseEntity<?> getFeedback(String name) {
        try {

            Optional<FeedbackEntity> feedback = feedbackRepository.findByName(name);

            FeedbackResponseDto responseDto = FeedbackResponseDto
                    .builder()
                    .status(AppResponseStatus.SUCCESS.name())
                    .message("Feedback" + feedback.get().getFeedback() + "\nUser Id: " + feedback.get().getUserId() + "\nName: " + feedback.get().getName() + "\nEmail: " + feedback.get().getEmail())
                    .imageUrl(getImageUrlAndAbout().getImageUrl())
                    .statusCode(200)
                    .build();

            return ResponseEntity.ok(responseDto);

        } catch (Exception e) {
            log.error("Request failed: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("SupportUtils.errorDtoSupportService(e)");
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> deleteFeedback(Long id) {
        try {

            feedbackRepository.deleteById(id);

            FeedbackResponseDto responseDto = FeedbackResponseDto
                    .builder()
                    .status(AppResponseStatus.SUCCESS.name())
                    .message("Feedback Deleted")
                    .imageUrl(getImageUrlAndAbout().getImageUrl())
                    .statusCode(200)
                    .build();

            return ResponseEntity.ok(responseDto);

        } catch (Exception e) {
            log.error("Request failed: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("SupportUtils.errorDtoSupportService(e)");
        }
    }

    private String currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getName() == null || "anonymousUser".equals(authentication.getName())) {
            log.warn("Unauthorized access attempt without valid authentication principal in SecurityContext");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return authentication.getName();
    }

    private AppMediaAssetEntity getImageUrlAndAbout() {
//        try {
//            return imageAndAboutRepository.findByNameIgnoreCase("feedback").orElse(AppMediaAssetEntity.builder().imageUrl("Not Found").build());
//        } catch (Exception e) {
//            return new AppMediaAssetEntity(0L, "Error Getting Name", "Error Getting Image", "Error Getting About");
//        }
        return null;

    }

}

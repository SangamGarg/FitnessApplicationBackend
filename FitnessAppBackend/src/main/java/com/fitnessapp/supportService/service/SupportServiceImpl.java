package com.fitnessapp.supportService.service;

import lombok.extern.slf4j.Slf4j;
import com.fitnessapp.portal.dev.appInfoDev.models.entity.AppInfoEntity;
import com.fitnessapp.portal.dev.appInfoDev.AppInfoRepository;
import com.fitnessapp.portal.dev.imagesUploadDev.models.AppMediaAssetEntity;
import com.fitnessapp.portal.dev.imagesUploadDev.AppMediaAssetRepository;
import com.fitnessapp.supportService.models.dtos.FeedbackRequestDto;
import com.fitnessapp.supportService.models.dtos.FeedbackResponseDto;
import com.fitnessapp.supportService.models.entities.FeedbackEntity;
import com.fitnessapp.supportService.repository.FeedbackRepository;
import com.fitnessapp.supportService.supportServiceUtilities.AppConstantSupportService;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class SupportServiceImpl implements SupportService {
    private final FeedbackRepository feedbackRepository;
    private final AppInfoRepository appInfoRepository;
    private final AppMediaAssetRepository imageAndAboutRepository;

    public SupportServiceImpl(FeedbackRepository feedbackRepository, AppInfoRepository appInfoRepository, AppMediaAssetRepository imageAndAboutRepository) {
        this.feedbackRepository = feedbackRepository;
        this.appInfoRepository = appInfoRepository;
        this.imageAndAboutRepository = imageAndAboutRepository;
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
            return ResponseEntity.internalServerError().body(AppConstantSupportService.errorDtoSupportService(e));
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> postFeedback(FeedbackRequestDto feedbackRequestDto) {
        try {

            feedbackRepository.save(FeedbackEntity.builder()
                    .feedback(feedbackRequestDto.getFeedback())
                    .email(feedbackRequestDto.getEmail())
                    .userId(feedbackRequestDto.getUserId())
                    .name(feedbackRequestDto.getName())
                    .build());

            FeedbackResponseDto responseDto = FeedbackResponseDto
                    .builder()
                    .status(AppConstantSupportService.SUCCESS_API_STATUS)
                    .message("Feedback Submitted")
                    .imageUrl(getImageUrlAndAbout().getImageUrl())
                    .statusCode(200)
                    .build();

            return ResponseEntity.ok(responseDto);

        } catch (Exception e) {
            log.error("Request failed: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(AppConstantSupportService.errorDtoSupportService(e));
        }
    }

    @Override
    public ResponseEntity<?> getAllFeedback() {
        try {
            return ResponseEntity.ok(feedbackRepository.findAll()
            );

        } catch (Exception e) {
            log.error("Request failed: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(AppConstantSupportService.errorDtoSupportService(e));
        }
    }

    @Override
    public ResponseEntity<?> getFeedback(String name) {
        try {

            FeedbackEntity feedback = feedbackRepository.findByName(name);

            FeedbackResponseDto responseDto = FeedbackResponseDto
                    .builder()
                    .status(AppConstantSupportService.SUCCESS_API_STATUS)
                    .message("Feedback" + feedback.getFeedback() + "\nUser Id: " + feedback.getUserId() + "\nName: " + feedback.getName() + "\nEmail: " + feedback.getEmail())
                    .imageUrl(getImageUrlAndAbout().getImageUrl())
                    .statusCode(200)
                    .build();

            return ResponseEntity.ok(responseDto);

        } catch (Exception e) {
            log.error("Request failed: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(AppConstantSupportService.errorDtoSupportService(e));
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> deleteFeedback(Long id) {
        try {

            feedbackRepository.deleteById(id);

            FeedbackResponseDto responseDto = FeedbackResponseDto
                    .builder()
                    .status(AppConstantSupportService.SUCCESS_API_STATUS)
                    .message("Feedback Deleted")
                    .imageUrl(getImageUrlAndAbout().getImageUrl())
                    .statusCode(200)
                    .build();

            return ResponseEntity.ok(responseDto);

        } catch (Exception e) {
            log.error("Request failed: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(AppConstantSupportService.errorDtoSupportService(e));
        }
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

package com.fitnessapp.supportService.service;

import com.fitnessapp.DeveloperAndTestingService.appInfoDevService.models.entity.AppInfoEntity;
import com.fitnessapp.DeveloperAndTestingService.appInfoDevService.repository.AppInfoRepository;
import com.fitnessapp.imageAndAboutService.models.ImageAndAboutEntity;
import com.fitnessapp.imageAndAboutService.repository.ImageAndAboutRepository;
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
public class SupportServiceImpl implements SupportService {
    private final FeedbackRepository feedbackRepository;
    private final AppInfoRepository appInfoRepository;
    private final ImageAndAboutRepository imageAndAboutRepository;

    public SupportServiceImpl(FeedbackRepository feedbackRepository, AppInfoRepository appInfoRepository, ImageAndAboutRepository imageAndAboutRepository) {
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
            return ResponseEntity.internalServerError().body(AppConstantSupportService.errorDtoSupportService(e));
        }
    }

    @Override
    public ResponseEntity<?> getAllFeedback() {
        try {
            return ResponseEntity.ok(feedbackRepository.findAll()
            );

        } catch (Exception e) {
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
            return ResponseEntity.internalServerError().body(AppConstantSupportService.errorDtoSupportService(e));
        }
    }


    private ImageAndAboutEntity getImageUrlAndAbout() {
        try {
            return imageAndAboutRepository.findByNameIgnoreCase("feedback").orElse(ImageAndAboutEntity.builder().imageUrl("Not Found").build());
        } catch (Exception e) {
            return new ImageAndAboutEntity(0L, "Error Getting Name", "Error Getting Image", "Error Getting About");
        }
    }

}

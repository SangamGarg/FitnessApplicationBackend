package com.fitnessapp.feedback;


import com.fitnessapp.portal.dev.assetUploadDev.model.AppMediaAssetEntity;
import com.fitnessapp.utils.appUtil.AppResponseStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeedbackServiceImpl implements FeedbackService {
    private final FeedbackRepository feedbackRepository;

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

    private AppMediaAssetEntity getImageUrlAndAbout() {
//        try {
//            return imageAndAboutRepository.findByNameIgnoreCase().orElse(AppMediaAssetEntity.builder().imageUrl("Not Found").build());
//        } catch (Exception e) {
//            return new AppMediaAssetEntity(0L, "Error Getting Name", "Error Getting Image", "Error Getting About");
//        }
        return null;

    }
}

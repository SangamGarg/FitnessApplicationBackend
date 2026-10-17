package com.fitnessapp.imageAndAboutService.service;

import lombok.extern.slf4j.Slf4j;
import com.fitnessapp.imageAndAboutService.models.ImageAndAboutEntity;
import com.fitnessapp.imageAndAboutService.models.UploadImageAndAboutRequestDto;
import com.fitnessapp.imageAndAboutService.repository.ImageAndAboutRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ImageAndAboutServiceImpl implements ImageAndAboutService {

    private final ImageAndAboutRepository imageAndAboutRepository;

    public ImageAndAboutServiceImpl(ImageAndAboutRepository imageAndAboutRepository) {
        this.imageAndAboutRepository = imageAndAboutRepository;
    }

    @Transactional
    @Override
    public ResponseEntity<?> uploadCalculatorImageAndAbout(UploadImageAndAboutRequestDto dto) {

        if (dto.getAbout() == null || dto.getAbout().isBlank()) {
            return ResponseEntity.badRequest().body("About is Null or Blank");
        }
        if (dto.getImageUrl() == null || dto.getImageUrl().isBlank()) {
            return ResponseEntity.badRequest().body("Image Url is Null or Blank");
        }
        try {
            imageAndAboutRepository.save(ImageAndAboutEntity.
                    builder()
                    .name(dto.getName())
                    .imageUrl(dto.getImageUrl())
                    .about(dto.getAbout())
                    .build());
            return ResponseEntity.ok("Uploaded Successfully");
        } catch (Exception e) {
            log.error("Request failed: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(e.getLocalizedMessage());
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> patchCalculatorImageAndAbout(UploadImageAndAboutRequestDto dto) {
        try {
            var existing = imageAndAboutRepository.findByNameIgnoreCase(dto.getName());
            if (existing.isEmpty()) {
                return ResponseEntity.badRequest().body("Calculator with the given name not found");
            }

            ImageAndAboutEntity entity = existing.get();

            if (dto.getImageUrl() != null && !dto.getImageUrl().isBlank()) {
                entity.setImageUrl(dto.getImageUrl());
            }

            if (dto.getAbout() != null && !dto.getAbout().isBlank()) {
                entity.setAbout(dto.getAbout());
            }

            imageAndAboutRepository.save(entity);
            return ResponseEntity.ok("Updated Successfully");

        } catch (Exception e) {
            log.error("Request failed: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(e.getLocalizedMessage());
        }
    }

}

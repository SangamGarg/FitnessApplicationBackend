package com.fitnessapp.imageAndAboutService.service;

import com.fitnessapp.imageAndAboutService.models.UploadImageAndAboutRequestDto;
import org.springframework.http.ResponseEntity;

public interface ImageAndAboutService {
    ResponseEntity<?> uploadCalculatorImageAndAbout(UploadImageAndAboutRequestDto uploadCalculatorImageAndAboutRequestDto);

    ResponseEntity<?> patchCalculatorImageAndAbout(UploadImageAndAboutRequestDto uploadCalculatorImageAndAboutRequestDto);
}

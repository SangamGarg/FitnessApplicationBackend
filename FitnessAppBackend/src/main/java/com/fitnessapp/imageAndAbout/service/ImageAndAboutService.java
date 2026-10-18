package com.fitnessapp.imageAndAbout.service;

import com.fitnessapp.imageAndAbout.models.UploadImageAndAboutRequestDto;
import org.springframework.http.ResponseEntity;

public interface ImageAndAboutService {
    ResponseEntity<?> uploadCalculatorImageAndAbout(UploadImageAndAboutRequestDto uploadCalculatorImageAndAboutRequestDto);

    ResponseEntity<?> patchCalculatorImageAndAbout(UploadImageAndAboutRequestDto uploadCalculatorImageAndAboutRequestDto);
}

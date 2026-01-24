package com.fitnessapp.imageAndAboutService.controller;

import com.fitnessapp.DeveloperAndTestingService.devAndTestingServiceUtilities.DevAndTestingServiceUtility;
import com.fitnessapp.imageAndAboutService.ImageAndAboutUtilities;
import com.fitnessapp.imageAndAboutService.models.UploadImageAndAboutRequestDto;
import com.fitnessapp.imageAndAboutService.service.ImageAndAboutService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ImageAndAboutUtilities.API_PREFIX_IMAGE_AND_ABOUT)
public class ImageAndAboutController {


    private final ImageAndAboutService imageAndAboutService;

    public ImageAndAboutController(ImageAndAboutService imageAndAboutService) {
        this.imageAndAboutService = imageAndAboutService;
    }


    @PostMapping("/upload-image-about")
    public ResponseEntity<?> uploadImageAndAbout(@RequestBody @Valid UploadImageAndAboutRequestDto requestDto) {
        return imageAndAboutService.uploadCalculatorImageAndAbout(requestDto);
    }

    @PatchMapping("/patch-image-about")
    public ResponseEntity<?> patchImageAndAbout(@RequestBody @Valid UploadImageAndAboutRequestDto requestDto) {
        return imageAndAboutService.patchCalculatorImageAndAbout(requestDto);
    }


}

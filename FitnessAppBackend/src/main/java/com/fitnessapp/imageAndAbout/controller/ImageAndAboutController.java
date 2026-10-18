package com.fitnessapp.imageAndAbout.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import com.fitnessapp.imageAndAbout.ImageAndAboutUtilities;
import com.fitnessapp.imageAndAbout.models.UploadImageAndAboutRequestDto;
import com.fitnessapp.imageAndAbout.service.ImageAndAboutService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ImageAndAboutUtilities.API_PREFIX_IMAGE_AND_ABOUT)
@Tag(name = "Image And About", description = "Endpoints for image and about")
public class ImageAndAboutController {


    private final ImageAndAboutService imageAndAboutService;

    public ImageAndAboutController(ImageAndAboutService imageAndAboutService) {
        this.imageAndAboutService = imageAndAboutService;
    }


    @Operation(summary = "upload Image And About", description = """
        Executes the upload image and about operation.
        """)
    @PostMapping("/upload-image-about")
    public ResponseEntity<?> uploadImageAndAbout(@RequestBody @Valid UploadImageAndAboutRequestDto requestDto) {
        return imageAndAboutService.uploadCalculatorImageAndAbout(requestDto);
    }

    @Operation(summary = "patch Image And About", description = """
        Executes the patch image and about operation.
        """)
    @PatchMapping("/patch-image-about")
    public ResponseEntity<?> patchImageAndAbout(@RequestBody @Valid UploadImageAndAboutRequestDto requestDto) {
        return imageAndAboutService.patchCalculatorImageAndAbout(requestDto);
    }


}

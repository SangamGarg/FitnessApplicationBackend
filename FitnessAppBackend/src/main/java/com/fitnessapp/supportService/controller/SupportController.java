package com.fitnessapp.supportService.controller;

import com.fitnessapp.supportService.models.dtos.FeedbackRequestDto;
import com.fitnessapp.supportService.service.SupportService;
import com.fitnessapp.supportService.supportServiceUtilities.AppConstantSupportService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping(AppConstantSupportService.API_PREFIX)
public class SupportController {

    private final SupportService supportService;

    public SupportController(SupportService supportService) {
        this.supportService = supportService;
    }

    @GetMapping("/app-info")
    public ResponseEntity<?> getAppInfo() {
        return supportService.getAppInfo();
    }


    @PostMapping("feedback")
    public ResponseEntity<?> postFeedback(@RequestBody FeedbackRequestDto feedbackRequestDto) {
        return supportService.postFeedback(feedbackRequestDto);
    }
}

package com.fitnessapp.support.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import com.fitnessapp.support.models.dtos.FeedbackRequestDto;
import com.fitnessapp.support.service.SupportService;
import com.fitnessapp.support.utils.SupportUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping(SupportUtils.API_PREFIX)
@Tag(name = "Support", description = "Endpoints for support")
public class SupportController {

    private final SupportService supportService;

    public SupportController(SupportService supportService) {
        this.supportService = supportService;
    }

    @Operation(summary = "get App Info", description = """
        Executes the get app info operation.
        """)
    @GetMapping("/app-info")
    public ResponseEntity<?> getAppInfo() {
        return supportService.getAppInfo();
    }


    @Operation(summary = "post Feedback", description = """
        Executes the post feedback operation.
        """)
    @PostMapping("feedback")
    public ResponseEntity<?> postFeedback(@RequestBody FeedbackRequestDto feedbackRequestDto) {
        return supportService.postFeedback(feedbackRequestDto);
    }
}

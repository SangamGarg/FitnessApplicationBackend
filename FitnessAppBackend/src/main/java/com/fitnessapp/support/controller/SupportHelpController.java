package com.fitnessapp.support.controller;

import com.fitnessapp.support.models.dtos.*;
import com.fitnessapp.support.models.dtos.request.FeedbackRequestDto;
import com.fitnessapp.support.models.dtos.response.FeedbackResponseDto;
import com.fitnessapp.support.models.dtos.response.SupportHomeResponseDto;
import com.fitnessapp.support.service.SupportHelpService;
import com.fitnessapp.support.service.SupportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.fitnessapp.utils.appUtil.AppUtilEndpoint.APP_SUPPORT_ENDPOINT;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(APP_SUPPORT_ENDPOINT)
@Validated
public class SupportHelpController {

    private final SupportHelpService supportHelpService;
    private final SupportService supportService;

    @GetMapping("/app-info")
    public ResponseEntity<?> getAppInfo() {
        log.debug("Fetching app information");

        ResponseEntity<?> response = supportService.getAppInfo();

        log.info("App information request completed with status={}", response.getStatusCode());
        return response;
    }

    @PostMapping("/feedback")
    public ResponseEntity<FeedbackResponseDto> postFeedback(
            @RequestBody @Valid FeedbackRequestDto feedbackRequestDto) {

        log.info("Feedback submission request received");

        ResponseEntity<FeedbackResponseDto> response = supportService.postFeedback(feedbackRequestDto);

        log.info("Feedback submission completed with status={}", response.getStatusCode());
        return response;
    }

    @GetMapping("/home")
    public ResponseEntity<SupportHomeResponseDto> getHome() {

        log.info("Support home requested");

        return ResponseEntity.ok(supportHelpService.getHome());

    }

    @GetMapping("/faq/categories")
    public ResponseEntity<List<SupportFaqCategoryDto>> categories() {
        log.debug("Fetching FAQ categories");

        return ResponseEntity.ok(supportHelpService.getCategories());
    }

    @GetMapping("/faq/popular")
    public ResponseEntity<List<SupportFaqDto>> popular(
            @RequestParam(defaultValue = "6") int limit) {

        log.info("Fetching popular FAQs; requestedLimit={}", limit);

        return ResponseEntity.ok(
                supportHelpService.getPopularFaqs(limit)
        );
    }

    @GetMapping("/faq")
    public ResponseEntity<Page<SupportFaqDto>> search(
            @RequestParam(required = false) String categoryKey,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("Searching FAQs; categoryKey={}, page={}, size={}, queryPresent={}", categoryKey, page, size, query != null && !query.isBlank());

        return ResponseEntity.ok(
                supportHelpService.searchFaqs(
                        categoryKey, query, page, size
                )
        );
    }

    @GetMapping("/faq/{faqId}")
    public ResponseEntity<SupportFaqDto> faq(@PathVariable Long faqId) {
        log.info("Fetching FAQ details; faqId={}", faqId);

        return ResponseEntity.ok(supportHelpService.getFaq(faqId));

    }

    @GetMapping("/tickets/summary")
    public ResponseEntity<SupportTicketSummaryDto> summary() {

        log.info("Fetching support ticket summary");

        return ResponseEntity.ok(
                supportHelpService.getTicketSummary()
        );
    }
}
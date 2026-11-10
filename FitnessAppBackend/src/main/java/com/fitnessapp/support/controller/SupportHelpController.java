package com.fitnessapp.support.controller;

import com.fitnessapp.support.models.dtos.*;
import com.fitnessapp.support.models.dtos.response.SupportHomeResponseDto;
import com.fitnessapp.support.models.dtos.response.AppInfoContentResponseDto;
import com.fitnessapp.support.service.SupportHelpService;
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

    @GetMapping("/about")
    public ResponseEntity<AppInfoContentResponseDto> getAboutUs() {
        log.debug("Fetching About Us content");
        return ResponseEntity.ok(supportHelpService.getAboutUs());
    }

    @GetMapping("/privacy-policy")
    public ResponseEntity<AppInfoContentResponseDto> getPrivacyPolicy() {
        log.debug("Fetching Privacy Policy content");
        return ResponseEntity.ok(supportHelpService.getPrivacyPolicy());
    }

    @GetMapping("/terms-condition")
    public ResponseEntity<AppInfoContentResponseDto> getTermsAndConditions() {
        log.debug("Fetching Terms and Conditions content");
        return ResponseEntity.ok(supportHelpService.getTermsAndConditions());
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
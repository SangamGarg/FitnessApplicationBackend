package com.fitnessapp.support.controller;

import com.fitnessapp.support.models.dtos.*;
import com.fitnessapp.support.service.SupportHelpService;
import com.fitnessapp.support.utils.SupportUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(SupportUtils.API_PREFIX)
@Tag(name = "Support Help Center", description = "FAQ and support-home endpoints")
public class SupportHelpController {
    private final SupportHelpService supportHelpService;

    @Operation(summary = "Get support screen bootstrap data", description = "Returns FAQ categories, popular FAQs and ticket counts in one response.")
    @GetMapping("/home")
    public ResponseEntity<SupportHomeResponseDto> getHome(@RequestAttribute(value = "userId", required = false) Long userId) {
        log.info("Support home requested; authenticatedUserPresent={}", userId != null);
        // Ticket summary is user-specific. If userId isn't supplied by the auth filter, fail clearly.
        if (userId == null)
            throw new org.springframework.security.authentication.AuthenticationCredentialsNotFoundException("Authenticated userId request attribute is required");
        return ResponseEntity.ok(supportHelpService.getHome(userId));
    }

    @Operation(summary = "Get FAQ categories")
    @GetMapping("/faq/categories")
    public ResponseEntity<List<SupportFaqCategoryDto>> categories() {
        return ResponseEntity.ok(supportHelpService.getCategories());
    }

    @Operation(summary = "Get popular FAQs")
    @GetMapping("/faq/popular")
    public ResponseEntity<List<SupportFaqDto>> popular(@RequestParam(defaultValue = "6") int limit) {
        return ResponseEntity.ok(supportHelpService.getPopularFaqs(limit));
    }

    @Operation(summary = "Search and paginate FAQs")
    @GetMapping("/faq")
    public ResponseEntity<Page<SupportFaqDto>> search(@RequestParam(required = false) String categoryKey, @RequestParam(required = false) String query, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(supportHelpService.searchFaqs(categoryKey, query, page, size));
    }

    @Operation(summary = "Get FAQ detail")
    @GetMapping("/faq/{faqId}")
    public ResponseEntity<SupportFaqDto> faq(@PathVariable Long faqId) {
        return ResponseEntity.ok(supportHelpService.getFaq(faqId));
    }

    @Operation(summary = "Get current user's support ticket summary")
    @GetMapping("/tickets/summary")
    public ResponseEntity<SupportTicketSummaryDto> summary(@RequestAttribute("userId") Long userId) {
        return ResponseEntity.ok(supportHelpService.getTicketSummary(userId));
    }
}

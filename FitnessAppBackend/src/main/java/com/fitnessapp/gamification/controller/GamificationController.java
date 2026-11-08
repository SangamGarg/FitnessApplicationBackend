package com.fitnessapp.gamification.controller;

import com.fitnessapp.gamification.dto.GamificationSummaryResponseDto;
import com.fitnessapp.gamification.dto.RewardHistoryResponseDto;
import com.fitnessapp.gamification.service.GamificationRewardService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.fitnessapp.utils.appUtil.AppUtilEndpoint.APP_GAMIFICATION_ENDPOINT;

@RestController
@RequestMapping(APP_GAMIFICATION_ENDPOINT)
@RequiredArgsConstructor
public class GamificationController {
    private final GamificationRewardService service;

    @GetMapping("/summary")
    @Operation(summary = "Get XP, level, wallet coins and activity streak")
    public ResponseEntity<GamificationSummaryResponseDto> summary() {
        return ResponseEntity.ok(service.getSummary());
    }

    @GetMapping("/rewards/history")
    @Operation(summary = "Get paginated reward history")
    public ResponseEntity<Page<RewardHistoryResponseDto>> history(
                                                                  @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), 100), Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(service.getHistory(pageable));
    }
}

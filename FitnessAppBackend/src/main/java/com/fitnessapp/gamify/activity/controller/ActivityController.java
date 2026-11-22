package com.fitnessapp.gamify.activity.controller;

import com.fitnessapp.gamify.activity.models.dtos.requestDto.CompleteActivityRequestDto;
import com.fitnessapp.gamify.activity.models.dtos.requestDto.HealthActivitySyncRequestDto;
import com.fitnessapp.gamify.activity.models.dtos.requestDto.StartActivityRequestDto;
import com.fitnessapp.gamify.activity.models.dtos.responseDto.ActivityRecordResponseDto;
import com.fitnessapp.gamify.activity.models.dtos.responseDto.ActivitySummaryResponseDto;
import com.fitnessapp.gamify.activity.models.dtos.responseDto.HealthActivitySyncResponseDto;
import com.fitnessapp.gamify.activity.service.ActivityInterface;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

import static com.fitnessapp.utils.appUtil.AppUtilEndpoint.APP_ACTIVITY_ENDPOINT;

@RestController
@RequestMapping(APP_ACTIVITY_ENDPOINT)
@RequiredArgsConstructor
@Validated
public class ActivityController {

    private final ActivityInterface activityService;

    @PostMapping("/start")
    public ResponseEntity<ActivityRecordResponseDto> startActivity(
            @Valid @RequestBody StartActivityRequestDto request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(activityService.startActivity(request));
    }

    @PostMapping("/{activityId}/complete")
    public ResponseEntity<ActivityRecordResponseDto> completeActivity(
            @PathVariable UUID activityId,
            @Valid @RequestBody CompleteActivityRequestDto request
    ) {
        return ResponseEntity.ok(
                activityService.completeActivity(activityId, request)
        );
    }

    @PostMapping("/health-sync")
    public ResponseEntity<HealthActivitySyncResponseDto> syncHealthActivities(
            @Valid @RequestBody HealthActivitySyncRequestDto request
    ) {
        return ResponseEntity.ok(
                activityService.syncHealthActivities(request)
        );
    }

    @GetMapping("/summary")
    public ResponseEntity<ActivitySummaryResponseDto> getDailySummary(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "UTC") String timeZone
    ) {
        ZoneId zoneId = parseZone(timeZone);
        LocalDate requestedDate = date == null ? LocalDate.now(zoneId) : date;

        return ResponseEntity.ok(
                activityService.getDailySummary(

                        requestedDate,
                        zoneId
                )
        );
    }

    @GetMapping("/history")
    public ResponseEntity<Page<ActivityRecordResponseDto>> getHistory(
            @PageableDefault(size = 20, sort = "startedAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                activityService.getHistory(pageable)
        );
    }

    private ZoneId parseZone(String timeZone) {
        try {
            return ZoneId.of(timeZone);
        } catch (DateTimeException exception) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid timeZone. Use an IANA zone such as Asia/Kolkata."
            );
        }
    }
}

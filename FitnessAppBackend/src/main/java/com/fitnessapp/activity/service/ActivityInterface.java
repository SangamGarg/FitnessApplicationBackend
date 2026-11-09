package com.fitnessapp.activity.service;

import com.fitnessapp.activity.models.dtos.requestDto.CompleteActivityRequestDto;
import com.fitnessapp.activity.models.dtos.requestDto.HealthActivitySyncRequestDto;
import com.fitnessapp.activity.models.dtos.requestDto.StartActivityRequestDto;
import com.fitnessapp.activity.models.dtos.responseDto.ActivityRecordResponseDto;
import com.fitnessapp.activity.models.dtos.responseDto.ActivitySummaryResponseDto;
import com.fitnessapp.activity.models.dtos.responseDto.HealthActivitySyncResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

public interface ActivityInterface {

    ActivityRecordResponseDto startActivity(StartActivityRequestDto request);

    ActivityRecordResponseDto completeActivity(

            UUID activityId,
            CompleteActivityRequestDto request
    );

    HealthActivitySyncResponseDto syncHealthActivities(

            HealthActivitySyncRequestDto request
    );

    ActivitySummaryResponseDto getDailySummary(
            LocalDate date,
            ZoneId zoneId
    );

    Page<ActivityRecordResponseDto> getHistory(Pageable pageable);
}

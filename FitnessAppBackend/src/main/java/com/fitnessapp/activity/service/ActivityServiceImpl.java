package com.fitnessapp.activity.service;

import com.fitnessapp.activity.models.dtos.requestDto.CompleteActivityRequestDto;
import com.fitnessapp.activity.models.dtos.requestDto.HealthActivityRecordRequestDto;
import com.fitnessapp.activity.models.dtos.requestDto.HealthActivitySyncRequestDto;
import com.fitnessapp.activity.models.dtos.requestDto.StartActivityRequestDto;
import com.fitnessapp.activity.models.dtos.responseDto.ActivityRecordResponseDto;
import com.fitnessapp.activity.models.dtos.responseDto.ActivitySummaryResponseDto;
import com.fitnessapp.activity.models.dtos.responseDto.HealthActivitySyncResponseDto;
import com.fitnessapp.activity.models.entities.ActivityRecordEntity;
import com.fitnessapp.activity.models.enums.ActivitySource;
import com.fitnessapp.activity.models.enums.ActivityStatus;
import com.fitnessapp.activity.repository.ActivityDailyAggregate;
import com.fitnessapp.activity.repository.ActivityRepository;
import com.fitnessapp.appAuth.model.entity.UserEntity;
import com.fitnessapp.appAuth.repo.UserRepo;
import com.fitnessapp.gamification.achievement.AchievementService;
import com.fitnessapp.gamification.challenge.ChallengeMetric;
import com.fitnessapp.gamification.challenge.ChallengeService;
import com.fitnessapp.gamification.service.GamificationRewardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityServiceImpl implements ActivityInterface {

    private static final Duration MAX_ACTIVITY_DURATION = Duration.ofHours(24);
    private static final Duration DUPLICATE_START_TOLERANCE = Duration.ofMinutes(5);
    private static final double DUPLICATE_OVERLAP_THRESHOLD = 0.80;

    private final ActivityRepository activityRepository;
    private final UserRepo userRepo;
    private final GamificationRewardService gamificationRewardService;
    private final ChallengeService challengeService;
    private final AchievementService achievementService;

    @Value("${activity.daily-active-minutes-goal:30}")
    private int dailyGoalMinutes;

    @Value("${activity.sync.ignored-origin-ids:com.muscleplay.app}")
    private List<String> ignoredOriginIds;

    @Override
    @Transactional
    public ActivityRecordResponseDto startActivity(
            StartActivityRequestDto request
    ) {
        String userId = currentUserId();

        UserEntity user = lockUser(userId);
        String sourceRecordId = request.clientActivityId().toString();

        var existing = activityRepository.findByUser_IdAndSourceAndSourceRecordId(
                userId,
                ActivitySource.APP,
                sourceRecordId
        );
        if (existing.isPresent()) {
            return ActivityRecordResponseDto.from(existing.get());
        }

        ActivityRecordEntity activity = new ActivityRecordEntity();
        activity.setUser(user);
        activity.setSource(ActivitySource.APP);
        activity.setSourceRecordId(sourceRecordId);
        activity.setDeduplicationKey(sourceRecordId);
        activity.setActivityType(request.activityType());
        activity.setActivityName(request.activityName().trim());
        activity.setStatus(ActivityStatus.IN_PROGRESS);
        activity.setStartedAt(Instant.now());
        activity.setIncludedInSummary(false);

        return ActivityRecordResponseDto.from(activityRepository.save(activity));
    }

    @Override
    @Transactional
    public ActivityRecordResponseDto completeActivity(
            UUID activityId,
            CompleteActivityRequestDto request
    ) {
        String userId = currentUserId();

        lockUser(userId);

        ActivityRecordEntity activity = activityRepository.findByIdAndUser_Id(activityId, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Activity not found"
                ));

        if (activity.getSource() != ActivitySource.APP) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only app-recorded activities can be completed through this endpoint"
            );
        }

        if (activity.getStatus() == ActivityStatus.COMPLETED) {
            return ActivityRecordResponseDto.from(activity);
        }

        Instant endedAt = request.endedAt();
        validateTimeRange(activity.getStartedAt(), endedAt);

        long durationSeconds = Duration.between(activity.getStartedAt(), endedAt).getSeconds();
        activity.setEndedAt(endedAt);
        activity.setDurationSeconds(Math.toIntExact(durationSeconds));
        activity.setCaloriesBurned(normalize(request.caloriesBurned()));
        activity.setDistanceMeters(normalize(request.distanceMeters()));
        activity.setSteps(request.steps());
        activity.setStatus(ActivityStatus.COMPLETED);
        activity.setIncludedInSummary(true);

        ActivityRecordEntity saved = activityRepository.saveAndFlush(activity);
        boolean isDuplicate = applyDuplicatePolicy(saved);
        if (!isDuplicate && saved.isIncludedInSummary()) {
            gamificationRewardService.rewardCompletedActivity(saved);
            if (saved.isIncludedInSummary()) {
                challengeService.recordProgress(userId, ChallengeMetric.ACTIVITY_COUNT, 1, saved.getActivityType().name(), saved.getId().toString());
                challengeService.recordProgress(userId, ChallengeMetric.ACTIVE_MINUTES, saved.getDurationSeconds() / 60L, saved.getActivityType().name(), saved.getId().toString());
                challengeService.recordProgress(userId, ChallengeMetric.STEPS, saved.getSteps() == null ? 0L : saved.getSteps(), saved.getActivityType().name(), saved.getId().toString());
                challengeService.recordProgress(userId, ChallengeMetric.CALORIES, saved.getCaloriesBurned() == null ? 0L : saved.getCaloriesBurned().longValue(), saved.getActivityType().name(), saved.getId().toString());
                achievementService.evaluate(userId);
            }
        }

        return ActivityRecordResponseDto.from(saved);
    }

    @Override
    @Transactional
    public HealthActivitySyncResponseDto syncHealthActivities(
            HealthActivitySyncRequestDto request
    ) {
        String userId = currentUserId();
        UserEntity user = lockUser(userId);

        if (request.source() != ActivitySource.HEALTH_CONNECT
                && request.source() != ActivitySource.APPLE_HEALTH) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Health sync source must be HEALTH_CONNECT or APPLE_HEALTH"
            );
        }

        int imported = 0;
        int updated = 0;
        int duplicates = 0;
        int ignoredOwnApp = 0;

        for (HealthActivityRecordRequestDto record : request.records()) {
            if (isIgnoredOrigin(record.originAppId())) {
                ignoredOwnApp++;
                continue;
            }

            validateTimeRange(record.startedAt(), record.endedAt());

            var existingSourceRecord = activityRepository.findByUser_IdAndSourceAndSourceRecordId(
                    userId,
                    request.source(),
                    record.sourceRecordId()
            );

            if (existingSourceRecord.isPresent()) {
                ActivityRecordEntity existing = existingSourceRecord.get();
                applyHealthRecord(existing, record);
                existing.setIncludedInSummary(true);
                existing.setDuplicateOf(null);
                ActivityRecordEntity refreshed = activityRepository.saveAndFlush(existing);
                applyDuplicatePolicy(refreshed);
                updated++;
                continue;
            }

            if (StringUtils.hasText(record.deduplicationKey())) {
                var sameLogicalActivity = activityRepository.findByUser_IdAndDeduplicationKey(
                        userId,
                        record.deduplicationKey().trim()
                );
                if (sameLogicalActivity.isPresent()) {
                    duplicates++;
                    continue;
                }
            }

            ActivityRecordEntity activity = new ActivityRecordEntity();
            activity.setUser(user);
            activity.setSource(request.source());
            activity.setSourceRecordId(record.sourceRecordId().trim());
            activity.setDeduplicationKey(normalize(record.deduplicationKey()));
            activity.setOriginAppId(normalize(record.originAppId()));
            activity.setStatus(ActivityStatus.COMPLETED);
            applyHealthRecord(activity, record);
            activity.setIncludedInSummary(true);

            ActivityRecordEntity saved = activityRepository.saveAndFlush(activity);
            boolean markedDuplicate = applyDuplicatePolicy(saved);

            imported++;
            if (markedDuplicate) {
                duplicates++;
            }
        }

        return new HealthActivitySyncResponseDto(
                request.records().size(),
                imported,
                updated,
                duplicates,
                ignoredOwnApp
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ActivitySummaryResponseDto getDailySummary(LocalDate date,
                                                      ZoneId zoneId
    ) {
        String userId = currentUserId();
        if (!userRepo.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User account not found");
        }

        Instant start = date.atStartOfDay(zoneId).toInstant();
        Instant end = date.plusDays(1).atStartOfDay(zoneId).toInstant();

        ActivityDailyAggregate aggregate = activityRepository.aggregateForPeriod(
                userId,
                ActivityStatus.COMPLETED,
                start,
                end
        );

        long activeSeconds = aggregate == null || aggregate.getActiveSeconds() == null
                ? 0L : aggregate.getActiveSeconds();
        BigDecimal calories = aggregate == null || aggregate.getCaloriesBurned() == null
                ? BigDecimal.ZERO : aggregate.getCaloriesBurned();
        BigDecimal distance = aggregate == null || aggregate.getDistanceMeters() == null
                ? BigDecimal.ZERO : aggregate.getDistanceMeters();
        long steps = aggregate == null || aggregate.getSteps() == null
                ? 0L : aggregate.getSteps();
        long completedActivities = aggregate == null || aggregate.getCompletedActivities() == null
                ? 0L : aggregate.getCompletedActivities();

        int activeMinutes = (int) (activeSeconds / 60L);
        int goal = Math.max(dailyGoalMinutes, 1);
        int progress = (int) Math.min(
                100L,
                Math.round((activeSeconds * 100.0) / (goal * 60.0))
        );

        return new ActivitySummaryResponseDto(
                date,
                zoneId.getId(),
                activeMinutes,
                calories.setScale(2, RoundingMode.HALF_UP),
                distance.setScale(2, RoundingMode.HALF_UP),
                steps,
                completedActivities,
                goal,
                progress
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ActivityRecordResponseDto> getHistory(Pageable pageable) {
        String userId = currentUserId();

        if (!userRepo.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User account not found");
        }
        return activityRepository
                .findAllByUser_IdAndStatusOrderByStartedAtDesc(
                        userId,
                        ActivityStatus.COMPLETED,
                        pageable
                )
                .map(ActivityRecordResponseDto::from);
    }

    /**
     * Returns true when the newly saved record is a duplicate and therefore
     * must not contribute to summary totals.
     */
    private boolean applyDuplicatePolicy(ActivityRecordEntity incoming) {
        String userId = currentUserId();

        Instant from = incoming.getStartedAt().minus(DUPLICATE_START_TOLERANCE);
        Instant to = incoming.getStartedAt().plus(DUPLICATE_START_TOLERANCE);

        List<ActivityRecordEntity> candidates =
                activityRepository
                        .findAllByUser_IdAndActivityTypeAndStatusAndIncludedInSummaryTrueAndStartedAtBetween(
                                userId,
                                incoming.getActivityType(),
                                ActivityStatus.COMPLETED,
                                from,
                                to
                        );

        ActivityRecordEntity duplicateCandidate = candidates.stream()
                .filter(candidate -> !candidate.getId().equals(incoming.getId()))
                .filter(candidate -> isLikelySameActivity(candidate, incoming))
                // Prefer an existing APP record as the canonical record.
                .sorted((left, right) -> {
                    int leftPriority = left.getSource() == ActivitySource.APP ? 0 : 1;
                    int rightPriority = right.getSource() == ActivitySource.APP ? 0 : 1;
                    int priorityCompare = Integer.compare(leftPriority, rightPriority);
                    return priorityCompare != 0
                            ? priorityCompare
                            : left.getStartedAt().compareTo(right.getStartedAt());
                })
                .findFirst()
                .orElse(null);

        if (duplicateCandidate == null) {
            incoming.setIncludedInSummary(true);
            activityRepository.save(incoming);
            return false;
        }

        boolean incomingIsPreferred = incoming.getSource() == ActivitySource.APP
                && duplicateCandidate.getSource() != ActivitySource.APP;

        if (incomingIsPreferred) {
            duplicateCandidate.setIncludedInSummary(false);
            duplicateCandidate.setDuplicateOf(incoming);
            activityRepository.save(duplicateCandidate);
            incoming.setIncludedInSummary(true);
            incoming.setDuplicateOf(null);
            activityRepository.save(incoming);
            return false;
        }

        incoming.setIncludedInSummary(false);
        incoming.setDuplicateOf(duplicateCandidate);
        activityRepository.save(incoming);
        return true;
    }

    private boolean isLikelySameActivity(
            ActivityRecordEntity first,
            ActivityRecordEntity second
    ) {
        if (first.getEndedAt() == null || second.getEndedAt() == null) {
            return false;
        }

        Instant overlapStart = first.getStartedAt().isAfter(second.getStartedAt())
                ? first.getStartedAt() : second.getStartedAt();
        Instant overlapEnd = first.getEndedAt().isBefore(second.getEndedAt())
                ? first.getEndedAt() : second.getEndedAt();

        long overlapSeconds = Math.max(0L, Duration.between(overlapStart, overlapEnd).getSeconds());
        long shorterDuration = Math.min(
                Duration.between(first.getStartedAt(), first.getEndedAt()).getSeconds(),
                Duration.between(second.getStartedAt(), second.getEndedAt()).getSeconds()
        );

        return shorterDuration > 0
                && ((double) overlapSeconds / shorterDuration) >= DUPLICATE_OVERLAP_THRESHOLD;
    }

    private void applyHealthRecord(
            ActivityRecordEntity entity,
            HealthActivityRecordRequestDto request
    ) {
        entity.setActivityType(request.activityType());
        entity.setActivityName(request.activityName().trim());
        entity.setStartedAt(request.startedAt());
        entity.setEndedAt(request.endedAt());
        entity.setDurationSeconds(Math.toIntExact(
                Duration.between(request.startedAt(), request.endedAt()).getSeconds()
        ));
        entity.setCaloriesBurned(normalize(request.caloriesBurned()));
        entity.setDistanceMeters(normalize(request.distanceMeters()));
        entity.setSteps(request.steps());
        entity.setStatus(ActivityStatus.COMPLETED);
    }

    private void validateTimeRange(Instant startedAt, Instant endedAt) {
        if (startedAt == null || endedAt == null || !endedAt.isAfter(startedAt)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Activity end time must be after its start time"
            );
        }

        Duration duration = Duration.between(startedAt, endedAt);
        if (duration.compareTo(MAX_ACTIVITY_DURATION) > 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Activity duration cannot exceed 24 hours"
            );
        }

        if (endedAt.isAfter(Instant.now().plus(Duration.ofMinutes(5)))) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Activity end time cannot be in the future"
            );
        }
    }

    private boolean isIgnoredOrigin(String originAppId) {
        if (!StringUtils.hasText(originAppId) || ignoredOriginIds == null) {
            return false;
        }
        String origin = originAppId.trim().toLowerCase(Locale.ROOT);
        return ignoredOriginIds.stream()
                .filter(StringUtils::hasText)
                .map(value -> value.trim().toLowerCase(Locale.ROOT))
                .anyMatch(origin::equals);
    }

    private UserEntity lockUser(String userId) {
        if (!StringUtils.hasText(userId)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return userRepo.findByIdForUpdate(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User account not found"
                ));
    }

    private String currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getName() == null || "anonymousUser".equals(authentication.getName())) {
            log.warn("Unauthorized access attempt without valid authentication principal in SecurityContext");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return authentication.getName();
    }

    private BigDecimal normalize(BigDecimal value) {
        return value == null ? null : value.max(BigDecimal.ZERO);
    }

    private String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}

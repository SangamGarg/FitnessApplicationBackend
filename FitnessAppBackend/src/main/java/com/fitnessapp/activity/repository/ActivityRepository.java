package com.fitnessapp.activity.repository;

import com.fitnessapp.activity.models.entities.ActivityRecordEntity;
import com.fitnessapp.activity.models.enums.ActivitySource;
import com.fitnessapp.activity.models.enums.ActivityStatus;
import com.fitnessapp.activity.models.enums.ActivityType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ActivityRepository extends JpaRepository<ActivityRecordEntity, UUID> {

    long countByUser_IdAndIncludedInSummaryTrue(String userId);

    Optional<ActivityRecordEntity> findByIdAndUser_Id(UUID id, String userId);

    Optional<ActivityRecordEntity> findByUser_IdAndSourceAndSourceRecordId(
            String userId,
            ActivitySource source,
            String sourceRecordId
    );

    Optional<ActivityRecordEntity> findByUser_IdAndDeduplicationKey(
            String userId,
            String deduplicationKey
    );

    List<ActivityRecordEntity>
    findAllByUser_IdAndActivityTypeAndStatusAndIncludedInSummaryTrueAndStartedAtBetween(
            String userId,
            ActivityType activityType,
            ActivityStatus status,
            Instant from,
            Instant to
    );

    List<ActivityRecordEntity> findAllByUser_IdAndStatusAndIncludedInSummaryTrueAndStartedAtBetween(
            String userId, ActivityStatus status, Instant from, Instant to
    );

    Page<ActivityRecordEntity> findAllByUser_IdAndStatusOrderByStartedAtDesc(
            String userId,
            ActivityStatus status,
            Pageable pageable
    );

    @Query("""
            select
                sum(a.durationSeconds) as activeSeconds,
                sum(a.caloriesBurned) as caloriesBurned,
                sum(a.distanceMeters) as distanceMeters,
                sum(a.steps) as steps,
                count(a.id) as completedActivities
            from ActivityRecordEntity a
            where a.user.id = :userId
              and a.status = :status
              and a.includedInSummary = true
              and a.startedAt >= :start
              and a.startedAt < :end
            """)
    ActivityDailyAggregate aggregateForPeriod(
            @Param("userId") String userId,
            @Param("status") ActivityStatus status,
            @Param("start") Instant start,
            @Param("end") Instant end
    );
}

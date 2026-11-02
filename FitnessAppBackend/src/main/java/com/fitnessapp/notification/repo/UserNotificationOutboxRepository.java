package com.fitnessapp.notification.repo;

import com.fitnessapp.notification.model.entity.UserNotificationOutboxEntity;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface UserNotificationOutboxRepository extends JpaRepository<UserNotificationOutboxEntity, UUID> {

    /**
     * PostgreSQL row locking lets multiple application instances claim separate work safely.
     * A stale PROCESSING row is reclaimed after a worker crash/restart.
     */
    @Query(value = """
            SELECT *
            FROM app_notification_outbox
            WHERE (
                (status IN ('PENDING', 'RETRY_WAIT') AND next_attempt_at <= :now)
                OR (status = 'PROCESSING' AND locked_at < :staleBefore)
            )
            ORDER BY next_attempt_at ASC, created_at ASC
            LIMIT :batchSize
            FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    List<UserNotificationOutboxEntity> lockDueBatch(
            @Param("now") Instant now,
            @Param("staleBefore") Instant staleBefore,
            @Param("batchSize") int batchSize);
}

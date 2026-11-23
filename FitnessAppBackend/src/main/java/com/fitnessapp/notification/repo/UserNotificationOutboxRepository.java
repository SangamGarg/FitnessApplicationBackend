package com.fitnessapp.notification.repo;

import com.fitnessapp.notification.model.entity.UserNotificationOutboxEntity;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import com.fitnessapp.notification.model.enums.NotificationOutboxStatus;

@Repository
public interface UserNotificationOutboxRepository extends JpaRepository<UserNotificationOutboxEntity, UUID> {

    @Query("select o from UserNotificationOutboxEntity o join o.notification n " +
            "where (:status is null or o.status = :status) order by o.createdAt desc")
    Page<UserNotificationOutboxEntity> findHistory(@Param("status") NotificationOutboxStatus status,
                                                     Pageable pageable);

    @Query("select o.status, count(o) from UserNotificationOutboxEntity o group by o.status")
    List<Object[]> countByStatus();

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

package com.fitnessapp.notification.repo;

import com.fitnessapp.notification.model.entity.UserNotificationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.UUID;

public interface UserNotificationRepository extends JpaRepository<UserNotificationEntity, UUID> {

    Page<UserNotificationEntity> findAllByUser_IdOrderByCreatedAtDesc(String userId, Pageable pageable);

    Page<UserNotificationEntity> findAllByUser_IdAndReadAtIsNullOrderByCreatedAtDesc(
            String userId, Pageable pageable);

    long countByUser_IdAndReadAtIsNull(String userId);

    boolean existsByIdAndUser_Id(UUID id, String userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update UserNotificationEntity n set n.readAt = :readAt " +
            "where n.id = :notificationId and n.user.id = :userId and n.readAt is null")
    int markAsRead(@Param("notificationId") UUID notificationId,
                   @Param("userId") String userId,
                   @Param("readAt") Instant readAt);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update UserNotificationEntity n set n.readAt = :readAt " +
            "where n.user.id = :userId and n.readAt is null")
    int markAllAsRead(@Param("userId") String userId, @Param("readAt") Instant readAt);
}

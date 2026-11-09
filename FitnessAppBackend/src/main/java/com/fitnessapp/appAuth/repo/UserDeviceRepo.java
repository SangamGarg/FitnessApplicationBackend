package com.fitnessapp.appAuth.repo;

import com.fitnessapp.appAuth.model.entity.UserDeviceEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserDeviceRepo extends JpaRepository<UserDeviceEntity, Long> {

    List<UserDeviceEntity> findAllByUser_IdOrderByLastActiveAtDesc(String userId);

    Optional<UserDeviceEntity> findByDeviceIdAndUser_Id(String deviceId, String userId);

    List<UserDeviceEntity> findAllByUser_IdInAndNotificationEnabledTrue(Collection<String> userIds);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from UserDeviceEntity d where d.deviceId = :deviceId")
    Optional<UserDeviceEntity> findByDeviceIdForUpdate(@Param("deviceId") String deviceId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from UserDeviceEntity d where d.fcmToken = :fcmToken")
    Optional<UserDeviceEntity> findByFcmTokenForUpdate(@Param("fcmToken") String fcmToken);

    long deleteByDeviceIdAndUser_Id(String deviceId, String userId);

    /** Delete only if the row still contains the exact token that FCM rejected. */
    @Modifying
    @Transactional
    long deleteByIdAndFcmToken(Long id, String fcmToken);
}

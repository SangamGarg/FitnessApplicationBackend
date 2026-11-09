package com.fitnessapp.appAuth.repo;

import com.fitnessapp.appAuth.model.entity.RefreshTokenEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepo extends JpaRepository<RefreshTokenEntity, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from RefreshTokenEntity t where t.tokenHash = :hash")
    Optional<RefreshTokenEntity> findByTokenHashForUpdate(@Param("hash") String hash);

    List<RefreshTokenEntity> findByFamilyId(String familyId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from RefreshTokenEntity t where t.familyId = :familyId")
    List<RefreshTokenEntity> findByFamilyIdForUpdate(@Param("familyId") String familyId);

    List<RefreshTokenEntity> findByUserIdAndRevokedAtIsNull(String userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from RefreshTokenEntity t where t.userId = :userId and t.revokedAt is null")
    List<RefreshTokenEntity> findActiveByUserIdForUpdate(@Param("userId") String userId);
    void deleteByUserId(String userId);
}

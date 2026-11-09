package com.fitnessapp.portal.common.auth;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface PortalRefreshTokenRepository extends JpaRepository<PortalRefreshTokenEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM PortalRefreshTokenEntity t WHERE t.tokenHash = :hash AND t.revokedAt IS NULL")
    Optional<PortalRefreshTokenEntity> findActiveByTokenHashForUpdate(@Param("hash") String tokenHash);

    List<PortalRefreshTokenEntity> findAllByAccountIdAndRevokedAtIsNull(Long accountId);

    @Modifying
    @Transactional
    @Query("DELETE FROM PortalRefreshTokenEntity t WHERE t.expiresAt < :instant")
    long deleteByExpiresAtBefore(@Param("instant") Instant instant);
}
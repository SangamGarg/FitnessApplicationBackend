package com.fitnessapp.wallet;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WalletAccountRepository extends JpaRepository<WalletAccountEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select w from WalletAccountEntity w where w.user.id = :userId")
    Optional<WalletAccountEntity> findByUserIdForUpdate(@Param("userId") String userId);

    Optional<WalletAccountEntity> findByUserId(String userId);
}

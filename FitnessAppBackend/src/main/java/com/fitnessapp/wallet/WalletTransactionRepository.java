package com.fitnessapp.wallet;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransactionEntity, Long> {

    Optional<WalletTransactionEntity> findByWalletUserIdAndIdempotencyKey(String userId, String idempotencyKey);

    Page<WalletTransactionEntity> findAllByWalletUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);
}

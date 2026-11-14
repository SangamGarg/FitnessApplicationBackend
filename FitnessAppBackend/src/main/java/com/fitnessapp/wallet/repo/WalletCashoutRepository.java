package com.fitnessapp.wallet.repo;

import com.fitnessapp.wallet.model.WalletCashoutStatus;
import com.fitnessapp.wallet.model.entity.WalletCashoutEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface WalletCashoutRepository extends JpaRepository<WalletCashoutEntity, Long> {
    List<WalletCashoutEntity> findByUserIdOrderByCreatedAtDesc(String userId);

    Optional<WalletCashoutEntity> findByTransactionId(String transactionId);

    @Query("SELECT SUM(c.amountCoins) FROM WalletCashoutEntity c WHERE c.user.id = :userId AND c.createdAt >= :since AND c.status <> :excludedStatus")
    Long sumAmountCoinsByUserIdAndCreatedAtAfterAndStatusNot(
            @Param("userId") String userId,
            @Param("since") Instant since,
            @Param("excludedStatus") WalletCashoutStatus excludedStatus
    );

    boolean existsByUserIdAndStatus(String userId, WalletCashoutStatus status);
}

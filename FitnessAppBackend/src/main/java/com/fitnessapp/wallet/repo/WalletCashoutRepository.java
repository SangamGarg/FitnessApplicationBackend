package com.fitnessapp.wallet.repo;

import com.fitnessapp.wallet.model.WalletCashoutStatus;
import com.fitnessapp.wallet.model.entity.WalletCashoutEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Query("""
            SELECT c FROM WalletCashoutEntity c WHERE
            (:userId = '' OR c.user.id = :userId) AND
            (:status IS NULL OR c.status = :status) AND
            (:type IS NULL OR c.type = :type) AND
            (:transactionId = '' OR LOWER(c.transactionId) LIKE LOWER(CONCAT('%', :transactionId, '%'))) AND
            (CAST(:createdFrom AS timestamp) IS NULL OR c.createdAt >= :createdFrom) AND
            (CAST(:createdTo AS timestamp) IS NULL OR c.createdAt <= :createdTo)
            """)
    Page<WalletCashoutEntity> searchCashouts(
            @Param("userId") String userId,
            @Param("status") WalletCashoutStatus status,
            @Param("type") com.fitnessapp.wallet.model.WalletCashoutType type,
            @Param("transactionId") String transactionId,
            @Param("createdFrom") Instant createdFrom,
            @Param("createdTo") Instant createdTo,
            Pageable pageable
    );
}
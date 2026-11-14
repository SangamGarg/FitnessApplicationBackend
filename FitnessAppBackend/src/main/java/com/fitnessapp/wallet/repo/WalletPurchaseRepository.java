package com.fitnessapp.wallet.repo;

import com.fitnessapp.wallet.model.entity.WalletPurchaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WalletPurchaseRepository extends JpaRepository<WalletPurchaseEntity, Long> {
    Optional<WalletPurchaseEntity> findByTransactionId(String transactionId);
}

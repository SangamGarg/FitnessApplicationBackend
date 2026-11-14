package com.fitnessapp.wallet.repo;

import com.fitnessapp.wallet.model.entity.WalletReferralHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WalletReferralHistoryRepository extends JpaRepository<WalletReferralHistoryEntity, Long> {
    List<WalletReferralHistoryEntity> findAllByReferrerIdOrderByCreatedAtDesc(String referrerId);
    java.util.Optional<WalletReferralHistoryEntity> findByReferredUserId(String referredUserId);
}

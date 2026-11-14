package com.fitnessapp.wallet.repo;

import com.fitnessapp.wallet.model.entity.WalletReferralEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WalletReferralRepository extends JpaRepository<WalletReferralEntity, Long> {
    Optional<WalletReferralEntity> findByUserId(String userId);
    Optional<WalletReferralEntity> findByReferralCode(String referralCode);
    boolean existsByReferralCode(String referralCode);
}

package com.fitnessapp.wallet.repo;

import com.fitnessapp.wallet.model.entity.WalletRulesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WalletRulesRepository extends JpaRepository<WalletRulesEntity, Long> {
    List<WalletRulesEntity> findAllByActiveTrueOrderByDisplayOrderAsc();
}

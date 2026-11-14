package com.fitnessapp.wallet.repo;

import com.fitnessapp.wallet.model.entity.WalletPackageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WalletPackageRepository extends JpaRepository<WalletPackageEntity, Long> {
    List<WalletPackageEntity> findAllByActiveTrueOrderByDisplayOrderAsc();
}

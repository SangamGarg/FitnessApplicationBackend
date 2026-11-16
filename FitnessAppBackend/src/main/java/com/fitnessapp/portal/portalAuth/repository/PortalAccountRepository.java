package com.fitnessapp.portal.portalAuth.repository;

import com.fitnessapp.portal.portalAuth.model.entity.PortalAccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PortalAccountRepository extends JpaRepository<PortalAccountEntity, Long>,
        JpaSpecificationExecutor<PortalAccountEntity> {

    Optional<PortalAccountEntity> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    Optional<PortalAccountEntity> findBySetupTokenHash(String setupTokenHash);

    List<PortalAccountEntity> findAllByRoleIdOrderByCreatedAtDesc(Integer roleId);
}
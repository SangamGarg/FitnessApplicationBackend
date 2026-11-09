package com.fitnessapp.support.portalSupport.repo;

import com.fitnessapp.support.portalSupport.models.entity.AppInfoPrivacyPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppInfoPrivacyPolicyRepository
        extends JpaRepository<AppInfoPrivacyPolicy, Long> {
}
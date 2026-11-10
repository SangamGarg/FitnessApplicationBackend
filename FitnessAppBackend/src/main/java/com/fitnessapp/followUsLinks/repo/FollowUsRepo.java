package com.fitnessapp.followUsLinks.repo;

import com.fitnessapp.followUsLinks.model.FollowUsLinksEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FollowUsRepo extends JpaRepository<FollowUsLinksEntity, Long> {

    List<FollowUsLinksEntity> findAllByOrderByIdAsc();

    boolean existsByTitleKey(String titleKey);

    boolean existsByNormalizedUrl(String normalizedUrl);

    boolean existsByTitleKeyAndIdNot(String titleKey, Long id);

    boolean existsByNormalizedUrlAndIdNot(String normalizedUrl, Long id);
}
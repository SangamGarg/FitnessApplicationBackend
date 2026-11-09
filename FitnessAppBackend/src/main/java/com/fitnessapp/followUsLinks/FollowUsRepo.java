package com.fitnessapp.followUsLinks;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FollowUsRepo extends JpaRepository<FollowUsLinksEntity, Long> {
}

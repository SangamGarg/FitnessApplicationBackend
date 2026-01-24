package com.fitnessapp.supportService.repository;

import com.fitnessapp.supportService.models.entities.FeedbackEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedbackRepository extends JpaRepository<FeedbackEntity, Long> {

    FeedbackEntity findByName(String name);
}

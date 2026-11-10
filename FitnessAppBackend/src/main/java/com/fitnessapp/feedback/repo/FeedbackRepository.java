package com.fitnessapp.feedback.repo;

import com.fitnessapp.feedback.model.FeedbackEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedbackRepository
        extends JpaRepository<FeedbackEntity, Long> {

    Page<FeedbackEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<FeedbackEntity> findByUserIdOrderByCreatedAtDesc(
            String userId,
            Pageable pageable
    );
}
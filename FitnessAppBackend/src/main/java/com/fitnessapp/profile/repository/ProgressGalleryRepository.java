package com.fitnessapp.profile.repository;

import com.fitnessapp.profile.model.entity.ProgressGalleryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProgressGalleryRepository extends JpaRepository<ProgressGalleryEntity, Long> {
    Page<ProgressGalleryEntity> findByUserIdOrderByUploadedAtDesc(String userId, Pageable pageable);
    Optional<ProgressGalleryEntity> findByIdAndUserId(Long id, String userId);
}

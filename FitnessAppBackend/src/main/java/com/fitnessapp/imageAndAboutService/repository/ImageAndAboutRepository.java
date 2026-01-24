package com.fitnessapp.imageAndAboutService.repository;

import com.fitnessapp.imageAndAboutService.models.ImageAndAboutEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ImageAndAboutRepository extends JpaRepository<ImageAndAboutEntity, Long> {

    Optional<ImageAndAboutEntity> findByNameIgnoreCase(String name);

}
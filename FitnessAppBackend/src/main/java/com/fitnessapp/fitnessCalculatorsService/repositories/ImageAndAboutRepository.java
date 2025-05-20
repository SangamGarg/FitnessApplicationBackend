package com.fitnessapp.fitnessCalculatorsService.repositories;

import com.fitnessapp.fitnessCalculatorsService.models.entities.ImageAndAboutEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ImageAndAboutRepository extends JpaRepository<ImageAndAboutEntity, Long> {

    Optional<ImageAndAboutEntity> findByNameIgnoreCase(String name);

}
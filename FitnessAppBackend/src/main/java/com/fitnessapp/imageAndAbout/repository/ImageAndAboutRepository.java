package com.fitnessapp.imageAndAbout.repository;

import com.fitnessapp.imageAndAbout.models.ImageAndAboutEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ImageAndAboutRepository extends JpaRepository<ImageAndAboutEntity, Long> {

    Optional<ImageAndAboutEntity> findByNameIgnoreCase(String name);

}
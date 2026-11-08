package com.fitnessapp.nutrition.repository;
import com.fitnessapp.nutrition.model.entity.UserDietPreferenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;
public interface UserDietPreferenceRepository extends JpaRepository<UserDietPreferenceEntity, UUID> {
    Optional<UserDietPreferenceEntity> findByUser_Id(String userId);
}

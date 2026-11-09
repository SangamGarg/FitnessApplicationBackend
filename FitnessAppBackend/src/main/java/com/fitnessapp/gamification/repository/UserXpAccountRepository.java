package com.fitnessapp.gamification.repository;
import com.fitnessapp.gamification.entity.UserXpAccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface UserXpAccountRepository extends JpaRepository<UserXpAccountEntity,Long>{
 Optional<UserXpAccountEntity> findByUserId(String userId);
}

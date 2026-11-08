package com.fitnessapp.gamification.repository;
import com.fitnessapp.gamification.entity.RewardLedgerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
public interface RewardLedgerRepository extends JpaRepository<RewardLedgerEntity,Long>{
 Page<RewardLedgerEntity> findAllByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);
 Optional<RewardLedgerEntity> findByUserIdAndEventKey(String userId,String eventKey);
 @Query("select coalesce(sum(r.xpAwarded),0) from RewardLedgerEntity r where r.user.id=:userId and r.rewardDate=:day")
 long sumXpForDay(@Param("userId") String userId,@Param("day") LocalDate day);
 @Query("select coalesce(sum(r.coinsAwarded),0) from RewardLedgerEntity r where r.user.id=:userId and r.rewardDate=:day")
 long sumCoinsForDay(@Param("userId") String userId,@Param("day") LocalDate day);
}

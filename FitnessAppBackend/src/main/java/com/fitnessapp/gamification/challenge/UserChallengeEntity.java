package com.fitnessapp.gamification.challenge;

import com.fitnessapp.appAuth.model.entity.UserEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Entity @Table(name="app_user_challenges", uniqueConstraints=@UniqueConstraint(name="uk_user_challenge",columnNames={"user_id","challenge_id"}), indexes=@Index(name="idx_user_challenge_status",columnList="user_id,status"))
@Getter @NoArgsConstructor
public class UserChallengeEntity {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="user_id",nullable=false) private UserEntity user;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="challenge_id",nullable=false) private ChallengeEntity challenge;
 @Column(name="progress_value",nullable=false) private long progressValue;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private ChallengeStatus status=ChallengeStatus.ACTIVE;
 @Column(name="joined_at",nullable=false) private Instant joinedAt;
 @Column(name="completed_at") private Instant completedAt;
 public UserChallengeEntity(UserEntity user,ChallengeEntity challenge){this.user=user;this.challenge=challenge;this.joinedAt=Instant.now();}
 public boolean addProgress(long amount){ if(status!=ChallengeStatus.ACTIVE || amount<=0)return false; progressValue=Math.min(challenge.getTargetValue(),Math.addExact(progressValue,amount)); if(progressValue>=challenge.getTargetValue()){status=ChallengeStatus.COMPLETED;completedAt=Instant.now();return true;} return false;}
}

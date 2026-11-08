package com.fitnessapp.gamification.entity;

import com.fitnessapp.appAuth.model.entity.UserEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "app_reward_ledger",
 uniqueConstraints = @UniqueConstraint(name="uk_reward_user_event", columnNames={"user_id","event_key"}),
 indexes = @Index(name="idx_reward_user_day", columnList="user_id,reward_date"))
@Getter @NoArgsConstructor
public class RewardLedgerEntity {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="user_id",nullable=false) private UserEntity user;
 @Column(name="event_key",nullable=false,length=160) private String eventKey;
 @Column(name="source_type",nullable=false,length=40) private String sourceType;
 @Column(name="source_id",nullable=false,length=100) private String sourceId;
 @Column(name="xp_awarded",nullable=false) private long xpAwarded;
 @Column(name="coins_awarded",nullable=false) private long coinsAwarded;
 @Column(name="reward_date",nullable=false) private LocalDate rewardDate;
 @Column(name="created_at",nullable=false,updatable=false) private Instant createdAt;
 public RewardLedgerEntity(UserEntity user,String eventKey,String sourceType,String sourceId,long xp,long coins,LocalDate date) {
  this.user=user; this.eventKey=eventKey; this.sourceType=sourceType; this.sourceId=sourceId;
  this.xpAwarded=xp; this.coinsAwarded=coins; this.rewardDate=date;
 }
 @PrePersist void onCreate(){ if(createdAt==null) createdAt=Instant.now(); }
}

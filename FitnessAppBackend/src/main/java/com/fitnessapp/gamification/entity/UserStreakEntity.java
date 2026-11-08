package com.fitnessapp.gamification.entity;
import com.fitnessapp.appAuth.model.entity.UserEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.time.LocalDate;
@Entity @Table(name="app_user_streaks")
@Getter @NoArgsConstructor
public class UserStreakEntity {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @OneToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="user_id",nullable=false,unique=true) private UserEntity user;
 @Column(name="current_streak",nullable=false) private int currentStreak;
 @Column(name="longest_streak",nullable=false) private int longestStreak;
 @Column(name="last_active_date") private LocalDate lastActiveDate;
 @Column(name="updated_at",nullable=false) private Instant updatedAt;
 public UserStreakEntity(UserEntity user){this.user=user;this.updatedAt=Instant.now();}
 public void recordActiveDay(LocalDate day){
  if(lastActiveDate==null){currentStreak=1;}
  else if(day.equals(lastActiveDate)){return;}
  else if(day.equals(lastActiveDate.plusDays(1))){currentStreak++;}
  else if(day.isAfter(lastActiveDate)){currentStreak=1;}
  else{return;}
  lastActiveDate=day; longestStreak=Math.max(longestStreak,currentStreak); updatedAt=Instant.now();
 }
}

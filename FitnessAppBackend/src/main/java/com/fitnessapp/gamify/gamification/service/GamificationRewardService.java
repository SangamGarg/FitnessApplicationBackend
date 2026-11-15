package com.fitnessapp.gamify.gamification.service;

import com.fitnessapp.appAuth.model.entity.UserEntity;
import com.fitnessapp.appAuth.repo.UserRepo;
import com.fitnessapp.gamify.activity.models.entities.ActivityRecordEntity;
import com.fitnessapp.gamify.activity.models.enums.ActivitySource;
import com.fitnessapp.gamify.activity.models.enums.ActivityStatus;
import com.fitnessapp.gamify.gamification.model.entity.RewardLedgerEntity;
import com.fitnessapp.gamify.gamification.model.entity.UserStreakEntity;
import com.fitnessapp.gamify.gamification.model.entity.UserXpAccountEntity;
import com.fitnessapp.gamify.gamification.repository.RewardLedgerRepository;
import com.fitnessapp.gamify.gamification.repository.UserStreakRepository;
import com.fitnessapp.gamify.gamification.repository.UserXpAccountRepository;
import com.fitnessapp.wallet.service.AppWalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
@Slf4j
public class GamificationRewardService {

    private final UserRepo userRepo;
    private final UserXpAccountRepository xpRepo;
    private final RewardLedgerRepository rewardLedgerRepo;
    private final UserStreakRepository streakRepo;
    private final AppWalletService appWalletService;


    @Value("${gamification.time-zone:Asia/Kolkata}")
    private String configuredTimeZone;
    @Value("${gamification.activity.minimum-duration-seconds:60}")
    private int minimumDurationSeconds;
    @Value("${gamification.activity.xp:50}")
    private long activityXp;
    @Value("${gamification.activity.coins:10}")
    private long activityCoins;
    @Value("${gamification.daily.max-xp:300}")
    private long maxDailyXp;
    @Value("${gamification.daily.max-coins:100}")
    private long maxDailyCoins;

    /**
     * Must be called in the same transaction that completes the activity.
     * User row locking serializes concurrent reward attempts for one account.
     */
    @Transactional
    public void rewardCompletedActivity(ActivityRecordEntity activity) {
        if (activity == null || activity.getId() == null || activity.getStatus() != ActivityStatus.COMPLETED
                || activity.getSource() != ActivitySource.APP || !activity.isIncludedInSummary()
                || activity.getDurationSeconds() == null || activity.getDurationSeconds() < minimumDurationSeconds)
            return;

        String userId = activity.getUser().getId();
        UserEntity user = userRepo.findByIdForUpdate(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User account not found"));
        String eventKey = "ACTIVITY_COMPLETION:" + activity.getId();
        if (rewardLedgerRepo.findByUserIdAndEventKey(userId, eventKey).isPresent()) return;

        ZoneId zone;
        try {
            zone = ZoneId.of(configuredTimeZone);
        } catch (DateTimeException ex) {
            throw new IllegalStateException("Invalid gamification.time-zone", ex);
        }
        LocalDate rewardDate = activity.getStartedAt().atZone(zone).toLocalDate();

        long xpRemaining = Math.max(0L, maxDailyXp - rewardLedgerRepo.sumXpForDay(userId, rewardDate));
        long coinsRemaining = Math.max(0L, maxDailyCoins - rewardLedgerRepo.sumCoinsForDay(userId, rewardDate));
        long xp = Math.min(Math.max(0L, activityXp), xpRemaining);
        long coins = Math.min(Math.max(0L, activityCoins), coinsRemaining);

        UserXpAccountEntity xpAccount = xpRepo.findByUserId(userId).orElseGet(() -> xpRepo.save(new UserXpAccountEntity(user)));
        if (xp > 0) xpAccount.addXp(xp);
        xpRepo.save(xpAccount);

        if (coins > 0) {
            appWalletService.creditForBusiness(userId, coins, "Activity completion reward", activity.getId().toString(),
                    "GAMIFICATION_ACTIVITY_" + activity.getId());
        }
        rewardLedgerRepo.saveAndFlush(new RewardLedgerEntity(user, eventKey, "ACTIVITY", activity.getId().toString(), xp, coins, rewardDate));

        UserStreakEntity streak = streakRepo.findByUserId(userId).orElseGet(() -> streakRepo.save(new UserStreakEntity(user)));
        streak.recordActiveDay(rewardDate);
        streakRepo.save(streak);
    }

    /** Generic idempotent reward entry point for games, challenges, and achievements. */
    @Transactional
    public void awardExternalReward(String userId, String eventKey, String sourceType, String sourceId, long requestedXp, long requestedCoins, String reason) {
        UserEntity user = userRepo.findByIdForUpdate(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User account not found"));
        if (rewardLedgerRepo.findByUserIdAndEventKey(userId, eventKey).isPresent()) return;
        ZoneId zone = ZoneId.of(configuredTimeZone);
        LocalDate day = LocalDate.now(zone);
        long xp = Math.min(Math.max(0, requestedXp), Math.max(0, maxDailyXp - rewardLedgerRepo.sumXpForDay(userId, day)));
        long coins = Math.min(Math.max(0, requestedCoins), Math.max(0, maxDailyCoins - rewardLedgerRepo.sumCoinsForDay(userId, day)));
        UserXpAccountEntity account = xpRepo.findByUserId(userId).orElseGet(() -> xpRepo.save(new UserXpAccountEntity(user)));
        if (xp > 0) account.addXp(xp);
        xpRepo.save(account);
        if (coins > 0) appWalletService.creditForBusiness(userId, coins, reason, sourceId, "GAMIFICATION_" + eventKey);
        rewardLedgerRepo.saveAndFlush(new RewardLedgerEntity(user, eventKey, sourceType, sourceId, xp, coins, day));
    }

//    private String currentUserId() {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        if (authentication == null || !authentication.isAuthenticated()
//                || authentication.getName() == null || "anonymousUser".equals(authentication.getName())) {
//            log.warn("Unauthorized access attempt without valid authentication principal in SecurityContext");
//            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
//        }
//        return authentication.getName();
//    }
}
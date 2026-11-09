package com.fitnessapp.gamification.service;

import com.fitnessapp.activity.models.entities.ActivityRecordEntity;
import com.fitnessapp.activity.models.enums.ActivitySource;
import com.fitnessapp.activity.models.enums.ActivityStatus;
import com.fitnessapp.appAuth.model.entity.UserEntity;
import com.fitnessapp.appAuth.repo.UserRepo;
import com.fitnessapp.gamification.dto.GamificationSummaryResponseDto;
import com.fitnessapp.gamification.dto.RewardHistoryResponseDto;
import com.fitnessapp.gamification.entity.*;
import com.fitnessapp.gamification.repository.*;
import com.fitnessapp.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class GamificationRewardService {
    private final UserRepo userRepo;
    private final UserXpAccountRepository xpRepo;
    private final RewardLedgerRepository ledgerRepo;
    private final UserStreakRepository streakRepo;
    private final WalletService walletService;
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
        if (ledgerRepo.findByUserIdAndEventKey(userId, eventKey).isPresent()) return;

        ZoneId zone;
        try {
            zone = ZoneId.of(configuredTimeZone);
        } catch (DateTimeException ex) {
            throw new IllegalStateException("Invalid gamification.time-zone", ex);
        }
        LocalDate rewardDate = activity.getStartedAt().atZone(zone).toLocalDate();

        long xpRemaining = Math.max(0L, maxDailyXp - ledgerRepo.sumXpForDay(userId, rewardDate));
        long coinsRemaining = Math.max(0L, maxDailyCoins - ledgerRepo.sumCoinsForDay(userId, rewardDate));
        long xp = Math.min(Math.max(0L, activityXp), xpRemaining);
        long coins = Math.min(Math.max(0L, activityCoins), coinsRemaining);

        UserXpAccountEntity xpAccount = xpRepo.findByUserId(userId).orElseGet(() -> xpRepo.save(new UserXpAccountEntity(user)));
        if (xp > 0) xpAccount.addXp(xp);
        xpRepo.save(xpAccount);

        if (coins > 0) {
            walletService.creditForBusiness(userId, coins, "Activity completion reward", activity.getId().toString(),
                    "GAMIFICATION_ACTIVITY_" + activity.getId());
        }
        ledgerRepo.saveAndFlush(new RewardLedgerEntity(user, eventKey, "ACTIVITY", activity.getId().toString(), xp, coins, rewardDate));

        UserStreakEntity streak = streakRepo.findByUserId(userId).orElseGet(() -> streakRepo.save(new UserStreakEntity(user)));
        streak.recordActiveDay(rewardDate);
        streakRepo.save(streak);
    }

    /** Generic idempotent reward entry point for games, challenges and achievements. */
    @Transactional
    public void awardExternalReward(String userId, String eventKey, String sourceType, String sourceId, long requestedXp, long requestedCoins, String reason) {
        UserEntity user = userRepo.findByIdForUpdate(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User account not found"));
        if (ledgerRepo.findByUserIdAndEventKey(userId, eventKey).isPresent()) return;
        ZoneId zone = ZoneId.of(configuredTimeZone);
        LocalDate day = LocalDate.now(zone);
        long xp = Math.min(Math.max(0, requestedXp), Math.max(0, maxDailyXp - ledgerRepo.sumXpForDay(userId, day)));
        long coins = Math.min(Math.max(0, requestedCoins), Math.max(0, maxDailyCoins - ledgerRepo.sumCoinsForDay(userId, day)));
        UserXpAccountEntity account = xpRepo.findByUserId(userId).orElseGet(() -> xpRepo.save(new UserXpAccountEntity(user)));
        if (xp > 0) account.addXp(xp);
        xpRepo.save(account);
        if (coins > 0) walletService.creditForBusiness(userId, coins, reason, sourceId, "GAMIFICATION_" + eventKey);
        ledgerRepo.saveAndFlush(new RewardLedgerEntity(user, eventKey, sourceType, sourceId, xp, coins, day));
    }

    @Transactional(readOnly = true)
    public GamificationSummaryResponseDto getSummary() {
        String userId = currentUserId();

        UserEntity user = userRepo.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User account not found"));
        long xp = xpRepo.findByUserId(userId).map(UserXpAccountEntity::getTotalXp).orElse(0L);
        int level = (int) Math.floor(Math.sqrt(xp / 100.0)) + 1;
        long next = (long) level * level * 100;
        long coins = walletService.getBalance(userId).balanceCoins();
        UserStreakEntity streak = streakRepo.findByUserId(userId).orElse(null);
        return new GamificationSummaryResponseDto(xp, level, coins,
                streak == null ? 0 : streak.getCurrentStreak(), streak == null ? 0 : streak.getLongestStreak(),
                streak == null ? null : streak.getLastActiveDate(), Math.max(0, next - xp));
    }

    @Transactional(readOnly = true)
    public Page<RewardHistoryResponseDto> getHistory(Pageable pageable) {
        String userId = currentUserId();

        if (!userRepo.existsById(userId))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User account not found");
        return ledgerRepo.findAllByUserIdOrderByCreatedAtDesc(userId, pageable).map(r ->
                new RewardHistoryResponseDto(r.getSourceType(), r.getSourceId(), r.getXpAwarded(), r.getCoinsAwarded(), r.getRewardDate(), r.getCreatedAt()));
    }

    private String currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getName() == null || "anonymousUser".equals(authentication.getName())) {
            log.warn("Unauthorized access attempt without valid authentication principal in SecurityContext");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return authentication.getName();
    }
}

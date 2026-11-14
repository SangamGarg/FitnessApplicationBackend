package com.fitnessapp.gamification.challenge;

import com.fitnessapp.appAuth.repo.UserRepo;
import com.fitnessapp.gamification.service.GamificationRewardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ChallengeService {
    private final ChallengeRepository challenges;
    private final UserChallengeRepository memberships;
    private final UserRepo users;
    private final GamificationRewardService rewards;
    private final com.fitnessapp.wallet.service.AppWalletService walletService;

    @Transactional(readOnly = true)
    public List<ChallengeResponseDto> available(String userId) {
        Instant now = Instant.now();
        return challenges.findAllByActiveTrueAndStartsAtLessThanEqualAndEndsAtAfterOrderByEndsAtAsc(now, now).stream().map(c -> {
            var m = memberships.findByUserIdAndChallengeId(userId, c.getId()).orElse(null);
            return dto(c, m);
        }).toList();
    }

    @Transactional
    public ChallengeResponseDto join(String userId, Long id) {
        var user = users.findByIdForUpdate(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        var c = challenges.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Challenge not found"));
        if (!c.isJoinable(Instant.now()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Challenge is not currently joinable");
        var m = memberships.findByUserIdAndChallengeId(userId, id).orElseGet(() -> memberships.save(new UserChallengeEntity(user, c)));
        return dto(c, m);
    }

    @Transactional(readOnly = true)
    public List<ChallengeResponseDto> mine(String userId) {
        return memberships.findAllByUserIdOrderByJoinedAtDesc(userId).stream().map(m -> dto(m.getChallenge(), m)).toList();
    }

    /** Called only with server-persisted, canonical activity/game metrics. */
    @Transactional
    public void recordProgress(String userId, ChallengeMetric metric, long amount, String activityType, String sourceId) {
        if (amount <= 0) return;
        for (var m : memberships.findAllByUserIdAndStatus(userId, ChallengeStatus.ACTIVE)) {
            var c = m.getChallenge();
            if (c.getMetric() != metric || !c.isJoinable(Instant.now())) continue;
            if (c.getActivityType() != null && !c.getActivityType().equalsIgnoreCase(activityType)) continue;
            boolean completed = m.addProgress(amount);
            memberships.save(m);
            if (completed) {
                rewards.awardExternalReward(userId, "CHALLENGE_COMPLETION:" + c.getId(), "CHALLENGE", String.valueOf(c.getId()), c.getRewardXp(), c.getRewardCoins(), "Challenge completion reward");
                try {
                    walletService.processReferralReward(userId);
                } catch (Exception e) {
                    // Log and continue - we don't want to fail challenge completion if referral reward fails
                }
            }
        }
    }

    private ChallengeResponseDto dto(ChallengeEntity c, UserChallengeEntity m) {
        return new ChallengeResponseDto(c.getId(), c.getTitle(), c.getDescription(), c.getMetric(), c.getTargetValue(), m == null ? 0 : m.getProgressValue(), m == null ? ChallengeStatus.ACTIVE : (m.getStatus() == ChallengeStatus.ACTIVE && !Instant.now().isBefore(c.getEndsAt()) ? ChallengeStatus.EXPIRED : m.getStatus()), c.getStartsAt(), c.getEndsAt(), c.getRewardXp(), c.getRewardCoins(), m != null);
    }
}

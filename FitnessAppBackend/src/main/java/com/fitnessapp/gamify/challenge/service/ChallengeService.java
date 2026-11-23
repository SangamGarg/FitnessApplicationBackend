package com.fitnessapp.gamify.challenge.service;

import com.fitnessapp.appAuth.repo.UserRepo;
import com.fitnessapp.gamify.challenge.model.dto.ChallengeResponseDto;
import com.fitnessapp.gamify.challenge.model.entity.ChallengeEntity;
import com.fitnessapp.gamify.challenge.model.entity.UserChallengeEntity;
import com.fitnessapp.gamify.challenge.model.enums.ChallengeMetric;
import com.fitnessapp.gamify.challenge.model.enums.ChallengeStatus;
import com.fitnessapp.gamify.challenge.repository.ChallengeRepository;
import com.fitnessapp.gamify.challenge.repository.UserChallengeRepository;
import com.fitnessapp.gamify.gamification.service.GamificationRewardService;
import com.fitnessapp.wallet.service.AppWalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChallengeService {
    private final ChallengeRepository challengeRepository;
    private final UserChallengeRepository userChallengeRepository;
    private final UserRepo users;
    private final GamificationRewardService gamificationRewardService;
    private final AppWalletService walletService;

    @Transactional(readOnly = true)
    public List<ChallengeResponseDto> available() {
        String userId = currentUserId();
        Instant now = Instant.now();
        return challengeRepository.findAllByActiveTrueAndStartsAtLessThanEqualAndEndsAtAfterOrderByEndsAtAsc(now, now).stream().map(c -> {
            var userChallengeEntity = userChallengeRepository.findByUserIdAndChallengeId(userId, c.getId()).orElse(null);
            return dto(c, userChallengeEntity);
        }).toList();
    }

    @Transactional
    public ChallengeResponseDto join(Long id) {
        String userId = currentUserId();

        var user = users.findByIdForUpdate(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        var c = challengeRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Challenge not found"));
        if (!c.isJoinable(Instant.now()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Challenge is not currently joinable");
        var m = userChallengeRepository.findByUserIdAndChallengeId(userId, id).orElseGet(() -> userChallengeRepository.save(new UserChallengeEntity(user, c)));
        return dto(c, m);
    }

    @Transactional(readOnly = true)
    public List<ChallengeResponseDto> mine() {
        String userId = currentUserId();
        return userChallengeRepository.findAllByUserIdOrderByJoinedAtDesc(userId).stream().map(m -> dto(m.getChallenge(), m)).toList();
    }

    /** Called only with server-persisted, canonical activity/game metrics. */
    @Transactional
    public void recordProgress(String userId, ChallengeMetric metric, long amount, String activityType, String sourceId) {
        if (amount <= 0) return;
        for (var m : userChallengeRepository.findAllByUserIdAndStatus(userId, ChallengeStatus.ACTIVE)) {
            var c = m.getChallenge();
            if (c.getMetric() != metric || !c.isJoinable(Instant.now())) continue;
            if (c.getActivityType() != null && !c.getActivityType().equalsIgnoreCase(activityType)) continue;
            boolean completed = m.addProgress(amount);
            userChallengeRepository.save(m);
            if (completed) {
                gamificationRewardService.awardExternalReward(userId, "CHALLENGE_COMPLETION:" + c.getId(), "CHALLENGE", String.valueOf(c.getId()), c.getRewardXp(), c.getRewardCoins(), "Challenge completion reward");
                try {
                    walletService.processReferralReward(userId);
                } catch (Exception e) {
                    // Log and continue - we don't want to fail challenge completion if the referral reward fails
                }
            }
        }
    }

    private ChallengeResponseDto dto(ChallengeEntity c, UserChallengeEntity m) {
        return new ChallengeResponseDto(c.getId(), c.getTitle(), c.getDescription(), c.getMetric(), c.getTargetValue(), m == null ? 0 : m.getProgressValue(), m == null ? ChallengeStatus.ACTIVE : (m.getStatus() == ChallengeStatus.ACTIVE && !Instant.now().isBefore(c.getEndsAt()) ? ChallengeStatus.EXPIRED : m.getStatus()), c.getStartsAt(), c.getEndsAt(), c.getRewardXp(), c.getRewardCoins(), m != null);
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

package com.fitnessapp.gamify.challenge.service;


import com.fitnessapp.gamify.challenge.model.dto.CreateChallengeRequestDto;
import com.fitnessapp.gamify.challenge.model.dto.ChallengePatchRequestDto;
import com.fitnessapp.gamify.challenge.model.dto.ChallengeStatusRequestDto;
import com.fitnessapp.gamify.challenge.model.dto.PortalChallengeResponseDto;
import com.fitnessapp.gamify.challenge.model.entity.ChallengeEntity;
import com.fitnessapp.gamify.challenge.repository.ChallengeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class PortalChallengeServiceImpl implements PortalChallengeService {

    private final ChallengeRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<PortalChallengeResponseDto> getChallengesForPortal() {
        return repository.findAllByOrderByStartsAtDesc().stream()
                .map(this::toResponse)
                .toList();
    }


    @Override
    @Transactional
    public Map<String, String> create(CreateChallengeRequestDto req) {
        validateDateRange(req);
        var c = repository.save(new ChallengeEntity(req.title().trim(), req.description(), req.metric(), req.targetValue(), req.activityType(), req.startsAt(), req.endsAt(), req.rewardXp(), req.rewardCoins()));
        return Map.of("Id", c.getId().toString(), "Challenge", c.getTitle() + " created successfully");
    }

    @Override
    @Transactional
    public PortalChallengeResponseDto update(Long id, CreateChallengeRequestDto req) {
        validateDateRange(req);
        ChallengeEntity challenge = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Challenge not found"));
        challenge.update(req.title().trim(), req.description(), req.metric(), req.targetValue(),
                req.activityType(), req.startsAt(), req.endsAt(), req.rewardXp(), req.rewardCoins());
        return toResponse(repository.save(challenge));
    }

    @Override
    @Transactional
    public PortalChallengeResponseDto patch(Long id, ChallengePatchRequestDto req) {
        if (req == null
                || (req.title() == null
                && req.description() == null
                && req.metric() == null
                && req.targetValue() == null
                && req.activityType() == null
                && req.startsAt() == null
                && req.endsAt() == null
                && req.rewardXp() == null
                && req.rewardCoins() == null)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one challenge field must be provided");
        }

        ChallengeEntity challenge = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Challenge not found"));

        if (req.title() != null) {
            String title = req.title().trim();
            if (title.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Challenge title cannot be blank");
            }
            challenge.setTitle(title);
        }
        if (req.description() != null) {
            challenge.setDescription(req.description().trim().isEmpty() ? null : req.description().trim());
        }
        if (req.metric() != null) {
            challenge.setMetric(req.metric());
        }
        if (req.targetValue() != null) {
            challenge.setTargetValue(req.targetValue());
        }
        if (req.activityType() != null) {
            challenge.setActivityType(req.activityType().trim().isEmpty() ? null : req.activityType().trim());
        }
        if (req.startsAt() != null) {
            challenge.setStartsAt(req.startsAt());
        }
        if (req.endsAt() != null) {
            challenge.setEndsAt(req.endsAt());
        }
        if (req.rewardXp() != null) {
            challenge.setRewardXp(req.rewardXp());
        }
        if (req.rewardCoins() != null) {
            challenge.setRewardCoins(req.rewardCoins());
        }

        if (!challenge.getEndsAt().isAfter(challenge.getStartsAt())
                || Duration.between(challenge.getStartsAt(), challenge.getEndsAt()).toDays() > 366) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Challenge date range is invalid");
        }

        return toResponse(repository.save(challenge));
    }

    @Override
    @Transactional
    public PortalChallengeResponseDto updateStatus(Long id, ChallengeStatusRequestDto req) {
        if (req == null || req.active() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Challenge active status is required");
        }
        ChallengeEntity challenge = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Challenge not found"));
        challenge.setActive(req.active());
        return toResponse(repository.save(challenge));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        ChallengeEntity challenge = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Challenge not found"));
        challenge.deactivate();
        repository.save(challenge);
    }

    private void validateDateRange(CreateChallengeRequestDto req) {
        if (!req.endsAt().isAfter(req.startsAt())
                || Duration.between(req.startsAt(), req.endsAt()).toDays() > 366) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Challenge date range is invalid");
        }
    }

    private PortalChallengeResponseDto toResponse(ChallengeEntity challenge) {
        return new PortalChallengeResponseDto(challenge.getId(), challenge.getTitle(), challenge.getDescription(),
                challenge.getMetric(), challenge.getTargetValue(), challenge.getActivityType(),
                challenge.getStartsAt(), challenge.getEndsAt(), challenge.getRewardXp(),
                challenge.getRewardCoins(), challenge.isActive(), challenge.getCreatedAt());
    }
}

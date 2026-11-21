package com.fitnessapp.gamify.challenge.controller;

import com.fitnessapp.gamify.challenge.model.entity.ChallengeEntity;
import com.fitnessapp.gamify.challenge.repository.ChallengeRepository;
import com.fitnessapp.gamify.challenge.model.dto.CreateChallengeRequestDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;

import static com.fitnessapp.utils.portalUtil.PortalUtil.PORTAL_DEV_CHALLENGE_ENDPOINT;

@RestController
@RequestMapping(PORTAL_DEV_CHALLENGE_ENDPOINT)
@RequiredArgsConstructor
public class PortalChallengeController {
    private final ChallengeRepository repository;

    @PostMapping
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Long> create(@Valid @RequestBody CreateChallengeRequestDto r) {
        if (!r.endsAt().isAfter(r.startsAt()) || Duration.between(r.startsAt(), r.endsAt()).toDays() > 366)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Challenge date range is invalid");
        var c = repository.save(new ChallengeEntity(r.title().trim(), r.description(), r.metric(), r.targetValue(), r.activityType(), r.startsAt(), r.endsAt(), r.rewardXp(), r.rewardCoins()));
        return ResponseEntity.status(HttpStatus.CREATED).body(c.getId());
    }
}

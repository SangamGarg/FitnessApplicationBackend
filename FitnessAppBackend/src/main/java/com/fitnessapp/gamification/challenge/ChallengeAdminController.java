package com.fitnessapp.gamification.challenge;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/api/v1/portal/challenges")
@RequiredArgsConstructor
public class ChallengeAdminController {
    private final ChallengeRepository repository;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Long> create(@Valid @RequestBody CreateChallengeRequestDto r) {
        if (!r.endsAt().isAfter(r.startsAt()) || Duration.between(r.startsAt(), r.endsAt()).toDays() > 366)
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.BAD_REQUEST, "Challenge date range is invalid");
        var c = repository.save(new ChallengeEntity(r.title().trim(), r.description(), r.metric(), r.targetValue(), r.activityType(), r.startsAt(), r.endsAt(), r.rewardXp(), r.rewardCoins()));
        return ResponseEntity.status(HttpStatus.CREATED).body(c.getId());
    }
}

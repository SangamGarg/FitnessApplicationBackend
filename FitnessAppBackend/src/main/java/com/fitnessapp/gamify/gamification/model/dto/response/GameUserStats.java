package com.fitnessapp.gamify.gamification.model.dto.response;

/** JPQL constructor projection: count/max are boxed because max() is null for a user who never played. */
public record GameUserStats(Long timesPlayed, Long bestScore) {
}
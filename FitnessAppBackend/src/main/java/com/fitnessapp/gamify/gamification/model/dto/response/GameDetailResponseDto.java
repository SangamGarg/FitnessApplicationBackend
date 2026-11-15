package com.fitnessapp.gamify.gamification.model.dto.response;


import com.fitnessapp.gamify.gamification.model.entity.GameDetailEntity;

import java.util.List;

public record GameDetailResponseDto(
        GameCatalogueResponseDto game,   // header, level, fee, reward... so one call fills the whole screen
        String gifUrl, String videoUrl,
        List<String> rules, List<String> tips,
        long timesPlayed, Long bestScore) {

    public static GameDetailResponseDto from(GameDetailEntity d, GameUserStats stats) {
        return new GameDetailResponseDto(
                GameCatalogueResponseDto.from(d.getCatalogue()),
                d.getGifUrl(), d.getVideoUrl(),
                List.copyOf(d.getRules()), List.copyOf(d.getTips()),
                stats == null || stats.timesPlayed() == null ? 0L : stats.timesPlayed(),
                stats == null ? null : stats.bestScore());
    }
}
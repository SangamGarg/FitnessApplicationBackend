package com.fitnessapp.gamify.gamification.service;

import com.fitnessapp.gamify.gamification.model.dto.request.CreateGameRequestDto;
import com.fitnessapp.gamify.gamification.model.dto.request.GameCompleteRequestDto;
import com.fitnessapp.gamify.gamification.model.dto.request.GameStartRequestDto;
import com.fitnessapp.gamify.gamification.model.dto.response.*;
import com.fitnessapp.gamify.gamification.model.enums.GameType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface GamificationService {
    GamificationHomeSummaryResponseDto getGamificationHomeSummary();

    Page<RewardHistoryResponseDto> getHistory(Pageable pageable);

    List<GameCatalogueResponseDto> getActiveGames();

    GameDetailResponseDto gameDetails(GameType gameType);

    GameSessionResponseDto start(GameStartRequestDto r);

    GameSessionResponseDto complete(UUID sessionId, GameCompleteRequestDto r);

    Page<GameSessionResponseDto> gameHistory(Pageable p);

    GameCatalogueResponseDto createGame(CreateGameRequestDto request);
    void deactivateGame(GameType gameType);
}

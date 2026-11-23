package com.fitnessapp.gamify.challenge.service;

import com.fitnessapp.gamify.challenge.model.dto.CreateChallengeRequestDto;
import com.fitnessapp.gamify.challenge.model.dto.ChallengePatchRequestDto;
import com.fitnessapp.gamify.challenge.model.dto.ChallengeStatusRequestDto;
import com.fitnessapp.gamify.challenge.model.dto.PortalChallengeResponseDto;

import java.util.List;
import java.util.Map;

public interface PortalChallengeService {

    Map<String, String> create(CreateChallengeRequestDto requestDto);

    List<PortalChallengeResponseDto> getChallengesForPortal();

    PortalChallengeResponseDto update(Long id, CreateChallengeRequestDto requestDto);

    PortalChallengeResponseDto patch(Long id, ChallengePatchRequestDto requestDto);

    PortalChallengeResponseDto updateStatus(Long id, ChallengeStatusRequestDto requestDto);

    void delete(Long id);
}

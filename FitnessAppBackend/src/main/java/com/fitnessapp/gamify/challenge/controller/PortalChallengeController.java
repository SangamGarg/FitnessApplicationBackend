package com.fitnessapp.gamify.challenge.controller;

import com.fitnessapp.gamify.challenge.model.dto.CreateChallengeRequestDto;
import com.fitnessapp.gamify.challenge.model.dto.ChallengePatchRequestDto;
import com.fitnessapp.gamify.challenge.model.dto.ChallengeStatusRequestDto;
import com.fitnessapp.gamify.challenge.model.dto.PortalChallengeResponseDto;
import com.fitnessapp.gamify.challenge.service.PortalChallengeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.fitnessapp.utils.portalUtil.PortalUtil.PORTAL_DEV_CHALLENGE_ENDPOINT;

@RestController
@RequestMapping(PORTAL_DEV_CHALLENGE_ENDPOINT)
@RequiredArgsConstructor
@Slf4j
public class PortalChallengeController {
    private final PortalChallengeService portalChallengeService;


    @GetMapping("/get")
    public ResponseEntity<List<PortalChallengeResponseDto>> getChallengesForPortal() {
        log.info("Received request to get challenges for portal");
        return ResponseEntity.ok(portalChallengeService.getChallengesForPortal());
    }


    @PostMapping("/create")
    public ResponseEntity<Map<String, String>> create(@Valid @RequestBody CreateChallengeRequestDto requestDto) {
        log.info("Received request to create challenge: {}", requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(portalChallengeService.create(requestDto));

    }

    @PutMapping("/{id}")
    public ResponseEntity<PortalChallengeResponseDto> update(
            @PathVariable Long id,
            @Valid @RequestBody CreateChallengeRequestDto requestDto) {
        log.info("Received request to update challenge: {}", id);
        return ResponseEntity.ok(portalChallengeService.update(id, requestDto));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<PortalChallengeResponseDto> patch(
            @PathVariable Long id,
            @Valid @RequestBody ChallengePatchRequestDto requestDto) {
        log.info("Received request to patch challenge: {}", id);
        return ResponseEntity.ok(portalChallengeService.patch(id, requestDto));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<PortalChallengeResponseDto> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody ChallengeStatusRequestDto requestDto) {
        log.info("Received request to update challenge status: {}", id);
        return ResponseEntity.ok(portalChallengeService.updateStatus(id, requestDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("Received request to delete challenge: {}", id);
        portalChallengeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
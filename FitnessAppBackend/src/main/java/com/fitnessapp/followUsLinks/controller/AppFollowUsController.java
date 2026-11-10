package com.fitnessapp.followUsLinks.controller;

import com.fitnessapp.followUsLinks.model.FollowUsLinksResponseDto;
import com.fitnessapp.followUsLinks.service.FollowUsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.fitnessapp.utils.appUtil.AppUtilEndpoint.APP_FOLLOW_US_ENDPOINT;

@Slf4j
@RestController
@RequestMapping(APP_FOLLOW_US_ENDPOINT)
@RequiredArgsConstructor
public class AppFollowUsController {

    private final FollowUsService followUsService;

    @GetMapping("/links")
    public ResponseEntity<List<FollowUsLinksResponseDto>> getAppLinks() {
        log.debug("REST request to fetch App Links");
        return ResponseEntity.ok(followUsService.getAppLinks());
    }
}

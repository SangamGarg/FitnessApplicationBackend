package com.fitnessapp.followUsLinks;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.fitnessapp.utils.appUtil.AppUtilEndpoint.APP_FOLLOW_US_ENDPOINT;

@Slf4j
@RestController
@RequestMapping(APP_FOLLOW_US_ENDPOINT)
@RequiredArgsConstructor
public class FollowUsController {

    private final FollowUsService followUsService;


    @GetMapping("/links")
    public ResponseEntity<List<FollowUsLinksResponseDto>> getAppLinks() {
        log.debug("REST request to fetch App Links");
        return ResponseEntity.ok(followUsService.getAppLinks());
    }

    @PostMapping("/upload/links")
    public ResponseEntity<Map<String, String>> uploadAppLinksUrl(
            @NotEmpty(message = "Links list cannot be empty") @RequestBody List<FollowUsLinksRequestDto> requestDto
    ) {
        log.info("REST request to upload {} app links", requestDto.size());
        return ResponseEntity.status(HttpStatus.CREATED).body(followUsService.uploadAppLinksUrl(requestDto));
    }

    @PatchMapping("/patch/links")
    public ResponseEntity<Map<String, String>> patchAppLinksUrl(@Valid @RequestBody FollowUsLinksRequestDto requestDto) {
        log.info("REST request to patch app link: [{}]", requestDto.getTitle());
        return ResponseEntity.ok(followUsService.patchAppLinksUrl(requestDto));
    }
}

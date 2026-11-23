
package com.fitnessapp.portal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

import static com.fitnessapp.utils.portalUtil.PortalUtil.PORTAL_OVERVIEW_ENDPOINT;

@RestController
@Slf4j
@RequestMapping(PORTAL_OVERVIEW_ENDPOINT)
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(
                Collections.singletonMap("status", "UP")
        );
    }
}
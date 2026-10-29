package com.fitnessapp.portal.common.auth;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PortalLoginRateLimiterTest {

    private final MutableClock clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));
    private final PortalLoginRateLimiter limiter = new PortalLoginRateLimiter(clock);

    @Test
    void blocksAfterFiveFailedAttemptsWithinWindow() {
        String key = "user@example.test\u0000192.0.2.1";

        for (int i = 0; i < PortalLoginRateLimiter.MAX_FAILED_ATTEMPTS; i++) {
            limiter.recordFailure(key);
        }

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> limiter.assertAllowed(key)
        );
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, exception.getStatusCode());
    }

    @Test
    void allowsAttemptAfterWindowExpires() {
        String key = "user@example.test\u0000192.0.2.1";
        for (int i = 0; i < PortalLoginRateLimiter.MAX_FAILED_ATTEMPTS; i++) {
            limiter.recordFailure(key);
        }

        clock.advance(PortalLoginRateLimiter.WINDOW);

        assertDoesNotThrow(() -> limiter.assertAllowed(key));
    }

    @Test
    void successfulLoginClearsPriorFailures() {
        String key = "user@example.test\u0000192.0.2.1";
        for (int i = 0; i < PortalLoginRateLimiter.MAX_FAILED_ATTEMPTS; i++) {
            limiter.recordFailure(key);
        }

        limiter.recordSuccess(key);

        assertDoesNotThrow(() -> limiter.assertAllowed(key));
    }

    private static final class MutableClock extends Clock {
        private Instant instant;

        private MutableClock(Instant instant) {
            this.instant = instant;
        }

        private void advance(java.time.Duration duration) {
            instant = instant.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }
}

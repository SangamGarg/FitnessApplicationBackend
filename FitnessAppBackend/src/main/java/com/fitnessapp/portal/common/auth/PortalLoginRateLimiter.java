package com.fitnessapp.portal.common.auth;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/** Bounded, process-local failed-login tracking for the current single-instance setup. */
@Component
public class PortalLoginRateLimiter {

    static final int MAX_FAILED_ATTEMPTS = 5;
    static final Duration WINDOW = Duration.ofMinutes(15);
    private static final int MAX_TRACKED_KEYS = 10_000;

    private final Clock clock;
    private final Map<String, AttemptWindow> attempts = new LinkedHashMap<>(16, 0.75f, true);

    public PortalLoginRateLimiter() {
        this(Clock.systemUTC());
    }

    PortalLoginRateLimiter(Clock clock) {
        this.clock = clock;
    }

    public synchronized void assertAllowed(String key) {
        AttemptWindow window = attempts.get(key);
        if (window == null) {
            return;
        }

        Instant now = clock.instant();
        if (!window.startedAt.plus(WINDOW).isAfter(now)) {
            attempts.remove(key);
            return;
        }

        if (window.failures >= MAX_FAILED_ATTEMPTS) {
            throw new ResponseStatusException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "Too many failed sign-in attempts. Try again later."
            );
        }
    }

    public synchronized void recordFailure(String key) {
        Instant now = clock.instant();
        AttemptWindow window = attempts.get(key);
        if (window == null || !window.startedAt.plus(WINDOW).isAfter(now)) {
            ensureCapacity(now);
            window = new AttemptWindow(now);
            attempts.put(key, window);
        }
        window.failures++;
    }

    public synchronized void recordSuccess(String key) {
        attempts.remove(key);
    }

    private void ensureCapacity(Instant now) {
        Iterator<Map.Entry<String, AttemptWindow>> iterator = attempts.entrySet().iterator();
        while (iterator.hasNext()) {
            AttemptWindow window = iterator.next().getValue();
            if (!window.startedAt.plus(WINDOW).isAfter(now)) {
                iterator.remove();
            }
        }

        if (attempts.size() >= MAX_TRACKED_KEYS) {
            attempts.remove(attempts.keySet().iterator().next());
        }
    }

    private static final class AttemptWindow {
        private final Instant startedAt;
        private int failures;

        private AttemptWindow(Instant startedAt) {
            this.startedAt = startedAt;
        }
    }
}

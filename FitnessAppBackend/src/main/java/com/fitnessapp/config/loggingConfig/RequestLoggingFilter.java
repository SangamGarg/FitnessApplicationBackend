package com.fitnessapp.config.loggingConfig;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Logs one line per HTTP request and tags every log line of that request with a request id
 * (and the user id once downstream filters authenticate the caller) through SLF4J MDC.
 *
 * <p>Privacy: Only the HTTP method, URI, status, and duration are logged. Query strings,
 * payloads, and Authorization headers are excluded to safeguard sensitive health data and tokens.
 *
 * <p>The request ID is attached to the response header {@code X-Request-Id} for tracing.
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE) // Executes prior to Spring Security to capture 401/403 responses
public class RequestLoggingFilter extends OncePerRequestFilter {

    public static final String REQUEST_ID_HEADER = "X-Request-Id";
    public static final String MDC_REQUEST_ID = "requestId";
    public static final String MDC_USER_ID = "userId";

    private static final Logger ACCESS_LOG = LoggerFactory.getLogger("http.access");
    private static final long SLOW_REQUEST_MS = 2000L;
    private static final int REQUEST_ID_SUBSTRING_LENGTH = 12;

    // Accepts client-supplied IDs only if short and alphanumeric (prevents log injection / CRLF)
    private static final Pattern SAFE_REQUEST_ID = Pattern.compile("^[A-Za-z0-9._-]{1,64}$");

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        boolean bypass = path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || "/favicon.ico".equals(path);

        if (bypass) {
            log.trace("Bypassing RequestLoggingFilter for path: {}", path);
        }
        return bypass;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String incomingId = request.getHeader(REQUEST_ID_HEADER);
        String requestId;

        if (incomingId != null && SAFE_REQUEST_ID.matcher(incomingId).matches()) {
            requestId = incomingId;
            log.trace("Using client-provided request ID: {}", requestId);
        } else {
            requestId = UUID.randomUUID().toString().replace("-", "").substring(0, REQUEST_ID_SUBSTRING_LENGTH);
            log.trace("Generated internal request ID: {}", requestId);
        }

        MDC.put(MDC_REQUEST_ID, requestId);
        response.setHeader(REQUEST_ID_HEADER, requestId);

        long startNanos = System.nanoTime();
        Throwable failure = null;

        try {
            log.trace("Incoming HTTP request: {} {}", request.getMethod(), request.getRequestURI());
            filterChain.doFilter(request, response);
        } catch (ServletException | IOException | RuntimeException e) {
            failure = e;
            log.error("Unhandled exception caught in filter chain for request [{}]: {}", requestId, e.getMessage());
            throw e;
        } finally {
            long durationMs = (System.nanoTime() - startNanos) / 1_000_000L;
            int status = response.getStatus();
            String method = request.getMethod();
            String path = request.getRequestURI();

            if (failure != null) {
                ACCESS_LOG.error("{} {} -> failed with {} after {} ms", method, path, failure.getClass().getSimpleName(), durationMs, failure);
            } else if (status >= 500) {
                ACCESS_LOG.error("{} {} -> {} ({} ms)", method, path, status, durationMs);
            } else if (status >= 400) {
                ACCESS_LOG.warn("{} {} -> {} ({} ms)", method, path, status, durationMs);
            } else if (durationMs >= SLOW_REQUEST_MS) {
                ACCESS_LOG.warn("{} {} -> {} SLOW ({} ms)", method, path, status, durationMs);
            } else {
                ACCESS_LOG.info("{} {} -> {} ({} ms)", method, path, status, durationMs);
            }

            // Prevent thread-pool leakage in multi-tenant/async servlet environments
            MDC.remove(MDC_USER_ID);
            MDC.remove(MDC_REQUEST_ID);
        }
    }
}
package com.fitnessapp.developerAndTestingService.configAppScope.loggingConfig;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
 * Logs one line per HTTP request and tags every log line of that request with a request id (and the
 * user id once the JWT filter has authenticated the caller) through the SLF4J MDC.
 *
 * Privacy: only the HTTP method, path, status and duration are logged. Query strings, request/response
 * bodies and the Authorization header are never logged (they contain health data and tokens).
 *
 * The request id is returned in the X-Request-Id response header, so a user report can be matched to the log.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE) // run before Spring Security so 401/403 responses are logged too
public class RequestLoggingFilter extends OncePerRequestFilter {

    public static final String REQUEST_ID_HEADER = "X-Request-Id";
    public static final String MDC_REQUEST_ID = "requestId";
    public static final String MDC_USER_ID = "userId";

    private static final Logger accessLog = LoggerFactory.getLogger("http.access");
    private static final long SLOW_REQUEST_MS = 2000;
    // Only accept a client supplied id if it is short and harmless (prevents log injection).
    private static final Pattern SAFE_REQUEST_ID = Pattern.compile("[A-Za-z0-9._-]{1,64}");

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs") || path.equals("/favicon.ico");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String incoming = request.getHeader(REQUEST_ID_HEADER);
        String requestId = (incoming != null && SAFE_REQUEST_ID.matcher(incoming).matches())
                ? incoming
                : UUID.randomUUID().toString().replace("-", "").substring(0, 12);

        MDC.put(MDC_REQUEST_ID, requestId);
        response.setHeader(REQUEST_ID_HEADER, requestId);

        long start = System.nanoTime();
        Throwable failure = null;
        try {
            filterChain.doFilter(request, response);
        } catch (ServletException | IOException | RuntimeException e) {
            failure = e;
            throw e;
        } finally {
            long tookMs = (System.nanoTime() - start) / 1_000_000;
            int status = response.getStatus();
            String method = request.getMethod();
            String path = request.getRequestURI();

            if (failure != null) {
                accessLog.error("{} {} -> failed with {} after {} ms", method, path, failure.getClass().getSimpleName(), tookMs, failure);
            } else if (status >= 500) {
                accessLog.error("{} {} -> {} ({} ms)", method, path, status, tookMs);
            } else if (status >= 400) {
                accessLog.warn("{} {} -> {} ({} ms)", method, path, status, tookMs);
            } else if (tookMs >= SLOW_REQUEST_MS) {
                accessLog.warn("{} {} -> {} SLOW ({} ms)", method, path, status, tookMs);
            } else {
                accessLog.info("{} {} -> {} ({} ms)", method, path, status, tookMs);
            }

            MDC.remove(MDC_USER_ID);
            MDC.remove(MDC_REQUEST_ID);
        }
    }
}

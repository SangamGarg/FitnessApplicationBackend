//package com.fitnessapp.config.loggingConfig;
//
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.core.Ordered;
//import org.springframework.core.annotation.Order;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//import org.springframework.web.util.ContentCachingRequestWrapper;
//import org.springframework.web.util.ContentCachingResponseWrapper;
//
//import java.io.IOException;
//import java.nio.charset.StandardCharsets;
//import java.util.Collections;
//import java.util.stream.Collectors;
//
///**
// * Filter to capture and log full incoming HTTP request headers and payload,
// * along with outgoing response status and response payload.
// */
//@Slf4j
//@Component
//@Order(Ordered.HIGHEST_PRECEDENCE)
//public class FullHttpLoggingFilter extends OncePerRequestFilter {
//
//    private static final int MAX_PAYLOAD_LENGTH = 10000; // Cap at 10KB to prevent memory exhaustion
//
//    @Override
//    protected boolean shouldNotFilter(HttpServletRequest request) {
//        String path = request.getServletPath();
//        return path.startsWith("/swagger-ui")
//                || path.startsWith("/v3/api-docs")
//                || "/favicon.ico".equals(path);
//    }
//
//    @Override
//    protected void doFilterInternal(
//            HttpServletRequest request,
//            HttpServletResponse response,
//            FilterChain filterChain
//    ) throws ServletException, IOException {
//
//        // If your dependency expects 2 arguments, pass MAX_PAYLOAD_LENGTH as the second arg:
//        // ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request, MAX_PAYLOAD_LENGTH);
//        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request, MAX_PAYLOAD_LENGTH);
//        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);
//
//        long startTime = System.currentTimeMillis();
//
//        try {
//            // Process the request pipeline first (buffers populate as downstream controllers read/write)
//            filterChain.doFilter(requestWrapper, responseWrapper);
//        } finally {
//            long duration = System.currentTimeMillis() - startTime;
//
//            logRequest(requestWrapper);
//            logResponse(responseWrapper, duration);
//
//            // Mandatory: push cached response body back to the actual client response stream
//            responseWrapper.copyBodyToResponse();
//        }
//    }
//
//    private void logRequest(ContentCachingRequestWrapper request) {
//        String headers = Collections.list(request.getHeaderNames()).stream()
//                .filter(header -> !header.equalsIgnoreCase("authorization")) // Redact authorization tokens
//                .map(header -> header + ": " + Collections.list(request.getHeaders(header)))
//                .collect(Collectors.joining(", "));
//
//        String requestBody = getPayload(request.getContentAsByteArray(), request.getCharacterEncoding());
//
//        log.info("""
//                        \n--- INCOMING HTTP REQUEST ---
//                        Method       : {}
//                        URI          : {}
//                        Query String : {}
//                        Headers      : [{}]
//                        Payload      : {}
//                        -----------------------------""",
//                request.getMethod(),
//                request.getRequestURI(),
//                request.getQueryString() != null ? request.getQueryString() : "NONE",
//                headers,
//                requestBody.isBlank() ? "[EMPTY]" : requestBody
//        );
//    }
//
//    private void logResponse(ContentCachingResponseWrapper response, long durationMs) {
//        String responseBody = getPayload(response.getContentAsByteArray(), response.getCharacterEncoding());
//
//        log.info("""
//                        \n--- OUTGOING HTTP RESPONSE ---
//                        Status       : {}
//                        Duration     : {} ms
//                        Payload      : {}
//                        ------------------------------""",
//                response.getStatus(),
//                durationMs,
//                responseBody.isBlank() ? "[EMPTY]" : responseBody
//        );
//    }
//
//    private String getPayload(byte[] buf, String characterEncoding) {
//        if (buf == null || buf.length == 0) {
//            return "";
//        }
//        int length = Math.min(buf.length, MAX_PAYLOAD_LENGTH);
//        try {
//            String charset = (characterEncoding != null) ? characterEncoding : StandardCharsets.UTF_8.name();
//            String payload = new String(buf, 0, length, charset);
//            return buf.length > MAX_PAYLOAD_LENGTH ? payload + " ... [TRUNCATED]" : payload;
//        } catch (Exception e) {
//            return "[Binary / Unreadable Data]";
//        }
//    }
//}
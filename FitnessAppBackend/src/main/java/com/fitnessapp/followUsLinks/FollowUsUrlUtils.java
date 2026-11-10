package com.fitnessapp.followUsLinks;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

public final class FollowUsUrlUtils {

    private FollowUsUrlUtils() {
    }

    public static String normalizeTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Title cannot be blank"
            );
        }

        return title.trim().toLowerCase(Locale.ROOT);
    }

    public static String normalizeUrl(String url) {

        if (url == null || url.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "URL cannot be blank"
            );
        }

        String value = url.trim();

        if (value.length() > 500) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "URL cannot exceed 500 characters"
            );
        }

        try {
            URI uri = new URI(value);

            if (!"https".equalsIgnoreCase(uri.getScheme())
                    || uri.getHost() == null
                    || uri.getHost().isBlank()
                    || uri.getUserInfo() != null) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Only valid HTTPS URLs are allowed"
                );
            }

            int port = uri.getPort();

            if (port == 0 || port > 65535) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Invalid URL port"
                );
            }

            String host = uri.getHost().toLowerCase(Locale.ROOT);

            StringBuilder normalized = new StringBuilder("https://")
                    .append(host);

            if (port != -1 && port != 443) {
                normalized.append(":").append(port);
            }

            URI normalizedUri = uri.normalize();

            if (normalizedUri.getRawPath() != null) {
                normalized.append(normalizedUri.getRawPath());
            }

            if (normalizedUri.getRawQuery() != null) {
                normalized.append("?").append(normalizedUri.getRawQuery());
            }

            if (normalizedUri.getRawFragment() != null) {
                normalized.append("#").append(normalizedUri.getRawFragment());
            }

            return normalized.toString();

        } catch (URISyntaxException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid URL format"
            );
        }
    }
}
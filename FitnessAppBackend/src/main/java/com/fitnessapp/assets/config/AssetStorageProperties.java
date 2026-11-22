package com.fitnessapp.assets.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.time.Duration;
import java.util.Arrays;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.storage")
public class AssetStorageProperties {
    private String baseUrl;
    private String supabaseUrl;
    private String bucket;
    private String serviceKey;
    private DataSize maxFileSize = DataSize.ofMegabytes(5);
    private Duration connectTimeout = Duration.ofSeconds(5);
    private Duration readTimeout = Duration.ofSeconds(30);

    @Autowired
    private Environment environment;

    @PostConstruct
    void validateServiceKey() {
        boolean developmentProfile = Arrays.stream(environment.getActiveProfiles())
                .anyMatch(profile -> profile.equalsIgnoreCase("dev") || profile.equalsIgnoreCase("test"));
        if (!developmentProfile && (serviceKey == null || serviceKey.isBlank())) {
            throw new IllegalStateException("app.storage.service-key must be configured outside dev/test profiles");
        }
    }

    public String normalizedBaseUrl() {
        if (baseUrl == null || baseUrl.isBlank()) {
            return "";
        }
        return baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
    }

    public String normalizedSupabaseUrl() {
        if (supabaseUrl == null || supabaseUrl.isBlank()) {
            return "";
        }
        return supabaseUrl.endsWith("/") ? supabaseUrl.substring(0, supabaseUrl.length() - 1) : supabaseUrl;
    }

    public void validateForUpload() {
        validateHttpUrl(supabaseUrl, "app.storage.supabase-url");
        validateHttpUrl(baseUrl, "app.storage.base-url");
        if (bucket == null || bucket.isBlank() || serviceKey == null || serviceKey.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Asset storage is not configured");
        }
    }

    private void validateHttpUrl(String value, String propertyName) {
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Asset storage is not configured");
        }

        try {
            URI uri = URI.create(value.trim());
            String scheme = uri.getScheme();
            if (!uri.isAbsolute()
                    || uri.getHost() == null
                    || !("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))
                    || uri.getRawUserInfo() != null
                    || uri.getRawQuery() != null
                    || uri.getRawFragment() != null) {
                throw invalidUrl(propertyName);
            }
        } catch (IllegalArgumentException exception) {
            throw invalidUrl(propertyName);
        }
    }

    private ResponseStatusException invalidUrl(String propertyName) {
        return new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Invalid asset storage URL configuration: " + propertyName
        );
    }
}

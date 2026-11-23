package com.fitnessapp.assets.service;

import com.fitnessapp.assets.config.AssetStorageProperties;
import com.fitnessapp.assets.util.ImageFileValidator;
import com.fitnessapp.assets.util.ImageFileValidator.ValidatedImage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

@Slf4j
@Service
public class FileStorageServiceImpl implements FileStorageService {
    private static final Pattern GENERATED_OBJECT_PATH =
            Pattern.compile("[a-z0-9_-]+/[0-9a-fA-F-]{36}\\.(?:png|jpg|webp|gif|mp4|webm)");

    private final RestClient restClient;
    private final AssetStorageProperties properties;
    private final ImageFileValidator imageFileValidator;

    public FileStorageServiceImpl(@Qualifier("assetStorageRestClient") RestClient restClient, AssetStorageProperties properties, ImageFileValidator imageFileValidator) {
        this.restClient = restClient;
        this.properties = properties;
        this.imageFileValidator = imageFileValidator;
    }

    @Override
    public String uploadFile(MultipartFile file, String folder) {
        ValidatedImage image = imageFileValidator.validate(file);
        properties.validateForUpload();

        String safeFolder = folder == null ? "" : folder.toLowerCase(Locale.ROOT);
        if (!safeFolder.matches("[a-z0-9_-]+")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid asset storage folder");
        }
        String objectPath = safeFolder + "/" + UUID.randomUUID() + "." + image.extension();
        byte[] content;
        try {
            content = file.getBytes();
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to read image file", exception);
        }

        try {
            restClient.post()
                    .uri(storageUri(objectPath))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getServiceKey())
                    .header("x-upsert", "false")
                    .contentType(MediaType.parseMediaType(image.contentType()))
                    .body(content)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Failed to upload image to storage",
                    exception
            );
        }
        return properties.normalizedBaseUrl() + objectPath;
    }

    @Override
    public String uploadMedia(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Media file cannot be empty");
        }
        if (file.getSize() > properties.getMaxFileSize().toBytes()) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "Media file exceeds the maximum allowed size");
        }

        properties.validateForUpload();
        String safeFolder = folder == null ? "" : folder.toLowerCase(Locale.ROOT);
        if (!safeFolder.matches("[a-z0-9_-]+")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid asset storage folder");
        }

        MediaType mediaType;
        String extension;
        String contentType = file.getContentType();
        byte[] content;
        try {
            content = file.getBytes();
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unable to read media file", exception);
        }

        if (isImage(content)) {
            ValidatedImage image = imageFileValidator.validate(file);
            mediaType = MediaType.parseMediaType(image.contentType());
            extension = image.extension();
        } else if (isMp4(content)) {
            mediaType = MediaType.parseMediaType("video/mp4");
            extension = "mp4";
        } else if (isWebm(content)) {
            mediaType = MediaType.parseMediaType("video/webm");
            extension = "webm";
        } else {
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                    "Unsupported media file type. Use PNG, JPG, WebP, GIF, MP4, or WebM.");
        }

        String objectPath = safeFolder + "/" + UUID.randomUUID() + "." + extension;
        try {
            restClient.post()
                    .uri(storageUri(objectPath))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getServiceKey())
                    .header("x-upsert", "false")
                    .contentType(mediaType)
                    .body(content)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Failed to upload media to storage", exception);
        }
        return properties.normalizedBaseUrl() + objectPath;
    }

    private boolean isImage(byte[] content) {
        return imageFileValidator.isSupportedImage(content);
    }

    private boolean isMp4(byte[] content) {
        return content.length >= 12
                && content[4] == 'f' && content[5] == 't' && content[6] == 'y' && content[7] == 'p';
    }

    private boolean isWebm(byte[] content) {
        return content.length >= 4
                && (content[0] & 0xff) == 0x1a
                && (content[1] & 0xff) == 0x45
                && (content[2] & 0xff) == 0xdf
                && (content[3] & 0xff) == 0xa3;
    }

    @Override
    public void deleteFileByUrl(String fileUrl) {
        String baseUrl = properties.normalizedBaseUrl();
        if (fileUrl == null || baseUrl.isBlank() || !fileUrl.startsWith(baseUrl)) {
            log.warn("Skipped storage deletion because the URL is outside the configured public asset base URL");
            return;
        }

        String objectPath = fileUrl.substring(baseUrl.length());
        if (!GENERATED_OBJECT_PATH.matcher(objectPath).matches()) {
            log.warn("Skipped storage deletion because the object path is invalid");
            return;
        }

        try {
            requireStorageConfiguration();
            restClient.delete()
                    .uri(storageUri(objectPath))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getServiceKey())
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException.NotFound ignored) {
            // Deleting an already-removed object is considered successful.
        } catch (RestClientException | IllegalArgumentException | ResponseStatusException exception) {
            log.warn("Unable to delete an asset from storage");
        }
    }

    private URI storageUri(String objectPath) {
        String[] segments = objectPath.split("/");
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(properties.normalizedSupabaseUrl())
                .pathSegment("storage", "v1", "object", properties.getBucket());
        builder.pathSegment(segments);
        return builder.build().encode().toUri();
    }

    private void requireStorageConfiguration() {
        if (properties.normalizedSupabaseUrl().isBlank()
                || properties.getBucket() == null
                || properties.getBucket().isBlank()
                || properties.getServiceKey() == null
                || properties.getServiceKey().isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Asset storage is not configured");
        }
    }
}

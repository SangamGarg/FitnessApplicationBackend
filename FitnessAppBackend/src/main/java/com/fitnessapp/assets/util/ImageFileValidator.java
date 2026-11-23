package com.fitnessapp.assets.util;

import com.fitnessapp.assets.config.AssetStorageProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class ImageFileValidator {
    private final AssetStorageProperties properties;

    public ValidatedImage validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image file cannot be empty");
        }
        checkSize(file.getSize());

        byte[] header;
        try {
            header = file.getBytes();
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unable to read uploaded image", exception);
        }
        if (header.length == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image file cannot be empty");
        }
        checkSize(header.length);

        ValidatedImage image = detect(header);
        if (image == null) {
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Unsupported image file type");
        }
        return new ValidatedImage(image.contentType(), image.extension(), header.length);
    }

    public boolean isSupportedImage(byte[] data) {
        return detect(data) != null;
    }

    private void checkSize(long size) {
        if (size > properties.getMaxFileSize().toBytes()) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "Image exceeds the maximum allowed size");
        }
    }

    private ValidatedImage detect(byte[] data) {
        if (matches(data, 0x89, 0x50, 0x4E, 0x47)) {
            return new ValidatedImage("image/png", "png", data.length);
        }
        if (matches(data, 0xFF, 0xD8, 0xFF)) {
            return new ValidatedImage("image/jpeg", "jpg", data.length);
        }
        if (matchesAscii(data, 0, "GIF87a") || matchesAscii(data, 0, "GIF89a")) {
            return new ValidatedImage("image/gif", "gif", data.length);
        }
        if (matchesAscii(data, 0, "RIFF") && matchesAscii(data, 8, "WEBP")) {
            return new ValidatedImage("image/webp", "webp", data.length);
        }
        return null;
    }

    private boolean matches(byte[] data, int... expected) {
        if (data.length < expected.length) {
            return false;
        }
        for (int index = 0; index < expected.length; index++) {
            if ((data[index] & 0xFF) != expected[index]) {
                return false;
            }
        }
        return true;
    }

    private boolean matchesAscii(byte[] data, int offset, String expected) {
        if (data.length < offset + expected.length()) {
            return false;
        }
        for (int index = 0; index < expected.length(); index++) {
            if (data[offset + index] != expected.charAt(index)) {
                return false;
            }
        }
        return true;
    }

    public record ValidatedImage(String contentType, String extension, long sizeBytes) {
    }
}

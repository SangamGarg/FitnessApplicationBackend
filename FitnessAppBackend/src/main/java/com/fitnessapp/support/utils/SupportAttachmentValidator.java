
package com.fitnessapp.support.utils;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Component
public class SupportAttachmentValidator {

    private static final int MAX_ATTACHMENTS = 3;

    private static final long MAX_FILE_SIZE =
            5L * 1024 * 1024; // 5 MB per file

    private static final long MAX_TOTAL_SIZE =
            10L * 1024 * 1024; // 10 MB total

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/webp"
    );

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "jpg",
            "jpeg",
            "png",
            "webp"
    );

    public void validate(List<MultipartFile> files) {

        if (files == null || files.isEmpty()) {
            return;
        }

        if (files.size() > MAX_ATTACHMENTS) {
            throw new IllegalArgumentException(
                    "Maximum " + MAX_ATTACHMENTS + " attachments are allowed."
            );
        }

        long totalSize = 0;

        for (MultipartFile file : files) {

            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException(
                        "Empty attachment is not allowed."
                );
            }

            // Validate individual file size
            if (file.getSize() > MAX_FILE_SIZE) {
                throw new IllegalArgumentException(
                        "Each attachment must be 5 MB or smaller."
                );
            }

            totalSize += file.getSize();

            // Validate total attachment size
            if (totalSize > MAX_TOTAL_SIZE) {
                throw new IllegalArgumentException(
                        "Total attachment size cannot exceed 10 MB."
                );
            }

            // Validate MIME type
            String contentType = file.getContentType();

            if (contentType == null ||
                    !ALLOWED_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {

                throw new IllegalArgumentException(
                        "Only JPG, JPEG, PNG and WEBP images are allowed."
                );
            }

            // Validate extension
            String filename = file.getOriginalFilename();
            String extension = extractExtension(filename);

            if (extension == null ||
                    !ALLOWED_EXTENSIONS.contains(extension.toLowerCase(Locale.ROOT))) {

                throw new IllegalArgumentException(
                        "Invalid file extension. Only JPG, JPEG, PNG and WEBP are allowed."
                );
            }

            // Validate actual file signature
            validateImageSignature(file);
        }
    }

    private void validateImageSignature(MultipartFile file) {

        try {
            byte[] bytes = file.getBytes();

            boolean valid =
                    isJpeg(bytes) ||
                            isPng(bytes) ||
                            isWebp(bytes);

            if (!valid) {
                throw new IllegalArgumentException(
                        "Invalid image file content."
                );
            }

        } catch (IOException e) {
            throw new IllegalArgumentException(
                    "Unable to read attachment."
            );
        }
    }

    private boolean isJpeg(byte[] b) {
        return b.length >= 3
                && (b[0] & 0xFF) == 0xFF
                && (b[1] & 0xFF) == 0xD8
                && (b[2] & 0xFF) == 0xFF;
    }

    private boolean isPng(byte[] b) {
        return b.length >= 8
                && (b[0] & 0xFF) == 0x89
                && b[1] == 0x50
                && b[2] == 0x4E
                && b[3] == 0x47
                && b[4] == 0x0D
                && b[5] == 0x0A
                && b[6] == 0x1A
                && b[7] == 0x0A;
    }

    private boolean isWebp(byte[] b) {
        return b.length >= 12
                && b[0] == 'R'
                && b[1] == 'I'
                && b[2] == 'F'
                && b[3] == 'F'
                && b[8] == 'W'
                && b[9] == 'E'
                && b[10] == 'B'
                && b[11] == 'P';
    }

    private String extractExtension(String filename) {

        if (filename == null || !filename.contains(".")) {
            return null;
        }

        return filename.substring(filename.lastIndexOf('.') + 1);
    }
}

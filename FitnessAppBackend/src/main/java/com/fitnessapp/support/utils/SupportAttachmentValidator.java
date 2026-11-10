package com.fitnessapp.support.utils;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@Component
public class SupportAttachmentValidator {

    private static final int MAX_ATTACHMENTS = 3;

    private static final long MAX_FILE_SIZE =
            5 * 1024 * 1024;

    private static final long MAX_TOTAL_SIZE =
            10 * 1024 * 1024;

    private static final Set<String> ALLOWED_TYPES =
            Set.of("image/jpeg", "image/png", "image/webp");

    public void validate(List<MultipartFile> files) {

        if (files == null || files.isEmpty()) {
            return;
        }

        if (files.size() > MAX_ATTACHMENTS) {
            throw new IllegalArgumentException(
                    "Maximum 3 attachments are allowed");
        }

        long totalSize = 0;

        for (MultipartFile file : files) {

            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException(
                        "Empty attachment is not allowed");
            }

            if (file.getSize() > MAX_FILE_SIZE) {
                throw new IllegalArgumentException(
                        "Each attachment must be 5 MB or smaller");
            }

            totalSize += file.getSize();

            if (totalSize > MAX_TOTAL_SIZE) {
                throw new IllegalArgumentException(
                        "Total attachment size cannot exceed 10 MB");
            }

            String contentType = file.getContentType();

            if (contentType == null ||
                    !ALLOWED_TYPES.contains(contentType.toLowerCase())) {
                throw new IllegalArgumentException(
                        "Only JPEG, PNG and WebP images are allowed");
            }

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
                        "Invalid image file content");
            }

        } catch (IOException e) {
            throw new IllegalArgumentException(
                    "Unable to read attachment");
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





    // 10 Megabytes in bytes
    private static final long MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024; // 10,485,760 bytes
    private static final int MAX_ATTACHMENT_COUNT = 3;

    // Allowed MIME types
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/webp",
            "video/mp4"
    );

    // Allowed file extensions
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "webp", "mp4"
    );

    public void validateAttachments(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return; // Attachments are optional
        }

        if (files.size() > MAX_ATTACHMENT_COUNT) {
            throw new IllegalArgumentException("You can upload a maximum of " + MAX_ATTACHMENT_COUNT + " attachments.");
        }

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }

            // 1. Validate File Size
            if (file.getSize() > MAX_FILE_SIZE_BYTES) {
                throw new IllegalArgumentException(
                        String.format("File '%s' exceeds the 10MB limit. Current size: %.2f MB",
                                file.getOriginalFilename(),
                                (file.getSize() / (1024.0 * 1024.0)))
                );
            }

            // 2. Validate MIME Type
            String contentType = file.getContentType();
            if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
                throw new IllegalArgumentException(
                        "File format not supported for '" + file.getOriginalFilename() +
                                "'. Only JPEG, PNG, WEBP, and MP4 files are allowed."
                );
            }

            // 3. Validate File Extension
            String originalFilename = file.getOriginalFilename();
            String extension = extractExtension(originalFilename);
            if (extension == null || !ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
                throw new IllegalArgumentException(
                        "Invalid file extension for '" + originalFilename +
                                "'. Allowed extensions: .jpg, .jpeg, .png, .webp, .mp4"
                );
            }
        }
    }

    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return null;
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
}
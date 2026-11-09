package com.fitnessapp.support.utils;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

@Component
public class TicketAttachmentValidator {

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
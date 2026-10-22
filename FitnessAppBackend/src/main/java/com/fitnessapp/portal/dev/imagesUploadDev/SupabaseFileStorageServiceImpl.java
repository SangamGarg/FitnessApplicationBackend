package com.fitnessapp.portal.dev.imagesUploadDev;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class SupabaseFileStorageServiceImpl implements FileStorageService {

    @Value("${app.storage.base-url:https://xyz.supabase.co/storage/v1/object/public/app-assets/}")
    private String storageBaseUrl;

    @Override
    public String uploadFile(MultipartFile file, String folder) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Cannot upload an empty file");
        }

        // Generate safe unique filename to avoid browser caching issues on updates
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String generatedFileName = folder + "/" + UUID.randomUUID() + extension;

        // Perform Supabase Storage API call or SDK client put operation:
        // supabaseClient.storage.from("app-assets").upload(generatedFileName, file.getBytes());

        return storageBaseUrl + generatedFileName;
    }

    @Override
    public void deleteFileByUrl(String fileUrl) {

    }
}
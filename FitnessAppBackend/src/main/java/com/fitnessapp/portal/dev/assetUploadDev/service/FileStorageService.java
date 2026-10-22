package com.fitnessapp.portal.dev.assetUploadDev.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    String uploadFile(MultipartFile file, String folder);

    void deleteFileByUrl(String fileUrl);
}
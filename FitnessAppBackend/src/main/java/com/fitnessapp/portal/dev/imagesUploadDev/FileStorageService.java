package com.fitnessapp.portal.dev.imagesUploadDev;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    String uploadFile(MultipartFile file, String folder);

    void deleteFileByUrl(String fileUrl);
}
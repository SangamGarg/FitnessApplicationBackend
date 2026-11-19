package com.fitnessapp.profile.service;

import com.fitnessapp.profile.model.dto.response.BadgeResponseDto;
import com.fitnessapp.profile.model.dto.response.ProfileHomeResponseDto;
import com.fitnessapp.profile.model.dto.response.ProgressGalleryResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProfileService {
    ProfileHomeResponseDto getProfileHome();

    List<BadgeResponseDto> getAllBadges();

    List<BadgeResponseDto> getUnlockedBadges();

    List<BadgeResponseDto> getLockedBadges();

    BadgeResponseDto getBadgeDetails(Long badgeId);

    BadgeResponseDto unlockBadge(String userId, Long badgeId);

    ProgressGalleryResponseDto uploadProgressImage(MultipartFile file, String caption);

    Page<ProgressGalleryResponseDto> getProgressGallery(Pageable pageable);

    ProgressGalleryResponseDto getProgressImage(Long imageId);

    void deleteProgressImage(Long imageId);
}

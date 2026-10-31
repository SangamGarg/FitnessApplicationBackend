package com.fitnessapp.appAuth.utils;

import com.fitnessapp.appAuth.model.dtos.UserDetailsDto;
import com.fitnessapp.appAuth.model.dtos.requestDto.UserPatchRequestDto;
import com.fitnessapp.appAuth.model.entity.UserDetailsEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class AppAuthUtils {

    private AppAuthUtils() {
    }

    public static String getUserIdFromTokenInHeader() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : null;
    }

    public static UserDetailsDto userDetailsEntityToUserDetailsDto(UserDetailsEntity user) {
        return UserDetailsDto.builder()
                .name(user.getName())
                .email(user.getEmail())
                .gender(user.getGender())
                .age(user.getAge())
                .profileImageUrl(user.getProfileImageUrl())
                .heightCm(user.getHeightCm())
                .weightKg(user.getWeightKg())
                .activityLevel(user.getActivityLevel())
                .goal(user.getGoal())
                .build();
    }

    /** Applies only supplied values; null fields in a PATCH request leave stored values unchanged. */
    public static UserDetailsEntity patchLogic(
            UserPatchRequestDto request,
            UserDetailsEntity existingUserDetails
    ) {
        if (request.getName() != null) existingUserDetails.setName(request.getName());
        if (request.getGender() != null) existingUserDetails.setGender(request.getGender());
        if (request.getAge() != null) existingUserDetails.setAge(request.getAge());
        if (request.getProfileImageUrl() != null) existingUserDetails.setProfileImageUrl(request.getProfileImageUrl());
        if (request.getHeightCm() != null) existingUserDetails.setHeightCm(request.getHeightCm());
        if (request.getWeightKg() != null) existingUserDetails.setWeightKg(request.getWeightKg());
        if (request.getActivityLevel() != null) existingUserDetails.setActivityLevel(request.getActivityLevel());
        if (request.getGoal() != null) existingUserDetails.setGoal(request.getGoal());

        return existingUserDetails;
    }
}

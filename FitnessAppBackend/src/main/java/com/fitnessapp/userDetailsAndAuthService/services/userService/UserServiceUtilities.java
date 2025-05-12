package com.fitnessapp.userDetailsAndAuthService.services.userService;

import com.fitnessapp.userDetailsAndAuthService.models.dtos.UserDetailsDto;
import com.fitnessapp.userDetailsAndAuthService.models.dtos.requestDtos.UserPatchRequestDto;
import com.fitnessapp.userDetailsAndAuthService.models.entitites.UserDetailsEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class UserServiceUtilities {
    public static String getUserIdFromTokenInHeader() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName(); // This is the Firebase UID from the token
    }

    public static UserDetailsDto userDetailsEntityToUserDetailsDto(UserDetailsEntity user) {
        return UserDetailsDto.builder()
                .name(user.getName())
                .firebaseCloudMessagingToken(user.getFirebaseCloudMessagingToken())
                .email(user.getEmail())
                .gender(user.getGender())
                .age(user.getAge())
                .profileImageUrl(user.getProfileImageUrl())
                .heightCm(user.getHeightCm())
                .weightKg(user.getWeightKg())
                .activityLevel(user.getActivityLevel())
                .goal(user.getGoal())
                .hipCm(user.getHipCm())
                .neckCm(user.getNeckCm())
                .waistCm(user.getWaistCm())
                .build();
    }

    public static UserDetailsEntity patchLogic(UserPatchRequestDto userDetailsDto, UserDetailsEntity existingUserDetails) {


        if (userDetailsDto.getName() != null) existingUserDetails.setName(userDetailsDto.getName());
        if (userDetailsDto.getFirebaseCloudMessagingToken() != null && !userDetailsDto.getFirebaseCloudMessagingToken().isEmpty())
            existingUserDetails.setFirebaseCloudMessagingToken(userDetailsDto.getFirebaseCloudMessagingToken());
        if (userDetailsDto.getGender() != null) existingUserDetails.setGender(userDetailsDto.getGender());
        if (userDetailsDto.getAge() != null) existingUserDetails.setAge(userDetailsDto.getAge());
        if (userDetailsDto.getProfileImageUrl() != null)
            existingUserDetails.setProfileImageUrl(userDetailsDto.getProfileImageUrl());
        if (userDetailsDto.getHeightCm() > 0) existingUserDetails.setHeightCm(userDetailsDto.getHeightCm());
        if (userDetailsDto.getWeightKg() > 0) existingUserDetails.setWeightKg(userDetailsDto.getWeightKg());
        if (userDetailsDto.getActivityLevel() != null)
            existingUserDetails.setActivityLevel(userDetailsDto.getActivityLevel());
        if (userDetailsDto.getGoal() != null) existingUserDetails.setGoal(userDetailsDto.getGoal());
        if (userDetailsDto.getHipCm() > 0) existingUserDetails.setHipCm(userDetailsDto.getHipCm());
        if (userDetailsDto.getNeckCm() > 0) existingUserDetails.setNeckCm(userDetailsDto.getNeckCm());
        if (userDetailsDto.getWaistCm() > 0) existingUserDetails.setWaistCm(userDetailsDto.getWaistCm());

        return existingUserDetails;
    }
}
package com.fitnessapp.appAuth.utils;

import com.fitnessapp.appAuth.model.dtos.UserDetailsDto;
import com.fitnessapp.appAuth.model.dtos.requestDto.UserPatchRequestDto;
import com.fitnessapp.appAuth.model.entity.UserDetailsEntity;
import com.fitnessapp.calculator.model.dtos.requestDto.BodyFatRequestDto;
import com.fitnessapp.calculator.model.dtos.requestDto.DailyCaloriesRequestDto;
import com.fitnessapp.calculator.model.dtos.responseDto.BmiCalculateResponseDto;
import com.fitnessapp.calculator.model.dtos.responseDto.BodyFatResponseDto;
import com.fitnessapp.calculator.model.dtos.responseDto.DailyCaloriesResponseDto;
import com.fitnessapp.calculator.model.dtos.responseDto.IdealWeightResponseDto;
import com.fitnessapp.calculator.service.CalculatorService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;

public final class AppAuthUtils {

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
//                .hipCm(user.getHipCm())
//                .neckCm(user.getNeckCm())
//                .waistCm(user.getWaistCm())
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
        if (userDetailsDto.getHeightCm() != null && userDetailsDto.getHeightCm() > 0)
            existingUserDetails.setHeightCm(userDetailsDto.getHeightCm());
        if (userDetailsDto.getWeightKg() != null && userDetailsDto.getWeightKg() > 0)
            existingUserDetails.setWeightKg(userDetailsDto.getWeightKg());
        if (userDetailsDto.getActivityLevel() != null)
            existingUserDetails.setActivityLevel(userDetailsDto.getActivityLevel());
        if (userDetailsDto.getGoal() != null) existingUserDetails.setGoal(userDetailsDto.getGoal());
//        if (userDetailsDto.getHipCm() != null && userDetailsDto.getHipCm() > 0)
//            existingUserDetails.setHipCm(userDetailsDto.getHipCm());
//        if (userDetailsDto.getNeckCm() != null && userDetailsDto.getNeckCm() > 0)
//            existingUserDetails.setNeckCm(userDetailsDto.getNeckCm());
//        if (userDetailsDto.getWaistCm() != null && userDetailsDto.getWaistCm() > 0)
//            existingUserDetails.setWaistCm(userDetailsDto.getWaistCm());

        return existingUserDetails;
    }


    public static float getBmi(CalculatorService calculatorService, UserDetailsDto dto) {
        ResponseEntity<?> response = calculatorService.getBmi(dto.getWeightKg(), dto.getHeightCm());
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() instanceof BmiCalculateResponseDto body) {
            return body.getBmi();
        } else {
            return 0;
        }
    }

    public static float getIdealWeight(CalculatorService calculatorService, UserDetailsDto dto) {
        ResponseEntity<?> response = calculatorService.getIdealWeight(dto.getGender(), dto.getHeightCm());
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() instanceof IdealWeightResponseDto body) {
            return (float) body.getIdealWeight().get("Hamwi");
        } else {
            return 0;
        }
    }


    public static float getDailyCalories(CalculatorService calculatorService, UserDetailsDto dto) {
        ResponseEntity<?> response = calculatorService.getDailyCalories(
                DailyCaloriesRequestDto.builder()
                        .activityLevel(dto.getActivityLevel())
                        .age(dto.getAge())
                        .gender(dto.getGender())
                        .heightCm(dto.getHeightCm())
                        .weightKg(dto.getWeightKg())
                        .build()
        );

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() instanceof DailyCaloriesResponseDto body) {
            Map<String, Object> goals = body.getGoals();
            if (goals != null && goals.containsKey(dto.getGoal())) {
                Object goalValue = goals.get(dto.getGoal());

                if (goalValue instanceof Integer) {
                    // Directly an integer (like "Maintain weight")
                    return ((Integer) goalValue).floatValue();
                } else if (goalValue instanceof Map<?, ?> goalMap) {
                    Object caloryValue = goalMap.get("calory");
                    if (caloryValue instanceof Integer) {
                        return ((Integer) caloryValue).floatValue();
                    }
                }
            }
        }

        return 0;
    }


    public static float getBodyFat(CalculatorService calculatorService, UserDetailsDto dto) {
        ResponseEntity<?> response = calculatorService.getBodyFat(
                BodyFatRequestDto.builder()
                        .age(dto.getAge())
                        .gender(dto.getGender())
                        .heightCm(dto.getHeightCm())
                        .weightKg(dto.getWeightKg())
//                        .waistCm(dto.getWaistCm())
//                        .hipCm(dto.getHipCm())
//                        .neckCm(dto.getNeckCm())
                        .build()
        );

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() instanceof BodyFatResponseDto body) {
            Map<String, Object> goals = body.getData();
            if (goals != null && goals.containsKey("BodyFatMass")) {
                Object maintainWeightValue = goals.get("BodyFatMass");
                if (maintainWeightValue instanceof Integer) {
                    return ((Integer) maintainWeightValue).floatValue();
                }
            }
        }
        return 0;
    }
}
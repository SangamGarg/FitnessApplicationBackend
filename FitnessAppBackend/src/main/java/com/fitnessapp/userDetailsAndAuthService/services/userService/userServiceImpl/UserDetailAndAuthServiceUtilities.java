package com.fitnessapp.userDetailsAndAuthService.services.userService.userServiceImpl;

import com.fitnessapp.fitnessCalculatorsService.models.dtos.requestDto.BodyFatRequestDto;
import com.fitnessapp.fitnessCalculatorsService.models.dtos.requestDto.DailyCaloriesRequestDto;
import com.fitnessapp.fitnessCalculatorsService.models.dtos.responseDto.BmiCalculateResponseDto;
import com.fitnessapp.fitnessCalculatorsService.models.dtos.responseDto.BodyFatResponseDto;
import com.fitnessapp.fitnessCalculatorsService.models.dtos.responseDto.DailyCaloriesResponseDto;
import com.fitnessapp.fitnessCalculatorsService.models.dtos.responseDto.IdealWeightResponseDto;
import com.fitnessapp.fitnessCalculatorsService.services.CalculatorService;
import com.fitnessapp.userDetailsAndAuthService.models.dtos.UserDetailsDto;
import com.fitnessapp.userDetailsAndAuthService.models.dtos.requestDtos.UserPatchRequestDto;
import com.fitnessapp.userDetailsAndAuthService.models.entitites.UserDetailsEntity;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;

public final class UserDetailAndAuthServiceUtilities {
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

        System.out.println("DAILYCALORIES  " + response.getBody());
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
                        .waistCm(dto.getWaistCm())
                        .hipCm(dto.getHipCm())
                        .neckCm(dto.getNeckCm())
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

    public static void sendSuccessfulRegistrationMailToUser(JavaMailSender mailSender, String name, String email) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

        helper.setTo(email);
        helper.setSubject("Welcome Aboard! You’ve Successfully Registered and Assigned as Developer");

        String htmlContent = """
                <html>
                        <head>
                            <style>
                                body {
                                    font-family: Arial, sans-serif;
                                    background-color: #f4f6f8;
                                    padding: 20px;
                                    color: #333;
                                }
                                .container {
                                    background-color: #ffffff;
                                    padding: 30px;
                                    border-radius: 10px;
                                    max-width: 600px;
                                    margin: auto;
                                    box-shadow: 0 4px 12px rgba(0,0,0,0.05);
                                }
                                h2 {
                                    color: #2E7D32;
                                    margin-bottom: 10px;
                                }
                                .welcome-box {
                                    background-color: #E8F5E9;
                                    padding: 15px;
                                    border-left: 5px solid #2E7D32;
                                    margin: 20px 0;
                                    font-size: 15px;
                                }
                                .button {
                                    display: inline-block;
                                    margin-top: 20px;
                                    padding: 12px 20px;
                                    background-color: #2E7D32;
                                    color: #fff;
                                    text-decoration: none;
                                    border-radius: 6px;
                                    font-weight: bold;
                                }
                                .footer {
                                    margin-top: 30px;
                                    font-size: 13px;
                                    color: #888;
                                    text-align: center;
                                }
                            </style>
                        </head>
                        <body>
                            <div class="container">
                                <h2>Welcome to Fitness App, %s! 🎉</h2>
                                <p>Your registration was successful, and your account is now ready to use.</p>
                
                                <div class="welcome-box">
                                    You can now explore workouts, track your progress, and achieve your fitness goals with us.
                                </div>
                
                                <p>We're excited to have you on board. Start your fitness journey today!</p>
                
                                <p style="margin-top: 40px;">Stay active,<br/><strong>The Fitness App Team</strong></p>
                            </div>
                
                            <div class="footer">
                                © 2026 Fitness App • All rights reserved
                            </div>
                        </body>
                        </html>
                """.formatted(name);


        helper.setText(htmlContent, true); // true = isHtml

        mailSender.send(mimeMessage);
    }
}
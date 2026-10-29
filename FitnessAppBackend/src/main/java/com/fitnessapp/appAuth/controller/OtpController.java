package com.fitnessapp.appAuth.controller;

import com.fitnessapp.appAuth.model.dtos.requestDto.OtpRequestDto;
import com.fitnessapp.appAuth.model.dtos.requestDto.ResetPasswordWithOtpRequestDto;
import com.fitnessapp.appAuth.model.dtos.requestDto.VerifyEmailOtpRequestDto;
import com.fitnessapp.appAuth.model.dtos.responseDto.OtpActionResponseDto;
import com.fitnessapp.appAuth.service.AppOtpService;
import com.fitnessapp.utils.appUtil.EmailMasker;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.fitnessapp.utils.appUtil.AppUtil.APP_OTP_ENDPOINT;

@Slf4j
@RestController
@RequestMapping(APP_OTP_ENDPOINT)
@RequiredArgsConstructor
public class OtpController {

    private final AppOtpService appOtpService;

    @PostMapping("/signup/send")
    public ResponseEntity<OtpActionResponseDto> sendSignupOtp(@Valid @RequestBody OtpRequestDto request) {
        String email = EmailMasker.maskEmail(request.getEmail());
        log.info("Received request to send signup OTP to email: {}", email);
        ResponseEntity<OtpActionResponseDto> response = appOtpService.sendSignupOtp(request);
        log.info("Signup OTP sent successfully to email: {}, status: {}", email, response.getStatusCode());
        return response;
    }

    @PostMapping("/signup/verify")
    public ResponseEntity<OtpActionResponseDto> verifySignupOtp(@Valid @RequestBody VerifyEmailOtpRequestDto request) {
        String email = EmailMasker.maskEmail(request.getEmail());
        log.info("Received request to verify signup OTP for email: {}", email);
        ResponseEntity<OtpActionResponseDto> response = appOtpService.verifySignupOtp(request);
        log.info("Signup OTP verification processed for email: {}, status: {}", email, response.getStatusCode());
        return response;
    }

    @PostMapping("/forgot-password/send")
    public ResponseEntity<OtpActionResponseDto> sendForgotPasswordOtp(@Valid @RequestBody OtpRequestDto request) {
        String email = EmailMasker.maskEmail(request.getEmail());
        log.info("Received request to send forgot-password OTP to email: {}", email);
        ResponseEntity<OtpActionResponseDto> response = appOtpService.sendForgotPasswordOtp(request);
        log.info("Forgot-password OTP sent successfully to email: {}, status: {}", email, response.getStatusCode());
        return response;
    }

    @PostMapping("/forgot-password/verify")
    public ResponseEntity<OtpActionResponseDto> verifyForgotPasswordOtp(@Valid @RequestBody VerifyEmailOtpRequestDto request) {
        String email = EmailMasker.maskEmail(request.getEmail());
        log.info("Received request to verify forgot-password OTP for email: {}", email);
        ResponseEntity<OtpActionResponseDto> response = appOtpService.verifyForgotPasswordOtp(request);
        log.info("Forgot-password OTP verification processed for email: {}, status: {}", email, response.getStatusCode());
        return response;
    }

    @PostMapping("/forgot-password/reset")
    public ResponseEntity<OtpActionResponseDto> resetPassword(@Valid @RequestBody ResetPasswordWithOtpRequestDto request) {
        String email = EmailMasker.maskEmail(request.getEmail());
        log.info("Received password reset request for email: {}", email);
        ResponseEntity<OtpActionResponseDto> response = appOtpService.resetPassword(request);
        log.info("Password reset processed for email: {}, status: {}", email, response.getStatusCode());
        return response;
    }
}
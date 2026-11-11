package com.fitnessapp.support.controller;


import com.fitnessapp.support.models.dtos.request.EmailFromUserRequestDto;
import com.fitnessapp.support.models.dtos.response.EmailFromUserResponseDto;
import com.fitnessapp.support.models.dtos.response.SupportEmailHomeResponseDto;
import com.fitnessapp.support.service.supportEmail.SupportEmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.fitnessapp.utils.appUtil.AppUtilEndpoint.APP_SUPPORT_ENDPOINT;

@Slf4j
@RestController
@RequestMapping(APP_SUPPORT_ENDPOINT + "/email")
@RequiredArgsConstructor
public class SupportEmailController {

    private final SupportEmailService emailService;

    @GetMapping("/home")
    public ResponseEntity<SupportEmailHomeResponseDto> getEmailHome() {

        log.info("Fetching support email screen configuration");

        return ResponseEntity.ok(emailService.getEmailHome());
    }

    @PostMapping(value = "/send", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EmailFromUserResponseDto> sendEmail(
            @Valid
            @RequestPart("request")
            EmailFromUserRequestDto request,

            @RequestPart(value = "attachments", required = false)
            List<MultipartFile> attachments) {

        log.info(
                "Received support email submission. Category: {}",
                request.getCategory()
        );

        EmailFromUserResponseDto response =
                emailService.sendEmail(request, attachments);

        return ResponseEntity.accepted().body(response);
    }
}
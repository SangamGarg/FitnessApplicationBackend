package com.fitnessapp.notificationAndEmailService.controller;

import com.fitnessapp.notificationAndEmailService.models.dtos.notificationDtos.request.SendBatchNotificationRequestDto;
import com.fitnessapp.notificationAndEmailService.models.dtos.notificationDtos.request.SendNotificationRequestDto;
import com.fitnessapp.notificationAndEmailService.notificationAndEmailServiceUtilities.AppConstantsNotificationAndEmailService;
import com.fitnessapp.notificationAndEmailService.services.notificationService.NotificationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(AppConstantsNotificationAndEmailService.API_PREFIX_NOTIFICATION)
public class NotificationController {

}
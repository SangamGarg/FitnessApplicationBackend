package com.fitnessapp.notification;

import io.swagger.v3.oas.annotations.tags.Tag;
import com.fitnessapp.notificationAndEmailService.notificationAndEmailServiceUtilities.AppConstantsEmail;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(AppConstantsEmail.API_PREFIX_NOTIFICATION)
@Tag(name = "Notification", description = "Endpoints for notification")
public class NotificationController {

}

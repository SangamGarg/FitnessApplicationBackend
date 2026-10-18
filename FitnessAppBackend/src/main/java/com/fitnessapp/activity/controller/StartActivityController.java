package com.fitnessapp.activity.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import com.fitnessapp.activity.constants.AppConstantsStartActivityService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(AppConstantsStartActivityService.API_PREFIX)
@Tag(name = "Start Activity", description = "Endpoints for start activity")
public class StartActivityController {


}

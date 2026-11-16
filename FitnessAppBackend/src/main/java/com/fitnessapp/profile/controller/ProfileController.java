package com.fitnessapp.profile.controller;

import com.fitnessapp.profile.service.ProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.fitnessapp.utils.appUtil.AppUtilEndpoint.APP_PROFILE_ENDPOINT;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping(APP_PROFILE_ENDPOINT)
public class ProfileController {

    private final ProfileService profileService;

}

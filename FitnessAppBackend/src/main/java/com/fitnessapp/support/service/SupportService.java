package com.fitnessapp.support.service;

import com.fitnessapp.support.models.dtos.response.AppInfoContentResponseDto;

public interface SupportService {

    AppInfoContentResponseDto getAboutUs();

    AppInfoContentResponseDto getPrivacyPolicy();

    AppInfoContentResponseDto getTermsAndConditions();

}

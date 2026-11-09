package com.fitnessapp.support.service;

import com.fitnessapp.support.portalSupport.models.entity.AppInfoAboutUs;
import com.fitnessapp.support.portalSupport.models.entity.AppInfoPrivacyPolicy;
import com.fitnessapp.support.portalSupport.models.entity.AppInfoTermsAndCondition;
import org.springframework.http.ResponseEntity;

public interface SupportService {

    ResponseEntity<?> getAppInfo();

    AppInfoAboutUs getAboutUs();

    AppInfoPrivacyPolicy getPrivacyPolicy();

    AppInfoTermsAndCondition getTermsAndConditions();

}

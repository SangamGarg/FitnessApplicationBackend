package com.fitnessapp.support.service;

import com.fitnessapp.support.models.dtos.*;
import com.fitnessapp.support.models.dtos.response.AppInfoContentResponseDto;
import com.fitnessapp.support.models.dtos.response.SupportHomeResponseDto;
import org.springframework.data.domain.Page;

import java.util.List;

public interface SupportHelpService {


    AppInfoContentResponseDto getAboutUs();

    AppInfoContentResponseDto getPrivacyPolicy();

    AppInfoContentResponseDto getTermsAndConditions();

    SupportHomeResponseDto getHome();

    List<SupportFaqCategoryDto> getCategories();

    List<SupportFaqDto> getPopularFaqs(int limit);

    Page<SupportFaqDto> searchFaqs(String categoryKey, String query, int page, int size);

    SupportFaqDto getFaq(Long faqId);

    SupportTicketSummaryDto getTicketSummary();
}

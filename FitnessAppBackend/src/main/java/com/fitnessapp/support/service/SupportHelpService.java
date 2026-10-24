package com.fitnessapp.support.service;

import com.fitnessapp.support.models.dtos.*;
import org.springframework.data.domain.Page;

import java.util.List;

public interface SupportHelpService {
    SupportHomeResponseDto getHome(Long userId);

    List<SupportFaqCategoryDto> getCategories();

    List<SupportFaqDto> getPopularFaqs(int limit);

    Page<SupportFaqDto> searchFaqs(String categoryKey, String query, int page, int size);

    SupportFaqDto getFaq(Long faqId);

    SupportTicketSummaryDto getTicketSummary(Long userId);
}

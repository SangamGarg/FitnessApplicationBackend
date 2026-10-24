package com.fitnessapp.support.service;

import com.fitnessapp.support.models.*;
import com.fitnessapp.support.models.dtos.*;
import com.fitnessapp.support.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SupportHelpServiceImpl implements SupportHelpService {
    private final SupportFaqCategoryRepository categoryRepository;
    private final SupportFaqRepository faqRepository;
    private final SupportTicketRepository ticketRepository;

    @Override
    public SupportHomeResponseDto getHome(Long userId) {
        log.debug("Loading support home payload for userId={}", userId);
        return SupportHomeResponseDto.builder().categories(getCategories()).popularFaqs(getPopularFaqs(6)).ticketSummary(getTicketSummary(userId)).build();
    }

    @Override
    public List<SupportFaqCategoryDto> getCategories() {
        return categoryRepository.findByActiveTrueOrderBySortOrderAscNameAsc().stream().map(c -> SupportFaqCategoryDto.builder()
                .id(c.getId()).categoryKey(c.getCategoryKey()).name(c.getName()).description(c.getDescription()).iconKey(c.getIconKey()).sortOrder(c.getSortOrder()).build()).toList();
    }

    @Override
    public List<SupportFaqDto> getPopularFaqs(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 20));
        return faqRepository.findByActiveTrueAndPopularTrueOrderBySortOrderAscUpdatedAtDesc(PageRequest.of(0, safeLimit)).stream().map(this::toDto).toList();
    }

    @Override
    public Page<SupportFaqDto> searchFaqs(String categoryKey, String query, int page, int size) {
        if (page < 0 || size < 1 || size > 100)
            throw new IllegalArgumentException("Page must be >= 0 and size must be between 1 and 100");
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.asc("sortOrder"), Sort.Order.asc("question")));
        String q = query == null ? "" : query.trim();
        Page<SupportFaqEntity> result;
        if (categoryKey != null && !categoryKey.isBlank() && !q.isBlank())
            result = faqRepository.findByActiveTrueAndCategory_CategoryKeyIgnoreCaseAndQuestionContainingIgnoreCase(categoryKey.trim(), q, pageable);
        else if (categoryKey != null && !categoryKey.isBlank())
            result = faqRepository.findByActiveTrueAndCategory_CategoryKeyIgnoreCase(categoryKey.trim(), pageable);
        else if (!q.isBlank()) result = faqRepository.findByActiveTrueAndQuestionContainingIgnoreCase(q, pageable);
        else result = faqRepository.findByActiveTrue(pageable);
        return result.map(this::toDto);
    }

    @Override
    public SupportFaqDto getFaq(Long faqId) {
        return faqRepository.findByIdAndActiveTrue(faqId).map(this::toDto).orElseThrow(() -> new EntityNotFoundException("FAQ not found"));
    }

    @Override
    public SupportTicketSummaryDto getTicketSummary(Long userId) {
        return SupportTicketSummaryDto.builder().total(ticketRepository.countByUserId(userId))
                .open(ticketRepository.countByUserIdAndStatus(userId, TicketStatus.OPEN))
                .inProgress(ticketRepository.countByUserIdAndStatus(userId, TicketStatus.IN_PROGRESS))
                .resolved(ticketRepository.countByUserIdAndStatus(userId, TicketStatus.RESOLVED))
                .closed(ticketRepository.countByUserIdAndStatus(userId, TicketStatus.CLOSED)).build();
    }

    private SupportFaqDto toDto(SupportFaqEntity f) {
        return SupportFaqDto.builder().id(f.getId()).categoryKey(f.getCategory().getCategoryKey()).categoryName(f.getCategory().getName())
                .question(f.getQuestion()).answer(f.getAnswer()).popular(f.isPopular()).updatedAt(f.getUpdatedAt()).build();
    }
}

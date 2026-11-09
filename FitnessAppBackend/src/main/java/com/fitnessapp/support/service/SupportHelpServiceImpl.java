package com.fitnessapp.support.service;

import com.fitnessapp.support.models.dtos.SupportFaqCategoryDto;
import com.fitnessapp.support.models.dtos.SupportFaqDto;
import com.fitnessapp.support.models.dtos.SupportTicketSummaryDto;
import com.fitnessapp.support.models.dtos.response.SupportHomeResponseDto;
import com.fitnessapp.support.models.entity.SupportFaqEntity;
import com.fitnessapp.support.models.enums.TicketStatus;
import com.fitnessapp.support.repo.SupportFaqCategoryRepository;
import com.fitnessapp.support.repo.SupportFaqRepository;
import com.fitnessapp.support.repo.SupportTicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SupportHelpServiceImpl implements SupportHelpService {

    private static final int MAX_FAQ_LIMIT = 20;
    private static final int DEFAULT_POPULAR_FAQ_LIMIT = 6;
    private static final int MAX_PAGE_SIZE = 100;

    private final SupportFaqCategoryRepository categoryRepository;
    private final SupportFaqRepository faqRepository;
    private final SupportTicketRepository ticketRepository;

    @Value("${version.app}")
    private String appVersion;
    @Value("${builds.app}")
    private String appBuild;
    @Value("${footer.app}")
    private String footer;

    @Override
    public SupportHomeResponseDto getHome() {

        String userId = currentUserId();

        log.debug("Loading support home payload for userId={}", userId);

        SupportHomeResponseDto.SupportHomeResponseDtoBuilder builder = SupportHomeResponseDto.builder()
                .categories(getCategories())
                .popularFaqs(getPopularFaqs(DEFAULT_POPULAR_FAQ_LIMIT))
                .ticketSummary(buildTicketSummary(userId))
                .appVersion(appVersion)
                .appBuild(appBuild).footer(footer);

        return builder.build();
    }

    @Override
    public List<SupportFaqCategoryDto> getCategories() {

        return categoryRepository
                .findByActiveTrueOrderBySortOrderAscNameAsc()
                .stream()
                .map(category -> SupportFaqCategoryDto.builder()
                        .id(category.getId())
                        .categoryKey(category.getCategoryKey())
                        .name(category.getName())
                        .description(category.getDescription())
                        .iconKey(category.getIconKey())
                        .sortOrder(category.getSortOrder())
                        .build())
                .toList();
    }

    @Override
    public List<SupportFaqDto> getPopularFaqs(int limit) {

        if (limit < 1 || limit > MAX_FAQ_LIMIT) {
            throw new IllegalArgumentException(
                    "FAQ limit must be between 1 and " + MAX_FAQ_LIMIT
            );
        }

        Pageable pageable = PageRequest.of(
                0,
                limit,
                Sort.by(
                        Sort.Order.asc("sortOrder"),
                        Sort.Order.desc("updatedAt")
                )
        );

        return faqRepository
                .findByActiveTrueAndPopularTrueOrderBySortOrderAscUpdatedAtDesc(pageable)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public Page<SupportFaqDto> searchFaqs(
            String categoryKey,
            String query,
            int page,
            int size
    ) {

        validatePagination(page, size);

        String normalizedCategory = normalize(categoryKey);
        String normalizedQuery = normalize(query);

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(
                        Sort.Order.asc("sortOrder"),
                        Sort.Order.asc("question")
                )
        );

        Page<SupportFaqEntity> result;

        if (normalizedCategory != null && normalizedQuery != null) {

            result = faqRepository
                    .findByActiveTrueAndCategory_CategoryKeyIgnoreCaseAndQuestionContainingIgnoreCase(
                            normalizedCategory,
                            normalizedQuery,
                            pageable
                    );

        } else if (normalizedCategory != null) {

            result = faqRepository
                    .findByActiveTrueAndCategory_CategoryKeyIgnoreCase(
                            normalizedCategory,
                            pageable
                    );

        } else if (normalizedQuery != null) {

            result = faqRepository
                    .findByActiveTrueAndQuestionContainingIgnoreCase(
                            normalizedQuery,
                            pageable
                    );

        } else {

            result = faqRepository.findByActiveTrue(pageable);
        }

        return result.map(this::toDto);
    }

    @Override
    public SupportFaqDto getFaq(Long faqId) {

        if (faqId == null || faqId <= 0) {
            throw new IllegalArgumentException("Invalid FAQ ID");
        }

        SupportFaqEntity faq = faqRepository
                .findByIdAndActiveTrue(faqId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "FAQ not found"
                ));

        return toDto(faq);
    }

    @Override
    public SupportTicketSummaryDto getTicketSummary() {

        String userId = currentUserId();

        return buildTicketSummary(userId);
    }

    private SupportTicketSummaryDto buildTicketSummary(String userId) {

        return SupportTicketSummaryDto.builder()
                .total(ticketRepository.countByUserId(userId))
                .open(ticketRepository.countByUserIdAndStatus(
                        userId, TicketStatus.OPEN
                ))
                .inProgress(ticketRepository.countByUserIdAndStatus(
                        userId, TicketStatus.IN_PROGRESS
                ))
                .resolved(ticketRepository.countByUserIdAndStatus(
                        userId, TicketStatus.RESOLVED
                ))
                .closed(ticketRepository.countByUserIdAndStatus(
                        userId, TicketStatus.CLOSED
                ))
                .build();
    }

    private SupportFaqDto toDto(SupportFaqEntity faq) {

        return SupportFaqDto.builder()
                .id(faq.getId())
                .categoryKey(faq.getCategory().getCategoryKey())
                .categoryName(faq.getCategory().getName())
                .question(faq.getQuestion())
                .answer(faq.getAnswer())
                .popular(faq.isPopular())
                .updatedAt(faq.getUpdatedAt())
                .build();
    }

    private String currentUserId() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null
                || authentication.getName().isBlank()
                || "anonymousUser".equals(authentication.getName())) {

            log.warn(
                    "Support request rejected: authenticated user not found"
            );

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authentication required"
            );
        }

        return authentication.getName();
    }

    private void validatePagination(int page, int size) {

        if (page < 0 || size < 1 || size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException(
                    "Page must be >= 0 and size must be between 1 and "
                            + MAX_PAGE_SIZE
            );
        }
    }

    private String normalize(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}
package com.fitnessapp.support.models.dtos.response;

import com.fitnessapp.support.models.dtos.SupportFaqCategoryDto;
import com.fitnessapp.support.models.dtos.SupportFaqDto;
import com.fitnessapp.support.models.dtos.SupportTicketSummaryDto;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupportHomeResponseDto {
    private List<SupportFaqCategoryDto> categories;
    private List<SupportFaqDto> popularFaqs;
    private SupportTicketSummaryDto ticketSummary;

    // Added App Information for the home screen
    private AppInfoBasicDto appInfo;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AppInfoBasicDto {
        private String appVersion;
        private String aboutUsSnippet;
        private LocalDateTime updatedAt;
    }
}

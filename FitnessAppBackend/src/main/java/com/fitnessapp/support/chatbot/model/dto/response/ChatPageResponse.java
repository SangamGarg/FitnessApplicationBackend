package com.fitnessapp.support.chatbot.model.dto.response;

import org.springframework.data.domain.Page;

import java.util.List;

public record ChatPageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {
    public static <T> ChatPageResponse<T> from(Page<T> result) {
        return new ChatPageResponse<>(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.isFirst(),
                result.isLast()
        );
    }
}

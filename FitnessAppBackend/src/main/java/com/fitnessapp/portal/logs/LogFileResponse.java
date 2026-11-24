package com.fitnessapp.portal.logs;

import java.util.List;

public record LogFileResponse(
        String type,
        String fileName,
        List<String> lines,
        int returnedLines,
        int requestedLines,
        boolean available,
        String requestId
) {
}

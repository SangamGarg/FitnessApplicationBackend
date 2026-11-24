package com.fitnessapp.portal.logs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class PortalLogServiceImpl implements PortalLogService {
    private static final int MAX_LINES = 1000;
    private static final Map<String, String> LOG_FILES = Map.of(
            "application", "fitness-app.log",
            "errors", "fitness-app-error.log",
            "access", "fitness-app-access.log"
    );

    @Value("${app.log.dir:./logs}")
    private String logDirectory;

    @Override
    public List<String> getAvailableLogs() {
        Path directory = Path.of(logDirectory).toAbsolutePath().normalize();
        List<String> available = new ArrayList<>();
        LOG_FILES.forEach((type, name) -> {
            if (Files.isRegularFile(directory.resolve(name))) available.add(type);
        });
        return available;
    }

    @Override
    public LogFileResponse getLogs(String type, int requestedLines, String requestId) {
        String fileName = LOG_FILES.get(type);
        if (fileName == null) throw new IllegalArgumentException("Unknown log type: " + type);
        if (requestId != null && (requestId.isBlank() || requestId.length() > 128
                || requestId.contains("[") || requestId.contains("]") || requestId.contains("\r") || requestId.contains("\n"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "requestId must be a non-empty ID of at most 128 characters");
        }
        int linesToRead = Math.max(1, Math.min(requestedLines, MAX_LINES));
        Path file = Path.of(logDirectory).toAbsolutePath().normalize().resolve(fileName);
        boolean exists = Files.isRegularFile(file);
        List<String> contents = exists ? readLastLines(file, linesToRead, requestId) : List.of();
        return new LogFileResponse(type, fileName, contents, contents.size(), linesToRead, exists, requestId);
    }

    private List<String> readLastLines(Path file, int limit, String requestId) {
        ArrayDeque<String> tail = new ArrayDeque<>(limit);
        String correlationToken = requestId == null ? null : "[req:" + requestId + "]";
        try (RandomAccessFile input = new RandomAccessFile(file.toFile(), "r")) {
            long position = input.length();
            StringBuilder reversedLine = new StringBuilder();
            while (position > 0 && (requestId != null || tail.size() < limit)) {
                input.seek(--position);
                int value = input.read();
                if (value == '\n') {
                    if (!reversedLine.isEmpty()) {
                        addMatchingLine(tail, decodeLine(reversedLine), correlationToken, limit);
                        reversedLine.setLength(0);
                    }
                } else if (value != '\r') {
                    reversedLine.append((char) (value & 0xff));
                }
            }
            if (!reversedLine.isEmpty()) {
                addMatchingLine(tail, decodeLine(reversedLine), correlationToken, limit);
            }
        } catch (IOException exception) {
            log.warn("Unable to read application log file {}", file, exception);
            throw new IllegalStateException("Unable to read requested log file", exception);
        }
        return List.copyOf(tail);
    }

    private void addMatchingLine(ArrayDeque<String> lines, String line, String correlationToken, int limit) {
        if ((correlationToken == null || line.contains(correlationToken)) && lines.size() < limit) {
            lines.addFirst(line);
        }
    }

    private String decodeLine(StringBuilder reversedBytes) {
        byte[] bytes = new byte[reversedBytes.length()];
        for (int index = 0; index < reversedBytes.length(); index++) {
            bytes[index] = (byte) reversedBytes.charAt(reversedBytes.length() - 1 - index);
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }
}

package com.fitnessapp.portal.logs;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.fitnessapp.portal.logs.LogFileResponse;

import static com.fitnessapp.utils.portalUtil.PortalUtil.PORTAL_DEV_LOGS_ENDPOINT;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(PORTAL_DEV_LOGS_ENDPOINT)
public class PortalLogsController {
    private final PortalLogService portalLogService;

    @GetMapping
    public java.util.List<String> getAvailableLogs() {
        return portalLogService.getAvailableLogs();
    }

    @GetMapping("/application")
    public LogFileResponse getApplicationLogs(@RequestParam(defaultValue = "200") int lines,
                                              @RequestParam(required = false) String requestId) {
        return portalLogService.getLogs("application", lines, requestId);
    }

    @GetMapping("/errors")
    public LogFileResponse getErrorLogs(@RequestParam(defaultValue = "200") int lines,
                                        @RequestParam(required = false) String requestId) {
        return portalLogService.getLogs("errors", lines, requestId);
    }

    @GetMapping("/access")
    public LogFileResponse getAccessLogs(@RequestParam(defaultValue = "200") int lines,
                                         @RequestParam(required = false) String requestId) {
        return portalLogService.getLogs("access", lines, requestId);
    }
}

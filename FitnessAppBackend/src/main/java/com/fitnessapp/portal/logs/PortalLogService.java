package com.fitnessapp.portal.logs;

import java.util.List;

public interface PortalLogService
{
    List<String> getAvailableLogs();
    LogFileResponse getLogs(String type, int lines, String requestId);
}

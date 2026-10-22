package com.fitnessapp.support.models;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceDiagnosticInfo {
    private String appVersion;
    private String osVersion;
    private String deviceModel;
    private String manufacturer;
    private Boolean isBatteryOptimizationIgnored;
    private Boolean isGoogleFitLinked;
}
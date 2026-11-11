package com.fitnessapp.support.models.dtos;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceDiagnosticInfo {
  //  @NotBlank
    @Size(max = 50)
    private String appVersion;
   // @NotBlank
    @Size(max = 50)
    private String osVersion;
  //  @NotBlank
    @Size(max = 100)
    private String deviceModel;
    @Size(max = 50)
    private String manufacturer;
    private Boolean isBatteryOptimizationIgnored;
    private Boolean isGoogleFitLinked;
    @Size(max = 50)
    private String deviceType;
}
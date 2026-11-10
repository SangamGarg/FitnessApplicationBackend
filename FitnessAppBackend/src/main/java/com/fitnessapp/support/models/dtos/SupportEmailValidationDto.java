package com.fitnessapp.support.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupportEmailValidationDto {

    private int subjectMinLength;
    private int subjectMaxLength;

    private int messageMinLength;
    private int messageMaxLength;

    private int maxAttachments;
    private long maxAttachmentSizeBytes;
    private long maxTotalAttachmentSizeBytes;

    private List<String> allowedMimeTypes;
}
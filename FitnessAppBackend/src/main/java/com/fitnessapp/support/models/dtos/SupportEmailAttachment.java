package com.fitnessapp.support.models.dtos;

public record SupportEmailAttachment(
        String filename,
        String contentType,
        byte[] content
) {
}
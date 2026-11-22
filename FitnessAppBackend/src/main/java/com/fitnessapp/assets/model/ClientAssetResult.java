package com.fitnessapp.assets.model;

public record ClientAssetResult<T>(T body, String etag) {
}

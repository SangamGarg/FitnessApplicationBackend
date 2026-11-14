package com.fitnessapp.nutrition.model.dto.response;
import java.util.List;
public record MealSyncResponse(int received, int created, int alreadySynced,
                               int rejected, List<MealEntryResponse> meals) {}

package com.fitnessapp.nutrition.service;

import com.fitnessapp.nutrition.dto.request.MealSyncRequest;
import com.fitnessapp.nutrition.dto.request.LogMealRequest;
import com.fitnessapp.nutrition.dto.response.MealEntryResponse;
import com.fitnessapp.nutrition.dto.response.MealSyncResponse;
import com.fitnessapp.nutrition.repository.MealEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NutritionMealSyncService {
    private final NutritionService nutritionService;
    private final MealEntryRepository mealRepository;

    @Transactional
    public MealSyncResponse sync(String userId, MealSyncRequest request) {
        List<MealEntryResponse> result = new ArrayList<>();
        int created=0, existing=0;
        for (LogMealRequest meal : request.meals()) {
            if (meal.clientMealId()==null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Every synced meal must include clientMealId");
            }
            boolean existed = mealRepository.findByUser_IdAndClientMealId(userId,meal.clientMealId()).isPresent();
            MealEntryResponse saved = nutritionService.logMeal(userId,meal);
            if (existed) existing++; else created++;
            result.add(saved);
        }
        return new MealSyncResponse(request.meals().size(),created,existing,0,result);
    }
}

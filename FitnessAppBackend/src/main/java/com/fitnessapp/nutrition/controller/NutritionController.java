package com.fitnessapp.nutrition.controller;

import com.fitnessapp.nutrition.dto.request.*;
import com.fitnessapp.nutrition.dto.response.*;
import com.fitnessapp.nutrition.service.NutritionService;
import com.fitnessapp.nutrition.service.NutritionMealSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.UUID;

import static com.fitnessapp.utils.appUtil.AppUtilEndpoint.APP_NUTRITION_ENDPOINT;

@RestController
@RequestMapping(APP_NUTRITION_ENDPOINT)
@RequiredArgsConstructor
@Tag(name = "Nutrition", description = "Food catalogue, meal logging and daily nutrition tracking")
@SecurityRequirement(name = "bearerAuth")
public class NutritionController {
    private final NutritionService nutritionService;
    private final NutritionMealSyncService mealSyncService;

    @GetMapping("/foods")
    @Operation(summary = "Search global and user-owned food items")
    public ResponseEntity<Page<FoodItemResponse>> searchFoods(Authentication auth,
        @RequestParam(required=false) String query,@RequestParam(defaultValue="0") int page,
        @RequestParam(defaultValue="20") int size) {
        return ResponseEntity.ok(nutritionService.searchFoods(auth.getName(),query,page,size));
    }
    @PostMapping("/foods/custom")
    public ResponseEntity<FoodItemResponse> createCustomFood(Authentication auth,
        @Valid @RequestBody CreateCustomFoodRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(nutritionService.createCustomFood(auth.getName(),request));
    }
    @PostMapping("/meals")
    public ResponseEntity<MealEntryResponse> logMeal(Authentication auth,
        @Valid @RequestBody LogMealRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(nutritionService.logMeal(auth.getName(),request));
    }
    @PutMapping("/meals/{mealId}")
    public ResponseEntity<MealEntryResponse> updateMeal(Authentication auth,@PathVariable UUID mealId,
        @Valid @RequestBody UpdateMealRequest request) {
        return ResponseEntity.ok(nutritionService.updateMeal(auth.getName(),mealId,request));
    }
    @DeleteMapping("/meals/{mealId}")
    public ResponseEntity<Void> deleteMeal(Authentication auth,@PathVariable UUID mealId) {
        nutritionService.deleteMeal(auth.getName(),mealId); return ResponseEntity.noContent().build();
    }
    @PostMapping("/meals/sync")
    public ResponseEntity<MealSyncResponse> syncMeals(Authentication auth,
        @Valid @RequestBody MealSyncRequest request) {
        return ResponseEntity.ok(mealSyncService.sync(auth.getName(),request));
    }
    @GetMapping("/meals/history")
    public ResponseEntity<Page<MealEntryResponse>> history(Authentication auth,
        @RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size) {
        return ResponseEntity.ok(nutritionService.getMealHistory(auth.getName(),page,size));
    }
    @GetMapping("/summary")
    public ResponseEntity<DailyNutritionSummaryResponse> summary(Authentication auth,
        @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(nutritionService.getDailySummary(auth.getName(),date));
    }
    @GetMapping("/goals")
    public ResponseEntity<NutritionGoalResponse> goals(Authentication auth) {
        NutritionGoalResponse result=nutritionService.getGoals(auth.getName());
        return result==null?ResponseEntity.noContent().build():ResponseEntity.ok(result);
    }
    @PutMapping("/goals")
    public ResponseEntity<NutritionGoalResponse> updateGoals(Authentication auth,
        @Valid @RequestBody UpdateNutritionGoalRequest request) {
        return ResponseEntity.ok(nutritionService.updateGoals(auth.getName(),request));
    }
}

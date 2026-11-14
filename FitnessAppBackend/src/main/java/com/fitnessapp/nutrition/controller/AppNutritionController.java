package com.fitnessapp.nutrition.controller;

import com.fitnessapp.nutrition.model.dto.request.*;
import com.fitnessapp.nutrition.model.dto.response.*;
import com.fitnessapp.nutrition.model.enums.DietaryPreference;
import com.fitnessapp.nutrition.model.enums.MealType;
import com.fitnessapp.nutrition.service.NutritionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static com.fitnessapp.utils.appUtil.AppUtilEndpoint.APP_NUTRITION_ENDPOINT;

@RestController
@RequestMapping(APP_NUTRITION_ENDPOINT)
@RequiredArgsConstructor
public class AppNutritionController {
    private final NutritionService nutritionService;


    @GetMapping("/home")
    //To get the food nutrition info
    public ResponseEntity<NutritionHomeResponse> getNutritionHome() {
        return ResponseEntity.ok(nutritionService.getNutritionHome());
    }
    @GetMapping("/summary")
    public ResponseEntity<DailyNutritionSummaryResponse> summary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(nutritionService.getDailySummary(date));
    }

    @GetMapping("/foods")
    //To get the food nutrition info
    public ResponseEntity<Page<FoodItemResponse>> searchFoods(
            @RequestParam(required = false) String query, @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(nutritionService.searchFoods(query, page, size));
    }

    @GetMapping("/preferences")
    public ResponseEntity<DietPreferencesResponse> preferences() {
        return ResponseEntity.ok(nutritionService.getPreferences());
    }

    @PutMapping("/preferences")
    public ResponseEntity<DietPreferencesResponse> updatePreferences(
            @Valid @RequestBody UpdateDietPreferencesRequest request) {
        return ResponseEntity.ok(nutritionService.updatePreferences(request));
    }


    @GetMapping("/recipes")
    public ResponseEntity<Page<RecipeResponse>> recipes(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) DietaryPreference dietaryPreference,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(nutritionService.searchRecipes(query, dietaryPreference, page, size));
    }

    @GetMapping("/recipes/{recipeId}")
    public ResponseEntity<RecipeResponse> recipe(@PathVariable UUID recipeId) {
        return ResponseEntity.ok(nutritionService.getRecipe(recipeId));
    }

    @PostMapping("/recipes/generate")
    public ResponseEntity<List<RecipeResponse>> generateRecipes(@Valid @RequestBody AiRecipeRequest request) {
        return ResponseEntity.ok(nutritionService.generateRecipes(request));
    }


    @PostMapping("/recipes/{recipeId}/log")
    public ResponseEntity<MealEntryResponse> logRecipe(@PathVariable UUID recipeId,
                                                       @RequestParam MealType mealType,
                                                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(nutritionService.logRecipe(recipeId, mealType, date));
    }


    // The below two has to be done at client side using ML Vision
    @PostMapping("/ingredients/normalize")
    public ResponseEntity<IngredientAnalysisResponse> normalizeIngredients(
            @Valid @RequestBody IngredientAnalysisRequest request) {
        return ResponseEntity.ok(nutritionService.normalizeIngredients(request));
    }

    @PostMapping(value = "/ingredients/analyze-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageIngredientAnalysisResponse> analyzeIngredientImage(
            @RequestPart("image") MultipartFile image,
            @RequestPart(value = "notes", required = false) String notes) throws IOException {
        if (image.isEmpty() || image.getSize() > 5L * 1024 * 1024)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image must be between 1 byte and 5 MB.");
        String contentType = image.getContentType();
        if (contentType == null || !List.of("image/jpeg", "image/png", "image/webp", "image/jpg").contains(contentType))
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Only JPEG, PNG and WEBP images are supported.");
        return ResponseEntity.ok(nutritionService.analyzeIngredientImage(image.getBytes(), contentType, notes));
    }
    // The above two has to be done at client side using ML Vision


    @PostMapping("/meals")
    public ResponseEntity<MealEntryResponse> logMeal(
            @Valid @RequestBody LogMealRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(nutritionService.logMeal(request));
    }

    @PutMapping("/meals/{mealId}")
    public ResponseEntity<MealEntryResponse> updateMeal(@PathVariable UUID mealId,
                                                        @Valid @RequestBody UpdateMealRequest request) {
        return ResponseEntity.ok(nutritionService.updateMeal(mealId, request));
    }

    @DeleteMapping("/meals/{mealId}")
    public ResponseEntity<Void> deleteMeal(@PathVariable UUID mealId) {
        nutritionService.deleteMeal(mealId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/meals/sync")
    public ResponseEntity<MealSyncResponse> syncMeals(
            @Valid @RequestBody MealSyncRequest request) {
        return ResponseEntity.ok(nutritionService.sync(request));
    }

    @GetMapping("/meals/history")
    public ResponseEntity<Page<MealEntryResponse>> history(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(nutritionService.getMealHistory(page, size));
    }


    @GetMapping("/goals")
    public ResponseEntity<NutritionGoalResponse> goals() {
        NutritionGoalResponse result = nutritionService.getGoals();
        return result == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(result);
    }

    @PutMapping("/goals")
    public ResponseEntity<NutritionGoalResponse> updateGoals(
            @Valid @RequestBody UpdateNutritionGoalRequest request) {
        return ResponseEntity.ok(nutritionService.updateGoals(request));
    }

}

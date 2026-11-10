package com.fitnessapp.nutrition.controller;

import com.fitnessapp.nutrition.dto.request.AiRecipeRequest;
import com.fitnessapp.nutrition.dto.request.IngredientAnalysisRequest;
import com.fitnessapp.nutrition.dto.request.RecipeUpsertRequest;
import com.fitnessapp.nutrition.dto.request.UpdateDietPreferencesRequest;
import com.fitnessapp.nutrition.dto.response.*;
import com.fitnessapp.nutrition.model.enums.DietaryPreference;
import com.fitnessapp.nutrition.model.enums.MealType;
import com.fitnessapp.nutrition.service.NutritionAdvancedService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static com.fitnessapp.utils.appUtil.AppUtilEndpoint.APP_NUTRITION_ENDPOINT;

@RestController
@RequestMapping(APP_NUTRITION_ENDPOINT)
@RequiredArgsConstructor
@Tag(name = "Nutrition - Recipes & Preferences")
@SecurityRequirement(name = "bearerAuth")
public class NutritionAdvancedController {
    private final NutritionAdvancedService service;

    @GetMapping("/preferences")
    public ResponseEntity<DietPreferencesResponse> preferences(Authentication auth) {
        return ResponseEntity.ok(service.getPreferences(auth.getName()));
    }

    @PutMapping("/preferences")
    public ResponseEntity<DietPreferencesResponse> updatePreferences(Authentication auth,
                                                                     @Valid @RequestBody UpdateDietPreferencesRequest request) {
        return ResponseEntity.ok(service.updatePreferences(auth.getName(), request));
    }

    @GetMapping("/recipes")
    public ResponseEntity<Page<RecipeResponse>> recipes(Authentication auth,
                                                        @RequestParam(required = false) String query,
                                                        @RequestParam(required = false) DietaryPreference dietaryPreference,
                                                        @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(service.searchRecipes(auth.getName(), query, dietaryPreference, page, size));
    }

    @GetMapping("/recipes/{recipeId}")
    public ResponseEntity<RecipeResponse> recipe(Authentication auth, @PathVariable UUID recipeId) {
        return ResponseEntity.ok(service.getRecipe(auth.getName(), recipeId));
    }

    @PostMapping("/portal/recipes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RecipeResponse> createRecipe(@Valid @RequestBody RecipeUpsertRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createRecipe(request));
    }

    @PostMapping("/recipes/{recipeId}/log")
    public ResponseEntity<MealEntryResponse> logRecipe(Authentication auth, @PathVariable UUID recipeId,
                                                       @RequestParam MealType mealType,
                                                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(service.logRecipe(auth.getName(), recipeId, mealType, date));
    }

    @PostMapping(value = "/ingredients/analyze-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageIngredientAnalysisResponse> analyzeIngredientImage(
            @RequestPart("image") MultipartFile image,
            @RequestPart(value = "notes", required = false) String notes) throws java.io.IOException {
        if (image.isEmpty() || image.getSize() > 5L * 1024 * 1024)
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.BAD_REQUEST, "Image must be between 1 byte and 5 MB.");
        String contentType = image.getContentType();
        if (contentType == null || !List.of("image/jpeg", "image/png", "image/webp").contains(contentType))
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Only JPEG, PNG and WEBP images are supported.");
        return ResponseEntity.ok(service.analyzeIngredientImage(image.getBytes(), contentType, notes));
    }

    @PostMapping("/ingredients/normalize")
    public ResponseEntity<IngredientAnalysisResponse> normalizeIngredients(
            @Valid @RequestBody IngredientAnalysisRequest request) {
        return ResponseEntity.ok(service.normalizeIngredients(request));
    }

    @PostMapping("/recipes/generate")
    public ResponseEntity<List<RecipeResponse>> generateRecipes(Authentication auth, @Valid @RequestBody AiRecipeRequest request) {
        return ResponseEntity.ok(service.generateRecipes(auth.getName(), request));
    }
}

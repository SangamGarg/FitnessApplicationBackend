package com.fitnessapp.nutrition.controller;


import com.fitnessapp.nutrition.model.dto.request.CreateCustomFoodRequest;
import com.fitnessapp.nutrition.model.dto.request.AiRecipeRequest;
import com.fitnessapp.nutrition.model.dto.request.RecipeUpsertRequest;
import com.fitnessapp.nutrition.model.dto.response.FoodItemResponse;
import com.fitnessapp.nutrition.model.dto.response.RecipeResponse;
import com.fitnessapp.nutrition.model.enums.DietaryPreference;
import com.fitnessapp.nutrition.service.PortalNutritionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import static com.fitnessapp.utils.portalUtil.PortalUtil.PORTAL_DEV_NUTRITION_ENDPOINT;

@RestController
@RequiredArgsConstructor
@RequestMapping(PORTAL_DEV_NUTRITION_ENDPOINT)
@Slf4j
public class PortalNutritionController {

    private final PortalNutritionService service;

    @GetMapping("/foods")
    public ResponseEntity<Page<FoodItemResponse>> searchFoods(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(service.searchFoods(query, page, size));
    }


    @PostMapping(value = "/foods", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FoodItemResponse> createCustomFoodWithImage(
            @Valid @RequestPart("request") CreateCustomFoodRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.createCustomFood(request, image));
    }

    @PutMapping(value = "/foods/{foodId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FoodItemResponse> updateCustomFood(
            @PathVariable UUID foodId,
            @Valid @RequestPart("request") CreateCustomFoodRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        return ResponseEntity.ok(service.updateCustomFood(foodId, request, image));
    }

    @DeleteMapping("/foods/{foodId}")
    public ResponseEntity<Void> deleteCustomFood(@PathVariable UUID foodId) {
        service.deleteCustomFood(foodId);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/recipes")
    public ResponseEntity<Page<RecipeResponse>> searchRecipes(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) DietaryPreference dietaryPreference,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(service.searchRecipes(query, dietaryPreference, page, size));
    }

    @GetMapping("/recipes/{recipeId}")
    public ResponseEntity<RecipeResponse> getRecipe(@PathVariable UUID recipeId) {
        return ResponseEntity.ok(service.getRecipe(recipeId));
    }

    @PostMapping("/recipes/generate")
    public ResponseEntity<List<RecipeResponse>> generateRecipes(
            @Valid @RequestBody AiRecipeRequest request) {
        return ResponseEntity.ok(service.generateRecipes(request));
    }

    @PostMapping(value = "/recipes", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RecipeResponse> createRecipeWithImage(
            @Valid @RequestPart("request") RecipeUpsertRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createRecipe(request, image));
    }

    @PutMapping(value = "/recipes/{recipeId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RecipeResponse> updateRecipe(
            @PathVariable UUID recipeId,
            @Valid @RequestPart("request") RecipeUpsertRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        return ResponseEntity.ok(service.updateRecipe(recipeId, request, image));
    }

    @DeleteMapping("/recipes/{recipeId}")
    public ResponseEntity<Void> deleteRecipe(@PathVariable UUID recipeId) {
        service.deleteRecipe(recipeId);
        return ResponseEntity.noContent().build();
    }

}

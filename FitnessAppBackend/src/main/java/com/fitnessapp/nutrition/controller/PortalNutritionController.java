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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    @PostMapping("/foods")
    public ResponseEntity<FoodItemResponse> createCustomFood(
            @Valid @RequestBody CreateCustomFoodRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.createCustomFood(request));
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



    @PostMapping("/recipes")
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RecipeResponse> createRecipe(@Valid @RequestBody RecipeUpsertRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createRecipe(request));
    }

}

package com.fitnessapp.nutrition.controller;


import com.fitnessapp.nutrition.model.dto.request.CreateCustomFoodRequest;
import com.fitnessapp.nutrition.model.dto.request.RecipeUpsertRequest;
import com.fitnessapp.nutrition.model.dto.response.FoodItemResponse;
import com.fitnessapp.nutrition.model.dto.response.RecipeResponse;
import com.fitnessapp.nutrition.service.PortalNutritionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.fitnessapp.utils.portalUtil.PortalUtil.PORTAL_NUTRITION_ENDPOINT;

@RestController
@RequiredArgsConstructor
@RequestMapping(PORTAL_NUTRITION_ENDPOINT)
@Slf4j
public class PortalNutritionController {

    private final PortalNutritionService service;


    @PostMapping("/foods")
    public ResponseEntity<FoodItemResponse> createCustomFood(
            @Valid @RequestBody CreateCustomFoodRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.createCustomFood(request));
    }

    @PostMapping("/recipes")
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RecipeResponse> createRecipe(@Valid @RequestBody RecipeUpsertRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createRecipe(request));
    }

}

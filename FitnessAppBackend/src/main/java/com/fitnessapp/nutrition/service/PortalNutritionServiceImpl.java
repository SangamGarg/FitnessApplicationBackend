package com.fitnessapp.nutrition.service;

import com.fitnessapp.appAuth.model.entity.UserEntity;
import com.fitnessapp.appAuth.repo.UserRepo;
import com.fitnessapp.nutrition.model.dto.request.CreateCustomFoodRequest;
import com.fitnessapp.nutrition.model.dto.request.RecipeUpsertRequest;
import com.fitnessapp.nutrition.model.dto.response.FoodItemResponse;
import com.fitnessapp.nutrition.model.dto.response.RecipeResponse;
import com.fitnessapp.nutrition.model.entity.FoodItemEntity;
import com.fitnessapp.nutrition.model.entity.RecipeEntity;
import com.fitnessapp.nutrition.repository.FoodItemRepository;
import com.fitnessapp.nutrition.repository.RecipeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PortalNutritionServiceImpl implements PortalNutritionService {

    private final RecipeRepository recipeRepo;
    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    private final FoodItemRepository foodRepository;
    private final UserRepo userRepository;

    @Override
    @Transactional
    public FoodItemResponse createCustomFood(CreateCustomFoodRequest request) {
        String userId = currentUserId();

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User account not found"));
        FoodItemEntity food = new FoodItemEntity();
        food.setOwner(user);
        food.setName(request.name().trim());
        food.setBrand(clean(request.brand()));
        food.setServingLabel(request.servingLabel().trim());
        food.setServingSizeGrams(request.servingSizeGrams());
        food.setCalories(request.calories());
        food.setProtein(request.protein());
        food.setCarbohydrates(request.carbohydrates());
        food.setFat(request.fat());
        food.setFiber(nvl(request.fiber()));
        food.setSugar(nvl(request.sugar()));
        food.setVerified(false);
        food.setActive(true);
        return toFoodResponse(foodRepository.save(food));
    }

    // Portal/admin authorization is applied by the controller, not by trusting a client-supplied owner.
    @Override
    @Transactional
    public RecipeResponse createRecipe(RecipeUpsertRequest r) {
        RecipeEntity e = new RecipeEntity();
        applyRecipe(e, r);
        e.setAiGenerated(false);
        return toRecipe(recipeRepo.save(e));
    }

    private FoodItemResponse toFoodResponse(FoodItemEntity f) {
        return new FoodItemResponse(f.getId(), f.getName(), f.getBrand(), f.getServingLabel(),
                f.getServingSizeGrams(), f.getCalories(), f.getProtein(), f.getCarbohydrates(),
                f.getFat(), nvl(f.getFiber()), nvl(f.getSugar()), f.isVerified(), f.getOwner() != null);
    }

    private RecipeResponse toRecipe(RecipeEntity r) {
        return new RecipeResponse(r.getId(), r.getTitle(), r.getDescription(), r.getPrepTimeMinutes(),
                r.getDifficulty(), r.getDietType(), r.getServings(), r.getCalories(), r.getProtein(),
                r.getCarbohydrates(), r.getFat(), r.getFiber(), r.getSugar(), r.getImageUrl(),
                List.copyOf(r.getIngredients()), List.copyOf(r.getInstructions()), r.isAiGenerated(),
                r.isAiGenerated() ? "AI_ESTIMATE_REVIEW_REQUIRED" : "ADMIN_ENTERED_UNVERIFIED",
                "Nutrition values are estimates and are not laboratory-verified. Check portions, ingredients and allergens before use.");
    }

    private BigDecimal nvl(BigDecimal value) {
        return value == null ? ZERO : value;
    }

    private void applyRecipe(RecipeEntity e, RecipeUpsertRequest r) {
        e.setTitle(r.title().trim());
        e.setDescription(clean(r.description()));
        e.setPrepTimeMinutes(r.prepTimeMinutes());
        e.setDifficulty(r.difficulty());
        e.setDietType(r.dietType());
        e.setServings(r.servings());
        e.setCalories(r.calories());
        e.setProtein(r.protein());
        e.setCarbohydrates(r.carbohydrates());
        e.setFat(r.fat());
        e.setFiber(r.fiber() == null ? BigDecimal.ZERO : r.fiber());
        e.setSugar(r.sugar() == null ? BigDecimal.ZERO : r.sugar());
        e.setImageUrl(clean(r.imageUrl()));
        e.setIngredients(r.ingredients());
        e.setInstructions(r.instructions());
        e.setActive(true);
    }


    private String clean(String s) {
        return StringUtils.hasText(s) ? s.trim() : null;
    }

    private String currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getName() == null || "anonymousUser".equals(authentication.getName())) {
            log.warn("Unauthorized access attempt without valid authentication principal in SecurityContext");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return authentication.getName();
    }

}

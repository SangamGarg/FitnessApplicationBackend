package com.fitnessapp.nutrition.service;

import com.fitnessapp.assets.service.FileStorageService;
import com.fitnessapp.nutrition.model.dto.request.AiRecipeRequest;
import com.fitnessapp.nutrition.model.dto.request.CreateCustomFoodRequest;
import com.fitnessapp.nutrition.model.dto.request.RecipeUpsertRequest;
import com.fitnessapp.nutrition.model.dto.response.FoodItemResponse;
import com.fitnessapp.nutrition.model.dto.response.RecipeResponse;
import com.fitnessapp.nutrition.model.entity.FoodItemEntity;
import com.fitnessapp.nutrition.model.entity.RecipeEntity;
import com.fitnessapp.nutrition.model.enums.DietaryPreference;
import com.fitnessapp.nutrition.repository.FoodItemRepository;
import com.fitnessapp.nutrition.repository.RecipeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class PortalNutritionServiceImpl implements PortalNutritionService {

    private final RecipeRepository recipeRepo;
    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    private final FoodItemRepository foodRepository;
    private final NutritionService nutritionService;
    private final FileStorageService fileStorageService;

    @Override
    @Transactional(readOnly = true)
    public Page<FoodItemResponse> searchFoods(String query, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        String normalizedQuery = StringUtils.hasText(query) ? query.trim() : "";
        return foodRepository.searchForPortal(normalizedQuery, PageRequest.of(safePage, safeSize))
                .map(this::toFoodResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RecipeResponse> searchRecipes(
            String query, DietaryPreference dietaryPreference, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        String normalizedQuery = StringUtils.hasText(query) ? query.trim() : "";
        return recipeRepo.searchForPortal(normalizedQuery, dietaryPreference,
                        PageRequest.of(safePage, safeSize))
                .map(this::toRecipe);
    }

    @Override
    @Transactional(readOnly = true)
    public RecipeResponse getRecipe(UUID recipeId) {
        RecipeEntity recipe = recipeRepo.findById(recipeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recipe not found"));
        return toRecipe(recipe);
    }

    @Override
    public List<RecipeResponse> generateRecipes(AiRecipeRequest request) {
        return nutritionService.generateRecipes(request);
    }


    @Override
    @Transactional
    public FoodItemResponse createCustomFood(CreateCustomFoodRequest request, MultipartFile image) {
        String userId = currentUserId();

        FoodItemEntity food = new FoodItemEntity();
        food.setOwner(userId);
        food.setName(request.name().trim());
        food.setBrand(clean(request.brand()));
        food.setImageUrl(uploadImage(image, "nutrition_foods"));
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

    @Override
    @Transactional
    public FoodItemResponse updateCustomFood(
            UUID foodId, CreateCustomFoodRequest request, MultipartFile image) {
        FoodItemEntity food = foodRepository.findById(foodId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Food not found"));
        if (!food.isActive()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Food not found");
        }

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
        if (image != null && !image.isEmpty()) {
            food.setImageUrl(uploadImage(image, "nutrition_foods"));
        }
        return toFoodResponse(foodRepository.save(food));
    }

    @Override
    @Transactional
    public void deleteCustomFood(UUID foodId) {
        FoodItemEntity food = foodRepository.findById(foodId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Food not found"));
        if (!food.isActive()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Food not found");
        }
        food.setActive(false);
        foodRepository.save(food);
    }

    @Override
    @Transactional
    public RecipeResponse createRecipe(RecipeUpsertRequest request, MultipartFile image) {
        if (image != null && !image.isEmpty() && StringUtils.hasText(request.imageUrl())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Provide either an image upload or imageUrl, not both"
            );
        }
        RecipeEntity recipe = new RecipeEntity();
        applyRecipe(recipe, request);
        if (image != null && !image.isEmpty()) {
            recipe.setImageUrl(uploadImage(image, "nutrition_recipes"));
        }
        String userId = currentUserId();
        recipe.setOwner(userId);
        recipe.setAiGenerated(false);
        return toRecipe(recipeRepo.save(recipe));
    }

    @Override
    @Transactional
    public RecipeResponse updateRecipe(UUID recipeId, RecipeUpsertRequest request, MultipartFile image) {
        if (image != null && !image.isEmpty() && StringUtils.hasText(request.imageUrl())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Provide either an image upload or imageUrl, not both"
            );
        }

        RecipeEntity recipe = recipeRepo.findById(recipeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recipe not found"));
        if (!recipe.isActive()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Recipe not found");
        }

        String existingImageUrl = recipe.getImageUrl();
        applyRecipe(recipe, request);
        if (image != null && !image.isEmpty()) {
            recipe.setImageUrl(uploadImage(image, "nutrition_recipes"));
        } else if (request.imageUrl() == null) {
            recipe.setImageUrl(existingImageUrl);
        }
        return toRecipe(recipeRepo.save(recipe));
    }

    @Override
    @Transactional
    public void deleteRecipe(UUID recipeId) {
        RecipeEntity recipe = recipeRepo.findById(recipeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recipe not found"));
        if (!recipe.isActive()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Recipe not found");
        }
        recipe.setActive(false);
        recipeRepo.save(recipe);
    }

    private FoodItemResponse toFoodResponse(FoodItemEntity f) {
        return new FoodItemResponse(f.getId(), f.getName(), f.getBrand(), f.getServingLabel(),
                f.getServingSizeGrams(), f.getCalories(), f.getProtein(), f.getCarbohydrates(),
                f.getFat(), nvl(f.getFiber()), nvl(f.getSugar()), f.getImageUrl(),
                f.isVerified(), f.getOwner() != null);
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

    private String uploadImage(MultipartFile image, String folder) {
        if (image == null || image.isEmpty()) {
            return null;
        }
        String imageUrl = fileStorageService.uploadFile(image, folder);
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            try {
                fileStorageService.deleteFileByUrl(imageUrl);
            } catch (RuntimeException cleanupException) {
                log.warn("Failed to clean up nutrition image after transaction setup failure", cleanupException);
            }
            throw new IllegalStateException("Nutrition image upload requires an active transaction");
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == STATUS_ROLLED_BACK) {
                    fileStorageService.deleteFileByUrl(imageUrl);
                }
            }
        });
        return imageUrl;
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

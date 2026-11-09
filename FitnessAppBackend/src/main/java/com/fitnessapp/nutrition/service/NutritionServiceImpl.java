package com.fitnessapp.nutrition.service;

import com.fitnessapp.appAuth.model.entity.UserEntity;
import com.fitnessapp.appAuth.repo.UserRepo;
import com.fitnessapp.nutrition.dto.request.*;
import com.fitnessapp.nutrition.dto.response.*;
import com.fitnessapp.nutrition.model.entity.*;
import com.fitnessapp.nutrition.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NutritionServiceImpl implements NutritionService {

    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    private final FoodItemRepository foodRepository;
    private final MealEntryRepository mealRepository;
    private final NutritionGoalRepository goalRepository;
    private final UserRepo userRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<FoodItemResponse> searchFoods(String userId, String query, int page, int size) {
        validateUserId(userId);
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        String normalizedQuery = StringUtils.hasText(query) ? query.trim() : null;
        return foodRepository.search(userId, normalizedQuery,
                PageRequest.of(safePage, safeSize)).map(this::toFoodResponse);
    }

    @Override
    @Transactional
    public FoodItemResponse createCustomFood(String userId, CreateCustomFoodRequest request) {
        UserEntity user = requireUser(userId);
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

    @Override
    @Transactional
    public MealEntryResponse logMeal(String userId, LogMealRequest request) {
        UserEntity user = requireUser(userId);
        if (request.clientMealId() != null) {
            var existing = mealRepository.findByUser_IdAndClientMealId(userId, request.clientMealId());
            if (existing.isPresent()) {
                return toMealResponse(existing.get());
            }
        }
        FoodItemEntity food = foodRepository.findAccessibleFood(request.foodId(), userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Food item not found"));

        BigDecimal factor = request.quantityGrams()
                .divide(food.getServingSizeGrams(), 8, RoundingMode.HALF_UP);

        MealEntryEntity meal = new MealEntryEntity();
        meal.setUser(user);
        meal.setFoodItem(food);
        meal.setMealType(request.mealType());
        meal.setMealDate(request.mealDate() == null ? LocalDate.now() : request.mealDate());
        meal.setFoodName(food.getName());
        meal.setQuantityGrams(request.quantityGrams());
        meal.setQuantityUnit("GRAMS");
        meal.setCalories(scale(food.getCalories().multiply(factor)));
        meal.setProtein(scale(food.getProtein().multiply(factor)));
        meal.setCarbohydrates(scale(food.getCarbohydrates().multiply(factor)));
        meal.setFat(scale(food.getFat().multiply(factor)));
        meal.setFiber(scale(nvl(food.getFiber()).multiply(factor)));
        meal.setSugar(scale(nvl(food.getSugar()).multiply(factor)));
        meal.setNotes(clean(request.notes()));
        meal.setClientMealId(request.clientMealId());
        meal.setPhotoUrl(clean(request.photoUrl()));
        meal.setSyncStatus(com.fitnessapp.nutrition.model.enums.MealSyncStatus.SYNCED);
        return toMealResponse(mealRepository.save(meal));
    }

    @Override
    @Transactional
    public MealEntryResponse logMealSnapshot(String userId, String foodName, BigDecimal quantity,
            BigDecimal calories, BigDecimal protein, BigDecimal carbohydrates, BigDecimal fat,
            BigDecimal fiber, BigDecimal sugar, com.fitnessapp.nutrition.model.enums.MealType mealType,
            LocalDate date, String notes, String photoUrl) {
        UserEntity user = requireUser(userId);
        MealEntryEntity meal = new MealEntryEntity();
        meal.setUser(user);
        meal.setFoodItem(null);
        meal.setMealType(mealType);
        meal.setMealDate(date == null ? LocalDate.now() : date);
        meal.setFoodName(foodName);
        meal.setQuantityGrams(quantity);
        meal.setQuantityUnit("SERVING");
        meal.setCalories(scale(calories));
        meal.setProtein(scale(protein));
        meal.setCarbohydrates(scale(carbohydrates));
        meal.setFat(scale(fat));
        meal.setFiber(scale(fiber));
        meal.setSugar(scale(sugar));
        meal.setNotes(clean(notes));
        meal.setPhotoUrl(clean(photoUrl));
        meal.setSyncStatus(com.fitnessapp.nutrition.model.enums.MealSyncStatus.SYNCED);
        return toMealResponse(mealRepository.save(meal));
    }

    @Override
    @Transactional
    public void deleteMeal(String userId, UUID mealId) {
        MealEntryEntity meal = mealRepository.findByIdAndUser_Id(mealId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Meal entry not found"));
        mealRepository.delete(meal);
    }

    @Override
    @Transactional
    public MealEntryResponse updateMeal(String userId, UUID mealId, UpdateMealRequest request) {
        MealEntryEntity meal = mealRepository.findByIdAndUser_Id(mealId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Meal entry not found"));
        FoodItemEntity food = foodRepository.findAccessibleFood(request.foodId(), userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Food item not found"));
        BigDecimal factor = request.quantityGrams().divide(food.getServingSizeGrams(), 8, RoundingMode.HALF_UP);
        meal.setFoodItem(food);
        meal.setFoodName(food.getName());
        meal.setMealType(request.mealType());
        meal.setMealDate(request.mealDate());
        meal.setQuantityGrams(request.quantityGrams());
        meal.setQuantityUnit("GRAMS");
        meal.setCalories(scale(food.getCalories().multiply(factor)));
        meal.setProtein(scale(food.getProtein().multiply(factor)));
        meal.setCarbohydrates(scale(food.getCarbohydrates().multiply(factor)));
        meal.setFat(scale(food.getFat().multiply(factor)));
        meal.setFiber(scale(nvl(food.getFiber()).multiply(factor)));
        meal.setSugar(scale(nvl(food.getSugar()).multiply(factor)));
        meal.setNotes(clean(request.notes()));
        meal.setPhotoUrl(clean(request.photoUrl()));
        return toMealResponse(mealRepository.save(meal));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MealEntryResponse> getMealHistory(String userId, int page, int size) {
        validateUserId(userId);
        return mealRepository.findAllByUser_IdOrderByMealDateDescCreatedAtDesc(
                userId, PageRequest.of(Math.max(0, page), Math.min(Math.max(size, 1), 100)))
                .map(this::toMealResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public DailyNutritionSummaryResponse getDailySummary(String userId, LocalDate date) {
        validateUserId(userId);
        LocalDate requestedDate = date == null ? LocalDate.now() : date;
        List<MealEntryEntity> entries =
                mealRepository.findAllByUser_IdAndMealDateOrderByCreatedAtAsc(userId, requestedDate);

        BigDecimal calories = sum(entries, MealEntryEntity::getCalories);
        BigDecimal protein = sum(entries, MealEntryEntity::getProtein);
        BigDecimal carbs = sum(entries, MealEntryEntity::getCarbohydrates);
        BigDecimal fat = sum(entries, MealEntryEntity::getFat);
        BigDecimal fiber = sum(entries, MealEntryEntity::getFiber);
        BigDecimal sugar = sum(entries, MealEntryEntity::getSugar);

        NutritionGoalResponse target = goalRepository.findByUser_Id(userId)
                .map(this::toGoalResponse).orElse(null);

        DailyNutritionSummaryResponse.NutrientTotals consumed =
                new DailyNutritionSummaryResponse.NutrientTotals(
                        calories, protein, carbs, fat, fiber, sugar);

        DailyNutritionSummaryResponse.NutrientTotals remaining = target == null ? null
                : new DailyNutritionSummaryResponse.NutrientTotals(
                    subtract(target.dailyCalories(), calories),
                    subtract(target.dailyProtein(), protein),
                    subtract(target.dailyCarbohydrates(), carbs),
                    subtract(target.dailyFat(), fat),
                    null, null);

        BigDecimal progress = target == null || target.dailyCalories().signum() == 0
                ? null : calories.multiply(BigDecimal.valueOf(100))
                    .divide(target.dailyCalories(), 2, RoundingMode.HALF_UP);

        return new DailyNutritionSummaryResponse(
                requestedDate, target, consumed, remaining, progress,
                entries.stream().map(this::toMealResponse).toList());
    }

    @Override
    @Transactional(readOnly = true)
    public NutritionGoalResponse getGoals(String userId) {
        validateUserId(userId);
        return goalRepository.findByUser_Id(userId)
                .map(this::toGoalResponse).orElse(null);
    }

    @Override
    @Transactional
    public NutritionGoalResponse updateGoals(String userId, UpdateNutritionGoalRequest request) {
        UserEntity user = requireUser(userId);
        NutritionGoalEntity goal = goalRepository.findByUser_Id(userId)
                .orElseGet(() -> {
                    NutritionGoalEntity created = new NutritionGoalEntity();
                    created.setUser(user);
                    return created;
                });
        goal.setDailyCalories(request.dailyCalories());
        goal.setDailyProtein(request.dailyProtein());
        goal.setDailyCarbohydrates(request.dailyCarbohydrates());
        goal.setDailyFat(request.dailyFat());
        return toGoalResponse(goalRepository.save(goal));
    }

    private UserEntity requireUser(String userId) {
        validateUserId(userId);
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User account not found"));
    }

    private void validateUserId(String userId) {
        if (!StringUtils.hasText(userId)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user is required");
        }
    }

    private FoodItemResponse toFoodResponse(FoodItemEntity f) {
        return new FoodItemResponse(f.getId(), f.getName(), f.getBrand(), f.getServingLabel(),
                f.getServingSizeGrams(), f.getCalories(), f.getProtein(), f.getCarbohydrates(),
                f.getFat(), nvl(f.getFiber()), nvl(f.getSugar()), f.isVerified(), f.getOwner() != null);
    }

    private MealEntryResponse toMealResponse(MealEntryEntity m) {
        return new MealEntryResponse(m.getId(),
                m.getFoodItem() == null ? null : m.getFoodItem().getId(),
                m.getFoodName(), m.getMealType(), m.getMealDate(), m.getQuantityGrams(),
                m.getQuantityUnit() == null ? "GRAMS" : m.getQuantityUnit(),
                m.getCalories(), m.getProtein(), m.getCarbohydrates(), m.getFat(),
                nvl(m.getFiber()), nvl(m.getSugar()), m.getNotes(), m.getCreatedAt(),
                m.getClientMealId(), m.getPhotoUrl(), m.getSyncStatus());
    }

    private NutritionGoalResponse toGoalResponse(NutritionGoalEntity g) {
        return new NutritionGoalResponse(g.getDailyCalories(), g.getDailyProtein(),
                g.getDailyCarbohydrates(), g.getDailyFat());
    }

    private interface AmountGetter { BigDecimal get(MealEntryEntity entry); }

    private BigDecimal sum(List<MealEntryEntity> entries, AmountGetter getter) {
        return scale(entries.stream().map(getter::get).map(this::nvl)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    private BigDecimal subtract(BigDecimal target, BigDecimal consumed) {
        return scale(target.subtract(consumed));
    }

    private BigDecimal scale(BigDecimal value) {
        return nvl(value).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal nvl(BigDecimal value) {
        return value == null ? ZERO : value;
    }

    private String clean(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}

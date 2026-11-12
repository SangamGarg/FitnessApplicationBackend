package com.fitnessapp.analytics.service;

import com.fitnessapp.activity.models.entities.ActivityRecordEntity;
import com.fitnessapp.activity.models.enums.ActivityStatus;
import com.fitnessapp.activity.repository.ActivityRepository;
import com.fitnessapp.analytics.model.dtos.responseDto.AnalyticsHomeResponseDto;
import com.fitnessapp.analytics.model.entity.CalculatorCatalogEntity;
import com.fitnessapp.analytics.repository.CalculatorCatalogRepository;
import com.fitnessapp.analytics.utils.CalculatorUtils;
import com.fitnessapp.gamification.entity.UserStreakEntity;
import com.fitnessapp.gamification.entity.UserXpAccountEntity;
import com.fitnessapp.gamification.repository.UserStreakRepository;
import com.fitnessapp.gamification.repository.UserXpAccountRepository;
import com.fitnessapp.nutrition.model.entity.MealEntryEntity;
import com.fitnessapp.nutrition.repository.MealEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private final CalculatorUtils calculatorUtils;


    private final ActivityRepository activityRepository;
    private final MealEntryRepository mealEntryRepository;
    private final UserStreakRepository userStreakRepository;
    private final UserXpAccountRepository userXpAccountRepository;
    private final CalculatorCatalogRepository catalogRepository;

    @Override
    public AnalyticsHomeResponseDto getAnalyticsHome() {
        return null;
    }

    @Override
    public List<Map<String, Object>> getCatalog() {
        List<CalculatorCatalogEntity> all = catalogRepository.findAll();
        Map<String, List<CalculatorCatalogEntity>> grouped = new LinkedHashMap<>();
        for (CalculatorCatalogEntity item : all)
            grouped.computeIfAbsent(item.getCategory(), k -> new ArrayList<>()).add(item);
        List<Map<String, Object>> result = new ArrayList<>();
        grouped.forEach((category, items) -> result.add(Map.of("category", category, "calculators", items)));
        return result;
    }

    @Override
    public Map<String, Object> calculate(String key, Map<String, Object> input) {
        CalculatorCatalogEntity def = catalogRepository.findById(key)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown calculator: " + key));
        double value;
        Map<String, Object> result = new LinkedHashMap<>();
        switch (key) {
            case "bmi" -> {
                double bmi = calculatorUtils.n(input, "weightKg") / Math.pow(calculatorUtils.n(input, "heightCm") / 100d, 2);
                result.put("value", calculatorUtils.round(bmi, 1));
                result.put("category", bmi < 18.5 ? "Underweight" : bmi < 25 ? "Normal range" : bmi < 30 ? "Overweight" : "Obesity range");
            }
            case "ideal-weight" -> {
                double h = calculatorUtils.n(input, "heightCm") - 152.4;
                double base = calculatorUtils.gender(input).equals("male") ? 50 : 45.5;
                result.put("value", calculatorUtils.round(base + 0.9 * (h / 2.54), 1));
                result.put("method", "Devine formula; adult reference estimate");
            }
            case "body-fat" -> {
                double bmi = calculatorUtils.n(input, "bmi"), age = calculatorUtils.n(input, "age");
                double sex = calculatorUtils.gender(input).equals("male") ? 1 : 0;
                result.put("value", calculatorUtils.round(1.20 * bmi + 0.23 * age - 10.8 * sex - 5.4, 1));
                result.put("method", "Deurenberg BMI-based estimate");
            }
            case "waist-hip-ratio" ->
                    result.put("value", calculatorUtils.round(calculatorUtils.n(input, "waistCm") / calculatorUtils.n(input, "hipCm"), 2));
            case "waist-height-ratio" ->
                    result.put("value", calculatorUtils.round(calculatorUtils.n(input, "waistCm") / calculatorUtils.n(input, "heightCm"), 2));
            case "lean-body-mass" -> {
                double w = calculatorUtils.n(input, "weightKg"), h = calculatorUtils.n(input, "heightCm");
                result.put("value", calculatorUtils.round(calculatorUtils.gender(input).equals("male") ? 0.407 * w + 0.267 * h - 19.2 : 0.252 * w + 0.473 * h - 48.3, 1));
                result.put("unit", "kg");
            }
            case "bmr", "tdee" -> {
                double bmr = 10 * calculatorUtils.n(input, "weightKg") + 6.25 * calculatorUtils.n(input, "heightCm") - 5 * calculatorUtils.n(input, "age") + (calculatorUtils.gender(input).equals("male") ? 5 : -161);
                value = key.equals("bmr") ? bmr : bmr * calculatorUtils.activityMultiplier(input);
                result.put("value", calculatorUtils.round(value, 0));
                result.put("bmr", calculatorUtils.round(bmr, 0));
                if (key.equals("tdee")) result.put("maintenanceCalories", calculatorUtils.round(value, 0));
            }
            case "calories-burned" ->
                    result.put("value", calculatorUtils.round(calculatorUtils.n(input, "met") * 3.5 * calculatorUtils.n(input, "weightKg") / 200 * calculatorUtils.n(input, "durationMinutes"), 0));
            case "calorie-deficit", "calorie-surplus" -> {
                double tdee = calculatorUtils.n(input, "tdee"), pct = calculatorUtils.optional(input, "deficitPercent", calculatorUtils.optional(input, "surplusPercent", 15));
                if (pct <= 0 || pct > 40) calculatorUtils.bad("deficitPercent/surplusPercent must be > 0 and <= 40");
                result.put("value", calculatorUtils.round(key.equals("calorie-deficit") ? tdee * (1 - pct / 100) : tdee * (1 + pct / 100), 0));
                result.put("changePercent", pct);
            }
            case "food-calories" -> result.putAll(calculatorUtils.scaleFood(input, "quantityGrams", "caloriesPer100g"));
            case "protein-intake" -> {
                double factor = switch (calculatorUtils.str(input, "goal").toLowerCase(Locale.ROOT)) {
                    case "muscle_gain", "muscle-gain" -> 1.6;
                    case "weight_loss", "weight-loss" -> 1.8;
                    default -> 1.2;
                };
                result.put("minimum", calculatorUtils.round(calculatorUtils.n(input, "weightKg") * factor, 0));
                result.put("maximum", calculatorUtils.round(calculatorUtils.n(input, "weightKg") * (factor + 0.4), 0));
                result.put("unit", "g/day");
            }
            case "macros" -> {
                double kcal = calculatorUtils.n(input, "dailyCalories"), pp = calculatorUtils.optional(input, "proteinPercent", 30), fp = calculatorUtils.optional(input, "fatPercent", 30), cp = 100 - pp - fp;
                if (pp < 0 || fp < 0 || cp < 0)
                    calculatorUtils.bad("Protein and fat percentages must total no more than 100");
                result.put("proteinGrams", calculatorUtils.round(kcal * pp / 100 / 4, 1));
                result.put("carbohydrateGrams", calculatorUtils.round(kcal * cp / 100 / 4, 1));
                result.put("fatGrams", calculatorUtils.round(kcal * fp / 100 / 9, 1));
                result.put("percentages", Map.of("protein", pp, "carbohydrates", cp, "fat", fp));
            }
            case "fiber-intake" ->
                    result.put("value", calculatorUtils.round(calculatorUtils.n(input, "dailyCalories") / 1000 * 14, 1));
            case "water-intake" ->
                    result.put("value", calculatorUtils.round(calculatorUtils.n(input, "weightKg") * 35, 0));
            case "meal-macros" -> {
                double kcal = calculatorUtils.n(input, "calories"), p = calculatorUtils.n0(input, "protein"), c = calculatorUtils.n0(input, "carbohydrates"), f = calculatorUtils.n0(input, "fat");
                result.put("calories", calculatorUtils.round(kcal, 1));
                result.put("protein", calculatorUtils.round(p, 1));
                result.put("carbohydrates", calculatorUtils.round(c, 1));
                result.put("fat", calculatorUtils.round(f, 1));
                result.put("calculatedCaloriesFromMacros", calculatorUtils.round(p * 4 + c * 4 + f * 9, 1));
            }
            case "sugar-intake" ->
                    result.put("maximumAddedSugarGrams", calculatorUtils.round(calculatorUtils.n(input, "dailyCalories") * 0.10 / 4, 1));
            case "target-heart-rate" -> {
                double max = 208 - 0.7 * calculatorUtils.n(input, "age"), intensity = calculatorUtils.n(input, "intensityPercent");
                if (intensity < 50 || intensity > 100)
                    calculatorUtils.bad("intensityPercent must be between 50 and 100");
                result.put("maximumHeartRate", calculatorUtils.round(max, 0));
                result.put("targetHeartRate", calculatorUtils.round(max * intensity / 100, 0));
            }
            case "one-rep-max" -> {
                double reps = calculatorUtils.n(input, "reps");
                if (reps < 1 || reps > 12) calculatorUtils.bad("reps must be between 1 and 12 for this estimate");
                result.put("value", calculatorUtils.round(calculatorUtils.n(input, "weightKg") * (1 + reps / 30), 1));
                result.put("method", "Epley");
            }
            case "running-pace" -> {
                double pace = calculatorUtils.n(input, "timeMinutes") / calculatorUtils.n(input, "distanceKm");
                result.put("minutesPerKm", calculatorUtils.round(pace, 2));
                result.put("kmPerHour", calculatorUtils.round(60 / pace, 2));
            }
            case "steps-distance" ->
                    result.put("value", calculatorUtils.round(calculatorUtils.n0(input, "steps") * calculatorUtils.n(input, "strideCm") / 100000, 2));
            case "workout-volume" ->
                    result.put("value", calculatorUtils.round(calculatorUtils.n(input, "sets") * calculatorUtils.n(input, "reps") * calculatorUtils.n(input, "weightKg"), 1));
            case "vo2-max" ->
                    result.put("value", calculatorUtils.round((calculatorUtils.n(input, "distanceMeters") - 504.9) / 44.73, 1));
            case "heart-rate-recovery" ->
                    result.put("value", calculatorUtils.round(calculatorUtils.n(input, "heartRateAtFinish") - calculatorUtils.n(input, "heartRateAfterOneMinute"), 0));
            case "training-zone" -> {
                double max = 208 - 0.7 * calculatorUtils.n(input, "age"), rest = calculatorUtils.n(input, "restingHeartRate"), reserve = max - rest;
                List<Map<String, Object>> zones = new ArrayList<>();
                double[][] ranges = {{.5, .6}, {.6, .7}, {.7, .8}, {.8, .9}, {.9, 1.0}};
                for (int i = 0; i < ranges.length; i++)
                    zones.add(Map.of("zone", i + 1, "minBpm", calculatorUtils.round(rest + reserve * ranges[i][0], 0), "maxBpm", calculatorUtils.round(rest + reserve * ranges[i][1], 0)));
                result.put("zones", zones);
            }
            case "race-time-predictor" ->
                    result.put("predictedMinutes", calculatorUtils.round(calculatorUtils.n(input, "recentTimeMinutes") * Math.pow(calculatorUtils.n(input, "targetDistanceKm") / calculatorUtils.n(input, "recentDistanceKm"), 1.06), 1));
            case "rest-time" -> {
                String g = calculatorUtils.str(input, "goal").toLowerCase(Locale.ROOT);
                result.put("seconds", g.contains("strength") ? 180 : g.contains("hypertrophy") ? 90 : g.contains("endurance") ? 45 : 60);
            }
            case "weight-goal" -> {
                double diff = Math.abs(calculatorUtils.n(input, "targetWeightKg") - calculatorUtils.n(input, "currentWeightKg")), rate = calculatorUtils.n(input, "weeklyChangeKg");
                if (rate <= 0) calculatorUtils.bad("weeklyChangeKg must be positive");
                result.put("estimatedWeeks", calculatorUtils.round(diff / rate, 1));
            }
            case "weight-change" ->
                    result.put("value", calculatorUtils.round((calculatorUtils.n(input, "currentWeightKg") - calculatorUtils.n(input, "startingWeightKg")) / calculatorUtils.n(input, "startingWeightKg") * 100, 2));
            case "workout-consistency" -> {
                double planned = calculatorUtils.n(input, "plannedDays"), completed = calculatorUtils.n0(input, "completedDays");
                if (completed > planned) calculatorUtils.bad("completedDays cannot exceed plannedDays");
                result.put("value", calculatorUtils.round(completed / planned * 100, 1));
            }
            case "workout-difficulty" -> {
                double rpe = calculatorUtils.n(input, "rpe");
                if (rpe < 1 || rpe > 10) calculatorUtils.bad("rpe must be between 1 and 10");
                result.put("sessionLoad", calculatorUtils.round(calculatorUtils.n(input, "durationMinutes") * rpe, 0));
            }
            case "body-recomposition" -> {
                result.put("weightChangeKg", calculatorUtils.round(calculatorUtils.n(input, "currentWeightKg") - calculatorUtils.n(input, "startingWeightKg"), 1));
                result.put("bodyFatChangePercentagePoints", calculatorUtils.round(calculatorUtils.n0(input, "currentBodyFatPercent") - calculatorUtils.n0(input, "startingBodyFatPercent"), 1));
            }
            case "workout-recovery" -> {
                double sleep = calculatorUtils.n0(input, "sleepHours"), soreness = calculatorUtils.n0(input, "sorenessScore"), fatigue = calculatorUtils.n0(input, "fatigueScore");
                if (soreness < 0 || soreness > 10 || fatigue < 0 || fatigue > 10)
                    calculatorUtils.bad("sorenessScore and fatigueScore must be between 0 and 10");
                result.put("value", calculatorUtils.round(Math.max(0, Math.min(100, (sleep / 8 * 50) + (10 - soreness) * 2.5 + (10 - fatigue) * 2.5)), 0));
            }
            case "fitness-age" -> {
                double age = calculatorUtils.n(input, "age"), vo2 = calculatorUtils.n(input, "vo2Max");
                double reference = calculatorUtils.gender(input).equals("male") ? 45 : 38;
                result.put("estimatedFitnessAge", calculatorUtils.round(age + (reference - vo2) * 0.6, 0));
                result.put("note", "Rough population-based proxy; not a clinical fitness-age test.");
            }
            case "food-label-serving" ->
                    result.putAll(calculatorUtils.scaleFood(input, "quantityGrams", "caloriesPerServing", true));
            default -> throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Calculator not implemented");
        }
        result.put("calculator", key);
        result.put("name", def.getName());
        result.put("unit", def.getUnit());
        result.put("assumptions", calculatorUtils.assumptions(key));
        return result;
    }

    @Override
    public Map<String, Object> getFitnessScore(ZoneId zone) {
        String userId = currentUserId();
        LocalDate today = LocalDate.now(zone), start = today.minusDays(6);
        Instant from = start.atStartOfDay(zone).toInstant(), to = today.plusDays(1).atStartOfDay(zone).toInstant();
        List<ActivityRecordEntity> activities = activityRepository.findAllByUser_IdAndStatusAndIncludedInSummaryTrueAndStartedAtBetween(userId, ActivityStatus.COMPLETED, from, to);
        Set<LocalDate> activeDays = new HashSet<>();
        long activeSeconds = 0;
        for (ActivityRecordEntity a : activities) {
            activeDays.add(a.getStartedAt().atZone(zone).toLocalDate());
            if (a.getDurationSeconds() != null) activeSeconds += a.getDurationSeconds();
        }
        Set<LocalDate> nutritionDays = new HashSet<>();
        for (MealEntryEntity meal : mealEntryRepository.findAllByUser_IdAndMealDateBetween(userId, start, today)) {
            nutritionDays.add(meal.getMealDate());
        }
        double activityConsistency = Math.min(25, activeDays.size() / 7d * 25);
        double workoutVolume = Math.min(15, activities.size() / 5d * 15);
        double nutritionScore = nutritionDays.size() / 7d * 25;
        long totalXp = userXpAccountRepository.findByUserId(userId).map(UserXpAccountEntity::getTotalXp).orElse(0L);
        int currentStreak = userStreakRepository.findByUserId(userId).map(UserStreakEntity::getCurrentStreak).orElse(0);
        double xpScore = Math.min(20, totalXp / 500d * 20);
        double streakScore = Math.min(15, currentStreak / 7d * 15);
        int score = (int) Math.round(activityConsistency + workoutVolume + nutritionScore + xpScore + streakScore);
        String level = score >= 80 ? "Excellent" : score >= 60 ? "Good" : score >= 40 ? "Building consistency" : "Getting started";
        return Map.ofEntries(
                Map.entry("score", score), Map.entry("outOf", 100), Map.entry("level", level),
                Map.entry("periodStart", start), Map.entry("periodEnd", today),
                Map.entry("activityDays", activeDays.size()), Map.entry("nutritionLoggedDays", nutritionDays.size()),
                Map.entry("completedActivities", activities.size()), Map.entry("activeMinutes", Math.round(activeSeconds / 60d)),
                Map.entry("totalXp", totalXp), Map.entry("currentStreakDays", currentStreak),
                Map.entry("components", Map.of("activityConsistency", Math.round(activityConsistency), "workoutVolume", Math.round(workoutVolume), "nutrition", Math.round(nutritionScore), "gamificationXp", Math.round(xpScore), "streak", Math.round(streakScore))),
                Map.entry("methodology", "Activity consistency 25%, completed workout volume 15%, nutrition logging consistency 25%, accumulated gamification XP 20%, current streak 15%. This is an engagement score, not a medical fitness assessment.")
        );
    }

    private String currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getName() == null || "anonymousUser".equals(authentication.getName())) {
            log.warn("Unauthorized access attempt without valid authentication principal in SecurityContext");
            throw new RuntimeException("Authentication required");
        }
        return authentication.getName();
    }
}

package com.fitnessapp.calculator.service;

import com.fitnessapp.activity.models.entities.ActivityRecordEntity;
import com.fitnessapp.activity.models.enums.ActivityStatus;
import com.fitnessapp.activity.repository.ActivityRepository;
import com.fitnessapp.calculator.model.CalculatorDefinition;
import com.fitnessapp.nutrition.model.entity.MealEntryEntity;
import com.fitnessapp.nutrition.repository.MealEntryRepository;
import com.fitnessapp.gamification.repository.UserStreakRepository;
import com.fitnessapp.gamification.repository.UserXpAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SmartCalculatorService {
    private final ActivityRepository activityRepository;
    private final MealEntryRepository mealEntryRepository;
    private final UserStreakRepository userStreakRepository;
    private final UserXpAccountRepository userXpAccountRepository;

    private static final List<CalculatorDefinition> CATALOG = List.of(
            d("bmi", "BMI Calculator", "BODY_COMPOSITION", "Estimate body mass index.", "weightKg,heightCm", "kg/m²"),
            d("ideal-weight", "Ideal Weight Calculator", "BODY_COMPOSITION", "Estimate an adult reference weight using the Devine formula.", "gender,heightCm", "kg"),
            d("body-fat", "Body Fat Percentage Calculator", "BODY_COMPOSITION", "Estimate body fat using the Deurenberg BMI equation; this is a screening estimate, not a measurement.", "age,gender,bmi", "%"),
            d("waist-hip-ratio", "Waist-to-Hip Ratio Calculator", "BODY_COMPOSITION", "Divide waist circumference by hip circumference.", "waistCm,hipCm", "ratio"),
            d("waist-height-ratio", "Waist-to-Height Ratio Calculator", "BODY_COMPOSITION", "Divide waist circumference by height.", "waistCm,heightCm", "ratio"),
            d("lean-body-mass", "Lean Body Mass Calculator", "BODY_COMPOSITION", "Estimate lean body mass using the Boer equation.", "gender,weightKg,heightCm", "kg"),
            d("tdee", "Daily Calorie Needs Calculator (TDEE)", "NUTRITION", "Estimate maintenance calories from BMR and activity multiplier.", "age,gender,weightKg,heightCm,activityLevel", "kcal/day"),
            d("bmr", "Basal Metabolic Rate Calculator (BMR)", "NUTRITION", "Estimate resting energy expenditure using Mifflin-St Jeor.", "age,gender,weightKg,heightCm", "kcal/day"),
            d("calories-burned", "Calories Burned Calculator", "NUTRITION", "Estimate activity energy expenditure using MET.", "weightKg,durationMinutes,met", "kcal"),
            d("calorie-deficit", "Calorie Deficit Calculator", "NUTRITION", "Calculate a target intake below maintenance calories.", "tdee", "kcal/day"),
            d("calorie-surplus", "Calorie Surplus Calculator", "NUTRITION", "Calculate a target intake above maintenance calories.", "tdee", "kcal/day"),
            d("food-calories", "Food Calorie Calculator", "NUTRITION", "Scale food energy and macros from per-100g nutrition values.", "quantityGrams,caloriesPer100g", "kcal"),
            d("protein-intake", "Protein Intake Calculator", "NUTRITION", "Estimate a daily protein range from body weight and goal.", "weightKg,goal", "g/day"),
            d("macros", "Macronutrient (Macro) Calculator", "NUTRITION", "Split a calorie target into protein, carbohydrate and fat.", "dailyCalories", "g/day"),
            d("fiber-intake", "Fiber Intake Calculator", "NUTRITION", "Estimate fiber using 14 grams per 1,000 kcal.", "dailyCalories", "g/day"),
            d("water-intake", "Water Intake Calculator", "NUTRITION", "Estimate a baseline daily fluid target from body weight.", "weightKg", "ml/day"),
            d("meal-macros", "Meal Calorie and Macro Calculator", "NUTRITION", "Sum the nutrition values of a meal's ingredients.", "calories,protein,carbohydrates,fat", "kcal and g"),
            d("sugar-intake", "Sugar Intake Calculator", "NUTRITION", "Estimate the 10%-of-energy upper limit for added sugar.", "dailyCalories", "g/day"),
            d("target-heart-rate", "Target Heart Rate Calculator", "PERFORMANCE", "Estimate maximum heart rate and target zone using age and intensity.", "age,intensityPercent", "bpm"),
            d("one-rep-max", "One-Rep Max (1RM) Calculator", "PERFORMANCE", "Estimate one-repetition maximum using the Epley equation.", "weightKg,reps", "kg"),
            d("running-pace", "Running Pace Calculator", "PERFORMANCE", "Calculate pace and speed from distance and elapsed time.", "distanceKm,timeMinutes", "min/km"),
            d("steps-distance", "Steps-to-Distance Calculator", "PERFORMANCE", "Estimate distance from steps and stride length.", "steps,strideCm", "km"),
            d("workout-volume", "Workout Volume Calculator", "PERFORMANCE", "Calculate total lifted volume across sets and repetitions.", "sets,reps,weightKg", "kg"),
            d("vo2-max", "VO2 Max Estimator", "PERFORMANCE", "Estimate VO2 max using the Cooper 12-minute distance test.", "distanceMeters", "ml/kg/min"),
            d("heart-rate-recovery", "Heart Rate Recovery Calculator", "PERFORMANCE", "Calculate the drop in heart rate after exercise.", "heartRateAtFinish,heartRateAfterOneMinute", "bpm"),
            d("training-zone", "Training Zone Calculator", "PERFORMANCE", "Calculate five heart-rate training zones using heart-rate reserve.", "age,restingHeartRate", "bpm"),
            d("race-time-predictor", "Race Time Predictor", "PERFORMANCE", "Predict race time using the Riegel distance formula.", "recentTimeMinutes,recentDistanceKm,targetDistanceKm", "minutes"),
            d("rest-time", "Rest Time Calculator", "PERFORMANCE", "Suggest a rest interval based on training goal.", "goal", "seconds"),
            d("weight-goal", "Weight Goal Calculator", "WEIGHT_RECOVERY", "Estimate a target date from weight change and weekly rate.", "currentWeightKg,targetWeightKg,weeklyChangeKg", "weeks"),
            d("weight-change", "Weight Change Percentage Calculator", "WEIGHT_RECOVERY", "Calculate percentage change between two weights.", "startingWeightKg,currentWeightKg", "%"),
            d("workout-consistency", "Workout Consistency Calculator", "WEIGHT_RECOVERY", "Calculate completed workout days as a percentage of planned days.", "completedDays,plannedDays", "%"),
            d("workout-difficulty", "Workout Difficulty Score Calculator", "WEIGHT_RECOVERY", "Estimate session load using duration and perceived exertion.", "durationMinutes,rpe", "arbitrary units"),
            d("body-recomposition", "Body Recomposition Tracker", "WEIGHT_RECOVERY", "Compare changes in body weight and body-fat percentage.", "startingWeightKg,currentWeightKg,startingBodyFatPercent,currentBodyFatPercent", "kg and percentage points"),
            d("workout-recovery", "Workout Recovery Tracker", "WEIGHT_RECOVERY", "Estimate a simple recovery readiness score from sleep and soreness.", "sleepHours,sorenessScore,fatigueScore", "score/100"),
            d("fitness-age", "Fitness Age Estimator", "WEIGHT_RECOVERY", "Estimate a rough fitness-age proxy from VO2 max and chronological age.", "age,vo2Max,gender", "years"),
            d("food-label-serving", "Food Label Serving Calculator", "WEIGHT_RECOVERY", "Scale label nutrients to the portion consumed.", "servingGrams,quantityGrams,caloriesPerServing", "kcal")
    );

    public List<Map<String, Object>> getCatalog() {
        Map<String, List<CalculatorDefinition>> grouped = new LinkedHashMap<>();
        for (CalculatorDefinition item : CATALOG)
            grouped.computeIfAbsent(item.category(), k -> new ArrayList<>()).add(item);
        List<Map<String, Object>> result = new ArrayList<>();
        grouped.forEach((category, items) -> result.add(Map.of("category", category, "calculators", items)));
        return result;
    }

    public Map<String, Object> calculate(String key, Map<String, Object> in) {
        CalculatorDefinition def = CATALOG.stream().filter(x -> x.key().equals(key)).findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown calculator: " + key));
        double value;
        Map<String, Object> result = new LinkedHashMap<>();
        switch (key) {
            case "bmi" -> {
                double bmi = n(in, "weightKg") / Math.pow(n(in, "heightCm") / 100d, 2);
                result.put("value", round(bmi, 1));
                result.put("category", bmi < 18.5 ? "Underweight" : bmi < 25 ? "Normal range" : bmi < 30 ? "Overweight" : "Obesity range");
            }
            case "ideal-weight" -> {
                double h = n(in, "heightCm") - 152.4;
                double base = gender(in).equals("male") ? 50 : 45.5;
                result.put("value", round(base + 0.9 * (h / 2.54), 1));
                result.put("method", "Devine formula; adult reference estimate");
            }
            case "body-fat" -> {
                double bmi = n(in, "bmi"), age = n(in, "age");
                double sex = gender(in).equals("male") ? 1 : 0;
                result.put("value", round(1.20 * bmi + 0.23 * age - 10.8 * sex - 5.4, 1));
                result.put("method", "Deurenberg BMI-based estimate");
            }
            case "waist-hip-ratio" -> result.put("value", round(n(in, "waistCm") / n(in, "hipCm"), 2));
            case "waist-height-ratio" -> result.put("value", round(n(in, "waistCm") / n(in, "heightCm"), 2));
            case "lean-body-mass" -> {
                double w = n(in, "weightKg"), h = n(in, "heightCm");
                result.put("value", round(gender(in).equals("male") ? 0.407 * w + 0.267 * h - 19.2 : 0.252 * w + 0.473 * h - 48.3, 1));
                result.put("unit", "kg");
            }
            case "bmr", "tdee" -> {
                double bmr = 10 * n(in, "weightKg") + 6.25 * n(in, "heightCm") - 5 * n(in, "age") + (gender(in).equals("male") ? 5 : -161);
                value = key.equals("bmr") ? bmr : bmr * activityMultiplier(in);
                result.put("value", round(value, 0));
                result.put("bmr", round(bmr, 0));
                if (key.equals("tdee")) result.put("maintenanceCalories", round(value, 0));
            }
            case "calories-burned" ->
                    result.put("value", round(n(in, "met") * 3.5 * n(in, "weightKg") / 200 * n(in, "durationMinutes"), 0));
            case "calorie-deficit", "calorie-surplus" -> {
                double tdee = n(in, "tdee"), pct = optional(in, "deficitPercent", optional(in, "surplusPercent", 15));
                if (pct <= 0 || pct > 40) bad("deficitPercent/surplusPercent must be > 0 and <= 40");
                result.put("value", round(key.equals("calorie-deficit") ? tdee * (1 - pct / 100) : tdee * (1 + pct / 100), 0));
                result.put("changePercent", pct);
            }
            case "food-calories" -> result.putAll(scaleFood(in, "quantityGrams", "caloriesPer100g"));
            case "protein-intake" -> {
                double factor = switch (str(in, "goal").toLowerCase(Locale.ROOT)) {
                    case "muscle_gain", "muscle-gain" -> 1.6;
                    case "weight_loss", "weight-loss" -> 1.8;
                    default -> 1.2;
                };
                result.put("minimum", round(n(in, "weightKg") * factor, 0));
                result.put("maximum", round(n(in, "weightKg") * (factor + 0.4), 0));
                result.put("unit", "g/day");
            }
            case "macros" -> {
                double kcal = n(in, "dailyCalories"), pp = optional(in, "proteinPercent", 30), fp = optional(in, "fatPercent", 30), cp = 100 - pp - fp;
                if (pp < 0 || fp < 0 || cp < 0) bad("Protein and fat percentages must total no more than 100");
                result.put("proteinGrams", round(kcal * pp / 100 / 4, 1));
                result.put("carbohydrateGrams", round(kcal * cp / 100 / 4, 1));
                result.put("fatGrams", round(kcal * fp / 100 / 9, 1));
                result.put("percentages", Map.of("protein", pp, "carbohydrates", cp, "fat", fp));
            }
            case "fiber-intake" -> result.put("value", round(n(in, "dailyCalories") / 1000 * 14, 1));
            case "water-intake" -> result.put("value", round(n(in, "weightKg") * 35, 0));
            case "meal-macros" -> {
                double kcal = n(in, "calories"), p = n0(in, "protein"), c = n0(in, "carbohydrates"), f = n0(in, "fat");
                result.put("calories", round(kcal, 1));
                result.put("protein", round(p, 1));
                result.put("carbohydrates", round(c, 1));
                result.put("fat", round(f, 1));
                result.put("calculatedCaloriesFromMacros", round(p * 4 + c * 4 + f * 9, 1));
            }
            case "sugar-intake" -> result.put("maximumAddedSugarGrams", round(n(in, "dailyCalories") * 0.10 / 4, 1));
            case "target-heart-rate" -> {
                double max = 208 - 0.7 * n(in, "age"), intensity = n(in, "intensityPercent");
                if (intensity < 50 || intensity > 100) bad("intensityPercent must be between 50 and 100");
                result.put("maximumHeartRate", round(max, 0));
                result.put("targetHeartRate", round(max * intensity / 100, 0));
            }
            case "one-rep-max" -> {
                double reps = n(in, "reps");
                if (reps < 1 || reps > 12) bad("reps must be between 1 and 12 for this estimate");
                result.put("value", round(n(in, "weightKg") * (1 + reps / 30), 1));
                result.put("method", "Epley");
            }
            case "running-pace" -> {
                double pace = n(in, "timeMinutes") / n(in, "distanceKm");
                result.put("minutesPerKm", round(pace, 2));
                result.put("kmPerHour", round(60 / pace, 2));
            }
            case "steps-distance" -> result.put("value", round(n0(in, "steps") * n(in, "strideCm") / 100000, 2));
            case "workout-volume" -> result.put("value", round(n(in, "sets") * n(in, "reps") * n(in, "weightKg"), 1));
            case "vo2-max" -> result.put("value", round((n(in, "distanceMeters") - 504.9) / 44.73, 1));
            case "heart-rate-recovery" ->
                    result.put("value", round(n(in, "heartRateAtFinish") - n(in, "heartRateAfterOneMinute"), 0));
            case "training-zone" -> {
                double max = 208 - 0.7 * n(in, "age"), rest = n(in, "restingHeartRate"), reserve = max - rest;
                List<Map<String, Object>> zones = new ArrayList<>();
                double[][] ranges = {{.5, .6}, {.6, .7}, {.7, .8}, {.8, .9}, {.9, 1.0}};
                for (int i = 0; i < ranges.length; i++)
                    zones.add(Map.of("zone", i + 1, "minBpm", round(rest + reserve * ranges[i][0], 0), "maxBpm", round(rest + reserve * ranges[i][1], 0)));
                result.put("zones", zones);
            }
            case "race-time-predictor" ->
                    result.put("predictedMinutes", round(n(in, "recentTimeMinutes") * Math.pow(n(in, "targetDistanceKm") / n(in, "recentDistanceKm"), 1.06), 1));
            case "rest-time" -> {
                String g = str(in, "goal").toLowerCase(Locale.ROOT);
                result.put("seconds", g.contains("strength") ? 180 : g.contains("hypertrophy") ? 90 : g.contains("endurance") ? 45 : 60);
            }
            case "weight-goal" -> {
                double diff = Math.abs(n(in, "targetWeightKg") - n(in, "currentWeightKg")), rate = n(in, "weeklyChangeKg");
                if (rate <= 0) bad("weeklyChangeKg must be positive");
                result.put("estimatedWeeks", round(diff / rate, 1));
            }
            case "weight-change" ->
                    result.put("value", round((n(in, "currentWeightKg") - n(in, "startingWeightKg")) / n(in, "startingWeightKg") * 100, 2));
            case "workout-consistency" -> {
                double planned = n(in, "plannedDays"), completed = n0(in, "completedDays");
                if (completed > planned) bad("completedDays cannot exceed plannedDays");
                result.put("value", round(completed / planned * 100, 1));
            }
            case "workout-difficulty" -> {
                double rpe = n(in, "rpe");
                if (rpe < 1 || rpe > 10) bad("rpe must be between 1 and 10");
                result.put("sessionLoad", round(n(in, "durationMinutes") * rpe, 0));
            }
            case "body-recomposition" -> {
                result.put("weightChangeKg", round(n(in, "currentWeightKg") - n(in, "startingWeightKg"), 1));
                result.put("bodyFatChangePercentagePoints", round(n0(in, "currentBodyFatPercent") - n0(in, "startingBodyFatPercent"), 1));
            }
            case "workout-recovery" -> {
                double sleep = n0(in, "sleepHours"), soreness = n0(in, "sorenessScore"), fatigue = n0(in, "fatigueScore");
                if (soreness < 0 || soreness > 10 || fatigue < 0 || fatigue > 10)
                    bad("sorenessScore and fatigueScore must be between 0 and 10");
                result.put("value", round(Math.max(0, Math.min(100, (sleep / 8 * 50) + (10 - soreness) * 2.5 + (10 - fatigue) * 2.5)), 0));
            }
            case "fitness-age" -> {
                double age = n(in, "age"), vo2 = n(in, "vo2Max");
                double reference = gender(in).equals("male") ? 45 : 38;
                result.put("estimatedFitnessAge", round(age + (reference - vo2) * 0.6, 0));
                result.put("note", "Rough population-based proxy; not a clinical fitness-age test.");
            }
            case "food-label-serving" -> result.putAll(scaleFood(in, "quantityGrams", "caloriesPerServing", true));
            default -> throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Calculator not implemented");
        }
        result.put("calculator", key);
        result.put("name", def.name());
        result.put("unit", def.unit());
        result.put("assumptions", assumptions(key));
        return result;
    }

    public Map<String, Object> getFitnessScore(String userId, ZoneId zone) {
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
        long totalXp = userXpAccountRepository.findByUserId(userId).map(x -> x.getTotalXp()).orElse(0L);
        int currentStreak = userStreakRepository.findByUserId(userId).map(x -> x.getCurrentStreak()).orElse(0);
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

    private static CalculatorDefinition d(String key, String name, String category, String description, String inputs, String unit) {
        return new CalculatorDefinition(key, name, category, description, List.of(inputs.split(",")), unit, false);
    }

    private static double n(Map<String, Object> m, String k) {
        Object v = m.get(k);
        if (v == null) bad("Missing required input: " + k);
        try {
            double x = Double.parseDouble(v.toString());
            if (!Double.isFinite(x) || x <= 0) bad(k + " must be a positive number");
            return x;
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, k + " must be numeric");
        }
    }

    private static double n0(Map<String, Object> m, String k) {
        Object v = m.get(k);
        if (v == null) bad("Missing required input: " + k);
        try {
            double x = Double.parseDouble(v.toString());
            if (!Double.isFinite(x) || x < 0) bad(k + " must be zero or a positive number");
            return x;
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, k + " must be numeric");
        }
    }

    private static double optional(Map<String, Object> m, String k, double def) {
        if (!m.containsKey(k) || m.get(k) == null) return def;
        try {
            double x = Double.parseDouble(m.get(k).toString());
            if (!Double.isFinite(x) || x < 0) bad(k + " must be zero or a positive number");
            return x;
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, k + " must be numeric");
        }
    }

    private static String str(Map<String, Object> m, String k) {
        Object v = m.get(k);
        if (v == null || v.toString().isBlank()) bad("Missing required input: " + k);
        return v.toString();
    }

    private static String gender(Map<String, Object> m) {
        String g = str(m, "gender").toLowerCase(Locale.ROOT);
        if (!g.equals("male") && !g.equals("female")) bad("gender must be male or female");
        return g;
    }

    private static double activityMultiplier(Map<String, Object> m) {
        return switch (str(m, "activityLevel").toLowerCase(Locale.ROOT)) {
            case "sedentary", "level_1" -> 1.2;
            case "light", "lightly_active", "level_2" -> 1.375;
            case "moderate", "moderately_active", "level_3" -> 1.55;
            case "active", "very_active", "level_4" -> 1.725;
            case "very_active_plus", "athlete", "level_5" -> 1.9;
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown activityLevel");
        };
    }

    private static Map<String, Object> scaleFood(Map<String, Object> in, String quantityKey, String calorieKey) {
        return scaleFood(in, quantityKey, calorieKey, false);
    }

    private static Map<String, Object> scaleFood(Map<String, Object> in, String quantityKey, String calorieKey, boolean perServing) {
        double q = n(in, quantityKey), base = n(in, calorieKey), serving = perServing ? n(in, "servingGrams") : 100;
        double factor = q / serving;
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("value", round(base * factor, 1));
        out.put("quantityGrams", q);
        for (String key : List.of("proteinPer100g", "carbohydratesPer100g", "fatPer100g", "fiberPer100g")) {
            if (in.containsKey(key)) out.put(key.replace("Per100g", ""), round(optional(in, key, 0) * q / 100, 1));
        }
        return out;
    }

    private static String assumptions(String key) {
        return switch (key) {
            case "bmr", "tdee" -> "Mifflin-St Jeor equation; adult estimate.";
            case "body-fat" -> "BMI-based population estimate; accuracy varies by individual.";
            case "calories-burned" -> "MET-based estimate; actual energy expenditure varies.";
            case "water-intake" -> "Baseline 35 ml/kg; adjust for climate, exercise and clinical advice.";
            case "vo2-max" -> "Cooper 12-minute test equation; requires a maximal effort test.";
            default -> "Estimate based on the supplied values; individual results may vary.";
        };
    }

    private static double round(double v, int scale) {
        return BigDecimal.valueOf(v).setScale(scale, RoundingMode.HALF_UP).doubleValue();
    }

    private static void bad(String message) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}

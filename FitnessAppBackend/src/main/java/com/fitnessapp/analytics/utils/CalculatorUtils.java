package com.fitnessapp.analytics.utils;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
public class CalculatorUtils {



    public double n(Map<String, Object> m, String k) {
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

    public double n0(Map<String, Object> m, String k) {
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

    public double optional(Map<String, Object> m, String k, double def) {
        if (!m.containsKey(k) || m.get(k) == null) return def;
        try {
            double x = Double.parseDouble(m.get(k).toString());
            if (!Double.isFinite(x) || x < 0) bad(k + " must be zero or a positive number");
            return x;
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, k + " must be numeric");
        }
    }

    public String str(Map<String, Object> m, String k) {
        Object v = m.get(k);
        if (v == null || v.toString().isBlank()) bad("Missing required input: " + k);
        return v.toString();
    }

    public String gender(Map<String, Object> m) {
        String g = str(m, "genderPortal").toLowerCase(Locale.ROOT);
        if (!g.equals("male") && !g.equals("female")) bad("genderPortal must be male or female");
        return g;
    }

    public double activityMultiplier(Map<String, Object> m) {
        return switch (str(m, "activityLevel").toLowerCase(Locale.ROOT)) {
            case "sedentary", "level_1" -> 1.2;
            case "light", "lightly_active", "level_2" -> 1.375;
            case "moderate", "moderately_active", "level_3" -> 1.55;
            case "active", "very_active", "level_4" -> 1.725;
            case "very_active_plus", "athlete", "level_5" -> 1.9;
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown activityLevel");
        };
    }

    public Map<String, Object> scaleFood(Map<String, Object> in, String quantityKey, String calorieKey) {
        return scaleFood(in, quantityKey, calorieKey, false);
    }

    public Map<String, Object> scaleFood(Map<String, Object> in, String quantityKey, String calorieKey, boolean perServing) {
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

    public String assumptions(String key) {
        return switch (key) {
            case "bmr", "tdee" -> "Mifflin-St Jeor equation; adult estimate.";
            case "body-fat" -> "BMI-based population estimate; accuracy varies by individual.";
            case "calories-burned" -> "MET-based estimate; actual energy expenditure varies.";
            case "water-intake" -> "Baseline 35 ml/kg; adjust for climate, exercise and clinical advice.";
            case "vo2-max" -> "Cooper 12-minute test equation; requires a maximal effort test.";
            default -> "Estimate based on the supplied values; individual results may vary.";
        };
    }

    public double round(double v, int scale) {
        return BigDecimal.valueOf(v).setScale(scale, RoundingMode.HALF_UP).doubleValue();
    }



    public void bad(String message) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}

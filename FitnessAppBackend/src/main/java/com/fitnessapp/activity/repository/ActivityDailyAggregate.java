package com.fitnessapp.activity.repository;

import java.math.BigDecimal;

public interface ActivityDailyAggregate {
    Long getActiveSeconds();
    BigDecimal getCaloriesBurned();
    BigDecimal getDistanceMeters();
    Long getSteps();
    Long getCompletedActivities();
}

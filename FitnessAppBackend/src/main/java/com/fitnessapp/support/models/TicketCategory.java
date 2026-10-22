package com.fitnessapp.support.models;

public enum TicketCategory {
    GOOGLE_FIT_SYNC,     // Sensor sync, step counter, bluetooth
    CALORIE_TRACKER,     // Food photo scan, incorrect macro calculation
    WORKOUT_ACTIVITY,    // Timer issue, calories burned metric
    BILLING_SUBSCRIPTION,// Payment failure, upgrade, refund
    ACCOUNT_PROFILE,     // Login, OTP, ML face verification
    BUG_REPORT,          // App crash, UI glitch
    FEATURE_SUGGESTION,  // Feedback, general ideas
    OTHER
}
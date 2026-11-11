package com.fitnessapp.support.models.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SupportIssueCategory {

    ACCOUNT("Account & Login"),
    PAYMENT("Payment & Subscription"),
    WORKOUT("Workout & Exercise"),
    TECHNICAL("Technical Issue"),
    NOTIFICATION("Notifications"),
    GOOGLE_FIT_SYNC("Sensor sync, step counter, bluetooth"),
    CALORIE_TRACKER("Food photo scan, incorrect macro calculation"),
    WORKOUT_ACTIVITY("Timer issue, calories burned metric"),
    BILLING_SUBSCRIPTION("Payment failure, upgrade, refund"),
    ACCOUNT_PROFILE("Login, OTP, ML face verification"),
    BUG_REPORT("App crash, UI glitch"),
    FEATURE_SUGGESTION("Feedback, general ideas"),
    OTHER("Other");

    private final String label;
}
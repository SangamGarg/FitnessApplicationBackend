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
    OTHER("Other");

    private final String label;
}
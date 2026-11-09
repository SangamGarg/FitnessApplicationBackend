package com.fitnessapp.portal.dev.assetUploadDev.model;

public enum AssetCategory {
    // System & Navigation
    APP_BRANDING,          // App logo, splash icon, watermarks
    SYSTEM_MAINTENANCE,    // App force update, under maintenance, blocked screens
    ONBOARDING,            // Tutorial screens, walk-through slides
    AUTH_AND_SECURITY,     // Login, registration, OTP, face verification badges
    ERROR_STATE,           // 404, 500, no internet, timeout, access denied
    EMPTY_STATE,           // No workouts, no recipes, empty notifications, no chat history

    // Fitness & Activity Tracking
    ACTIVITY_TRACKING,     // Running, cycling, gym, timer icons, calorie burn illustrations
    FITNESS_CALCULATORS,   // BMI, BMR, Body Fat, TDEE calculator header graphics
    DIET_AND_RECIPES,      // Meal plan headers, veg/non-veg badges, cuisine cards
    CHALLENGES_AND_REWARDS,// Walk challenge banners, trophy badges, streak milestones
    PROGRESS_REPORTS,      // Weekly/monthly progress summary badges, weight scale visuals

    // Communication & User Engagement
    NOTIFICATION,          // Push notification icons, rich push GIF banners
    EMAIL_TEMPLATES,       // Headers and footers for transactional HTML emails
    COMMUNICATION_CHAT,    // Chatbot avatar, CometChat placeholders, agent icons
    ABOUT_US,              // Company vision, team photos, certifications
    FEEDBACK_AND_SUPPORT,  // Feedback success/failure, helpdesk illustrations
    COMMON_BADGES          // Verified tick, premium/pro badge, placeholder avatars
}
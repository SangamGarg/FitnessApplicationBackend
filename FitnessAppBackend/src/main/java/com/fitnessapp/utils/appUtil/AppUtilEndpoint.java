package com.fitnessapp.utils.appUtil;

public final class AppUtilEndpoint {
    private AppUtilEndpoint() {
    }

    public static final String APP_NAME = "Muscle Play";
    public static final String APP_URL = "https://yourapp.com/open"; // deep link or website


    //Root endpoint for all app-related API requests.
    public static final String APP_BASE_ENDPOINT = "/api/v1/app";
    public static final String APP_AUTH_ENDPOINT = APP_BASE_ENDPOINT + "/auth";
    public static final String APP_OTP_ENDPOINT = APP_BASE_ENDPOINT + "/otp";
    public static final String APP_DEVICES_ENDPOINT = APP_BASE_ENDPOINT + "/devices";
    public static final String APP_NOTIFICATIONS_ENDPOINT = APP_BASE_ENDPOINT + "/notifications";
    public static final String APP_DASHBOARD_ENDPOINT = APP_BASE_ENDPOINT + "/dashboard";
    public static final String APP_ACTIVITY_ENDPOINT = APP_BASE_ENDPOINT + "/activity";
    public static final String APP_GAME_ENDPOINT = APP_BASE_ENDPOINT + "/games";
    public static final String APP_GAMIFICATION_ENDPOINT = APP_BASE_ENDPOINT + "/gamification";
    public static final String APP_NUTRITION_ENDPOINT = APP_BASE_ENDPOINT + "/nutrition";
    public static final String APP_WALLET_ENDPOINT = APP_BASE_ENDPOINT + "/wallet";
    public static final String APP_CHALLENGE_ENDPOINT = APP_BASE_ENDPOINT + "/challenges";
    public static final String APP_ACHIEVEMENT_ENDPOINT = APP_BASE_ENDPOINT + "/achievements";

}

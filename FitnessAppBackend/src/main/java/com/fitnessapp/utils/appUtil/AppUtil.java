package com.fitnessapp.utils.appUtil;

public final class AppUtil {
    private AppUtil() {
    }

    public static final String APP_NAME = "Muscle Play";
    public static final String APP_URL = "https://yourapp.com/open"; // deep link or website


    //Root endpoint for all app-related API requests.
    public static final String APP_BASE_ENDPOINT = "/api/v1/app";
    public static final String APP_AUTH_ENDPOINT = APP_BASE_ENDPOINT + "/auth";
    public static final String APP_OTP_ENDPOINT = APP_BASE_ENDPOINT + "/otp";



}

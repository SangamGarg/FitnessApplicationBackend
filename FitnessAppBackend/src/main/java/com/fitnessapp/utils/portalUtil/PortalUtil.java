package com.fitnessapp.utils.portalUtil;

import java.util.Set;

/**
 * Defines global routing constants and base endpoints for the portal API.
 */
public final class PortalUtil {

    private PortalUtil() {
    }

    public static final String PORTAL_BASE_ENDPOINT = "/api/v1/portal";
    public static final String PORTAL_ADMIN_BASE_ENDPOINT = PORTAL_BASE_ENDPOINT + "/admin";
    public static final String PORTAL_DEV_BASE_ENDPOINT = PORTAL_BASE_ENDPOINT + "/dev";
    /*********************************************************/
    public static final String PORTAL_AUTH_ENDPOINT = PORTAL_BASE_ENDPOINT + "/auth";
    public static final String PORTAL_SUPPORT_ENDPOINT = PORTAL_DEV_BASE_ENDPOINT + "/support";
    public static final String PORTAL_FEEDBACK_ENDPOINT = PORTAL_DEV_BASE_ENDPOINT + "/feedback";
    public static final String PORTAL_FOLLOW_US_ENDPOINT = PORTAL_DEV_BASE_ENDPOINT + "/follow-us";
    public static final String PORTAL_EMAIL_ENDPOINT = PORTAL_DEV_BASE_ENDPOINT + "/email";
    public static final String PORTAL_CALCULATOR_ENDPOINT = PORTAL_DEV_BASE_ENDPOINT + "/calculator";
    public static final String PORTAL_EXERCISE_ENDPOINT = PORTAL_DEV_BASE_ENDPOINT + "/exercise";
    public static final String PORTAL_ASSET_ENDPOINT = PORTAL_DEV_BASE_ENDPOINT + "/assets";
    public static final String PORTAL_NUTRITION_ENDPOINT = PORTAL_DEV_BASE_ENDPOINT + "/nutrition";
    public static final String PORTAL_ADMIN_DEV_ENDPOINT = PORTAL_ADMIN_BASE_ENDPOINT + "/developers";
    public static final String PORTAL_ADMIN_WALLET_ENDPOINT = PORTAL_ADMIN_BASE_ENDPOINT + "/wallets";


    public static final String BEARER_PREFIX = "Bearer ";
    public static final String AUTH_HEADER = "Authorization";

    public static final Set<String> EXCLUDED_PATHS = Set.of(
            PORTAL_AUTH_ENDPOINT + "/login",
            PORTAL_AUTH_ENDPOINT + "/refresh",
            PORTAL_AUTH_ENDPOINT + "/logout",
            PORTAL_AUTH_ENDPOINT + "/setup"
    );

    public static final String CLAIM_TOKEN_TYPE = "token_type";
    public static final String CLAIM_ROLE = "role";
    public static final String CLAIM_ROLE_ID = "roleId";
    public static final String CLAIM_NAME = "name";
    public static final String EXPECTED_TOKEN_TYPE = "portal";
    public static final long EXPIRATION_SECONDS = 1800L; // 30 minutes
    public static final long CLOCK_SKEW_SECONDS = 60L;    // 1 minute tolerance

}
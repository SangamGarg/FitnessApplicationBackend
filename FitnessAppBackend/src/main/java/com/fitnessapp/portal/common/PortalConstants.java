package com.fitnessapp.portal.common;

import java.util.Set;

/**
 * Defines global routing constants and base endpoints for the portal API.
 */
public final class PortalConstants {

    private PortalConstants() {
    }

    //Root endpoint for all portal-related API requests.
    public static final String PORTAL_BASE_URL = "/api/portal";

    //Base endpoint for portal authentication and authorization flows.
    public static final String PORTAL_AUTH_BASE_REQUEST_URL = PORTAL_BASE_URL + "/auth";

    // Base endpoint for admin developer management operations.
    public static final String PORTAL_ADMIN_DEVELOPER_BASE_REQUEST_URL = PORTAL_BASE_URL + "/admin/developers";

    public static final String BEARER_PREFIX = "Bearer ";
    public static final String AUTH_HEADER = "Authorization";

    public static final Set<String> EXCLUDED_PATHS = Set.of(
            "/api/portal/auth/login",
            "/api/portal/auth/refresh",
            "/api/portal/auth/logout",
            "/api/portal/auth/setup"
    );

    public static final String CLAIM_TOKEN_TYPE = "token_type";
    public static final String CLAIM_ROLE = "role";
    public static final String CLAIM_ROLE_ID = "roleId";
    public static final String CLAIM_NAME = "name";
    public static final String EXPECTED_TOKEN_TYPE = "portal";
    public static final long EXPIRATION_SECONDS = 1800L; // 30 minutes
    public static final long CLOCK_SKEW_SECONDS = 60L;    // 1 minute tolerance

}
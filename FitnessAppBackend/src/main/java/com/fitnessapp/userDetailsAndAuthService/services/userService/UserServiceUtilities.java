package com.fitnessapp.userDetailsAndAuthService.services.userService;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class UserServiceUtilities {
    public static String getUserIdFromTokenInHeader() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName(); // This is the Firebase UID from the token
    }
}
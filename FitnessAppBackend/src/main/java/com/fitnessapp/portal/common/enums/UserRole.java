package com.fitnessapp.portal.common.enums;

import java.util.Arrays;

/** Stable role IDs are stored in the database; names are used for authorization and API responses. */
public enum UserRole {
    ADMIN(1),
    DEVELOPER(2);

    private final int id;
    UserRole(int id) { this.id = id; }
    public int getId() { return id; }
    public static UserRole fromId(int id) {
        return Arrays.stream(values()).filter(role -> role.id == id).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown user role id: " + id));
    }
}

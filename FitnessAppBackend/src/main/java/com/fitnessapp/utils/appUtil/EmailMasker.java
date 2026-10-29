package com.fitnessapp.utils.appUtil;

public final class EmailMasker {

    private EmailMasker() {
    }

    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***";
        }

        String[] parts = email.split("@", 2);
        String name = parts[0];
        String domain = parts[1];

        String maskedName;
        if (name.length() <= 1) {
            maskedName = "*";
        } else if (name.length() <= 2) {
            maskedName = name.charAt(0) + "*";
        } else if (name.length() <= 4) {
            maskedName = name.charAt(0) + "***" + name.charAt(name.length() - 1);
        } else {
            // Keep first 2 and last character visible
            maskedName = name.substring(0, 2) + "***" + name.charAt(name.length() - 1);
        }

        return maskedName + "@" + domain;
    }
}
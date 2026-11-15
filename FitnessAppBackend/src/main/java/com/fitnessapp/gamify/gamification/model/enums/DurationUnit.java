package com.fitnessapp.gamify.gamification.model.enums;

public enum DurationUnit {
    SECOND("second"), MINUTE("minute");

    private final String word;

    DurationUnit(String word) {
        this.word = word;
    }

    /** 1 -> "1 minute", 30 -> "30 seconds" */
    public String format(int value) {
        return value + " " + word + (value == 1 ? "" : "s");
    }
}
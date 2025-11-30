package com.github.cybellereaper.model;

public record ItemStats(double damage, double defense, double critChance) {
    public static final ItemStats EMPTY = new ItemStats(0.0, 0.0, 0.0);
}

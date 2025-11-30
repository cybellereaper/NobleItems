package com.github.cybellereaper.model;

public record BlockbenchModel(String modelPath, String texturePath, double scale) {
    public static final BlockbenchModel NONE = new BlockbenchModel("", "", 1.0);
}

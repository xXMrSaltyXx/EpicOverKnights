package com.saltycodes.epicoverknights.items;

/**
 * Defines the weapon types for which blade items exist.
 * Add a new entry here to automatically register blades,
 * models, creative tab entries, and recipe removals for all materials.
 */
public enum BladeType {
    STYLET("stylet"),
    SHORTSWORD("shortsword"),
    KATZBALGER("katzbalger"),
    PIKE("pike"),
    AHLSPIESS("ahlspiess");

    private final String name;

    BladeType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}


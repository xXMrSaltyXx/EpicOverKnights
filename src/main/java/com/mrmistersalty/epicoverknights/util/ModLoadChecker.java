package com.mrmistersalty.epicoverknights.util;

import net.minecraftforge.fml.ModList;

/**
 * Utility class to check if required mods are loaded.
 */
public class ModLoadChecker {
    public static final String EPIC_KNIGHTS_MODID = "magistuarmory";
    public static final String OVERGEARED_MODID = "overgeared";

    /**
     * Check if Epic Knights (Magistuarmory) is loaded.
     */
    public static boolean isEpicKnightsLoaded() {
        return ModList.get().isLoaded(EPIC_KNIGHTS_MODID);
    }

    /**
     * Check if Overgeared is loaded.
     */
    public static boolean isOvergearedLoaded() {
        return ModList.get().isLoaded(OVERGEARED_MODID);
    }

    /**
     * Check if both required mods are loaded.
     * This is the primary check for whether compatibility features should be enabled.
     */
    public static boolean areBothModsLoaded() {
        return isEpicKnightsLoaded() && isOvergearedLoaded();
    }
}

package com.saltycodes.overgearedepicknights.items;

import com.saltycodes.overgearedepicknights.Mappings;

// Each material carries casting/forging recipe data. STONE has no casting or forging — knapping only.
public enum BladeMaterial {
    BRONZE("bronze", "Bronze", true,  0.15f, false, Mappings.COMMON + ":ingots/bronze", "stone"),
    COPPER("copper", "Copper", true,  0.10f, true,  "overgeared:heated_copper_ingot",   "stone"),
    GOLD(  "gold",   "Gold",   true,  0.25f, false, Mappings.COMMON + ":ingots/gold",   "iron"),
    IRON(  "iron",   "Iron",   true,  0.20f, true,  "overgeared:heated_iron_ingot",     "iron"),
    SILVER("silver", "Silver", true,  0.30f, false, Mappings.COMMON + ":ingots/silver", "iron"),
    STEEL( "steel",  "Steel",  true,  0.35f, true,  "overgeared:heated_steel_ingot",    "iron"),
    TIN(   "tin",    "Tin",    true,  0.10f, false, Mappings.COMMON + ":ingots/tin",    "stone"),
    STONE( "stone",  "Stone",  false, 0,     false, null,                               null);

    private final String name;
    private final String displayName;
    private final boolean hasCasting;
    private final float castingXp;
    /** true = use {"item": forgingIngredient}, false = use {"tag": forgingIngredient} */
    private final boolean forgingIsItem;
    /** item ID or tag ID for the forging key; null for STONE */
    private final String forgingIngredient;
    /** required anvil tier: "stone", "iron", or null for STONE */
    private final String forgingTier;

    BladeMaterial(String name, String displayName, boolean hasCasting, float castingXp,
                  boolean forgingIsItem, String forgingIngredient, String forgingTier) {
        this.name = name;
        this.displayName = displayName;
        this.hasCasting = hasCasting;
        this.castingXp = castingXp;
        this.forgingIsItem = forgingIsItem;
        this.forgingIngredient = forgingIngredient;
        this.forgingTier = forgingTier;
    }

    public String getName() { return name; }
    public String getDisplayName() { return displayName; }
    public boolean hasCasting() { return hasCasting; }
    public float getCastingXp() { return castingXp; }
    public boolean isForgingItem() { return forgingIsItem; }
    public String getForgingIngredient() { return forgingIngredient; }
    public String getForgingTier() { return forgingTier; }
}

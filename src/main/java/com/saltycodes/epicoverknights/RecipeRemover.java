package com.saltycodes.epicoverknights;

import com.saltycodes.epicoverknights.items.BladeMaterial;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mod.EventBusSubscriber(modid = EpicOverKnights.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RecipeRemover {

    /**
     * Weapon types from magistuarmory whose recipes should be removed for every external recipe material.
     * To also remove recipes for a new weapon type, add it here by its magistuarmory name.
     */
    private static final String[] MAGISTU_WEAPON_TYPES = {"stylet", "shortsword", "katzbalger", "pike", "ranseur", "ahlspiess", "bastardsword", "estoc", "claymore", "zweihander", "lochaberaxe", "concavehalberd", "heavymace"};

    /**
     * Material names used by magistuarmory recipe IDs.
     * This intentionally includes external materials like diamond without making them local BladeMaterials.
     */
    private static final Set<String> MAGISTU_RECIPE_MATERIALS = Stream.concat(
            Arrays.stream(BladeMaterial.values()).map(BladeMaterial::getName),
            Stream.of("diamond")
    ).collect(Collectors.toUnmodifiableSet());

    /**
     * Extra magistuarmory recipes that are not tied to a material/weapon combination.
     */
    private static final Set<ResourceLocation> EXTRA_RECIPES_TO_REMOVE = Set.of(
            ResourceLocation.fromNamespaceAndPath("magistuarmory", "jousting_boots"),
            ResourceLocation.fromNamespaceAndPath("magistuarmory", "jousting_leggings"),
            ResourceLocation.fromNamespaceAndPath("magistuarmory", "jousting_chestplate"),
            ResourceLocation.fromNamespaceAndPath("magistuarmory", "stechhelm"),
            ResourceLocation.fromNamespaceAndPath("magistuarmory", "steel_plate"),
            ResourceLocation.fromNamespaceAndPath("magistuarmory", "small_steel_plate"),
            ResourceLocation.fromNamespaceAndPath("magistuarmory", "steel_ingot_blasting"),
            ResourceLocation.fromNamespaceAndPath("magistuarmory", "steel_ingot_to_steel_nuggets"),
            ResourceLocation.fromNamespaceAndPath("magistuarmory", "steel_nuggets_to_steel_ingot"),
            ResourceLocation.fromNamespaceAndPath("magistuarmory", "furnace/steel_ingot_blasting"),
            ResourceLocation.fromNamespaceAndPath("magistuarmory", "furnace/steel_nugget_blasting")
    );

    private static final Set<ResourceLocation> RECIPES_TO_REMOVE = buildRecipesToRemove();

    private static Set<ResourceLocation> buildRecipesToRemove() {
        Set<ResourceLocation> toRemove = new HashSet<>(EXTRA_RECIPES_TO_REMOVE);
        for (String weaponType : MAGISTU_WEAPON_TYPES) {
            for (String material : MAGISTU_RECIPE_MATERIALS) {
                toRemove.add(ResourceLocation.fromNamespaceAndPath(
                        "magistuarmory", material + "_" + weaponType));
            }
        }
        return Set.copyOf(toRemove);
    }

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        RecipeManager recipeManager = event.getServer().getRecipeManager();
        Map<ResourceLocation, Recipe<?>> recipes = new HashMap<>();

        // Collect all recipes except the ones we want to remove
        recipeManager.getRecipes().forEach(recipe -> {
            if (!RECIPES_TO_REMOVE.contains(recipe.getId())) {
                recipes.put(recipe.getId(), recipe);
            }
        });

        recipeManager.replaceRecipes(recipes.values());
    }
}
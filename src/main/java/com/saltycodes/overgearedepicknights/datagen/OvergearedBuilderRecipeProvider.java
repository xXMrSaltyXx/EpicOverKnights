package com.saltycodes.overgearedepicknights.datagen;

import com.saltycodes.overgearedepicknights.Mappings;
import com.saltycodes.overgearedepicknights.items.BladeMaterial;
import com.saltycodes.overgearedepicknights.items.BladeType;
import com.saltycodes.overgearedepicknights.items.ModItems;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.stirdrem.overgeared.datagen.CastingRecipeBuilder;
import net.stirdrem.overgeared.datagen.ShapedForgingRecipeBuilder;
import net.stirdrem.overgeared.datagen.ToolCastBlastingRecipeBuilder;
import net.stirdrem.overgeared.datagen.ToolCastSmeltingRecipeBuilder;
import net.stirdrem.overgeared.client.ForgingBookCategory;
import net.stirdrem.overgeared.AnvilTier;
import net.stirdrem.overgeared.ForgingQuality;
//? if forge {
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Consumer;
//?} else {
/*import net.minecraft.advancements.Criterion;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeOutput;

import java.util.concurrent.CompletableFuture;
*///?}

import java.util.Locale;
import java.util.Map;

// Builder-based recipes: casting, blade forging, plate forging.
// Everything else is in OvergearedRecipeProvider. With addon=true only the Epic Knights: Addon
// blades are written (into the addon datapack, under the addon/ recipe path).
public class OvergearedBuilderRecipeProvider extends RecipeProvider {

    private final String modId;
    private final boolean addon;

    //? if forge {
    public OvergearedBuilderRecipeProvider(PackOutput output, String modId, boolean addon) {
        super(output);
        this.modId = modId;
        this.addon = addon;
    }
    //?} else {
    /*public OvergearedBuilderRecipeProvider(PackOutput output,
                                           CompletableFuture<HolderLookup.Provider> lookupProvider,
                                           String modId, boolean addon) {
        super(output, lookupProvider);
        this.modId = modId;
        this.addon = addon;
    }
    *///?}

    @Override
    //? if forge {
    protected void buildRecipes(Consumer<FinishedRecipe> output) {
    //?} else {
    /*protected void buildRecipes(RecipeOutput output) {
    *///?}
        buildCasting(output);
        buildForging(output);
        if (!addon) buildPlateForging(output);
    }

    private String prefix() { return addon ? "addon/" : ""; }

    // ── Casting ──────────────────────────────────────────────────────────────

    //? if forge {
    private void buildCasting(Consumer<FinishedRecipe> output) {
    //?} else {
    /*private void buildCasting(RecipeOutput output) {
    *///?}
        for (BladeType type : BladeType.of(addon)) {
            if (!type.hasCasting()) continue;
            for (BladeMaterial mat : type.getMaterials()) {
                if (!mat.hasCasting()) continue;
                //? if forge {
                Item blade = ModItems.getBlade(type, mat).get();
                //?} else {
                /*ItemLike blade = ModItems.getBlade(type, mat);
                *///?}
                String ts     = type.getName() + type.getSuffix();
                String prefix = mat.getName() + "_" + ts;
                ResourceLocation base = rl(prefix() + "casting/" + ts + "/" + prefix);

                CastingRecipeBuilder.casting(blade, mat.getCastingXp(), 150)
                        .toolType(type.getTooltype())
                        .material(mat.getName(), type.getCastingAmount())
                        .needsPolishing(true)
                        .unlockedBy("has_blade", hasItem(blade))
                        .save(output, base);

                ToolCastSmeltingRecipeBuilder.cast(blade, mat.getCastingXp(), 150)
                        .toolType(type.getTooltype())
                        .material(mat.getName(), type.getCastingAmount())
                        .needsPolishing(true)
                        .unlockedBy("has_blade", hasItem(blade))
                        .save(output, base);

                ToolCastBlastingRecipeBuilder.cast(blade, mat.getCastingXp(), 150)
                        .toolType(type.getTooltype())
                        .material(mat.getName(), type.getCastingAmount())
                        .needsPolishing(true)
                        .unlockedBy("has_blade", hasItem(blade))
                        .save(output, base);
            }
        }
    }

    // ── Forging ───────────────────────────────────────────────────────────────

    //? if forge {
    private void buildForging(Consumer<FinishedRecipe> output) {
    //?} else {
    /*private void buildForging(RecipeOutput output) {
    *///?}
        for (BladeType type : BladeType.of(addon)) {
            for (BladeMaterial mat : type.getMaterials()) {
                if (mat == BladeMaterial.STONE) continue;   // stone blades are knapped, not forged
                //? if forge {
                Item      blade = ModItems.getBlade(type, mat).get();
                //?} else {
                /*ItemLike  blade = ModItems.getBlade(type, mat);
                *///?}
                AnvilTier tier  = mat.getForgingTier().equals("iron") ? AnvilTier.IRON : AnvilTier.STONE;
                String    ts    = type.getName() + type.getSuffix();
                String    name  = mat.getName() + "_" + ts;

                var b = ShapedForgingRecipeBuilder.shaped(ForgingBookCategory.MISC, blade, type.getForgeHammering())
                        .tier(tier)
                        .setBlueprint(type.getTooltype())
                        .requiresBlueprint(false)
                        .setQuality(true)
                        .minimumQuality(ForgingQuality.POOR)
                        .setPolishing(true)
                        .setNeedQuenching(true)
                        .needsMinigame(true)
                        .unlockedBy("has_blade", hasItem(blade));

                for (String row : type.getForgePattern()) b.pattern(row);

                for (Map.Entry<Character, String> key : type.getForgeKeys().entrySet()) {
                    defineKey(b, key.getKey(), key.getValue(), mat);
                }

                b.save(output, rl(prefix() + "forging/" + ts + "/" + name));
            }
        }
    }

    /** Resolves a BladeType key spec (see its class comment) for one material. */
    private static void defineKey(ShapedForgingRecipeBuilder b, char c, String spec, BladeMaterial mat) {
        if (spec.equals("ingot")) {
            if (mat.isForgingItem()) {
                b.define(c, fromId(mat.getForgingIngredient()));          // heated ingot item
            } else {
                b.define(c, ItemTags.create(ResourceLocation.parse(mat.getForgingIngredient())));   // common ingot tag
            }
        } else if (spec.equals("ingot_tag")) {
            b.define(c, ItemTags.create(ResourceLocation.parse(Mappings.COMMON + ":ingots/" + mat.getName())));
        } else if (spec.startsWith("blade:")) {
            BladeType other = BladeType.valueOf(spec.substring("blade:".length()).toUpperCase(Locale.ROOT));
            //? if forge {
            b.define(c, ModItems.getBlade(other, mat).get());
            //?} else {
            /*b.define(c, ModItems.getBlade(other, mat));
            *///?}
        } else if (spec.startsWith("item:")) {
            b.define(c, fromId(spec.substring("item:".length())));
        } else if (spec.startsWith("tag:")) {
            b.define(c, ItemTags.create(ResourceLocation.parse(spec.substring("tag:".length()))));
        } else {
            throw new IllegalArgumentException("Unknown forging key spec: " + spec);
        }
    }

    // ── Plate forging ─────────────────────────────────────────────────────────

    //? if forge {
    private void buildPlateForging(Consumer<FinishedRecipe> output) {
        plateForge(output, BladeMaterial.BRONZE, ModItems.BRONZE_PLATE.get());
        plateForge(output, BladeMaterial.GOLD,   ModItems.GOLD_PLATE.get());
        plateForge(output, BladeMaterial.SILVER, ModItems.SILVER_PLATE.get());
        plateForge(output, BladeMaterial.TIN,    ModItems.TIN_PLATE.get());
    }
    //?} else {
    /*private void buildPlateForging(RecipeOutput output) {
        plateForge(output, BladeMaterial.BRONZE, ModItems.BRONZE_PLATE);
        plateForge(output, BladeMaterial.GOLD,   ModItems.GOLD_PLATE);
        plateForge(output, BladeMaterial.SILVER, ModItems.SILVER_PLATE);
        plateForge(output, BladeMaterial.TIN,    ModItems.TIN_PLATE);
    }
    *///?}

    //? if forge {
    private void plateForge(Consumer<FinishedRecipe> output, BladeMaterial mat, Item plate) {
    //?} else {
    /*private void plateForge(RecipeOutput output, BladeMaterial mat, ItemLike plate) {
    *///?}
        ShapedForgingRecipeBuilder.shaped(ForgingBookCategory.MISC, plate, 3)
                .tier(AnvilTier.STONE)
                .setQuality(false)
                .setNeedQuenching(false)
                .needsMinigame(false)
                .pattern("#  ")
                .pattern("   ")
                .pattern("   ")
                .define('#', ItemTags.create(ResourceLocation.parse(Mappings.COMMON + ":ingots/" + mat.getName())))
                .unlockedBy("has_plate", hasItem(plate))
                .save(output, rl("forging/plate/" + mat.getName() + "_plate"));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(modId, path);
    }

    //? if forge {
    private static InventoryChangeTrigger.TriggerInstance hasItem(ItemLike item) {
        return has(item);
    }
    //?} else {
    /*private static Criterion<?> hasItem(ItemLike item) {
        return InventoryChangeTrigger.TriggerInstance.hasItems(item);
    }
    *///?}

    /** Resolve a full "namespace:path" item ID to an ItemLike at recipe-save time. */
    //? if forge {
    private static ItemLike fromId(String fullId) {
        return ForgeRegistries.ITEMS.getValue(ResourceLocation.parse(fullId));
    }
    //?} else {
    /*private static ItemLike fromId(String fullId) {
        ResourceLocation rl = ResourceLocation.parse(fullId);
        return () -> BuiltInRegistries.ITEM.get(rl);
    }
    *///?}
}

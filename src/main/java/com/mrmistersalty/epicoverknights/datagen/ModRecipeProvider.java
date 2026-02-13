package com.mrmistersalty.epicoverknights.datagen;

import com.mrmistersalty.epicoverknights.items.ModItems;
import java.util.function.Consumer;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.world.item.Items;
import net.stirdrem.overgeared.AnvilTier;
import net.stirdrem.overgeared.datagen.ShapedForgingRecipeBuilder;
import org.checkerframework.checker.nullness.qual.NonNull;

public class ModRecipeProvider extends RecipeProvider {
  public ModRecipeProvider(PackOutput output) {
    super(output);
  }

  @Override
  protected void buildRecipes(Consumer<FinishedRecipe> consumer) {
    ShapedForgingRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.COPPER_STYLET_BLADE.get(), 3)
            .tier(AnvilTier.STONE)
            .setBlueprint("stylet")
            .setPolishing(true)
            .setQuality(true)
            .define('#', net.stirdrem.overgeared.item.ModItems.HEATED_COPPER_INGOT.get())
            .pattern("  #").unlockedBy(getHasName(Items.COPPER_INGOT), has(Items.COPPER_INGOT))
            .save(consumer);
  }
}

package com.mrmistersalty.epicoverknights.datagen;

import com.mrmistersalty.epicoverknights.EpicOverKnights;
import com.mrmistersalty.epicoverknights.items.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, EpicOverKnights.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        simpleItem(ModItems.COPPER_STYLET_BLADE);
        simpleItem(ModItems.GOLD_STYLET_BLADE);
        simpleItem(ModItems.TIN_STYLET_BLADE);
        simpleItem(ModItems.STONE_STYLET_BLADE);
        simpleItem(ModItems.SILVER_STYLET_BLADE);
        simpleItem(ModItems.BRONZE_STYLET_BLADE);
        simpleItem(ModItems.IRON_STYLET_BLADE);
        simpleItem(ModItems.STEEL_STYLET_BLADE);
    }

    private void registerStyletBlades() {
        simpleItem(ModItems.COPPER_STYLET_BLADE);
        simpleItem(ModItems.GOLD_STYLET_BLADE);
        simpleItem(ModItems.TIN_STYLET_BLADE);
        simpleItem(ModItems.STONE_STYLET_BLADE);
        simpleItem(ModItems.SILVER_STYLET_BLADE);
        simpleItem(ModItems.BRONZE_STYLET_BLADE);
        simpleItem(ModItems.IRON_STYLET_BLADE);
        simpleItem(ModItems.STEEL_STYLET_BLADE);
    }

    private ItemModelBuilder simpleItem(RegistryObject<Item> item) {
        return withExistingParent(item.getId().getPath(),
                new ResourceLocation("item/generated")).texture("layer0",
                new ResourceLocation(EpicOverKnights.MODID, "item/" + item.getId().getPath()));
    }
}

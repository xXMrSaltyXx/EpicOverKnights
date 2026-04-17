package com.saltycodes.epicoverknights.datagen;

import com.saltycodes.epicoverknights.EpicOverKnights;
import com.saltycodes.epicoverknights.items.BladeMaterial;
import com.saltycodes.epicoverknights.items.BladeType;
import com.saltycodes.epicoverknights.items.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, EpicOverKnights.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        for (BladeType type : BladeType.values()) {
            for (BladeMaterial material : type.getMaterials()) {
                simpleItem(ModItems.getBlade(type, material));
            }
        }
        simpleItem(ModItems.GOLD_PLATE);
        simpleItem(ModItems.BRONZE_PLATE);
        simpleItem(ModItems.TIN_PLATE);
        simpleItem(ModItems.SILVER_PLATE);
        simpleItem(ModItems.CRUSADER_SURCOAT);
    }

    private void simpleItem(RegistryObject<Item> item) {
        assert item.getId() != null;
        withExistingParent(item.getId().getPath(),
                ResourceLocation.parse("item/generated")).texture("layer0",
                ResourceLocation.fromNamespaceAndPath(EpicOverKnights.MODID, "item/" + item.getId().getPath()));
    }
}

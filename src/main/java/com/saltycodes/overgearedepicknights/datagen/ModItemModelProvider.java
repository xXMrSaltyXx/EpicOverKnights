package com.saltycodes.overgearedepicknights.datagen;

import com.saltycodes.overgearedepicknights.OvergearedEpicKnights;
import com.saltycodes.overgearedepicknights.items.BladeMaterial;
import com.saltycodes.overgearedepicknights.items.BladeType;
import com.saltycodes.overgearedepicknights.items.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
//? if forge {
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;
//?} else {
/*import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredItem;
*///?}

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, OvergearedEpicKnights.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        // Addon blades are only registered (and therefore only modelled) when the addon is on the
        // DataGen classpath; their textures still ship unconditionally, which is harmless.
        for (BladeType type : BladeType.active()) {
            for (BladeMaterial material : type.getMaterials()) {
                simpleItem(ModItems.getBlade(type, material));
            }
        }
        simpleItem(ModItems.GOLD_PLATE);
        simpleItem(ModItems.BRONZE_PLATE);
        simpleItem(ModItems.TIN_PLATE);
        simpleItem(ModItems.SILVER_PLATE);
        simpleItem(ModItems.CRUSADER_SURCOAT);
        simpleItem(ModItems.HUSSAR_WINGS);
    }

    //? if forge {
    private void simpleItem(RegistryObject<Item> item) {
        assert item.getId() != null;
        withExistingParent(item.getId().getPath(),
                ResourceLocation.parse("item/generated")).texture("layer0",
                ResourceLocation.fromNamespaceAndPath(OvergearedEpicKnights.MODID, "item/" + item.getId().getPath()));
    }
    //?} else {
    /*private void simpleItem(DeferredItem<Item> item) {
        String path = item.getKey().location().getPath();
        withExistingParent(path, ResourceLocation.parse("item/generated"))
                .texture("layer0", ResourceLocation.fromNamespaceAndPath(
                        OvergearedEpicKnights.MODID, "item/" + path));
    }
    *///?}
}

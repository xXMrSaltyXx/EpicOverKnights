package com.saltycodes.epicoverknights.items;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModCreativeModeTabs {
    @SubscribeEvent
    public static void onBuildCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().location().equals(ResourceLocation.fromNamespaceAndPath("overgeared", "overgeared_tab"))) {
            for (BladeType type : BladeType.values()) {
                for (BladeMaterial material : type.getMaterials()) {
                    event.accept(ModItems.getBlade(type, material).get());
                }
            }
            event.accept(ModItems.GOLD_PLATE.get());
            event.accept(ModItems.BRONZE_PLATE.get());
            event.accept(ModItems.TIN_PLATE.get());
            event.accept(ModItems.SILVER_PLATE.get());
            event.accept(ModItems.CRUSADER_SURCOAT.get());
        }
    }
}

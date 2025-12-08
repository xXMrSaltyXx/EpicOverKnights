package com.mrmistersalty.epicoverknights.items;

import com.mrmistersalty.epicoverknights.EpicOverKnights;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModCreativeModeTabs {
  @SubscribeEvent
  public static void onBuildCreativeTab(BuildCreativeModeTabContentsEvent event) {
    if (event.getTabKey().location().equals(new ResourceLocation("overgeared", "overgeared_tab"))) {
      event.accept(ModItems.STEEL_STYLET_BLADE.get());
    }
  }
}

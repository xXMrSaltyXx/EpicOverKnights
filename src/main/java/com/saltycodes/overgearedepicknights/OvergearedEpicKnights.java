package com.saltycodes.overgearedepicknights;

import com.saltycodes.overgearedepicknights.items.ModItems;
//? if forge {
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
//?} else {
/*import com.saltycodes.overgearedepicknights.datagen.DataGenerators;
import com.saltycodes.overgearedepicknights.items.ModCreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
*///?}

@Mod(OvergearedEpicKnights.MODID)
public class OvergearedEpicKnights {
    public static final String MODID = "overgeared_epic_knights";
    /** Epic Knights: Addon — optional; its blades and recipes only exist when it is loaded. */
    public static final String ADDON_MODID = "magistuarmoryaddon";

    //? if forge {
    public OvergearedEpicKnights() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModItems.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);
    }
    //?} else {
    /*public OvergearedEpicKnights(IEventBus modEventBus, ModContainer modContainer) {
        ModItems.register(modEventBus);
        modEventBus.addListener(ModCreativeModeTabs::onBuildCreativeTab);
        modEventBus.addListener(AddonDatapack::onAddPackFinders);
        modEventBus.addListener(DataGenerators::gatherData);
    }
    *///?}
}

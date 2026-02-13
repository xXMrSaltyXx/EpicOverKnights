package com.mrmistersalty.epicoverknights;

import com.mrmistersalty.epicoverknights.items.ModItems;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * Main mod class for EpicOverKnights - a compatibility mod between Epic Knights and Overgeared.
 * This mod only initializes when both parent mods are loaded.
 */
@Mod(EpicOverKnights.MODID)
public class EpicOverKnights {
    public static final String MODID = "epicoverknights";

    public EpicOverKnights() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register deferred registers - must be done before checking mods
        // This is required for data generation to work
        ModItems.register(modEventBus);

        // Register to Forge event bus for game events
        MinecraftForge.EVENT_BUS.register(this);
    }
}

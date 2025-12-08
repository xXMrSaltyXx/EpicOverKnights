package com.mrmistersalty.epicoverknights;

import com.mojang.logging.LogUtils;
import com.mrmistersalty.epicoverknights.integration.EpicKnightsIntegration;
import com.mrmistersalty.epicoverknights.integration.OvergearedIntegration;
import com.mrmistersalty.epicoverknights.items.ModItems;
import com.mrmistersalty.epicoverknights.util.ModLoadChecker;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

/**
 * Main mod class for EpicOverKnights - a compatibility mod between Epic Knights and Overgeared.
 * This mod only initializes when both parent mods are loaded.
 */
@Mod(EpicOverKnights.MODID)
public class EpicOverKnights {
    public static final String MODID = "epicoverknights";
    private static final Logger LOGGER = LogUtils.getLogger();

    public EpicOverKnights() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Verify that both required mods are loaded
        if (!ModLoadChecker.areBothModsLoaded()) {
            LOGGER.warn("EpicOverKnights requires both Epic Knights and Overgeared to be installed!");
            return;
        }

        LOGGER.info("EpicOverKnights initializing - Both parent mods detected");

        // Register deferred registers
        ModItems.register(modEventBus);

        // Register lifecycle event listeners
        modEventBus.addListener(this::commonSetup);

        // Register to Forge event bus for game events
        MinecraftForge.EVENT_BUS.register(this);

        // Register config
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("EpicOverKnights common setup starting");

        // Initialize integrationsin parallel-safe way
        event.enqueueWork(() -> {
            EpicKnightsIntegration.init();
            OvergearedIntegration.init();
            LOGGER.info("EpicOverKnights compatibility features initialized");
        });
    }

    /**
     * Client-side setup events
     */
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            LOGGER.info("EpicOverKnights client setup complete");
        }
    }
}

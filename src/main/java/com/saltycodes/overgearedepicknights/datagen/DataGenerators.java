package com.saltycodes.overgearedepicknights.datagen;

import com.saltycodes.overgearedepicknights.AddonDatapack;
import com.saltycodes.overgearedepicknights.OvergearedEpicKnights;
import com.saltycodes.overgearedepicknights.items.BladeType;
import net.minecraft.SharedConstants;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.metadata.PackMetadataGenerator;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;

import java.util.concurrent.CompletableFuture;
//? if forge {
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
//?} else {
/*import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.Optional;
*///?}

//? if forge {
@Mod.EventBusSubscriber(modid = OvergearedEpicKnights.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
//?}
public class DataGenerators {
    //? if forge {
    @SubscribeEvent
    //?}
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        // ── Client assets (models, names) — cover the addon items too ──────────
        generator.addProvider(event.includeClient(),
                new ModItemModelProvider(packOutput, existingFileHelper));
        generator.addProvider(event.includeClient(),
                new ModLanguageProvider(packOutput));

        // ── Base data ────────────────────────────────────────────────────────
        addData(event, generator, packOutput, false);

        // ── Epic Knights: Addon data → built-in datapack, only enabled at runtime with the addon ──
        // Needs the addon on the DataGen classpath so its items and our addon blades exist.
        if (BladeType.isAddonLoaded()) {
            PackOutput addonOutput = new PackOutput(packOutput.getOutputFolder().resolve(AddonDatapack.PATH));
            generator.addProvider(event.includeServer(), new PackMetadataGenerator(addonOutput)
                    .add(PackMetadataSection.TYPE, packMetadata()));
            addData(event, generator, addonOutput, true);
        }
    }

    private static void addData(GatherDataEvent event, DataGenerator generator, PackOutput output, boolean addon) {
        DataProvider builderRecipes =
                //? if forge {
                new OvergearedBuilderRecipeProvider(output, OvergearedEpicKnights.MODID, addon);
                //?} else {
                /*new OvergearedBuilderRecipeProvider(output, event.getLookupProvider(), OvergearedEpicKnights.MODID, addon);
                *///?}
        // RecipeProvider#getName is final ("Recipes") and DataGenerator rejects duplicate names,
        // so the addon instance runs behind a renamed delegate.
        generator.addProvider(event.includeServer(), addon ? named(builderRecipes, "Recipes (addon)") : builderRecipes);
        generator.addProvider(event.includeServer(),
                new OvergearedRecipeProvider(output, OvergearedEpicKnights.MODID, addon));
        generator.addProvider(event.includeServer(),
                new OvergearedStaticDataProvider(output, addon));
    }

    private static DataProvider named(DataProvider delegate, String name) {
        return new DataProvider() {
            @Override public CompletableFuture<?> run(CachedOutput cache) { return delegate.run(cache); }
            @Override public String getName() { return name; }
        };
    }

    private static PackMetadataSection packMetadata() {
        int format = SharedConstants.getCurrentVersion().getPackVersion(PackType.SERVER_DATA);
        //? if forge {
        return new PackMetadataSection(Component.literal(AddonDatapack.TITLE), format);
        //?} else {
        /*return new PackMetadataSection(Component.literal(AddonDatapack.TITLE), format, Optional.empty());
        *///?}
    }
}

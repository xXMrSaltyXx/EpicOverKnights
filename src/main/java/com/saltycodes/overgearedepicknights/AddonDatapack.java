package com.saltycodes.overgearedepicknights;

import com.saltycodes.overgearedepicknights.items.BladeType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
//? if forge {
import net.minecraft.server.packs.PackResources;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.resource.PathPackResources;

import java.nio.file.Path;
//?} else {
/*import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.event.AddPackFindersEvent;
*///?}

/**
 * Everything that belongs to Epic Knights: Addon (recipes, tool types, tags) is generated into a
 * built-in datapack at {@code datapacks/addon} in the root of this jar. The vanilla loader ignores
 * that folder; this class registers it as an always-active pack — but only when the addon mod is
 * present, so a world without the addon never sees recipes for missing items.
 * (NeoForge resolves pack locations relative to the jar root, hence no {@code data/<modid>/} prefix.)
 */
//? if forge {
@Mod.EventBusSubscriber(modid = OvergearedEpicKnights.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
//?}
public class AddonDatapack {
    /** Pack folder relative to the jar root; DataGen writes here (see DataGenerators). */
    public static final String PATH = "datapacks/addon";
    public static final String PACK_ID = OvergearedEpicKnights.MODID + ":addon";
    public static final String TITLE = "Overgeared x Epic Knights: Addon";

    //? if forge {
    @SubscribeEvent
    //?}
    public static void onAddPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.SERVER_DATA || !BladeType.isAddonLoaded()) return;
        //? if forge {
        Path root = ModList.get().getModFileById(OvergearedEpicKnights.MODID).getFile()
                .findResource(PATH.split("/"));
        PackResources resources = new PathPackResources(PACK_ID, true, root);
        event.addRepositorySource(consumer -> consumer.accept(Pack.readMetaAndCreate(
                PACK_ID, Component.literal(TITLE), true, id -> resources,
                PackType.SERVER_DATA, Pack.Position.TOP, PackSource.BUILT_IN)));
        //?} else {
        /*event.addPackFinders(
                ResourceLocation.fromNamespaceAndPath(OvergearedEpicKnights.MODID, PATH),
                PackType.SERVER_DATA, Component.literal(TITLE), PackSource.BUILT_IN, true, Pack.Position.TOP);
        *///?}
    }
}

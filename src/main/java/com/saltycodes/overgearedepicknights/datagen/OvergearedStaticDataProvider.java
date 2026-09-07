package com.saltycodes.overgearedepicknights.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.saltycodes.overgearedepicknights.Mappings;
import com.saltycodes.overgearedepicknights.items.BladeMaterial;
import com.saltycodes.overgearedepicknights.items.BladeType;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

// Generates blueprint_tooltypes, casting_tooltypes, knapping_resources, and item tags.
// With addon=true only the Epic Knights: Addon share of that is written (into the addon datapack).
public class OvergearedStaticDataProvider implements DataProvider {

    private final PackOutput packOutput;
    private final boolean addon;

    public OvergearedStaticDataProvider(PackOutput packOutput, boolean addon) {
        this.packOutput = packOutput;
        this.addon = addon;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        List<CompletableFuture<?>> futures = new ArrayList<>();
        generateBlueprintTooltypes(cache, futures);
        generateCastingTooltypes(cache, futures);
        generateForgedSteelTag(cache, futures);
        if (!addon) {
            generateKnappingResources(cache, futures);
            generateKnappablesTag(cache, futures);
            generateSteelTags(cache, futures);
        }
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "Overgeared Static Data Provider" + (addon ? " (addon)" : "");
    }

    private String fileName(String base) {
        return addon ? "epicoverknights_addon_" + base : "epicoverknights_" + base;
    }

    // ── blueprint_tooltypes ───────────────────────────────────────────────────

    private void generateBlueprintTooltypes(CachedOutput cache, List<CompletableFuture<?>> futures) {
        Set<String> tooltypes = new LinkedHashSet<>();
        for (BladeType type : BladeType.of(addon)) {
            tooltypes.add(type.getTooltype());
        }
        if (!addon) tooltypes.addAll(ForgingTable.SHIELDS.keySet());
        for (ForgingTable.Entry entry : ForgingTable.of(addon)) {
            if (entry.blueprint() != null) tooltypes.add(entry.blueprint());
        }
        JsonObject obj = new JsonObject();
        JsonArray arr = new JsonArray();
        for (String t : tooltypes) arr.add(t.toUpperCase(Locale.ROOT));
        obj.add("tooltypes", arr);
        saveTo(cache, futures, "overgeared", "blueprint_tooltypes/" + fileName("blueprint_tooltypes"), obj);
    }

    // ── casting_tooltypes ─────────────────────────────────────────────────────

    private void generateCastingTooltypes(CachedOutput cache, List<CompletableFuture<?>> futures) {
        JsonObject obj = new JsonObject();
        JsonArray tools = new JsonArray();
        for (BladeType type : BladeType.of(addon)) {
            if (!type.isCastableType()) continue;
            JsonArray entry = new JsonArray();
            entry.add(type.getTooltype());
            entry.add(type.getCastingAmount());
            tools.add(entry);
        }
        if (!addon) {
            for (String shield : new String[]{"buckler", "target"}) {
                JsonArray entry = new JsonArray();
                entry.add(shield);
                entry.add(36);
                tools.add(entry);
            }
        }
        obj.add("tools", tools);
        saveTo(cache, futures, "overgeared", "casting_tooltypes/" + fileName("casting_tooltypes"), obj);
    }

    // ── knapping_resources ────────────────────────────────────────────────────

    private void generateKnappingResources(CachedOutput cache, List<CompletableFuture<?>> futures) {
        JsonObject obj = new JsonObject();
        JsonArray knapping = new JsonArray();
        JsonObject entry = new JsonObject();
        entry.addProperty("item", "overgeared:cobblestone");
        entry.addProperty("texture", "minecraft:textures/block/glass.png");
        entry.addProperty("sound", "minecraft:block.stone.break");
        knapping.add(entry);
        obj.add("knapping", knapping);
        saveTo(cache, futures,
                "overgeared", "knapping_resources/cobblestone_knapping", obj);
    }

    // ── overgeared/tags/items/knappables ──────────────────────────────────────

    private void generateKnappablesTag(CachedOutput cache, List<CompletableFuture<?>> futures) {
        JsonObject obj = new JsonObject();
        obj.addProperty("replace", false);
        JsonArray values = new JsonArray();
        values.add("minecraft:cobblestone");
        obj.add("values", values);
        saveTo(cache, futures,
                "overgeared", Mappings.TAG_ITEM_DIR + "/knappables", obj);
    }

    // ── common steel tags: Epic Knights' steel is replaced by Overgeared's ────

    private void generateSteelTags(CachedOutput cache, List<CompletableFuture<?>> futures) {
        for (String[] tag : new String[][]{
                {"ingots/steel",  "overgeared:steel_ingot"},
                {"nuggets/steel", "overgeared:steel_nugget"},
                {"plates/steel",  "overgeared:steel_plate"}}) {
            JsonObject obj = new JsonObject();
            obj.addProperty("replace", true);
            JsonArray vals = new JsonArray();
            vals.add(tag[1]);
            obj.add("values", vals);
            saveTo(cache, futures, Mappings.COMMON, Mappings.TAG_ITEM_DIR + "/" + tag[0], obj);
        }
    }

    // ── forged/steel: steel gear that can be blasted back into nuggets ───────

    private static final String[] BASE_FORGED_STEEL = {
            "magistuarmory:steel_chivalrylance", "magistuarmory:steel_flamebladedsword",
            "magistuarmory:steel_chainmorgenstern",
            "magistuarmory:barbute","magistuarmory:halfarmor_chestplate","magistuarmory:armet",
            "magistuarmory:knight_chestplate","magistuarmory:knight_leggings","magistuarmory:knight_boots",
            "magistuarmory:sallet","magistuarmory:gothic_chestplate","magistuarmory:gothic_leggings",
            "magistuarmory:gothic_boots","magistuarmory:stechhelm","magistuarmory:jousting_chestplate",
            "magistuarmory:jousting_leggings","magistuarmory:jousting_boots","magistuarmory:maximilian_helmet",
            "magistuarmory:maximilian_chestplate","magistuarmory:maximilian_leggings","magistuarmory:maximilian_boots",
            "magistuarmory:chainmail_helmet","magistuarmory:chainmail_chestplate","magistuarmory:chainmail_leggings",
            "magistuarmory:kettlehat","magistuarmory:platemail_chestplate","magistuarmory:platemail_leggings",
            "magistuarmory:platemail_boots",
            "magistuarmory:blacksmith_hammer","magistuarmory:greathelm","magistuarmory:crusader_chestplate",
            "magistuarmory:crusader_leggings","magistuarmory:ceremonialarmet","magistuarmory:ceremonial_chestplate",
            "magistuarmory:ceremonial_boots","magistuarmory:brigandine_chestplate","magistuarmory:norman_helmet",
            "magistuarmory:shishak","magistuarmory:rustedbarbute","magistuarmory:rustedhalfarmor_chestplate",
            "magistuarmory:rustedgreathelm","magistuarmory:rustedcrusader_chestplate","magistuarmory:rustednorman_helmet",
            "magistuarmory:rustedchainmail_helmet","magistuarmory:rustedchainmail_chestplate","magistuarmory:rustedchainmail_leggings",
            "magistuarmory:rustedkettlehat","magistuarmory:bascinet","magistuarmory:xivcenturyknight_chestplate",
            "magistuarmory:xivcenturyknight_leggings","magistuarmory:wingedhussar_chestplate","magistuarmory:cuirassier_helmet",
            "magistuarmory:cuirassier_chestplate","magistuarmory:cuirassier_leggings","magistuarmory:grand_bascinet",
            "magistuarmory:kastenbrust_chestplate","magistuarmory:kastenbrust_leggings","magistuarmory:kastenbrust_boots",
            "magistuarmory:face_helmet","magistuarmory:lamellar_chestplate"
    };

    private void generateForgedSteelTag(CachedOutput cache, List<CompletableFuture<?>> futures) {
        Set<String> items = new LinkedHashSet<>();
        for (BladeType type : BladeType.of(addon)) {
            if (type.getMaterials().contains(BladeMaterial.STEEL)) items.add(type.resultId(BladeMaterial.STEEL));
        }
        for (ForgingTable.Entry entry : ForgingTable.of(addon)) {
            if (entry.blueprint() != null) items.add(entry.result());
        }
        for (ForgingTable.AssemblyOnly a : ForgingTable.assemblyOnly(addon)) {
            items.add(a.result().replace("{mat}", "steel"));
        }
        if (!addon) items.addAll(List.of(BASE_FORGED_STEEL));

        JsonObject obj = new JsonObject();
        obj.addProperty("replace", false);
        JsonArray vals = new JsonArray();
        for (String item : items) vals.add(item);
        obj.add("values", vals);
        saveTo(cache, futures, Mappings.COMMON, Mappings.TAG_ITEM_DIR + "/forged/steel", obj);
    }

    // ── IO ────────────────────────────────────────────────────────────────────

    private void saveTo(CachedOutput cache, List<CompletableFuture<?>> futures,
                        String namespace, String path, JsonObject json) {
        // Build path manually: data/{namespace}/{path}.json
        Path filePath = packOutput.getOutputFolder(PackOutput.Target.DATA_PACK)
                .resolve(namespace).resolve(path + ".json");
        futures.add(DataProvider.saveStable(cache, json, filePath));
    }
}

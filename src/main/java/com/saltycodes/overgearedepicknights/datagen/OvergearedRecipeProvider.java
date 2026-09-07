package com.saltycodes.overgearedepicknights.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.saltycodes.overgearedepicknights.Mappings;
import com.saltycodes.overgearedepicknights.items.BladeMaterial;
import com.saltycodes.overgearedepicknights.items.BladeType;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Gson-based recipes: assembly, direct forging (armour and other blade-less items), knapping,
 * tool types, blueprints, smithing, blasting, shields and the odd special recipe.
 * Casting and blade forging are in {@link OvergearedBuilderRecipeProvider}.
 *
 * <p>With {@code addon = true} the provider writes only the Epic Knights: Addon share — the
 * per-{@link BladeType} recipes of the addon entries and the addon rows of {@link ForgingTable} —
 * under the {@code addon/} recipe path so nothing collides with the base pack.
 */
public class OvergearedRecipeProvider implements DataProvider {

    private final PackOutput.PathProvider recipePaths;
    private final String modId;
    private final boolean addon;
    private final List<BladeType> types;

    private static final BladeMaterial[] SHIELD_MATS = {
            BladeMaterial.BRONZE, BladeMaterial.COPPER, BladeMaterial.GOLD,
            BladeMaterial.IRON, BladeMaterial.SILVER, BladeMaterial.STEEL,
            BladeMaterial.TIN
    };

    public OvergearedRecipeProvider(PackOutput output, String modId, boolean addon) {
        this.recipePaths = output.createPathProvider(PackOutput.Target.DATA_PACK, Mappings.RECIPE_DIR);
        this.modId = modId;
        this.addon = addon;
        this.types = BladeType.of(addon);
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        List<CompletableFuture<?>> futures = new ArrayList<>();
        generateDirectForging(cache, futures);
        generateAssembly(cache, futures);
        generateAssemblyOnly(cache, futures);
        generateArmorAssembly(cache, futures);
        generateKnapping(cache, futures);
        generateTooltypes(cache, futures);
        generateBlueprintCrafting(cache, futures);
        generateToolCastPlaceholders(cache, futures);
        if (!addon) {
            generateChainmorgensternAssembly(cache, futures);
            generateSpecialCrafting(cache, futures);
            generateSmithing(cache, futures);
            generateBlasting(cache, futures);
            generateShieldForging(cache, futures);
            generateShieldCasting(cache, futures);
        }
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "Overgeared Recipe Provider" + (addon ? " (addon)" : "");
    }

    // ── Direct forging (ForgingTable: armour, plates, all-metal addon pieces) ──

    private void generateDirectForging(CachedOutput cache, List<CompletableFuture<?>> futures) {
        for (ForgingTable.Entry e : ForgingTable.of(addon)) {
            JsonObject obj = new JsonObject();
            obj.addProperty("type", "overgeared:forging");
            obj.addProperty("category", e.category());
            if (e.blueprint() != null) {
                obj.add("blueprint", strArray(new String[]{e.blueprint()}));
                obj.addProperty("requires_blueprint", false);
            }
            obj.addProperty("hammering", e.hammering());
            obj.addProperty("has_quality", e.quality());
            obj.addProperty("need_quenching", e.quench());
            obj.addProperty("needs_minigame", e.minigame());
            JsonObject key = new JsonObject();
            for (Map.Entry<Character, String> k : e.keys().entrySet()) {
                key.add(String.valueOf(k.getKey()), parseIngredient(k.getValue()));
            }
            obj.add("key", key);
            obj.add("pattern", strArray(e.pattern()));
            obj.add("result", resultRef(e.result()));
            obj.addProperty("show_notification", true);
            save(cache, futures, e.path(), obj);
        }
    }

    // ── Assembly (crafting_shapeless) ────────────────────────────────────────

    private void generateAssembly(CachedOutput cache, List<CompletableFuture<?>> futures) {
        for (BladeType type : types) {
            if (type.getAssemblyIngredients() == null) {
                if (type == BladeType.HEAVY_CROSSBOW) generateHeavyCrossbowAssembly(cache, futures);
                continue;
            }
            for (BladeMaterial mat : type.getMaterials()) {
                generateShapelessAssembly(cache, futures, type, mat);
                if (type.hasStoneBlade() && mat == BladeMaterial.STONE) {
                    generateStoneBladeCraft(cache, futures, type);
                }
            }
        }
    }

    private void generateShapelessAssembly(CachedOutput cache, List<CompletableFuture<?>> futures,
                                            BladeType type, BladeMaterial mat) {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", "overgeared:crafting_shapeless");
        obj.addProperty("category", "equipment");
        JsonArray ingredients = new JsonArray();
        ingredients.add(itemRef(type.itemId(mat)));
        for (String extra : type.getAssemblyIngredients()) {
            ingredients.add(parseIngredient(extra));
        }
        obj.add("ingredients", ingredients);
        obj.add("result", resultRef(type.resultId(mat)));

        save(cache, futures, "crafting/" + type.getName() + "/" + mat.getName() + "_" + type.getName(), obj);
    }

    private void generateStoneBladeCraft(CachedOutput cache, List<CompletableFuture<?>> futures,
                                          BladeType type) {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", "minecraft:crafting_shaped");
        obj.addProperty("category", "equipment");
        obj.add("pattern", strArray(type.getStoneBladePattern()));
        JsonObject key = new JsonObject();
        key.add("I", itemRef("minecraft:cobblestone"));
        key.add("#", itemRef(BladeType.SHORTSWORD.itemId(BladeMaterial.STONE)));
        obj.add("key", key);
        JsonObject result = new JsonObject();
        result.addProperty(Mappings.RESULT_KEY, type.itemId(BladeMaterial.STONE));
        obj.add("result", result);
        save(cache, futures, "crafting/" + type.getName() + "/" + type.itemPath(BladeMaterial.STONE), obj);
    }

    private void generateChainmorgensternAssembly(CachedOutput cache, List<CompletableFuture<?>> futures) {
        for (BladeMaterial mat : BladeType.MORGENSTERN.getMaterials()) {
            JsonObject obj = new JsonObject();
            obj.addProperty("type", "overgeared:crafting_shapeless");
            obj.addProperty("category", "equipment");
            JsonArray ingredients = new JsonArray();
            ingredients.add(itemRef(BladeType.MORGENSTERN.itemId(mat)));
            ingredients.add(tagRef("magistuarmory:chains/steel"));
            ingredients.add(itemRef("magistuarmory:hilt"));
            ingredients.add(tagRef(Mappings.COMMON + ":rods/wooden"));
            obj.add("ingredients", ingredients);
            obj.add("result", resultRef("magistuarmory:" + mat.getName() + "_chainmorgenstern"));
            save(cache, futures,
                    "crafting/chainmorgenstern/" + mat.getName() + "_chainmorgenstern", obj);
        }
    }

    /** Weapons without a blade item of their own: another blade plus handle parts (ranseur, glaive). */
    private void generateAssemblyOnly(CachedOutput cache, List<CompletableFuture<?>> futures) {
        for (ForgingTable.AssemblyOnly a : ForgingTable.assemblyOnly(addon)) {
            Iterable<BladeMaterial> materials = addon ? List.of(BladeMaterial.STEEL) : a.blade().getMaterials();
            for (BladeMaterial mat : materials) {
                JsonObject obj = new JsonObject();
                obj.addProperty("type", "overgeared:crafting_shapeless");
                obj.addProperty("category", "equipment");
                JsonArray ingredients = new JsonArray();
                ingredients.add(itemRef(a.blade().itemId(mat)));
                for (String extra : a.extra()) ingredients.add(parseIngredient(extra));
                obj.add("ingredients", ingredients);
                obj.add("result", resultRef(a.result().replace("{mat}", mat.getName())));
                save(cache, futures, "crafting/" + a.name() + "/" + mat.getName() + "_" + a.name(), obj);
            }
        }
    }

    /** Armour from a forged base plus loose parts (ForgingTable.assemblies): the base's quality carries over. */
    private void generateArmorAssembly(CachedOutput cache, List<CompletableFuture<?>> futures) {
        for (ForgingTable.Assembly a : ForgingTable.assemblies(addon)) {
            JsonObject obj = new JsonObject();
            obj.addProperty("type", "overgeared:crafting_shapeless");
            obj.addProperty("category", "equipment");
            JsonArray ingredients = new JsonArray();
            ingredients.add(parseIngredient(a.base()));
            for (String extra : a.extra()) ingredients.add(parseIngredient(extra));
            obj.add("ingredients", ingredients);
            obj.add("result", resultRef(a.result()));
            save(cache, futures, "crafting/" + a.name(), obj);
        }
    }

    private void generateHeavyCrossbowAssembly(CachedOutput cache, List<CompletableFuture<?>> futures) {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", "minecraft:crafting_shaped");
        obj.addProperty("category", "equipment");
        obj.add("pattern", strArray(new String[]{" B ", "shs", " p "}));
        JsonObject key = new JsonObject();
        key.add("B", itemRef(BladeType.HEAVY_CROSSBOW.itemId(BladeMaterial.STEEL)));
        key.add("s", itemRef("minecraft:string"));
        key.add("h", itemRef("minecraft:tripwire_hook"));
        key.add("p", itemRef("magistuarmory:pole"));
        obj.add("key", key);
        JsonObject result = new JsonObject();
        result.addProperty(Mappings.RESULT_KEY, BladeType.HEAVY_CROSSBOW.resultId(BladeMaterial.STEEL));
        //? if neoforge {
        /*result.addProperty("count", 1);
        *///?}
        obj.add("result", result);
        save(cache, futures, "crafting/heavy_crossbow/steel_heavy_crossbow", obj);
    }

    // ── Special crafting (vanilla shaped + wingedhussar assembly) ────────────

    private void generateSpecialCrafting(CachedOutput cache, List<CompletableFuture<?>> futures) {
        // crusader_surcoat — shaped from woolen_fabric
        {
            JsonObject obj = new JsonObject();
            obj.addProperty("type", "minecraft:crafting_shaped");
            obj.addProperty("category", "misc");
            obj.add("pattern", strArray(new String[]{"F F", "F F", "FFF"}));
            JsonObject key = new JsonObject();
            key.add("F", itemRef("magistuarmory:woolen_fabric"));
            obj.add("key", key);
            JsonObject result = new JsonObject();
            result.addProperty(Mappings.RESULT_KEY, modId + ":crusader_surcoat");
            //? if neoforge {
            /*result.addProperty("count", 1);
            *///?}
            obj.add("result", result);
            save(cache, futures, "crafting/crusader_surcoat", obj);
        }
        // hussar_wings — shaped from feather, rabbit_hide, stick
        {
            JsonObject obj = new JsonObject();
            obj.addProperty("type", "minecraft:crafting_shaped");
            obj.addProperty("category", "misc");
            obj.add("pattern", strArray(new String[]{"FRF", "F F", "FSF"}));
            JsonObject key = new JsonObject();
            key.add("F", itemRef("minecraft:feather"));
            key.add("R", itemRef("minecraft:rabbit_hide"));
            key.add("S", itemRef("minecraft:stick"));
            obj.add("key", key);
            JsonObject result = new JsonObject();
            result.addProperty(Mappings.RESULT_KEY, modId + ":hussar_wings");
            //? if neoforge {
            /*result.addProperty("count", 1);
            *///?}
            obj.add("result", result);
            save(cache, futures, "crafting/hussar_wings", obj);
        }
        // wingedhussar_chestplate — assembly: hussar_wings + halfarmor_chestplate
        {
            JsonObject obj = new JsonObject();
            obj.addProperty("type", "overgeared:crafting_shapeless");
            obj.addProperty("category", "equipment");
            JsonArray ingredients = new JsonArray();
            ingredients.add(itemRef(modId + ":hussar_wings"));
            ingredients.add(itemRef("magistuarmory:halfarmor_chestplate"));
            obj.add("ingredients", ingredients);
            obj.add("result", resultRef("magistuarmory:wingedhussar_chestplate"));
            save(cache, futures, "crafting/wingedhussar_chestplate", obj);
        }
        // crusader_chestplate — assembly: crusader_surcoat + platemail_chestplate
        {
            JsonObject obj = new JsonObject();
            obj.addProperty("type", "overgeared:crafting_shapeless");
            obj.addProperty("category", "equipment");
            JsonArray ingredients = new JsonArray();
            ingredients.add(itemRef(modId + ":crusader_surcoat"));
            ingredients.add(itemRef("magistuarmory:platemail_chestplate"));
            obj.add("ingredients", ingredients);
            obj.add("result", resultRef("magistuarmory:crusader_chestplate"));
            save(cache, futures, "crafting/crusader_chestplate", obj);
        }
        // steel_ring — shaped: 8x from steel nuggets
        {
            JsonObject obj = new JsonObject();
            obj.addProperty("type", "minecraft:crafting_shaped");
            obj.addProperty("category", "misc");
            obj.add("pattern", strArray(new String[]{" N ", "N N", " N "}));
            JsonObject key = new JsonObject();
            key.add("N", itemRef("overgeared:steel_nugget"));
            obj.add("key", key);
            JsonObject result = new JsonObject();
            result.addProperty(Mappings.RESULT_KEY, "magistuarmory:steel_ring");
            result.addProperty("count", 8);
            obj.add("result", result);
            save(cache, futures, "crafting/steel_ring", obj);
        }
    }

    // ── Knapping ─────────────────────────────────────────────────────────────

    private void generateKnapping(CachedOutput cache, List<CompletableFuture<?>> futures) {
        for (BladeType type : types) {
            if (!type.hasKnapping()) continue;
            JsonObject obj = new JsonObject();
            obj.addProperty("type", "overgeared:rock_knapping");
            obj.add("pattern", strArray(type.getKnapPattern()));
            obj.add("ingredient", itemRef("minecraft:cobblestone"));
            obj.add("result", resultRef(type.itemId(BladeMaterial.STONE)));
            obj.addProperty("show_notification", true);
            save(cache, futures, "knapping/" + type.itemPath(BladeMaterial.STONE), obj);
        }
    }

    // ── Item-to-tooltype ─────────────────────────────────────────────────────

    private void generateTooltypes(CachedOutput cache, List<CompletableFuture<?>> futures) {
        for (BladeType type : types) {
            JsonObject obj = new JsonObject();
            obj.addProperty("type", "overgeared:item_to_tooltype");
            JsonArray items = new JsonArray();
            for (BladeMaterial mat : type.getMaterials()) {
                if (mat == BladeMaterial.STONE) continue;
                items.add(itemRef(type.itemId(mat)));
            }
            if (items.isEmpty()) continue;
            obj.add("item", items);
            obj.addProperty("tooltype", type.getTooltype());
            save(cache, futures, "tooltypes/" + type.getName() + "_to_tooltype", obj);
        }
        if (addon) return;
        // chainmorgenstern uses morgenstern blades
        JsonObject chain = new JsonObject();
        chain.addProperty("type", "overgeared:item_to_tooltype");
        JsonArray chainItems = new JsonArray();
        for (BladeMaterial mat : BladeType.MORGENSTERN.getMaterials()) {
            if (mat == BladeMaterial.STONE) continue;
            chainItems.add(itemRef(BladeType.MORGENSTERN.itemId(mat)));
        }
        chain.add("item", chainItems);
        chain.addProperty("tooltype", "chainmorgenstern");
        save(cache, futures, "tooltypes/chainmorgenstern_to_tooltype", chain);
    }

    // ── Smithing (steel → diamond upgrades) ──────────────────────────────────

    private static final String[] SMITHING_WEAPON_TYPES = {
            "stylet", "shortsword", "katzbalger", "pike", "ranseur", "ahlspiess",
            "bastardsword", "estoc", "claymore", "zweihander", "lochaberaxe",
            "concavehalberd", "heavymace", "heavywarhammer", "lucernhammer",
            "morgenstern", "chainmorgenstern", "guisarme"
    };

    private void generateSmithing(CachedOutput cache, List<CompletableFuture<?>> futures) {
        String template = "overgeared:diamond_upgrade_smithing_template";
        for (String weapon : SMITHING_WEAPON_TYPES) {
            save(cache, futures,
                    "smithing/steel_" + weapon + "_to_diamond_" + weapon,
                    smithingTransform(template, "magistuarmory:steel_" + weapon, "magistuarmory:diamond_" + weapon));
        }
        for (String shield : ForgingTable.SHIELDS.keySet()) {
            save(cache, futures,
                    "smithing/steel_" + shield + "_to_diamond_" + shield,
                    smithingTransform(template, "magistuarmory:steel_" + shield, "magistuarmory:diamond_" + shield));
        }
    }

    private JsonObject smithingTransform(String template, String base, String result) {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", "minecraft:smithing_transform");
        obj.add("addition", itemRef("minecraft:diamond"));
        obj.add("base", itemRef(base));
        JsonObject res = new JsonObject();
        res.addProperty(Mappings.RESULT_KEY, result);
        obj.add("result", res);
        obj.add("template", itemRef(template));
        return obj;
    }

    // ── Blasting (steel nugget) ──────────────────────────────────────────────

    private void generateBlasting(CachedOutput cache, List<CompletableFuture<?>> futures) {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", "minecraft:blasting");
        obj.addProperty("category", "misc");
        obj.addProperty("cookingtime", 200);
        obj.addProperty("experience", 0.1f);
        obj.add("ingredient", tagRef(Mappings.COMMON + ":forged/steel"));
        obj.addProperty("result", "overgeared:steel_nugget");
        save(cache, futures, "blasting/overgeared_steel_nugget_from_blasting", obj);
    }

    // ── Shield forging ────────────────────────────────────────────────────────

    private void generateShieldForging(CachedOutput cache, List<CompletableFuture<?>> futures) {
        String[][] simple6 = {
                {"heatershield",     " p ", "psp", " p "},
                {"ellipticalshield", " P ", "PSP", " P "},
                {"kiteshield",       " P ", "PSP", " P "},
                {"pavese",           " P ", "PSP", " P "},
                {"roundshield",      " P ", "PSP", " P "},
                {"tartsche",         " P ", "PSP", " P "},
        };
        for (String[] row : simple6) {
            String shieldType = row[0];
            String[] pat = new String[]{row[1], row[2], row[3]};
            String plateKey = shieldType.equals("heatershield") ? "p" : "P";
            String woodKey  = shieldType.equals("heatershield") ? "s" : "S";
            String woodItem = "magistuarmory:wood_" + shieldType;
            for (BladeMaterial mat : SHIELD_MATS) {
                JsonObject obj = shieldForgingBase(shieldType, 6, false);
                JsonObject key = new JsonObject();
                key.add(plateKey, itemRef(plateItem(mat)));
                key.add(woodKey, itemRef(woodItem));
                obj.add("key", key);
                obj.add("pattern", strArray(pat));
                obj.add("result", resultRef("magistuarmory:" + mat.getName() + "_" + shieldType));
                obj.addProperty("show_notification", true);
                save(cache, futures,
                        "forging/" + shieldType + "/" + mat.getName() + "_" + shieldType, obj);
            }
        }
        // rondache
        for (BladeMaterial mat : SHIELD_MATS) {
            JsonObject obj = shieldForgingBase("rondache", 10, false);
            JsonObject key = new JsonObject();
            key.add("P", itemRef(plateItem(mat)));
            key.add("S", itemRef("magistuarmory:wood_rondache"));
            obj.add("key", key);
            obj.add("pattern", strArray(new String[]{"PPP", "PSP", "PPP"}));
            obj.add("result", resultRef("magistuarmory:" + mat.getName() + "_rondache"));
            obj.addProperty("show_notification", true);
            save(cache, futures, "forging/rondache/" + mat.getName() + "_rondache", obj);
        }
        // buckler
        for (BladeMaterial mat : SHIELD_MATS) {
            JsonObject obj = shieldForgingBase("buckler", 7, true);
            JsonObject key = new JsonObject();
            key.add("P", itemRef(plateItem(mat)));
            key.add("S", tagRef(Mappings.COMMON + ":ingots/steel"));
            obj.add("key", key);
            obj.add("pattern", strArray(new String[]{"   ", "PPS", "PP "}));
            obj.add("result", resultRef("magistuarmory:" + mat.getName() + "_buckler"));
            obj.addProperty("show_notification", true);
            save(cache, futures, "forging/buckler/" + mat.getName() + "_buckler", obj);
        }
        // target
        for (BladeMaterial mat : SHIELD_MATS) {
            JsonObject obj = shieldForgingBase("target", 7, true);
            JsonObject key = new JsonObject();
            key.add("P", itemRef(plateItem(mat)));
            key.add("S", tagRef(Mappings.COMMON + ":ingots/steel"));
            obj.add("key", key);
            obj.add("pattern", strArray(new String[]{"   ", "PP ", "PPS"}));
            obj.add("result", resultRef("magistuarmory:" + mat.getName() + "_target"));
            obj.addProperty("show_notification", true);
            save(cache, futures, "forging/target/" + mat.getName() + "_target", obj);
        }
    }

    private JsonObject shieldForgingBase(String blueprint, int hammering, boolean quench) {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", "overgeared:forging");
        obj.addProperty("category", "misc");
        JsonArray bp = new JsonArray();
        bp.add(blueprint);
        obj.add("blueprint", bp);
        obj.addProperty("requires_blueprint", false);
        obj.addProperty("hammering", hammering);
        obj.addProperty("has_quality", true);
        obj.addProperty("need_quenching", quench);
        obj.addProperty("needs_minigame", true);
        return obj;
    }

    private String plateItem(BladeMaterial mat) {
        return switch (mat) {
            case BRONZE, GOLD, SILVER, TIN -> modId + ":" + mat.getName() + "_plate";
            case COPPER, IRON -> "overgeared:" + mat.getName() + "_plate";
            case STEEL -> "overgeared:steel_plate";
            default -> throw new IllegalArgumentException("Stone has no plate");
        };
    }

    // ── Shield casting ────────────────────────────────────────────────────────

    private void generateShieldCasting(CachedOutput cache, List<CompletableFuture<?>> futures) {
        for (BladeMaterial mat : SHIELD_MATS) {
            for (String[] config : new String[][]{{"buckler", "36"}, {"target", "36"}}) {
                String shieldType = config[0];
                int    amount     = Integer.parseInt(config[1]);
                String resultId   = "magistuarmory:" + mat.getName() + "_" + shieldType;
                String folder     = "casting/" + shieldType;
                String prefix     = mat.getName() + "_" + shieldType;

                JsonObject base = new JsonObject();
                base.addProperty("cookingtime", 150);
                base.addProperty("experience", mat.getCastingXp());
                JsonObject input = new JsonObject();
                input.addProperty(mat.getName(), amount);
                base.add("input", input);
                base.addProperty("need_polishing", true);
                base.add("result", resultRef(resultId));
                base.addProperty("tool_type", shieldType);

                save(cache, futures, folder + "/" + prefix + "_from_cast_furnace",
                        withType(base, "overgeared:casting"));

                JsonObject baseG = base.deepCopy();
                baseG.addProperty("category", "misc");
                baseG.addProperty("group", "misc");
                save(cache, futures, folder + "/" + prefix + "_from_cast_blasting",
                        withType(baseG, "overgeared:cast_blasting"));
                save(cache, futures, folder + "/" + prefix + "_from_cast_smelting",
                        withType(baseG.deepCopy(), "overgeared:cast_smelting"));
            }
        }
    }

    // ── Craftable blueprints ─────────────────────────────────────────────────
    // Alternative to the drafting-table selector: craft a ready-made blueprint by
    // combining an empty blueprint with the item that blueprint forges.

    private void generateBlueprintCrafting(CachedOutput cache, List<CompletableFuture<?>> futures) {
        // Weapons: representative = the steel (or first non-stone) blade the blueprint forges.
        for (BladeType type : types) {
            saveBlueprintRecipe(cache, futures, type.getTooltype(), type.itemId(type.canonicalMaterial()));
        }
        if (!addon) {
            for (String t : ForgingTable.SHIELDS.keySet()) {
                saveBlueprintRecipe(cache, futures, t, "magistuarmory:steel_" + t);
            }
        }
        for (ForgingTable.Entry e : ForgingTable.of(addon)) {
            if (e.blueprint() != null) saveBlueprintRecipe(cache, futures, e.blueprint(), e.result());
        }
    }

    private void saveBlueprintRecipe(CachedOutput cache, List<CompletableFuture<?>> futures,
                                     String toolType, String representativeItem) {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", "overgeared:crafting_shapeless");
        obj.addProperty("category", "misc");
        JsonArray ingredients = new JsonArray();
        ingredients.add(itemRef("overgeared:empty_blueprint"));
        JsonObject template = itemRef(representativeItem);
        template.addProperty("remainder", true);
        ingredients.add(template);
        obj.add("ingredients", ingredients);

        JsonObject result = new JsonObject();
        result.addProperty(Mappings.RESULT_KEY, "overgeared:blueprint");
        result.addProperty("count", 1);
        //? if forge {
        // 1.20.1 reads "nbt" (SNBT string) on the result; Quality "well" matches the drafting table.
        result.addProperty("nbt", "{ToolType:\"" + toolType + "\",Quality:\"well\",Uses:0}");
        //?} else {
        /*// 1.21.1 carries the blueprint state as a data component instead of NBT.
        JsonObject components = new JsonObject();
        JsonObject data = new JsonObject();
        data.addProperty("tool_type", toolType);
        data.addProperty("quality", "well"); // matches the drafting table's default
        data.addProperty("uses", 0);
        components.add("overgeared:blueprint_data", data);
        result.add("components", components);
        *///?}
        obj.add("result", result);

        save(cache, futures, "blueprint/" + toolType, obj);
    }

    // ── Tool cast EMI placeholders ─────────────────────────────────────────────

    // Overgeared's ToolCastEmiRecipe builds IDs like "overgeared:clay_tool_cast/{type}"
    // without the synthetic "/" prefix, so EMI validates them against the recipe manager.
    // Adding a placeholder crafting_cast recipe at each expected path silences the warnings.
    private void generateToolCastPlaceholders(CachedOutput cache, List<CompletableFuture<?>> futures) {
        JsonObject placeholder = new JsonObject();
        placeholder.addProperty("type", "overgeared:crafting_cast");
        placeholder.addProperty("category", "misc");

        List<String> tooltypes = new ArrayList<>();
        for (BladeType type : types) {
            if (!tooltypes.contains(type.getTooltype())) tooltypes.add(type.getTooltype());
        }
        if (!addon) tooltypes.add("chainmorgenstern");

        for (String type : tooltypes) {
            saveAs(cache, futures, "overgeared", "clay_tool_cast/" + type, placeholder);
            saveAs(cache, futures, "overgeared", "nether_tool_cast/" + type, placeholder);
        }
    }

    // ── JSON helpers ──────────────────────────────────────────────────────────

    private JsonObject itemRef(String id) {
        JsonObject o = new JsonObject();
        o.addProperty("item", id);
        return o;
    }

    private JsonObject resultRef(String id) {
        JsonObject o = new JsonObject();
        o.addProperty(Mappings.RESULT_KEY, id);
        //? if neoforge {
        /*o.addProperty("count", 1);
        *///?}
        return o;
    }

    private JsonObject tagRef(String tag) {
        JsonObject o = new JsonObject();
        o.addProperty("tag", tag);
        return o;
    }

    private JsonObject parseIngredient(String spec) {
        int colon = spec.indexOf(':');
        String type = spec.substring(0, colon);
        String id   = spec.substring(colon + 1);
        return "tag".equals(type) ? tagRef(id) : itemRef(id);
    }

    private JsonArray strArray(String[] arr) {
        JsonArray a = new JsonArray();
        for (String s : arr) a.add(s);
        return a;
    }

    private JsonObject withType(JsonObject obj, String type) {
        JsonObject copy = obj.deepCopy();
        JsonObject result = new JsonObject();
        result.addProperty("type", type);
        copy.entrySet().forEach(e -> result.add(e.getKey(), e.getValue()));
        return result;
    }

    // ── IO ────────────────────────────────────────────────────────────────────

    /** Saves under this mod's namespace; addon recipes get the {@code addon/} path prefix. */
    private void save(CachedOutput cache, List<CompletableFuture<?>> futures,
                      String recipePath, JsonObject json) {
        String path = (addon ? "addon/" : "") + recipePath;
        futures.add(DataProvider.saveStable(cache, json,
                recipePaths.json(ResourceLocation.fromNamespaceAndPath(modId, path))));
    }

    /** Saves under a foreign namespace at the exact path (no prefix). */
    private void saveAs(CachedOutput cache, List<CompletableFuture<?>> futures,
                        String namespace, String recipePath, JsonObject json) {
        Path path = recipePaths.json(ResourceLocation.fromNamespaceAndPath(namespace, recipePath));
        futures.add(DataProvider.saveStable(cache, json, path));
    }
}

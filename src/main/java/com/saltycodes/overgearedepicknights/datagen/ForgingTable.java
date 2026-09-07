package com.saltycodes.overgearedepicknights.datagen;

import com.saltycodes.overgearedepicknights.Mappings;
import com.saltycodes.overgearedepicknights.items.BladeType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Items forged directly on the anvil without an intermediate blade: armour, plates and the
 * all-metal addon pieces. One row per recipe; ingredient specs are {@code item:<id>} / {@code tag:<id>}.
 */
public final class ForgingTable {
    private ForgingTable() {}

    public record Entry(String path, String displayName, String blueprint, String category, int hammering,
                        String[] pattern, Map<Character, String> keys, String result,
                        boolean quality, boolean quench, boolean minigame, boolean addon) {
        /** Blueprint tool type of this recipe, or {@code null} for plain parts like small plates. */
        public String tooltype() { return blueprint; }

        /** True when steel goes into the piece — gold jewellery must not blast into steel nuggets. */
        public boolean usesSteel() {
            return keys.values().stream().anyMatch(spec -> spec.contains("steel") || spec.contains("chainmail"));
        }
    }

    private static final String HEATED_STEEL = "item:overgeared:heated_steel_ingot";
    private static final String STEEL_PLATE  = "item:overgeared:steel_plate";
    private static final String STEEL_NUGGET = "item:overgeared:steel_nugget";
    private static final String SMALL_PLATE  = "item:magistuarmory:small_steel_plate";
    private static final String CHAINMAIL    = "item:magistuarmory:steel_chainmail";
    private static final String STEEL_RING   = "item:magistuarmory:steel_ring";
    private static final String LAMELLAR_ROW = "tag:magistuarmory:lamellar_rows/steel";
    private static final String GOLD_NUGGET  = "tag:" + Mappings.COMMON + ":nuggets/gold";
    private static final String GOLD_INGOT   = "tag:" + Mappings.COMMON + ":ingots/gold";
    // loose parts riveted on while hammering, or added at assembly
    private static final String LEATHER_STRIP = "tag:magistuarmory:leather_strips";
    private static final String LEATHER       = "item:minecraft:leather";
    private static final String WOOL_FABRIC   = "tag:magistuarmory:woolen_fabrics";
    private static final String BLAZE_POWDER  = "item:minecraft:blaze_powder";
    private static final String FEATHER       = "item:minecraft:feather";
    private static final String LEATHER_BOOTS = "item:minecraft:leather_boots";

    /** Epic Knights shield types (forged from plates around the wooden shield) and their names. */
    public static final Map<String, String> SHIELDS = new LinkedHashMap<>();
    static {
        SHIELDS.put("buckler", "Buckler");
        SHIELDS.put("heatershield", "Heater Shield");
        SHIELDS.put("ellipticalshield", "Elliptical Shield");
        SHIELDS.put("kiteshield", "Kite Shield");
        SHIELDS.put("pavese", "Pavese");
        SHIELDS.put("rondache", "Rondache");
        SHIELDS.put("roundshield", "Round Shield");
        SHIELDS.put("tartsche", "Tartsche");
        SHIELDS.put("target", "Target Shield");
    }

    /**
     * Weapons assembled straight from another blade, without a blade item of their own —
     * Epic Knights' ranseur is a shortsword blade on a pole, the addon's glaive likewise.
     * {@code result} uses {@code {mat}} for the material; addon rows are steel only.
     */
    public record AssemblyOnly(String name, BladeType blade, String[] extra, String result, boolean addon) {}

    public static final List<AssemblyOnly> ASSEMBLY_ONLY = List.of(
            new AssemblyOnly("ranseur", BladeType.SHORTSWORD,
                    new String[]{"item:magistuarmory:pole"}, "magistuarmory:{mat}_ranseur", false),
            new AssemblyOnly("glaive", BladeType.SHORTSWORD,
                    new String[]{"tag:magistuarmory:poles", "tag:" + Mappings.COMMON + ":rods/wooden"},
                    "magistuarmoryaddon:{mat}_glaive", true)
    );

    /**
     * Armour assembled from a forged base plus loose parts, no metal added — the base's forging
     * quality carries over through Overgeared's shapeless crafting, like the hussar wings on the
     * half armour. {@code extra} lists one ingredient spec per part.
     */
    public record Assembly(String name, String base, String[] extra, String result, boolean addon) {}

    private static final List<Assembly> ASSEMBLIES = new ArrayList<>();

    public static List<Assembly> assemblies(boolean addon) {
        List<Assembly> out = new ArrayList<>();
        for (Assembly a : ASSEMBLIES) if (a.addon() == addon) out.add(a);
        return out;
    }

    public static List<AssemblyOnly> assemblyOnly(boolean addon) {
        List<AssemblyOnly> out = new ArrayList<>();
        for (AssemblyOnly a : ASSEMBLY_ONLY) if (a.addon() == addon) out.add(a);
        return out;
    }

    private static final List<Entry> ENTRIES = new ArrayList<>();

    static {
        // ── Base: Epic Knights armour (blueprint "armor" category, full minigame) ──
        armor("norman_helmet", "Norman Helmet", 7, p("III", "I I", "CCC"), k('I', HEATED_STEEL), k('C', CHAINMAIL));
        armor("barbute", "Barbute", 7, p("   ", "III", "P P"), k('I', HEATED_STEEL), k('P', STEEL_PLATE));
        armor("bascinet", "Bascinet", 4, p("   ", "PNP", "   "), k('P', STEEL_PLATE), k('N', "item:magistuarmory:norman_helmet"));
        armor("grand_bascinet", "Grand Bascinet", 7, p("   ", "PHP", "PPP"), k('P', STEEL_PLATE), k('H', "item:magistuarmory:norman_helmet"));
        armor("kettlehat", "Kettle Hat", 7, p("III", "P P", "CCC"), k('I', HEATED_STEEL), k('P', STEEL_PLATE), k('C', CHAINMAIL));
        armor("shishak", "Shishak", 8, p("III", "INI", "CCC"), k('I', HEATED_STEEL), k('C', CHAINMAIL), k('N', STEEL_NUGGET));
        armor("face_helmet", "Face Helmet", 7, p("   ", "SHS", "SSS"), k('H', "item:magistuarmory:shishak"), k('S', SMALL_PLATE));
        armor("greathelm", "Great Helm", 9, p("III", "I I", "ICI"), k('I', HEATED_STEEL), k('C', CHAINMAIL));
        armor("stechhelm", "Stechhelm", 5, p("ppp", "pbp", "ppp"), k('p', STEEL_PLATE), k('b', "item:magistuarmory:barbute"));
        armor("armet", "Armet", 5, p("   ", "PBP", " P "), k('P', STEEL_PLATE), k('B', "item:magistuarmory:barbute"));
        armor("sallet", "Sallet", 5, p("   ", " B ", "PPP"), k('B', "item:magistuarmory:barbute"), k('P', STEEL_PLATE));
        armor("halfarmor_chestplate", "Half Armor Chestplate", 10, p("I I", "III", "PIP"), k('I', HEATED_STEEL), k('P', STEEL_PLATE));
        armor("platemail_chestplate", "Platemail Chestplate", 4, p("I I", "CCC", "CCC"), k('I', HEATED_STEEL), k('C', CHAINMAIL));
        armor("platemail_leggings", "Platemail Leggings", 4, p("CCC", "I I", "C C"), k('I', HEATED_STEEL), k('C', CHAINMAIL));
        armor("knight_chestplate", "Knight Chestplate", 6, p("P P", "PHP", " P "), k('P', STEEL_PLATE), k('H', "item:magistuarmory:halfarmor_chestplate"));
        armor("knight_leggings", "Knight Leggings", 9, p("PPP", "I I", "P P"), k('P', STEEL_PLATE), k('I', HEATED_STEEL));
        armor("knight_boots", "Knight Boots", 6, p("   ", "I I", "P P"), k('P', STEEL_PLATE), k('I', HEATED_STEEL));
        armor("gothic_chestplate", "Gothic Chestplate", 7, p("P P", "PHP", "NPN"), k('P', STEEL_PLATE), k('H', "item:magistuarmory:halfarmor_chestplate"), k('N', STEEL_NUGGET));
        armor("gothic_leggings", "Gothic Leggings", 9, p("PPP", "PNP", "PNP"), k('P', STEEL_PLATE), k('N', STEEL_NUGGET));
        armor("gothic_boots", "Gothic Boots", 6, p("P P", "P P", "N N"), k('P', STEEL_PLATE), k('N', STEEL_NUGGET));
        armor("jousting_chestplate", "Jousting Chestplate", 5, p("pkp", "ppp", "ppp"), k('p', STEEL_PLATE), k('k', "item:magistuarmory:knight_chestplate"));
        armor("jousting_leggings", "Jousting Leggings", 5, p("ppp", "pkp", "p p"), k('p', STEEL_PLATE), k('k', "item:magistuarmory:knight_leggings"));
        armor("jousting_boots", "Jousting Boots", 5, p("   ", "pkp", "p p"), k('p', STEEL_PLATE), k('k', "item:magistuarmory:knight_boots"));
        armor("kastenbrust_chestplate", "Kastenbrust Chestplate", 9, p("P P", "PHP", "SPS"), k('P', STEEL_PLATE), k('H', "item:magistuarmory:halfarmor_chestplate"), k('S', SMALL_PLATE));
        armor("kastenbrust_leggings", "Kastenbrust Leggings", 11, p("PPP", "PSP", "PSP"), k('P', STEEL_PLATE), k('S', SMALL_PLATE));
        armor("kastenbrust_boots", "Kastenbrust Boots", 8, p("P P", "P P", "S S"), k('P', STEEL_PLATE), k('S', SMALL_PLATE));
        armor("cuirassier_chestplate", "Cuirassier Chestplate", 9, p(" I ", "III", "PIP"), k('I', HEATED_STEEL), k('P', STEEL_PLATE));
        armor("cuirassier_helmet", "Cuirassier Helmet", 7, p("   ", "PIP", "P P"), k('I', HEATED_STEEL), k('P', STEEL_PLATE));
        armor("xivcenturyknight_chestplate", "XIV Century Knight Chestplate", 7, p("P P", "PPP", "CCC"), k('P', STEEL_PLATE), k('C', CHAINMAIL));
        armor("xivcenturyknight_leggings", "XIV Century Knight Leggings", 6, p("CCC", "P P", "P P"), k('P', STEEL_PLATE), k('C', CHAINMAIL));
        armor("crusader_leggings", "Crusader Leggings", 6, p("CCC", "I I", "I I"), k('C', CHAINMAIL), k('I', HEATED_STEEL));

        // small_steel_plate — plain part: no blueprint, no quality, no minigame
        ENTRIES.add(new Entry("forging/small_steel_plate", "Small Steel Plate", null, "misc", 3,
                p("##", "##", "##"), keys(k('#', STEEL_NUGGET)), "magistuarmory:small_steel_plate",
                false, true, false, false));

        // ── Epic Knights: Addon — armour and decorations, generated by tools/derive_addon.py --table:
        //    every metal piece (straps, leather or cloth riveted on) forged 1:1 in the addon's grid ──
        addon("mustache_decoration", "Mustache Decoration", "misc", 5, "magistuarmoryaddon:steel_mustache_decoration",
                p("N N", " N ", "   "), k('N', STEEL_NUGGET));
        addon("puff_and_slash_chestplate", "Puff And Slash Chestplate", "armor", 11, "magistuarmoryaddon:steel_puff_and_slash_chestplate",
                p("NNN", "IBI", "INI"), k('N', STEEL_NUGGET), k('I', HEATED_STEEL), k('B', "item:magistuarmory:halfarmor_chestplate"));
        addon("skirt_decoration", "Skirt Decoration", "misc", 9, "magistuarmoryaddon:steel_skirt_decoration",
                p(" P ", "IPI", "IPI"), k('P', STEEL_PLATE), k('I', HEATED_STEEL));
        addon("articulated_chestplate", "Articulated Chestplate", "armor", 10, "magistuarmoryaddon:articulated_chestplate",
                p("I I", "ZBZ", "SSS"), k('I', HEATED_STEEL), k('Z', BLAZE_POWDER), k('B', "item:magistuarmory:halfarmor_chestplate"), k('S', SMALL_PLATE));
        addon("articulated_pauldrons_decoration", "Articulated Pauldrons Decoration", "misc", 7, "magistuarmoryaddon:articulated_pauldrons_decoration",
                p("SLS", "I I", "   "), k('S', SMALL_PLATE), k('L', LEATHER_STRIP), k('I', HEATED_STEEL));
        addon("articulated_shoulder_defenses_decoration", "Articulated Shoulder Defenses Decoration", "misc", 7, "magistuarmoryaddon:articulated_shoulder_defenses_decoration",
                p("   ", "PLP", "P P"), k('P', STEEL_PLATE), k('L', LEATHER_STRIP));
        addon("avant_boots", "Avant Boots", "armor", 8, "magistuarmoryaddon:avant_boots",
                p("I I", "I I", "S S"), k('I', HEATED_STEEL), k('S', SMALL_PLATE));
        addon("avant_chestplate", "Avant Chestplate", "armor", 10, "magistuarmoryaddon:avant_chestplate",
                p("I I", "IBI", "SIS"), k('I', HEATED_STEEL), k('B', "item:magistuarmory:halfarmor_chestplate"), k('S', SMALL_PLATE));
        addon("avant_leggings", "Avant Leggings", "armor", 11, "magistuarmoryaddon:avant_leggings",
                p("III", "ISI", "ISI"), k('I', HEATED_STEEL), k('S', SMALL_PLATE));
        addon("bicoque", "Bicoque", "armor", 6, "magistuarmoryaddon:bicoque",
                p("   ", "PBP", " I "), k('P', STEEL_PLATE), k('B', "item:magistuarmory:norman_helmet"), k('I', HEATED_STEEL));
        addon("british_armet", "British Armet", "armor", 7, "magistuarmoryaddon:british_armet",
                p(" I ", "IBI", " P "), k('I', HEATED_STEEL), k('B', "item:magistuarmory:barbute"), k('P', STEEL_PLATE));
        addon("burgundian_kettlehat", "Burgundian Kettlehat", "armor", 10, "magistuarmoryaddon:burgundian_kettlehat",
                p("NIN", "INI", "P P"), k('N', STEEL_NUGGET), k('I', HEATED_STEEL), k('P', STEEL_PLATE));
        addon("cabasset", "Cabasset", "armor", 8, "magistuarmoryaddon:cabasset",
                p(" I ", "IPI", "I I"), k('I', HEATED_STEEL), k('P', STEEL_PLATE));
        addon("cervelliere", "Cervelliere", "armor", 8, "magistuarmoryaddon:cervelliere",
                p("PPP", "L L", " N "), k('P', STEEL_PLATE), k('L', LEATHER_STRIP), k('N', STEEL_NUGGET));
        addon("chainmail_gloves_decoration", "Chainmail Gloves Decoration", "misc", 5, "magistuarmoryaddon:chainmail_gloves_decoration",
                p(" L ", " C ", " C "), k('L', LEATHER_STRIP), k('C', CHAINMAIL));
        addon("chapel", "Chapel", "armor", 10, "magistuarmoryaddon:chapel",
                p("III", "P P", "WWW"), k('I', HEATED_STEEL), k('P', STEEL_PLATE), k('W', WOOL_FABRIC));
        addon("close_helmet", "Close Helmet", "armor", 6, "magistuarmoryaddon:close_helmet",
                p("   ", "PBP", " I "), k('P', STEEL_PLATE), k('B', "item:magistuarmory:barbute"), k('I', HEATED_STEEL));
        addon("closed_barbute", "Closed Barbute", "armor", 9, "magistuarmoryaddon:closed_barbute",
                p("III", "PNP", " P "), k('I', HEATED_STEEL), k('P', STEEL_PLATE), k('N', STEEL_NUGGET));
        addon("codpiece_decoration", "Codpiece Decoration", "misc", 4, "magistuarmoryaddon:codpiece_decoration",
                p(" S ", " P ", "   "), k('S', SMALL_PLATE), k('P', STEEL_PLATE));
        addon("composite_gloves_decoration", "Composite Gloves Decoration", "misc", 5, "magistuarmoryaddon:composite_gloves_decoration",
                p(" L ", " S ", " C "), k('L', LEATHER_STRIP), k('S', SMALL_PLATE), k('C', CHAINMAIL));
        addon("cuman_captain_helmet", "Cuman Captain Helmet", "armor", 7, "magistuarmoryaddon:cuman_captain_helmet",
                p(" I ", "CBC", " C "), k('I', HEATED_STEEL), k('C', CHAINMAIL), k('B', "item:magistuarmory:face_helmet"));
        addon("cuman_helmet", "Cuman Helmet", "armor", 5, "magistuarmoryaddon:cuman_helmet",
                p(" N ", " B ", " C "), k('N', STEEL_NUGGET), k('B', "item:magistuarmory:face_helmet"), k('C', CHAINMAIL));
        addon("dragon_shoulder_pads_decoration", "Dragon Shoulder Pads Decoration", "misc", 7, "magistuarmoryaddon:dragon_shoulder_pads_decoration",
                p("Z Z", "ILI", "   "), k('Z', BLAZE_POWDER), k('I', HEATED_STEEL), k('L', LEATHER_STRIP));
        addon("early_cabasset", "Early Cabasset", "armor", 9, "magistuarmoryaddon:early_cabasset",
                p(" P ", "PIP", "PLP"), k('P', STEEL_PLATE), k('I', HEATED_STEEL), k('L', LEATHER_STRIP));
        addon("embosed_parade_boots", "Embosed Parade Boots", "armor", 8, "magistuarmoryaddon:embosed_parade_boots",
                p("N N", "P P", "I I"), k('N', STEEL_NUGGET), k('P', STEEL_PLATE), k('I', HEATED_STEEL));
        addon("embosed_parade_burgonet", "Embosed Parade Burgonet", "armor", 8, "magistuarmoryaddon:embosed_parade_burgonet",
                p("NNN", "NBN", "   "), k('N', STEEL_NUGGET), k('B', "item:magistuarmory:cuirassier_helmet"));
        addon("embosed_parade_chestplate", "Embosed Parade Chestplate", "armor", 11, "magistuarmoryaddon:embosed_parade_chestplate",
                p("NPN", "PBP", "PPP"), k('N', STEEL_NUGGET), k('P', STEEL_PLATE), k('B', "item:magistuarmory:halfarmor_chestplate"));
        addon("english_knight_boots", "English Knight Boots", "armor", 9, "magistuarmoryaddon:english_knight_boots",
                p("SLS", "P P", "P P"), k('S', SMALL_PLATE), k('L', LEATHER_STRIP), k('P', STEEL_PLATE));
        addon("english_knight_chestplate", "English Knight Chestplate", "armor", 10, "magistuarmoryaddon:english_knight_chestplate",
                p("P P", "PBP", "III"), k('P', STEEL_PLATE), k('B', "item:magistuarmory:halfarmor_chestplate"), k('I', HEATED_STEEL));
        addon("engraved_chestplate", "Engraved Chestplate", "armor", 10, "magistuarmoryaddon:engraved_chestplate",
                p("I I", "ISI", "SSS"), k('I', HEATED_STEEL), k('S', SMALL_PLATE));
        addon("engraved_close_helmet", "Engraved Close Helmet", "armor", 10, "magistuarmoryaddon:engraved_close_helmet",
                p("III", "SIS", "S S"), k('I', HEATED_STEEL), k('S', SMALL_PLATE));
        addon("frontal_feather_decoration", "Frontal Feather Decoration", "misc", 5, "magistuarmoryaddon:frontal_feather_decoration",
                p(" F ", " F ", " G "), k('F', FEATHER), k('G', GOLD_NUGGET));
        addon("gallowglass_boots", "Gallowglass Boots", "armor", 8, "magistuarmoryaddon:gallowglass_boots",
                p("C C", "W W", "E E"), k('C', CHAINMAIL), k('W', WOOL_FABRIC), k('E', LEATHER));
        addon("gallowglass_chestplate", "Gallowglass Chestplate", "armor", 10, "magistuarmoryaddon:gallowglass_chestplate",
                p("C C", "WCW", "WCW"), k('C', CHAINMAIL), k('W', WOOL_FABRIC));
        addon("gallowglass_leggings", "Gallowglass Leggings", "armor", 9, "magistuarmoryaddon:gallowglass_leggings",
                p("CCC", "W W", "W W"), k('C', CHAINMAIL), k('W', WOOL_FABRIC));
        addon("german_bascinet", "German Bascinet", "armor", 5, "magistuarmoryaddon:german_bascinet",
                p(" S ", " B ", " S "), k('S', SMALL_PLATE), k('B', "item:magistuarmory:norman_helmet"));
        addon("golden_ball_decoration", "Golden Ball Decoration", "misc", 4, "magistuarmoryaddon:golden_ball_decoration",
                p("   ", " A ", " G "), k('A', GOLD_INGOT), k('G', GOLD_NUGGET));
        addon("golden_necklace_decoration", "Golden Necklace Decoration", "misc", 10, "magistuarmoryaddon:golden_necklace_decoration",
                p("GGG", "G G", "GGG"), k('G', GOLD_NUGGET));
        addon("gorget_decoration", "Gorget Decoration", "misc", 3, "magistuarmoryaddon:gorget_decoration",
                p(" P ", "   ", "   "), k('P', STEEL_PLATE));
        addon("greenwich_armet", "Greenwich Armet", "armor", 6, "magistuarmoryaddon:greenwich_armet",
                p("   ", "NBN", " I "), k('N', STEEL_NUGGET), k('B', "item:magistuarmory:armet"), k('I', HEATED_STEEL));
        addon("greenwich_chestplate", "Greenwich Chestplate", "armor", 7, "magistuarmoryaddon:greenwich_chestplate",
                p("   ", "IBI", "N N"), k('I', HEATED_STEEL), k('B', "item:magistuarmory:knight_chestplate"), k('N', STEEL_NUGGET));
        addon("grilled_helmet", "Grilled Helmet", "armor", 10, "magistuarmoryaddon:grilled_helmet",
                p("III", "NNN", "S S"), k('I', HEATED_STEEL), k('N', STEEL_NUGGET), k('S', SMALL_PLATE));
        addon("heavy_brigandine_boots", "Heavy Brigandine Boots", "armor", 7, "magistuarmoryaddon:heavy_brigandine_boots",
                p(" L ", "P P", "P P"), k('L', LEATHER_STRIP), k('P', STEEL_PLATE));
        addon("heavy_brigandine_leggings", "Heavy Brigandine Leggings", "armor", 10, "magistuarmoryaddon:heavy_brigandine_leggings",
                p("EPE", "PEP", "P P"), k('E', LEATHER), k('P', STEEL_PLATE));
        addon("heavy_cuirassier_boots", "Heavy Cuirassier Boots", "armor", 8, "magistuarmoryaddon:heavy_cuirassier_boots",
                p("I I", "I I", "E E"), k('I', HEATED_STEEL), k('E', LEATHER));
        addon("heavy_cuirassier_chestplate", "Heavy Cuirassier Chestplate", "armor", 9, "magistuarmoryaddon:heavy_cuirassier_chestplate",
                p("P P", "PBP", "I I"), k('P', STEEL_PLATE), k('B', "item:magistuarmory:cuirassier_chestplate"), k('I', HEATED_STEEL));
        addon("heavy_shoulder_pad_decoration", "Heavy Shoulder Pad Decoration", "misc", 5, "magistuarmoryaddon:heavy_shoulder_pad_decoration",
                p("   ", "PLP", "   "), k('P', STEEL_PLATE), k('L', LEATHER_STRIP));
        addon("helmet_rondel_decoration", "Helmet Rondel Decoration", "misc", 4, "magistuarmoryaddon:helmet_rondel_decoration",
                p("   ", "N N", "   "), k('N', STEEL_NUGGET));
        addon("klappvisor_bascinet", "Klappvisor Bascinet", "armor", 5, "magistuarmoryaddon:klappvisor_bascinet",
                p("   ", "IBI", "   "), k('I', HEATED_STEEL), k('B', "item:magistuarmory:norman_helmet"));
        addon("late_bascinet", "Late Bascinet", "armor", 7, "magistuarmoryaddon:late_bascinet",
                p("   ", "IBI", "R R"), k('I', HEATED_STEEL), k('B', "item:magistuarmory:norman_helmet"), k('R', STEEL_RING));
        addon("late_burgonet", "Late Burgonet", "armor", 9, "magistuarmoryaddon:late_burgonet",
                p(" I ", "PIP", "PRP"), k('I', HEATED_STEEL), k('P', STEEL_PLATE), k('R', STEEL_RING));
        addon("late_kettlehat", "Late Kettlehat", "armor", 10, "magistuarmoryaddon:late_kettlehat",
                p("PIP", "P P", "CCC"), k('P', STEEL_PLATE), k('I', HEATED_STEEL), k('C', CHAINMAIL));
        addon("late_sallet", "Late Sallet", "armor", 9, "magistuarmoryaddon:late_sallet",
                p(" N ", "NBN", "PPP"), k('N', STEEL_NUGGET), k('B', "item:magistuarmory:barbute"), k('P', STEEL_PLATE));
        addon("light_burgonet", "Light Burgonet", "armor", 8, "magistuarmoryaddon:light_burgonet",
                p(" I ", "PIP", "I I"), k('I', HEATED_STEEL), k('P', STEEL_PLATE));
        addon("light_cuman_helmet", "Light Cuman Helmet", "armor", 6, "magistuarmoryaddon:light_cuman_helmet",
                p(" N ", " S ", "S S"), k('N', STEEL_NUGGET), k('S', SMALL_PLATE));
        addon("lion_helmet", "Lion Helmet", "armor", 8, "magistuarmoryaddon:lion_helmet",
                p("ABA", "PPP", "   "), k('A', GOLD_INGOT), k('B', "item:magistuarmory:barbute"), k('P', STEEL_PLATE));
        addon("lobster_tailed_helmet", "Lobster Tailed Helmet", "armor", 7, "magistuarmoryaddon:lobster_tailed_helmet",
                p(" P ", "NBN", " N "), k('P', STEEL_PLATE), k('N', STEEL_NUGGET), k('B', "item:magistuarmoryaddon:cervelliere"));
        addon("mamluk_helmet", "Mamluk Helmet", "armor", 11, "magistuarmoryaddon:mamluk_helmet",
                p("III", "SNS", "CCC"), k('I', HEATED_STEEL), k('S', SMALL_PLATE), k('N', STEEL_NUGGET), k('C', CHAINMAIL));
        addon("milanese_armet", "Milanese Armet", "armor", 6, "magistuarmoryaddon:milanese_armet",
                p("   ", "IBI", " P "), k('I', HEATED_STEEL), k('B', "item:magistuarmory:barbute"), k('P', STEEL_PLATE));
        addon("mirror_boots", "Mirror Boots", "armor", 7, "magistuarmoryaddon:mirror_boots",
                p("P P", "MOM", "   "), k('P', STEEL_PLATE), k('M', LAMELLAR_ROW), k('O', LEATHER_BOOTS));
        addon("morion", "Morion", "armor", 8, "magistuarmoryaddon:morion",
                p(" P ", "III", "P P"), k('P', STEEL_PLATE), k('I', HEATED_STEEL));
        addon("patrician_tuher_helmet", "Patrician Tuher Helmet", "armor", 9, "magistuarmoryaddon:patrician_tuher_helmet",
                p(" P ", "ZBZ", "PPP"), k('P', STEEL_PLATE), k('Z', BLAZE_POWDER), k('B', "item:magistuarmory:barbute"));
        addon("peascod_chestplate", "Peascod Chestplate", "armor", 10, "magistuarmoryaddon:peascod_chestplate",
                p("S S", "NBN", "SNS"), k('S', SMALL_PLATE), k('N', STEEL_NUGGET), k('B', "item:magistuarmory:halfarmor_chestplate"));
        addon("pikeman_chestplate", "Pikeman Chestplate", "armor", 7, "magistuarmoryaddon:pikeman_chestplate",
                p("   ", "IBI", "L L"), k('I', HEATED_STEEL), k('B', "item:magistuarmory:halfarmor_chestplate"), k('L', LEATHER_STRIP));
        addon("plackart_decoration", "Plackart Decoration", "misc", 8, "magistuarmoryaddon:plackart_decoration",
                p("   ", "LPL", "PPP"), k('L', LEATHER_STRIP), k('P', STEEL_PLATE));
        addon("plated_chainmail_gloves_decoration", "Plated Chainmail Gloves Decoration", "misc", 5, "magistuarmoryaddon:plated_chainmail_gloves_decoration",
                p(" L ", " C ", " S "), k('L', LEATHER_STRIP), k('C', CHAINMAIL), k('S', SMALL_PLATE));
        addon("rivited_gauntlets_decoration", "Rivited Gauntlets Decoration", "misc", 5, "magistuarmoryaddon:rivited_gauntlets_decoration",
                p(" L ", " S ", " S "), k('L', LEATHER_STRIP), k('S', SMALL_PLATE));
        addon("sallet_without_neck_protection", "Sallet Without Neck Protection", "armor", 5, "magistuarmoryaddon:sallet_without_neck_protection",
                p("   ", " B ", "P P"), k('B', "item:magistuarmory:barbute"), k('P', STEEL_PLATE));
        addon("sallet_without_visor", "Sallet Without Visor", "armor", 5, "magistuarmoryaddon:sallet_without_visor",
                p("   ", "SBS", "   "), k('S', SMALL_PLATE), k('B', "item:magistuarmory:barbute"));
        addon("savoyard_helmet", "Savoyard Helmet", "armor", 4, "magistuarmoryaddon:savoyard_helmet",
                p("   ", " BP", "   "), k('B', "item:magistuarmory:cuirassier_helmet"), k('P', STEEL_PLATE));
        addon("scale_helmet", "Scale Helmet", "armor", 8, "magistuarmoryaddon:scale_helmet",
                p("MMM", "L L", " N "), k('M', LAMELLAR_ROW), k('L', LEATHER_STRIP), k('N', STEEL_NUGGET));
        addon("shoulder_pads_decoration", "Shoulder Pads Decoration", "misc", 5, "magistuarmoryaddon:shoulder_pads_decoration",
                p("   ", "ILI", "   "), k('I', HEATED_STEEL), k('L', LEATHER_STRIP));
        addon("splint_boots", "Splint Boots", "armor", 8, "magistuarmoryaddon:splint_boots",
                p("S S", "L L", "S S"), k('S', SMALL_PLATE), k('L', LEATHER_STRIP));
        addon("splint_leggings", "Splint Leggings", "armor", 9, "magistuarmoryaddon:splint_leggings",
                p("WLW", "S S", "S S"), k('W', WOOL_FABRIC), k('L', LEATHER_STRIP), k('S', SMALL_PLATE));
        addon("square_besagews_decoration", "Square Besagews Decoration", "misc", 8, "magistuarmoryaddon:square_besagews_decoration",
                p("P P", "S S", "L L"), k('P', STEEL_PLATE), k('S', SMALL_PLATE), k('L', LEATHER_STRIP));
        addon("sturmhaube", "Sturmhaube", "armor", 9, "magistuarmoryaddon:sturmhaube",
                p("III", "NBN", " P "), k('I', HEATED_STEEL), k('N', STEEL_NUGGET), k('B', "item:magistuarmoryaddon:cervelliere"), k('P', STEEL_PLATE));
        addon("sugarloaf_helmet", "Sugarloaf Helmet", "armor", 10, "magistuarmoryaddon:sugarloaf_helmet",
                p("IPI", "P P", "ICI"), k('I', HEATED_STEEL), k('P', STEEL_PLATE), k('C', CHAINMAIL));
        addon("tablet_helmet", "Tablet Helmet", "armor", 11, "magistuarmoryaddon:tablet_helmet",
                p("PPP", "CNC", "CCC"), k('P', STEEL_PLATE), k('C', CHAINMAIL), k('N', STEEL_NUGGET));
        addon("two_eye_slits_sallet", "Two Eye Slits Sallet", "armor", 7, "magistuarmoryaddon:two_eye_slits_sallet",
                p(" B ", "SSS", " N "), k('B', "item:magistuarmory:barbute"), k('S', SMALL_PLATE), k('N', STEEL_NUGGET));
        addon("visored_kettlehat", "Visored Kettlehat", "armor", 6, "magistuarmoryaddon:visored_kettlehat",
                p("   ", " B ", "PPP"), k('B', "item:magistuarmory:kettlehat"), k('P', STEEL_PLATE));
        addon("closed_burgonet", "Closed Burgonet", "armor", 5, "magistuarmoryaddon:closed_burgonet",
                p("   ", "PBP", "   "), k('P', STEEL_PLATE), k('B', "item:magistuarmoryaddon:late_burgonet"));
        addon("early_greathelm", "Early Greathelm", "armor", 4, "magistuarmoryaddon:early_greathelm",
                p(" B ", " P ", "   "), k('B', "item:magistuarmoryaddon:tablet_helmet"), k('P', STEEL_PLATE));
        addon("golden_cross_necklace_decoration", "Golden Cross Necklace Decoration", "misc", 8, "magistuarmoryaddon:golden_cross_necklace_decoration",
                p(" GB", "GGG", " G "), k('G', GOLD_NUGGET), k('B', "item:magistuarmoryaddon:golden_necklace_decoration"));
        addon("kulah_khud", "Kulah Khud", "armor", 7, "magistuarmoryaddon:kulah_khud",
                p(" I ", "GBG", " C "), k('I', HEATED_STEEL), k('G', GOLD_NUGGET), k('B', "item:magistuarmoryaddon:tablet_helmet"), k('C', CHAINMAIL));
        addon("late_greathelm", "Late Greathelm", "armor", 8, "magistuarmoryaddon:late_greathelm",
                p("   ", "PBP", "PIP"), k('P', STEEL_PLATE), k('B', "item:magistuarmoryaddon:tablet_helmet"), k('I', HEATED_STEEL));

        // Forged base plus loose parts only: assembled, the base's quality carries over
        assemble("proto_maximilian_boots", "item:magistuarmory:knight_boots", "magistuarmoryaddon:proto_maximilian_boots", BLAZE_POWDER, BLAZE_POWDER);  // Proto Maximilian Boots
        assemble("proto_maximilian_chestplate", "item:magistuarmory:knight_chestplate", "magistuarmoryaddon:proto_maximilian_chestplate", BLAZE_POWDER, BLAZE_POWDER, BLAZE_POWDER);  // Proto Maximilian Chestplate

        // Everything else in the addon stays vanilla crafting:
        //   alla_tedesca_boots: not forgeable: minecraft:red_dye
        //   alla_tedesca_chestplate: not forgeable: minecraft:red_dye
        //   black_cross_medieval_cloak_decoration: not forgeable: minecraft:black_dye
        //   black_puff_and_slash_chestplate: not forgeable: minecraft:black_dye
        //   black_puff_and_slash_leggings: not forgeable: minecraft:black_dye
        //   black_puff_and_slash_sleeves_decoration: not forgeable: minecraft:black_dye
        //   blue_puff_and_slash_chestplate: not forgeable: minecraft:blue_dye
        //   blue_puff_and_slash_leggings: not forgeable: minecraft:blue_dye
        //   blue_puff_and_slash_sleeves_decoration: not forgeable: minecraft:blue_dye
        //   brown_puff_and_slash_chestplate: not forgeable: minecraft:brown_dye
        //   brown_puff_and_slash_leggings: not forgeable: minecraft:brown_dye
        //   brown_puff_and_slash_sleeves_decoration: not forgeable: minecraft:brown_dye
        //   chained_gambeson: base magistuarmory:gambeson_chestplate is not forged
        //   chained_gambeson_boots: base magistuarmory:gambeson_boots is not forged
        //   chainmail_hood_decoration: base magistuarmory:chainmail_helmet is not forged
        //   coat_of_plates_boots: base magistuarmory:chainmail_boots is not forged
        //   coat_of_plates_chestplate: base magistuarmory:chainmail_chestplate is not forged
        //   condottiero_cap: not forgeable: minecraft:red_dye
        //   cyan_puff_and_slash_chestplate: not forgeable: minecraft:cyan_dye
        //   cyan_puff_and_slash_leggings: not forgeable: minecraft:cyan_dye
        //   cyan_puff_and_slash_sleeves_decoration: not forgeable: minecraft:cyan_dye
        //   doublet: not forgeable: minecraft:white_wool
        //   fancy_hat: not forgeable: minecraft:red_wool
        //   giornea_decoration: not forgeable: minecraft:red_dye
        //   golden_pince_nez_decoration: not forgeable: minecraft:ghast_tear, minecraft:gold_nugget
        //   gray_puff_and_slash_chestplate: not forgeable: minecraft:gray_dye
        //   gray_puff_and_slash_leggings: not forgeable: minecraft:gray_dye
        //   gray_puff_and_slash_sleeves_decoration: not forgeable: minecraft:gray_dye
        //   green_puff_and_slash_chestplate: not forgeable: minecraft:green_dye
        //   green_puff_and_slash_leggings: not forgeable: minecraft:green_dye
        //   green_puff_and_slash_sleeves_decoration: not forgeable: minecraft:green_dye
        //   greenwich_boots: not forgeable: minecraft:red_dye
        //   hanging_cloth_decoration: not forgeable: minecraft:red_dye
        //   heavy_brigandine_chestplate: base magistuarmory:brigandine_chestplate is not forged
        //   horse_tail_decoration: not forgeable: minecraft:red_dye
        //   landsknecht_black_hat_decoration: not forgeable: minecraft:black_dye
        //   landsknecht_blue_hat_decoration: not forgeable: minecraft:blue_dye
        //   landsknecht_brown_hat_decoration: not forgeable: minecraft:brown_dye
        //   landsknecht_cyan_hat_decoration: not forgeable: minecraft:cyan_dye
        //   landsknecht_feathers_decoration: no metal
        //   landsknecht_gray_hat_decoration: not forgeable: minecraft:gray_dye
        //   landsknecht_green_hat_decoration: not forgeable: minecraft:green_dye
        //   landsknecht_light_blue_hat_decoration: not forgeable: minecraft:light_blue_dye
        //   landsknecht_light_gray_hat_decoration: not forgeable: minecraft:light_gray_dye
        //   landsknecht_lime_hat_decoration: not forgeable: minecraft:lime_dye
        //   landsknecht_magenta_hat_decoration: not forgeable: minecraft:magenta_dye
        //   landsknecht_orange_hat_decoration: not forgeable: minecraft:orange_dye
        //   landsknecht_pink_hat_decoration: not forgeable: minecraft:pink_dye
        //   landsknecht_purple_hat_decoration: not forgeable: minecraft:purple_dye
        //   landsknecht_red_hat_decoration: not forgeable: minecraft:red_dye
        //   landsknecht_white_hat_decoration: not forgeable: minecraft:white_dye
        //   landsknecht_yellow_hat_decoration: not forgeable: minecraft:yellow_dye
        //   leather_gloves_decoration: no metal
        //   light_blue_puff_and_slash_chestplate: not forgeable: minecraft:light_blue_dye
        //   light_blue_puff_and_slash_leggings: not forgeable: minecraft:light_blue_dye
        //   light_gray_puff_and_slash_chestplate: not forgeable: minecraft:light_gray_dye
        //   light_gray_puff_and_slash_leggings: not forgeable: minecraft:light_gray_dye
        //   light_gray_puff_and_slash_sleeves_decoration: not forgeable: minecraft:light_gray_dye
        //   lime_puff_and_slash_chestplate: not forgeable: minecraft:lime_dye
        //   lime_puff_and_slash_leggings: not forgeable: minecraft:lime_dye
        //   lime_puff_and_slash_sleeves_decoration: not forgeable: minecraft:lime_dye
        //   linen_coif: no metal
        //   magenta_puff_and_slash_chestplate: not forgeable: minecraft:magenta_dye
        //   magenta_puff_and_slash_leggings: not forgeable: minecraft:magenta_dye
        //   magenta_puff_and_slash_sleeves_decoration: not forgeable: minecraft:magenta_dye
        //   medieval_cloak_decoration: no metal
        //   mirror_chestplate: base magistuarmory:lamellar_chestplate is not forged
        //   orange_puff_and_slash_chestplate: not forgeable: minecraft:orange_dye
        //   orange_puff_and_slash_leggings: not forgeable: minecraft:orange_dye
        //   orange_puff_and_slash_sleeves_decoration: not forgeable: minecraft:orange_dye
        //   pikeman_boots: base magistuarmory:chainmail_boots is not forged
        //   pink_puff_and_slash_chestplate: not forgeable: minecraft:pink_dye
        //   pink_puff_and_slash_leggings: not forgeable: minecraft:pink_dye
        //   pink_puff_and_slash_sleeves_decoration: not forgeable: minecraft:pink_dye
        //   puff_and_slash_boots: not forgeable: minecraft:brown_dye
        //   puff_and_slash_robe_decoration: not forgeable: minecraft:black_dye, minecraft:red_dye
        //   purple_puff_and_slash_chestplate: not forgeable: minecraft:purple_dye
        //   purple_puff_and_slash_leggings: not forgeable: minecraft:purple_dye
        //   purple_puff_and_slash_sleeves_decoration: not forgeable: minecraft:purple_dye
        //   red_cross_medieval_cloak_decoration: not forgeable: minecraft:red_dye
        //   red_puff_and_slash_chestplate: not forgeable: minecraft:red_dye
        //   red_puff_and_slash_leggings: not forgeable: minecraft:red_dye
        //   red_puff_and_slash_sleeves_decoration: not forgeable: minecraft:red_dye
        //   royal_plume_decoration: no metal
        //   saracen_boots: not forgeable: minecraft:red_dye
        //   saracen_chestplate: not forgeable: minecraft:red_dye
        //   saracen_helmet: not forgeable: minecraft:red_dye, minecraft:string
        //   shoes: no metal
        //   silver_cross_necklace_decoration: not forgeable: #c:silver_nuggets
        //   silver_necklace_decoration: not forgeable: #c:silver_nuggets
        //   splint_chestplate: base magistuarmory:brigandine_chestplate is not forged
        //   straw_hat: not forgeable: minecraft:wheat
        //   tilted_puff_and_slash_hat: base magistuarmoryaddon:landsknecht_black_hat_decoration is not forged
        //   training_sword: not forgeable: #minecraft:planks
        //   tunic: no metal
        //   tunic_boots: no metal
        //   underarmor_tunic_decoration: not forgeable: minecraft:red_dye
        //   white_cross_medieval_cloak_decoration: not forgeable: minecraft:black_dye
        //   white_puff_and_slash_chestplate: not forgeable: minecraft:white_dye
        //   white_puff_and_slash_leggings: not forgeable: minecraft:white_dye
        //   white_puff_and_slash_sleeves_decoration: not forgeable: minecraft:white_dye
        //   xiii_century_knight_boots: base magistuarmory:chainmail_boots is not forged
        //   xiii_century_knight_chestplate: base magistuarmory:chainmail_chestplate is not forged
        //   xiii_century_knight_leggings: base magistuarmory:pantyhose is not forged
        //   yellow_puff_and_slash_chestplate: not forgeable: minecraft:yellow_dye
        //   yellow_puff_and_slash_leggings: not forgeable: minecraft:yellow_dye
        //   yellow_puff_and_slash_sleeves_decoration: not forgeable: minecraft:yellow_dye
    }

    public static List<Entry> of(boolean addon) {
        List<Entry> out = new ArrayList<>();
        for (Entry e : ENTRIES) if (e.addon() == addon) out.add(e);
        return out;
    }

    public static List<Entry> all() { return List.copyOf(ENTRIES); }

    // ── row helpers ───────────────────────────────────────────────────────────

    private record Key(char c, String spec) {}

    private static String[] p(String... rows) { return rows; }
    private static Key k(char c, String spec) { return new Key(c, spec); }

    private static Map<Character, String> keys(Key... keys) {
        Map<Character, String> m = new LinkedHashMap<>();
        for (Key key : keys) m.put(key.c(), key.spec());
        return m;
    }

    private static void armor(String name, String displayName, int hammering, String[] pattern, Key... keys) {
        ENTRIES.add(new Entry("forging/" + name, displayName, name, "armor", hammering, pattern, keys(keys),
                "magistuarmory:" + name, true, true, true, false));
    }

    private static void addon(String name, String displayName, String category, int hammering, String result,
                              String[] pattern, Key... keys) {
        addon(name, displayName, category, hammering, result, name, pattern, keys);
    }

    private static void assemble(String name, String base, String result, String... extra) {
        ASSEMBLIES.add(new Assembly(name, base, extra, result, true));
    }

    private static void addon(String name, String displayName, String category, int hammering, String result,
                              String tooltype, String[] pattern, Key... keys) {
        ENTRIES.add(new Entry("forging/" + name, displayName, tooltype, category, hammering, pattern, keys(keys),
                result, true, true, true, true));
    }
}

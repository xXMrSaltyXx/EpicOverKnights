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
    private static final String GOLD_NUGGET  = "tag:" + Mappings.COMMON + ":nuggets/gold";
    private static final String GOLD_INGOT   = "tag:" + Mappings.COMMON + ":ingots/gold";

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

        // ── Epic Knights: Addon — pure-metal armour and decorations, generated by
        //    tools/derive_addon.py --table (grid 1:1 the addon recipe, steel only) ──
        addon("mustache_decoration", "Mustache Decoration", "misc", 3, "magistuarmoryaddon:steel_mustache_decoration",
                p("N N", " N ", "   "), k('N', STEEL_NUGGET));
        addon("puff_and_slash_boots", "Puff And Slash Boots", "armor", 7, "magistuarmoryaddon:steel_puff_and_slash_boots",
                p("NNN", "INI", "P P"), k('N', STEEL_NUGGET), k('I', HEATED_STEEL), k('P', STEEL_PLATE));
        addon("puff_and_slash_chestplate", "Puff And Slash Chestplate", "armor", 7, "magistuarmoryaddon:steel_puff_and_slash_chestplate",
                p("NNN", "IBI", "INI"), k('N', STEEL_NUGGET), k('I', HEATED_STEEL), k('B', "item:magistuarmory:halfarmor_chestplate"));
        addon("skirt_decoration", "Skirt Decoration", "misc", 7, "magistuarmoryaddon:steel_skirt_decoration",
                p(" P ", "IPI", "IPI"), k('P', STEEL_PLATE), k('I', HEATED_STEEL));
        addon("avant_boots", "Avant Boots", "armor", 6, "magistuarmoryaddon:avant_boots",
                p("I I", "I I", "S S"), k('I', HEATED_STEEL), k('S', SMALL_PLATE));
        addon("avant_chestplate", "Avant Chestplate", "armor", 7, "magistuarmoryaddon:avant_chestplate",
                p("I I", "IBI", "SIS"), k('I', HEATED_STEEL), k('B', "item:magistuarmory:halfarmor_chestplate"), k('S', SMALL_PLATE));
        addon("avant_leggings", "Avant Leggings", "armor", 9, "magistuarmoryaddon:avant_leggings",
                p("III", "ISI", "ISI"), k('I', HEATED_STEEL), k('S', SMALL_PLATE));
        addon("bicoque", "Bicoque", "armor", 4, "magistuarmoryaddon:bicoque",
                p("   ", "PBP", " I "), k('P', STEEL_PLATE), k('B', "item:magistuarmory:norman_helmet"), k('I', HEATED_STEEL));
        addon("british_armet", "British Armet", "armor", 4, "magistuarmoryaddon:british_armet",
                p(" I ", "IBI", " P "), k('I', HEATED_STEEL), k('B', "item:magistuarmory:barbute"), k('P', STEEL_PLATE));
        addon("burgundian_kettlehat", "Burgundian Kettlehat", "armor", 8, "magistuarmoryaddon:burgundian_kettlehat",
                p("NIN", "INI", "P P"), k('N', STEEL_NUGGET), k('I', HEATED_STEEL), k('P', STEEL_PLATE));
        addon("cabasset", "Cabasset", "armor", 6, "magistuarmoryaddon:cabasset",
                p(" I ", "IPI", "I I"), k('I', HEATED_STEEL), k('P', STEEL_PLATE));
        addon("close_helmet", "Close Helmet", "armor", 4, "magistuarmoryaddon:close_helmet",
                p("   ", "PBP", " I "), k('P', STEEL_PLATE), k('B', "item:magistuarmory:barbute"), k('I', HEATED_STEEL));
        addon("closed_barbute", "Closed Barbute", "armor", 7, "magistuarmoryaddon:closed_barbute",
                p("III", "PNP", " P "), k('I', HEATED_STEEL), k('P', STEEL_PLATE), k('N', STEEL_NUGGET));
        addon("closed_burgonet", "Closed Burgonet", "armor", 4, "magistuarmoryaddon:closed_burgonet",
                p("   ", "PBP", "   "), k('P', STEEL_PLATE), k('B', "item:magistuarmoryaddon:late_burgonet"));
        addon("codpiece_decoration", "Codpiece Decoration", "misc", 4, "magistuarmoryaddon:codpiece_decoration",
                p(" S ", " P ", "   "), k('S', SMALL_PLATE), k('P', STEEL_PLATE));
        addon("cuman_captain_helmet", "Cuman Captain Helmet", "armor", 4, "magistuarmoryaddon:cuman_captain_helmet",
                p(" I ", "CBC", " C "), k('I', HEATED_STEEL), k('C', CHAINMAIL), k('B', "item:magistuarmory:face_helmet"));
        addon("cuman_helmet", "Cuman Helmet", "armor", 4, "magistuarmoryaddon:cuman_helmet",
                p(" N ", " B ", " C "), k('N', STEEL_NUGGET), k('B', "item:magistuarmory:face_helmet"), k('C', CHAINMAIL));
        addon("early_greathelm", "Early Greathelm", "armor", 4, "magistuarmoryaddon:early_greathelm",
                p(" B ", " P ", "   "), k('B', "item:magistuarmoryaddon:tablet_helmet"), k('P', STEEL_PLATE));
        addon("embosed_parade_boots", "Embosed Parade Boots", "armor", 6, "magistuarmoryaddon:embosed_parade_boots",
                p("N N", "P P", "I I"), k('N', STEEL_NUGGET), k('P', STEEL_PLATE), k('I', HEATED_STEEL));
        addon("embosed_parade_burgonet", "Embosed Parade Burgonet", "armor", 5, "magistuarmoryaddon:embosed_parade_burgonet",
                p("NNN", "NBN", "   "), k('N', STEEL_NUGGET), k('B', "item:magistuarmory:cuirassier_helmet"));
        addon("embosed_parade_chestplate", "Embosed Parade Chestplate", "armor", 8, "magistuarmoryaddon:embosed_parade_chestplate",
                p("NPN", "PBP", "PPP"), k('N', STEEL_NUGGET), k('P', STEEL_PLATE), k('B', "item:magistuarmory:halfarmor_chestplate"));
        addon("english_knight_chestplate", "English Knight Chestplate", "armor", 7, "magistuarmoryaddon:english_knight_chestplate",
                p("P P", "PBP", "III"), k('P', STEEL_PLATE), k('B', "item:magistuarmory:halfarmor_chestplate"), k('I', HEATED_STEEL));
        addon("engraved_chestplate", "Engraved Chestplate", "armor", 8, "magistuarmoryaddon:engraved_chestplate",
                p("I I", "ISI", "SSS"), k('I', HEATED_STEEL), k('S', SMALL_PLATE));
        addon("engraved_close_helmet", "Engraved Close Helmet", "armor", 8, "magistuarmoryaddon:engraved_close_helmet",
                p("III", "SIS", "S S"), k('I', HEATED_STEEL), k('S', SMALL_PLATE));
        addon("german_bascinet", "German Bascinet", "armor", 4, "magistuarmoryaddon:german_bascinet",
                p(" S ", " B ", " S "), k('S', SMALL_PLATE), k('B', "item:magistuarmory:norman_helmet"));
        addon("golden_ball_decoration", "Golden Ball Decoration", "misc", 4, "magistuarmoryaddon:golden_ball_decoration",
                p("   ", " A ", " G "), k('A', GOLD_INGOT), k('G', GOLD_NUGGET));
        addon("golden_cross_necklace_decoration", "Golden Cross Necklace Decoration", "misc", 5, "magistuarmoryaddon:golden_cross_necklace_decoration",
                p(" GB", "GGG", " G "), k('G', GOLD_NUGGET), k('B', "item:magistuarmoryaddon:golden_necklace_decoration"));
        addon("golden_necklace_decoration", "Golden Necklace Decoration", "misc", 8, "magistuarmoryaddon:golden_necklace_decoration",
                p("GGG", "G G", "GGG"), k('G', GOLD_NUGGET));
        addon("gorget_decoration", "Gorget Decoration", "misc", 4, "magistuarmoryaddon:gorget_decoration",
                p(" P ", "   ", "   "), k('P', STEEL_PLATE));
        addon("greenwich_armet", "Greenwich Armet", "armor", 4, "magistuarmoryaddon:greenwich_armet",
                p("   ", "NBN", " I "), k('N', STEEL_NUGGET), k('B', "item:magistuarmory:armet"), k('I', HEATED_STEEL));
        addon("greenwich_chestplate", "Greenwich Chestplate", "armor", 4, "magistuarmoryaddon:greenwich_chestplate",
                p("   ", "IBI", "N N"), k('I', HEATED_STEEL), k('B', "item:magistuarmory:knight_chestplate"), k('N', STEEL_NUGGET));
        addon("grilled_helmet", "Grilled Helmet", "armor", 8, "magistuarmoryaddon:grilled_helmet",
                p("III", "NNN", "S S"), k('I', HEATED_STEEL), k('N', STEEL_NUGGET), k('S', SMALL_PLATE));
        addon("heavy_cuirassier_chestplate", "Heavy Cuirassier Chestplate", "armor", 6, "magistuarmoryaddon:heavy_cuirassier_chestplate",
                p("P P", "PBP", "I I"), k('P', STEEL_PLATE), k('B', "item:magistuarmory:cuirassier_chestplate"), k('I', HEATED_STEEL));
        addon("helmet_rondel_decoration", "Helmet Rondel Decoration", "misc", 4, "magistuarmoryaddon:helmet_rondel_decoration",
                p("   ", "N N", "   "), k('N', STEEL_NUGGET));
        addon("klappvisor_bascinet", "Klappvisor Bascinet", "armor", 4, "magistuarmoryaddon:klappvisor_bascinet",
                p("   ", "IBI", "   "), k('I', HEATED_STEEL), k('B', "item:magistuarmory:norman_helmet"));
        addon("kulah_khud", "Kulah Khud", "armor", 4, "magistuarmoryaddon:kulah_khud",
                p(" I ", "GBG", " C "), k('I', HEATED_STEEL), k('G', GOLD_NUGGET), k('B', "item:magistuarmoryaddon:tablet_helmet"), k('C', CHAINMAIL));
        addon("late_bascinet", "Late Bascinet", "armor", 4, "magistuarmoryaddon:late_bascinet",
                p("   ", "IBI", "R R"), k('I', HEATED_STEEL), k('B', "item:magistuarmory:norman_helmet"), k('R', STEEL_RING));
        addon("late_burgonet", "Late Burgonet", "armor", 7, "magistuarmoryaddon:late_burgonet",
                p(" I ", "PIP", "PRP"), k('I', HEATED_STEEL), k('P', STEEL_PLATE), k('R', STEEL_RING));
        addon("late_greathelm", "Late Greathelm", "armor", 5, "magistuarmoryaddon:late_greathelm",
                p("   ", "PBP", "PIP"), k('P', STEEL_PLATE), k('B', "item:magistuarmoryaddon:tablet_helmet"), k('I', HEATED_STEEL));
        addon("late_kettlehat", "Late Kettlehat", "armor", 8, "magistuarmoryaddon:late_kettlehat",
                p("PIP", "P P", "CCC"), k('P', STEEL_PLATE), k('I', HEATED_STEEL), k('C', CHAINMAIL));
        addon("late_sallet", "Late Sallet", "armor", 6, "magistuarmoryaddon:late_sallet",
                p(" N ", "NBN", "PPP"), k('N', STEEL_NUGGET), k('B', "item:magistuarmory:barbute"), k('P', STEEL_PLATE));
        addon("light_burgonet", "Light Burgonet", "armor", 6, "magistuarmoryaddon:light_burgonet",
                p(" I ", "PIP", "I I"), k('I', HEATED_STEEL), k('P', STEEL_PLATE));
        addon("light_cuman_helmet", "Light Cuman Helmet", "armor", 4, "magistuarmoryaddon:light_cuman_helmet",
                p(" N ", " S ", "S S"), k('N', STEEL_NUGGET), k('S', SMALL_PLATE));
        addon("lion_helmet", "Lion Helmet", "armor", 5, "magistuarmoryaddon:lion_helmet",
                p("ABA", "PPP", "   "), k('A', GOLD_INGOT), k('B', "item:magistuarmory:barbute"), k('P', STEEL_PLATE));
        addon("lobster_tailed_helmet", "Lobster Tailed Helmet", "armor", 4, "magistuarmoryaddon:lobster_tailed_helmet",
                p(" P ", "NBN", " N "), k('P', STEEL_PLATE), k('N', STEEL_NUGGET), k('B', "item:magistuarmoryaddon:cervelliere"));
        addon("mamluk_helmet", "Mamluk Helmet", "armor", 9, "magistuarmoryaddon:mamluk_helmet",
                p("III", "SNS", "CCC"), k('I', HEATED_STEEL), k('S', SMALL_PLATE), k('N', STEEL_NUGGET), k('C', CHAINMAIL));
        addon("milanese_armet", "Milanese Armet", "armor", 4, "magistuarmoryaddon:milanese_armet",
                p("   ", "IBI", " P "), k('I', HEATED_STEEL), k('B', "item:magistuarmory:barbute"), k('P', STEEL_PLATE));
        addon("morion", "Morion", "armor", 6, "magistuarmoryaddon:morion",
                p(" P ", "III", "P P"), k('P', STEEL_PLATE), k('I', HEATED_STEEL));
        addon("peascod_chestplate", "Peascod Chestplate", "armor", 7, "magistuarmoryaddon:peascod_chestplate",
                p("S S", "NBN", "SNS"), k('S', SMALL_PLATE), k('N', STEEL_NUGGET), k('B', "item:magistuarmory:halfarmor_chestplate"));
        addon("sallet_without_neck_protection", "Sallet Without Neck Protection", "armor", 4, "magistuarmoryaddon:sallet_without_neck_protection",
                p("   ", " B ", "P P"), k('B', "item:magistuarmory:barbute"), k('P', STEEL_PLATE));
        addon("sallet_without_visor", "Sallet Without Visor", "armor", 4, "magistuarmoryaddon:sallet_without_visor",
                p("   ", "SBS", "   "), k('S', SMALL_PLATE), k('B', "item:magistuarmory:barbute"));
        addon("savoyard_helmet", "Savoyard Helmet", "armor", 4, "magistuarmoryaddon:savoyard_helmet",
                p("   ", " BP", "   "), k('B', "item:magistuarmory:cuirassier_helmet"), k('P', STEEL_PLATE));
        addon("sturmhaube", "Sturmhaube", "armor", 6, "magistuarmoryaddon:sturmhaube",
                p("III", "NBN", " P "), k('I', HEATED_STEEL), k('N', STEEL_NUGGET), k('B', "item:magistuarmoryaddon:cervelliere"), k('P', STEEL_PLATE));
        addon("sugarloaf_helmet", "Sugarloaf Helmet", "armor", 8, "magistuarmoryaddon:sugarloaf_helmet",
                p("IPI", "P P", "ICI"), k('I', HEATED_STEEL), k('P', STEEL_PLATE), k('C', CHAINMAIL));
        addon("tablet_helmet", "Tablet Helmet", "armor", 9, "magistuarmoryaddon:tablet_helmet",
                p("PPP", "CNC", "CCC"), k('P', STEEL_PLATE), k('C', CHAINMAIL), k('N', STEEL_NUGGET));
        addon("two_eye_slits_sallet", "Two Eye Slits Sallet", "armor", 4, "magistuarmoryaddon:two_eye_slits_sallet",
                p(" B ", "SSS", " N "), k('B', "item:magistuarmory:barbute"), k('S', SMALL_PLATE), k('N', STEEL_NUGGET));
        addon("visored_kettlehat", "Visored Kettlehat", "armor", 4, "magistuarmoryaddon:visored_kettlehat",
                p("   ", " B ", "PPP"), k('B', "item:magistuarmory:kettlehat"), k('P', STEEL_PLATE));

        // Everything else in the addon (cloth, leather, dye, feathers, or built on a base we do not
        // forge) stays vanilla crafting; derive_addon.py --table lists each piece with its reason.
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

    private static void addon(String name, String displayName, String category, int hammering, String result,
                              String tooltype, String[] pattern, Key... keys) {
        ENTRIES.add(new Entry("forging/" + name, displayName, tooltype, category, hammering, pattern, keys(keys),
                result, true, true, true, true));
    }
}

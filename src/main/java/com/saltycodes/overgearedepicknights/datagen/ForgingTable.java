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
    }

    private static final String HEATED_STEEL = "item:overgeared:heated_steel_ingot";
    private static final String STEEL_PLATE  = "item:overgeared:steel_plate";
    private static final String STEEL_NUGGET = "item:overgeared:steel_nugget";
    private static final String SMALL_PLATE  = "item:magistuarmory:small_steel_plate";
    private static final String CHAINMAIL    = "item:magistuarmory:steel_chainmail";

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

        // ── Epic Knights: Addon — all-metal pieces forged directly (steel only) ──
        addon("puff_and_slash_chestplate", "Puff And Slash Chestplate", "armor", 7, p("NNN", "IHI", "INI"),
                k('N', STEEL_NUGGET), k('I', HEATED_STEEL), k('H', "item:magistuarmory:halfarmor_chestplate"));
        addon("puff_and_slash_boots", "Puff And Slash Boots", "armor", 7, p("NNN", "INI", "P P"),
                k('N', STEEL_NUGGET), k('I', HEATED_STEEL), k('P', STEEL_PLATE));
        addon("skirt_decoration", "Skirt Decoration", "misc", 7, p(" P ", "IPI", "IPI"),
                k('P', STEEL_PLATE), k('I', HEATED_STEEL));
        addon("mustache_decoration", "Mustache Decoration", "misc", 3, p("N N", " N ", "   "),
                k('N', STEEL_NUGGET));
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

    private static void addon(String name, String displayName, String category, int hammering, String[] pattern, Key... keys) {
        ENTRIES.add(new Entry("forging/" + name, displayName, name, category, hammering, pattern, keys(keys),
                "magistuarmoryaddon:steel_" + name, true, true, true, true));
    }
}

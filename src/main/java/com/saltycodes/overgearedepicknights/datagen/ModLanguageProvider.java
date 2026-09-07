package com.saltycodes.overgearedepicknights.datagen;

import com.saltycodes.overgearedepicknights.OvergearedEpicKnights;
import com.saltycodes.overgearedepicknights.items.BladeMaterial;
import com.saltycodes.overgearedepicknights.items.BladeType;
import net.minecraft.data.PackOutput;
//? if forge {
import net.minecraftforge.common.data.LanguageProvider;
//?} else {
/*import net.neoforged.neoforge.common.data.LanguageProvider;
*///?}

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * en_us.json — every name is derived from {@link BladeType}, {@link ForgingTable} and the shield
 * catalogue, so a new entry never needs a hand-written translation line.
 */
public class ModLanguageProvider extends LanguageProvider {
    private static final String ITEM = "item." + OvergearedEpicKnights.MODID + ".";
    private static final String TOOLTYPE = "tooltype.overgeared.";

    public ModLanguageProvider(PackOutput output) {
        super(output, OvergearedEpicKnights.MODID, "en_us");
    }

    @Override
    protected void addTranslations() {
        // LinkedHashMap: the same tool type may be shared by several entries (messer_sword).
        Map<String, String> lang = new LinkedHashMap<>();

        for (BladeType type : BladeType.values()) {
            for (BladeMaterial mat : type.getMaterials()) {
                lang.put(ITEM + type.itemPath(mat), type.itemName(mat));
            }
            lang.put(TOOLTYPE + type.getTooltype(), type.getDisplayName());
        }
        lang.put(TOOLTYPE + "chainmorgenstern", "Flail");

        lang.put(ITEM + "gold_plate", "Gold Plate");
        lang.put(ITEM + "bronze_plate", "Bronze Plate");
        lang.put(ITEM + "tin_plate", "Tin Plate");
        lang.put(ITEM + "silver_plate", "Silver Plate");
        lang.put(ITEM + "crusader_surcoat", "Crusader Surcoat");
        lang.put(ITEM + "hussar_wings", "Hussar Wings");

        for (Map.Entry<String, String> shield : ForgingTable.SHIELDS.entrySet()) {
            lang.put(TOOLTYPE + shield.getKey(), shield.getValue());
        }
        for (ForgingTable.Entry entry : ForgingTable.all()) {
            if (entry.blueprint() != null) lang.put(TOOLTYPE + entry.blueprint(), entry.displayName());
        }

        lang.forEach(this::add);
    }
}

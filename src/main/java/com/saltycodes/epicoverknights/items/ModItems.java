package com.saltycodes.epicoverknights.items;

import com.saltycodes.epicoverknights.EpicOverKnights;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS,
            EpicOverKnights.MODID);

    /**
     * All blade items, keyed by BladeType → BladeMaterial → RegistryObject.
     * To add a new blade type, add an entry to {@link BladeType}.
     * To add a new material, add an entry to {@link BladeMaterial}.
     */
    public static final Map<BladeType, Map<BladeMaterial, RegistryObject<Item>>> BLADES;

    static {
        Map<BladeType, Map<BladeMaterial, RegistryObject<Item>>> blades = new EnumMap<>(BladeType.class);
        for (BladeType type : BladeType.values()) {
            Map<BladeMaterial, RegistryObject<Item>> materialMap = new EnumMap<>(BladeMaterial.class);
            for (BladeMaterial material : type.getMaterials()) {
                String id = material.getName() + "_" + type.getName() + "_blade";
                materialMap.put(material, ITEMS.register(id, () -> new Item(new Item.Properties())));
            }
            blades.put(type, Collections.unmodifiableMap(materialMap));
        }
        BLADES = Collections.unmodifiableMap(blades);
    }

    /**
     * Convenience method to get a specific blade's RegistryObject.
     */
    public static RegistryObject<Item> getBlade(BladeType type, BladeMaterial material) {
        return BLADES.get(type).get(material);
    }

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}

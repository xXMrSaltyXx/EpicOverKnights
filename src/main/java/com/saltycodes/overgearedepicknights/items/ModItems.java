package com.saltycodes.overgearedepicknights.items;

import com.saltycodes.overgearedepicknights.OvergearedEpicKnights;
import net.minecraft.world.item.Item;
//? if forge {
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
//?} else {
/*import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
*///?}

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public class ModItems {
    //? if forge {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS,
            OvergearedEpicKnights.MODID);
    //?} else {
    /*public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(OvergearedEpicKnights.MODID);
    *///?}

    /**
     * All blade items, keyed by BladeType → BladeMaterial → registered item holder.
     * Only {@link BladeType#active()} entries are registered: addon blades exist solely when
     * Epic Knights: Addon is loaded.
     * To add a new blade type, add an entry to {@link BladeType}.
     * To add a new material, add an entry to {@link BladeMaterial}.
     */
    //? if forge {
    public static final Map<BladeType, Map<BladeMaterial, RegistryObject<Item>>> BLADES;
    //?} else {
    /*public static final Map<BladeType, Map<BladeMaterial, DeferredItem<Item>>> BLADES;
    *///?}

    static {
        //? if forge {
        Map<BladeType, Map<BladeMaterial, RegistryObject<Item>>> blades = new EnumMap<>(BladeType.class);
        //?} else {
        /*Map<BladeType, Map<BladeMaterial, DeferredItem<Item>>> blades = new EnumMap<>(BladeType.class);
        *///?}
        for (BladeType type : BladeType.active()) {
            //? if forge {
            Map<BladeMaterial, RegistryObject<Item>> materialMap = new EnumMap<>(BladeMaterial.class);
            //?} else {
            /*Map<BladeMaterial, DeferredItem<Item>> materialMap = new EnumMap<>(BladeMaterial.class);
            *///?}
            for (BladeMaterial material : type.getMaterials()) {
                String id = type.itemPath(material);
                //? if forge {
                materialMap.put(material, ITEMS.register(id, () -> new Item(new Item.Properties())));
                //?} else {
                /*materialMap.put(material, ITEMS.registerSimpleItem(id));
                *///?}
            }
            blades.put(type, Collections.unmodifiableMap(materialMap));
        }
        BLADES = Collections.unmodifiableMap(blades);
    }

    //? if forge {
    public static RegistryObject<Item> getBlade(BladeType type, BladeMaterial material) {
    //?} else {
    /*public static DeferredItem<Item> getBlade(BladeType type, BladeMaterial material) {
    *///?}
        Map<BladeMaterial, ?> byMaterial = BLADES.get(type);
        if (byMaterial == null) {
            throw new IllegalStateException(type + " is not registered (Epic Knights: Addon missing?)");
        }
        //? if forge {
        return (RegistryObject<Item>) byMaterial.get(material);
        //?} else {
        /*return (DeferredItem<Item>) byMaterial.get(material);
        *///?}
    }

    //? if forge {
    public static final RegistryObject<Item> GOLD_PLATE = ITEMS.register("gold_plate",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> BRONZE_PLATE = ITEMS.register("bronze_plate",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> TIN_PLATE = ITEMS.register("tin_plate",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> SILVER_PLATE = ITEMS.register("silver_plate",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> CRUSADER_SURCOAT = ITEMS.register("crusader_surcoat",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> HUSSAR_WINGS = ITEMS.register("hussar_wings",
            () -> new Item(new Item.Properties()));
    //?} else {
    /*public static final DeferredItem<Item> GOLD_PLATE   = ITEMS.registerSimpleItem("gold_plate");
    public static final DeferredItem<Item> BRONZE_PLATE = ITEMS.registerSimpleItem("bronze_plate");
    public static final DeferredItem<Item> TIN_PLATE    = ITEMS.registerSimpleItem("tin_plate");
    public static final DeferredItem<Item> SILVER_PLATE = ITEMS.registerSimpleItem("silver_plate");
    public static final DeferredItem<Item> CRUSADER_SURCOAT = ITEMS.registerSimpleItem("crusader_surcoat");
    public static final DeferredItem<Item> HUSSAR_WINGS     = ITEMS.registerSimpleItem("hussar_wings");
    *///?}

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}

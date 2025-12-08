package com.mrmistersalty.epicoverknights.items;

import com.mrmistersalty.epicoverknights.EpicOverKnights;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
  public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS,
      EpicOverKnights.MODID);

  public static final RegistryObject<Item> COPPER_STYLET_BLADE = ITEMS.register("copper_stylet_blade", ()-> new Item(new Item.Properties()));
  public static final RegistryObject<Item> GOLD_STYLET_BLADE = ITEMS.register("gold_stylet_blade", ()-> new Item(new Item.Properties()));
  public static final RegistryObject<Item> TIN_STYLET_BLADE = ITEMS.register("tin_stylet_blade", ()-> new Item(new Item.Properties()));
  public static final RegistryObject<Item> STONE_STYLET_BLADE = ITEMS.register("stone_stylet_blade", ()-> new Item(new Item.Properties()));
  public static final RegistryObject<Item> SILVER_STYLET_BLADE = ITEMS.register("silver_stylet_blade", ()-> new Item(new Item.Properties()));
  public static final RegistryObject<Item> BRONZE_STYLET_BLADE = ITEMS.register("bronze_stylet_blade", ()-> new Item(new Item.Properties()));
  public static final RegistryObject<Item> IRON_STYLET_BLADE = ITEMS.register("iron_stylet_blade", ()-> new Item(new Item.Properties()));
  public static final RegistryObject<Item> STEEL_STYLET_BLADE = ITEMS.register("steel_stylet_blade", ()-> new Item(new Item.Properties()));

  public static void register(IEventBus bus) {
    ITEMS.register(bus);
  }
}

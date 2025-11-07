package com.mrmistersalty.epicoverknights.debug;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.logging.LogUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Debug command to list recipes from a specific mod.
 * Useful for finding recipe IDs to remove.
 *
 * Usage: /listrecipes <modid>
 * Example: /listrecipes magistuarmory
 *
 * IMPORTANT: This is a debug utility. Comment out the @Mod.EventBusSubscriber annotation
 * before releasing your mod to production!
 */
@Mod.EventBusSubscriber(modid = "epicoverknights", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RecipeDebugCommand {
    private static final Logger LOGGER = LogUtils.getLogger();

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(
            Commands.literal("listrecipes")
                .requires(source -> source.hasPermission(2)) // Requires OP level 2
                .then(Commands.argument("modid", StringArgumentType.string())
                    .executes(context -> {
                        String modid = StringArgumentType.getString(context, "modid");
                        return listRecipes(context.getSource(), modid);
                    })
                )
        );
    }

    private static int listRecipes(CommandSourceStack source, String modid) {
        RecipeManager recipeManager = source.getServer().getRecipeManager();

        try {
            // Access the recipe map
            var byNameField = RecipeManager.class.getDeclaredField("byName");
            byNameField.setAccessible(true);

            @SuppressWarnings("unchecked")
            var byName = (java.util.Map<ResourceLocation, ?>) byNameField.get(recipeManager);

            // Filter recipes by modid
            List<ResourceLocation> matchingRecipes = new ArrayList<>();
            for (ResourceLocation recipeId : byName.keySet()) {
                if (recipeId.getNamespace().equals(modid)) {
                    matchingRecipes.add(recipeId);
                }
            }

            // Sort for readability
            matchingRecipes.sort(ResourceLocation::compareTo);

            if (matchingRecipes.isEmpty()) {
                source.sendSuccess(() -> Component.literal("No recipes found for mod: " + modid), false);
                return 0;
            }

            // Send results to command executor
            source.sendSuccess(() -> Component.literal("Found " + matchingRecipes.size() + " recipes for " + modid + ":"), false);

            for (ResourceLocation recipeId : matchingRecipes) {
                source.sendSuccess(() -> Component.literal("  - " + recipeId.toString()), false);
                LOGGER.info("Recipe: {}", recipeId);
            }

            source.sendSuccess(() -> Component.literal("(Full list also printed to log)"), false);

            return matchingRecipes.size();

        } catch (Exception e) {
            source.sendFailure(Component.literal("Error accessing recipes: " + e.getMessage()));
            LOGGER.error("Failed to list recipes", e);
            return 0;
        }
    }
}

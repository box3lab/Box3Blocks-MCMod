package com.box3lab.box3js.registries;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Box3JSRecipeManager {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static RecipeManager recipeManager;
    private static List<RecipeHolder<?>> originalRecipes = List.of();
    private static final Set<ResourceLocation> blacklist = ConcurrentHashMap.newKeySet();

    public static void init(MinecraftServer server) {
        recipeManager = server.getRecipeManager();
        originalRecipes = List.copyOf(recipeManager.getRecipes());
        LOGGER.info("Box3JS recipe manager initialized with {} recipes.", originalRecipes.size());
    }

    public static List<String> listRecipes(String filter) {
        if (recipeManager == null) return List.of();
        String lower = filter.toLowerCase();
        return originalRecipes.stream()
            .filter(r -> r.id().toString().toLowerCase().contains(lower))
            .map(r -> r.id().toString())
            .sorted()
            .toList();
    }

    public static boolean removeRecipe(String id) {
        if (recipeManager == null) return false;
        ResourceLocation rl = ResourceLocation.tryParse(id);
        if (rl == null) return false;
        blacklist.add(rl);
        applyBlacklist();
        return true;
    }

    public static void clearRecipes() {
        if (recipeManager == null) return;
        blacklist.clear();
        recipeManager.replaceRecipes(originalRecipes);
    }

    private static void applyBlacklist() {
        if (blacklist.isEmpty()) {
            recipeManager.replaceRecipes(originalRecipes);
            return;
        }
        List<RecipeHolder<?>> filtered = new ArrayList<>();
        for (var recipe : originalRecipes) {
            if (!blacklist.contains(recipe.id())) {
                filtered.add(recipe);
            }
        }
        recipeManager.replaceRecipes(filtered);
        LOGGER.info("Box3JS recipe blacklist applied: {} recipes active ({} removed).",
            filtered.size(), blacklist.size());
    }
}

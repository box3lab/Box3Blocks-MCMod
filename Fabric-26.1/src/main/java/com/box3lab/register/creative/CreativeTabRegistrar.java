package com.box3lab.register.creative;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.box3lab.util.BlockIndexData;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.ItemLike;

public final class CreativeTabRegistrar {
    public static final String DEFAULT_MODEL_TAB = "models";

    private CreativeTabRegistrar() {
    }

    private static String sanitizeCategoryPath(String category) {
        if (category == null || category.isBlank()) {
            return "";
        }
        String lower = category.toLowerCase(Locale.ROOT);
        return lower.replaceAll("[^a-z0-9_\\-]+", "_");
    }

    private static ItemStack defaultIcon(Map<String, Block> blocks) {
        Block iconBlock = blocks.get("stone");
        if (iconBlock == null && !blocks.isEmpty()) {
            iconBlock = blocks.values().iterator().next();
        }
        return iconBlock == null ? new ItemStack(Items.STONE) : new ItemStack(iconBlock);
    }

    public static void registerCreativeTabs(String modId, Map<String, Block> blocks, BlockIndexData data) {
        Map<String, List<String>> categoryToRegistryNames = new HashMap<>();
        for (String rn : blocks.keySet()) {
            String voxelName = rn;
            String category = data.categoryByName.getOrDefault(voxelName, "");
            String categoryPath = sanitizeCategoryPath(category);
            if (categoryPath != null && !categoryPath.isBlank()) {
                categoryToRegistryNames.computeIfAbsent(categoryPath, k -> new ArrayList<>()).add(rn);
            }
        }

        List<String> categories = new ArrayList<>(categoryToRegistryNames.keySet());
        categories.sort(Comparator.naturalOrder());

        int tabColumn = 0;
        for (String categoryPath : categories) {
            List<String> registryNames = categoryToRegistryNames.get(categoryPath);
            if (registryNames == null || registryNames.isEmpty()) {
                continue;
            }
            registryNames.sort(Comparator.naturalOrder());

            Block categoryIconBlock = blocks.get(registryNames.get(0));

            ResourceKey<CreativeModeTab> key = ResourceKey.create(
                    BuiltInRegistries.CREATIVE_MODE_TAB.key(),
                    Identifier.fromNamespaceAndPath(modId, "creative_tab_" + categoryPath));
            CreativeModeTab tab = CreativeModeTab.builder(CreativeModeTab.Row.TOP, tabColumn++)
                    .icon(() -> categoryIconBlock == null ? defaultIcon(blocks) : new ItemStack(categoryIconBlock))
                    .title(Component.translatable("itemGroup." + modId + "." + categoryPath))
                    .displayItems((params, output) -> {
                        for (String rn : registryNames) {
                            Block b = blocks.get(rn);
                            if (b != null) {
                                output.accept(b.asItem());
                            }
                        }

                        List<ItemLike> extras = CreativeTabExtras.extras().get(categoryPath);
                        if (extras != null) {
                            for (ItemLike extra : extras) {
                                if (extra != null) {
                                    output.accept(extra);
                                }
                            }
                        }
                    })
                    .build();

            Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, key, tab);
        }
    }

    public static void registerModelTab(String modId) {
        String categoryPath = sanitizeCategoryPath(DEFAULT_MODEL_TAB);
        if (categoryPath.isBlank()) {
            return;
        }
        if (BuiltInRegistries.CREATIVE_MODE_TAB
                .containsKey(Identifier.fromNamespaceAndPath(modId, "creative_tab_" + categoryPath))) {
            return;
        }

        List<ItemLike> extras = CreativeTabExtras.extras().get(categoryPath);
        if (extras == null || extras.isEmpty()) {
            return;
        }

        ItemLike iconItem = extras.get(0);
        ResourceKey<CreativeModeTab> key = ResourceKey.create(
                BuiltInRegistries.CREATIVE_MODE_TAB.key(),
                Identifier.fromNamespaceAndPath(modId, "creative_tab_" + categoryPath));

        CreativeModeTab tab = CreativeModeTab.builder(CreativeModeTab.Row.BOTTOM, 0)
                .icon(() -> new ItemStack(iconItem))
                .title(Component.translatable("itemGroup." + modId + "." + categoryPath))
                .displayItems((params, output) -> {
                    for (ItemLike extra : extras) {
                        if (extra != null) {
                            output.accept(extra);
                        }
                    }
                })
                .build();

        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, key, tab);
    }
}

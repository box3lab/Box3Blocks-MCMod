package com.box3lab.register.creative;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.world.level.ItemLike;

public final class CreativeTabExtras {
    private static final Map<String, List<ItemLike>> EXTRAS = new HashMap<>();

    private CreativeTabExtras() {
    }

    public static void add(String categoryPath, ItemLike item) {
        if (categoryPath == null || categoryPath.isBlank() || item == null) {
            return;
        }
        EXTRAS.computeIfAbsent(categoryPath, k -> new ArrayList<>()).add(item);
    }

    public static Map<String, List<ItemLike>> extras() {
        return EXTRAS;
    }
}

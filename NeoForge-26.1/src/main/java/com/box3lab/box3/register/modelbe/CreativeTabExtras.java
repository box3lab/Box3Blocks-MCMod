package com.box3lab.box3.register.modelbe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import net.minecraft.world.level.ItemLike;

public final class CreativeTabExtras {
    private static final Map<String, List<Supplier<? extends ItemLike>>> EXTRAS = new HashMap<>();

    private CreativeTabExtras() {
    }

    public static void add(String categoryPath, Supplier<? extends ItemLike> itemSupplier) {
        if (categoryPath == null || categoryPath.isBlank() || itemSupplier == null) {
            return;
        }
        EXTRAS.computeIfAbsent(categoryPath, key -> new ArrayList<>()).add(itemSupplier);
    }

    public static Map<String, List<Supplier<? extends ItemLike>>> extras() {
        return EXTRAS;
    }
}

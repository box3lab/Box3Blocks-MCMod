package com.box3lab.box3js.registries;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.ItemLore;
import org.slf4j.Logger;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Box3JSCustomItems {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<String, Box3JSCustomItemDef> ITEMS = new LinkedHashMap<>();
    private static String baseItemId = "minecraft:paper";

    public static void init(Path gameDir) {
        loadFromPack(gameDir.resolve("resourcepacks/box3js-items/items.json"));
    }

    /** Load custom items from a resource pack's items.json. Called from JS. */
    public static void loadFromPack(Path itemsFile) {
        if (!Files.exists(itemsFile)) {
            LOGGER.warn("Box3JS: Custom item config not found: {}", itemsFile);
            return;
        }

        JsonObject root;
        try (Reader r = Files.newBufferedReader(itemsFile)) {
            root = JsonParser.parseReader(r).getAsJsonObject();
        } catch (Exception e) {
            LOGGER.error("Box3JS: Failed to parse {}: {}", itemsFile, e.getMessage());
            return;
        }

        if (root.has("base_item")) {
            baseItemId = root.get("base_item").getAsString();
        }

        JsonObject itemsObj = root.has("items") ? root.getAsJsonObject("items") : root;
        int loaded = 0;
        for (var entry : itemsObj.entrySet()) {
            String id = entry.getKey();
            if (id.equals("base_item") || !entry.getValue().isJsonObject()) continue;
            Box3JSCustomItemDef def = Box3JSCustomItemDef.fromMcJson(id, entry.getValue().getAsJsonObject());
            ITEMS.put(id, def);
            loaded++;
        }

        LOGGER.info("Box3JS loaded {} custom items from {} (base: {}).", loaded, itemsFile, baseItemId);
    }

    public static Box3JSCustomItemDef get(String id) {
        return ITEMS.get(id);
    }

    public static Collection<String> getIds() {
        return ITEMS.keySet();
    }

    /** Create an ItemStack for the given custom item ID. */
    public static ItemStack createStack(String id, int count) {
        Box3JSCustomItemDef def = ITEMS.get(id);
        if (def == null) return null;

        net.minecraft.world.item.Item baseItem = BuiltInRegistriesShim.getItem(baseItemId);
        if (baseItem == null) {
            LOGGER.error("Box3JS: Base item '{}' not found.", baseItemId);
            return null;
        }

        ItemStack stack = new ItemStack(baseItem, Math.max(1, Math.min(count, def.maxStack)));
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(def.name));
        if (!def.lore.isEmpty()) {
            List<Component> loreComponents = new ArrayList<>();
            for (String line : def.lore) {
                loreComponents.add(Component.literal(line));
            }
            stack.set(DataComponents.LORE, new ItemLore(loreComponents));
        }
        stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(def.modelData));
        if (def.maxStack != 64) {
            stack.set(DataComponents.MAX_STACK_SIZE, def.maxStack);
        }
        if (def.glint) {
            stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        }
        if (def.rarity != null) {
            stack.set(DataComponents.RARITY, def.rarity);
        }
        if (def.food != null) {
            stack.set(DataComponents.FOOD, def.food);
        }

        return stack;
    }

    public static class Box3JSCustomItemDef {
        public final String id;
        public final int modelData;
        public final String name;
        public final List<String> lore;
        public final int maxStack;
        public final boolean glint;
        public final Rarity rarity;
        public final FoodProperties food;

        public Box3JSCustomItemDef(String id, int modelData, String name, List<String> lore,
                                    int maxStack, boolean glint, Rarity rarity, FoodProperties food) {
            this.id = id;
            this.modelData = modelData;
            this.name = name;
            this.lore = lore;
            this.maxStack = maxStack;
            this.glint = glint;
            this.rarity = rarity;
            this.food = food;
        }

        /** Parse from JSON using Minecraft component IDs as keys. */
        public static Box3JSCustomItemDef fromMcJson(String id, JsonObject obj) {
            // minecraft:custom_model_data
            int modelData = getInt(obj, "minecraft:custom_model_data", 0);

            // minecraft:custom_name
            String name = getString(obj, "minecraft:custom_name", id);

            // minecraft:lore
            List<String> lore = new ArrayList<>();
            if (obj.has("minecraft:lore") && obj.get("minecraft:lore").isJsonArray()) {
                for (JsonElement e : obj.getAsJsonArray("minecraft:lore")) {
                    lore.add(e.getAsString());
                }
            }

            // minecraft:max_stack_size
            int maxStack = clamp(getInt(obj, "minecraft:max_stack_size", 64), 1, 64);

            // minecraft:enchantment_glint_override
            boolean glint = getBool(obj, "minecraft:enchantment_glint_override", false);

            // minecraft:rarity
            Rarity rarity = null;
            String rarityStr = getString(obj, "minecraft:rarity", null);
            if (rarityStr != null) {
                try { rarity = Rarity.valueOf(rarityStr.toUpperCase(Locale.ROOT)); } catch (IllegalArgumentException ignored) {}
            }

            // minecraft:food
            FoodProperties food = null;
            if (obj.has("minecraft:food") && obj.get("minecraft:food").isJsonObject()) {
                JsonObject f = obj.getAsJsonObject("minecraft:food");
                int nutrition = clamp(getInt(f, "nutrition", 4), 1, 20);
                float saturation = getFloat(f, "saturation", 0.6f);
                boolean alwaysEdible = getBool(f, "can_always_eat", false);
                float eatSeconds = getFloat(f, "eat_seconds", 1.6f);

                FoodProperties.Builder builder = new FoodProperties.Builder()
                    .nutrition(nutrition)
                    .saturationModifier(saturation);
                if (alwaysEdible) builder.alwaysEdible();
                if (eatSeconds <= 0.8f) builder.fast();
                food = builder.build();
            }

            return new Box3JSCustomItemDef(id, modelData, name, lore, maxStack, glint, rarity, food);
        }

        private static int getInt(JsonObject obj, String key, int def) {
            return obj.has(key) ? obj.get(key).getAsInt() : def;
        }

        private static float getFloat(JsonObject obj, String key, float def) {
            return obj.has(key) ? obj.get(key).getAsFloat() : def;
        }

        private static boolean getBool(JsonObject obj, String key, boolean def) {
            return obj.has(key) ? obj.get(key).getAsBoolean() : def;
        }

        private static String getString(JsonObject obj, String key, String def) {
            return obj.has(key) ? obj.get(key).getAsString() : def;
        }

        private static int clamp(int v, int min, int max) {
            return Math.max(min, Math.min(max, v));
        }
    }

    /** Shim to look up vanilla items without touching DeferredRegister. */
    private static class BuiltInRegistriesShim {
        static net.minecraft.world.item.Item getItem(String id) {
            ResourceLocation rl = ResourceLocation.tryParse(id);
            if (rl == null) return null;
            return net.minecraft.core.registries.BuiltInRegistries.ITEM.getOptional(rl).orElse(null);
        }
    }
}

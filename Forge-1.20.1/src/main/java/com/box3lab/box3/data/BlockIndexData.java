package com.box3lab.box3.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class BlockIndexData {
    public static final class Entry {
        public final String name;
        public final String resourceName;
        public final int id;
        public final String category;
        public final boolean transparent;
        public final boolean fluid;
        public final int emissivePacked;
        public final float hardness;
        public final float resistance;
        public final float friction;

        private Entry(String name, int id, String category, boolean transparent, boolean fluid, int emissivePacked,
                float hardness, float resistance, float friction) {
            this.name = name;
            this.resourceName = name.toLowerCase(Locale.ROOT);
            this.id = id;
            this.category = category == null ? "" : category;
            this.transparent = transparent;
            this.fluid = fluid;
            this.emissivePacked = emissivePacked;
            this.hardness = hardness;
            this.resistance = resistance;
            this.friction = friction;
        }
    }

    public final List<Entry> entries;
    public final Map<String, String> categoryByName;
    public final Set<Integer> notSolidIds;
    public final Map<Integer, Integer> indexById;

    private static volatile BlockIndexData INSTANCE;

    private BlockIndexData(List<Entry> entries, Map<String, String> categoryByName, Set<Integer> notSolidIds,
            Map<Integer, Integer> indexById) {
        this.entries = entries;
        this.categoryByName = categoryByName;
        this.notSolidIds = notSolidIds;
        this.indexById = indexById;
    }

    public static BlockIndexData get() {
        BlockIndexData inst = INSTANCE;
        if (inst != null) {
            return inst;
        }
        synchronized (BlockIndexData.class) {
            if (INSTANCE == null) {
                INSTANCE = load();
            }
            return INSTANCE;
        }
    }

    private static BlockIndexData load() {
        JsonObject root;
        try (InputStream is = BlockIndexData.class.getClassLoader().getResourceAsStream("block-spec.json")) {
            if (is == null) {
                throw new IllegalStateException("Missing resource: block-spec.json");
            }
            root = JsonParser.parseReader(new InputStreamReader(is, StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load block-spec.json", e);
        }

        List<Entry> entries = new ArrayList<>(root.size());
        for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
            String name = entry.getKey();
            JsonObject obj = entry.getValue().getAsJsonObject();

            int id = obj.has("id") ? obj.get("id").getAsInt() : -1;
            if (id < 0) {
                continue;
            }

            String category = obj.has("category") ? obj.get("category").getAsString() : "";
            boolean transparent = obj.has("transparent") && obj.get("transparent").getAsBoolean();
            boolean fluid = obj.has("fluid") && obj.get("fluid").getAsBoolean();

            int emissivePacked = 0;
            if (obj.has("emissive") && obj.get("emissive").isJsonArray()
                    && obj.getAsJsonArray("emissive").size() >= 3) {
                double er = obj.getAsJsonArray("emissive").get(0).getAsDouble();
                double eg = obj.getAsJsonArray("emissive").get(1).getAsDouble();
                double eb = obj.getAsJsonArray("emissive").get(2).getAsDouble();
                double max = Math.max(er, Math.max(eg, eb));
                emissivePacked = (int) Math.round(Math.max(0.0, Math.min(1.0, max / 15.0)) * 4095.0);
            }

            float hardness = 1.0F;
            float resistance = 1.0F;
            if (obj.has("strength") && obj.get("strength").isJsonObject()) {
                JsonObject strength = obj.getAsJsonObject("strength");
                hardness = strength.has("hardness") ? strength.get("hardness").getAsFloat() : hardness;
                resistance = strength.has("resistance") ? strength.get("resistance").getAsFloat() : resistance;
            }

            float friction = obj.has("friction") ? obj.get("friction").getAsFloat() : 1.0F;
            entries.add(new Entry(name, id, category, transparent, fluid, emissivePacked, hardness, resistance, friction));
        }

        entries.sort((left, right) -> Integer.compare(left.id, right.id));

        Map<String, String> categoryByName = new HashMap<>(entries.size() * 2);
        Set<Integer> notSolidIds = new HashSet<>();
        Map<Integer, Integer> indexById = new HashMap<>(entries.size() * 2);

        for (int i = 0; i < entries.size(); i++) {
            Entry entry = entries.get(i);
            categoryByName.put(entry.resourceName, entry.category);
            indexById.put(entry.id, i);
            if (entry.transparent || entry.fluid) {
                notSolidIds.add(entry.id);
            }
        }

        return new BlockIndexData(List.copyOf(entries), Map.copyOf(categoryByName), Set.copyOf(notSolidIds),
                Map.copyOf(indexById));
    }
}

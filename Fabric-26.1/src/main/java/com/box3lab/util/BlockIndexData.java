package com.box3lab.util;

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
import java.util.Map;
import java.util.Locale;
import java.util.Set;

public final class BlockIndexData {
    public static final class FluidInfo {
        public final int id;
        public final int mass;
        public final long info;
        public final double fluidExtinction;

        public FluidInfo(int id, int mass, long info, double fluidExtinction) {
            this.id = id;
            this.mass = mass;
            this.info = info;
            this.fluidExtinction = fluidExtinction;
        }
    }

    public final int[] ids;
    public final String[] names;
    public final int[] emissive;
    public final float[] blockHardness;
    public final float[] blockResistance;
    public final float[] blockFriction;
    public final Map<String, String> categoryByName;
    public final Set<Integer> notSolidIds;
    public final Map<Integer, FluidInfo> fluidsById;

    public final Map<Integer, Integer> indexById;
    public final Map<String, Integer> idByName;

    private BlockIndexData(
            int[] ids,
            String[] names,
            int[] emissive,
            float[] blockHardness,
            float[] blockResistance,
            float[] blockFriction,
            Map<String, String> categoryByName,
            Set<Integer> notSolidIds,
            Map<Integer, FluidInfo> fluidsById) {
        this.ids = ids;
        this.names = names;
        this.emissive = emissive;
        this.blockHardness = blockHardness;
        this.blockResistance = blockResistance;
        this.blockFriction = blockFriction;
        this.categoryByName = categoryByName;
        this.notSolidIds = notSolidIds;
        this.fluidsById = fluidsById;

        Map<Integer, Integer> indexByIdTmp = new HashMap<>(ids.length * 2);
        for (int i = 0; i < ids.length; i++) {
            indexByIdTmp.put(ids[i], i);
        }
        this.indexById = indexByIdTmp;

        Map<String, Integer> idByNameTmp = new HashMap<>(names.length * 2);
        int len = Math.min(ids.length, names.length);
        for (int i = 0; i < len; i++) {
            idByNameTmp.put(names[i], ids[i]);
        }
        this.idByName = idByNameTmp;
    }

    private static volatile BlockIndexData INSTANCE;

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

        final class Entry {
            final String name;
            final int id;
            final int emissive;
            final String category;
            final boolean transparent;
            final boolean fluid;
            final int mass;
            final int fluidR;
            final int fluidG;
            final int fluidB;
            final double fluidExtinction;
            final float hardness;
            final float resistance;
            final float friction;

            Entry(String name, int id, int emissive, String category, boolean transparent, boolean fluid, int mass,
                    int fluidR, int fluidG, int fluidB, double fluidExtinction, float hardness, float resistance,
                    float friction) {
                this.name = name;
                this.id = id;
                this.emissive = emissive;
                this.category = category;
                this.transparent = transparent;
                this.fluid = fluid;
                this.mass = mass;
                this.fluidR = fluidR;
                this.fluidG = fluidG;
                this.fluidB = fluidB;
                this.fluidExtinction = fluidExtinction;
                this.hardness = hardness;
                this.resistance = resistance;
                this.friction = friction;
            }
        }

        List<Entry> entries = new ArrayList<>(root.size());
        for (Map.Entry<String, JsonElement> e : root.entrySet()) {
            String name = e.getKey();
            JsonObject obj = e.getValue().getAsJsonObject();

            int id = obj.has("id") ? obj.get("id").getAsInt() : -1;
            String category = obj.has("category") ? obj.get("category").getAsString() : "";
            boolean transparent = obj.has("transparent") && obj.get("transparent").getAsBoolean();
            boolean fluid = obj.has("fluid") && obj.get("fluid").getAsBoolean();
            int mass = obj.has("mass") ? obj.get("mass").getAsInt() : 0;

            int emissivePacked = 0;
            if (obj.has("emissive") && obj.get("emissive").isJsonArray()
                    && obj.getAsJsonArray("emissive").size() >= 3) {
                double er = obj.getAsJsonArray("emissive").get(0).getAsDouble();
                double eg = obj.getAsJsonArray("emissive").get(1).getAsDouble();
                double eb = obj.getAsJsonArray("emissive").get(2).getAsDouble();
                double max = Math.max(er, Math.max(eg, eb));
                emissivePacked = (int) Math.round(Math.max(0.0, Math.min(1.0, max / 15.0)) * 4095.0);
            }

            int fr = 0, fg = 0, fb = 0;
            if (obj.has("fluidColor") && obj.get("fluidColor").isJsonArray()
                    && obj.getAsJsonArray("fluidColor").size() >= 3) {
                double r = obj.getAsJsonArray("fluidColor").get(0).getAsDouble();
                double g = obj.getAsJsonArray("fluidColor").get(1).getAsDouble();
                double b = obj.getAsJsonArray("fluidColor").get(2).getAsDouble();

                fr = (int) Math.round((r <= 1.0 ? r * 255.0 : r));
                fg = (int) Math.round((g <= 1.0 ? g * 255.0 : g));
                fb = (int) Math.round((b <= 1.0 ? b * 255.0 : b));

                fr = Math.max(0, Math.min(255, fr));
                fg = Math.max(0, Math.min(255, fg));
                fb = Math.max(0, Math.min(255, fb));
            }

            double fluidExtinction = 0.0;
            if (obj.has("fluidExtinction")) {
                fluidExtinction = obj.get("fluidExtinction").getAsDouble();
            }

            // Parse strength values
            float hardness = 1.0f; // default hardness
            float resistance = 1.0f; // default resistance

            if (obj.has("strength") && obj.get("strength").isJsonObject()) {
                JsonObject strengthObj = obj.get("strength").getAsJsonObject();
                hardness = strengthObj.has("hardness") ? strengthObj.get("hardness").getAsFloat() : 1.0f;
                resistance = strengthObj.has("resistance") ? strengthObj.get("resistance").getAsFloat() : 1.0f;
            }

            // Parse friction value (default 1.0 if missing)
            float friction = 1.0f;
            if (obj.has("friction")) {
                friction = obj.get("friction").getAsFloat();
            }

            if (id >= 0) {
                entries.add(new Entry(name, id, emissivePacked, category, transparent, fluid, mass, fr, fg, fb,
                        fluidExtinction, hardness, resistance, friction));
            }
        }

        entries.sort((a, b) -> Integer.compare(a.id, b.id));

        int[] ids = new int[entries.size()];
        String[] names = new String[entries.size()];
        int[] emissive = new int[entries.size()];
        float[] blockHardness = new float[entries.size()];
        float[] blockResistance = new float[entries.size()];
        float[] blockFriction = new float[entries.size()];
        Map<String, String> categoryByName = new HashMap<>(entries.size() * 2);

        Set<Integer> notSolidSet = new HashSet<>();
        Map<Integer, FluidInfo> fluidsById = new HashMap<>();

        for (int i = 0; i < entries.size(); i++) {
            Entry en = entries.get(i);
            ids[i] = en.id;
            names[i] = en.name;
            emissive[i] = en.emissive;
            blockHardness[i] = en.hardness;
            blockResistance[i] = en.resistance;
            blockFriction[i] = en.friction;
            categoryByName.put(en.name.toLowerCase(Locale.ROOT), en.category == null ? "" : en.category);

            if (en.transparent || en.fluid) {
                notSolidSet.add(en.id);
            }
            if (en.fluid) {
                int a = 255;
                long info = (en.fluidR & 255L) | ((en.fluidG & 255L) << 8) | ((en.fluidB & 255L) << 16)
                        | ((a & 255L) << 24);
                fluidsById.put(en.id, new FluidInfo(en.id, en.mass, info, en.fluidExtinction));
            }
        }

        return new BlockIndexData(ids, names, emissive, blockHardness, blockResistance, blockFriction, categoryByName,
                notSolidSet, fluidsById);
    }
}

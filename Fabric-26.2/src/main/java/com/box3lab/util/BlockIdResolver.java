package com.box3lab.util;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.box3lab.register.ModBlocks;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public final class BlockIdResolver {

    private static JsonObject blockIdMapping = null;
    private static Map<String, Integer> blockToIdMapping = null;

    private BlockIdResolver() {
    }

    private static void loadBlockIdMapping() {
        if (blockIdMapping != null) {
            return;
        }

        try (InputStream is = BlockIdResolver.class.getClassLoader().getResourceAsStream("block-id.json")) {
            if (is == null) {
                throw new RuntimeException(Component
                        .translatable("command.box3.block_id.missing_file")
                        .getString());
            }
            try (InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                blockIdMapping = JsonParser.parseReader(reader).getAsJsonObject();
            }
        } catch (Exception e) {
            throw new RuntimeException(
                    Component.translatable("command.box3.block_id.read_failed").getString(),
                    e);
        }
    }

    private static void loadReverseMapping() {
        loadBlockIdMapping();
        if (blockToIdMapping != null) {
            return;
        }

        Map<String, Integer> reverse = new HashMap<>();
        for (var entry : blockIdMapping.entrySet()) {
            String registryKey = entry.getValue().getAsString().toLowerCase(Locale.ROOT);
            reverse.putIfAbsent(registryKey, Integer.parseInt(entry.getKey()));
        }
        blockToIdMapping = reverse;
    }

    public static Block getBlockById(int id) {
        return getBlockById(id, false);
    }

    public static Block getBlockById(int id, boolean ignoreWater) {
        loadBlockIdMapping();

        String idStr = String.valueOf(id);
        if (!blockIdMapping.has(idStr)) {
            System.err.println(Component
                    .translatable("command.box3.block_id.no_mapping_for_id", idStr)
                    .getString());
            return Blocks.STONE;
        }

        // 364: spec_water_block (water)
        // 412-430: spec_*_juice_block, milk, soy_sauce, coffee, peach_juice
        if (id == 364
                || id == 412
                || id == 414
                || id == 416
                || id == 418
                || id == 420
                || id == 422
                || id == 424
                || id == 426
                || id == 428
                || id == 430) {
            if (ignoreWater) {
                return Blocks.AIR;
            }
            return Blocks.WATER;
        }

        String registryKey = blockIdMapping.get(idStr).getAsString();
        String normalizedKey = registryKey.toLowerCase(Locale.ROOT);

        Block block = ModBlocks.BLOCKS.get(normalizedKey);
        if (block == null) {
            System.err.println(Component
                    .translatable("command.box3.block_id.missing_registered_block", registryKey)
                    .getString());
            return Blocks.STONE;
        }

        return block;
    }

    public static boolean isBarrierId(int id) {
        loadBlockIdMapping();

        String idStr = String.valueOf(id);
        if (!blockIdMapping.has(idStr)) {
            return false;
        }
        String registryKey = blockIdMapping.get(idStr).getAsString();
        return "barrier".equalsIgnoreCase(registryKey);
    }

    public static int getIdByBlock(Block block) {
        loadReverseMapping();
        String key = BuiltInRegistries.BLOCK.getKey(block).getPath().toLowerCase(Locale.ROOT);
        Integer id = blockToIdMapping.get(key);
        if (id != null) {
            return id;
        }

        if (block == Blocks.WATER) {
            return 364;
        }

        return 0;
    }
}

package com.box3lab.box3js.script;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Shared JSON storage helpers for server and client storage implementations.
 */
public final class Box3StorageSupport {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new Gson();
    private static final Type MAP_TYPE = new TypeToken<Map<String, Box3StorageTypes.ValueEntry>>() {}.getType();

    private Box3StorageSupport() {}

    public static String sanitize(String value) {
        return value.replaceAll("[^a-zA-Z0-9_.\\-]", "_");
    }

    public static Path resolveStoragePath(Path baseDir, String name) {
        String[] parts = name.split("/");
        Path dir = baseDir;
        for (int i = 0; i < parts.length - 1; i++) {
            String segment = sanitize(parts[i]);
            if (!segment.isEmpty()) {
                dir = dir.resolve(segment);
            }
        }
        String file = sanitize(parts[parts.length - 1]);
        if (file.isEmpty()) {
            file = "default";
        }
        return dir.resolve(file + ".json");
    }

    public static Map<String, Box3StorageTypes.ValueEntry> emptyDataMap() {
        return Collections.synchronizedMap(new LinkedHashMap<>());
    }

    public static Map<String, Box3StorageTypes.ValueEntry> readData(Path path, String label) {
        if (!Files.exists(path)) {
            return emptyDataMap();
        }
        try {
            String json = Files.readString(path);
            Map<String, Box3StorageTypes.ValueEntry> map = GSON.fromJson(json, MAP_TYPE);
            return map != null ? Collections.synchronizedMap(new LinkedHashMap<>(map)) : emptyDataMap();
        } catch (IOException e) {
            LOGGER.warn("Failed to read {} storage file: {}", label, path, e);
            return emptyDataMap();
        }
    }

    public static void writeData(Path path, Map<String, Box3StorageTypes.ValueEntry> data, String label) {
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, GSON.toJson(data));
        } catch (IOException e) {
            LOGGER.warn("Failed to persist {} storage file: {}", label, path, e);
        }
    }

    public static void deleteData(Path path, String label) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            LOGGER.warn("Failed to delete {} storage file: {}", label, path, e);
        }
    }
}

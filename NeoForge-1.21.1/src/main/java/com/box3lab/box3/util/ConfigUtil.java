package com.box3lab.box3.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

import static com.box3lab.box3.Box3Blocks.MODID;

public final class ConfigUtil {
    public static final String CONFIG_DIR_NAME = "config.json";

    private ConfigUtil() {
    }

    public static JsonObject readConfig(String fileName) {
        try {
            Path dir = FMLPaths.CONFIGDIR.get().resolve(MODID);
            Path path = dir.resolve(fileName);
            if (!Files.exists(path)) {
                return null;
            }

            try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                return JsonParser.parseReader(reader).getAsJsonObject();
            }
        } catch (IOException | JsonParseException e) {
            return null;
        }
    }

    public static void writeConfig(String fileName, JsonObject obj) {
        if (obj == null) {
            return;
        }

        try {
            Path dir = FMLPaths.CONFIGDIR.get().resolve(MODID);
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
            Path path = dir.resolve(fileName);

            try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                writer.write(obj.toString());
            }
        } catch (IOException ignored) {
        }
    }

    public static JsonObject readOrCreateConfig(String fileName) {
        JsonObject obj = readConfig(fileName);
        return obj != null ? obj : new JsonObject();
    }

    public static void updateConfig(String fileName, Consumer<JsonObject> updater) {
        if (updater == null) {
            return;
        }

        JsonObject obj = readOrCreateConfig(fileName);
        updater.accept(obj);
        writeConfig(fileName, obj);
    }
}

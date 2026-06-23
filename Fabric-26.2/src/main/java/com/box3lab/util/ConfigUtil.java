package com.box3lab.util;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import net.fabricmc.loader.api.FabricLoader;

import static com.box3lab.Box3.MOD_ID;

public final class ConfigUtil {

    public static final String CONFIG_DIR_NAME = "config.json";

    private ConfigUtil() {
    }

    public static JsonObject readConfig(String fileName) {
        try {
            Path dir = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID);
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
            Path dir = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID);
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
            Path path = dir.resolve(fileName);

            try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                writer.write(obj.toString());
            }
        } catch (IOException e) {
        }
    }
}

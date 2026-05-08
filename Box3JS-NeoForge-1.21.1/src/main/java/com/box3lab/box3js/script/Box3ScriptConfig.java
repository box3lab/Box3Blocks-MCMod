package com.box3lab.box3js.script;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import net.minecraft.server.MinecraftServer;

public class Box3ScriptConfig {

    private static final Gson GSON = new Gson();
    private static final Type MAP_TYPE = new TypeToken<Map<String, Boolean>>() {
    }.getType();

    private static Box3ScriptConfig INSTANCE;

    private Path configFile;
    private Map<String, Boolean> projects = new LinkedHashMap<>();

    public static Box3ScriptConfig get() {
        if (INSTANCE == null)
            INSTANCE = new Box3ScriptConfig();
        return INSTANCE;
    }

    private Box3ScriptConfig() {
    }

    /** Load config from disk. Call once when server starts. */
    public void load(MinecraftServer server) {
        configFile = server.getServerDirectory().resolve("config/box3/scripts.json");
        if (Files.exists(configFile)) {
            try {
                String json = Files.readString(configFile);
                Map<String, Boolean> loaded = GSON.fromJson(json, MAP_TYPE);
                if (loaded != null)
                    projects = new LinkedHashMap<>(loaded);
            } catch (IOException ignored) {
            }
        }
    }

    /** Save config to disk. */
    private void save() {
        if (configFile == null)
            return;
        try {
            Files.createDirectories(configFile.getParent());
            Files.writeString(configFile, GSON.toJson(projects));
        } catch (IOException ignored) {
        }
    }

    public boolean isEnabled(String projectName) {
        return Boolean.TRUE.equals(projects.get(projectName));
    }

    public void setEnabled(String projectName, boolean enabled) {
        projects.put(projectName, enabled);
        save();
    }

    public void setAllEnabled(boolean enabled) {
        projects.replaceAll((k, v) -> enabled);
        save();
    }

    public Map<String, Boolean> listProjects() {
        return new LinkedHashMap<>(projects);
    }

    /** Scan script directory for new projects, add them as disabled by default. */
    public void discover(MinecraftServer server) {
        Path scriptDir = getScriptDir(server);
        if (!Files.exists(scriptDir))
            return;
        try (var dirs = Files.list(scriptDir)) {
            dirs.filter(Files::isDirectory).forEach(dir -> {
                Path distAppJs = dir.resolve("dist/app.js");
                Path legacyAppJs = dir.resolve("app.js");
                if (Files.exists(distAppJs) || Files.exists(legacyAppJs)) {
                    String name = dir.getFileName().toString();
                    projects.putIfAbsent(name, false);
                }
            });
        } catch (IOException ignored) {
        }
        save();
    }

    public Path getScriptDir(MinecraftServer server) {
        return server.getServerDirectory().resolve("config/box3/script");
    }
}

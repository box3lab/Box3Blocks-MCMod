package com.box3lab.box3.config;

import com.box3lab.box3.util.ConfigUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public final class Box3Config {
    private static final String KEY_REQUIRE_OP_FOR_COMMANDS = "requireOpForCommands";

    private static volatile int commandPermissionLevel = 0;

    private Box3Config() {
    }

    public static void load() {
        int level = 0;
        JsonObject obj = ConfigUtil.readConfig(ConfigUtil.CONFIG_DIR_NAME);
        if (obj != null && obj.has(KEY_REQUIRE_OP_FOR_COMMANDS)) {
            JsonElement element = obj.get(KEY_REQUIRE_OP_FOR_COMMANDS);
            if (element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
                level = element.getAsInt();
            }
        }
        commandPermissionLevel = clampPermissionLevel(level);
    }

    public static int commandPermissionLevel() {
        return commandPermissionLevel;
    }

    public static void setCommandPermissionLevel(int level) {
        int clamped = clampPermissionLevel(level);
        commandPermissionLevel = clamped;
        ConfigUtil.updateConfig(ConfigUtil.CONFIG_DIR_NAME,
                json -> json.addProperty(KEY_REQUIRE_OP_FOR_COMMANDS, clamped));
    }

    private static int clampPermissionLevel(int level) {
        if (level < 0) {
            return 0;
        }
        if (level > 4) {
            return 4;
        }
        return level;
    }
}

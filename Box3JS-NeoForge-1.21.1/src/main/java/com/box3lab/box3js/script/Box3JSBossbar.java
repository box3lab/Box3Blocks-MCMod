package com.box3lab.box3js.script;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent.BossBarColor;
import net.minecraft.world.BossEvent.BossBarOverlay;
import net.minecraft.server.level.ServerBossEvent;

import java.util.*;

class Box3JSBossbar {

    private final MinecraftServer server;
    private final Map<String, Map<String, ServerBossEvent>> projectBossBars = new HashMap<>();

    Box3JSBossbar(MinecraftServer server) {
        this.server = server;
    }

    void showBossbar(String project, String name, String text, double progress, String colorName) {
        Map<String, ServerBossEvent> bars = projectBossBars.computeIfAbsent(project, k -> new HashMap<>());
        ServerBossEvent bar = bars.get(name);
        if (bar == null) {
            bar = new ServerBossEvent(Component.literal(text), resolveColor(colorName), BossBarOverlay.PROGRESS);
            bars.put(name, bar);
        } else {
            bar.setName(Component.literal(text));
            if (colorName != null) bar.setColor(resolveColor(colorName));
        }
        bar.setProgress((float) Math.max(0, Math.min(1, progress)));
        for (ServerPlayer sp : server.getPlayerList().getPlayers()) bar.addPlayer(sp);
    }

    void removeBossbar(String project, String name) {
        Map<String, ServerBossEvent> bars = projectBossBars.get(project);
        if (bars == null) return;
        ServerBossEvent bar = bars.remove(name);
        if (bar != null) bar.removeAllPlayers();
    }

    void removeProject(String project) {
        Map<String, ServerBossEvent> bars = projectBossBars.remove(project);
        if (bars != null) {
            for (ServerBossEvent bar : bars.values()) bar.removeAllPlayers();
        }
    }

    void resetAll() {
        for (Map<String, ServerBossEvent> bars : projectBossBars.values()) {
            for (ServerBossEvent bar : bars.values()) bar.removeAllPlayers();
        }
        projectBossBars.clear();
    }

    private static BossBarColor resolveColor(String colorName) {
        if (colorName == null) return BossBarColor.WHITE;
        return switch (colorName.toLowerCase(Locale.ROOT)) {
            case "red" -> BossBarColor.RED;
            case "blue" -> BossBarColor.BLUE;
            case "green" -> BossBarColor.GREEN;
            case "yellow" -> BossBarColor.YELLOW;
            case "purple" -> BossBarColor.PURPLE;
            case "pink" -> BossBarColor.PINK;
            default -> BossBarColor.WHITE;
        };
    }
}

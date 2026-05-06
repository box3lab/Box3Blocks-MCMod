package com.box3lab.box3js.script;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent.BossBarColor;
import net.minecraft.world.BossEvent.BossBarOverlay;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerBossEvent;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

class Box3JSBossbar {

    private final MinecraftServer server;
    private final Map<String, ServerBossEvent> bossBars = new HashMap<>();

    Box3JSBossbar(MinecraftServer server) {
        this.server = server;
    }

    void showBossbar(String name, String text, double progress, String colorName) {
        ServerBossEvent bar = bossBars.get(name);
        if (bar == null) {
            bar = new ServerBossEvent(Component.literal(text), resolveColor(colorName), BossBarOverlay.PROGRESS);
            bossBars.put(name, bar);
        } else {
            bar.setName(Component.literal(text));
            if (colorName != null) bar.setColor(resolveColor(colorName));
        }
        bar.setProgress((float) Math.max(0, Math.min(1, progress)));
        for (ServerPlayer sp : server.getPlayerList().getPlayers()) bar.addPlayer(sp);
    }

    void removeBossbar(String name) {
        ServerBossEvent bar = bossBars.remove(name);
        if (bar != null) bar.removeAllPlayers();
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

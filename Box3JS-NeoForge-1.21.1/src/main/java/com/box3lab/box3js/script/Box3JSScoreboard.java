package com.box3lab.box3js.script;

import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.ScriptableObject;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ScoreAccess;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

import java.util.ArrayList;
import java.util.List;

class Box3JSScoreboard {

    private final MinecraftServer server;

    Box3JSScoreboard(MinecraftServer server) {
        this.server = server;
    }

    void addScoreboard(String name) { addScoreboard(name, "dummy"); }

    void addScoreboard(String name, String criteria) {
        Scoreboard sb = server.getScoreboard();
        if (sb.getObjective(name) != null) return;
        ObjectiveCriteria crit = "dummy".equals(criteria) || criteria == null
            ? ObjectiveCriteria.DUMMY
            : ObjectiveCriteria.byName(criteria).orElse(ObjectiveCriteria.DUMMY);
        sb.addObjective(name, crit, Component.literal(name), ObjectiveCriteria.RenderType.INTEGER, false, null);
    }

    void removeScoreboard(String name) {
        Scoreboard sb = server.getScoreboard();
        Objective obj = sb.getObjective(name);
        if (obj != null) sb.removeObjective(obj);
    }

    void setScore(Object entityOrName, String objectiveName, int value) {
        Scoreboard sb = server.getScoreboard();
        Objective obj = sb.getObjective(objectiveName);
        if (obj == null) return;
        String name = Box3ScriptUtils.resolveScoreName(entityOrName);
        if (name == null) return;
        sb.getOrCreatePlayerScore(ScoreHolder.forNameOnly(name), obj).set(value);
    }

    int getScore(Object entityOrName, String objectiveName) {
        Scoreboard sb = server.getScoreboard();
        Objective obj = sb.getObjective(objectiveName);
        if (obj == null) return 0;
        String name = Box3ScriptUtils.resolveScoreName(entityOrName);
        if (name == null) return 0;
        ScoreAccess access = sb.getOrCreatePlayerScore(ScoreHolder.forNameOnly(name), obj);
        return access.get();
    }

    void showScoreboard(String slot, String objectiveName) {
        Scoreboard sb = server.getScoreboard();
        DisplaySlot displaySlot = parseSlot(slot);
        Objective obj = sb.getObjective(objectiveName);
        sb.setDisplayObjective(displaySlot, obj);
    }

    void hideScoreboard(String slot) {
        Scoreboard sb = server.getScoreboard();
        sb.setDisplayObjective(parseSlot(slot), null);
    }

    List<NativeObject> listScores(String objectiveName) {
        List<NativeObject> result = new ArrayList<>();
        Scoreboard sb = server.getScoreboard();
        Objective obj = sb.getObjective(objectiveName);
        if (obj == null) return result;
        for (var entry : sb.listPlayerScores(obj)) {
            NativeObject m = new NativeObject();
            ScriptableObject.putProperty(m, "name", entry.owner());
            ScriptableObject.putProperty(m, "score", entry.value());
            result.add(m);
        }
        return result;
    }

    private static DisplaySlot parseSlot(String slot) {
        return switch (slot.toLowerCase()) {
            case "list" -> DisplaySlot.LIST;
            case "belowname", "below_name" -> DisplaySlot.BELOW_NAME;
            default -> DisplaySlot.SIDEBAR;
        };
    }
}

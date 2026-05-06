package com.box3lab.box3js.script;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;

import java.util.*;

class Box3JSTeam {

    private final MinecraftServer server;
    private final Map<String, Set<String>> projectTeams = new HashMap<>();

    Box3JSTeam(MinecraftServer server) {
        this.server = server;
    }

    void createTeam(String project, String name, String colorName) {
        Scoreboard sb = server.getScoreboard();
        if (sb.getPlayerTeam(name) != null) return;
        PlayerTeam team = sb.addPlayerTeam(name);
        ChatFormatting fmt = ChatFormatting.getByName(colorName);
        if (fmt != null) {
            team.setColor(fmt);
            team.setDisplayName(Component.literal(name));
        }
        projectTeams.computeIfAbsent(project, k -> new HashSet<>()).add(name);
    }

    void removeTeam(String name) {
        Scoreboard sb = server.getScoreboard();
        PlayerTeam team = sb.getPlayerTeam(name);
        if (team != null) {
            sb.removePlayerTeam(team);
            for (Set<String> set : projectTeams.values()) set.remove(name);
        }
    }

    void joinTeam(Object entityOrName, String teamName) {
        Scoreboard sb = server.getScoreboard();
        PlayerTeam team = sb.getPlayerTeam(teamName);
        if (team == null) return;
        String name = Box3ScriptUtils.resolveScoreName(entityOrName);
        if (name != null) sb.addPlayerToTeam(name, team);
    }

    void leaveTeam(Object entityOrName) {
        Scoreboard sb = server.getScoreboard();
        String name = Box3ScriptUtils.resolveScoreName(entityOrName);
        if (name != null) sb.removePlayerFromTeam(name);
    }

    String getTeamOf(Object entityOrName) {
        Scoreboard sb = server.getScoreboard();
        String name = Box3ScriptUtils.resolveScoreName(entityOrName);
        if (name == null) return null;
        PlayerTeam team = sb.getPlayersTeam(name);
        return team != null ? team.getName() : null;
    }

    void removeProject(String project) {
        Set<String> teams = projectTeams.remove(project);
        if (teams != null) {
            Scoreboard sb = server.getScoreboard();
            for (String name : teams) {
                PlayerTeam team = sb.getPlayerTeam(name);
                if (team != null) sb.removePlayerTeam(team);
            }
        }
    }

    void resetAll() {
        Scoreboard sb = server.getScoreboard();
        for (Set<String> teams : projectTeams.values()) {
            for (String name : teams) {
                PlayerTeam team = sb.getPlayerTeam(name);
                if (team != null) sb.removePlayerTeam(team);
            }
        }
        projectTeams.clear();
    }
}

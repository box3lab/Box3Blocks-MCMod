package com.box3lab.box3js.script;

import net.minecraft.core.BlockPos;
import org.mozilla.javascript.Function;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

class Box3JSEventBus {

    // Core callback lists — per-project keyed
    final Map<String, List<Runnable>> tickCallbacks = new ConcurrentHashMap<>();
    final Map<String, List<PlayerJoinCallback>> joinCallbacks = new ConcurrentHashMap<>();
    final Map<String, List<PlayerLeaveCallback>> leaveCallbacks = new ConcurrentHashMap<>();
    final Map<String, List<VoxelDestroyCallback>> voxelDestroyCallbacks = new ConcurrentHashMap<>();
    final Map<String, List<VoxelContactCallback>> voxelContactCallbacks = new ConcurrentHashMap<>();
    final Map<String, List<InteractCallback>> interactCallbacks = new ConcurrentHashMap<>();
    final Map<String, List<ChatCallback>> chatCallbacks = new ConcurrentHashMap<>();
    final Map<String, List<FluidEnterCallback>> fluidEnterCallbacks = new ConcurrentHashMap<>();
    final Map<String, List<FluidLeaveCallback>> fluidLeaveCallbacks = new ConcurrentHashMap<>();
    final Map<String, List<EntityContactCallback>> entityContactCallbacks = new ConcurrentHashMap<>();
    final Map<String, List<EntitySeparateCallback>> entitySeparateCallbacks = new ConcurrentHashMap<>();
    final Map<String, List<BlockPlaceCallback>> blockPlaceCallbacks = new ConcurrentHashMap<>();
    final Map<String, List<EntityDeathCallback>> entityDeathCallbacks = new ConcurrentHashMap<>();
    final Map<String, List<PlayerRespawnCallback>> respawnCallbacks = new ConcurrentHashMap<>();
    final Map<String, List<BlockActivateCallback>> blockActivateCallbacks = new ConcurrentHashMap<>();
    final Map<String, List<EntityDamageCallback>> entityDamageCallbacks = new ConcurrentHashMap<>();
    final Map<String, List<MessageCallback>> messageCallbacks = new ConcurrentHashMap<>();

    // Tracking state — per-project
    final Map<String, Map<UUID, BlockPos>> voxelContactTracked = new ConcurrentHashMap<>();
    final Map<String, Map<UUID, String>> fluidStateTracked = new ConcurrentHashMap<>();
    final Map<String, Set<String>> entityContactPairs = new ConcurrentHashMap<>();
    final Map<String, Map<UUID, Function>> playerChatHandlers = new ConcurrentHashMap<>();
    final Map<UUID, Map<String, Object>> entityCustomProps = new HashMap<>();
    final Map<String, List<Box3ScriptEngine.TimerEntry>> timers = new ConcurrentHashMap<>();
    final Map<String, Integer> timerIdCounters = new ConcurrentHashMap<>();

    // Per-project helpers
    Map<UUID, BlockPos> voxelContactFor(String project) {
        return voxelContactTracked.computeIfAbsent(project, k -> new ConcurrentHashMap<>());
    }
    Map<UUID, String> fluidStateFor(String project) {
        return fluidStateTracked.computeIfAbsent(project, k -> new ConcurrentHashMap<>());
    }
    Set<String> contactPairsFor(String project) {
        return entityContactPairs.computeIfAbsent(project, k -> ConcurrentHashMap.newKeySet());
    }
    Map<UUID, Function> chatHandlersFor(String project) {
        return playerChatHandlers.computeIfAbsent(project, k -> new ConcurrentHashMap<>());
    }
    List<Box3ScriptEngine.TimerEntry> timersFor(String project) {
        return timers.computeIfAbsent(project, k -> new ArrayList<>());
    }
    int nextTimerId(String project) {
        return timerIdCounters.merge(project, 1, Integer::sum);
    }

    // ---- Add callbacks ----

    void addTick(String project, Runnable cb) { add(tickCallbacks, project, cb); }
    void addJoin(String project, PlayerJoinCallback cb) { add(joinCallbacks, project, cb); }
    void addLeave(String project, PlayerLeaveCallback cb) { add(leaveCallbacks, project, cb); }
    void addVoxelDestroy(String project, VoxelDestroyCallback cb) { add(voxelDestroyCallbacks, project, cb); }
    void addVoxelContact(String project, VoxelContactCallback cb) { add(voxelContactCallbacks, project, cb); }
    void addInteract(String project, InteractCallback cb) { add(interactCallbacks, project, cb); }
    void addChat(String project, ChatCallback cb) { add(chatCallbacks, project, cb); }
    void addFluidEnter(String project, FluidEnterCallback cb) { add(fluidEnterCallbacks, project, cb); }
    void addFluidLeave(String project, FluidLeaveCallback cb) { add(fluidLeaveCallbacks, project, cb); }
    void addEntityContact(String project, EntityContactCallback cb) { add(entityContactCallbacks, project, cb); }
    void addEntitySeparate(String project, EntitySeparateCallback cb) { add(entitySeparateCallbacks, project, cb); }
    void addBlockPlace(String project, BlockPlaceCallback cb) { add(blockPlaceCallbacks, project, cb); }
    void addEntityDeath(String project, EntityDeathCallback cb) { add(entityDeathCallbacks, project, cb); }
    void addRespawn(String project, PlayerRespawnCallback cb) { add(respawnCallbacks, project, cb); }
    void addBlockActivate(String project, BlockActivateCallback cb) { add(blockActivateCallbacks, project, cb); }
    void addEntityDamage(String project, EntityDamageCallback cb) { add(entityDamageCallbacks, project, cb); }
    void addMessage(String project, MessageCallback cb) { add(messageCallbacks, project, cb); }

    private static <T> void add(Map<String, List<T>> map, String project, T cb) {
        map.computeIfAbsent(project, k -> new CopyOnWriteArrayList<>()).add(cb);
    }

    // ---- Remove one project ----

    void removeProject(String project) {
        tickCallbacks.remove(project);
        joinCallbacks.remove(project);
        leaveCallbacks.remove(project);
        voxelDestroyCallbacks.remove(project);
        voxelContactCallbacks.remove(project);
        interactCallbacks.remove(project);
        chatCallbacks.remove(project);
        fluidEnterCallbacks.remove(project);
        fluidLeaveCallbacks.remove(project);
        entityContactCallbacks.remove(project);
        entitySeparateCallbacks.remove(project);
        blockPlaceCallbacks.remove(project);
        entityDeathCallbacks.remove(project);
        respawnCallbacks.remove(project);
        blockActivateCallbacks.remove(project);
        entityDamageCallbacks.remove(project);
        messageCallbacks.remove(project);
        voxelContactTracked.remove(project);
        fluidStateTracked.remove(project);
        entityContactPairs.remove(project);
        playerChatHandlers.remove(project);
        timers.remove(project);
        timerIdCounters.remove(project);
    }

    // ---- Clear all ----

    void clearAll() {
        tickCallbacks.clear();
        joinCallbacks.clear();
        leaveCallbacks.clear();
        voxelDestroyCallbacks.clear();
        voxelContactCallbacks.clear();
        interactCallbacks.clear();
        chatCallbacks.clear();
        fluidEnterCallbacks.clear();
        fluidLeaveCallbacks.clear();
        entityContactCallbacks.clear();
        entitySeparateCallbacks.clear();
        blockPlaceCallbacks.clear();
        entityDeathCallbacks.clear();
        respawnCallbacks.clear();
        blockActivateCallbacks.clear();
        entityDamageCallbacks.clear();
        messageCallbacks.clear();
        voxelContactTracked.clear();
        fluidStateTracked.clear();
        entityContactPairs.clear();
        playerChatHandlers.clear();
        entityCustomProps.clear();
        timers.clear();
        timerIdCounters.clear();
    }
}

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
    final Map<String, List<ButtonPressedCallback>> buttonPressedCallbacks = new ConcurrentHashMap<>();
    final Map<String, List<MessageCallback>> messageCallbacks = new ConcurrentHashMap<>();

    // Tracking state — per-project
    final Map<String, Map<UUID, BlockPos>> voxelContactTracked = new ConcurrentHashMap<>();
    final Map<String, Map<UUID, String>> fluidStateTracked = new ConcurrentHashMap<>();
    final Map<String, Set<String>> entityContactPairs = new ConcurrentHashMap<>();
    final Map<String, Map<UUID, Function>> playerChatHandlers = new ConcurrentHashMap<>();
    final Map<UUID, Set<String>> previousButtonStates = new ConcurrentHashMap<>();
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

    // ---- Add callbacks (return the stored wrapper for later removal) ----

    Runnable addTick(String project, Runnable cb) { return add(tickCallbacks, project, cb); }
    PlayerJoinCallback addJoin(String project, PlayerJoinCallback cb) { return add(joinCallbacks, project, cb); }
    PlayerLeaveCallback addLeave(String project, PlayerLeaveCallback cb) { return add(leaveCallbacks, project, cb); }
    VoxelDestroyCallback addVoxelDestroy(String project, VoxelDestroyCallback cb) { return add(voxelDestroyCallbacks, project, cb); }
    VoxelContactCallback addVoxelContact(String project, VoxelContactCallback cb) { return add(voxelContactCallbacks, project, cb); }
    InteractCallback addInteract(String project, InteractCallback cb) { return add(interactCallbacks, project, cb); }
    ChatCallback addChat(String project, ChatCallback cb) { return add(chatCallbacks, project, cb); }
    FluidEnterCallback addFluidEnter(String project, FluidEnterCallback cb) { return add(fluidEnterCallbacks, project, cb); }
    FluidLeaveCallback addFluidLeave(String project, FluidLeaveCallback cb) { return add(fluidLeaveCallbacks, project, cb); }
    EntityContactCallback addEntityContact(String project, EntityContactCallback cb) { return add(entityContactCallbacks, project, cb); }
    EntitySeparateCallback addEntitySeparate(String project, EntitySeparateCallback cb) { return add(entitySeparateCallbacks, project, cb); }
    BlockPlaceCallback addBlockPlace(String project, BlockPlaceCallback cb) { return add(blockPlaceCallbacks, project, cb); }
    EntityDeathCallback addEntityDeath(String project, EntityDeathCallback cb) { return add(entityDeathCallbacks, project, cb); }
    PlayerRespawnCallback addRespawn(String project, PlayerRespawnCallback cb) { return add(respawnCallbacks, project, cb); }
    BlockActivateCallback addBlockActivate(String project, BlockActivateCallback cb) { return add(blockActivateCallbacks, project, cb); }
    EntityDamageCallback addEntityDamage(String project, EntityDamageCallback cb) { return add(entityDamageCallbacks, project, cb); }
    ButtonPressedCallback addButtonPressed(String project, ButtonPressedCallback cb) { return add(buttonPressedCallbacks, project, cb); }
    MessageCallback addMessage(String project, MessageCallback cb) { return add(messageCallbacks, project, cb); }

    private static <T> T add(Map<String, List<T>> map, String project, T cb) {
        map.computeIfAbsent(project, k -> new CopyOnWriteArrayList<>()).add(cb);
        return cb;
    }

    // ---- Remove single callbacks ----

    void removeTick(String project, Runnable cb) { remove(tickCallbacks, project, cb); }
    void removeJoin(String project, PlayerJoinCallback cb) { remove(joinCallbacks, project, cb); }
    void removeLeave(String project, PlayerLeaveCallback cb) { remove(leaveCallbacks, project, cb); }
    void removeVoxelDestroy(String project, VoxelDestroyCallback cb) { remove(voxelDestroyCallbacks, project, cb); }
    void removeVoxelContact(String project, VoxelContactCallback cb) { remove(voxelContactCallbacks, project, cb); }
    void removeInteract(String project, InteractCallback cb) { remove(interactCallbacks, project, cb); }
    void removeChat(String project, ChatCallback cb) { remove(chatCallbacks, project, cb); }
    void removeFluidEnter(String project, FluidEnterCallback cb) { remove(fluidEnterCallbacks, project, cb); }
    void removeFluidLeave(String project, FluidLeaveCallback cb) { remove(fluidLeaveCallbacks, project, cb); }
    void removeEntityContact(String project, EntityContactCallback cb) { remove(entityContactCallbacks, project, cb); }
    void removeEntitySeparate(String project, EntitySeparateCallback cb) { remove(entitySeparateCallbacks, project, cb); }
    void removeBlockPlace(String project, BlockPlaceCallback cb) { remove(blockPlaceCallbacks, project, cb); }
    void removeEntityDeath(String project, EntityDeathCallback cb) { remove(entityDeathCallbacks, project, cb); }
    void removeRespawn(String project, PlayerRespawnCallback cb) { remove(respawnCallbacks, project, cb); }
    void removeBlockActivate(String project, BlockActivateCallback cb) { remove(blockActivateCallbacks, project, cb); }
    void removeEntityDamage(String project, EntityDamageCallback cb) { remove(entityDamageCallbacks, project, cb); }
    void removeButtonPressed(String project, ButtonPressedCallback cb) { remove(buttonPressedCallbacks, project, cb); }
    void removeMessage(String project, MessageCallback cb) { remove(messageCallbacks, project, cb); }

    private static <T> void remove(Map<String, List<T>> map, String project, T cb) {
        List<T> list = map.get(project);
        if (list != null) list.remove(cb);
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
        buttonPressedCallbacks.remove(project);
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
        buttonPressedCallbacks.clear();
        messageCallbacks.clear();
        previousButtonStates.clear();
        voxelContactTracked.clear();
        fluidStateTracked.clear();
        entityContactPairs.clear();
        playerChatHandlers.clear();
        entityCustomProps.clear();
        timers.clear();
        timerIdCounters.clear();
    }
}

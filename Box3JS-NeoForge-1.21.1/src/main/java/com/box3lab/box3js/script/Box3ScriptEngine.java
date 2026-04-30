package com.box3lab.box3js.script;

import com.box3lab.box3js.Box3JS;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import org.mozilla.javascript.*;
import org.mozilla.javascript.commonjs.module.ModuleScriptProvider;
import org.mozilla.javascript.commonjs.module.Require;
import org.mozilla.javascript.commonjs.module.RequireBuilder;
import org.mozilla.javascript.commonjs.module.provider.StrongCachingModuleScriptProvider;
import org.mozilla.javascript.commonjs.module.provider.UrlModuleSourceProvider;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class Box3ScriptEngine {

    private static final Box3ScriptEngine INSTANCE = new Box3ScriptEngine();

    private ScriptableObject scope;
    private Box3JSWorld worldBinding;
    private Box3JSVoxels voxelsBinding;
    private Box3JSStorage storageBinding;
    private MinecraftServer server;
    private boolean initialized;

    private final List<Runnable> tickCallbacks = new CopyOnWriteArrayList<>();
    private final List<Box3JSWorld.PlayerJoinCallback> joinCallbacks = new CopyOnWriteArrayList<>();
    private final List<Box3JSWorld.PlayerLeaveCallback> leaveCallbacks = new CopyOnWriteArrayList<>();
    private final List<Box3JSWorld.VoxelDestroyCallback> voxelDestroyCallbacks = new CopyOnWriteArrayList<>();
    private final List<Box3JSWorld.VoxelContactCallback> voxelContactCallbacks = new CopyOnWriteArrayList<>();
    private final List<Box3JSWorld.InteractCallback> interactCallbacks = new CopyOnWriteArrayList<>();
    private final List<Box3JSWorld.ChatCallback> chatCallbacks = new CopyOnWriteArrayList<>();
    private final List<Box3JSWorld.FluidEnterCallback> fluidEnterCallbacks = new CopyOnWriteArrayList<>();
    private final List<Box3JSWorld.FluidLeaveCallback> fluidLeaveCallbacks = new CopyOnWriteArrayList<>();
    private final List<Box3JSWorld.EntityContactCallback> entityContactCallbacks = new CopyOnWriteArrayList<>();
    private final List<Box3JSWorld.EntitySeparateCallback> entitySeparateCallbacks = new CopyOnWriteArrayList<>();
    private final List<Box3JSWorld.BlockPlaceCallback> blockPlaceCallbacks = new CopyOnWriteArrayList<>();
    private final List<Box3JSWorld.EntityDeathCallback> entityDeathCallbacks = new CopyOnWriteArrayList<>();
    private final List<Box3JSWorld.PlayerRespawnCallback> respawnCallbacks = new CopyOnWriteArrayList<>();
    private final List<Box3JSWorld.BlockActivateCallback> blockActivateCallbacks = new CopyOnWriteArrayList<>();
    private final List<Box3JSWorld.EntityDamageCallback> entityDamageCallbacks = new CopyOnWriteArrayList<>();
    private final Map<String, List<Box3JSWorld.MessageCallback>> messageCallbacks = new ConcurrentHashMap<>();
    private String currentProject;
    private final Map<UUID, BlockPos> voxelContactTracked = new ConcurrentHashMap<>();
    private final Map<UUID, String> fluidStateTracked = new ConcurrentHashMap<>();
    private final Set<String> entityContactPairs = ConcurrentHashMap.newKeySet();
    private final Map<UUID, Function> playerChatHandlers = new ConcurrentHashMap<>();
    private final Map<UUID, Map<String, Object>> entityCustomProps = new HashMap<>();
    private final Map<String, Require> projectRequires = new HashMap<>();
    private final List<TimerEntry> timers = new ArrayList<>();
    private int timerIdCounter;

    public static Box3ScriptEngine get() {
        return INSTANCE;
    }

    public void init(MinecraftServer server) {
        if (initialized) return;
        this.server = server;
        this.worldBinding = new Box3JSWorld(server, this);
        this.voxelsBinding = new Box3JSVoxels(server);
        this.storageBinding = new Box3JSStorage(server.getServerDirectory().resolve("config"), this);
        setupScope();
        initialized = true;
    }

    /** Execute app.js for enabled projects under config/box3/script/ */
    public void autoLoad(MinecraftServer server) {
        init(server);
        Box3ScriptConfig config = Box3ScriptConfig.get();
        config.load(server);
        config.discover(server);

        Path scriptDir = config.getScriptDir(server);
        if (!Files.exists(scriptDir)) return;
        try (var dirs = Files.list(scriptDir)) {
            dirs.filter(Files::isDirectory)
                .sorted()
                .forEach(project -> {
                    String name = project.getFileName().toString();
                    Path appJs = project.resolve("dist/app.js");
                    if (!Files.exists(appJs)) {
                        appJs = project.resolve("app.js");
                    }
                    if (Files.exists(appJs) && config.isEnabled(name)) {
                        try {
                            setCurrentProject(name);
                            eval("require('./app')");
                            Box3JS.LOGGER.info("Auto-loaded project: {}", name);
                        } catch (Exception e) {
                            Box3JS.LOGGER.error("Failed to auto-load: {}", appJs, e);
                        } finally {
                            setCurrentProject(null);
                        }
                    }
                });
        } catch (IOException ignored) {}
    }

    public Object eval(String code) {
        if (!initialized) throw new IllegalStateException("ScriptEngine not initialized");
        Context cx = Context.enter();
        try {
            return cx.evaluateString(scope, code, "script", 1, null);
        } finally {
            Context.exit();
        }
    }

    /** Tick callback from Box3JSWorld — wraps to restore project context */
    public void addTickCallback(Runnable cb) {
        String project = currentProject;
        tickCallbacks.add(() -> {
            String prev = currentProject;
            setCurrentProject(project);
            try { cb.run(); } finally { setCurrentProject(prev); }
        });
    }
    public void addJoinCallback(Box3JSWorld.PlayerJoinCallback cb) { joinCallbacks.add(cb); }
    public void addLeaveCallback(Box3JSWorld.PlayerLeaveCallback cb) { leaveCallbacks.add(cb); }
    public void addVoxelDestroyCallback(Box3JSWorld.VoxelDestroyCallback cb) { voxelDestroyCallbacks.add(cb); }
    public void addVoxelContactCallback(Box3JSWorld.VoxelContactCallback cb) { voxelContactCallbacks.add(cb); }
    public void addInteractCallback(Box3JSWorld.InteractCallback cb) { interactCallbacks.add(cb); }
    public void addChatCallback(Box3JSWorld.ChatCallback cb) { chatCallbacks.add(cb); }
    public void addFluidEnterCallback(Box3JSWorld.FluidEnterCallback cb) { fluidEnterCallbacks.add(cb); }
    public void addFluidLeaveCallback(Box3JSWorld.FluidLeaveCallback cb) { fluidLeaveCallbacks.add(cb); }
    public void addEntityContactCallback(Box3JSWorld.EntityContactCallback cb) { entityContactCallbacks.add(cb); }
    public void addEntitySeparateCallback(Box3JSWorld.EntitySeparateCallback cb) { entitySeparateCallbacks.add(cb); }
    public void addBlockPlaceCallback(Box3JSWorld.BlockPlaceCallback cb) { blockPlaceCallbacks.add(cb); }
    public void addEntityDeathCallback(Box3JSWorld.EntityDeathCallback cb) { entityDeathCallbacks.add(cb); }
    public void addRespawnCallback(Box3JSWorld.PlayerRespawnCallback cb) { respawnCallbacks.add(cb); }
    public void addBlockActivateCallback(Box3JSWorld.BlockActivateCallback cb) { blockActivateCallbacks.add(cb); }
    public void addEntityDamageCallback(Box3JSWorld.EntityDamageCallback cb) { entityDamageCallbacks.add(cb); }
    public void addMessageCallback(String project, Box3JSWorld.MessageCallback cb) {
        messageCallbacks.computeIfAbsent(project, k -> new CopyOnWriteArrayList<>()).add(cb);
    }
    public void setPlayerChatHandler(UUID uuid, Function handler) { playerChatHandlers.put(uuid, handler); }

    public void setCurrentProject(String name) {
        currentProject = name;
        worldBinding.setProjectName(name);
    }
    public String getCurrentProject() { return currentProject; }

    public void fireMessage(String sender, String target, Object data) {
        if ("*".equals(target)) {
            for (var entry : messageCallbacks.entrySet()) {
                if (!entry.getKey().equals(sender)) {
                    for (var cb : entry.getValue()) {
                        String prev = currentProject;
                        setCurrentProject(entry.getKey());
                        try { cb.onMessage(sender, data); } finally { setCurrentProject(prev); }
                    }
                }
            }
        } else {
            List<Box3JSWorld.MessageCallback> cbs = messageCallbacks.get(target);
            if (cbs != null) {
                for (var cb : cbs) {
                    String prev = currentProject;
                    setCurrentProject(target);
                    try { cb.onMessage(sender, data); } finally { setCurrentProject(prev); }
                }
            }
        }
    }

    public int scheduleTimeout(Function handler, int ticks) {
        int id = ++timerIdCounter;
        timers.add(new TimerEntry(id, handler, ticks, 0, currentProject));
        return id;
    }

    public int scheduleInterval(Function handler, int ticks) {
        int id = ++timerIdCounter;
        timers.add(new TimerEntry(id, handler, ticks, ticks, currentProject));
        return id;
    }

    public void clearTimer(int id) {
        timers.removeIf(t -> t.id == id);
    }

    private void fireTimers() {
        var toFire = new ArrayList<TimerEntry>();
        var toRemove = new ArrayList<TimerEntry>();
        for (var t : timers) {
            if (--t.remaining <= 0) {
                toFire.add(t);
                if (t.interval == 0) {
                    toRemove.add(t);
                } else {
                    t.remaining = t.interval;
                }
            }
        }
        timers.removeAll(toRemove);
        for (var t : toFire) {
            String prev = currentProject;
            setCurrentProject(t.project);
            try { callFunction(t.handler); } finally { setCurrentProject(prev); }
        }
    }

    public void fireTick() {
        fireTimers();
        for (Runnable cb : tickCallbacks) cb.run();
        // Voxel contact tracking: check if any tracked entity changed block position
        if (!voxelContactCallbacks.isEmpty()) {
            long tick = server.getTickCount();
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                UUID uuid = player.getUUID();
                BlockPos current = player.blockPosition();
                BlockPos last = voxelContactTracked.put(uuid, current);
                if (!current.equals(last)) {
                    Box3JSEntity entity = new Box3JSEntity(player, server, this);
                    var state = player.level().getBlockState(current);
                    int voxelId = voxelsBinding.getId(state);
                    double force = player.getDeltaMovement().length();
                    for (var cb : voxelContactCallbacks) {
                        cb.onContact(entity, voxelId, current.getX(), current.getY(), current.getZ(), 1, force, tick);
                    }
                }
            }
        }
        // Fluid state tracking
        if (!fluidEnterCallbacks.isEmpty() || !fluidLeaveCallbacks.isEmpty()) {
            long tick = server.getTickCount();
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                UUID uuid = player.getUUID();
                String current = player.isInLava() ? "lava" : player.isInWater() ? "water" : "none";
                String last = fluidStateTracked.put(uuid, current);
                if (!current.equals(last)) {
                    Box3JSEntity entity = new Box3JSEntity(player, server, this);
                    BlockPos pos = player.blockPosition();
                    if (!"none".equals(current) && !"none".equals(last) && last != null) {
                        // Switched fluid type (water→lava or lava→water)
                        for (var cb : fluidLeaveCallbacks) {
                            cb.onLeave(entity, last, pos.getX(), pos.getY(), pos.getZ(), tick);
                        }
                        for (var cb : fluidEnterCallbacks) {
                            cb.onEnter(entity, current, pos.getX(), pos.getY(), pos.getZ(), tick);
                        }
                    } else if (!"none".equals(current) && ("none".equals(last) || last == null)) {
                        for (var cb : fluidEnterCallbacks) {
                            cb.onEnter(entity, current, pos.getX(), pos.getY(), pos.getZ(), tick);
                        }
                    } else if ("none".equals(current) && last != null && !"none".equals(last)) {
                        for (var cb : fluidLeaveCallbacks) {
                            cb.onLeave(entity, last, pos.getX(), pos.getY(), pos.getZ(), tick);
                        }
                    }
                }
            }
        }
        // Entity contact tracking
        if (!entityContactCallbacks.isEmpty()) {
            long tick = server.getTickCount();
            var players = server.getPlayerList().getPlayers();
            for (int i = 0; i < players.size(); i++) {
                for (int j = i + 1; j < players.size(); j++) {
                    ServerPlayer a = players.get(i);
                    ServerPlayer b = players.get(j);
                    double dist = a.distanceToSqr(b);
                    String pairKey = a.getStringUUID() + "|" + b.getStringUUID();
                    if (dist < 2.25) { // 1.5 blocks squared
                        if (entityContactPairs.add(pairKey)) {
                            Box3JSEntity ea = new Box3JSEntity(a, server, this);
                            Box3JSEntity eb = new Box3JSEntity(b, server, this);
                            for (var cb : entityContactCallbacks) {
                                cb.onContact(ea, eb, tick);
                            }
                        }
                    } else if (entityContactPairs.remove(pairKey)) {
                        if (!entitySeparateCallbacks.isEmpty()) {
                            Box3JSEntity ea = new Box3JSEntity(a, server, this);
                            Box3JSEntity eb = new Box3JSEntity(b, server, this);
                            for (var cb : entitySeparateCallbacks) {
                                cb.onSeparate(ea, eb, tick);
                            }
                        }
                    }
                }
            }
        }
    }

    private String getBlockIdString(BlockPos pos) {
        var state = server.overworld().getBlockState(pos);
        var key = state.getBlock().builtInRegistryHolder().key();
        return key != null ? key.location().toString() : "minecraft:air";
    }

    public void fireVoxelDestroy(ServerPlayer player, BlockPos pos) {
        if (voxelDestroyCallbacks.isEmpty()) return;
        Box3JSEntity entity = new Box3JSEntity(player, server, this);
        long tick = server.getTickCount();
        String voxel = getBlockIdString(pos);
        for (var cb : voxelDestroyCallbacks) {
            cb.onDestroy(entity, pos.getX(), pos.getY(), pos.getZ(), voxel, tick);
        }
    }

    public void fireInteract(ServerPlayer player, net.minecraft.world.entity.Entity target) {
        if (interactCallbacks.isEmpty()) return;
        Box3JSEntity entity = new Box3JSEntity(player, server, this);
        Box3JSEntity targetEntity = new Box3JSEntity(target, server, this);
        long tick = server.getTickCount();
        for (var cb : interactCallbacks) {
            cb.onInteract(entity, targetEntity, tick);
        }
    }

    public void fireChat(ServerPlayer player, String message) {
        Box3JSEntity entity = new Box3JSEntity(player, server, this);
        long tick = server.getTickCount();
        // Global chat callbacks
        for (var cb : chatCallbacks) {
            cb.onChat(entity, message, tick);
        }
        // Per-player chat handler
        Function playerHandler = playerChatHandlers.get(player.getUUID());
        if (playerHandler != null) {
            callFunction(playerHandler, entity, message, tick);
        }
    }

    public void fireBlockPlace(ServerPlayer player, BlockPos pos, BlockState state) {
        if (blockPlaceCallbacks.isEmpty()) return;
        Box3JSEntity entity = new Box3JSEntity(player, server, this);
        long tick = server.getTickCount();
        int voxelId = voxelsBinding.getId(state);
        String voxel = state.isAir() ? "minecraft:air" : state.getBlock().builtInRegistryHolder().key().location().toString();
        for (var cb : blockPlaceCallbacks) {
            cb.onPlace(entity, pos.getX(), pos.getY(), pos.getZ(), voxel, voxelId, tick);
        }
    }

    public void fireEntityDeath(net.minecraft.world.entity.Entity deadEntity, net.minecraft.world.entity.Entity attacker) {
        if (entityDeathCallbacks.isEmpty()) return;
        Box3JSEntity entity = new Box3JSEntity(deadEntity, server, this);
        Box3JSEntity killer = attacker != null ? new Box3JSEntity(attacker, server, this) : null;
        long tick = server.getTickCount();
        for (var cb : entityDeathCallbacks) {
            cb.onDeath(entity, killer, tick);
        }
    }

    public void firePlayerRespawn(ServerPlayer player) {
        if (respawnCallbacks.isEmpty()) return;
        Box3JSEntity entity = new Box3JSEntity(player, server, this);
        for (var cb : respawnCallbacks) {
            cb.onRespawn(entity);
        }
    }

    public void fireBlockActivate(ServerPlayer player, BlockPos pos, BlockState state) {
        if (blockActivateCallbacks.isEmpty()) return;
        Box3JSEntity entity = new Box3JSEntity(player, server, this);
        long tick = server.getTickCount();
        String voxel = state.isAir() ? "minecraft:air" : state.getBlock().builtInRegistryHolder().key().location().toString();
        for (var cb : blockActivateCallbacks) {
            cb.onActivate(entity, pos.getX(), pos.getY(), pos.getZ(), voxel, tick);
        }
    }

    public void fireEntityDamage(net.minecraft.world.entity.Entity damagedEntity, double amount, String source, net.minecraft.world.entity.Entity attacker) {
        if (entityDamageCallbacks.isEmpty()) return;
        Box3JSEntity entity = new Box3JSEntity(damagedEntity, server, this);
        Box3JSEntity attackerEntity = attacker != null ? new Box3JSEntity(attacker, server, this) : null;
        long tick = server.getTickCount();
        for (var cb : entityDamageCallbacks) {
            cb.onDamage(entity, amount, source, attackerEntity, tick);
        }
    }

    public void firePlayerJoin(ServerPlayer player) {
        Box3JSEntity entity = new Box3JSEntity(player, server, this);
        for (var cb : joinCallbacks) cb.onJoin(entity);
    }

    public void firePlayerLeave(ServerPlayer player) {
        Box3JSEntity entity = new Box3JSEntity(player, server, this);
        for (var cb : leaveCallbacks) cb.onLeave(entity);
    }

    /** Call a JS function from Java, managing Rhino context */
    public Object callFunction(Function fn, Object... args) {
        Context cx = Context.enter();
        try {
            return fn.call(cx, scope, scope, args);
        } finally {
            Context.exit();
        }
    }

    /** Wrap a Java object for return to JS */
    public Object wrap(Object obj) {
        return Context.javaToJS(obj, scope);
    }

    public ScriptableObject getScope() { return scope; }

    public Map<String, Object> getCustomProps(UUID uuid) {
        return entityCustomProps.computeIfAbsent(uuid, k -> new HashMap<>());
    }

    public void clearCustomProps(UUID uuid) {
        entityCustomProps.remove(uuid);
    }

    /** Clear all callbacks and reset the JS scope (keeps server binding) */
    public void reset() {
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
        timerIdCounter = 0;
        projectRequires.clear();
        this.worldBinding = new Box3JSWorld(server, this);
        this.voxelsBinding = new Box3JSVoxels(server);
        this.storageBinding = new Box3JSStorage(server.getServerDirectory().resolve("config"), this);
        setupScope();
    }

    private void setupScope() {
        Context cx = Context.enter();
        try {
            scope = cx.initStandardObjects();
            ScriptableObject.putProperty(scope, "world", Context.javaToJS(worldBinding, scope));
            ScriptableObject.putProperty(scope, "voxels", Context.javaToJS(voxelsBinding, scope));
            ScriptableObject.putProperty(scope, "storage", Context.javaToJS(storageBinding, scope));
            ScriptableObject.putProperty(scope, "_jConsole", Context.javaToJS(new Box3JSConsole(), scope));
            cx.evaluateString(scope,
                "console = {" +
                "  log: function() { return _jConsole.log.apply(_jConsole, arguments); }," +
                "  debug: function() { return _jConsole.debug.apply(_jConsole, arguments); }," +
                "  warn: function() { return _jConsole.warn.apply(_jConsole, arguments); }," +
                "  error: function() { return _jConsole.error.apply(_jConsole, arguments); }," +
                "  clear: function() { return _jConsole.clear.apply(_jConsole, arguments); }," +
                "  assert: function(a) {" +
                "    if (!a) {" +
                "      var b = [];" +
                "      for (var i = 1; i < arguments.length; i++) b.push(arguments[i]);" +
                "      _jConsole.error(b.length ? b : ['Assertion failed']);" +
                "    }" +
                "  }" +
                "};",
                "console-init", 1, null);
            ScriptableObject.putProperty(scope, "require", new BaseFunction() {
                @Override
                public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
                    String moduleId = args[0].toString();
                    String project = currentProject;
                    if (project == null) {
                        throw ScriptRuntime.throwError(cx, scope, "require() called outside a project context");
                    }
                    Path projectDir = Box3ScriptConfig.get().getScriptDir(server).resolve(project);
                    Require req = projectRequires.computeIfAbsent(project, p -> {
                        try {
                            ModuleScriptProvider provider = new StrongCachingModuleScriptProvider(
                                new UrlModuleSourceProvider(
                                    Collections.unmodifiableList(java.util.Arrays.asList(
                                        projectDir.resolve("dist").toUri(),
                                        projectDir.toUri())), null) {
                                    @Override
                                    protected String getCharacterEncoding(java.net.URLConnection c) {
                                        return "utf-8";
                                    }
                                });
                            return new RequireBuilder()
                                .setModuleScriptProvider(provider)
                                .setSandboxed(false)
                                .createRequire(cx, Box3ScriptEngine.this.scope);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });
                    return req.requireMain(cx, moduleId);
                }
            });
            ScriptableObject.putProperty(scope, "sleep", new BaseFunction() {
                @Override
                public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
                    int ms = ((Number) args[0]).intValue();
                    try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
                    return Undefined.instance;
                }
            });
            ScriptableObject.putProperty(scope, "GameVector3", new NativeJavaClass(scope, GameVector3.class));
            ScriptableObject.putProperty(scope, "GameBounds3", new NativeJavaClass(scope, GameBounds3.class));
            ScriptableObject.putProperty(scope, "GameRGBColor", new NativeJavaClass(scope, GameRGBColor.class));
            ScriptableObject.putProperty(scope, "GameRGBAColor", new NativeJavaClass(scope, GameRGBAColor.class));
            ScriptableObject.putProperty(scope, "GameQuaternion", new NativeJavaClass(scope, GameQuaternion.class));
            cx.evaluateString(scope,
                "GameDialogType = { TEXT: 'TEXT', INPUT: 'INPUT', SELECT: 'SELECT' }; " +
                "GameButtonType = { WALK: 'WALK', RUN: 'RUN', CROUCH: 'CROUCH', JUMP: 'JUMP', " +
                "  DOUBLE_JUMP: 'DOUBLE_JUMP', FLY: 'FLY', ACTION0: 'ACTION0', ACTION1: 'ACTION1' }; " +
                "GameInputDirection = { NONE: 0, VERTICAL: 1, HORIZONTAL: 2, BOTH: 3 }; " +
                "GameCameraMode = { FIXED: 'FIXED', FOLLOW: 'FOLLOW', FPS: 'FPS', RELATIVE: 'RELATIVE' }; " +
                "GamePlayerMoveState = { FLYING: 'FLYING', GROUND: 'GROUND', SWIM: 'SWIM', FALL: 'FALL', " +
                "  JUMP: 'JUMP', DOUBLE_JUMP: 'DOUBLE_JUMP' }; " +
                "GamePlayerWalkState = { NONE: 'NONE', CROUCH: 'CROUCH', WALK: 'WALK', RUN: 'RUN' };",
                "enums", 1, null);
        } finally {
            Context.exit();
        }
    }

    public MinecraftServer getServer() { return server; }
    public Box3JSWorld getWorldBinding() { return worldBinding; }
    public Box3JSVoxels getVoxelsBinding() { return voxelsBinding; }

    public class Box3JSConsole {
        private void print(String level, Object... args) {
            StringBuilder sb = new StringBuilder();
            String proj = currentProject;
            if (proj != null) sb.append('[').append(proj).append("] ");
            for (Object a : args) sb.append(a).append(' ');
            System.out.println("[Box3JS]" + level + " " + sb.toString().trim());
        }

        public void log(Object... args) { print("", args); }
        public void debug(Object... args) { print("[DEBUG]", args); }
        public void warn(Object... args) { print("[WARN]", args); }

        public void error(Object... args) {
            StringBuilder sb = new StringBuilder();
            String proj = currentProject;
            if (proj != null) sb.append('[').append(proj).append("] ");
            for (Object a : args) sb.append(a).append(' ');
            System.err.println("[Box3JS][ERROR] " + sb.toString().trim());
        }

        public void clear() {
            System.out.print("\033[H\033[2J");
            System.out.flush();
        }
    }

    private static class TimerEntry {
        final int id;
        final Function handler;
        int remaining;
        final int interval;
        final String project;

        TimerEntry(int id, Function handler, int remaining, int interval, String project) {
            this.id = id;
            this.handler = handler;
            this.remaining = remaining;
            this.interval = interval;
            this.project = project;
        }
    }
}

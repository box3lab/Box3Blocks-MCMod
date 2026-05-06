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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class Box3ScriptEngine {

    private static final Box3ScriptEngine INSTANCE = new Box3ScriptEngine();

    private ScriptableObject scope;
    private Box3JSWorld worldBinding;
    private Box3JSVoxels voxelsBinding;
    private Box3JSStorage storageBinding;
    private MinecraftServer server;
    private boolean initialized;

    final Box3JSEventBus bus = new Box3JSEventBus();
    private String currentProject;
    private Consumer<String> errorReporter;
    private final Map<String, Require> projectRequires = new HashMap<>();

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

    /** Report error to the current errorReporter (player), or just log if none. */
    void reportError(String msg) {
        Box3JS.LOGGER.error(msg);
        if (errorReporter != null) errorReporter.accept(msg);
    }

    /** Set reporter for the current operation, clear after. Returns self for chaining. */
    Box3ScriptEngine withErrorReporter(Consumer<String> reporter) {
        this.errorReporter = reporter;
        return this;
    }
    void clearErrorReporter() { this.errorReporter = null; }

    // ---- Callback registration (all wrap project context) ----

    public void addTickCallback(Runnable cb) {
        String project = currentProject;
        bus.addTick(project, wrapContext(project, cb));
    }
    public void addJoinCallback(PlayerJoinCallback cb) {
        String project = currentProject;
        bus.addJoin(project, (e) -> runInContext(project, () -> cb.onJoin(e)));
    }
    public void addLeaveCallback(PlayerLeaveCallback cb) {
        String project = currentProject;
        bus.addLeave(project, (e) -> runInContext(project, () -> cb.onLeave(e)));
    }
    public void addVoxelDestroyCallback(VoxelDestroyCallback cb) {
        String project = currentProject;
        bus.addVoxelDestroy(project, (e, x, y, z, v, t) -> runInContext(project, () -> cb.onDestroy(e, x, y, z, v, t)));
    }
    public void addVoxelContactCallback(VoxelContactCallback cb) {
        String project = currentProject;
        bus.addVoxelContact(project, (e, v, x, y, z, a, f, t) -> runInContext(project, () -> cb.onContact(e, v, x, y, z, a, f, t)));
    }
    public void addInteractCallback(InteractCallback cb) {
        String project = currentProject;
        bus.addInteract(project, (e, tgt, tick) -> runInContext(project, () -> cb.onInteract(e, tgt, tick)));
    }
    public void addChatCallback(ChatCallback cb) {
        String project = currentProject;
        bus.addChat(project, (e, msg, tick) -> runInContext(project, () -> cb.onChat(e, msg, tick)));
    }
    public void addFluidEnterCallback(FluidEnterCallback cb) {
        String project = currentProject;
        bus.addFluidEnter(project, (e, f, x, y, z, t) -> runInContext(project, () -> cb.onEnter(e, f, x, y, z, t)));
    }
    public void addFluidLeaveCallback(FluidLeaveCallback cb) {
        String project = currentProject;
        bus.addFluidLeave(project, (e, f, x, y, z, t) -> runInContext(project, () -> cb.onLeave(e, f, x, y, z, t)));
    }
    public void addEntityContactCallback(EntityContactCallback cb) {
        String project = currentProject;
        bus.addEntityContact(project, (e, o, t) -> runInContext(project, () -> cb.onContact(e, o, t)));
    }
    public void addEntitySeparateCallback(EntitySeparateCallback cb) {
        String project = currentProject;
        bus.addEntitySeparate(project, (e, o, t) -> runInContext(project, () -> cb.onSeparate(e, o, t)));
    }
    public void addBlockPlaceCallback(BlockPlaceCallback cb) {
        String project = currentProject;
        bus.addBlockPlace(project, (e, x, y, z, v, vid, t) -> runInContext(project, () -> cb.onPlace(e, x, y, z, v, vid, t)));
    }
    public void addEntityDeathCallback(EntityDeathCallback cb) {
        String project = currentProject;
        bus.addEntityDeath(project, (e, k, t) -> runInContext(project, () -> cb.onDeath(e, k, t)));
    }
    public void addRespawnCallback(PlayerRespawnCallback cb) {
        String project = currentProject;
        bus.addRespawn(project, (e) -> runInContext(project, () -> cb.onRespawn(e)));
    }
    public void addBlockActivateCallback(BlockActivateCallback cb) {
        String project = currentProject;
        bus.addBlockActivate(project, (e, x, y, z, v, t) -> runInContext(project, () -> cb.onActivate(e, x, y, z, v, t)));
    }
    public void addEntityDamageCallback(EntityDamageCallback cb) {
        String project = currentProject;
        bus.addEntityDamage(project, (e, a, s, at, t) -> runInContext(project, () -> cb.onDamage(e, a, s, at, t)));
    }
    public void addMessageCallback(String project, MessageCallback cb) {
        bus.addMessage(project, (from, d) -> runInContext(project, () -> cb.onMessage(from, d)));
    }
    public void setPlayerChatHandler(UUID uuid, Function handler) {
        String project = currentProject;
        bus.chatHandlersFor(project).put(uuid, handler);
    }

    private Runnable wrapContext(String project, Runnable cb) {
        return () -> {
            String prev = currentProject;
            setCurrentProject(project);
            try { cb.run(); } finally { setCurrentProject(prev); }
        };
    }

    private void runInContext(String project, Runnable action) {
        String prev = currentProject;
        setCurrentProject(project);
        try { action.run(); } finally { setCurrentProject(prev); }
    }

    public void setCurrentProject(String name) {
        currentProject = name;
        worldBinding.setProjectName(name);
    }
    public String getCurrentProject() { return currentProject; }

    // ---- Project lifecycle ----

    /** Remove one project's callbacks without affecting others. */
    public void removeProject(String project) {
        bus.removeProject(project);
        projectRequires.remove(project);
        Box3JS.LOGGER.info("Removed project: {}", project);
    }

    // ---- Message routing ----

    public void fireMessage(String sender, String target, Object data) {
        if ("*".equals(target)) {
            for (var entry : bus.messageCallbacks.entrySet()) {
                if (!entry.getKey().equals(sender)) {
                    for (var cb : entry.getValue()) cb.onMessage(sender, data);
                }
            }
        } else {
            List<MessageCallback> cbs = bus.messageCallbacks.get(target);
            if (cbs != null) {
                for (var cb : cbs) cb.onMessage(sender, data);
            }
        }
    }

    // ---- Timers ----

    public int scheduleTimeout(Function handler, int ticks) {
        String project = currentProject;
        int id = bus.nextTimerId(project);
        bus.timersFor(project).add(new TimerEntry(id, handler, ticks, 0, project));
        return id;
    }

    public int scheduleInterval(Function handler, int ticks) {
        String project = currentProject;
        int id = bus.nextTimerId(project);
        bus.timersFor(project).add(new TimerEntry(id, handler, ticks, ticks, project));
        return id;
    }

    public void clearTimer(int id) {
        for (var list : bus.timers.values()) {
            if (list.removeIf(t -> t.id == id)) return;
        }
    }

    private void fireTimers() {
        for (var list : bus.timers.values()) {
            var toFire = new ArrayList<TimerEntry>();
            var toRemove = new ArrayList<TimerEntry>();
            for (var t : list) {
                if (--t.remaining <= 0) {
                    toFire.add(t);
                    if (t.interval == 0) toRemove.add(t);
                    else t.remaining = t.interval;
                }
            }
            list.removeAll(toRemove);
            for (var t : toFire) {
                runInContext(t.project, () -> callFunction(t.handler));
            }
        }
    }

    // ---- Tick ----

    public void fireTick() {
        fireTimers();
        for (var list : bus.tickCallbacks.values()) {
            for (Runnable cb : list) cb.run();
        }
        // Voxel contact tracking — per-project
        for (var entry : bus.voxelContactCallbacks.entrySet()) {
            String project = entry.getKey();
            var callbacks = entry.getValue();
            if (callbacks.isEmpty()) continue;
            long tick = server.getTickCount();
            var tracked = bus.voxelContactFor(project);
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                UUID uuid = player.getUUID();
                BlockPos current = player.blockPosition();
                BlockPos last = tracked.put(uuid, current);
                if (!current.equals(last)) {
                    Box3JSEntity entity = new Box3JSEntity(player, server, this);
                    var state = player.level().getBlockState(current);
                    int voxelId = voxelsBinding.getId(state);
                    double force = player.getDeltaMovement().length();
                    runInContext(project, () -> {
                        for (var cb : callbacks) {
                            cb.onContact(entity, voxelId, current.getX(), current.getY(), current.getZ(), 1, force, tick);
                        }
                    });
                }
            }
        }
        // Fluid state tracking — per-project
        for (var entry : bus.fluidEnterCallbacks.entrySet()) {
            tickFluid(entry.getKey(), entry.getValue(), bus.fluidLeaveCallbacks.get(entry.getKey()));
        }
        for (var entry : bus.fluidLeaveCallbacks.entrySet()) {
            if (bus.fluidEnterCallbacks.containsKey(entry.getKey())) continue; // handled above
            tickFluid(entry.getKey(), bus.fluidEnterCallbacks.get(entry.getKey()), entry.getValue());
        }
        // Entity contact tracking — per-project
        for (var entry : bus.entityContactCallbacks.entrySet()) {
            String project = entry.getKey();
            var callbacks = entry.getValue();
            if (callbacks.isEmpty()) continue;
            long tick = server.getTickCount();
            var pairs = bus.contactPairsFor(project);
            var separate = bus.entitySeparateCallbacks.getOrDefault(project, Collections.emptyList());
            var players = server.getPlayerList().getPlayers();
            for (int i = 0; i < players.size(); i++) {
                for (int j = i + 1; j < players.size(); j++) {
                    ServerPlayer a = players.get(i);
                    ServerPlayer b = players.get(j);
                    double dist = a.distanceToSqr(b);
                    String pairKey = a.getStringUUID() + "|" + b.getStringUUID();
                    if (dist < 2.25) {
                        if (pairs.add(pairKey)) {
                            Box3JSEntity ea = new Box3JSEntity(a, server, this);
                            Box3JSEntity eb = new Box3JSEntity(b, server, this);
                            runInContext(project, () -> {
                                for (var cb : callbacks) cb.onContact(ea, eb, tick);
                            });
                        }
                    } else if (pairs.remove(pairKey) && !separate.isEmpty()) {
                        Box3JSEntity ea = new Box3JSEntity(a, server, this);
                        Box3JSEntity eb = new Box3JSEntity(b, server, this);
                        runInContext(project, () -> {
                            for (var cb : separate) cb.onSeparate(ea, eb, tick);
                        });
                    }
                }
            }
        }
    }

    private void tickFluid(String project, List<FluidEnterCallback> enter, List<FluidLeaveCallback> leave) {
        if ((enter == null || enter.isEmpty()) && (leave == null || leave.isEmpty())) return;
        long tick = server.getTickCount();
        var tracked = bus.fluidStateFor(project);
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            UUID uuid = player.getUUID();
            String current = player.isInLava() ? "lava" : player.isInWater() ? "water" : "none";
            String last = tracked.put(uuid, current);
            if (current.equals(last)) continue;
            Box3JSEntity entity = new Box3JSEntity(player, server, this);
            BlockPos pos = player.blockPosition();
            if (!"none".equals(current) && !"none".equals(last) && last != null) {
                runInContext(project, () -> {
                    if (leave != null) for (var cb : leave) cb.onLeave(entity, last, pos.getX(), pos.getY(), pos.getZ(), tick);
                    if (enter != null) for (var cb : enter) cb.onEnter(entity, current, pos.getX(), pos.getY(), pos.getZ(), tick);
                });
            } else if (!"none".equals(current) && ("none".equals(last) || last == null)) {
                runInContext(project, () -> {
                    if (enter != null) for (var cb : enter) cb.onEnter(entity, current, pos.getX(), pos.getY(), pos.getZ(), tick);
                });
            } else if ("none".equals(current) && last != null && !"none".equals(last)) {
                runInContext(project, () -> {
                    if (leave != null) for (var cb : leave) cb.onLeave(entity, last, pos.getX(), pos.getY(), pos.getZ(), tick);
                });
            }
        }
    }

    // ---- Fire methods (iterate all projects) ----

    private String getBlockIdString(BlockPos pos) {
        var state = server.overworld().getBlockState(pos);
        var key = state.getBlock().builtInRegistryHolder().key();
        return key != null ? key.location().toString() : "minecraft:air";
    }

    public void fireVoxelDestroy(ServerPlayer player, BlockPos pos) {
        String voxel = null;
        long tick = -1;
        Box3JSEntity entity = null;
        for (var entry : bus.voxelDestroyCallbacks.entrySet()) {
            if (entry.getValue().isEmpty()) continue;
            if (entity == null) { entity = new Box3JSEntity(player, server, this); tick = server.getTickCount(); voxel = getBlockIdString(pos); }
            Box3JSEntity e = entity;
            long t = tick;
            String v = voxel;
            runInContext(entry.getKey(), () -> {
                for (var cb : entry.getValue()) cb.onDestroy(e, pos.getX(), pos.getY(), pos.getZ(), v, t);
            });
        }
    }

    public void fireInteract(ServerPlayer player, net.minecraft.world.entity.Entity target) {
        Box3JSEntity entity = null;
        Box3JSEntity targetEntity = null;
        long tick = -1;
        for (var entry : bus.interactCallbacks.entrySet()) {
            if (entry.getValue().isEmpty()) continue;
            if (entity == null) { entity = new Box3JSEntity(player, server, this); targetEntity = new Box3JSEntity(target, server, this); tick = server.getTickCount(); }
            Box3JSEntity e = entity;
            Box3JSEntity te = targetEntity;
            long t = tick;
            runInContext(entry.getKey(), () -> {
                for (var cb : entry.getValue()) cb.onInteract(e, te, t);
            });
        }
    }

    public void fireChat(ServerPlayer player, String message) {
        Box3JSEntity entity = null;
        long tick = -1;
        for (var entry : bus.chatCallbacks.entrySet()) {
            if (entity == null) { entity = new Box3JSEntity(player, server, this); tick = server.getTickCount(); }
            Box3JSEntity e = entity;
            long t = tick;
            runInContext(entry.getKey(), () -> {
                for (var cb : entry.getValue()) cb.onChat(e, message, t);
            });
        }
        // Per-player chat handlers
        for (var entry : bus.playerChatHandlers.entrySet()) {
            Function handler = entry.getValue().get(player.getUUID());
            if (handler != null) {
                Box3JSEntity e = entity != null ? entity : new Box3JSEntity(player, server, this);
                long t = tick != -1 ? tick : server.getTickCount();
                String project = entry.getKey();
                runInContext(project, () -> callFunction(handler, e, message, t));
            }
        }
    }

    public void fireBlockPlace(ServerPlayer player, BlockPos pos, BlockState state) {
        Box3JSEntity entity = null;
        long tick = -1;
        int voxelId = -1;
        String voxel = null;
        for (var entry : bus.blockPlaceCallbacks.entrySet()) {
            if (entry.getValue().isEmpty()) continue;
            if (entity == null) { entity = new Box3JSEntity(player, server, this); tick = server.getTickCount(); voxelId = voxelsBinding.getId(state); voxel = state.isAir() ? "minecraft:air" : state.getBlock().builtInRegistryHolder().key().location().toString(); }
            Box3JSEntity e = entity;
            long t = tick;
            int vid = voxelId;
            String v = voxel;
            runInContext(entry.getKey(), () -> {
                for (var cb : entry.getValue()) cb.onPlace(e, pos.getX(), pos.getY(), pos.getZ(), v, vid, t);
            });
        }
    }

    public void fireEntityDeath(net.minecraft.world.entity.Entity deadEntity, net.minecraft.world.entity.Entity attacker) {
        Box3JSEntity entity = null;
        Box3JSEntity killer = null;
        long tick = -1;
        for (var entry : bus.entityDeathCallbacks.entrySet()) {
            if (entry.getValue().isEmpty()) continue;
            if (entity == null) { entity = new Box3JSEntity(deadEntity, server, this); killer = attacker != null ? new Box3JSEntity(attacker, server, this) : null; tick = server.getTickCount(); }
            Box3JSEntity e = entity;
            Box3JSEntity k = killer;
            long t = tick;
            runInContext(entry.getKey(), () -> {
                for (var cb : entry.getValue()) cb.onDeath(e, k, t);
            });
        }
    }

    public void firePlayerRespawn(ServerPlayer player) {
        Box3JSEntity entity = null;
        for (var entry : bus.respawnCallbacks.entrySet()) {
            if (entry.getValue().isEmpty()) continue;
            if (entity == null) entity = new Box3JSEntity(player, server, this);
            Box3JSEntity e = entity;
            runInContext(entry.getKey(), () -> {
                for (var cb : entry.getValue()) cb.onRespawn(e);
            });
        }
    }

    public void fireBlockActivate(ServerPlayer player, BlockPos pos, BlockState state) {
        Box3JSEntity entity = null;
        long tick = -1;
        String voxel = null;
        for (var entry : bus.blockActivateCallbacks.entrySet()) {
            if (entry.getValue().isEmpty()) continue;
            if (entity == null) { entity = new Box3JSEntity(player, server, this); tick = server.getTickCount(); voxel = state.isAir() ? "minecraft:air" : state.getBlock().builtInRegistryHolder().key().location().toString(); }
            Box3JSEntity e = entity;
            long t = tick;
            String v = voxel;
            runInContext(entry.getKey(), () -> {
                for (var cb : entry.getValue()) cb.onActivate(e, pos.getX(), pos.getY(), pos.getZ(), v, t);
            });
        }
    }

    public void fireEntityDamage(net.minecraft.world.entity.Entity damagedEntity, double amount, String source, net.minecraft.world.entity.Entity attacker) {
        Box3JSEntity entity = null;
        Box3JSEntity attackerEntity = null;
        long tick = -1;
        for (var entry : bus.entityDamageCallbacks.entrySet()) {
            if (entry.getValue().isEmpty()) continue;
            if (entity == null) { entity = new Box3JSEntity(damagedEntity, server, this); attackerEntity = attacker != null ? new Box3JSEntity(attacker, server, this) : null; tick = server.getTickCount(); }
            Box3JSEntity e = entity;
            Box3JSEntity ae = attackerEntity;
            long t = tick;
            runInContext(entry.getKey(), () -> {
                for (var cb : entry.getValue()) cb.onDamage(e, amount, source, ae, t);
            });
        }
    }

    public void firePlayerJoin(ServerPlayer player) {
        Box3JSEntity entity = null;
        for (var entry : bus.joinCallbacks.entrySet()) {
            if (entry.getValue().isEmpty()) continue;
            if (entity == null) entity = new Box3JSEntity(player, server, this);
            Box3JSEntity e = entity;
            runInContext(entry.getKey(), () -> {
                for (var cb : entry.getValue()) cb.onJoin(e);
            });
        }
    }

    public void firePlayerLeave(ServerPlayer player) {
        Box3JSEntity entity = null;
        for (var entry : bus.leaveCallbacks.entrySet()) {
            if (entry.getValue().isEmpty()) continue;
            if (entity == null) entity = new Box3JSEntity(player, server, this);
            Box3JSEntity e = entity;
            runInContext(entry.getKey(), () -> {
                for (var cb : entry.getValue()) cb.onLeave(e);
            });
        }
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
        return bus.entityCustomProps.computeIfAbsent(uuid, k -> new HashMap<>());
    }

    public void clearCustomProps(UUID uuid) {
        bus.entityCustomProps.remove(uuid);
    }

    /** Clear all callbacks and reset the JS scope (keeps server binding) */
    public void reset() {
        bus.clearAll();
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

    static class TimerEntry {
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

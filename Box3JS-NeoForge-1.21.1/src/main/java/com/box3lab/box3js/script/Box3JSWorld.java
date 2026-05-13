package com.box3lab.box3js.script;

import com.box3lab.box3js.registries.Box3JSRecipeManager;
import java.nio.file.Path;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.storage.ServerLevelData;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.ScriptableObject;

import java.util.*;

public class Box3JSWorld {

    private final MinecraftServer server;
    private final Box3ScriptEngine engine;
    private String projectName;
    private final Box3JSScoreboard scoreboard;
    private final Box3JSTeam team;
    private final Box3JSBossbar bossbar;
    private final Box3JSQuery query;

    public Box3JSWorld(MinecraftServer server, Box3ScriptEngine engine) {
        this.server = server;
        this.engine = engine;
        this.scoreboard = new Box3JSScoreboard(server);
        this.team = new Box3JSTeam(server);
        this.bossbar = new Box3JSBossbar(server);
        this.query = new Box3JSQuery(server, engine);
    }

    public void setProjectName(String name) { this.projectName = name; }

    /** Clean up all bossbar/scoreboard/team state for one project. */
    public void removeProject(String project) {
        bossbar.removeProject(project);
        scoreboard.removeProject(project);
        team.removeProject(project);
    }

    /** Remove ALL bossbar/scoreboard/team state across all projects. */
    public void resetAll() {
        bossbar.resetAll();
        scoreboard.resetAll();
        team.resetAll();
    }

    private void trackIfSandboxed() {
        engine.getSandbox().trackWorld(engine.getCurrentProject());
    }

    // ---- World properties ----

    public String projectName() { return projectName != null ? projectName : ""; }
    public String getProjectName() { return projectName(); }

    public int currentTick() { return server.getTickCount(); }
    public int getCurrentTick() { return server.getTickCount(); }

    public String getServerId() { return server.getMotd(); }
    public void setServerId(String id) { server.setMotd(id); }

    public double getRainDensity() { return server.overworld().getRainLevel(1.0f); }
    public void setRainDensity(double v) { trackIfSandboxed(); server.overworld().getLevelData().setRaining(v > 0); }

    public double getThunderDensity() { return server.overworld().getThunderLevel(1.0f); }
    public void setThunderDensity(double v) {
        trackIfSandboxed();
        ((ServerLevelData) server.overworld().getLevelData()).setThundering(v > 0);
    }

    public void clearWeather() {
        trackIfSandboxed();
        var level = server.overworld();
        level.getLevelData().setRaining(false);
        ((ServerLevelData) level.getLevelData()).setThundering(false);
    }

    // ---- Time ----

    public long getTime() { return server.overworld().getDayTime(); }
    public void setTime(long tick) { trackIfSandboxed(); server.overworld().setDayTime(tick); }

    public double getTimeScale() {
        return server.overworld().getGameRules().getBoolean(GameRules.RULE_DAYLIGHT) ? 1.0 : 0.0;
    }
    public void setTimeScale(double v) {
        trackIfSandboxed();
        server.overworld().getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(v > 0, server);
    }

    // ---- Difficulty ----

    public String getDifficulty() { return server.overworld().getDifficulty().getKey(); }
    public void setDifficulty(Object v) {
        trackIfSandboxed();
        Difficulty diff = v instanceof Number n ? Difficulty.byId(n.intValue()) : Difficulty.byName(v.toString());
        if (diff != null) server.setDifficulty(diff, true);
    }

    // ---- Game Rules ----

    public Object getGameRule(String name) {
        GameRules rules = server.overworld().getGameRules();
        return switch (name) {
            case "doDaylightCycle" -> rules.getBoolean(GameRules.RULE_DAYLIGHT);
            case "doWeatherCycle" -> rules.getBoolean(GameRules.RULE_WEATHER_CYCLE);
            case "keepInventory" -> rules.getBoolean(GameRules.RULE_KEEPINVENTORY);
            case "doMobSpawning" -> rules.getBoolean(GameRules.RULE_DOMOBSPAWNING);
            case "doFireTick" -> rules.getBoolean(GameRules.RULE_DOFIRETICK);
            case "mobGriefing" -> rules.getBoolean(GameRules.RULE_MOBGRIEFING);
            case "doImmediateRespawn" -> rules.getBoolean(GameRules.RULE_DO_IMMEDIATE_RESPAWN);
            default -> null;
        };
    }

    public void setGameRule(String name, Object value) {
        trackIfSandboxed();
        GameRules rules = server.overworld().getGameRules();
        switch (name) {
            case "doDaylightCycle": rules.getRule(GameRules.RULE_DAYLIGHT).set(Box3ScriptUtils.coerceBool(value), server); break;
            case "doWeatherCycle": rules.getRule(GameRules.RULE_WEATHER_CYCLE).set(Box3ScriptUtils.coerceBool(value), server); break;
            case "keepInventory": rules.getRule(GameRules.RULE_KEEPINVENTORY).set(Box3ScriptUtils.coerceBool(value), server); break;
            case "doMobSpawning": rules.getRule(GameRules.RULE_DOMOBSPAWNING).set(Box3ScriptUtils.coerceBool(value), server); break;
            case "doFireTick": rules.getRule(GameRules.RULE_DOFIRETICK).set(Box3ScriptUtils.coerceBool(value), server); break;
            case "mobGriefing": rules.getRule(GameRules.RULE_MOBGRIEFING).set(Box3ScriptUtils.coerceBool(value), server); break;
            case "doImmediateRespawn": rules.getRule(GameRules.RULE_DO_IMMEDIATE_RESPAWN).set(Box3ScriptUtils.coerceBool(value), server); break;
        }
    }

    // ---- Spawn ----

    public GameVector3 getSpawnPoint() {
        var pos = server.overworld().getSharedSpawnPos();
        return new GameVector3(pos.getX(), pos.getY(), pos.getZ());
    }
    public void setWorldSpawn(GameVector3 pos) {
        server.overworld().setDefaultSpawnPos(new BlockPos((int) pos.x, (int) pos.y, (int) pos.z), 0);
    }

    // ---- Entity spawning ----

    public Box3JSEntity spawnEntity(String type, GameVector3 pos) {
        EntityType<?> eType = Box3ScriptUtils.lookupEntityType(type);
        if (eType == null) return null;
        Entity entity = eType.create(server.overworld());
        if (entity == null) return null;
        entity.setPos(pos.x, pos.y, pos.z);
        server.overworld().addFreshEntity(entity);
        engine.getSandbox().trackEntity(engine.getCurrentProject(), entity);
        return new Box3JSEntity(entity, server, engine);
    }

    // ---- createEntity(config) ----

    public Box3JSEntity createEntity(NativeObject config) {
        String type = config.containsKey("type") ? config.get("type").toString() : "minecraft:pig";
        EntityType<?> eType = Box3ScriptUtils.lookupEntityType(type);
        if (eType == null) return null;
        Entity entity = eType.create(server.overworld());
        if (entity == null) return null;

        GameVector3 pos = config.containsKey("position") ? (GameVector3) config.get("position")
            : new GameVector3(0, 0, 0);
        entity.setPos(pos.x, pos.y, pos.z);
        server.overworld().addFreshEntity(entity);
        engine.getSandbox().trackEntity(engine.getCurrentProject(), entity);
        Box3JSEntity be = new Box3JSEntity(entity, server, engine);

        if (config.containsKey("velocity")) {
            GameVector3 v = (GameVector3) config.get("velocity");
            if (v != null) entity.setDeltaMovement(v.x, v.y, v.z);
        }
        if (config.containsKey("fixed")) be.setFixed(Box3ScriptUtils.coerceBool(config.get("fixed")));
        if (config.containsKey("gravity")) be.setGravity(Box3ScriptUtils.coerceBool(config.get("gravity")));
        if (config.containsKey("friction")) be.setFriction(((Number) config.get("friction")).doubleValue());
        if (config.containsKey("mass")) be.setMass(((Number) config.get("mass")).doubleValue());
        if (config.containsKey("restitution")) be.setRestitution(((Number) config.get("restitution")).doubleValue());
        if (config.containsKey("collides")) be.setCollides(Box3ScriptUtils.coerceBool(config.get("collides")));
        if (config.containsKey("meshInvisible")) be.setMeshInvisible(Box3ScriptUtils.coerceBool(config.get("meshInvisible")));
        if (config.containsKey("hp")) be.setHp(((Number) config.get("hp")).doubleValue());
        if (config.containsKey("maxHp")) be.setMaxHp(((Number) config.get("maxHp")).doubleValue());
        if (config.containsKey("tags")) {
            Object tags = config.get("tags");
            if (tags instanceof NativeArray arr) {
                for (int i = 0; i < arr.getLength(); i++) be.addTag(arr.get(i).toString());
            }
        }
        return be;
    }

    // ---- sound(config) ----

    public void sound(Object cfg) {
        if (cfg instanceof String path) {
            playSound(path, 0, 0, 0, 1.0, 1.0);
        } else if (cfg instanceof NativeObject obj) {
            String path = obj.containsKey("path") ? obj.get("path").toString() : "";
            double x = 0, y = 0, z = 0;
            if (obj.containsKey("position")) {
                GameVector3 pos = (GameVector3) obj.get("position");
                x = pos.x; y = pos.y; z = pos.z;
            }
            double vol = obj.containsKey("volume") ? ((Number) obj.get("volume")).doubleValue() : 1.0;
            double pitch = obj.containsKey("pitch") ? ((Number) obj.get("pitch")).doubleValue() : 1.0;
            playSound(path, x, y, z, vol, pitch);
        }
    }

    // ---- searchBox(bounds) ----

    public List<Box3JSEntity> searchBox(GameBounds3 bounds) {
        return entitiesInArea(bounds.lo, bounds.hi);
    }

    // ---- Sound properties ----

    public String getAmbientSound() { return ambientSound; }
    public void setAmbientSound(String path) { ambientSound = path; }

    public String getPlayerJoinSound() { return playerJoinSound; }
    public void setPlayerJoinSound(String path) { playerJoinSound = path; }

    public String getPlayerLeaveSound() { return playerLeaveSound; }
    public void setPlayerLeaveSound(String path) { playerLeaveSound = path; }

    public String getPlaceVoxelSound() { return placeVoxelSound; }
    public void setPlaceVoxelSound(String path) { placeVoxelSound = path; }

    public String getBreakVoxelSound() { return breakVoxelSound; }
    public void setBreakVoxelSound(String path) { breakVoxelSound = path; }

    private String ambientSound, playerJoinSound, playerLeaveSound, placeVoxelSound, breakVoxelSound;
    private long lastAmbientPlayTick;

    void tickAmbientSound(long currentTick) {
        if (ambientSound != null && !ambientSound.isEmpty() && currentTick - lastAmbientPlayTick >= 200) {
            lastAmbientPlayTick = currentTick;
            var pos = server.overworld().getSharedSpawnPos();
            playSound(ambientSound, pos.getX(), pos.getY(), pos.getZ(), 0.3, 1.0);
        }
    }

    // ---- Events ----

    public GameEventHandlerToken onTick(Function handler) {
        return new GameEventHandlerToken(engine.addTickCallback(() -> {
            long tick = server.getTickCount();
            long prevTick = engine.getPrevTick();
            NativeObject info = new NativeObject();
            ScriptableObject.putProperty(info, "tick", tick);
            ScriptableObject.putProperty(info, "prevTick", prevTick);
            ScriptableObject.putProperty(info, "elapsedTimeMS", tick * 50);
            ScriptableObject.putProperty(info, "skip", 0);
            engine.callFunction(handler, info);
        }));
    }
    public GameEventHandlerToken onPlayerJoin(Function handler) {
        return new GameEventHandlerToken(engine.addJoinCallback((entity, tick) -> engine.callFunction(handler, entity, tick)));
    }
    public GameEventHandlerToken onPlayerLeave(Function handler) {
        return new GameEventHandlerToken(engine.addLeaveCallback((entity, tick) -> engine.callFunction(handler, entity, tick)));
    }
    public GameEventHandlerToken onVoxelDestroy(Function handler) {
        return new GameEventHandlerToken(engine.addVoxelDestroyCallback((entity, x, y, z, voxel, tick) ->
            engine.callFunction(handler, entity, x, y, z, voxel, tick)));
    }
    public GameEventHandlerToken onVoxelContact(Function handler) {
        return new GameEventHandlerToken(engine.addVoxelContactCallback((entity, voxel, x, y, z, axis, force, tick) ->
            engine.callFunction(handler, entity, voxel, x, y, z, axis, force, tick)));
    }
    public GameEventHandlerToken onInteract(Function handler) {
        return new GameEventHandlerToken(engine.addInteractCallback((entity, target, tick) ->
            engine.callFunction(handler, entity, target, tick)));
    }
    public GameEventHandlerToken onChat(Function handler) {
        return new GameEventHandlerToken(engine.addChatCallback((entity, message, tick) ->
            engine.callFunction(handler, entity, message, tick)));
    }
    public GameEventHandlerToken onFluidEnter(Function handler) {
        return new GameEventHandlerToken(engine.addFluidEnterCallback((entity, fluid, x, y, z, tick) ->
            engine.callFunction(handler, entity, fluid, x, y, z, tick)));
    }
    public GameEventHandlerToken onFluidLeave(Function handler) {
        return new GameEventHandlerToken(engine.addFluidLeaveCallback((entity, fluid, x, y, z, tick) ->
            engine.callFunction(handler, entity, fluid, x, y, z, tick)));
    }
    public GameEventHandlerToken onEntityContact(Function handler) {
        return new GameEventHandlerToken(engine.addEntityContactCallback((entity, other, tick) ->
            engine.callFunction(handler, entity, other, tick)));
    }
    public GameEventHandlerToken onEntitySeparate(Function handler) {
        return new GameEventHandlerToken(engine.addEntitySeparateCallback((entity, other, tick) ->
            engine.callFunction(handler, entity, other, tick)));
    }
    public GameEventHandlerToken onBlockPlace(Function handler) {
        return new GameEventHandlerToken(engine.addBlockPlaceCallback((entity, x, y, z, voxel, voxelId, tick) ->
            engine.callFunction(handler, entity, x, y, z, voxel, voxelId, tick)));
    }
    public GameEventHandlerToken onEntityDeath(Function handler) {
        return new GameEventHandlerToken(engine.addEntityDeathCallback((entity, killer, tick) ->
            engine.callFunction(handler, entity, killer, tick)));
    }
    public GameEventHandlerToken onPlayerRespawn(Function handler) {
        return new GameEventHandlerToken(engine.addRespawnCallback((entity, tick) -> engine.callFunction(handler, entity, tick)));
    }
    public GameEventHandlerToken onBlockActivate(Function handler) {
        return new GameEventHandlerToken(engine.addBlockActivateCallback((entity, x, y, z, voxel, tick) ->
            engine.callFunction(handler, entity, x, y, z, voxel, tick)));
    }
    public GameEventHandlerToken onEntityDamage(Function handler) {
        return new GameEventHandlerToken(engine.addEntityDamageCallback((entity, amount, source, attacker, tick) ->
            engine.callFunction(handler, entity, amount, source, attacker, tick)));
    }
    public GameEventHandlerToken onButtonPressed(Function handler) {
        return new GameEventHandlerToken(engine.addButtonPressedCallback((entity, button, tick) ->
            engine.callFunction(handler, entity, button, tick)));
    }
    public GameEventHandlerToken onMessage(Function handler) {
        String project = engine.getCurrentProject();
        if (project != null) {
            return new GameEventHandlerToken(engine.addMessageCallback(project, (from, d) -> engine.callFunction(handler, from, d)));
        }
        return new GameEventHandlerToken(() -> {});
    }

    // ---- Entity Query ----

    public List<Box3JSEntity> querySelectorAll(String selector) { return query.querySelectorAll(selector); }
    public Box3JSEntity querySelector(String selector) { return query.querySelector(selector); }

    // ---- Chat ----

    public void say(String message) {
        server.getPlayerList().broadcastSystemMessage(net.minecraft.network.chat.Component.literal(message), false);
    }

    // ---- Timers ----

    public int setTimeout(Function handler, int ticks) { return engine.scheduleTimeout(handler, ticks); }
    public int setInterval(Function handler, int ticks) { return engine.scheduleInterval(handler, ticks); }
    public void clearTimeout(int id) { engine.clearTimer(id); }
    public void clearInterval(int id) { engine.clearTimer(id); }

    // ---- Command ----

    public void runCommand(String cmd) {
        server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), cmd);
    }

    // ---- Scoreboard ----

    public void addScoreboard(String name) { scoreboard.addScoreboard(engine.getCurrentProject(), name); }
    public void addScoreboard(String name, String criteria) { scoreboard.addScoreboard(engine.getCurrentProject(), name, criteria); }
    public void removeScoreboard(String name) { scoreboard.removeScoreboard(name); }
    public void setScore(Object entityOrName, String objectiveName, int value) { scoreboard.setScore(entityOrName, objectiveName, value); }
    public int getScore(Object entityOrName, String objectiveName) { return scoreboard.getScore(entityOrName, objectiveName); }
    public void showScoreboard(String slot, String objectiveName) { scoreboard.showScoreboard(slot, objectiveName); }
    public void hideScoreboard(String slot) { scoreboard.hideScoreboard(slot); }
    public java.util.List<org.mozilla.javascript.NativeObject> listScores(String objectiveName) { return scoreboard.listScores(objectiveName); }

    // ---- Boss Bar ----

    public void showBossbar(String name, String text, double progress, String colorName) { bossbar.showBossbar(engine.getCurrentProject(), name, text, progress, colorName); }
    public void removeBossbar(String name) { bossbar.removeBossbar(engine.getCurrentProject(), name); }

    // ---- Team ----

    public void createTeam(String name, String colorName) { team.createTeam(engine.getCurrentProject(), name, colorName); }
    public void removeTeam(String name) { team.removeTeam(name); }
    public void joinTeam(Object entityOrName, String teamName) { team.joinTeam(entityOrName, teamName); }
    public void leaveTeam(Object entityOrName) { team.leaveTeam(entityOrName); }
    public String getTeamOf(Object entityOrName) { return team.getTeamOf(entityOrName); }

    // ---- World Border ----

    public double getBorderSize() { return server.overworld().getWorldBorder().getSize(); }
    public void setBorderCenter(double x, double z) { trackIfSandboxed(); server.overworld().getWorldBorder().setCenter(x, z); }
    public void setBorderSize(double size) { trackIfSandboxed(); server.overworld().getWorldBorder().setSize(size); }
    public void shrinkBorder(double targetSize, double seconds) {
        trackIfSandboxed();
        WorldBorder border = server.overworld().getWorldBorder();
        border.lerpSizeBetween(border.getSize(), targetSize, (long)(seconds * 1000));
    }
    public void setBorderDamage(double damage) { trackIfSandboxed(); server.overworld().getWorldBorder().setDamagePerBlock(damage); }
    public void setBorderWarning(int blocks) { trackIfSandboxed(); server.overworld().getWorldBorder().setWarningBlocks(blocks); }

    // ---- Lightning ----

    public boolean strikeLightning(double x, double y, double z) {
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(server.overworld());
        if (bolt == null) return false;
        bolt.moveTo(x, y, z);
        bolt.setVisualOnly(false);
        server.overworld().addFreshEntity(bolt);
        return true;
    }
    public boolean strikeLightning(GameVector3 pos) { return strikeLightning(pos.x, pos.y, pos.z); }
    public boolean strikeLightning(double x, double y, double z, double damage) {
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(server.overworld());
        if (bolt == null) return false;
        bolt.moveTo(x, y, z);
        bolt.setDamage((float) damage);
        bolt.setVisualOnly(false);
        server.overworld().addFreshEntity(bolt);
        return true;
    }
    public boolean strikeLightning(GameVector3 pos, double damage) { return strikeLightning(pos.x, pos.y, pos.z, damage); }

    // ---- Projectile ----

    public Box3JSEntity launchProjectile(String type, double x, double y, double z,
                                          double tx, double ty, double tz, double speed) {
        EntityType<?> eType = Box3ScriptUtils.lookupEntityType(type);
        if (eType == null) return null;
        Entity entity = eType.create(server.overworld());
        if (entity == null) return null;
        entity.moveTo(x, y, z);
        double dx = tx - x, dy = ty - y, dz = tz - z;
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (dist > 0.001) {
            double s = speed / dist;
            entity.setDeltaMovement(dx * s, dy * s, dz * s);
        }
        if (entity instanceof net.minecraft.world.entity.projectile.Projectile proj) {
            proj.shoot(dx, dy, dz, (float) speed, 0);
        }
        server.overworld().addFreshEntity(entity);
        return new Box3JSEntity(entity, server, engine);
    }
    public Box3JSEntity launchProjectile(String type, GameVector3 pos, GameVector3 target, double speed) {
        return launchProjectile(type, pos.x, pos.y, pos.z, target.x, target.y, target.z, speed);
    }

    // ---- Firework ----

    public void launchFirework(double x, double y, double z, String color, String shape) {
        int colorInt = switch (color != null ? color.toLowerCase(java.util.Locale.ROOT) : "") {
            case "red" -> 0xFF0000;
            case "blue" -> 0x0000FF;
            case "green", "lime" -> 0x00FF00;
            case "yellow" -> 0xFFFF00;
            case "gold", "orange" -> 0xFFAA00;
            case "white" -> 0xFFFFFF;
            case "aqua", "cyan" -> 0x00FFFF;
            case "pink", "magenta" -> 0xFF00FF;
            case "purple" -> 0xAA00FF;
            default -> 0xFFFFFF;
        };
        FireworkExplosion.Shape fireworkShape = switch (shape != null ? shape.toLowerCase() : "ball") {
            case "large_ball" -> FireworkExplosion.Shape.LARGE_BALL;
            case "star" -> FireworkExplosion.Shape.STAR;
            case "creeper" -> FireworkExplosion.Shape.CREEPER;
            case "burst" -> FireworkExplosion.Shape.BURST;
            default -> FireworkExplosion.Shape.SMALL_BALL;
        };
        var explosion = new FireworkExplosion(fireworkShape,
            new it.unimi.dsi.fastutil.ints.IntArrayList(new int[]{colorInt}),
            new it.unimi.dsi.fastutil.ints.IntArrayList(new int[]{colorInt}),
            false, true);
        var fireworks = new Fireworks(1, java.util.List.of(explosion));
        ItemStack rocket = new ItemStack(Items.FIREWORK_ROCKET);
        rocket.set(DataComponents.FIREWORKS, fireworks);
        var entity = new net.minecraft.world.entity.projectile.FireworkRocketEntity(server.overworld(), x, y, z, rocket);
        server.overworld().addFreshEntity(entity);
    }
    public void launchFirework(GameVector3 pos, String color, String shape) {
        launchFirework(pos.x, pos.y, pos.z, color, shape);
    }

    public void launchFirework(double x, double y, double z, GameRGBColor[] colors, String shape) {
        var colorInts = new it.unimi.dsi.fastutil.ints.IntArrayList();
        for (GameRGBColor c : colors) {
            int r = (int) (Math.max(0, Math.min(1, c.r)) * 255);
            int g = (int) (Math.max(0, Math.min(1, c.g)) * 255);
            int b = (int) (Math.max(0, Math.min(1, c.b)) * 255);
            colorInts.add(0xFF000000 | (r << 16) | (g << 8) | b);
        }
        if (colorInts.isEmpty()) colorInts.add(0xFFFFFFFF);

        FireworkExplosion.Shape fireworkShape = switch (shape != null ? shape.toLowerCase(java.util.Locale.ROOT) : "ball") {
            case "large_ball" -> FireworkExplosion.Shape.LARGE_BALL;
            case "star" -> FireworkExplosion.Shape.STAR;
            case "creeper" -> FireworkExplosion.Shape.CREEPER;
            case "burst" -> FireworkExplosion.Shape.BURST;
            default -> FireworkExplosion.Shape.SMALL_BALL;
        };
        var explosion = new FireworkExplosion(fireworkShape, colorInts, colorInts, false, true);
        var fireworks = new Fireworks(1, java.util.List.of(explosion));
        ItemStack rocket = new ItemStack(Items.FIREWORK_ROCKET);
        rocket.set(DataComponents.FIREWORKS, fireworks);
        var entity = new net.minecraft.world.entity.projectile.FireworkRocketEntity(server.overworld(), x, y, z, rocket);
        server.overworld().addFreshEntity(entity);
    }
    public void launchFirework(GameVector3 pos, GameRGBColor[] colors, String shape) {
        launchFirework(pos.x, pos.y, pos.z, colors, shape);
    }

    // ---- Particle ----

    public void spawnParticle(String type, double x, double y, double z, int count, double dx, double dy, double dz, double speed) {
        var particle = Box3ScriptUtils.lookupParticle(type);
        if (particle != null) server.overworld().sendParticles(particle, x, y, z, count, dx, dy, dz, speed);
    }
    public void spawnParticle(String type, GameVector3 pos, int count, double dx, double dy, double dz, double speed) {
        spawnParticle(type, pos.x, pos.y, pos.z, count, dx, dy, dz, speed);
    }
    public void spawnParticle(double x, double y, double z, GameRGBColor color, int count, double dx, double dy, double dz, double speed) {
        server.overworld().sendParticles(new DustParticleOptions(new org.joml.Vector3f((float) color.r, (float) color.g, (float) color.b), 1.0f), x, y, z, count, dx, dy, dz, speed);
    }
    public void spawnParticle(GameVector3 pos, GameRGBColor color, int count, double dx, double dy, double dz, double speed) {
        spawnParticle(pos.x, pos.y, pos.z, color, count, dx, dy, dz, speed);
    }
    public void spawnParticleCircle(double x, double y, double z, double radius, String type, int count) {
        var particle = Box3ScriptUtils.lookupParticle(type);
        if (particle == null) return;
        for (int i = 0; i < count; i++) {
            double angle = (2.0 * Math.PI * i) / count;
            server.overworld().sendParticles(particle, x + Math.cos(angle) * radius, y, z + Math.sin(angle) * radius, 1, 0, 0, 0, 0);
        }
    }
    public void spawnParticleCircle(GameVector3 pos, double radius, String type, int count) {
        spawnParticleCircle(pos.x, pos.y, pos.z, radius, type, count);
    }

    // ---- Drop Item ----

    public void dropItem(double x, double y, double z, String itemId, int count) {
        var item = Box3ScriptUtils.lookupItem(itemId);
        if (item == null) return;
        ItemStack stack = new ItemStack(item, Math.max(1, count));
        server.overworld().addFreshEntity(new ItemEntity(server.overworld(), x, y, z, stack));
    }
    public void dropItem(GameVector3 pos, String itemId, int count) {
        dropItem(pos.x, pos.y, pos.z, itemId, count);
    }

    // ---- Query ----

    public Object raycast(GameVector3 origin, GameVector3 direction) { return query.raycast(origin, direction); }
    public Object raycast(GameVector3 origin, GameVector3 direction, double maxDistance) { return query.raycast(origin, direction, maxDistance); }
    public List<Box3JSEntity> entitiesInArea(GameVector3 pos1, GameVector3 pos2) { return query.entitiesInArea(pos1, pos2); }
    public List<Box3JSEntity> entitiesInRadius(double x, double y, double z, double radius) { return query.entitiesInRadius(x, y, z, radius); }
    public List<Box3JSEntity> entitiesInRadius(GameVector3 pos, double radius) { return query.entitiesInRadius(pos, radius); }
    public String getBiome(int x, int y, int z) { return query.getBiome(x, y, z); }
    public String getBiome(GameVector3 pos) { return query.getBiome(pos); }

    // ---- Explode ----

    public void explode(double x, double y, double z, double power) { explode(x, y, z, power, false); }
    public void explode(GameVector3 pos, double power) { explode(pos.x, pos.y, pos.z, power, false); }
    public void explode(double x, double y, double z, double power, boolean fire) {
        server.overworld().explode(null, x, y, z, (float) power, fire, Level.ExplosionInteraction.BLOCK);
    }
    public void explode(GameVector3 pos, double power, boolean fire) { explode(pos.x, pos.y, pos.z, power, fire); }

    // ---- Sound ----

    public void playSound(String path, double x, double y, double z, double volume, double pitch) {
        var sound = Box3ScriptUtils.lookupSoundEvent(path);
        if (sound == null) return;
        var packet = new ClientboundSoundPacket(sound, SoundSource.PLAYERS, x, y, z, (float) volume, (float) pitch, server.overworld().getRandom().nextLong());
        for (var sp : server.getPlayerList().getPlayers()) sp.connection.send(packet);
    }
    public void playSound(String path, GameVector3 pos, double volume, double pitch) {
        playSound(path, pos.x, pos.y, pos.z, volume, pitch);
    }

    // ---- Structure ----

    public void placeStructure(double x, double y, double z, String structureId) {
        ResourceLocation rl = ResourceLocation.tryParse(structureId);
        if (rl == null) return;
        server.overworld().getStructureManager().get(rl).ifPresent(template -> {
            template.placeInWorld(server.overworld(),
                new BlockPos(0, 0, 0),
                new BlockPos((int) x, (int) y, (int) z),
                new StructurePlaceSettings().setKnownShape(true),
                server.overworld().getRandom(), 3);
        });
    }
    public void placeStructure(GameVector3 pos, String structureId) {
        placeStructure(pos.x, pos.y, pos.z, structureId);
    }

    // ---- Advancement ----

    public void grantAdvancement(String playerName, String advancementId) {
        ServerPlayer sp = server.getPlayerList().getPlayerByName(playerName);
        if (sp == null) return;
        ResourceLocation rl = ResourceLocation.tryParse(advancementId);
        if (rl == null) return;
        var holder = server.getAdvancements().get(rl);
        if (holder != null) {
            for (String criterion : holder.value().criteria().keySet()) {
                sp.getAdvancements().award(holder, criterion);
            }
        }
    }

    // ---- Recipe ----

    public List<String> listRecipes(String filter) {
        return Box3JSRecipeManager.listRecipes(filter != null ? filter : "");
    }

    public boolean removeRecipe(String recipeId) {
        return Box3JSRecipeManager.removeRecipe(recipeId);
    }

    public void clearRecipes() {
        Box3JSRecipeManager.clearRecipes();
    }

    // ---- Message ----

    public void sendMessage(String target, Object data) {
        engine.fireMessage(engine.getCurrentProject(), target, data);
    }
}

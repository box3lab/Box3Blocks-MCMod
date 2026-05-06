package com.box3lab.box3js.script;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
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
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.storage.ServerLevelData;
import org.mozilla.javascript.Function;

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

    public String projectName() { return server.getMotd(); }

    public int currentTick() { return server.getTickCount(); }

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

    // ---- Events ----

    public void onTick(Function handler) {
        engine.addTickCallback(() -> engine.callFunction(handler));
    }
    public void onPlayerJoin(Function handler) {
        engine.addJoinCallback(entity -> engine.callFunction(handler, entity));
    }
    public void onPlayerLeave(Function handler) {
        engine.addLeaveCallback(entity -> engine.callFunction(handler, entity));
    }
    public void onVoxelDestroy(Function handler) {
        engine.addVoxelDestroyCallback((entity, x, y, z, voxel, tick) ->
            engine.callFunction(handler, entity, x, y, z, voxel, tick));
    }
    public void onVoxelContact(Function handler) {
        engine.addVoxelContactCallback((entity, voxel, x, y, z, axis, force, tick) ->
            engine.callFunction(handler, entity, voxel, x, y, z, axis, force, tick));
    }
    public void onInteract(Function handler) {
        engine.addInteractCallback((entity, target, tick) ->
            engine.callFunction(handler, entity, target, tick));
    }
    public void onChat(Function handler) {
        engine.addChatCallback((entity, message, tick) ->
            engine.callFunction(handler, entity, message, tick));
    }
    public void onFluidEnter(Function handler) {
        engine.addFluidEnterCallback((entity, fluid, x, y, z, tick) ->
            engine.callFunction(handler, entity, fluid, x, y, z, tick));
    }
    public void onFluidLeave(Function handler) {
        engine.addFluidLeaveCallback((entity, fluid, x, y, z, tick) ->
            engine.callFunction(handler, entity, fluid, x, y, z, tick));
    }
    public void onEntityContact(Function handler) {
        engine.addEntityContactCallback((entity, other, tick) ->
            engine.callFunction(handler, entity, other, tick));
    }
    public void onEntitySeparate(Function handler) {
        engine.addEntitySeparateCallback((entity, other, tick) ->
            engine.callFunction(handler, entity, other, tick));
    }
    public void onBlockPlace(Function handler) {
        engine.addBlockPlaceCallback((entity, x, y, z, voxel, voxelId, tick) ->
            engine.callFunction(handler, entity, x, y, z, voxel, voxelId, tick));
    }
    public void onEntityDeath(Function handler) {
        engine.addEntityDeathCallback((entity, killer, tick) ->
            engine.callFunction(handler, entity, killer, tick));
    }
    public void onPlayerRespawn(Function handler) {
        engine.addRespawnCallback(entity -> engine.callFunction(handler, entity));
    }
    public void onBlockActivate(Function handler) {
        engine.addBlockActivateCallback((entity, x, y, z, voxel, tick) ->
            engine.callFunction(handler, entity, x, y, z, voxel, tick));
    }
    public void onEntityDamage(Function handler) {
        engine.addEntityDamageCallback((entity, amount, source, attacker, tick) ->
            engine.callFunction(handler, entity, amount, source, attacker, tick));
    }
    public void onMessage(Function handler) {
        String project = engine.getCurrentProject();
        if (project != null) {
            engine.addMessageCallback(project, (from, d) -> engine.callFunction(handler, from, d));
        }
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

    // ---- Particle ----

    public void spawnParticle(String type, double x, double y, double z, int count, double dx, double dy, double dz, double speed) {
        var particle = Box3ScriptUtils.lookupParticle(type);
        if (particle != null) server.overworld().sendParticles(particle, x, y, z, count, dx, dy, dz, speed);
    }
    public void spawnParticle(String type, GameVector3 pos, int count, double dx, double dy, double dz, double speed) {
        spawnParticle(type, pos.x, pos.y, pos.z, count, dx, dy, dz, speed);
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

    // ---- Message ----

    public void sendMessage(String target, Object data) {
        engine.fireMessage(engine.getCurrentProject(), target, data);
    }
}

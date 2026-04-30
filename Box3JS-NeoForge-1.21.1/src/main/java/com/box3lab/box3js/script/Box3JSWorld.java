package com.box3lab.box3js.script;

import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.ScriptableObject;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ScoreAccess;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.world.BossEvent.BossBarColor;
import net.minecraft.world.BossEvent.BossBarOverlay;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.mozilla.javascript.Function;

import java.util.*;

public class Box3JSWorld {

    private final MinecraftServer server;
    private final Box3ScriptEngine engine;
    private String projectName;
    private final Map<String, ServerBossEvent> bossBars = new HashMap<>();

    public final ScoreboardNS scoreboard;
    public final BossBarNS bossbar;
    public final TeamNS team;
    public final BorderNS border;
    public final LightningNS lightning;
    public final FireworkNS firework;
    public final ParticleNS particle;
    public final DropNS drop;
    public final QueryNS query;
    public final EffectNS effect;
    public final SoundNS sound;
    public final MessageNS message;

    public Box3JSWorld(MinecraftServer server, Box3ScriptEngine engine) {
        this.server = server;
        this.engine = engine;
        this.scoreboard = new ScoreboardNS(server);
        this.bossbar = new BossBarNS(server, bossBars);
        this.team = new TeamNS(server);
        this.border = new BorderNS(server);
        this.lightning = new LightningNS(server);
        this.firework = new FireworkNS(server);
        this.particle = new ParticleNS(server);
        this.drop = new DropNS(server);
        this.query = new QueryNS(server, engine);
        this.effect = new EffectNS(server);
        this.sound = new SoundNS(server);
        this.message = new MessageNS(engine);
    }

    public void setProjectName(String name) { this.projectName = name; }

    // ---- World properties ----

    public String projectName() { return server.getMotd(); }

    public int currentTick() { return server.getTickCount(); }

    public double getRainDensity() {
        return server.overworld().getRainLevel(1.0f);
    }
    public void setRainDensity(double v) {
        server.overworld().getLevelData().setRaining(v > 0);
    }

    public double getThunderDensity() {
        return server.overworld().getThunderLevel(1.0f);
    }
    public void setThunderDensity(double v) {
        ((ServerLevelData) server.overworld().getLevelData()).setThundering(v > 0);
    }

    public void clearWeather() {
        var level = server.overworld();
        level.getLevelData().setRaining(false);
        ((ServerLevelData) level.getLevelData()).setThundering(false);
    }

    // ---- Time ----

    public long getTime() { return server.overworld().getDayTime(); }
    public void setTime(long tick) { server.overworld().setDayTime(tick); }

    public double getTimeScale() {
        return server.overworld().getGameRules().getBoolean(GameRules.RULE_DAYLIGHT) ? 1.0 : 0.0;
    }
    public void setTimeScale(double v) {
        server.overworld().getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(v > 0, server);
    }

    // ---- Difficulty ----

    public String getDifficulty() {
        return server.overworld().getDifficulty().getKey();
    }
    public void setDifficulty(Object v) {
        Difficulty diff;
        if (v instanceof Number n) {
            diff = Difficulty.byId(n.intValue());
        } else {
            diff = Difficulty.byName(v.toString());
        }
        if (diff != null) server.setDifficulty(diff, true);
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
        ResourceLocation rl = ResourceLocation.tryParse(type);
        if (rl == null) return null;
        var opt = BuiltInRegistries.ENTITY_TYPE.getOptional(rl);
        if (opt.isEmpty()) return null;
        Entity entity = opt.get().create(server.overworld());
        if (entity == null) return null;
        entity.setPos(pos.x, pos.y, pos.z);
        server.overworld().addFreshEntity(entity);
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
        engine.addRespawnCallback(entity ->
            engine.callFunction(handler, entity));
    }

    public void onBlockActivate(Function handler) {
        engine.addBlockActivateCallback((entity, x, y, z, voxel, tick) ->
            engine.callFunction(handler, entity, x, y, z, voxel, tick));
    }

    public void onEntityDamage(Function handler) {
        engine.addEntityDamageCallback((entity, amount, source, attacker, tick) ->
            engine.callFunction(handler, entity, amount, source, attacker, tick));
    }

    // ---- Entity Query ----

    public List<Box3JSEntity> querySelectorAll(String selector) {
        List<Box3JSEntity> result = new ArrayList<>();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            Box3JSEntity e = new Box3JSEntity(player, server, engine);
            if (matchesSelector(e, selector)) result.add(e);
        }
        return result;
    }

    public Box3JSEntity querySelector(String selector) {
        List<Box3JSEntity> all = querySelectorAll(selector);
        return all.isEmpty() ? null : all.get(0);
    }

    private boolean matchesSelector(Box3JSEntity entity, String selector) {
        if (selector.equals("*") || selector.equals("player")) return entity.isPlayer();
        if (selector.startsWith("#")) {
            String id = selector.substring(1);
            return id.equals(entity.getId());
        }
        if (selector.startsWith(".")) {
            String tag = selector.substring(1);
            return entity.hasTag(tag);
        }
        return false;
    }

    // ---- Chat ----

    public void say(String message) {
        server.getPlayerList().broadcastSystemMessage(
                net.minecraft.network.chat.Component.literal(message), false);
    }

    // ---- Timers ----

    public int setTimeout(Function handler, int ticks) {
        return engine.scheduleTimeout(handler, ticks);
    }

    public int setInterval(Function handler, int ticks) {
        return engine.scheduleInterval(handler, ticks);
    }

    public void clearTimeout(int id) {
        engine.clearTimer(id);
    }

    public void clearInterval(int id) {
        engine.clearTimer(id);
    }

    // ---- Command ----

    public void runCommand(String cmd) {
        CommandSourceStack source = server.createCommandSourceStack();
        server.getCommands().performPrefixedCommand(source, cmd);
    }

    private static String resolveScoreName(Object entityOrName) {
        if (entityOrName instanceof String s) return s;
        if (entityOrName instanceof Box3JSEntity e) return e.getEntity().getScoreboardName();
        if (entityOrName instanceof ServerPlayer sp) return sp.getScoreboardName();
        return null;
    }

    // ---- Callback interfaces ----

    @FunctionalInterface
    public interface PlayerJoinCallback {
        void onJoin(Box3JSEntity entity);
    }

    @FunctionalInterface
    public interface PlayerLeaveCallback {
        void onLeave(Box3JSEntity entity);
    }

    @FunctionalInterface
    public interface VoxelDestroyCallback {
        void onDestroy(Box3JSEntity entity, int x, int y, int z, String voxel, long tick);
    }

    @FunctionalInterface
    public interface VoxelContactCallback {
        void onContact(Box3JSEntity entity, int voxel, int x, int y, int z, int axis, double force, long tick);
    }

    @FunctionalInterface
    public interface InteractCallback {
        void onInteract(Box3JSEntity entity, Box3JSEntity target, long tick);
    }

    @FunctionalInterface
    public interface ChatCallback {
        void onChat(Box3JSEntity entity, String message, long tick);
    }

    @FunctionalInterface
    public interface FluidEnterCallback {
        void onEnter(Box3JSEntity entity, String fluid, int x, int y, int z, long tick);
    }

    @FunctionalInterface
    public interface FluidLeaveCallback {
        void onLeave(Box3JSEntity entity, String fluid, int x, int y, int z, long tick);
    }

    @FunctionalInterface
    public interface EntityContactCallback {
        void onContact(Box3JSEntity entity, Box3JSEntity other, long tick);
    }

    @FunctionalInterface
    public interface EntitySeparateCallback {
        void onSeparate(Box3JSEntity entity, Box3JSEntity other, long tick);
    }

    @FunctionalInterface
    public interface BlockPlaceCallback {
        void onPlace(Box3JSEntity entity, int x, int y, int z, String voxel, int voxelId, long tick);
    }

    @FunctionalInterface
    public interface EntityDeathCallback {
        void onDeath(Box3JSEntity entity, Box3JSEntity killer, long tick);
    }

    @FunctionalInterface
    public interface PlayerRespawnCallback {
        void onRespawn(Box3JSEntity entity);
    }

    @FunctionalInterface
    public interface BlockActivateCallback {
        void onActivate(Box3JSEntity entity, int x, int y, int z, String voxel, long tick);
    }

    @FunctionalInterface
    public interface EntityDamageCallback {
        void onDamage(Box3JSEntity entity, double amount, String source, Box3JSEntity attacker, long tick);
    }

    @FunctionalInterface
    public interface MessageCallback {
        void onMessage(String from, Object data);
    }

    // ---- Namespace inner classes ----

    public static class ScoreboardNS {
        private final MinecraftServer server;
        ScoreboardNS(MinecraftServer server) { this.server = server; }

        public void add(String name) { add(name, "dummy"); }
        public void add(String name, String criteria) {
            Scoreboard sb = server.getScoreboard();
            if (sb.getObjective(name) != null) return;
            ObjectiveCriteria crit = "dummy".equals(criteria) || criteria == null
                ? ObjectiveCriteria.DUMMY
                : ObjectiveCriteria.byName(criteria).orElse(ObjectiveCriteria.DUMMY);
            sb.addObjective(name, crit, Component.literal(name), ObjectiveCriteria.RenderType.INTEGER, false, null);
        }
        public void setScore(Object entityOrName, String objectiveName, int value) {
            Scoreboard sb = server.getScoreboard();
            Objective obj = sb.getObjective(objectiveName);
            if (obj == null) return;
            String name = resolveScoreName(entityOrName);
            if (name == null) return;
            sb.getOrCreatePlayerScore(ScoreHolder.forNameOnly(name), obj).set(value);
        }
        public int getScore(Object entityOrName, String objectiveName) {
            Scoreboard sb = server.getScoreboard();
            Objective obj = sb.getObjective(objectiveName);
            if (obj == null) return 0;
            String name = resolveScoreName(entityOrName);
            if (name == null) return 0;
            ScoreAccess access = sb.getOrCreatePlayerScore(ScoreHolder.forNameOnly(name), obj);
            return access.get();
        }
        public void show(String slot, String objectiveName) {
            Scoreboard sb = server.getScoreboard();
            DisplaySlot displaySlot = switch (slot.toLowerCase()) {
                case "list" -> DisplaySlot.LIST;
                case "belowname", "below_name" -> DisplaySlot.BELOW_NAME;
                default -> DisplaySlot.SIDEBAR;
            };
            Objective obj = sb.getObjective(objectiveName);
            sb.setDisplayObjective(displaySlot, obj);
        }
        public void hide(String slot) {
            Scoreboard sb = server.getScoreboard();
            DisplaySlot displaySlot = switch (slot.toLowerCase()) {
                case "list" -> DisplaySlot.LIST;
                case "belowname", "below_name" -> DisplaySlot.BELOW_NAME;
                default -> DisplaySlot.SIDEBAR;
            };
            sb.setDisplayObjective(displaySlot, null);
        }
        public void remove(String name) {
            Scoreboard sb = server.getScoreboard();
            Objective obj = sb.getObjective(name);
            if (obj != null) sb.removeObjective(obj);
        }
        public java.util.List<NativeObject> list(String objectiveName) {
            java.util.List<NativeObject> result = new ArrayList<>();
            Scoreboard sb = server.getScoreboard();
            Objective obj = sb.getObjective(objectiveName);
            if (obj == null) return result;
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                int s = sb.getOrCreatePlayerScore(ScoreHolder.forNameOnly(player.getScoreboardName()), obj).get();
                NativeObject m = new NativeObject();
                ScriptableObject.putProperty(m, "name", player.getScoreboardName());
                ScriptableObject.putProperty(m, "value", s);
                result.add(m);
            }
            return result;
        }
    }

    public static class BossBarNS {
        private final MinecraftServer server;
        private final Map<String, ServerBossEvent> bossBars;
        BossBarNS(MinecraftServer server, Map<String, ServerBossEvent> bossBars) { this.server = server; this.bossBars = bossBars; }

        public void show(String name, String text, double progress, String colorName) {
            ServerBossEvent bar = bossBars.get(name);
            if (bar == null) {
                BossBarColor color = colorName == null ? BossBarColor.WHITE : switch (colorName.toLowerCase(Locale.ROOT)) {
                    case "red" -> BossBarColor.RED;
                    case "blue" -> BossBarColor.BLUE;
                    case "green" -> BossBarColor.GREEN;
                    case "yellow" -> BossBarColor.YELLOW;
                    case "purple" -> BossBarColor.PURPLE;
                    case "pink" -> BossBarColor.PINK;
                    default -> BossBarColor.WHITE;
                };
                bar = new ServerBossEvent(Component.literal(text), color, BossBarOverlay.PROGRESS);
                bossBars.put(name, bar);
            } else {
                bar.setName(Component.literal(text));
                if (colorName != null) bar.setColor(switch (colorName.toLowerCase(Locale.ROOT)) {
                    case "red" -> BossBarColor.RED;
                    case "blue" -> BossBarColor.BLUE;
                    case "green" -> BossBarColor.GREEN;
                    case "yellow" -> BossBarColor.YELLOW;
                    case "purple" -> BossBarColor.PURPLE;
                    case "pink" -> BossBarColor.PINK;
                    default -> BossBarColor.WHITE;
                });
            }
            bar.setProgress((float) Math.max(0, Math.min(1, progress)));
            for (ServerPlayer sp : server.getPlayerList().getPlayers()) bar.addPlayer(sp);
        }
        public void remove(String name) {
            ServerBossEvent bar = bossBars.remove(name);
            if (bar != null) bar.removeAllPlayers();
        }
    }

    public static class TeamNS {
        private final MinecraftServer server;
        TeamNS(MinecraftServer server) { this.server = server; }

        public void create(String name, String colorName) {
            Scoreboard sb = server.getScoreboard();
            if (sb.getPlayerTeam(name) != null) return;
            PlayerTeam team = sb.addPlayerTeam(name);
            ChatFormatting fmt = ChatFormatting.getByName(colorName);
            if (fmt != null) {
                team.setColor(fmt);
                team.setDisplayName(Component.literal(name));
            }
        }
        public void join(Object entityOrName, String teamName) {
            Scoreboard sb = server.getScoreboard();
            PlayerTeam team = sb.getPlayerTeam(teamName);
            if (team == null) return;
            String name = resolveScoreName(entityOrName);
            if (name != null) sb.addPlayerToTeam(name, team);
        }
        public void leave(Object entityOrName) {
            Scoreboard sb = server.getScoreboard();
            String name = resolveScoreName(entityOrName);
            if (name != null) sb.removePlayerFromTeam(name);
        }
        public void remove(String name) {
            Scoreboard sb = server.getScoreboard();
            PlayerTeam team = sb.getPlayerTeam(name);
            if (team != null) sb.removePlayerTeam(team);
        }
        public String of(Object entityOrName) {
            Scoreboard sb = server.getScoreboard();
            String name = resolveScoreName(entityOrName);
            if (name == null) return null;
            PlayerTeam team = sb.getPlayersTeam(name);
            return team != null ? team.getName() : null;
        }
    }

    public static class BorderNS {
        private final MinecraftServer server;
        BorderNS(MinecraftServer server) { this.server = server; }

        public double size() { return server.overworld().getWorldBorder().getSize(); }
        public void center(double x, double z) { server.overworld().getWorldBorder().setCenter(x, z); }
        public void set(double size) { server.overworld().getWorldBorder().setSize(size); }
        public void shrink(double targetSize, double seconds) {
            WorldBorder border = server.overworld().getWorldBorder();
            border.lerpSizeBetween(border.getSize(), targetSize, (long)(seconds * 1000));
        }
        public void damage(double damage) { server.overworld().getWorldBorder().setDamagePerBlock(damage); }
        public void warning(int blocks) { server.overworld().getWorldBorder().setWarningBlocks(blocks); }
    }

    public static class LightningNS {
        private final MinecraftServer server;
        LightningNS(MinecraftServer server) { this.server = server; }

        public boolean strike(double x, double y, double z) {
            ServerLevel level = server.overworld();
            LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
            if (bolt == null) return false;
            bolt.moveTo(x, y, z);
            bolt.setVisualOnly(false);
            level.addFreshEntity(bolt);
            return true;
        }
        public boolean strike(double x, double y, double z, double damage) {
            ServerLevel level = server.overworld();
            LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
            if (bolt == null) return false;
            bolt.moveTo(x, y, z);
            bolt.setDamage((float) damage);
            bolt.setVisualOnly(false);
            level.addFreshEntity(bolt);
            return true;
        }
    }

    public static class FireworkNS {
        private final MinecraftServer server;
        FireworkNS(MinecraftServer server) { this.server = server; }

        public void launch(double x, double y, double z, String color, String shape) {
            ServerLevel level = server.overworld();
            int colorInt = switch (color != null ? color.toLowerCase(Locale.ROOT) : "") {
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
            var entity = new net.minecraft.world.entity.projectile.FireworkRocketEntity(level, x, y, z, rocket);
            level.addFreshEntity(entity);
        }
    }

    public static class ParticleNS {
        private final MinecraftServer server;
        ParticleNS(MinecraftServer server) { this.server = server; }

        public void spawn(String type, double x, double y, double z, int count, double dx, double dy, double dz, double speed) {
            var particle = resolveParticle(type);
            if (particle != null) {
                server.overworld().sendParticles(particle, x, y, z, count, dx, dy, dz, speed);
            }
        }
        public void circle(double x, double y, double z, double radius, String type, int count) {
            var particle = resolveParticle(type);
            if (particle == null) return;
            ServerLevel level = server.overworld();
            for (int i = 0; i < count; i++) {
                double angle = (2.0 * Math.PI * i) / count;
                double px = x + Math.cos(angle) * radius;
                double pz = z + Math.sin(angle) * radius;
                level.sendParticles(particle, px, y, pz, 1, 0, 0, 0, 0);
            }
        }
        private ParticleOptions resolveParticle(String type) {
            ResourceLocation rl = ResourceLocation.tryParse(type);
            if (rl == null) return null;
            var particle = BuiltInRegistries.PARTICLE_TYPE.getOptional(rl);
            if (particle.isEmpty()) return null;
            var p = particle.get();
            if (p instanceof ParticleOptions options) return options;
            return null;
        }
    }

    public static class DropNS {
        private final MinecraftServer server;
        DropNS(MinecraftServer server) { this.server = server; }

        public void item(double x, double y, double z, String itemId, int count) {
            ServerLevel level = server.overworld();
            ResourceLocation rl = ResourceLocation.tryParse(itemId);
            if (rl == null) return;
            var item = BuiltInRegistries.ITEM.getOptional(rl).orElse(null);
            if (item == null) return;
            ItemStack stack = new ItemStack(item, Math.max(1, count));
            ItemEntity itemEntity = new ItemEntity(level, x, y, z, stack);
            level.addFreshEntity(itemEntity);
        }
    }

    public static class QueryNS {
        private final MinecraftServer server;
        private final Box3ScriptEngine engine;
        QueryNS(MinecraftServer server, Box3ScriptEngine engine) { this.server = server; this.engine = engine; }

        public Object raycast(GameVector3 origin, GameVector3 direction) {
            return raycast(origin, direction, 5.0);
        }

        public Object raycast(GameVector3 origin, GameVector3 direction, double maxDistance) {
            ServerLevel level = server.overworld();
            Vec3 start = new Vec3(origin.x, origin.y, origin.z);
            double len = Math.sqrt(direction.x * direction.x + direction.y * direction.y + direction.z * direction.z);
            if (len < 0.0001) {
                NativeObject result = new NativeObject();
                ScriptableObject.putProperty(result, "hit", false);
                return result;
            }
            Vec3 dir = new Vec3(direction.x / len, direction.y / len, direction.z / len);
            Vec3 end = start.add(dir.scale(maxDistance));
            ClipContext ctx = new ClipContext(start, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, CollisionContext.empty());
            BlockHitResult blockHit = level.clip(ctx);
            AABB searchBox = new AABB(start, end).inflate(1.0);
            Entity closestEntity = null;
            Vec3 entityHitPos = null;
            double closestEntDistSqr = maxDistance * maxDistance;
            for (Entity e : level.getEntities((Entity) null, searchBox, e -> true)) {
                var hit = e.getBoundingBox().clip(start, end);
                if (hit.isPresent()) {
                    double dSqr = start.distanceToSqr(hit.get());
                    if (dSqr < closestEntDistSqr) {
                        closestEntDistSqr = dSqr;
                        closestEntity = e;
                        entityHitPos = hit.get();
                    }
                }
            }
            double blockDistSqr = blockHit.getType() != HitResult.Type.MISS
                ? start.distanceToSqr(blockHit.getLocation()) : Double.MAX_VALUE;
            NativeObject result = new NativeObject();
            if (closestEntity != null && closestEntDistSqr < blockDistSqr) {
                ScriptableObject.putProperty(result, "hit", true);
                ScriptableObject.putProperty(result, "x", entityHitPos.x);
                ScriptableObject.putProperty(result, "y", entityHitPos.y);
                ScriptableObject.putProperty(result, "z", entityHitPos.z);
                ScriptableObject.putProperty(result, "normalX", 0);
                ScriptableObject.putProperty(result, "normalY", 0);
                ScriptableObject.putProperty(result, "normalZ", 0);
                ScriptableObject.putProperty(result, "distance", Math.sqrt(closestEntDistSqr));
                ScriptableObject.putProperty(result, "entity", new Box3JSEntity(closestEntity, server, engine));
            } else if (blockHit.getType() != HitResult.Type.MISS) {
                Vec3 pos = blockHit.getLocation();
                ScriptableObject.putProperty(result, "hit", true);
                ScriptableObject.putProperty(result, "x", pos.x);
                ScriptableObject.putProperty(result, "y", pos.y);
                ScriptableObject.putProperty(result, "z", pos.z);
                Direction face = blockHit.getDirection();
                ScriptableObject.putProperty(result, "normalX", face.getStepX());
                ScriptableObject.putProperty(result, "normalY", face.getStepY());
                ScriptableObject.putProperty(result, "normalZ", face.getStepZ());
                ScriptableObject.putProperty(result, "distance", Math.sqrt(blockDistSqr));
                ScriptableObject.putProperty(result, "entity", null);
                BlockPos bp = blockHit.getBlockPos();
                ScriptableObject.putProperty(result, "voxel", engine.getVoxelsBinding().getId(level.getBlockState(bp)));
            } else {
                ScriptableObject.putProperty(result, "hit", false);
            }
            return result;
        }

        public List<Box3JSEntity> entitiesInArea(GameVector3 pos1, GameVector3 pos2) {
            AABB aabb = new AABB(pos1.x, pos1.y, pos1.z, pos2.x, pos2.y, pos2.z);
            List<Box3JSEntity> result = new ArrayList<>();
            for (Entity e : server.overworld().getEntities((Entity) null, aabb, e -> true)) {
                result.add(new Box3JSEntity(e, server, engine));
            }
            return result;
        }

        public String biome(int x, int y, int z) {
            Holder<Biome> biome = server.overworld().getBiome(new BlockPos(x, y, z));
            var key = biome.unwrapKey();
            return key.map(k -> k.location().toString()).orElse("unknown");
        }
    }

    public static class EffectNS {
        private final MinecraftServer server;
        EffectNS(MinecraftServer server) { this.server = server; }

        public void explode(double x, double y, double z, double power) {
            explode(x, y, z, power, false);
        }

        public void explode(double x, double y, double z, double power, boolean fire) {
            server.overworld().explode(null, x, y, z, (float) power, fire, Level.ExplosionInteraction.BLOCK);
        }
    }

    public static class SoundNS {
        private final MinecraftServer server;
        SoundNS(MinecraftServer server) { this.server = server; }

        public void playAll(String path, double x, double y, double z, double volume, double pitch) {
            ResourceLocation rl = ResourceLocation.tryParse(path);
            if (rl == null) return;
            var sound = BuiltInRegistries.SOUND_EVENT.getHolder(rl);
            if (sound.isEmpty()) return;
            var packet = new ClientboundSoundPacket(sound.get(), SoundSource.PLAYERS, x, y, z, (float) volume, (float) pitch, server.overworld().getRandom().nextLong());
            for (ServerPlayer sp : server.getPlayerList().getPlayers()) {
                sp.connection.send(packet);
            }
        }
    }

    public static class MessageNS {
        private final Box3ScriptEngine engine;
        MessageNS(Box3ScriptEngine engine) { this.engine = engine; }

        public void send(String target, Object data) {
            engine.fireMessage(engine.getCurrentProject(), target, data);
        }

        public void on(Function handler) {
            String project = engine.getCurrentProject();
            if (project != null) {
                engine.addMessageCallback(project, (from, d) -> engine.callFunction(handler, from, d));
            }
        }
    }
}

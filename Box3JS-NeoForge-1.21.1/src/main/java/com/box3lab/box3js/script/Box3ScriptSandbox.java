package com.box3lab.box3js.script;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ServerLevelData;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

class Box3ScriptSandbox {

    private static final int MAX_BLOCK_CHANGES = 5_000_000;
    private static final double WARN_THRESHOLD = 0.9;

    private final Map<String, Map<BlockPos, BlockState>> blockChanges = new ConcurrentHashMap<>();
    private final Map<String, List<Entity>> spawnedEntities = new ConcurrentHashMap<>();
    private final Map<String, Map<UUID, PlayerSnapshot>> playerSnapshots = new ConcurrentHashMap<>();
    private final Map<String, Map<UUID, EntitySnapshot>> entitySnapshots = new ConcurrentHashMap<>();
    private final Map<String, WorldSnapshot> worldSnapshots = new ConcurrentHashMap<>();
    private final Set<String> enabledProjects = ConcurrentHashMap.newKeySet();
    private final Set<String> blockWarnedProjects = ConcurrentHashMap.newKeySet();
    private final ServerLevel level;

    Box3ScriptSandbox(ServerLevel level) {
        this.level = level;
    }

    boolean isEnabled(String project) { return project != null && enabledProjects.contains(project); }

    void enable(String project) { enabledProjects.add(project); }

    RestoreSummary disable(String project) {
        enabledProjects.remove(project);
        return restoreProject(project);
    }

    // ── Block tracking ──

    void trackBlock(String project, BlockPos pos) {
        if (!isEnabled(project)) return;
        Map<BlockPos, BlockState> changes = blockChanges.computeIfAbsent(project, k -> new HashMap<>());
        if (changes.size() >= MAX_BLOCK_CHANGES) return;
        changes.putIfAbsent(pos.immutable(), level.getBlockState(pos));
        if (changes.size() >= MAX_BLOCK_CHANGES * WARN_THRESHOLD && blockWarnedProjects.add(project)) {
            com.box3lab.box3js.Box3JS.LOGGER.warn("[Sandbox:{}] Block tracking at {}% ({} / {})",
                project, (int)(WARN_THRESHOLD * 100), changes.size(), MAX_BLOCK_CHANGES);
        }
    }

    // ── Entity tracking ──

    void trackEntity(String project, Entity entity) {
        if (!isEnabled(project)) return;
        spawnedEntities.computeIfAbsent(project, k -> new ArrayList<>()).add(entity);
    }

    void trackEntityModify(String project, Entity entity) {
        if (!isEnabled(project) || !(entity instanceof LivingEntity)) return;
        Map<UUID, EntitySnapshot> snapshots = entitySnapshots.computeIfAbsent(project, k -> new HashMap<>());
        snapshots.computeIfAbsent(entity.getUUID(), uuid -> EntitySnapshot.capture((LivingEntity) entity));
    }

    // ── Player tracking ──

    void trackPlayer(String project, ServerPlayer player) {
        if (!isEnabled(project)) return;
        Map<UUID, PlayerSnapshot> snapshots = playerSnapshots.computeIfAbsent(project, k -> new HashMap<>());
        snapshots.computeIfAbsent(player.getUUID(), uuid -> PlayerSnapshot.capture(player, level.getServer()));
    }

    // ── World state tracking ──

    void trackWorld(String project) {
        if (!isEnabled(project)) return;
        worldSnapshots.computeIfAbsent(project, k -> WorldSnapshot.capture(level));
    }

    // ── Restore ──

    RestoreSummary restoreProject(String project) {
        int blockCount = 0, entityCount = 0, playerCount = 0;
        boolean worldRestored = false;

        Map<BlockPos, BlockState> changes = blockChanges.remove(project);
        if (changes != null) {
            blockCount = changes.size();
            for (var entry : changes.entrySet()) {
                level.setBlock(entry.getKey(), entry.getValue(), 3);
            }
        }

        List<Entity> entities = spawnedEntities.remove(project);
        if (entities != null) {
            entityCount = entities.size();
            for (Entity e : entities) {
                if (e.isAlive()) e.discard();
            }
        }

        Map<UUID, PlayerSnapshot> pSnapshots = playerSnapshots.remove(project);
        if (pSnapshots != null) {
            playerCount = pSnapshots.size();
            MinecraftServer server = level.getServer();
            for (var entry : pSnapshots.entrySet()) {
                ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
                if (player != null) entry.getValue().restore(player, server);
            }
        }

        Map<UUID, EntitySnapshot> eSnapshots = entitySnapshots.remove(project);
        if (eSnapshots != null) {
            for (var entry : eSnapshots.entrySet()) {
                ServerPlayer player = level.getServer().getPlayerList().getPlayer(entry.getKey());
                if (player == null) continue;
                // Entity snapshots track entities, not players — skip lookup via player list
            }
            // Actually restore entity snapshots by finding entities still in the world
            for (var entry : eSnapshots.entrySet()) {
                Entity entity = level.getEntity(entry.getKey());
                if (entity instanceof LivingEntity le) {
                    entry.getValue().restore(le);
                }
            }
        }

        WorldSnapshot ws = worldSnapshots.remove(project);
        if (ws != null) {
            ws.restore(level);
            worldRestored = true;
        }

        blockWarnedProjects.remove(project);
        return new RestoreSummary(blockCount, entityCount, playerCount, worldRestored);
    }

    void restoreAll() {
        for (String project : new HashSet<>(blockChanges.keySet())) restoreProject(project);
        for (String project : new HashSet<>(spawnedEntities.keySet())) restoreProject(project);
        for (String project : new HashSet<>(playerSnapshots.keySet())) restoreProject(project);
        for (String project : new HashSet<>(entitySnapshots.keySet())) restoreProject(project);
        for (String project : new HashSet<>(worldSnapshots.keySet())) restoreProject(project);
    }

    void inheritEnabled(Box3ScriptSandbox other) {
        if (other != null) {
            for (String project : other.enabledProjects) enabledProjects.add(project);
        }
    }

    // ── RestoreSummary ──

    record RestoreSummary(int blocks, int entities, int players, boolean worldRestored) {
        boolean hasAny() { return blocks > 0 || entities > 0 || players > 0 || worldRestored; }

        String toMessage() {
            StringBuilder sb = new StringBuilder();
            if (blocks > 0) sb.append(blocks).append(" blocks, ");
            if (entities > 0) sb.append(entities).append(" entities, ");
            if (players > 0) sb.append(players).append(" players, ");
            if (worldRestored) sb.append("world state, ");
            if (!sb.isEmpty()) sb.setLength(sb.length() - 2);
            return sb.toString();
        }
    }

    // ═══════════════════════════════════════════════
    //  WorldSnapshot
    // ═══════════════════════════════════════════════

    static class WorldSnapshot {
        final boolean raining, thundering;
        final long dayTime;
        final boolean daylightCycle;
        final String difficulty;
        final double borderSize, borderCenterX, borderCenterZ, borderDamage;
        final int borderWarning;
        final Map<String, String> gameRules;

        WorldSnapshot(boolean raining, boolean thundering, long dayTime, boolean daylightCycle,
                      String difficulty, double borderSize, double borderCenterX, double borderCenterZ,
                      double borderDamage, int borderWarning, Map<String, String> gameRules) {
            this.raining = raining; this.thundering = thundering; this.dayTime = dayTime;
            this.daylightCycle = daylightCycle; this.difficulty = difficulty;
            this.borderSize = borderSize; this.borderCenterX = borderCenterX;
            this.borderCenterZ = borderCenterZ; this.borderDamage = borderDamage;
            this.borderWarning = borderWarning; this.gameRules = gameRules;
        }

        static WorldSnapshot capture(ServerLevel level) {
            var gamerules = level.getGameRules();
            Map<String, String> rules = new HashMap<>();
            rules.put("doDaylightCycle", String.valueOf(gamerules.getBoolean(GameRules.RULE_DAYLIGHT)));
            rules.put("doWeatherCycle", String.valueOf(gamerules.getBoolean(GameRules.RULE_WEATHER_CYCLE)));
            rules.put("keepInventory", String.valueOf(gamerules.getBoolean(GameRules.RULE_KEEPINVENTORY)));
            rules.put("doMobSpawning", String.valueOf(gamerules.getBoolean(GameRules.RULE_DOMOBSPAWNING)));
            rules.put("doFireTick", String.valueOf(gamerules.getBoolean(GameRules.RULE_DOFIRETICK)));
            rules.put("mobGriefing", String.valueOf(gamerules.getBoolean(GameRules.RULE_MOBGRIEFING)));
            rules.put("doImmediateRespawn", String.valueOf(gamerules.getBoolean(GameRules.RULE_DO_IMMEDIATE_RESPAWN)));
            var border = level.getWorldBorder();
            return new WorldSnapshot(
                level.getLevelData().isRaining(), ((ServerLevelData) level.getLevelData()).isThundering(),
                level.getDayTime(), gamerules.getBoolean(GameRules.RULE_DAYLIGHT),
                level.getDifficulty().getKey(),
                border.getSize(), border.getCenterX(), border.getCenterZ(),
                border.getDamagePerBlock(), border.getWarningBlocks(), rules
            );
        }

        void restore(ServerLevel level) {
            level.getLevelData().setRaining(raining);
            ((ServerLevelData) level.getLevelData()).setThundering(thundering);
            level.setDayTime(dayTime);
            level.getGameRules().getRule(GameRules.RULE_DAYLIGHT)
                .set(Boolean.parseBoolean(gameRules.get("doDaylightCycle")), level.getServer());
            level.getGameRules().getRule(GameRules.RULE_WEATHER_CYCLE)
                .set(Boolean.parseBoolean(gameRules.get("doWeatherCycle")), level.getServer());
            level.getGameRules().getRule(GameRules.RULE_KEEPINVENTORY)
                .set(Boolean.parseBoolean(gameRules.get("keepInventory")), level.getServer());
            level.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING)
                .set(Boolean.parseBoolean(gameRules.get("doMobSpawning")), level.getServer());
            level.getGameRules().getRule(GameRules.RULE_DOFIRETICK)
                .set(Boolean.parseBoolean(gameRules.get("doFireTick")), level.getServer());
            level.getGameRules().getRule(GameRules.RULE_MOBGRIEFING)
                .set(Boolean.parseBoolean(gameRules.get("mobGriefing")), level.getServer());
            level.getGameRules().getRule(GameRules.RULE_DO_IMMEDIATE_RESPAWN)
                .set(Boolean.parseBoolean(gameRules.get("doImmediateRespawn")), level.getServer());
            MinecraftServer server = level.getServer();
            net.minecraft.world.Difficulty diff = net.minecraft.world.Difficulty.byName(difficulty);
            if (diff != null) server.setDifficulty(diff, true);
            var border = level.getWorldBorder();
            border.setSize(borderSize);
            border.setCenter(borderCenterX, borderCenterZ);
            border.setDamagePerBlock(borderDamage);
            border.setWarningBlocks(borderWarning);
        }
    }

    // ═══════════════════════════════════════════════
    //  EntitySnapshot
    // ═══════════════════════════════════════════════

    static class EntitySnapshot {
        final float hp, maxHp;
        final boolean invisible, glowing, invulnerable, aiEnabled, persistent;
        final String nameTag, mainHandItem;
        final int fireTicks;
        final List<MobEffectInstance> effects;
        final List<String> tags;

        EntitySnapshot(float hp, float maxHp, boolean invisible, boolean glowing, boolean invulnerable,
                       boolean aiEnabled, boolean persistent, String nameTag, String mainHandItem,
                       int fireTicks, List<MobEffectInstance> effects, List<String> tags) {
            this.hp = hp; this.maxHp = maxHp; this.invisible = invisible; this.glowing = glowing;
            this.invulnerable = invulnerable; this.aiEnabled = aiEnabled; this.persistent = persistent;
            this.nameTag = nameTag; this.mainHandItem = mainHandItem; this.fireTicks = fireTicks;
            this.effects = effects; this.tags = tags;
        }

        static EntitySnapshot capture(LivingEntity entity) {
            String mainHand = "";
            ItemStack held = entity.getMainHandItem();
            if (!held.isEmpty()) {
                ResourceLocation key = entity.registryAccess().registryOrThrow(Registries.ITEM).getKey(held.getItem());
                if (key != null) mainHand = key.toString();
            }
            List<String> tagList = new ArrayList<>(entity.getTags());
            boolean ai = entity instanceof Mob m && !m.isNoAi();
            return new EntitySnapshot(
                entity.getHealth(), entity.getMaxHealth(),
                entity.isInvisible(), entity.isCurrentlyGlowing(), entity.isInvulnerable(),
                ai, entity instanceof Mob m && m.isPersistenceRequired(),
                entity.getCustomName() != null ? entity.getCustomName().getString() : "",
                mainHand, entity.getRemainingFireTicks(),
                new ArrayList<>(entity.getActiveEffects()), tagList
            );
        }

        void restore(LivingEntity entity) {
            if (entity.getMaxHealth() > 0) entity.setHealth(Math.min(hp, entity.getMaxHealth()));
            entity.setInvisible(invisible);
            entity.setGlowingTag(glowing);
            entity.setInvulnerable(invulnerable);
            entity.setRemainingFireTicks(fireTicks);
            if (!nameTag.isEmpty()) entity.setCustomName(net.minecraft.network.chat.Component.literal(nameTag));
            else entity.setCustomName(null);
            if (entity instanceof Mob m) {
                m.setNoAi(!aiEnabled);
                if (persistent) m.setPersistenceRequired();
            }
            entity.removeAllEffects();
            for (var e : effects) entity.addEffect(e);
            for (var tag : new ArrayList<>(entity.getTags())) entity.removeTag(tag);
            for (var tag : tags) entity.addTag(tag);
            if (!mainHandItem.isEmpty()) {
                var item = Box3ScriptUtils.lookupItem(mainHandItem);
                if (item != null) {
                    entity.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, new ItemStack(item));
                }
            }
        }
    }

    // ═══════════════════════════════════════════════
    //  PlayerSnapshot
    // ═══════════════════════════════════════════════

    static class PlayerSnapshot {
        final GameType gameMode;
        final boolean mayfly, flying;
        final float flySpeed;
        final double walkSpeed, jumpPower;
        final boolean invisible;
        final int opLevel;
        final List<MobEffectInstance> effects;
        // Expanded fields
        final List<Tag> inventory;
        final List<Tag> armor;
        final Tag offhand;
        final double posX, posY, posZ;
        final String dimension;
        final int xp;
        final int food;
        final float saturation;
        final String respawnDim;
        final int respawnX, respawnY, respawnZ;

        PlayerSnapshot(GameType gameMode, boolean mayfly, boolean flying, float flySpeed,
                       double walkSpeed, double jumpPower, boolean invisible, int opLevel,
                       List<MobEffectInstance> effects, List<Tag> inventory,
                       List<Tag> armor, Tag offhand,
                       double posX, double posY, double posZ, String dimension,
                       int xp, int food, float saturation,
                       String respawnDim, int respawnX, int respawnY, int respawnZ) {
            this.gameMode = gameMode; this.mayfly = mayfly; this.flying = flying;
            this.flySpeed = flySpeed; this.walkSpeed = walkSpeed; this.jumpPower = jumpPower;
            this.invisible = invisible; this.opLevel = opLevel; this.effects = effects;
            this.inventory = inventory; this.armor = armor; this.offhand = offhand;
            this.posX = posX; this.posY = posY; this.posZ = posZ; this.dimension = dimension;
            this.xp = xp; this.food = food; this.saturation = saturation;
            this.respawnDim = respawnDim; this.respawnX = respawnX; this.respawnY = respawnY;
            this.respawnZ = respawnZ;
        }

        static PlayerSnapshot capture(ServerPlayer player, MinecraftServer server) {
            var abilities = player.getAbilities();
            List<Tag> inv = new ArrayList<>();
            var registryAccess = player.registryAccess();
            for (ItemStack stack : player.getInventory().items) {
                inv.add(stack.isEmpty() ? null : stack.save(registryAccess));
            }
            List<Tag> arm = new ArrayList<>();
            for (ItemStack stack : player.getInventory().armor) {
                arm.add(stack.isEmpty() ? null : stack.save(registryAccess));
            }
            ItemStack offhandStack = player.getInventory().offhand.getFirst();
            Tag oh = offhandStack.isEmpty() ? null : offhandStack.save(registryAccess);
            var resp = player.getRespawnPosition();
            String rDim = "";
            int rx = 0, ry = 0, rz = 0;
            if (resp != null) {
                rDim = player.getRespawnDimension().location().toString();
                rx = resp.getX(); ry = resp.getY(); rz = resp.getZ();
            }
            return new PlayerSnapshot(
                player.gameMode.getGameModeForPlayer(),
                abilities.mayfly, abilities.flying, abilities.getFlyingSpeed(),
                player.getAttributeValue(Attributes.MOVEMENT_SPEED),
                player.getAttributeValue(Attributes.JUMP_STRENGTH),
                player.isInvisible(), server.getProfilePermissions(player.getGameProfile()),
                new ArrayList<>(player.getActiveEffects()),
                inv, arm, oh,
                player.getX(), player.getY(), player.getZ(),
                player.level().dimension().location().toString(),
                player.experienceLevel, player.getFoodData().getFoodLevel(),
                player.getFoodData().getSaturationLevel(),
                rDim, rx, ry, rz
            );
        }

        void restore(ServerPlayer player, MinecraftServer server) {
            player.setGameMode(gameMode);
            var abilities = player.getAbilities();
            abilities.mayfly = mayfly;
            abilities.flying = flying;
            abilities.setFlyingSpeed(flySpeed);
            player.onUpdateAbilities();
            player.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(walkSpeed);
            player.getAttribute(Attributes.JUMP_STRENGTH).setBaseValue(jumpPower);
            player.setInvisible(invisible);
            player.removeAllEffects();
            for (var effect : effects) player.addEffect(effect);
            if (opLevel > 0) server.getPlayerList().op(player.getGameProfile());
            else server.getPlayerList().deop(player.getGameProfile());
            // Restore inventory
            var registryAccess = player.registryAccess();
            if (inventory != null) {
                for (int i = 0; i < inventory.size() && i < player.getInventory().items.size(); i++) {
                    Tag tag = inventory.get(i);
                    player.getInventory().items.set(i, tag != null
                        ? ItemStack.parse(registryAccess, tag).orElse(ItemStack.EMPTY)
                        : ItemStack.EMPTY);
                }
            }
            if (armor != null) {
                for (int i = 0; i < armor.size() && i < player.getInventory().armor.size(); i++) {
                    Tag tag = armor.get(i);
                    player.getInventory().armor.set(i, tag != null
                        ? ItemStack.parse(registryAccess, tag).orElse(ItemStack.EMPTY)
                        : ItemStack.EMPTY);
                }
            }
            if (offhand != null) {
                player.getInventory().offhand.set(0, ItemStack.parse(registryAccess, offhand)
                    .orElse(ItemStack.EMPTY));
            }
            // Restore position / dimension
            if (dimension != null && !dimension.equals(player.level().dimension().location().toString())) {
                ResourceLocation rl = ResourceLocation.tryParse(dimension);
                if (rl != null) {
                    ServerLevel target = server.getLevel(ResourceKey.create(Registries.DIMENSION, rl));
                    if (target != null) player.teleportTo(target, posX, posY, posZ, player.getYRot(), player.getXRot());
                    else player.teleportTo(posX, posY, posZ);
                } else {
                    player.teleportTo(posX, posY, posZ);
                }
            } else {
                player.teleportTo(posX, posY, posZ);
            }
            player.experienceLevel = xp;
            player.getFoodData().setFoodLevel(food);
            player.getFoodData().setSaturation(saturation);
            // Restore respawn point
            if (!respawnDim.isEmpty()) {
                ResourceLocation rl = ResourceLocation.tryParse(respawnDim);
                if (rl != null) {
                    player.setRespawnPosition(ResourceKey.create(Registries.DIMENSION, rl),
                        new BlockPos(respawnX, respawnY, respawnZ), 0, true, false);
                }
            }
        }
    }
}

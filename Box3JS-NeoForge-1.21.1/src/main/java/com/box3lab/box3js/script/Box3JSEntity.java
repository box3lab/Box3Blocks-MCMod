package com.box3lab.box3js.script;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.mozilla.javascript.Function;

import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class Box3JSEntity {

    private final Entity entity;
    private final MinecraftServer server;
    private final Box3ScriptEngine engine;
    private Box3JSPlayer playerProxy;
    private Function _onDestroyHandler;
    private final GameVector3 _position, _velocity, _bounds;

    public final EffectNS effect;
    public final EquipmentNS equipment;

    public Box3JSEntity(Entity entity, MinecraftServer server, Box3ScriptEngine engine) {
        this.entity = entity;
        this.server = server;
        this.engine = engine;
        this._position = new LiveVec3(v -> entity.teleportTo(v.x, v.y, v.z));
        this._velocity = new LiveVec3(v -> entity.setDeltaMovement(v.x, v.y, v.z));
        this._bounds = new GameVector3();
        this.effect = new EffectNS(entity);
        this.equipment = new EquipmentNS(entity);
    }

    public Entity getEntity() { return entity; }

    // ---- Identity ----

    public String getId() {
        return entity.getStringUUID();
    }

    public boolean isPlayer() { return entity instanceof ServerPlayer; }

    public String getEntityType() {
        var key = entity.getType().builtInRegistryHolder().key();
        return key != null ? key.location().toString() : "unknown";
    }

    public Box3JSPlayer getPlayer() {
        if (!isPlayer()) return null;
        if (playerProxy == null) playerProxy = new Box3JSPlayer((ServerPlayer) entity, server, engine);
        return playerProxy;
    }

    // ---- Position / Velocity / Bounds ----

    public GameVector3 getPosition() {
        _position.x = entity.getX();
        _position.y = entity.getY();
        _position.z = entity.getZ();
        return _position;
    }

    public GameVector3 getVelocity() {
        var v = entity.getDeltaMovement();
        _velocity.x = v.x; _velocity.y = v.y; _velocity.z = v.z;
        return _velocity;
    }

    public GameVector3 getBounds() {
        var bb = entity.getBoundingBox();
        _bounds.x = (bb.maxX - bb.minX) / 2.0;
        _bounds.y = (bb.maxY - bb.minY) / 2.0;
        _bounds.z = (bb.maxZ - bb.minZ) / 2.0;
        return _bounds;
    }

    // ---- Appearance ----

    public boolean getMeshInvisible() { return getProp("meshInvisible", false); }
    public void setMeshInvisible(boolean v) {
        setProp("meshInvisible", v);
        entity.setInvisible(v);
    }

    // ---- Tags ----

    public void addTag(String tag) {
        entity.addTag(tag);
    }

    public boolean hasTag(String tag) {
        return entity.getTags().contains(tag);
    }

    public void removeTag(String tag) {
        entity.removeTag(tag);
    }

    // ---- Glowing (MC extension) ----

    public boolean isGlowing() { return entity.isCurrentlyGlowing(); }
    public void setGlowing(boolean v) { entity.setGlowingTag(v); }

    // ---- Name tag (MC extension) ----

    public String getNameTag() {
        var cn = entity.getCustomName();
        return cn != null ? cn.getString() : "";
    }

    public void setNameTag(String name) {
        entity.setCustomName(net.minecraft.network.chat.Component.literal(name));
        entity.setCustomNameVisible(true);
    }

    // ---- Movement queries (MC extension) ----

    public boolean getOnGround() { return entity.onGround(); }

    public GameVector3 getEyePosition() {
        var eye = entity.getEyePosition();
        return new GameVector3(eye.x, eye.y, eye.z);
    }

    // ---- Health / Combat ----

    public boolean getDestroyed() { return entity.isRemoved(); }

    public double getHp() {
        if (entity instanceof LivingEntity le) return le.getHealth();
        return getProp("hp", 100.0);
    }
    public void setHp(double v) {
        setProp("hp", v);
        if (entity instanceof LivingEntity le) {
            double max = le.getMaxHealth();
            le.setHealth((float) Math.max(0, Math.min(v, max)));
        }
    }

    public double getMaxHp() {
        if (entity instanceof LivingEntity le) return le.getMaxHealth();
        return getProp("maxHp", 100.0);
    }
    public void setMaxHp(double v) {
        setProp("maxHp", v);
        if (entity instanceof LivingEntity le) {
            le.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH)
                    .setBaseValue(v);
            if (le.getHealth() > v) le.setHealth((float) v);
        }
    }

    public void hurt(double amount) {
        if (entity instanceof LivingEntity le) {
            le.hurt(le.damageSources().generic(), (float) amount);
        }
    }

    public void heal(double amount) {
        if (entity instanceof LivingEntity le) {
            le.heal((float) amount);
        }
    }

    // ---- Invulnerable (MC extension) ----

    public boolean isInvulnerable() { return entity.isInvulnerable(); }
    public void setInvulnerable(boolean v) { entity.setInvulnerable(v); }

    // ---- Fire (MC extension) ----

    public void setFire(int ticks) {
        entity.setRemainingFireTicks(ticks);
    }

    public void clearFire() {
        entity.setRemainingFireTicks(0);
    }

    // ---- Look at (MC extension) ----

    public void lookAt(double x, double y, double z) {
        double dx = x - entity.getX();
        double dy = y - entity.getEyeY();
        double dz = z - entity.getZ();
        double horizontalDist = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        float pitch = (float) (-Math.toDegrees(Math.atan2(dy, horizontalDist)));
        entity.setYRot(yaw);
        entity.setXRot(pitch);
    }

    // ---- Navigation (MC extension) ----

    /** Pathfinding-based movement to a target position. Returns true if a path was found. */
    public boolean navigateTo(double x, double y, double z, double speed) {
        if (entity instanceof PathfinderMob mob) {
            return mob.getNavigation().moveTo(x, y, z, speed);
        }
        return false;
    }

    /** Set the mob's attack target. The mob will pathfind to and attack the target. */
    public void setTarget(Box3JSEntity target) {
        if (entity instanceof Mob mob && target != null && target.getEntity() instanceof LivingEntity le) {
            mob.setTarget(le);
        }
    }

    /** Clear the mob's attack target, stopping pursuit. */
    public void clearTarget() {
        if (entity instanceof Mob mob) {
            mob.setTarget(null);
        }
    }

    /** Get the mob's current attack target, or null. */
    public Box3JSEntity getTarget() {
        if (entity instanceof Mob mob) {
            LivingEntity target = mob.getTarget();
            return target != null ? new Box3JSEntity(target, server, engine) : null;
        }
        return null;
    }

    /** Enable or disable the mob's AI (pathfinding, goals, etc.) */
    public void setAI(boolean enabled) {
        if (entity instanceof Mob mob) {
            mob.setNoAi(!enabled);
        }
    }

    // ---- Lifecycle ----

    public void destroy() {
        if (_onDestroyHandler != null) {
            engine.callFunction(_onDestroyHandler, this);
        }
        entity.discard();
        engine.clearCustomProps(entity.getUUID());
    }

    /** Remove entity without triggering onDestroy callback */
    public void remove() {
        entity.discard();
        engine.clearCustomProps(entity.getUUID());
    }

    public void setOnDestroy(Function handler) {
        this._onDestroyHandler = handler;
    }

    // ---- Custom properties ----

    private Map<String, Object> props() {
        return engine.getCustomProps(entity.getUUID());
    }

    @SuppressWarnings("unchecked")
    private <T> T getProp(String key, T defaultValue) {
        Object v = props().get(key);
        return v != null ? (T) v : defaultValue;
    }

    private void setProp(String key, Object value) {
        props().put(key, value);
    }

    /** Vector whose set() call syncs back to the MC entity */
    private static class LiveVec3 extends GameVector3 {
        private final Consumer<GameVector3> onSet;

        LiveVec3(Consumer<GameVector3> onSet) { this.onSet = onSet; }

        @Override
        public GameVector3 set(double x, double y, double z) {
            this.x = x; this.y = y; this.z = z;
            onSet.accept(this);
            return this;
        }
    }

    // ---- Namespace classes ----

    public static class EffectNS {
        private final Entity entity;
        EffectNS(Entity entity) { this.entity = entity; }

        public void add(String effectId, int duration, int amplifier) {
            if (!(entity instanceof LivingEntity le)) return;
            ResourceLocation rl = ResourceLocation.tryParse(effectId);
            if (rl == null) return;
            Holder<MobEffect> effect = BuiltInRegistries.MOB_EFFECT.getHolder(rl).orElse(null);
            if (effect == null) return;
            le.addEffect(new MobEffectInstance(effect, duration, amplifier));
        }
    }

    public static class EquipmentNS {
        private final Entity entity;
        EquipmentNS(Entity entity) { this.entity = entity; }

        public void set(String slot, String itemId) {
            if (!(entity instanceof Mob mob)) return;
            EquipmentSlot equipmentSlot = switch (slot.toLowerCase()) {
                case "mainhand" -> EquipmentSlot.MAINHAND;
                case "offhand" -> EquipmentSlot.OFFHAND;
                case "head", "helmet", "helm" -> EquipmentSlot.HEAD;
                case "chest", "chestplate" -> EquipmentSlot.CHEST;
                case "legs", "leggings" -> EquipmentSlot.LEGS;
                case "feet", "boots" -> EquipmentSlot.FEET;
                default -> null;
            };
            if (equipmentSlot == null) return;
            ResourceLocation rl = ResourceLocation.tryParse(itemId);
            if (rl == null) return;
            Item item = BuiltInRegistries.ITEM.getOptional(rl).orElse(null);
            if (item == null) return;
            mob.setItemSlot(equipmentSlot, new ItemStack(item));
        }
    }
}

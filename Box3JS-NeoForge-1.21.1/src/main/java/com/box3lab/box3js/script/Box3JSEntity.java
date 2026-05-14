package com.box3lab.box3js.script;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import com.mojang.logging.LogUtils;
import org.mozilla.javascript.Function;
import org.slf4j.Logger;

import java.util.Map;
import java.util.function.Consumer;

public class Box3JSEntity {

    private static final Logger LOGGER = LogUtils.getLogger();

    private final Entity entity;
    private final MinecraftServer server;
    private final Box3ScriptEngine engine;
    private Box3JSPlayer playerProxy;
    private Function _onDestroyHandler;
    private final GameVector3 _position, _velocity, _bounds;

    public Box3JSEntity(Entity entity, MinecraftServer server, Box3ScriptEngine engine) {
        this.entity = entity;
        this.server = server;
        this.engine = engine;
        this._position = new LiveVec3(v -> entity.teleportTo(v.x, v.y, v.z));
        this._velocity = new LiveVec3(v -> entity.setDeltaMovement(v.x, v.y, v.z));
        this._bounds = new GameVector3();
    }

    public Entity getEntity() { return entity; }

    // ---- Identity ----

    public String getId() {
        return entity.getStringUUID();
    }

    public boolean isPlayer() { return entity instanceof ServerPlayer; }

    public String getEntityType() {
        var key = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        return key != null ? key.toString() : "unknown";
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
        trackIfSandboxed();
        setProp("meshInvisible", v);
        entity.setInvisible(v);
    }

    // ---- Tags ----

    public void addTag(String tag) {
        trackIfSandboxed();
        entity.addTag(tag);
    }

    public boolean hasTag(String tag) {
        return entity.getTags().contains(tag);
    }

    public void removeTag(String tag) {
        trackIfSandboxed();
        entity.removeTag(tag);
    }

    public String[] tags() {
        return entity.getTags().toArray(new String[0]);
    }

    // ---- Glowing (MC extension) ----

    public boolean isGlowing() { return entity.isCurrentlyGlowing(); }
    public void setGlowing(boolean v) { trackIfSandboxed(); entity.setGlowingTag(v); }

    public void setGlowColor(GameRGBColor color) {
        trackIfSandboxed();
        Scoreboard sb = server.getScoreboard();
        String teamName = "b3js_g_" + entity.getStringUUID().replaceAll("-", "");
        PlayerTeam team = sb.getPlayerTeam(teamName);
        if (team == null) team = sb.addPlayerTeam(teamName);
        team.setColor(closestChatFormatting(color));
        sb.addPlayerToTeam(entity.getScoreboardName(), team);
        entity.setGlowingTag(true);
    }

    private static ChatFormatting closestChatFormatting(GameRGBColor c) {
        ChatFormatting best = ChatFormatting.WHITE;
        double bestDist = Double.MAX_VALUE;
        for (ChatFormatting cf : ChatFormatting.values()) {
            Integer col = cf.getColor();
            if (col == null) continue;
            double dr = ((col >> 16) & 0xFF) / 255.0 - c.r;
            double dg = ((col >> 8) & 0xFF) / 255.0 - c.g;
            double db = (col & 0xFF) / 255.0 - c.b;
            double dist = dr * dr + dg * dg + db * db;
            if (dist < bestDist) { bestDist = dist; best = cf; }
        }
        return best;
    }

    // ---- Name tag (MC extension) ----

    public String getNameTag() {
        var cn = entity.getCustomName();
        return cn != null ? cn.getString() : "";
    }

    public void setNameTag(String name) {
        trackIfSandboxed();
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
        LivingEntity le = asLiving();
        if (le != null) return le.getHealth();
        return getProp("hp", 100.0);
    }
    public void setHp(double v) {
        trackIfSandboxed();
        setProp("hp", v);
        LivingEntity le = asLiving();
        if (le != null) {
            double max = le.getMaxHealth();
            le.setHealth((float) Math.max(0, Math.min(v, max)));
        }
    }

    public double getMaxHp() {
        LivingEntity le = asLiving();
        if (le != null) return le.getMaxHealth();
        return getProp("maxHp", 100.0);
    }
    public void setMaxHp(double v) {
        trackIfSandboxed();
        setProp("maxHp", v);
        LivingEntity le = asLiving();
        if (le != null) {
            le.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH)
                    .setBaseValue(v);
            if (le.getHealth() > v) le.setHealth((float) v);
        }
    }

    public void hurt(double amount) {
        LivingEntity le = asLiving();
        if (le != null) le.hurt(le.damageSources().generic(), (float) amount);
    }

    public void heal(double amount) {
        LivingEntity le = asLiving();
        if (le != null) le.heal((float) amount);
    }

    private LivingEntity asLiving() {
        return entity instanceof LivingEntity le ? le : null;
    }

    private void trackIfSandboxed() {
        engine.getSandbox().trackEntityModify(engine.getCurrentProject(), entity);
    }

    // ---- Physics ----

    public boolean getCollides() { return getProp("collides", true); }
    public void setCollides(boolean v) {
        trackIfSandboxed();
        setProp("collides", v);
        if (!v && entity instanceof LivingEntity le) le.setNoGravity(true);
    }

    public boolean getFixed() { return getProp("fixed", false); }
    public void setFixed(boolean v) {
        trackIfSandboxed();
        setProp("fixed", v);
        if (v && entity instanceof LivingEntity le) le.setNoGravity(true);
    }

    public boolean getGravity() { return getProp("gravity", true); }
    public void setGravity(boolean v) {
        trackIfSandboxed();
        setProp("gravity", v);
        if (!v && entity instanceof LivingEntity le) le.setNoGravity(true);
    }

    public double getFriction() { return getProp("friction", 0.0); }
    public void setFriction(double v) { trackIfSandboxed(); setProp("friction", v); }

    public double getMass() { return getProp("mass", 1.0); }
    public void setMass(double v) { trackIfSandboxed(); setProp("mass", v); }

    public double getRestitution() { return getProp("restitution", 0.0); }
    public void setRestitution(double v) { trackIfSandboxed(); setProp("restitution", v); }

    // ---- Invulnerable (MC extension) ----

    public boolean isInvulnerable() { return entity.isInvulnerable(); }
    public void setInvulnerable(boolean v) { trackIfSandboxed(); entity.setInvulnerable(v); }

    // ---- Fire (MC extension) ----

    public void setFire(int ticks) {
        trackIfSandboxed();
        entity.setRemainingFireTicks(ticks);
    }

    public void clearFire() {
        entity.setRemainingFireTicks(0);
    }

    // ---- Look at (MC extension) ----

    public void lookAt(double x, double y, double z) { Box3ScriptUtils.lookAt(entity, x, y, z); }
    public void lookAt(GameVector3 pos) { lookAt(pos.x, pos.y, pos.z); }

    // ---- Navigation (MC extension) ----

    public boolean navigateTo(double x, double y, double z, double speed) {
        if (entity instanceof PathfinderMob mob) {
            return mob.getNavigation().moveTo(x, y, z, speed);
        }
        return false;
    }
    public boolean navigateTo(GameVector3 pos, double speed) {
        return navigateTo(pos.x, pos.y, pos.z, speed);
    }

    /** Set the mob's attack target. The mob will pathfind to and attack the target. */
    public void setTarget(Box3JSEntity target) {
        trackIfSandboxed();
        if (entity instanceof Mob mob && target != null && target.getEntity() instanceof LivingEntity le) {
            mob.setTarget(le);
        }
    }

    /** Clear the mob's attack target, stopping pursuit. */
    public void clearTarget() {
        trackIfSandboxed();
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
        trackIfSandboxed();
        if (entity instanceof Mob mob) {
            mob.setNoAi(!enabled);
        }
    }

    // ---- Effects (MC extension) ----

    public void addEffect(String effectId, int duration, int amplifier) {
        addEffect(effectId, duration, amplifier, false);
    }

    public void addEffect(String effectId, int duration, int amplifier, boolean hideParticles) {
        trackIfSandboxed();
        LivingEntity le = asLiving();
        if (le == null) return;
        Holder<MobEffect> effect = Box3ScriptUtils.lookupMobEffect(effectId);
        if (effect == null) return;
        le.addEffect(new MobEffectInstance(effect, duration, amplifier, false, !hideParticles, true));
    }

    // ---- Equipment (MC extension) ----

    public void setEquipment(String slot, String itemId) {
        trackIfSandboxed();
        if (!(entity instanceof Mob mob)) return;
        EquipmentSlot equipmentSlot = parseEquipmentSlot(slot);
        if (equipmentSlot == null) return;
        Item item = Box3ScriptUtils.lookupItem(itemId);
        if (item == null) return;
        mob.setItemSlot(equipmentSlot, new ItemStack(item));
    }

    // ---- Drop chances (MC extension) ----

    public void setDropChance(String slot, double chance) {
        trackIfSandboxed();
        if (!(entity instanceof Mob mob)) return;
        float f = (float) Math.max(0, Math.min(1, chance));
        if ("all".equalsIgnoreCase(slot)) {
            for (EquipmentSlot es : EquipmentSlot.values()) {
                mob.setDropChance(es, f);
            }
            return;
        }
        EquipmentSlot es = parseEquipmentSlot(slot);
        if (es != null) mob.setDropChance(es, f);
    }

    // ---- Persistence (MC extension) ----

    public void setPersistent(boolean v) {
        trackIfSandboxed();
        if (entity instanceof Mob mob && v) mob.setPersistenceRequired();
    }

    // ---- TextDisplay (MC extension) ----

    private static final java.lang.reflect.Method _tdSetText;
    private static final java.lang.reflect.Method _tdGetText;
    private static final java.lang.reflect.Method _tdSetBgColor;
    static {
        try {
            Class<?> td = net.minecraft.world.entity.Display.TextDisplay.class;
            _tdSetText = td.getDeclaredMethod("setText", Component.class);
            _tdSetText.setAccessible(true);
            _tdGetText = td.getDeclaredMethod("getText");
            _tdGetText.setAccessible(true);
            _tdSetBgColor = td.getDeclaredMethod("setBackgroundColor", int.class);
            _tdSetBgColor.setAccessible(true);
        } catch (Exception e) {
            throw new RuntimeException("Failed to access TextDisplay methods", e);
        }
    }

    public void setText(String text) {
        if (entity instanceof net.minecraft.world.entity.Display.TextDisplay td) {
            try {
                _tdSetText.invoke(td, Component.literal(text));
            } catch (Exception e) {
                LOGGER.warn("Failed to set TextDisplay text for entity {}", entity.getStringUUID(), e);
            }
        }
    }

    public void setTextColor(GameRGBColor color) {
        if (entity instanceof net.minecraft.world.entity.Display.TextDisplay td) {
            try {
                Component current = (Component) _tdGetText.invoke(td);
                String text = current != null ? current.getString() : "";
                int r = (int) (Math.max(0, Math.min(1, color.r)) * 255);
                int g = (int) (Math.max(0, Math.min(1, color.g)) * 255);
                int b = (int) (Math.max(0, Math.min(1, color.b)) * 255);
                int rgb = (r << 16) | (g << 8) | b;
                _tdSetText.invoke(td, Component.literal(text).withColor(rgb));
            } catch (Exception e) {
                LOGGER.warn("Failed to set TextDisplay text color for entity {}", entity.getStringUUID(), e);
            }
        }
    }

    public void setTextBackgroundColor(GameRGBAColor color) {
        if (entity instanceof net.minecraft.world.entity.Display.TextDisplay td) {
            int r = (int) (Math.max(0, Math.min(1, color.r)) * 255);
            int g = (int) (Math.max(0, Math.min(1, color.g)) * 255);
            int b = (int) (Math.max(0, Math.min(1, color.b)) * 255);
            int a = (int) (Math.max(0, Math.min(1, color.a)) * 255);
            try {
                _tdSetBgColor.invoke(td, (a << 24) | (r << 16) | (g << 8) | b);
            } catch (Exception e) {
                LOGGER.warn("Failed to set TextDisplay background color for entity {}", entity.getStringUUID(), e);
            }
        }
    }

    // ---- Attributes (MC extension) ----

    public double getAttribute(String attributeId) {
        LivingEntity le = asLiving();
        if (le == null) return 0;
        var holder = Box3ScriptUtils.lookupAttribute(attributeId);
        if (holder != null) return le.getAttributeValue(holder);
        return 0;
    }

    public void setAttribute(String attributeId, double value) {
        trackIfSandboxed();
        LivingEntity le = asLiving();
        if (le == null) return;
        var holder = Box3ScriptUtils.lookupAttribute(attributeId);
        if (holder != null) {
            var instance = le.getAttribute(holder);
            if (instance != null) instance.setBaseValue(value);
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

    private static EquipmentSlot parseEquipmentSlot(String slot) {
        return switch (slot.toLowerCase()) {
            case "mainhand" -> EquipmentSlot.MAINHAND;
            case "offhand" -> EquipmentSlot.OFFHAND;
            case "head", "helmet", "helm" -> EquipmentSlot.HEAD;
            case "chest", "chestplate" -> EquipmentSlot.CHEST;
            case "legs", "leggings" -> EquipmentSlot.LEGS;
            case "feet", "boots" -> EquipmentSlot.FEET;
            default -> null;
        };
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
}

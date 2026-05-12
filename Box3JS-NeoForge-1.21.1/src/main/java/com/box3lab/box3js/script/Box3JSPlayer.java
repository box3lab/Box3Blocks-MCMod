package com.box3lab.box3js.script;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.ScriptableObject;

import java.util.Map;
import java.util.function.Consumer;

public class Box3JSPlayer {

    private final ServerPlayer player;
    private final MinecraftServer server;
    private final Box3ScriptEngine engine;
    private final GameVector3 _position, _velocity, _bounds;

    public Box3JSPlayer(ServerPlayer player, MinecraftServer server, Box3ScriptEngine engine) {
        this.player = player;
        this.server = server;
        this.engine = engine;
        this._position = new GameVector3();
        this._velocity = new GameVector3();
        this._bounds = new GameVector3();
    }

    // ---- Position / Velocity / Bounds ----

    public GameVector3 getPosition() {
        _position.x = player.getX();
        _position.y = player.getY();
        _position.z = player.getZ();
        return _position;
    }

    public GameVector3 getVelocity() {
        var v = player.getDeltaMovement();
        _velocity.x = v.x; _velocity.y = v.y; _velocity.z = v.z;
        return _velocity;
    }

    public GameVector3 getBounds() {
        var bb = player.getBoundingBox();
        _bounds.x = (bb.maxX - bb.minX) / 2.0;
        _bounds.y = (bb.maxY - bb.minY) / 2.0;
        _bounds.z = (bb.maxZ - bb.minZ) / 2.0;
        return _bounds;
    }

    public boolean getOnGround() { return player.onGround(); }

    // ---- Info ----

    public String getName() { return player.getGameProfile().getName(); }
    public String getUserId() { return player.getUUID().toString(); }
    public ServerPlayer getPlayer() { return player; }

    /** Whether this player has Box3JS installed on their client. */
    public boolean hasBox3JSClientMod() {
        return com.box3lab.box3js.Box3JS.clientsWithBox3JS.contains(player.getUUID());
    }

    public int getOpLevel() { return server.getProfilePermissions(player.getGameProfile()); }

    public void setOpLevel(int level) {
        trackIfSandboxed();
        if (level > 0) {
            server.getPlayerList().op(player.getGameProfile());
        } else {
            server.getPlayerList().deop(player.getGameProfile());
        }
    }

    // ---- Appearance ----

    public boolean getInvisible() { return player.isInvisible(); }
    public void setInvisible(boolean v) { trackIfSandboxed(); player.setInvisible(v); }

    public double getScale() { return player.getScale(); }

    // ---- Movement ----

    public double getWalkSpeed() { return player.getAttributeValue(Attributes.MOVEMENT_SPEED); }
    public void setWalkSpeed(double v) {
        trackIfSandboxed();
        player.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(v);
    }

    public double getRunSpeed() { return player.getAttributeValue(Attributes.MOVEMENT_SPEED) * 1.3; }
    public void setRunSpeed(double v) {
        trackIfSandboxed();
        player.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(v / 1.3);
    }

    public double getJumpPower() { return player.getAttributeValue(Attributes.JUMP_STRENGTH); }
    public void setJumpPower(double v) {
        trackIfSandboxed();
        player.getAttribute(Attributes.JUMP_STRENGTH).setBaseValue(v);
    }

    public String getMoveState() {
        if (player.getAbilities().flying) return "FLYING";
        if (player.isInWater()) return "SWIM";
        if (!player.onGround()) {
            if (player.getDeltaMovement().y > 0.01) return "JUMP";
            return "FALL";
        }
        return "GROUND";
    }

    public String getWalkState() {
        if (player.isCrouching()) return "CROUCH";
        if (player.isSprinting()) return "RUN";
        var delta = player.getDeltaMovement();
        if (Math.abs(delta.x) > 0.01 || Math.abs(delta.z) > 0.01) return "WALK";
        return "NONE";
    }

    // ---- Jump / Sneak / Swim ----

    public boolean getEnableJump() { return getProp("enableJump", true); }
    public void setEnableJump(boolean v) {
        trackIfSandboxed();
        setProp("enableJump", v);
        if (!v) {
            setProp("_savedJumpStrength", player.getAttributeValue(Attributes.JUMP_STRENGTH));
            player.getAttribute(Attributes.JUMP_STRENGTH).setBaseValue(0);
        } else {
            double saved = getProp("_savedJumpStrength", 0.42);
            player.getAttribute(Attributes.JUMP_STRENGTH).setBaseValue(saved);
        }
    }

    public double getCrouchSpeed() { return getProp("crouchSpeed", 0.0); }
    public void setCrouchSpeed(double v) { trackIfSandboxed(); setProp("crouchSpeed", v); }

    public double getSwimSpeed() {
        return player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.WATER_MOVEMENT_EFFICIENCY);
    }
    public void setSwimSpeed(double v) {
        trackIfSandboxed();
        player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.WATER_MOVEMENT_EFFICIENCY).setBaseValue(v);
    }

    // ---- Fly / Spectator ----

    public boolean getCanFly() { return player.getAbilities().mayfly; }
    public void setCanFly(boolean v) {
        trackIfSandboxed();
        updateAbility(a -> a.mayfly = v);
    }

    public boolean getFlying() { return player.getAbilities().flying; }
    public void setFlying(boolean v) {
        trackIfSandboxed();
        updateAbility(a -> a.flying = v);
    }

    public boolean getCollision() {
        var team = server.getScoreboard().getPlayersTeam(player.getScoreboardName());
        return team == null || team.getCollisionRule() != net.minecraft.world.scores.Team.CollisionRule.NEVER;
    }
    public void setCollision(boolean enabled) {
        trackIfSandboxed();
        var team = server.getScoreboard().getPlayersTeam(player.getScoreboardName());
        if (team != null) {
            team.setCollisionRule(enabled
                ? net.minecraft.world.scores.Team.CollisionRule.ALWAYS
                : net.minecraft.world.scores.Team.CollisionRule.NEVER);
        }
    }

    public boolean getSpectator() { return player.isSpectator(); }

    public double getFlySpeed() { return player.getAbilities().getFlyingSpeed(); }
    public void setFlySpeed(double v) {
        trackIfSandboxed();
        updateAbility(a -> a.setFlyingSpeed((float) v));
    }

    // ---- Game Mode ----

    public String getGameMode() { return player.gameMode.getGameModeForPlayer().getName(); }
    public void setGameMode(Object v) {
        trackIfSandboxed();
        GameType type;
        if (v instanceof Number n) {
            type = GameType.byId(n.intValue());
        } else {
            type = GameType.byName(v.toString());
        }
        if (type != null) player.setGameMode(type);
    }

    // ---- Dimension (MC extension) ----

    public String getDimension() {
        return player.level().dimension().location().toString();
    }
    public void setDimension(String dimId) {
        ResourceLocation rl = ResourceLocation.tryParse(dimId);
        if (rl == null) return;
        ServerLevel target = server.getLevel(ResourceKey.create(Registries.DIMENSION, rl));
        if (target != null) {
            player.teleportTo(target, player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());
        }
    }

    // ---- Disable Fly ----

    public boolean getDisableFly() { return getProp("disableFly", false); }
    public void setDisableFly(boolean v) {
        trackIfSandboxed();
        setProp("disableFly", v);
        if (v) { player.getAbilities().mayfly = false; player.getAbilities().flying = false; }
    }

    // ---- Camera ----

    public String getCameraMode() { return getProp("cameraMode", "FPS"); }
    public void setCameraMode(String v) {
        setProp("cameraMode", v);
        if ("FPS".equals(v)) player.setCamera(null);
    }

    public Object getCameraEntity() {
        return getProp("cameraEntity", null);
    }
    public void setCameraEntity(Object v) {
        setProp("cameraEntity", v);
        if (v == null) {
            setProp("cameraMode", "FPS");
            player.setCamera(null);
        } else if (v instanceof Box3JSEntity be) {
            setProp("cameraMode", "FOLLOW");
            player.setCamera(be.getEntity());
        }
    }

    public double getCameraPitch() { return player.getXRot(); }
    public void setCameraPitch(double v) { player.setXRot((float) v); }

    public double getCameraYaw() { return player.getYRot(); }
    public void setCameraYaw(double v) { player.setYRot((float) v); }

    public GameVector3 getFacingDirection() {
        var look = player.getLookAngle();
        return new GameVector3(look.x, look.y, look.z);
    }

    public GameVector3 getCameraTarget() {
        var look = player.getLookAngle();
        var eye = player.getEyePosition();
        return new GameVector3(eye.x + look.x * 5.0, eye.y + look.y * 5.0, eye.z + look.z * 5.0);
    }

    // ---- Respawn ----

    public boolean getDead() { return player.isDeadOrDying(); }

    public void setRespawnPoint(GameVector3 pos) {
        player.setRespawnPosition(
            player.level().dimension(),
            new BlockPos((int) pos.x, (int) pos.y, (int) pos.z),
            0, true, false);
    }

    public GameVector3 getSpawnPoint() {
        var pos = player.getRespawnPosition();
        if (pos == null) {
            var worldSpawn = server.overworld().getSharedSpawnPos();
            return new GameVector3(worldSpawn.getX(), worldSpawn.getY(), worldSpawn.getZ());
        }
        return new GameVector3(pos.getX(), pos.getY(), pos.getZ());
    }

    public void setSpawnPoint(GameVector3 pos) {
        setRespawnPoint(pos);
    }

    public void respawn() {
        if (player.isDeadOrDying()) {
            player.respawn();
        }
    }

    // ---- Kick ----

    public void kick() { kick("Kicked"); }

    public void kick(String reason) {
        player.connection.disconnect(Component.literal(reason));
    }

    // ---- Messaging ----

    public void directMessage(String msg) {
        player.sendSystemMessage(Component.literal(msg));
    }

    public void directMessage(String msg, GameRGBColor color) {
        int r = (int) (Math.max(0, Math.min(1, color.r)) * 255);
        int g = (int) (Math.max(0, Math.min(1, color.g)) * 255);
        int b = (int) (Math.max(0, Math.min(1, color.b)) * 255);
        int rgb = (r << 16) | (g << 8) | b;
        player.sendSystemMessage(Component.literal(msg).withColor(rgb));
    }

    public void actionBar(String message) {
        player.displayClientMessage(Component.literal(message), true);
    }

    public void title(String title, String subtitle) {
        title(title, subtitle, 10, 70, 20);
    }
    public void title(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        player.connection.send(new ClientboundSetTitlesAnimationPacket(fadeIn, stay, fadeOut));
        player.connection.send(new ClientboundSetTitleTextPacket(Component.literal(title)));
        if (subtitle != null && !subtitle.isEmpty()) {
            player.connection.send(new ClientboundSetSubtitleTextPacket(Component.literal(subtitle)));
        }
    }

    public void teleport(GameVector3 pos) {
        player.teleportTo(pos.x, pos.y, pos.z);
    }

    public Object dialog(NativeObject config) {
        String content = config.containsKey("content") ? config.get("content").toString() : "";
        Object optionsVal = config.containsKey("options") ? config.get("options") : null;

        String[] opts;
        if (optionsVal instanceof NativeObject optObj && optObj.containsKey("length")) {
            int len = ((Number) optObj.get("length")).intValue();
            opts = new String[len];
            for (int i = 0; i < len; i++) {
                opts[i] = String.valueOf(optObj.get(i));
            }
        } else {
            opts = new String[]{"OK"};
        }

        player.sendSystemMessage(Component.literal(content));

        NativeObject result = new NativeObject();
        ScriptableObject.putProperty(result, "index", 0);
        ScriptableObject.putProperty(result, "value", opts[0]);
        return result;
    }

    // ---- Chat (player-level) ----

    public void onChat(Function handler) {
        engine.setPlayerChatHandler(player.getUUID(), handler);
    }

    // ---- Link ----

    public void link(String href) {
        var comp = Component.literal(href)
            .withStyle(style -> style
                .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, href))
                .withUnderlined(true)
                .withColor(net.minecraft.network.chat.TextColor.fromRgb(0x5555FF)));
        player.sendSystemMessage(comp);
    }

    // ---- Tab list (MC extension) ----

    public void setPlayerListName(String name) {
        try {
            java.lang.reflect.Field f = net.minecraft.world.entity.player.Player.class.getDeclaredField("displayName");
            f.setAccessible(true);
            f.set(player, Component.literal(name));
            server.getPlayerList().broadcastAll(
                new net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket(
                    java.util.EnumSet.of(net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME),
                    java.util.List.of(player)));
        } catch (Exception ignored) {}
    }

    // ---- Look at (MC extension) ----

    public void lookAt(double x, double y, double z) { Box3ScriptUtils.lookAt(player, x, y, z); }
    public void lookAt(GameVector3 pos) { lookAt(pos.x, pos.y, pos.z); }

    // ---- Command ----

    public void runCommand(String cmd) {
        net.minecraft.commands.CommandSourceStack source = player.createCommandSourceStack();
        server.getCommands().performPrefixedCommand(source, cmd);
    }

    // ---- Health ----

    public double getHp() {
        return player.getHealth();
    }
    public void setHp(double v) {
        trackIfSandboxed();
        player.setHealth((float) Math.min(v, player.getMaxHealth()));
    }

    public double getMaxHp() {
        return player.getMaxHealth();
    }
    public void setMaxHp(double v) {
        trackIfSandboxed();
        player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(v);
        if (player.getHealth() > v) player.setHealth((float) v);
    }

    // ---- XP / Food ----

    public int getXp() { return player.experienceLevel; }
    public void setXp(int v) { player.experienceLevel = v; }

    public void addExperienceLevels(int levels) {
        player.experienceLevel += levels;
    }

    public int getFood() { return player.getFoodData().getFoodLevel(); }
    public void setFood(int v) { player.getFoodData().setFoodLevel(v); }

    public float getSaturation() { return player.getFoodData().getSaturationLevel(); }
    public void setSaturation(float v) { player.getFoodData().setSaturation(v); }

    // ---- Inventory ----

    public void giveItem(String itemId, int count) {
        ItemStack stack = makeItemStack(itemId, count, null);
        if (stack != null) player.getInventory().add(stack);
    }

    public void giveEnchantedItem(String itemId, int count, NativeObject enchants) {
        ItemStack stack = makeItemStack(itemId, count, enchants);
        if (stack != null) player.getInventory().add(stack);
    }

    public void giveNamedItem(String itemId, int count, String customName, Object lore) {
        ItemStack stack = makeItemStack(itemId, count, null);
        if (stack == null) return;
        if (customName != null && !customName.isEmpty()) {
            stack.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME,
                Component.literal(customName));
        }
        if (lore instanceof NativeObject lo) {
            var lines = new java.util.ArrayList<Component>();
            for (int i = 0; ; i++) {
                Object line = lo.get(i);
                if (line == null || line == org.mozilla.javascript.UniqueTag.NOT_FOUND) break;
                lines.add(Component.literal(line.toString()));
            }
            if (!lines.isEmpty()) {
                stack.set(net.minecraft.core.component.DataComponents.LORE,
                    new net.minecraft.world.item.component.ItemLore(lines));
            }
        }
        player.getInventory().add(stack);
    }

    public Object getHeldItem() {
        ItemStack stack = player.getMainHandItem();
        NativeObject result = new NativeObject();
        if (stack.isEmpty()) {
            ScriptableObject.putProperty(result, "id", "minecraft:air");
            ScriptableObject.putProperty(result, "count", 0);
            return result;
        }
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(stack.getItem());
        ScriptableObject.putProperty(result, "id", key.toString());
        ScriptableObject.putProperty(result, "count", stack.getCount());
        return result;
    }

    public void clearInventory() {
        player.getInventory().clearContent();
    }

    // ---- Advancements ----

    public void grantAdvancement(String advancementId) {
        ResourceLocation rl = ResourceLocation.tryParse(advancementId);
        if (rl == null) return;
        var holder = player.server.getAdvancements().get(rl);
        if (holder != null) {
            for (String criterion : holder.value().criteria().keySet()) {
                player.getAdvancements().award(holder, criterion);
            }
        }
    }

    public void revokeAdvancement(String advancementId) {
        ResourceLocation rl = ResourceLocation.tryParse(advancementId);
        if (rl == null) return;
        var holder = player.server.getAdvancements().get(rl);
        if (holder != null) {
            for (String criterion : holder.value().criteria().keySet()) {
                player.getAdvancements().revoke(holder, criterion);
            }
        }
    }

    // ---- Effects ----

    public void addEffect(String effectId, int duration, int amplifier) {
        addEffect(effectId, duration, amplifier, false);
    }

    public void addEffect(String effectId, int duration, int amplifier, boolean hideParticles) {
        trackIfSandboxed();
        var effect = Box3ScriptUtils.lookupMobEffect(effectId);
        if (effect != null) {
            player.addEffect(new MobEffectInstance(effect, duration, amplifier, false, !hideParticles, true));
        }
    }

    public void clearEffects() {
        trackIfSandboxed();
        player.removeAllEffects();
    }

    // ---- Sound ----

    public void playSound(String path, double volume, double pitch) {
        var sound = Box3ScriptUtils.lookupSoundEvent(path);
        if (sound != null) {
            player.playNotifySound(sound.value(), net.minecraft.sounds.SoundSource.PLAYERS, (float) volume, (float) pitch);
        }
    }

    // ---- Custom properties ----

    private void trackIfSandboxed() {
        engine.getSandbox().trackPlayer(engine.getCurrentProject(), player);
    }

    private Map<String, Object> props() {
        return engine.getCustomProps(player.getUUID());
    }

    @SuppressWarnings("unchecked")
    private <T> T getProp(String key, T defaultValue) {
        Object v = props().get(key);
        return v != null ? (T) v : defaultValue;
    }

    private void setProp(String key, Object value) {
        props().put(key, value);
    }

    private void updateAbility(Consumer<net.minecraft.world.entity.player.Abilities> updater) {
        updater.accept(player.getAbilities());
        player.onUpdateAbilities();
    }

    private ItemStack makeItemStack(String itemId, int count, NativeObject enchants) {
        var item = Box3ScriptUtils.lookupItem(itemId);
        if (item == null) return null;
        ItemStack stack = new ItemStack(item, Math.max(1, Math.min(count, 64)));
        if (enchants != null) {
            var enchRegistry = player.server.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
            for (Object key : enchants.keySet()) {
                String enchId = key.toString();
                int level = ((Number) enchants.get(key)).intValue();
                ResourceLocation enchRl = ResourceLocation.tryParse(enchId);
                if (enchRl == null) continue;
                var holder = enchRegistry.getHolder(enchRl);
                if (holder.isPresent()) {
                    stack.enchant(holder.get(), level);
                }
            }
        }
        return stack;
    }
}

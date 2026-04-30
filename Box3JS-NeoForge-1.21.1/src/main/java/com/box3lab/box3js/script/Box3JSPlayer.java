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

public class Box3JSPlayer {

    private final ServerPlayer player;
    private final MinecraftServer server;
    private final Box3ScriptEngine engine;

    public Box3JSPlayer(ServerPlayer player, MinecraftServer server, Box3ScriptEngine engine) {
        this.player = player;
        this.server = server;
        this.engine = engine;
        this.inventory = new InventoryNS(player);
        this.effect = new EffectNS(player);
        this.sound = new SoundNS(player);
    }

    public final InventoryNS inventory;
    public final EffectNS effect;
    public final SoundNS sound;

    // ---- Info ----

    public String getName() { return player.getGameProfile().getName(); }
    public String getUserId() { return player.getUUID().toString(); }

    // ---- Appearance ----

    public boolean getInvisible() { return player.isInvisible(); }
    public void setInvisible(boolean v) { player.setInvisible(v); }

    public double getScale() { return player.getScale(); }

    // ---- Movement ----

    public double getWalkSpeed() { return player.getAttributeValue(Attributes.MOVEMENT_SPEED); }
    public void setWalkSpeed(double v) {
        player.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(v);
    }

    public double getRunSpeed() { return player.getAttributeValue(Attributes.MOVEMENT_SPEED) * 1.3; }
    public void setRunSpeed(double v) {
        player.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(v / 1.3);
    }

    public double getJumpPower() { return player.getAttributeValue(Attributes.JUMP_STRENGTH); }
    public void setJumpPower(double v) {
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

    // ---- Fly / Spectator ----

    public boolean getCanFly() { return player.getAbilities().mayfly; }
    public void setCanFly(boolean v) {
        player.getAbilities().mayfly = v;
        player.onUpdateAbilities();
    }

    public boolean getSpectator() { return player.isSpectator(); }

    public double getFlySpeed() { return player.getAbilities().getFlyingSpeed(); }
    public void setFlySpeed(double v) {
        player.getAbilities().setFlyingSpeed((float) v);
        player.onUpdateAbilities();
    }

    // ---- Game Mode ----

    public String getGameMode() { return player.gameMode.getGameModeForPlayer().getName(); }
    public void setGameMode(Object v) {
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

    public void setRespawnPoint(GameVector3 pos) {
        player.setRespawnPosition(
            player.level().dimension(),
            new BlockPos((int) pos.x, (int) pos.y, (int) pos.z),
            0, true, false);
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

    // ---- Look at (MC extension) ----

    public void lookAt(double x, double y, double z) {
        double dx = x - player.getX();
        double dy = y - player.getEyeY();
        double dz = z - player.getZ();
        double hd = Math.sqrt(dx * dx + dz * dz);
        player.setYRot((float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0));
        player.setXRot((float) (-Math.toDegrees(Math.atan2(dy, hd))));
    }

    // ---- Command ----

    public void runCommand(String cmd) {
        net.minecraft.commands.CommandSourceStack source = player.createCommandSourceStack();
        server.getCommands().performPrefixedCommand(source, cmd);
    }

    // ---- XP / Food ----

    public int getXp() { return player.experienceLevel; }
    public void setXp(int v) { player.experienceLevel = v; }

    public int getFood() { return player.getFoodData().getFoodLevel(); }
    public void setFood(int v) { player.getFoodData().setFoodLevel(v); }

    public float getSaturation() { return player.getFoodData().getSaturationLevel(); }
    public void setSaturation(float v) { player.getFoodData().setSaturation(v); }

    // ---- Custom properties ----

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

    // ---- Namespace classes ----

    public static class InventoryNS {
        private final ServerPlayer player;
        InventoryNS(ServerPlayer player) { this.player = player; }

        public void give(String itemId, int count) {
            ResourceLocation rl = ResourceLocation.tryParse(itemId);
            if (rl == null) return;
            var item = BuiltInRegistries.ITEM.getOptional(rl);
            if (item.isPresent()) {
                ItemStack stack = new ItemStack(item.get(), Math.max(1, Math.min(count, 64)));
                player.getInventory().add(stack);
            }
        }

        public Object held() {
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

        public void clear() {
            player.getInventory().clearContent();
        }
    }

    public static class EffectNS {
        private final ServerPlayer player;
        EffectNS(ServerPlayer player) { this.player = player; }

        public void add(String effectId, int duration, int amplifier) {
            ResourceLocation rl = ResourceLocation.tryParse(effectId);
            if (rl == null) return;
            var effect = BuiltInRegistries.MOB_EFFECT.getHolder(rl);
            if (effect.isPresent()) {
                player.addEffect(new MobEffectInstance(effect.get(), duration, amplifier));
            }
        }

        public void clear() {
            player.removeAllEffects();
        }
    }

    public static class SoundNS {
        private final ServerPlayer player;
        SoundNS(ServerPlayer player) { this.player = player; }

        public void play(String path, double volume, double pitch) {
            ResourceLocation rl = ResourceLocation.tryParse(path);
            if (rl == null) return;
            var sound = net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.getOptional(rl);
            if (sound.isPresent()) {
                player.playNotifySound(sound.get(), net.minecraft.sounds.SoundSource.PLAYERS, (float) volume, (float) pitch);
            }
        }
    }
}

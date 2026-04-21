package com.box3lab.box3.block.entity;

import com.box3lab.box3.register.modelbe.PackModelBlockEntityRegistrar;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PackModelBlockEntity extends BlockEntity {
    private static final long RESPAWN_INTERVAL_TICKS = 20L;
    private static final String DISPLAY_TAG_PREFIX = "box3_pack_model:";

    private static final float SCALE_STEP = 0.1F;
    private static final float SCALE_MIN = 0.1F;
    private static final float SCALE_MAX = 4.0F;
    private static final float OFFSET_STEP = 0.05F;
    private static final float ROTATION_STEP = 15.0F;
    private static final Map<UUID, ConfigSnapshot> CONFIG_CLIPBOARD = new ConcurrentHashMap<>();

    private float scale = 1.0F;
    private float offsetX = 0.0F;
    private float offsetY = 0.0F;
    private float offsetZ = 0.0F;
    private float rotationOffset = 0.0F;
    private int modeIndex = 0;

    public PackModelBlockEntity(BlockPos pos, BlockState state) {
        super(PackModelBlockEntityRegistrar.typeFor(state.getBlock()), pos, state);
    }

    @Override
    public void setRemoved() {
        removeDisplaysAt(this.level, this.getBlockPos());
        super.setRemoved();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, PackModelBlockEntity blockEntity) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        String tag = displayTag(pos);
        List<Display.ItemDisplay> displays = findDisplays(serverLevel, pos, tag);
        if (displays.isEmpty()) {
            if (serverLevel.getGameTime() % RESPAWN_INTERVAL_TICKS != 0) {
                return;
            }
            spawnDisplay(serverLevel, pos, state, blockEntity, tag);
            return;
        }

        for (Display.ItemDisplay display : displays) {
            blockEntity.applyPose(serverLevel, pos, state, display);
        }
    }

    public void cycleMode(Player player) {
        this.modeIndex = (this.modeIndex + 1) % Mode.values().length;
        this.setChanged();
        player.displayClientMessage(Component.translatable("message.box3.model.config.mode",
                Component.translatable(currentMode().translationKey())), true);
    }

    public void adjustCurrentMode(ServerLevel level, BlockPos pos, BlockState state, int direction, Player player) {
        Mode mode = currentMode();
        switch (mode) {
            case SCALE -> this.scale = clamp(this.scale + SCALE_STEP * direction, SCALE_MIN, SCALE_MAX);
            case OFFSET_X -> this.offsetX += OFFSET_STEP * direction;
            case OFFSET_Y -> this.offsetY += OFFSET_STEP * direction;
            case OFFSET_Z -> this.offsetZ += OFFSET_STEP * direction;
            case ROTATION -> this.rotationOffset = normalizeDegrees(this.rotationOffset + ROTATION_STEP * direction);
        }

        this.setChanged();
        player.displayClientMessage(statusComponent(), true);
        applyToDisplays(level, pos, state);
    }

    public void copyConfig(Player player) {
        CONFIG_CLIPBOARD.put(player.getUUID(),
                new ConfigSnapshot(this.scale, this.offsetX, this.offsetY, this.offsetZ, this.rotationOffset));
        player.displayClientMessage(Component.translatable("message.box3.model.config.copy.success"), true);
    }

    public void pasteConfig(ServerLevel level, BlockPos pos, BlockState state, Player player) {
        ConfigSnapshot snapshot = CONFIG_CLIPBOARD.get(player.getUUID());
        if (snapshot == null) {
            player.displayClientMessage(Component.translatable("message.box3.model.config.copy.empty"), true);
            return;
        }

        this.scale = clamp(snapshot.scale, SCALE_MIN, SCALE_MAX);
        this.offsetX = snapshot.offsetX;
        this.offsetY = snapshot.offsetY;
        this.offsetZ = snapshot.offsetZ;
        this.rotationOffset = normalizeDegrees(snapshot.rotationOffset);
        this.setChanged();

        applyToDisplays(level, pos, state);
        player.displayClientMessage(Component.translatable("message.box3.model.config.copy.pasted"), true);
        player.displayClientMessage(statusComponent(), true);
    }

    public static void removeDisplaysAt(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        String tag = displayTag(pos);
        for (Display.ItemDisplay display : findDisplays(serverLevel, pos, tag)) {
            display.discard();
        }
    }

    private static void spawnDisplay(ServerLevel level, BlockPos pos, BlockState state, PackModelBlockEntity be,
            String tag) {
        Display.ItemDisplay display = EntityType.ITEM_DISPLAY.create(level);
        if (display == null) {
            return;
        }

        display.setNoGravity(true);
        display.setInvulnerable(true);
        display.getSlot(0).set(new ItemStack(state.getBlock()));
        display.addTag(tag);

        be.applyPose(level, pos, state, display);
        level.addFreshEntity(display);
    }

    private void applyPose(ServerLevel level, BlockPos pos, BlockState state, Display.ItemDisplay display) {
        double x = pos.getX() + 0.5D;
        double y = pos.getY() + this.offsetY;
        double z = pos.getZ() + 0.5D;
        display.setPos(x, y, z);

        float baseYaw = 0.0F;
        if (state.hasProperty(PackModelEntityBlock.HORIZONTAL_FACING)) {
            Direction facing = state.getValue(PackModelEntityBlock.HORIZONTAL_FACING);
            baseYaw = facing.toYRot();
        }
        display.setYRot(normalizeDegrees(baseYaw + this.rotationOffset));

        applyDisplayTransformation(level, display);
    }

    private void applyDisplayTransformation(ServerLevel level, Display.ItemDisplay display) {
        MinecraftServer server = level.getServer();
        CommandSourceStack source = server.createCommandSourceStack().withSuppressedOutput().withPermission(4);
        String cmd = String.format(
                Locale.ROOT,
                "data merge entity %s {item_display:\"fixed\",transformation:{translation:[%sf,%sf,%sf],left_rotation:[0f,0f,0f,1f],scale:[%sf,%sf,%sf],right_rotation:[0f,0f,0f,1f]}}",
                display.getStringUUID(),
                fmt(this.offsetX), fmt(0.0F), fmt(this.offsetZ),
                fmt(this.scale), fmt(this.scale), fmt(this.scale));
        server.getCommands().performPrefixedCommand(source, cmd);
    }

    private static String fmt(float value) {
        return String.format(Locale.ROOT, "%.3f", value);
    }

    private static List<Display.ItemDisplay> findDisplays(ServerLevel level, BlockPos pos, String tag) {
        return level.getEntitiesOfClass(Display.ItemDisplay.class, new AABB(pos).inflate(0.25D),
                display -> display.getTags().contains(tag));
    }

    private void applyToDisplays(ServerLevel level, BlockPos pos, BlockState state) {
        for (Display.ItemDisplay display : findDisplays(level, pos, displayTag(pos))) {
            applyPose(level, pos, state, display);
        }
    }

    private Mode currentMode() {
        return Mode.values()[this.modeIndex];
    }

    private Component statusComponent() {
        return Component.translatable("message.box3.model.config.status",
                Component.translatable(currentMode().translationKey()),
                String.format(Locale.ROOT, "%.2f", this.scale),
                String.format(Locale.ROOT, "%.2f", this.offsetX),
                String.format(Locale.ROOT, "%.2f", this.offsetY),
                String.format(Locale.ROOT, "%.2f", this.offsetZ),
                String.format(Locale.ROOT, "%.1f", this.rotationOffset));
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private static float normalizeDegrees(float value) {
        float normalized = value % 360.0F;
        return normalized < 0.0F ? normalized + 360.0F : normalized;
    }

    private static String displayTag(BlockPos pos) {
        return DISPLAY_TAG_PREFIX + pos.asLong();
    }

    private enum Mode {
        SCALE("scale"),
        OFFSET_X("offset_x"),
        OFFSET_Y("offset_y"),
        OFFSET_Z("offset_z"),
        ROTATION("rotation");

        private final String keyPart;

        Mode(String keyPart) {
            this.keyPart = keyPart;
        }

        public String translationKey() {
            return "message.box3.model.config.mode." + this.keyPart;
        }
    }

    private record ConfigSnapshot(float scale, float offsetX, float offsetY, float offsetZ, float rotationOffset) {
    }
}

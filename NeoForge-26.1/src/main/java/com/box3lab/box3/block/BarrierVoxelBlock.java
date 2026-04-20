package com.box3lab.box3.block;

import com.box3lab.box3.util.ConfigUtil;
import com.google.gson.JsonObject;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class BarrierVoxelBlock extends VoxelBlock {
    public static final MapCodec<BarrierVoxelBlock> CODEC = simpleCodec(BarrierVoxelBlock::new);
    private static volatile boolean visible = false;

    static {
        loadConfig();
    }

    public BarrierVoxelBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return visible ? 0.0F : 1.0F;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return visible ? RenderShape.MODEL : RenderShape.INVISIBLE;
    }

    public static boolean isVisible() {
        return visible;
    }

    public static void setVisible(boolean value) {
        visible = value;
        saveConfig();
    }

    private static void loadConfig() {
        JsonObject obj = ConfigUtil.readConfig(ConfigUtil.CONFIG_DIR_NAME);
        if (obj == null) {
            return;
        }
        if (obj.has("barrierVisible")) {
            visible = obj.get("barrierVisible").getAsBoolean();
        }
    }

    private static void saveConfig() {
        ConfigUtil.updateConfig(ConfigUtil.CONFIG_DIR_NAME, obj -> obj.addProperty("barrierVisible", visible));
    }
}

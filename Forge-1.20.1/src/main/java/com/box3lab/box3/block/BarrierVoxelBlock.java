package com.box3lab.box3.block;

import com.box3lab.box3.util.ConfigUtil;
import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class BarrierVoxelBlock extends VoxelBlock {
    private static final String KEY_VISIBLE = "barrierVisible";
    private static volatile boolean visible = false;

    static {
        loadConfig();
    }

    public BarrierVoxelBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return visible ? 0.0F : 1.0F;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
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
        JsonObject obj = ConfigUtil.readConfig(ConfigUtil.CONFIG_FILE_NAME);
        if (obj == null) {
            return;
        }
        if (obj.has(KEY_VISIBLE)) {
            visible = obj.get(KEY_VISIBLE).getAsBoolean();
        }
    }

    private static void saveConfig() {
        ConfigUtil.updateConfig(ConfigUtil.CONFIG_FILE_NAME, json -> json.addProperty(KEY_VISIBLE, visible));
    }
}

package com.box3lab.box3.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class GlassVoxelBlock extends VoxelBlock {
    public static final MapCodec<GlassVoxelBlock> CODEC = simpleCodec(GlassVoxelBlock::new);

    public GlassVoxelBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected boolean skipRendering(BlockState state, BlockState adjacentState, Direction side) {
        if (adjacentState.getBlock() == state.getBlock()) {
            return true;
        }
        return super.skipRendering(state, adjacentState, side);
    }
}

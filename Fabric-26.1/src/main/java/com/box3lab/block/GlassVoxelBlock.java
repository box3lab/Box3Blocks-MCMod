package com.box3lab.block;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class GlassVoxelBlock extends VoxelBlock {

    public GlassVoxelBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public boolean skipRendering(BlockState state, BlockState adjacentState, Direction side) {
        if (adjacentState.getBlock() == state.getBlock()) {
            return true;
        }
        return super.skipRendering(state, adjacentState, side);
    }
}

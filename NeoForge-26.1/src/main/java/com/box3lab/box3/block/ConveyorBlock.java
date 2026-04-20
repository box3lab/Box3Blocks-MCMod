package com.box3lab.box3.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class ConveyorBlock extends VoxelBlock {
    public static final MapCodec<ConveyorBlock> CODEC = simpleCodec(ConveyorBlock::new);

    public ConveyorBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        super.stepOn(level, pos, state, entity);

        Direction facing = state.getValue(HORIZONTAL_FACING);
        double speed = 0.06D;
        Vec3 velocity = entity.getDeltaMovement();

        switch (facing) {
            case NORTH -> velocity = new Vec3(velocity.x, velocity.y, velocity.z - speed);
            case SOUTH -> velocity = new Vec3(velocity.x, velocity.y, velocity.z + speed);
            case WEST -> velocity = new Vec3(velocity.x - speed, velocity.y, velocity.z);
            case EAST -> velocity = new Vec3(velocity.x + speed, velocity.y, velocity.z);
            default -> {
            }
        }

        entity.setDeltaMovement(velocity);
    }
}

package com.box3lab.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class ConveyorBlock extends VoxelBlock {

    public ConveyorBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        super.stepOn(level, pos, state, entity);

        Direction facing = state.getValue(HORIZONTAL_FACING);
        double speed = 0.06D;

        Vec3 vel = entity.getDeltaMovement();
        switch (facing) {
            case NORTH -> vel = new Vec3(vel.x, vel.y, vel.z - speed);
            case SOUTH -> vel = new Vec3(vel.x, vel.y, vel.z + speed);
            case WEST  -> vel = new Vec3(vel.x - speed, vel.y, vel.z);
            case EAST  -> vel = new Vec3(vel.x + speed, vel.y, vel.z);
            default -> { }
        }

        entity.setDeltaMovement(vel);

    }
}

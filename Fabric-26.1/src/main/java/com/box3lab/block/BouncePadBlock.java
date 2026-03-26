package com.box3lab.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class BouncePadBlock extends VoxelBlock {

    public BouncePadBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        super.stepOn(level, pos, state, entity);

        if (entity.getY() < pos.getY() + 0.5D) {
            return;
        }

        Vec3 vel = entity.getDeltaMovement();

        double bounce = 0.7D + level.getRandom().nextDouble() * 0.3D;

        if (vel.y < bounce) {
            vel = new Vec3(vel.x, bounce, vel.z);
        }

        entity.setDeltaMovement(vel);
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, double fallDistance) {

        if (entity.isSuppressingBounce()) {
            super.fallOn(level, state, pos, entity, fallDistance);
        } else {
            entity.resetFallDistance();
        }
    }
}

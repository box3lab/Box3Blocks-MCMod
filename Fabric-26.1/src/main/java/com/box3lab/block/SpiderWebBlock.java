package com.box3lab.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class SpiderWebBlock extends VoxelBlock {

    public SpiderWebBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        super.stepOn(level, pos, state, entity);

        if (entity instanceof LivingEntity) {
            LivingEntity living = (LivingEntity) entity;
   
            
            boolean isSprinting = living.isSprinting();
            
            if (isSprinting) {
                Vec3 movement = living.getDeltaMovement();
                living.setDeltaMovement(movement.multiply(0.15, 1.0, 0.15));
            } else {
                living.setDeltaMovement(Vec3.ZERO);
            }
        }
    }
}

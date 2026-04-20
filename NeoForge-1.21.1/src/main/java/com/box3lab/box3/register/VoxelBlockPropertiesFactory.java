package com.box3lab.box3.register;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public final class VoxelBlockPropertiesFactory {
    private VoxelBlockPropertiesFactory() {
    }

    public static BlockBehaviour.Properties create(boolean solid, SoundType soundType, int lightLevel, float hardness,
            float resistance, float friction) {
        BlockBehaviour.Properties props = BlockBehaviour.Properties.of()
                .sound(soundType)
                .mapColor(solid ? MapColor.COLOR_CYAN : MapColor.NONE)
                .lightLevel(state -> lightLevel)
                .strength(hardness, resistance)
                .friction(friction);

        if (!solid) {
            props = props.noOcclusion();
        }

        return props;
    }
}

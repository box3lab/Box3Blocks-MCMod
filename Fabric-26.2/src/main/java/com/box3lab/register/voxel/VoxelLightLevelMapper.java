package com.box3lab.register.voxel;

public final class VoxelLightLevelMapper {
    private VoxelLightLevelMapper() {
    }

    public static int lightLevelFromEmissivePacked(int emissivePacked) {
        int rawLight = emissivePacked == 0 ? 0 : (int) Math.round(15.0 * (0.8 + 0.2 * emissivePacked / 4095.0));
        return Math.max(0, Math.min(15, rawLight));
    }
}

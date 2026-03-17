package com.box3lab.util;

import java.util.Locale;

public final class BlockIndexUtil {
    private BlockIndexUtil() {
    }

    public static final class RGBA {
        public final int r;
        public final int g;
        public final int b;
        public final float a;

        public RGBA(int r, int g, int b, float a) {
            this.r = r;
            this.g = g;
            this.b = b;
            this.a = a;
        }
    }

    public static int normalizeId(int blockId) {
        return blockId & 4095;
    }

    public static String getNameById(int blockId) {
        int id = normalizeId(blockId);
        BlockIndexData data = BlockIndexData.get();
        Integer idx = data.indexById.get(id);
        if (idx == null) {
            return null;
        }
        if (idx < 0 || idx >= data.names.length) {
            return null;
        }
        return data.names[idx];
    }

    public static Integer getIdByName(String name) {
        if (name == null) {
            return null;
        }
        BlockIndexData data = BlockIndexData.get();
        return data.idByName.get(name);
    }

    public static boolean isFluid(int blockId) {
        int id = normalizeId(blockId);
        return BlockIndexData.get().fluidsById.containsKey(id);
    }

    public static boolean isSolid(int blockId) {
        int id = normalizeId(blockId);
        return !BlockIndexData.get().notSolidIds.contains(id);
    }

    public static boolean isSoild(int blockId) {
        return isSolid(blockId);
    }

    public static int blockEmissiveLight(int blockId) {
        int id = normalizeId(blockId);
        BlockIndexData data = BlockIndexData.get();
        Integer idx = data.indexById.get(id);
        if (idx == null) {
            return 0;
        }
        if (idx < 0 || idx >= data.emissive.length) {
            return 0;
        }
        return data.emissive[idx];
    }

    public static RGBA getFluidColor(int blockId) {
        int id = normalizeId(blockId);
        BlockIndexData.FluidInfo fluid = BlockIndexData.get().fluidsById.get(id);
        if (fluid == null) {
            return null;
        }
        long info = fluid.info;

        int r = (int) (info & 255L);
        int g = (int) ((info & 65280L) >> 8);
        int b = (int) ((info & 16711680L) >>> 16);
        float a = (float) (((info >>> 24) & 255L) / 255.0);

        return new RGBA(r, g, b, a);
    }

    public static String getVoxelNameLowerCaseById(int blockId) {
        String name = getNameById(blockId);
        if (name == null) {
            return null;
        }
        return name.toLowerCase(Locale.ROOT);
    }
}

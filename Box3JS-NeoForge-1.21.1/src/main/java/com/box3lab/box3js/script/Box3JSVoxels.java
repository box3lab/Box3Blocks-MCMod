package com.box3lab.box3js.script;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

import java.util.*;

public class Box3JSVoxels {

    static final int ROTATION_MULTIPLIER = 16384;

    private static final Direction[] ROTATION_TO_DIRECTION = {
        Direction.SOUTH,  // 0 = 0°
        Direction.WEST,   // 1 = 90°
        Direction.NORTH,  // 2 = 180°
        Direction.EAST    // 3 = 270°
    };

    private static final Map<Direction, Integer> DIRECTION_TO_ROTATION = Map.of(
        Direction.SOUTH, 0,
        Direction.WEST, 1,
        Direction.NORTH, 2,
        Direction.EAST, 3
    );

    private final MinecraftServer server;
    private final Map<String, Integer> nameToId = new HashMap<>();
    private final Map<Integer, String> idToName = new HashMap<>();
    private final Map<String, Block> resourceToBlock = new HashMap<>();
    private final Map<Block, Integer> blockToId = new HashMap<>();

    // Public fields for JS access matching Box3 API naming
    public final GameVector3 shape;
    public final String[] VoxelTypes;

    public Box3JSVoxels(MinecraftServer server) {
        this.server = server;

        nameToId.put("air", 0);
        idToName.put(0, "air");

        List<String> types = new ArrayList<>();
        types.add("air");

        int nextId = 1;
        for (var entry : BuiltInRegistries.BLOCK.entrySet()) {
            Block block = entry.getValue();
            if (block == Blocks.AIR) continue;
            ResourceLocation key = entry.getKey().location();
            String fullName = key.toString();
            String path = key.getPath();

            nameToId.put(fullName, nextId);
            if (!fullName.equals(path)) nameToId.put(path, nextId);
            idToName.put(nextId, fullName);
            resourceToBlock.put(fullName, block);
            resourceToBlock.put(path.toLowerCase(Locale.ROOT), block);
            blockToId.put(block, nextId);
            types.add(fullName);
            nextId++;
        }
        this.VoxelTypes = types.toArray(new String[0]);

        ServerLevel level = server.overworld();
        int h = level != null ? level.getMaxBuildHeight() : 256;
        this.shape = new GameVector3(h, h, h);
    }

    // ---- Name / ID mapping ----

    public int id(String name) {
        if (name == null || name.equalsIgnoreCase("air")) return 0;
        Integer id = nameToId.get(name);
        if (id != null) return id;
        // Try vanilla block by ResourceLocation
        ResourceLocation rl = ResourceLocation.tryParse(name);
        if (rl != null) {
            Block block = BuiltInRegistries.BLOCK.getOptional(rl).orElse(null);
            if (block != null && block != Blocks.AIR) {
                Integer foundId = blockToId.get(block);
                if (foundId != null) return foundId;
            }
        }
        return 0;
    }

    public String name(int id) {
        if (id == 0) return "air";
        int baseId = id % ROTATION_MULTIPLIER;
        String n = idToName.get(baseId);
        if (n != null) return n;
        return "air";
    }

    // ---- Write ----

    /** setVoxel(x, y, z, voxel: number|string): number */
    public int setVoxel(int x, int y, int z, Object voxel) {
        return setVoxel(x, y, z, voxel, 0);
    }
    /** setVoxel(pos, voxel): number */
    public int setVoxel(GameVector3 pos, Object voxel) {
        return setVoxel((int) pos.x, (int) pos.y, (int) pos.z, voxel, 0);
    }

    /** setVoxel(x, y, z, voxel: number|string, rotation?: number|string): number */
    public int setVoxel(int x, int y, int z, Object voxel, Object rotation) {
        ServerLevel level = server.overworld();
        BlockPos pos = new BlockPos(x, y, z);

        if (voxel == null) return 0;

        // Resolve "air" or 0 → remove block
        if (isAir(voxel)) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            return 0;
        }

        Block block = resolveBlock(voxel);
        if (block == null) return 0;

        int rot = coerceRotation(rotation);
        BlockState state = applyRotation(block.defaultBlockState(), rot);
        level.setBlock(pos, state, 3);

        Integer baseId = blockToId.get(block);
        return baseId != null ? rot * ROTATION_MULTIPLIER + baseId : 0;
    }
    /** setVoxel(pos, voxel, rotation): number */
    public int setVoxel(GameVector3 pos, Object voxel, Object rotation) {
        return setVoxel((int) pos.x, (int) pos.y, (int) pos.z, voxel, rotation);
    }

    /** fillVoxel(x1, y1, z1, x2, y2, z2, voxel): void — fill a region */
    public void fillVoxel(int x1, int y1, int z1, int x2, int y2, int z2, Object voxel) {
        int minX = Math.min(x1, x2), maxX = Math.max(x1, x2);
        int minY = Math.min(y1, y2), maxY = Math.max(y1, y2);
        int minZ = Math.min(z1, z2), maxZ = Math.max(z1, z2);
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    setVoxel(x, y, z, voxel);
                }
            }
        }
    }
    /** fillVoxel(pos1, pos2, voxel): void */
    public void fillVoxel(GameVector3 pos1, GameVector3 pos2, Object voxel) {
        fillVoxel((int) pos1.x, (int) pos1.y, (int) pos1.z, (int) pos2.x, (int) pos2.y, (int) pos2.z, voxel);
    }

    /** countVoxel(x1, y1, z1, x2, y2, z2, voxel): number — count matching blocks in region */
    public int countVoxel(int x1, int y1, int z1, int x2, int y2, int z2, Object voxel) {
        var targetBlock = resolveBlock(voxel);
        if (targetBlock == null) return 0;

        int minX = Math.min(x1, x2), maxX = Math.max(x1, x2);
        int minY = Math.min(y1, y2), maxY = Math.max(y1, y2);
        int minZ = Math.min(z1, z2), maxZ = Math.max(z1, z2);

        int count = 0;
        var level = server.overworld();
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    var state = level.getBlockState(new BlockPos(x, y, z));
                    if (state.getBlock() == targetBlock) count++;
                }
            }
        }
        return count;
    }
    /** countVoxel(pos1, pos2, voxel): number */
    public int countVoxel(GameVector3 pos1, GameVector3 pos2, Object voxel) {
        return countVoxel((int) pos1.x, (int) pos1.y, (int) pos1.z, (int) pos2.x, (int) pos2.y, (int) pos2.z, voxel);
    }

    /** setVoxelId(x, y, z, voxel: number): number — rotation already encoded in the ID */
    public int setVoxelId(int x, int y, int z, int voxel) {
        ServerLevel level = server.overworld();
        BlockPos pos = new BlockPos(x, y, z);

        int rot = voxel / ROTATION_MULTIPLIER;
        int baseId = voxel % ROTATION_MULTIPLIER;

        if (baseId == 0) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            return 0;
        }

        Block block = resolveBlock(voxel);
        if (block == null) return 0;

        BlockState state = applyRotation(block.defaultBlockState(), rot);
        level.setBlock(pos, state, 3);
        return voxel;
    }
    /** setVoxelId(pos, voxel): number */
    public int setVoxelId(GameVector3 pos, int voxel) {
        return setVoxelId((int) pos.x, (int) pos.y, (int) pos.z, voxel);
    }

    // ---- Read ----

    /** getVoxel(x, y, z): number — base block ID, or 0 for air / unknown. */
    public int getVoxel(int x, int y, int z) {
        ServerLevel level = server.overworld();
        BlockState state = level.getBlockState(new BlockPos(x, y, z));
        if (state.isAir()) return 0;
        Integer id = blockToId.get(state.getBlock());
        return id != null ? id : 0;
    }
    /** getVoxel(pos): number */
    public int getVoxel(GameVector3 pos) {
        return getVoxel((int) pos.x, (int) pos.y, (int) pos.z);
    }

    /** getVoxelId(x, y, z): number — full ID with rotation encoded */
    public int getVoxelId(int x, int y, int z) {
        ServerLevel level = server.overworld();
        BlockState state = level.getBlockState(new BlockPos(x, y, z));
        if (state.isAir()) return 0;
        Integer boxId = blockToId.get(state.getBlock());
        if (boxId == null) return 0;
        int baseId = boxId;
        int rot = extractRotation(state);
        return rot * ROTATION_MULTIPLIER + baseId;
    }
    /** getVoxelId(pos): number */
    public int getVoxelId(GameVector3 pos) {
        return getVoxelId((int) pos.x, (int) pos.y, (int) pos.z);
    }

    /** getVoxelName(x, y, z): string — returns ResourceLocation name of block at position (e.g. "minecraft:stone"). */
    public String getVoxelName(int x, int y, int z) {
        ServerLevel level = server.overworld();
        BlockState state = level.getBlockState(new BlockPos(x, y, z));
        if (state.isAir()) return "air";
        Integer boxId = blockToId.get(state.getBlock());
        if (boxId != null) {
            String n = idToName.get(boxId);
            if (n != null) return n;
        }
        return BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
    }
    /** getVoxelName(pos): string */
    public String getVoxelName(GameVector3 pos) {
        return getVoxelName((int) pos.x, (int) pos.y, (int) pos.z);
    }

    /** setSpawner(x, y, z, entityType) */
    public void setSpawner(int x, int y, int z, String entityType) {
        ServerLevel level = server.overworld();
        BlockPos pos = new BlockPos(x, y, z);
        var be = level.getBlockEntity(pos);
        if (!(be instanceof net.minecraft.world.level.block.entity.SpawnerBlockEntity spawnerBe)) return;

        ResourceLocation rl = ResourceLocation.tryParse(entityType);
        if (rl == null) return;
        var opt = BuiltInRegistries.ENTITY_TYPE.getOptional(rl);
        if (opt.isEmpty()) return;

        spawnerBe.setEntityId(opt.get(), level.getRandom());
    }
    public void setSpawner(GameVector3 pos, String entityType) {
        setSpawner((int) pos.x, (int) pos.y, (int) pos.z, entityType);
    }

    /** getVoxelRotation(x, y, z): number — 0, 1, 2, 3 */
    public int getVoxelRotation(int x, int y, int z) {
        ServerLevel level = server.overworld();
        BlockState state = level.getBlockState(new BlockPos(x, y, z));
        return extractRotation(state);
    }
    /** getVoxelRotation(pos): number */
    public int getVoxelRotation(GameVector3 pos) {
        return getVoxelRotation((int) pos.x, (int) pos.y, (int) pos.z);
    }

    /** Resolve Box3 numeric ID from a BlockState. Returns 0 for non-Box3/air blocks. */
    public int getId(BlockState state) {
        if (state.isAir()) return 0;
        Integer id = blockToId.get(state.getBlock());
        return id != null ? id : 0;
    }

    // ---- Internals ----

    private boolean isAir(Object voxel) {
        if (voxel instanceof Number n) return n.intValue() == 0;
        if (voxel instanceof String s) return s.equalsIgnoreCase("air");
        return false;
    }

    private Block resolveBlock(Object voxel) {
        if (voxel instanceof Number n) {
            int baseId = n.intValue() % ROTATION_MULTIPLIER;
            if (baseId == 0) return null;
            // Check Box3 block first
            String name = idToName.get(baseId);
            if (name != null) {
                Block b = resourceToBlock.get(name.toLowerCase(Locale.ROOT));
                if (b != null) return b;
            }
            return null;
        }
        if (voxel instanceof String s) {
            // Try Box3 block first
            Block b = resourceToBlock.get(s.toLowerCase(Locale.ROOT));
            if (b != null) return b;
            // Try vanilla block by ResourceLocation (e.g. "minecraft:stone" or "stone")
            ResourceLocation rl = ResourceLocation.tryParse(s);
            if (rl == null) return null;
            return BuiltInRegistries.BLOCK.getOptional(rl).orElse(null);
        }
        return null;
    }

    private int coerceRotation(Object rotation) {
        if (rotation instanceof Number n) {
            int r = n.intValue();
            return (r < 0 || r > 3) ? 0 : r;
        }
        if (rotation instanceof String s) {
            try { return Integer.parseInt(s); } catch (NumberFormatException e) { return 0; }
        }
        return 0;
    }

    private BlockState applyRotation(BlockState state, int rot) {
        if (rot == 0) return state;
        Direction dir = ROTATION_TO_DIRECTION[rot];
        if (state.hasProperty(HorizontalDirectionalBlock.FACING)) {
            return state.setValue(HorizontalDirectionalBlock.FACING, dir);
        }
        for (var prop : state.getProperties()) {
            if (prop instanceof DirectionProperty dp && dp.getName().equals("facing")) {
                if (dp.getPossibleValues().contains(dir)) {
                    return state.setValue(dp, dir);
                }
            }
        }
        return state;
    }

    private int extractRotation(BlockState state) {
        if (state.isAir()) return 0;
        if (state.hasProperty(HorizontalDirectionalBlock.FACING)) {
            return DIRECTION_TO_ROTATION.getOrDefault(state.getValue(HorizontalDirectionalBlock.FACING), 0);
        }
        for (var prop : state.getProperties()) {
            if (prop instanceof DirectionProperty dp && dp.getName().equals("facing")) {
                return DIRECTION_TO_ROTATION.getOrDefault(state.getValue(dp), 0);
            }
        }
        return 0;
    }
}

package com.box3lab.box3.register;

import com.box3lab.box3.util.BlockIdResolver;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import static com.box3lab.box3.Box3Blocks.MODID;

public final class VoxelExport {
    private static final int[] DEFAULT_DIR = new int[] {1, 1, 1};

    private VoxelExport() {
    }

    public static ExportResult exportRegion(ServerLevel level, BlockPos from, BlockPos to, String fileName) throws IOException {
        BlockPos min = new BlockPos(
                Math.min(from.getX(), to.getX()),
                Math.min(from.getY(), to.getY()),
                Math.min(from.getZ(), to.getZ()));
        BlockPos max = new BlockPos(
                Math.max(from.getX(), to.getX()),
                Math.max(from.getY(), to.getY()),
                Math.max(from.getZ(), to.getZ()));

        int sizeX = max.getX() - min.getX() + 1;
        int sizeY = max.getY() - min.getY() + 1;
        int sizeZ = max.getZ() - min.getZ() + 1;

        List<Integer> indices = new ArrayList<>();
        List<Integer> data = new ArrayList<>();
        List<Integer> rot = new ArrayList<>();

        for (int z = 0; z < sizeZ; z++) {
            for (int y = 0; y < sizeY; y++) {
                for (int x = 0; x < sizeX; x++) {
                    BlockPos pos = min.offset(x, y, z);
                    BlockState state = level.getBlockState(pos);
                    int id = BlockIdResolver.getIdByBlock(state.getBlock());
                    if (id == 0) {
                        continue;
                    }

                    int idx = x + y * sizeX + z * sizeX * sizeY;
                    indices.add(idx);
                    data.add(id);
                    rot.add(toRotationIndex(state));
                }
            }
        }

        JsonObject root = new JsonObject();
        root.add("shape", intArray(sizeX, sizeY, sizeZ));
        root.add("dir", intArray(DEFAULT_DIR[0], DEFAULT_DIR[1], DEFAULT_DIR[2]));
        root.add("indices", toJsonArray(indices));
        root.add("data", toJsonArray(data));
        root.add("rot", toJsonArray(rot));

        Path output = resolveOutput(fileName);
        Files.createDirectories(output.getParent());
        writeGzipJson(output, root.toString());

        return new ExportResult(output, sizeX * sizeY * sizeZ, indices.size());
    }

    private static int toRotationIndex(BlockState state) {
        Direction dir = null;
        if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            dir = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        } else if (state.hasProperty(BlockStateProperties.FACING)) {
            Direction facing = state.getValue(BlockStateProperties.FACING);
            if (facing.getAxis().isHorizontal()) {
                dir = facing;
            }
        }

        if (dir == null) {
            return 0;
        }

        Rotation rotation = switch (dir) {
            case EAST -> Rotation.CLOCKWISE_90;
            case SOUTH -> Rotation.CLOCKWISE_180;
            case WEST -> Rotation.COUNTERCLOCKWISE_90;
            default -> Rotation.NONE;
        };

        return switch (rotation) {
            case CLOCKWISE_90 -> 1;
            case CLOCKWISE_180 -> 2;
            case COUNTERCLOCKWISE_90 -> 3;
            default -> 0;
        };
    }

    private static JsonArray intArray(int a, int b, int c) {
        JsonArray array = new JsonArray();
        array.add(a);
        array.add(b);
        array.add(c);
        return array;
    }

    private static JsonArray toJsonArray(List<Integer> values) {
        JsonArray array = new JsonArray();
        for (Integer value : values) {
            array.add(value);
        }
        return array;
    }

    private static Path resolveOutput(String fileName) {
        String cleaned = (fileName == null || fileName.isBlank()) ? "export" : fileName.trim();
        if (!cleaned.endsWith(".gz")) {
            cleaned = cleaned + ".gz";
        }
        return FMLPaths.CONFIGDIR.get()
                .resolve(MODID)
                .resolve(cleaned);
    }

    private static void writeGzipJson(Path outputPath, String json) throws IOException {
        try (OutputStream fos = Files.newOutputStream(outputPath);
                GZIPOutputStream gos = new GZIPOutputStream(fos)) {
            gos.write(json.getBytes(StandardCharsets.UTF_8));
            gos.finish();
        }
    }

    public record ExportResult(Path output, int scannedBlocks, int exportedBlocks) {
    }
}

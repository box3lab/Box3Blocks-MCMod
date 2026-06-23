package com.box3lab.register;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;

import static com.box3lab.Box3.MOD_ID;
import com.box3lab.util.BlockIdResolver;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.Vec3;

public final class VoxelImport {

    public static void apply(Object voxels, Object world, String mapName, Vec3 customOrigin,
            ServerPlayer player, boolean ignoreBarrier, boolean ignoreWater) throws Exception {
        JsonObject cfg = loadConfig(mapName);
        if (cfg == null) {
            throw new Exception(Component
                    .translatable("command.box3.box3import.config_invalid")
                    .getString());
        }

        int[] shape = toIntArray(cfg.getAsJsonArray("shape"));
        int[] dir = toIntArray(cfg.getAsJsonArray("dir"));
        int[] indices = toIntArray(cfg.getAsJsonArray("indices"));
        int[] data = toIntArray(cfg.getAsJsonArray("data"));
        int[] rot = toIntArray(cfg.getAsJsonArray("rot"));

        int total = Math.min(indices.length, data.length);
        int lastProgress = -1;

        int[] origin = new int[] {
                (int) Math.floor(customOrigin.x),
                (int) Math.floor(customOrigin.y),
                (int) Math.floor(customOrigin.z)
        };

        for (int i = 0; i < indices.length && i < data.length; i++) {
            int idx = indices[i];
            int id = data[i];

            if (ignoreBarrier && BlockIdResolver.isBarrierId(id)) {
                continue;
            }

            int x = idx % shape[0];
            int y = (idx / shape[0]) % shape[1];
            int z = idx / (shape[0] * shape[1]);
            int r = rot.length > i ? rot[i] : 0;
            int wx = origin[0] + dir[0] * x;
            int wy = origin[1] + dir[1] * y;
            int wz = origin[2] + dir[2] * z;

            if (world instanceof ServerLevel level) {
                BlockPos pos = new BlockPos(wx, wy, wz);
                Block block = BlockIdResolver.getBlockById(id, ignoreWater);
                Rotation rotation = switch (r & 3) {
                    case 1 -> Rotation.CLOCKWISE_90;
                    case 2 -> Rotation.CLOCKWISE_180;
                    case 3 -> Rotation.COUNTERCLOCKWISE_90;
                    default -> Rotation.NONE;
                };

                level.setBlock(pos, block.defaultBlockState().rotate(rotation), 3);

                if (player != null) {
                    int progress = (i + 1) * 100 / total;
                    if (progress / 10 > lastProgress / 10) {
                        lastProgress = progress;
                        player.sendSystemMessage(Component.translatable(
                                "command.box3.box3import.progress", mapName, progress));
                    }
                }
            }
        }
    }

    private static JsonObject loadConfig(String mapName) {
        if (mapName.startsWith("http://") || mapName.startsWith("https://")) {
            try {
                String json = readGzipJsonFromUrl(mapName);
                return JsonParser.parseString(json).getAsJsonObject();
            } catch (IOException | JsonParseException e) {
                System.err.println(Component
                        .translatable("command.box3.box3import.config_read_failed", mapName)
                        .getString());
                return null;
            }
        }

        // 否则仍然从本地 config 目录读取
        Path configPath = FabricLoader.getInstance()
                .getConfigDir()
                .resolve(MOD_ID)
                .resolve(mapName.endsWith(".gz") ? mapName : mapName + ".gz");

        if (!Files.exists(configPath)) {
            System.err.println(Component
                    .translatable("command.box3.box3import.config_missing", configPath.toString())
                    .getString());
            return null;
        }

        try {
            String json = readGzipJson(configPath.toString());
            return JsonParser.parseString(json).getAsJsonObject();
        } catch (IOException | JsonParseException e) {
            System.err.println(Component
                    .translatable("command.box3.box3import.config_read_failed", configPath.toString())
                    .getString());
            return null;
        }
    }

    private static String readGzipJson(String path) throws IOException {
        try (FileInputStream fis = new FileInputStream(path);
                GZIPInputStream gis = new GZIPInputStream(fis);
                ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[8192];
            int n;
            while ((n = gis.read(buffer)) > 0) {
                baos.write(buffer, 0, n);
            }
            return baos.toString(StandardCharsets.UTF_8);
        }
    }

    private static String readGzipJsonFromUrl(String urlString) throws IOException {
        URI uri = URI.create(urlString);
        URL url = uri.toURL();
        URLConnection connection = url.openConnection();
        connection.setConnectTimeout(10_000);
        connection.setReadTimeout(30_000);

        try (InputStream is = connection.getInputStream();
                GZIPInputStream gis = new GZIPInputStream(is);
                ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[8192];
            int n;
            while ((n = gis.read(buffer)) > 0) {
                baos.write(buffer, 0, n);
            }
            return baos.toString(StandardCharsets.UTF_8);
        }
    }

    private static int[] toIntArray(JsonArray array) {
        if (array == null) {
            return new int[0];
        }
        int[] result = new int[array.size()];
        for (int i = 0; i < array.size(); i++) {
            result[i] = array.get(i).getAsInt();
        }
        return result;
    }
}
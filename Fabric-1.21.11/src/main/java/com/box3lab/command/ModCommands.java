package com.box3lab.command;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.box3lab.block.BarrierVoxelBlock;
import com.box3lab.register.VoxelExport;
import com.box3lab.register.VoxelImport;
import com.box3lab.util.Box3ImportFiles;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;

public final class ModCommands {
        private ModCommands() {
        }

        private static final String DEFAULT_EXPORT_MARKER_BLOCK = "minecraft:redstone_block";
        private static final int MARKER_SCAN_RADIUS = 64;

        private static final SuggestionProvider<CommandSourceStack> BOX3_FILE_SUGGESTIONS = (context, builder) -> {
                try {
                        List<String> files = Box3ImportFiles.listJsonFiles();
                        for (String file : files) {
                                String name = file;
                                if (name.endsWith(".gz")) {
                                        name = name.substring(0, name.length() - 3);
                                }
                                builder.suggest(name);
                        }
                } catch (IOException ignored) {

                }
                return builder.buildFuture();
        };

        public static void register() {
                CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
                        dispatcher.register(
                                        literal("box3import")
                                                        .executes(context -> listBox3ImportFiles(context.getSource()))
                                                        .then(argument("fileName", StringArgumentType.word())
                                                                        .suggests(BOX3_FILE_SUGGESTIONS)
                                                                        // /box3import <fileName>
                                                                        .executes(context -> executeBox3Import(
                                                                                        context.getSource(),
                                                                                        StringArgumentType.getString(
                                                                                                        context,
                                                                                                        "fileName"),
                                                                                        0,
                                                                                        false,
                                                                                        false))
                                                                        // /box3import <fileName> <offsetY>
                                                                        .then(argument("offsetY",
                                                                                        IntegerArgumentType.integer())
                                                                                        .executes(context -> executeBox3Import(
                                                                                                        context.getSource(),
                                                                                                        StringArgumentType
                                                                                                                        .getString(
                                                                                                                                        context,
                                                                                                                                        "fileName"),
                                                                                                        IntegerArgumentType
                                                                                                                        .getInteger(
                                                                                                                                        context,
                                                                                                                                        "offsetY"),
                                                                                                        false,
                                                                                                        false))
                                                                                        // /box3import <fileName>
                                                                                        // <offsetY> <ignoreBarrier>
                                                                                        .then(argument("ignoreBarrier",
                                                                                                        BoolArgumentType.bool())
                                                                                                        .executes(context -> executeBox3Import(
                                                                                                                        context.getSource(),
                                                                                                                        StringArgumentType
                                                                                                                                        .getString(
                                                                                                                                                        context,
                                                                                                                                                        "fileName"),
                                                                                                                        IntegerArgumentType
                                                                                                                                        .getInteger(
                                                                                                                                                        context,
                                                                                                                                                        "offsetY"),
                                                                                                                        BoolArgumentType.getBool(
                                                                                                                                        context,
                                                                                                                                        "ignoreBarrier"),
                                                                                                                        false))
                                                                                                        // /box3import
                                                                                                        // <fileName>
                                                                                                        // <offsetY>
                                                                                                        // <ignoreBarrier>
                                                                                                        // <ignoreWater>
                                                                                                        .then(argument("ignoreWater",
                                                                                                                        BoolArgumentType.bool())
                                                                                                                        .executes(context -> executeBox3Import(
                                                                                                                                        context.getSource(),
                                                                                                                                        StringArgumentType
                                                                                                                                                        .getString(
                                                                                                                                                                        context,
                                                                                                                                                                        "fileName"),
                                                                                                                                        IntegerArgumentType
                                                                                                                                                        .getInteger(
                                                                                                                                                                        context,
                                                                                                                                                                        "offsetY"),
                                                                                                                                        BoolArgumentType.getBool(
                                                                                                                                                        context,
                                                                                                                                                        "ignoreBarrier"),
                                                                                                                                        BoolArgumentType.getBool(
                                                                                                                                                        context,
                                                                                                                                                        "ignoreWater"))))))));

                        dispatcher.register(
                                        literal("box3barrier")
                                                        .executes(context -> showBarrierStatus(context.getSource()))
                                                        .then(argument("value", BoolArgumentType.bool())
                                                                        .executes(context -> setBarrierVisible(
                                                                                        context.getSource(),
                                                                                        BoolArgumentType.getBool(
                                                                                                        context,
                                                                                                        "value")))
                                                                        .then(literal("toggle")
                                                                                        .executes(context -> toggleBarrierVisible(
                                                                                                        context.getSource())))));

                        dispatcher.register(
                                        literal("box3export")
                                                        .then(literal("marker")
                                                                        .then(argument("fileName", StringArgumentType.word())
                                                                                        .executes(context -> executeBox3ExportByMarkers(
                                                                                                        context.getSource(),
                                                                                                        StringArgumentType
                                                                                                                        .getString(
                                                                                                                                        context,
                                                                                                                                        "fileName"),
                                                                                                        DEFAULT_EXPORT_MARKER_BLOCK))
                                                                                        .then(argument("markerBlock",
                                                                                                        StringArgumentType.word())
                                                                                                        .executes(context -> executeBox3ExportByMarkers(
                                                                                                                        context.getSource(),
                                                                                                                        StringArgumentType
                                                                                                                                        .getString(
                                                                                                                                                        context,
                                                                                                                                                        "fileName"),
                                                                                                                        StringArgumentType
                                                                                                                                        .getString(
                                                                                                                                                        context,
                                                                                                                                                        "markerBlock"))))))
                                                        .then(literal("pos1")
                                                                        .executes(context -> setExportPosFromPlayer(
                                                                                        context.getSource(),
                                                                                        true))
                                                                        .then(argument("x", IntegerArgumentType.integer())
                                                                                        .then(argument("y",
                                                                                                        IntegerArgumentType.integer())
                                                                                                        .then(argument("z",
                                                                                                                        IntegerArgumentType.integer())
                                                                                                                        .executes(context -> setExportPos(
                                                                                                                                        context.getSource(),
                                                                                                                                        true,
                                                                                                                                        IntegerArgumentType.getInteger(
                                                                                                                                                        context,
                                                                                                                                                        "x"),
                                                                                                                                        IntegerArgumentType.getInteger(
                                                                                                                                                        context,
                                                                                                                                                        "y"),
                                                                                                                                        IntegerArgumentType.getInteger(
                                                                                                                                                        context,
                                                                                                                                                        "z")))))))
                                                        .then(literal("pos2")
                                                                        .executes(context -> setExportPosFromPlayer(
                                                                                        context.getSource(),
                                                                                        false))
                                                                        .then(argument("x", IntegerArgumentType.integer())
                                                                                        .then(argument("y",
                                                                                                        IntegerArgumentType.integer())
                                                                                                        .then(argument("z",
                                                                                                                        IntegerArgumentType.integer())
                                                                                                                        .executes(context -> setExportPos(
                                                                                                                                        context.getSource(),
                                                                                                                                        false,
                                                                                                                                        IntegerArgumentType.getInteger(
                                                                                                                                                        context,
                                                                                                                                                        "x"),
                                                                                                                                        IntegerArgumentType.getInteger(
                                                                                                                                                        context,
                                                                                                                                                        "y"),
                                                                                                                                        IntegerArgumentType.getInteger(
                                                                                                                                                        context,
                                                                                                                                                        "z")))))))
                                                        .then(literal("clear")
                                                                        .executes(context -> clearExportSelection(
                                                                                        context.getSource())))
                                                        .then(argument("fileName", StringArgumentType.word())
                                                                        .executes(context -> executeBox3ExportBySelection(
                                                                                        context.getSource(),
                                                                                        StringArgumentType.getString(
                                                                                                        context,
                                                                                                        "fileName")))
                                                                        .then(argument("x1", IntegerArgumentType.integer())
                                                                                        .then(argument("y1", IntegerArgumentType.integer())
                                                                                                        .then(argument("z1", IntegerArgumentType.integer())
                                                                                                                        .then(argument("x2", IntegerArgumentType.integer())
                                                                                                                                        .then(argument("y2", IntegerArgumentType.integer())
                                                                                                                                                        .then(argument("z2", IntegerArgumentType.integer())
                                                                                                                                                                        .executes(context -> executeBox3Export(
                                                                                                                                                                                        context.getSource(),
                                                                                                                                                                                        StringArgumentType.getString(context, "fileName"),
                                                                                                                                                                                        IntegerArgumentType.getInteger(context, "x1"),
                                                                                                                                                                                        IntegerArgumentType.getInteger(context, "y1"),
                                                                                                                                                                                        IntegerArgumentType.getInteger(context, "z1"),
                                                                                                                                                                                        IntegerArgumentType.getInteger(context, "x2"),
                                                                                                                                                                                        IntegerArgumentType.getInteger(context, "y2"),
                                                                                                                                                                                        IntegerArgumentType.getInteger(context, "z2")))))))))));
                });
        }

        private static int listBox3ImportFiles(CommandSourceStack source) {
                var dir = Box3ImportFiles.getImportDir();

                try {
                        List<String> files = Box3ImportFiles.listJsonFiles();

                        if (files.isEmpty()) {
                                source.sendSuccess(
                                                () -> Component.translatable(
                                                                "command.box3.box3import.list.empty",
                                                                dir.toString()),
                                                false);
                        } else {
                                String joined = String.join(", ", files);
                                source.sendSuccess(
                                                () -> Component.translatable(
                                                                "command.box3.box3import.list.success",
                                                                dir.toString(), joined),
                                                false);
                        }
                } catch (IOException e) {
                        source.sendFailure(
                                        Component.translatable(
                                                        "command.box3.box3import.list.error",
                                                        dir.toString(), e.getMessage()));
                }

                return 1;
        }

        private static String resolveMapName(String fileName) {
                if (fileName != null && fileName.startsWith("Box3-")) {
                        String suffix = fileName.substring("Box3-".length());
                        if (!suffix.isEmpty()) {
                                return "https://box3lab.com/mc/build/" + suffix + ".gz";
                        }
                }
                return fileName;
        }

        private static int executeBox3Import(CommandSourceStack source, String fileName,
                        int offsetY, boolean ignoreBarrier, boolean useVanillaWater) {
                ServerLevel level = source.getServer().overworld();
                try {
                        ServerPlayer player = source.getPlayer();
                        String mapName = resolveMapName(fileName);
                        var basePos = player != null ? player.position() : new BlockPos(0, 0, 0).getCenter();
                        var offsetPos = basePos.add(0, offsetY, 0);

                        VoxelImport.apply(null, level, mapName,
                                        offsetPos,
                                        player,
                                        ignoreBarrier,
                                        useVanillaWater);

                        source.sendSuccess(
                                        () -> Component.translatable("command.box3.box3import.success",
                                                        mapName),
                                        false);
                } catch (Exception e) {
                        source.sendFailure(
                                        Component.translatable("command.box3.box3import.failure", e.getMessage()));
                }
                return 1;
        }

        private static int showBarrierStatus(CommandSourceStack source) {
                boolean visible = BarrierVoxelBlock.isVisible();
                source.sendSuccess(
                                () -> Component.translatable("command.box3.box3barrier.status",
                                                String.valueOf(visible)),
                                false);
                return 1;
        }

        private static int setBarrierVisible(CommandSourceStack source, boolean value) {
                BarrierVoxelBlock.setVisible(value);
                source.sendSuccess(
                                () -> Component.translatable("command.box3.box3barrier.set", String.valueOf(value)),
                                false);
                return 1;
        }

        private static int toggleBarrierVisible(CommandSourceStack source) {
                boolean current = BarrierVoxelBlock.isVisible();
                boolean next = !current;
                BarrierVoxelBlock.setVisible(next);
                source.sendSuccess(
                                () -> Component.translatable("command.box3.box3barrier.toggled",
                                                String.valueOf(next)),
                                false);
                return 1;
        }

        private static int executeBox3Export(CommandSourceStack source, String fileName,
                        int x1, int y1, int z1, int x2, int y2, int z2) {
                ServerLevel level = source.getLevel();
                BlockPos from = new BlockPos(x1, y1, z1);
                BlockPos to = new BlockPos(x2, y2, z2);

                try {
                        VoxelExport.ExportResult result = VoxelExport.exportRegion(level, from, to, fileName);
                        source.sendSuccess(
                                        () -> Component.translatable(
                                                        "command.box3.box3export.success",
                                                        result.output().toString(),
                                                        result.scannedBlocks(),
                                                        result.exportedBlocks()),
                                        false);
                } catch (Exception e) {
                        source.sendFailure(
                                        Component.translatable("command.box3.box3export.failure", e.getMessage()));
                }
                return 1;
        }

        private static int executeBox3ExportBySelection(CommandSourceStack source, String fileName) {
                ServerPlayer player = source.getPlayer();
                if (player == null) {
                        source.sendFailure(Component.translatable("command.box3.box3export.player_only"));
                        return 0;
                }

                var selection = Box3ExportSelectionStore.get(player.getUUID());
                if (selection == null || !selection.complete()) {
                        source.sendFailure(Component.translatable("command.box3.box3export.selection_incomplete"));
                        return 0;
                }

                return executeBox3Export(
                                source,
                                fileName,
                                selection.pos1().getX(),
                                selection.pos1().getY(),
                                selection.pos1().getZ(),
                                selection.pos2().getX(),
                                selection.pos2().getY(),
                                selection.pos2().getZ());
        }

        private static int setExportPosFromPlayer(CommandSourceStack source, boolean firstPos) {
                ServerPlayer player = source.getPlayer();
                if (player == null) {
                        source.sendFailure(Component.translatable("command.box3.box3export.player_only"));
                        return 0;
                }
                BlockPos pos = player.blockPosition();
                return setExportPos(source, firstPos, pos.getX(), pos.getY(), pos.getZ());
        }

        private static int setExportPos(CommandSourceStack source, boolean firstPos, int x, int y, int z) {
                ServerPlayer player = source.getPlayer();
                if (player == null) {
                        source.sendFailure(Component.translatable("command.box3.box3export.player_only"));
                        return 0;
                }

                BlockPos pos = new BlockPos(x, y, z);
                if (firstPos) {
                        Box3ExportSelectionStore.setPos1(player.getUUID(), pos);
                        source.sendSuccess(
                                        () -> Component.translatable("command.box3.box3export.pos_set", "pos1", x, y, z),
                                        false);
                } else {
                        Box3ExportSelectionStore.setPos2(player.getUUID(), pos);
                        source.sendSuccess(
                                        () -> Component.translatable("command.box3.box3export.pos_set", "pos2", x, y, z),
                                        false);
                }
                return 1;
        }

        private static int clearExportSelection(CommandSourceStack source) {
                ServerPlayer player = source.getPlayer();
                if (player == null) {
                        source.sendFailure(Component.translatable("command.box3.box3export.player_only"));
                        return 0;
                }

                Box3ExportSelectionStore.clear(player.getUUID());
                source.sendSuccess(() -> Component.translatable("command.box3.box3export.selection_cleared"), false);
                return 1;
        }

        private static int executeBox3ExportByMarkers(CommandSourceStack source, String fileName, String markerBlockId) {
                ServerPlayer player = source.getPlayer();
                if (player == null) {
                        source.sendFailure(Component.translatable("command.box3.box3export.player_only"));
                        return 0;
                }

                Block markerBlock = resolveMarkerBlock(markerBlockId);
                if (markerBlock == null) {
                        source.sendFailure(Component.translatable("command.box3.box3export.marker_invalid", markerBlockId));
                        return 0;
                }

                List<BlockPos> positions = findMarkerPositions(source.getLevel(), player.blockPosition(), markerBlock,
                                MARKER_SCAN_RADIUS, 3);
                if (positions.size() != 2) {
                        source.sendFailure(Component.translatable(
                                        "command.box3.box3export.marker_count_invalid",
                                        MARKER_SCAN_RADIUS,
                                        positions.size(),
                                        BuiltInRegistries.BLOCK.getKey(markerBlock).toString()));
                        return 0;
                }

                BlockPos p1 = positions.get(0);
                BlockPos p2 = positions.get(1);
                return executeBox3Export(source, fileName, p1.getX(), p1.getY(), p1.getZ(), p2.getX(), p2.getY(), p2.getZ());
        }

        private static Block resolveMarkerBlock(String blockId) {
                Identifier id = Identifier.tryParse(blockId);
                if (id == null) {
                        return null;
                }
                if (!BuiltInRegistries.BLOCK.containsKey(id)) {
                        return null;
                }
                return BuiltInRegistries.BLOCK.get(id).map(holder -> holder.value()).orElse(null);
        }

        private static List<BlockPos> findMarkerPositions(ServerLevel level, BlockPos center, Block markerBlock, int radius,
                        int maxResults) {
                List<BlockPos> positions = new ArrayList<>();
                int minX = center.getX() - radius;
                int maxX = center.getX() + radius;
                int minY = center.getY() - radius;
                int maxY = center.getY() + radius;
                int minZ = center.getZ() - radius;
                int maxZ = center.getZ() + radius;
                BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

                for (int y = minY; y <= maxY; y++) {
                        for (int x = minX; x <= maxX; x++) {
                                for (int z = minZ; z <= maxZ; z++) {
                                        cursor.set(x, y, z);
                                        if (level.getBlockState(cursor).getBlock() == markerBlock) {
                                                positions.add(cursor.immutable());
                                                if (positions.size() >= maxResults) {
                                                        return positions;
                                                }
                                        }
                                }
                        }
                }
                return positions;
        }
}

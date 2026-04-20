package com.box3lab.box3.command;

import com.box3lab.box3.block.BarrierVoxelBlock;
import com.box3lab.box3.config.Box3Config;
import com.box3lab.box3.register.VoxelExport;
import com.box3lab.box3.register.VoxelImport;
import com.box3lab.box3.util.Box3ImportFiles;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public final class ModCommands {
    private ModCommands() {
    }

    private static final String DEFAULT_EXPORT_MARKER_BLOCK = "minecraft:redstone_block";
    private static final int MAX_MARKER_SCAN_RADIUS = 1024;
    private static final int MARKER_Y_TOLERANCE = 512;

    private static final SuggestionProvider<CommandSourceStack> BOX3_FILE_SUGGESTIONS = (context, builder) -> {
        try {
            List<String> files = Box3ImportFiles.listJsonFiles();
            for (String file : files) {
                String name = file.endsWith(".gz") ? file.substring(0, file.length() - 3) : file;
                builder.suggest(name);
            }
        } catch (IOException ignored) {
        }
        return builder.buildFuture();
    };

    public static void register(RegisterCommandsEvent event) {
        var dispatcher = event.getDispatcher();

        dispatcher.register(
                literal("box3import")
                        .requires(ModCommands::canUseBox3Command)
                        .executes(context -> listBox3ImportFiles(context.getSource()))
                        .then(argument("fileName", StringArgumentType.word())
                                .suggests(BOX3_FILE_SUGGESTIONS)
                                .executes(context -> executeBox3Import(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "fileName"),
                                        0,
                                        false,
                                        false))
                                .then(argument("offsetY", IntegerArgumentType.integer())
                                        .executes(context -> executeBox3Import(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "fileName"),
                                                IntegerArgumentType.getInteger(context, "offsetY"),
                                                false,
                                                false))
                                        .then(argument("ignoreBarrier", BoolArgumentType.bool())
                                                .executes(context -> executeBox3Import(
                                                        context.getSource(),
                                                        StringArgumentType.getString(context, "fileName"),
                                                        IntegerArgumentType.getInteger(context, "offsetY"),
                                                        BoolArgumentType.getBool(context, "ignoreBarrier"),
                                                        false))
                                                .then(argument("ignoreWater", BoolArgumentType.bool())
                                                        .executes(context -> executeBox3Import(
                                                                context.getSource(),
                                                                StringArgumentType.getString(context, "fileName"),
                                                                IntegerArgumentType.getInteger(context, "offsetY"),
                                                                BoolArgumentType.getBool(context, "ignoreBarrier"),
                                                                BoolArgumentType.getBool(context, "ignoreWater"))))))));

        dispatcher.register(
                literal("box3barrier")
                        .requires(ModCommands::canUseBox3Command)
                        .executes(context -> showBarrierStatus(context.getSource()))
                        .then(argument("value", BoolArgumentType.bool())
                                .executes(context -> setBarrierVisible(
                                        context.getSource(),
                                        BoolArgumentType.getBool(context, "value"))))
                        .then(literal("toggle")
                                .executes(context -> toggleBarrierVisible(context.getSource()))));

        dispatcher.register(
                literal("box3export")
                        .requires(ModCommands::canUseBox3Command)
                        .executes(context -> showBox3ExportUsage(context.getSource()))
                        .then(argument("fileName", StringArgumentType.word())
                                .executes(context -> executeBox3ExportByMarkers(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "fileName")))));

        dispatcher.register(
                literal("box3perm")
                        .requires(source -> hasPermissionLevel(source, 4))
                        .executes(context -> showCommandPermissionLevel(context.getSource()))
                        .then(argument("level", IntegerArgumentType.integer(0, 4))
                                .executes(context -> setCommandPermissionLevel(
                                        context.getSource(),
                                        IntegerArgumentType.getInteger(context, "level")))));
    }

    private static int listBox3ImportFiles(CommandSourceStack source) {
        var dir = Box3ImportFiles.getImportDir();
        try {
            List<String> files = Box3ImportFiles.listJsonFiles();
            if (files.isEmpty()) {
                source.sendSuccess(() -> Component.translatable("command.box3.box3import.list.empty", dir.toString()),
                        false);
            } else {
                String joined = String.join(", ", files);
                source.sendSuccess(
                        () -> Component.translatable("command.box3.box3import.list.success", dir.toString(), joined),
                        false);
            }
        } catch (IOException e) {
            source.sendFailure(
                    Component.translatable("command.box3.box3import.list.error", dir.toString(), e.getMessage()));
        }
        return 1;
    }

    private static int showBox3ExportUsage(CommandSourceStack source) {
        source.sendFailure(Component.translatable("command.box3.box3export.usage"));
        return 0;
    }

    private static boolean canUseBox3Command(CommandSourceStack source) {
        return hasPermissionLevel(source, Box3Config.commandPermissionLevel());
    }

    private static int showCommandPermissionLevel(CommandSourceStack source) {
        source.sendSuccess(
                () -> Component.translatable(
                        "command.box3.box3perm.status",
                        Box3Config.commandPermissionLevel()),
                false);
        return Box3Config.commandPermissionLevel();
    }

    private static int setCommandPermissionLevel(CommandSourceStack source, int level) {
        Box3Config.setCommandPermissionLevel(level);
        source.sendSuccess(
                () -> Component.translatable("command.box3.box3perm.set", level),
                true);
        return level;
    }

    private static boolean hasPermissionLevel(CommandSourceStack source, int level) {
        if (level <= 0) {
            return true;
        }
        PermissionLevel required = PermissionLevel.byId(level);
        PermissionSet permissions = source.permissions();

        if (permissions instanceof LevelBasedPermissionSet levelSet) {
            return levelSet.level().isEqualOrHigherThan(required);
        }

        PermissionLevel actual = PermissionLevel.ALL;
        if (permissions.hasPermission(Permissions.COMMANDS_OWNER)) {
            actual = PermissionLevel.OWNERS;
        } else if (permissions.hasPermission(Permissions.COMMANDS_ADMIN)) {
            actual = PermissionLevel.ADMINS;
        } else if (permissions.hasPermission(Permissions.COMMANDS_GAMEMASTER)) {
            actual = PermissionLevel.GAMEMASTERS;
        } else if (permissions.hasPermission(Permissions.COMMANDS_MODERATOR)) {
            actual = PermissionLevel.MODERATORS;
        }

        return actual.isEqualOrHigherThan(required);
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

    private static int executeBox3Import(CommandSourceStack source, String fileName, int offsetY,
            boolean ignoreBarrier, boolean ignoreWater) {
        ServerLevel level = source.getServer().overworld();
        try {
            ServerPlayer player = source.getPlayer();
            String mapName = resolveMapName(fileName);
            var basePos = player != null ? player.position() : source.getPosition();
            var offsetPos = basePos.add(0, offsetY, 0);

            VoxelImport.apply(level, mapName, offsetPos, player, ignoreBarrier, ignoreWater);

            source.sendSuccess(() -> Component.translatable("command.box3.box3import.success", mapName), false);
        } catch (Exception e) {
            source.sendFailure(Component.translatable("command.box3.box3import.failure", e.getMessage()));
        }
        return 1;
    }

    private static int showBarrierStatus(CommandSourceStack source) {
        boolean visible = BarrierVoxelBlock.isVisible();
        source.sendSuccess(
                () -> Component.translatable("command.box3.box3barrier.status", String.valueOf(visible)),
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
        boolean next = !BarrierVoxelBlock.isVisible();
        BarrierVoxelBlock.setVisible(next);
        source.sendSuccess(
                () -> Component.translatable("command.box3.box3barrier.toggled", String.valueOf(next)),
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
            source.sendFailure(Component.translatable("command.box3.box3export.failure", e.getMessage()));
        }
        return 1;
    }

    private static int executeBox3ExportByMarkers(CommandSourceStack source, String fileName) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.translatable("command.box3.box3export.player_only"));
            return 0;
        }

        Block markerBlock = resolveMarkerBlock(DEFAULT_EXPORT_MARKER_BLOCK);
        if (markerBlock == null) {
            source.sendFailure(Component.translatable("command.box3.box3export.marker_invalid",
                    DEFAULT_EXPORT_MARKER_BLOCK));
            return 0;
        }

        List<BlockPos> positions = findMarkerPositions(
                source.getLevel(),
                player.blockPosition(),
                markerBlock,
                MAX_MARKER_SCAN_RADIUS,
                MARKER_Y_TOLERANCE,
                2);

        if (positions.size() < 2) {
            source.sendFailure(Component.translatable(
                    "command.box3.box3export.marker_count_invalid",
                    MAX_MARKER_SCAN_RADIUS,
                    positions.size(),
                    BuiltInRegistries.BLOCK.getKey(markerBlock).toString()));
            return 0;
        }

        BlockPos p1 = positions.get(0);
        BlockPos p2 = positions.get(1);
        return executeBox3Export(source, fileName,
                p1.getX(), p1.getY(), p1.getZ(),
                p2.getX(), p2.getY(), p2.getZ());
    }

    private static Block resolveMarkerBlock(String blockId) {
        Identifier id = Identifier.tryParse(blockId);
        if (id == null) {
            return null;
        }
        return BuiltInRegistries.BLOCK.get(id)
                .map(holder -> holder.value())
                .orElse(null);
    }

    private static List<BlockPos> findMarkerPositions(ServerLevel level, BlockPos center, Block markerBlock,
            int maxRadius, int yTolerance, int maxResults) {
        List<BlockPos> positions = new ArrayList<>();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        int cx = center.getX();
        int cy = center.getY();
        int cz = center.getZ();

        for (int radius = 0; radius <= maxRadius; radius++) {
            int minX = cx - radius;
            int maxX = cx + radius;
            int minZ = cz - radius;
            int maxZ = cz + radius;

            if (radius == 0) {
                if (scanMarkerColumn(level, markerBlock, cy, yTolerance, cx, cz, cursor, positions, maxResults)) {
                    return positions;
                }
                continue;
            }

            for (int x = minX; x <= maxX; x++) {
                if (scanMarkerColumn(level, markerBlock, cy, yTolerance, x, minZ, cursor, positions, maxResults)) {
                    return positions;
                }
                if (scanMarkerColumn(level, markerBlock, cy, yTolerance, x, maxZ, cursor, positions, maxResults)) {
                    return positions;
                }
            }

            for (int z = minZ + 1; z <= maxZ - 1; z++) {
                if (scanMarkerColumn(level, markerBlock, cy, yTolerance, minX, z, cursor, positions, maxResults)) {
                    return positions;
                }
                if (scanMarkerColumn(level, markerBlock, cy, yTolerance, maxX, z, cursor, positions, maxResults)) {
                    return positions;
                }
            }
        }

        return positions;
    }

    private static boolean scanMarkerColumn(ServerLevel level, Block markerBlock, int centerY, int yTolerance,
            int x, int z, BlockPos.MutableBlockPos cursor, List<BlockPos> positions, int maxResults) {
        for (int dy = 0; dy <= yTolerance; dy++) {
            int y1 = centerY + dy;
            cursor.set(x, y1, z);
            if (level.isLoaded(cursor) && level.getBlockState(cursor).getBlock() == markerBlock) {
                positions.add(cursor.immutable());
                if (positions.size() >= maxResults) {
                    return true;
                }
            }

            if (dy == 0) {
                continue;
            }

            int y2 = centerY - dy;
            cursor.set(x, y2, z);
            if (level.isLoaded(cursor) && level.getBlockState(cursor).getBlock() == markerBlock) {
                positions.add(cursor.immutable());
                if (positions.size() >= maxResults) {
                    return true;
                }
            }
        }

        return false;
    }
}

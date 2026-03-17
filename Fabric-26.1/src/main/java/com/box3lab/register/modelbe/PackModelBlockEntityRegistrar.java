package com.box3lab.register.modelbe;

import static com.box3lab.Box3.MOD_ID;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.box3lab.block.entity.PackModelEntityBlock;
import com.box3lab.register.creative.CreativeTabExtras;
import com.box3lab.register.creative.CreativeTabRegistrar;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public final class PackModelBlockEntityRegistrar {
    private static final String ASSET_PREFIX = "assets/" + MOD_ID + "/";

    private static final Map<Block, BlockEntityType<?>> TYPES_BY_BLOCK = new LinkedHashMap<>();

    private PackModelBlockEntityRegistrar() {
    }

    public static void registerAll() {
        Set<String> modelNames = discoverPairedModelNames();
        if (modelNames.isEmpty()) {
            return;
        }

        for (String name : modelNames) {
            Identifier id = Identifier.tryBuild(MOD_ID, name);
            if (id == null) {
                continue;
            }

            ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, id);
            if (BuiltInRegistries.BLOCK.containsKey(blockKey)) {
                continue;
            }

            Block block = new PackModelEntityBlock(
                    BlockBehaviour.Properties.of()
                            .setId(blockKey)
                            .mapColor(MapColor.STONE)
                            .strength(1.5F, 6.0F)
                            .noOcclusion()
                            .isViewBlocking((state, level, pos) -> false)
                            .isSuffocating((state, level, pos) -> false)
                            .isRedstoneConductor((state, level, pos) -> false));
            Registry.register(BuiltInRegistries.BLOCK, blockKey, block);

            ResourceKey<Item> itemKey = ResourceKey.create(
                    Registries.ITEM,
                    Identifier.fromNamespaceAndPath(MOD_ID, name));
            if (!BuiltInRegistries.ITEM.containsKey(itemKey)) {
                Item item = new BlockItem(block, new Item.Properties().setId(itemKey).useItemDescriptionPrefix());
                Registry.register(BuiltInRegistries.ITEM, itemKey, item);
                CreativeTabExtras.add(CreativeTabRegistrar.DEFAULT_MODEL_TAB, item);
            }

            ResourceKey<BlockEntityType<?>> blockEntityKey = ResourceKey.create(Registries.BLOCK_ENTITY_TYPE, id);
            if (BuiltInRegistries.BLOCK_ENTITY_TYPE.containsKey(blockEntityKey)) {
                continue;
            }

            BlockEntityType<?> type = FabricBlockEntityTypeBuilder
                    .create(com.box3lab.block.entity.PackModelBlockEntity::new, block)
                    .build();

            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, blockEntityKey, type);
            TYPES_BY_BLOCK.put(block, type);
        }

        CreativeTabRegistrar.registerModelTab(MOD_ID);
    }

    public static BlockEntityType<?> typeFor(Block block) {
        BlockEntityType<?> type = TYPES_BY_BLOCK.get(block);
        if (type == null) {
            throw new IllegalStateException("No block entity type bound for block: " + block);
        }
        return type;
    }

    private static Set<String> discoverPairedModelNames() {
        Set<String> result = new LinkedHashSet<>();
        Path packsRoot = FabricLoader.getInstance().getGameDir().resolve("resourcepacks");
        if (!Files.isDirectory(packsRoot)) {
            return result;
        }

        try (var entries = Files.list(packsRoot)) {
            entries.forEach(entry -> {
                if (Files.isDirectory(entry)) {
                    collectFromDirectory(entry, result);
                } else if (isArchive(entry)) {
                    collectFromArchive(entry, result);
                }
            });
        } catch (IOException ignored) {
        }

        return result;
    }

    private static void collectFromDirectory(Path packDir, Set<String> out) {
        Path assetsRoot = packDir.resolve("assets").resolve(MOD_ID);
        if (!Files.isDirectory(assetsRoot)) {
            return;
        }

        Set<String> models = collectBaseNamesFromDirectory(assetsRoot, ".json");
        if (models.isEmpty()) {
            return;
        }

        Set<String> textures = collectBaseNamesFromDirectory(assetsRoot, ".png");
        if (textures.isEmpty()) {
            return;
        }

        for (String model : models) {
            if (textures.contains(model)) {
                out.add(model);
            }
        }
    }

    private static Set<String> collectBaseNamesFromDirectory(Path root, String suffix) {
        Set<String> names = new LinkedHashSet<>();
        try (var files = Files.walk(root)) {
            files.filter(Files::isRegularFile).forEach(path -> {
                String fileName = path.getFileName().toString();
                if (!fileName.toLowerCase(Locale.ROOT).endsWith(suffix)) {
                    return;
                }

                String base = fileName.substring(0, fileName.length() - suffix.length()).toLowerCase(Locale.ROOT);
                if (!base.isBlank()) {
                    names.add(base);
                }
            });
        } catch (IOException ignored) {
        }
        return names;
    }

    private static void collectFromArchive(Path archive, Set<String> out) {
        try (ZipFile zip = new ZipFile(archive.toFile())) {
            Set<String> models = new LinkedHashSet<>();
            Set<String> textures = new LinkedHashSet<>();

            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.isDirectory()) {
                    continue;
                }

                String name = entry.getName();
                if (!name.startsWith(ASSET_PREFIX)) {
                    continue;
                }

                String fileName = name.substring(name.lastIndexOf('/') + 1);
                String lower = fileName.toLowerCase(Locale.ROOT);
                if (lower.endsWith(".json")) {
                    String base = fileName.substring(0, fileName.length() - 5).toLowerCase(Locale.ROOT);
                    if (!base.isBlank()) {
                        models.add(base);
                    }
                } else if (lower.endsWith(".png")) {
                    String base = fileName.substring(0, fileName.length() - 4).toLowerCase(Locale.ROOT);
                    if (!base.isBlank()) {
                        textures.add(base);
                    }
                }
            }

            for (String model : models) {
                if (textures.contains(model)) {
                    out.add(model);
                }
            }
        } catch (IOException ignored) {
        }
    }

    private static boolean isArchive(Path path) {
        String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
        return name.endsWith(".zip") || name.endsWith(".jar");
    }
}

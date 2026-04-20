package com.box3lab.box3.register.modelbe;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.box3lab.box3.Box3Blocks;
import static com.box3lab.box3.Box3Blocks.DEFAULT_MODEL_TAB;
import static com.box3lab.box3.Box3Blocks.MODID;
import com.box3lab.box3.block.entity.PackModelBlockEntity;
import com.box3lab.box3.block.entity.PackModelEntityBlock;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

public final class PackModelBlockEntityRegistrar {
    private static final String ASSET_PREFIX = "assets/" + MODID + "/";
    private static final Set<String> MODEL_NAMES = discoverPairedModelNames();

    public static final Map<String, DeferredBlock<Block>> REGISTERED_BLOCKS = registerBlocks();
    public static final Map<String, DeferredItem<Item>> REGISTERED_ITEMS = registerItems();
    public static final Map<String, DeferredHolder<BlockEntityType<?>, BlockEntityType<?>>> REGISTERED_TYPES = registerBlockEntityTypes();

    private PackModelBlockEntityRegistrar() {
    }

    private static Map<String, DeferredBlock<Block>> registerBlocks() {
        Map<String, DeferredBlock<Block>> blocks = new LinkedHashMap<>();
        for (String name : MODEL_NAMES) {
            blocks.put(name, Box3Blocks.BLOCKS.register(name, () -> new PackModelEntityBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.STONE)
                            .strength(1.5F, 6.0F)
                            .noOcclusion()
                            .isViewBlocking((state, level, pos) -> false)
                            .isSuffocating((state, level, pos) -> false)
                            .isRedstoneConductor((state, level, pos) -> false))));
        }
        return Map.copyOf(blocks);
    }

    private static Map<String, DeferredItem<Item>> registerItems() {
        Map<String, DeferredItem<Item>> items = new LinkedHashMap<>();
        for (Map.Entry<String, DeferredBlock<Block>> entry : REGISTERED_BLOCKS.entrySet()) {
            String name = entry.getKey();
            items.put(name,
                    Box3Blocks.ITEMS.register(name, () -> new BlockItem(entry.getValue().get(), new Item.Properties()) {
                        @Override
                        public Component getName(ItemStack stack) {
                            return Component.translatable("item." + MODID + "." + name);
                        }
                    }));
        }
        return Map.copyOf(items);
    }

    private static Map<String, DeferredHolder<BlockEntityType<?>, BlockEntityType<?>>> registerBlockEntityTypes() {
        Map<String, DeferredHolder<BlockEntityType<?>, BlockEntityType<?>>> types = new LinkedHashMap<>();
        for (Map.Entry<String, DeferredBlock<Block>> entry : REGISTERED_BLOCKS.entrySet()) {
            String name = entry.getKey();
            Supplier<Block> blockSupplier = entry.getValue();
            types.put(name, Box3Blocks.BLOCK_ENTITY_TYPES.register(name,
                    () -> BlockEntityType.Builder.of(PackModelBlockEntity::new, blockSupplier.get()).build(null)));
        }
        return Map.copyOf(types);
    }

    public static void registerAll() {
        REGISTERED_ITEMS.values().forEach(item -> CreativeTabExtras.add(DEFAULT_MODEL_TAB, item::get));
    }

    public static List<ItemLike> modelTabItems() {
        List<Supplier<? extends ItemLike>> suppliers = CreativeTabExtras.extras().getOrDefault(DEFAULT_MODEL_TAB,
                List.of());
        List<ItemLike> items = new ArrayList<>(suppliers.size());
        for (Supplier<? extends ItemLike> supplier : suppliers) {
            ItemLike item = supplier.get();
            if (item != null) {
                items.add(item);
            }
        }
        return items;
    }

    public static boolean hasModels() {
        return !REGISTERED_ITEMS.isEmpty();
    }

    public static BlockEntityType<?> typeFor(Block block) {
        for (Map.Entry<String, DeferredBlock<Block>> entry : REGISTERED_BLOCKS.entrySet()) {
            if (entry.getValue().get() == block) {
                return REGISTERED_TYPES.get(entry.getKey()).get();
            }
        }
        throw new IllegalStateException("No block entity type bound for block: " + block);
    }

    private static Set<String> discoverPairedModelNames() {
        Set<String> result = new LinkedHashSet<>();
        Path packsRoot = FMLPaths.GAMEDIR.get().resolve("resourcepacks");
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
        Path assetsRoot = packDir.resolve("assets").resolve(MODID);
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

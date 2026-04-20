package com.box3lab.box3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;

import com.box3lab.box3.command.ModCommands;
import com.box3lab.box3.config.Box3Config;
import com.box3lab.box3.data.BlockIndexData;
import com.box3lab.box3.register.CategorySoundTypes;
import com.box3lab.box3.register.VoxelBlockFactories;
import com.box3lab.box3.register.VoxelBlockPropertiesFactory;
import com.box3lab.box3.register.VoxelLightLevelMapper;
import com.box3lab.box3.register.modelbe.PackModelBlockEntityRegistrar;
import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(Box3Blocks.MODID)
public class Box3Blocks {
    public static final String MODID = "box3";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final String DEFAULT_MODEL_TAB = "models";

    private static final BlockIndexData BLOCK_INDEX_DATA = BlockIndexData.get();
    private static final List<BlockDefinition> BLOCK_DEFINITIONS = loadBlockDefinitions();
    private static final Map<String, List<String>> CATEGORY_TO_BLOCK_NAMES = buildCategoryIndex();
    private static final Map<String, Boolean> BLOCK_TRANSPARENCY = buildTransparencyIndex();

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister
            .create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister
            .create(Registries.BLOCK_ENTITY_TYPE, MODID);

    public static final Map<String, DeferredBlock<Block>> REGISTERED_BLOCKS = registerBlocks();
    public static final Map<String, DeferredItem<BlockItem>> REGISTERED_BLOCK_ITEMS = registerBlockItems();
    public static final Map<String, DeferredHolder<CreativeModeTab, CreativeModeTab>> CATEGORY_TABS = registerCategoryTabs();

    public Box3Blocks(IEventBus modEventBus, ModContainer modContainer) {
        Box3Config.load();
        PackModelBlockEntityRegistrar.registerAll();
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        BLOCK_ENTITY_TYPES.register(modEventBus);
        NeoForge.EVENT_BUS.addListener(ModCommands::register);

        LOGGER.info("Registered {} Box3 blocks, {} block items and {} creative tabs.", REGISTERED_BLOCKS.size(),
                REGISTERED_BLOCK_ITEMS.size(), CATEGORY_TABS.size());
    }

    private static List<BlockDefinition> loadBlockDefinitions() {
        List<BlockDefinition> definitions = new ArrayList<>();
        for (BlockIndexData.Entry entry : BLOCK_INDEX_DATA.entries) {
            if (entry.id == 0 || entry.fluid || !hasBlockResources(entry.resourceName)) {
                continue;
            }

            String category = sanitizeCategoryPath(entry.category);
            boolean solid = !BLOCK_INDEX_DATA.notSolidIds.contains(entry.id);
            boolean transparent = !solid;
            SoundType soundType = CategorySoundTypes.soundTypeForCategory(entry.category);
            int lightLevel = VoxelLightLevelMapper.lightLevelFromEmissivePacked(entry.emissivePacked);
            BlockBehaviour.Properties properties = VoxelBlockPropertiesFactory.create(solid, soundType, lightLevel,
                    entry.hardness, entry.resistance, entry.friction);

            definitions.add(new BlockDefinition(entry.resourceName, entry.id, category, transparent, properties));
        }

        definitions.sort(Comparator.comparingInt(BlockDefinition::id).thenComparing(BlockDefinition::resourceName));
        return List.copyOf(definitions);
    }

    private static Map<String, DeferredBlock<Block>> registerBlocks() {
        Map<String, DeferredBlock<Block>> blocks = new LinkedHashMap<>();
        for (BlockDefinition definition : BLOCK_DEFINITIONS) {
            blocks.put(definition.resourceName(), BLOCKS.register(definition.resourceName(),
                    () -> VoxelBlockFactories.factoryFor(definition.resourceName(), definition.transparent())
                            .apply(definition.properties())));
        }
        return Map.copyOf(blocks);
    }

    private static Map<String, DeferredItem<BlockItem>> registerBlockItems() {
        Map<String, DeferredItem<BlockItem>> items = new LinkedHashMap<>();
        for (Map.Entry<String, DeferredBlock<Block>> entry : REGISTERED_BLOCKS.entrySet()) {
            items.put(entry.getKey(), ITEMS.registerSimpleBlockItem(entry.getKey(), entry.getValue()));
        }
        return Map.copyOf(items);
    }

    private static Map<String, DeferredHolder<CreativeModeTab, CreativeModeTab>> registerCategoryTabs() {
        Map<String, DeferredHolder<CreativeModeTab, CreativeModeTab>> tabs = new LinkedHashMap<>();
        for (Map.Entry<String, List<String>> entry : CATEGORY_TO_BLOCK_NAMES.entrySet()) {
            String categoryPath = entry.getKey();
            List<String> names = entry.getValue();
            if (names.isEmpty()) {
                continue;
            }

            tabs.put(categoryPath,
                    CREATIVE_MODE_TABS.register("creative_tab_" + categoryPath, () -> CreativeModeTab.builder()
                            .title(Component.translatable("itemGroup." + MODID + "." + categoryPath))
                            .icon(() -> getItemForName(names.get(0)).getDefaultInstance())
                            .displayItems((parameters, output) -> {
                                names.forEach(name -> output.accept(getItemForName(name)));
                                if (DEFAULT_MODEL_TAB.equals(categoryPath)) {
                                    PackModelBlockEntityRegistrar.modelTabItems().forEach(output::accept);
                                }
                            })
                            .build()));
        }

        if (!tabs.containsKey(DEFAULT_MODEL_TAB) && PackModelBlockEntityRegistrar.hasModels()) {
            tabs.put(DEFAULT_MODEL_TAB, CREATIVE_MODE_TABS.register("creative_tab_" + DEFAULT_MODEL_TAB,
                    () -> CreativeModeTab.builder()
                            .title(Component.translatable("itemGroup." + MODID + "." + DEFAULT_MODEL_TAB))
                            .icon(() -> PackModelBlockEntityRegistrar.modelTabItems().get(0).asItem()
                                    .getDefaultInstance())
                            .displayItems((parameters, output) -> PackModelBlockEntityRegistrar.modelTabItems()
                                    .forEach(output::accept))
                            .build()));
        }
        return Map.copyOf(tabs);
    }

    private static Map<String, List<String>> buildCategoryIndex() {
        Map<String, List<String>> categories = new TreeMap<>();
        for (BlockDefinition definition : BLOCK_DEFINITIONS) {
            String category = definition.category();
            if (category.isBlank()) {
                category = DEFAULT_MODEL_TAB;
            }
            categories.computeIfAbsent(category, key -> new ArrayList<>()).add(definition.resourceName());
        }

        for (List<String> names : categories.values()) {
            names.sort(Comparator.naturalOrder());
        }
        return Map.copyOf(categories);
    }

    private static Map<String, Boolean> buildTransparencyIndex() {
        Map<String, Boolean> transparency = new HashMap<>();
        for (BlockDefinition definition : BLOCK_DEFINITIONS) {
            transparency.put(definition.resourceName(), definition.transparent());
        }
        return Map.copyOf(transparency);
    }

    private static BlockItem getItemForName(String resourceName) {
        DeferredItem<BlockItem> preferred = REGISTERED_BLOCK_ITEMS.get(resourceName);
        if (preferred != null) {
            return preferred.get();
        }
        return REGISTERED_BLOCK_ITEMS.values().iterator().next().get();
    }

    private static boolean hasBlockResources(String resourceName) {
        return Box3Blocks.class.getResource(resourcePath("blockstates", resourceName)) != null
                && Box3Blocks.class.getResource(resourcePath("models/block", resourceName)) != null
                && Box3Blocks.class.getResource(resourcePath("models/item", resourceName)) != null;
    }

    private static String resourcePath(String kind, String resourceName) {
        return "/assets/" + MODID + "/" + kind + "/" + resourceName + ".json";
    }

    private static String sanitizeCategoryPath(String category) {
        if (category == null || category.isBlank()) {
            return "";
        }
        String lower = category.toLowerCase(Locale.ROOT);
        return lower.replaceAll("[^a-z0-9_\\-]+", "_");
    }

    public static boolean isTransparentBlock(String resourceName) {
        if (resourceName == null) {
            return false;
        }
        return BLOCK_TRANSPARENCY.getOrDefault(resourceName, false);
    }

    private record BlockDefinition(String resourceName, int id, String category, boolean transparent,
            BlockBehaviour.Properties properties) {
    }
}

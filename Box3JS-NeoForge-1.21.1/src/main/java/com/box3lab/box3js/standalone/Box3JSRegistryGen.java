package com.box3lab.box3js.standalone;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads {@code registries/blocks.json} and {@code registries/creativeTabs.json}
 * from the project directory and generates Java DeferredRegister source code
 * that gets injected into the generated {@code @Mod} class.
 *
 * <p>
 * Only used during {@code /box3script compile} — never loaded at mod runtime.
 */
public class Box3JSRegistryGen {

    private static final Pattern JSON_KV = Pattern.compile("\"(\\w+)\"\\s*:\\s*");

    // ── Sound type mapping ──
    private static final Map<String, String> SOUND_TYPE_MAP = Map.ofEntries(
            Map.entry("wood", "WOOD"), Map.entry("stone", "STONE"),
            Map.entry("metal", "METAL"), Map.entry("glass", "GLASS"),
            Map.entry("wool", "WOOL"), Map.entry("sand", "SAND"),
            Map.entry("snow", "SNOW"), Map.entry("slime", "SLIME_BLOCK"),
            Map.entry("anvil", "ANVIL"), Map.entry("gravel", "GRAVEL"),
            Map.entry("grass", "GRASS"), Map.entry("bamboo", "BAMBOO"),
            Map.entry("netherite", "NETHERITE_BLOCK"), Map.entry("empty", "EMPTY"),
            Map.entry("powder_snow", "POWDER_SNOW"), Map.entry("sculk", "SCULK"),
            Map.entry("vine", "VINE"), Map.entry("ladder", "LADDER"),
            Map.entry("lantern", "LANTERN"), Map.entry("chain", "CHAIN"));

    // ── MapColor mapping ──
    private static final Map<String, String> MAP_COLOR_MAP = Map.ofEntries(
            Map.entry("none", "NONE"), Map.entry("grass", "GRASS"),
            Map.entry("sand", "SAND"), Map.entry("wool", "WOOL"),
            Map.entry("fire", "FIRE"), Map.entry("ice", "ICE"),
            Map.entry("metal", "METAL"), Map.entry("plant", "PLANT"),
            Map.entry("snow", "SNOW"), Map.entry("clay", "CLAY"),
            Map.entry("dirt", "DIRT"), Map.entry("stone", "STONE"),
            Map.entry("water", "WATER"), Map.entry("wood", "WOOD"),
            Map.entry("quartz", "QUARTZ"), Map.entry("color_orange", "COLOR_ORANGE"),
            Map.entry("color_magenta", "COLOR_MAGENTA"), Map.entry("color_light_blue", "COLOR_LIGHT_BLUE"),
            Map.entry("color_yellow", "COLOR_YELLOW"), Map.entry("color_light_green", "COLOR_LIGHT_GREEN"),
            Map.entry("color_pink", "COLOR_PINK"), Map.entry("color_gray", "COLOR_GRAY"),
            Map.entry("color_light_gray", "COLOR_LIGHT_GRAY"), Map.entry("color_cyan", "COLOR_CYAN"),
            Map.entry("color_purple", "COLOR_PURPLE"), Map.entry("color_blue", "COLOR_BLUE"),
            Map.entry("color_brown", "COLOR_BROWN"), Map.entry("color_green", "COLOR_GREEN"),
            Map.entry("color_red", "COLOR_RED"), Map.entry("color_black", "COLOR_BLACK"),
            Map.entry("gold", "GOLD"), Map.entry("diamond", "DIAMOND"),
            Map.entry("lapis", "LAPIS"), Map.entry("emerald", "EMERALD"),
            Map.entry("podzol", "PODZOL"), Map.entry("nether", "NETHER"),
            Map.entry("terracotta_white", "TERRACOTTA_WHITE"), Map.entry("terracotta_orange", "TERRACOTTA_ORANGE"),
            Map.entry("terracotta_magenta", "TERRACOTTA_MAGENTA"),
            Map.entry("terracotta_light_blue", "TERRACOTTA_LIGHT_BLUE"),
            Map.entry("terracotta_yellow", "TERRACOTTA_YELLOW"),
            Map.entry("terracotta_light_green", "TERRACOTTA_LIGHT_GREEN"),
            Map.entry("terracotta_pink", "TERRACOTTA_PINK"), Map.entry("terracotta_gray", "TERRACOTTA_GRAY"),
            Map.entry("terracotta_light_gray", "TERRACOTTA_LIGHT_GRAY"),
            Map.entry("terracotta_cyan", "TERRACOTTA_CYAN"),
            Map.entry("terracotta_purple", "TERRACOTTA_PURPLE"), Map.entry("terracotta_blue", "TERRACOTTA_BLUE"),
            Map.entry("terracotta_brown", "TERRACOTTA_BROWN"), Map.entry("terracotta_green", "TERRACOTTA_GREEN"),
            Map.entry("terracotta_red", "TERRACOTTA_RED"), Map.entry("terracotta_black", "TERRACOTTA_BLACK"),
            Map.entry("crimson_nylium", "CRIMSON_NYLIUM"), Map.entry("crimson_stem", "CRIMSON_STEM"),
            Map.entry("crimson_hyphae", "CRIMSON_HYPHAE"), Map.entry("warped_nylium", "WARPED_NYLIUM"),
            Map.entry("warped_stem", "WARPED_STEM"), Map.entry("warped_hyphae", "WARPED_HYPHAE"),
            Map.entry("warped_wart_block", "WARPED_WART_BLOCK"));

    /**
     * Parsed block definition.
     */
    public record BlockDef(
            String id, double hardness, double resistance, String sound,
            int lightLevel, String mapColor, double friction,
            double speedFactor, double jumpFactor,
            boolean noOcclusion, boolean noCollision,
            boolean requiresTool, boolean instabreak,
            String creativeTab) {
    }

    /**
     * Parsed creative tab definition.
     */
    public record CreativeTabDef(
            String id, String title, String icon,
            boolean searchBar, boolean rightAligned) {
    }

    /**
     * Parsed item definition (food, decorative, tool, or armor).
     */
    public record ItemDef(
            String id, String type, String displayName,
            String rarity, int maxStackSize, boolean glint,
            String creativeTab,
            int nutrition, double saturation, boolean alwaysEdible,
            String tier, String armorTexture) {
        public boolean isTool() {
            return "sword".equals(type) || "pickaxe".equals(type) || "axe".equals(type)
                    || "shovel".equals(type) || "hoe".equals(type);
        }

        public boolean isArmor() {
            return "helmet".equals(type) || "chestplate".equals(type)
                    || "leggings".equals(type) || "boots".equals(type);
        }

        public boolean isEquipment() {
            return isTool() || isArmor();
        }
    }

    /**
     * Parsed sound definition.
     */
    public record SoundDef(
            String id, String subtitle, boolean stream) {
    }

    // ── JSON parsing ──

    /**
     * Reads and parses {@code registries/blocks.json} if it exists.
     * Returns an empty list if the file is absent.
     */
    public static List<BlockDef> readBlocks(Path projectDir) {
        Path file = projectDir.resolve("registries/blocks.json");
        if (!Files.exists(file))
            return List.of();
        String raw = readFile(file);
        if (raw.isEmpty())
            return List.of();
        List<BlockDef> blocks = new ArrayList<>();
        int pos = 0;
        while ((pos = raw.indexOf('"', pos)) != -1) {
            int keyStart = pos + 1;
            int keyEnd = raw.indexOf('"', keyStart);
            if (keyEnd == -1)
                break;
            String blockId = raw.substring(keyStart, keyEnd);
            pos = raw.indexOf('{', keyEnd);
            if (pos == -1)
                break;
            int blockEnd = findMatchingBrace(raw, pos);
            if (blockEnd == -1)
                break;
            String body = raw.substring(pos + 1, blockEnd);
            blocks.add(parseBlockDef(blockId, body));
            pos = blockEnd + 1;
        }
        return blocks;
    }

    /**
     * Reads and parses {@code registries/creativeTabs.json} if it exists.
     */
    public static List<CreativeTabDef> readCreativeTabs(Path projectDir) {
        Path file = projectDir.resolve("registries/creativeTabs.json");
        if (!Files.exists(file))
            return List.of();
        String raw = readFile(file);
        if (raw.isEmpty())
            return List.of();
        List<CreativeTabDef> tabs = new ArrayList<>();
        int pos = 0;
        while ((pos = raw.indexOf('"', pos)) != -1) {
            int keyStart = pos + 1;
            int keyEnd = raw.indexOf('"', keyStart);
            if (keyEnd == -1)
                break;
            String tabId = raw.substring(keyStart, keyEnd);
            pos = raw.indexOf('{', keyEnd);
            if (pos == -1)
                break;
            int tabEnd = findMatchingBrace(raw, pos);
            if (tabEnd == -1)
                break;
            String body = raw.substring(pos + 1, tabEnd);
            tabs.add(parseCreativeTabDef(tabId, body));
            pos = tabEnd + 1;
        }
        return tabs;
    }

    /**
     * Reads and parses {@code registries/items.json} if it exists.
     */
    public static List<ItemDef> readItems(Path projectDir) {
        Path file = projectDir.resolve("registries/items.json");
        if (!Files.exists(file))
            return List.of();
        String raw = readFile(file);
        if (raw.isEmpty())
            return List.of();
        List<ItemDef> items = new ArrayList<>();
        int pos = 0;
        while ((pos = raw.indexOf('"', pos)) != -1) {
            int keyStart = pos + 1;
            int keyEnd = raw.indexOf('"', keyStart);
            if (keyEnd == -1)
                break;
            String itemId = raw.substring(keyStart, keyEnd);
            pos = raw.indexOf('{', keyEnd);
            if (pos == -1)
                break;
            int itemEnd = findMatchingBrace(raw, pos);
            if (itemEnd == -1)
                break;
            String body = raw.substring(pos + 1, itemEnd);
            items.add(parseItemDef(itemId, body));
            pos = itemEnd + 1;
        }
        return items;
    }

    /**
     * Reads and parses {@code registries/sounds.json} if it exists.
     */
    public static List<SoundDef> readSounds(Path projectDir) {
        Path file = projectDir.resolve("registries/sounds.json");
        if (!Files.exists(file))
            return List.of();
        String raw = readFile(file);
        if (raw.isEmpty())
            return List.of();
        List<SoundDef> sounds = new ArrayList<>();
        int pos = 0;
        while ((pos = raw.indexOf('"', pos)) != -1) {
            int keyStart = pos + 1;
            int keyEnd = raw.indexOf('"', keyStart);
            if (keyEnd == -1)
                break;
            String soundId = raw.substring(keyStart, keyEnd);
            pos = raw.indexOf('{', keyEnd);
            if (pos == -1)
                break;
            int soundEnd = findMatchingBrace(raw, pos);
            if (soundEnd == -1)
                break;
            String body = raw.substring(pos + 1, soundEnd);
            sounds.add(parseSoundDef(soundId, body));
            pos = soundEnd + 1;
        }
        return sounds;
    }

    // ── Tier / ArmorMaterial mappings ──
    private static final Map<String, String> TIER_MAP = Map.ofEntries(
            Map.entry("wood", "Tiers.WOOD"), Map.entry("stone", "Tiers.STONE"),
            Map.entry("iron", "Tiers.IRON"), Map.entry("gold", "Tiers.GOLD"),
            Map.entry("diamond", "Tiers.DIAMOND"), Map.entry("netherite", "Tiers.NETHERITE"));

    private static final Map<String, String> ARMOR_MATERIAL_MAP = Map.ofEntries(
            Map.entry("leather", "ArmorMaterials.LEATHER"), Map.entry("chain", "ArmorMaterials.CHAIN"),
            Map.entry("iron", "ArmorMaterials.IRON"), Map.entry("gold", "ArmorMaterials.GOLD"),
            Map.entry("diamond", "ArmorMaterials.DIAMOND"), Map.entry("netherite", "ArmorMaterials.NETHERITE"),
            Map.entry("turtle", "ArmorMaterials.TURTLE"));

    private static final Map<String, String> ARMOR_TYPE_MAP = Map.of(
            "helmet", "ArmorItem.Type.HELMET",
            "chestplate", "ArmorItem.Type.CHESTPLATE",
            "leggings", "ArmorItem.Type.LEGGINGS",
            "boots", "ArmorItem.Type.BOOTS");

    private static final Map<String, String> TOOL_CLASS_MAP = Map.of(
            "sword", "SwordItem", "pickaxe", "PickaxeItem",
            "axe", "AxeItem", "shovel", "ShovelItem", "hoe", "HoeItem");

    private static BlockDef parseBlockDef(String id, String body) {
        double hardness = 1.0, resistance = 1.0, friction = 0.6;
        double speedFactor = 1.0, jumpFactor = 1.0;
        int lightLevel = 0;
        String sound = "stone", mapColor = "stone", creativeTab = "";
        boolean noOcclusion = false, noCollision = false;
        boolean requiresTool = false, instabreak = false;

        Matcher m = JSON_KV.matcher(body);
        int lastEnd = 0;
        while (m.find(lastEnd)) {
            String key = m.group(1);
            int valStart = m.end();
            lastEnd = valStart;
            switch (key) {
                case "hardness" -> {
                    double[] v = { hardness };
                    parseDouble(body, valStart, v);
                    hardness = v[0];
                }
                case "resistance" -> {
                    double[] v = { resistance };
                    parseDouble(body, valStart, v);
                    resistance = v[0];
                }
                case "friction" -> {
                    double[] v = { friction };
                    parseDouble(body, valStart, v);
                    friction = v[0];
                }
                case "speedFactor" -> {
                    double[] v = { speedFactor };
                    parseDouble(body, valStart, v);
                    speedFactor = v[0];
                }
                case "jumpFactor" -> {
                    double[] v = { jumpFactor };
                    parseDouble(body, valStart, v);
                    jumpFactor = v[0];
                }
                case "lightLevel" -> {
                    int[] v = { lightLevel };
                    parseInt(body, valStart, v);
                    lightLevel = Math.max(0, Math.min(15, v[0]));
                }
                case "sound" -> sound = parseString(body, valStart, "stone");
                case "mapColor" -> mapColor = parseString(body, valStart, "stone");
                case "creativeTab" -> creativeTab = parseString(body, valStart, "");
                case "noOcclusion" -> noOcclusion = parseBool(body, valStart);
                case "noCollision" -> noCollision = parseBool(body, valStart);
                case "requiresTool" -> requiresTool = parseBool(body, valStart);
                case "instabreak" -> instabreak = parseBool(body, valStart);
            }
        }
        return new BlockDef(id, hardness, resistance, sound, lightLevel, mapColor,
                friction, speedFactor, jumpFactor, noOcclusion, noCollision,
                requiresTool, instabreak, creativeTab);
    }

    private static CreativeTabDef parseCreativeTabDef(String id, String body) {
        String title = id, icon = "";
        boolean searchBar = false, rightAligned = false;
        Matcher m = JSON_KV.matcher(body);
        int lastEnd = 0;
        while (m.find(lastEnd)) {
            String key = m.group(1);
            int valStart = m.end();
            lastEnd = valStart;
            switch (key) {
                case "title" -> title = parseString(body, valStart, id);
                case "icon" -> icon = parseString(body, valStart, "");
                case "searchBar" -> searchBar = parseBool(body, valStart);
                case "rightAligned" -> rightAligned = parseBool(body, valStart);
            }
        }
        return new CreativeTabDef(id, title, icon, searchBar, rightAligned);
    }

    private static ItemDef parseItemDef(String id, String body) {
        String type = "item", displayName = id, rarity = "common", creativeTab = "";
        int maxStackSize = 64;
        boolean glint = false;
        int nutrition = 4;
        double saturation = 0.6;
        boolean alwaysEdible = false;
        String tier = "iron";
        String armorTexture = "";

        Matcher m = JSON_KV.matcher(body);
        int lastEnd = 0;
        while (m.find(lastEnd)) {
            String key = m.group(1);
            int valStart = m.end();
            lastEnd = valStart;
            switch (key) {
                case "type" -> type = parseString(body, valStart, "item");
                case "displayName" -> displayName = parseString(body, valStart, id);
                case "rarity" -> rarity = parseString(body, valStart, "common");
                case "creativeTab" -> creativeTab = parseString(body, valStart, "");
                case "tier" -> tier = parseString(body, valStart, "iron");
                case "armorTexture" -> armorTexture = parseString(body, valStart, "");
                case "maxStackSize" -> {
                    int[] v = { maxStackSize };
                    parseInt(body, valStart, v);
                    maxStackSize = Math.max(1, Math.min(64, v[0]));
                }
                case "glint" -> glint = parseBool(body, valStart);
                case "nutrition" -> {
                    int[] v = { nutrition };
                    parseInt(body, valStart, v);
                    nutrition = Math.max(1, Math.min(20, v[0]));
                }
                case "saturation" -> {
                    double[] v = { saturation };
                    parseDouble(body, valStart, v);
                    saturation = v[0];
                }
                case "alwaysEdible" -> alwaysEdible = parseBool(body, valStart);
            }
        }
        return new ItemDef(id, type, displayName, rarity, maxStackSize, glint,
                creativeTab, nutrition, saturation, alwaysEdible, tier, armorTexture);
    }

    private static SoundDef parseSoundDef(String id, String body) {
        String subtitle = "";
        boolean stream = false;
        Matcher m = JSON_KV.matcher(body);
        int lastEnd = 0;
        while (m.find(lastEnd)) {
            String key = m.group(1);
            int valStart = m.end();
            lastEnd = valStart;
            switch (key) {
                case "subtitle" -> subtitle = parseString(body, valStart, "");
                case "stream" -> stream = parseBool(body, valStart);
            }
        }
        return new SoundDef(id, subtitle, stream);
    }

    // ── Code generation ──

    /**
     * Generates the Java source code for DeferredRegister fields and
     * registration calls that get injected into the generated @Mod class.
     *
     * @param modId  the modId for DeferredRegister.createBlocks/createItems
     * @param blocks parsed block definitions
     * @param tabs   parsed creative tab definitions
     * @param items  parsed item definitions
     * @param sounds parsed sound definitions
     * @return array of [fieldDeclarations, constructorRegistrations]
     */
    public static String[] generateJavaCode(String modId, List<BlockDef> blocks,
            List<CreativeTabDef> tabs, List<ItemDef> items, List<SoundDef> sounds) {
        StringBuilder fields = new StringBuilder();
        StringBuilder regs = new StringBuilder();

        if (blocks.isEmpty() && tabs.isEmpty() && items.isEmpty() && sounds.isEmpty())
            return new String[] { "", "" };

        boolean needItemsRegister = !blocks.isEmpty() || !items.isEmpty();

        if (!blocks.isEmpty()) {
            fields.append("""
                    \n    private static final DeferredRegister.Blocks BLOCKS =
                            DeferredRegister.createBlocks("%s");
                        private static final DeferredRegister.Items ITEMS =
                            DeferredRegister.createItems("%s");
                    """.formatted(modId, modId));

            for (BlockDef b : blocks) {
                String field = b.id().toUpperCase();
                // Build BlockBehaviour.Properties chain
                StringBuilder props = new StringBuilder();
                props.append("BlockBehaviour.Properties.of()");
                props.append(".strength(").append(blockFloat(b.hardness()))
                        .append("f, ").append(blockFloat(b.resistance())).append("f)");
                String soundType = SOUND_TYPE_MAP.getOrDefault(b.sound(), "STONE");
                props.append(".sound(SoundType.").append(soundType).append(")");
                if (b.lightLevel() > 0)
                    props.append(".lightLevel(s -> ").append(b.lightLevel()).append(")");
                if (Math.abs(b.friction() - 0.6) > 0.001)
                    props.append(".friction(").append(blockFloat(b.friction())).append("f)");
                if (Math.abs(b.speedFactor() - 1.0) > 0.001)
                    props.append(".speedFactor(").append(blockFloat(b.speedFactor())).append("f)");
                if (Math.abs(b.jumpFactor() - 1.0) > 0.001)
                    props.append(".jumpFactor(").append(blockFloat(b.jumpFactor())).append("f)");
                if (b.noOcclusion())
                    props.append(".noOcclusion()");
                if (b.noCollision())
                    props.append(".noCollision()");
                if (b.requiresTool())
                    props.append(".requiresCorrectToolForDrops()");
                if (b.instabreak())
                    props.append(".instabreak()");
                String mc = MAP_COLOR_MAP.getOrDefault(b.mapColor(), "STONE");
                if (!"STONE".equals(mc))
                    props.append(".mapColor(MapColor.").append(mc).append(")");

                fields.append("""
                        \n    public static final DeferredBlock<Block> %s =
                                BLOCKS.registerSimpleBlock("%s", %s);
                            public static final DeferredItem<BlockItem> %s_ITEM =
                                ITEMS.registerSimpleBlockItem(%s);
                        """.formatted(field, b.id(), props, field, field));
            }

            regs.append("        BLOCKS.register(modEventBus);\n");
            regs.append("        ITEMS.register(modEventBus);\n");
        } else if (!items.isEmpty()) {
            // Items-only project: need ITEMS register without BLOCKS
            fields.append("""
                    \n    private static final DeferredRegister.Items ITEMS =
                            DeferredRegister.createItems("%s");
                    """.formatted(modId));
        }

        // Standalone item registrations (food, decorative, tools, armor)
        if (!items.isEmpty()) {
            // Pre-pass: map armorTexture → base tier for custom ArmorMaterial generation
            var armorTexTier = new java.util.LinkedHashMap<String, String>();
            for (ItemDef it : items) {
                if (it.isArmor() && !it.armorTexture().isEmpty()) {
                    armorTexTier.putIfAbsent(it.armorTexture(), it.tier());
                }
            }
            // Generate custom ArmorMaterial Holder fields
            for (var entry : armorTexTier.entrySet()) {
                String texName = entry.getKey(); // e.g. "star"
                String baseTier = entry.getValue(); // e.g. "diamond"
                String fieldName = texName.toUpperCase() + "_ARMOR";
                fields.append("""
                        \n    private static final Holder<ArmorMaterial> %1$s =
                                Holder.direct(new ArmorMaterial(
                                    ArmorMaterials.%6$s.value().defense(),
                                    ArmorMaterials.%6$s.value().enchantmentValue(),
                                    ArmorMaterials.%6$s.value().equipSound(),
                                    ArmorMaterials.%6$s.value().repairIngredient(),
                                    java.util.List.of(new ArmorMaterial.Layer(
                                        ResourceLocation.fromNamespaceAndPath("%2$s", "%3$s"))),
                                    ArmorMaterials.%6$s.value().toughness(),
                                    ArmorMaterials.%6$s.value().knockbackResistance()));
                        """.formatted(fieldName, modId, texName, "", "", baseTier.toUpperCase()));
            }

            for (ItemDef it : items) {
                String field = it.id().toUpperCase();
                if (it.isTool()) {
                    String toolClass = TOOL_CLASS_MAP.get(it.type());
                    String tierExpr = TIER_MAP.getOrDefault(it.tier(), "Tiers.IRON");
                    var props = buildItemProperties(it);
                    fields.append("""
                            \n    public static final DeferredItem<%1$s> %2$s =
                                    ITEMS.register("%3$s", () -> new %1$s(
                                            %4$s,
                                            %5$s));
                            """.formatted(toolClass, field, it.id(), tierExpr, props));
                } else if (it.isArmor()) {
                    String armorMaterial;
                    if (!it.armorTexture().isEmpty()) {
                        armorMaterial = it.armorTexture().toUpperCase() + "_ARMOR";
                    } else {
                        armorMaterial = ARMOR_MATERIAL_MAP.getOrDefault(it.tier(), "ArmorMaterials.IRON");
                    }
                    String armorType = ARMOR_TYPE_MAP.get(it.type());
                    var props = buildItemProperties(it);
                    fields.append("""
                            \n    public static final DeferredItem<ArmorItem> %1$s =
                                    ITEMS.register("%2$s", () -> new ArmorItem(
                                            %3$s, %4$s,
                                            %5$s));
                            """.formatted(field, it.id(), armorMaterial, armorType, props));
                } else {
                    var props = buildItemProperties(it);
                    fields.append("""
                            \n    public static final DeferredItem<Item> %s =
                                    ITEMS.register("%s", () -> new Item(%s));
                            """.formatted(field, it.id(), props));
                }
            }
            if (blocks.isEmpty()) {
                regs.append("        ITEMS.register(modEventBus);\n");
            }
        }

        // Sound registrations
        if (!sounds.isEmpty()) {
            fields.append("""
                    \n    private static final DeferredRegister<SoundEvent> SOUNDS =
                            DeferredRegister.create(Registries.SOUND_EVENT, "%s");
                    """.formatted(modId));

            for (SoundDef s : sounds) {
                String field = s.id().toUpperCase();
                fields.append("""
                        \n    public static final DeferredHolder<SoundEvent, SoundEvent> %s =
                                SOUNDS.register("%s",
                                        () -> SoundEvent.createVariableRangeEvent(
                                                ResourceLocation.fromNamespaceAndPath("%s", "%s")));
                        """.formatted(field, s.id(), modId, s.id()));
            }

            regs.append("        SOUNDS.register(modEventBus);\n");
        }

        if (!tabs.isEmpty()) {
            if (!needItemsRegister && sounds.isEmpty()) {
                // Tabs need an items register for icons even without blocks/items
                fields.append("""
                        \n    private static final DeferredRegister.Items ITEMS =
                                DeferredRegister.createItems("%s");
                        """.formatted(modId));
            }
            fields.append("""
                    \n    private static final DeferredRegister<CreativeModeTab> TABS =
                            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, "%s");
                    """.formatted(modId));

            int tabIndex = 0;
            for (CreativeTabDef t : tabs) {
                String tabField = t.id().toUpperCase() + "_TAB";
                // Collect block items for this tab
                StringBuilder itemsCode = new StringBuilder();
                for (BlockDef b : blocks) {
                    if (t.id().equals(b.creativeTab())) {
                        itemsCode.append("output.accept(").append(b.id().toUpperCase()).append("_ITEM.get());\n");
                    }
                }
                // Collect standalone items for this tab
                for (ItemDef it : items) {
                    if (t.id().equals(it.creativeTab())) {
                        itemsCode.append("output.accept(").append(it.id().toUpperCase()).append(".get());\n");
                    }
                }
                // Icon: search items first, then blocks
                String iconExpr = null;
                if (!t.icon().isEmpty()) {
                    // Search items
                    for (ItemDef it : items) {
                        if (it.id().equals(t.icon())) {
                            iconExpr = it.id().toUpperCase() + ".get()";
                            break;
                        }
                    }
                    // Search blocks
                    if (iconExpr == null) {
                        for (BlockDef b : blocks) {
                            if (b.id().equals(t.icon())) {
                                iconExpr = b.id().toUpperCase() + "_ITEM.get()";
                                break;
                            }
                        }
                    }
                }
                StringBuilder tabOpts = new StringBuilder();
                if (t.searchBar())
                    tabOpts.append(".withSearchBar()");
                if (t.rightAligned())
                    tabOpts.append(".alignedRight()");

                String titleText = escapeJavaString(t.title());
                fields.append("""
                        \n    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> %s =
                                TABS.register("%s", () -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, %d)
                                    .title(Component.literal("%s"))
                                    .icon(() -> new ItemStack(%s))
                                    .displayItems((params, output) -> {
                                        %s                                })
                                    %s.build());
                        """.formatted(tabField, t.id(),
                        tabIndex,
                        titleText,
                        iconExpr != null ? iconExpr : "Items.STONE",
                        itemsCode.toString().indent(2).stripTrailing(),
                        tabOpts.toString()));
                tabIndex++;
            }

            regs.append("        TABS.register(modEventBus);\n");
        }

        return new String[] { fields.toString(), regs.toString() };
    }

    private static String buildItemProperties(ItemDef it) {
        StringBuilder props = new StringBuilder();
        props.append("new Item.Properties()");
        if (!it.isEquipment() && it.maxStackSize() != 64)
            props.append(".stacksTo(").append(it.maxStackSize()).append(")");
        if (!"common".equals(it.rarity()))
            props.append(".rarity(Rarity.").append(it.rarity().toUpperCase()).append(")");
        if ("food".equals(it.type())) {
            props.append(".food(new FoodProperties.Builder()");
            if (it.nutrition() != 4)
                props.append(".nutrition(").append(it.nutrition()).append(")");
            if (Math.abs(it.saturation() - 0.6) > 0.001)
                props.append(".saturationModifier(").append(it.saturation()).append("f)");
            if (it.alwaysEdible())
                props.append(".alwaysEdible()");
            props.append(".build())");
        }
        return props.toString();
    }

    /**
     * Generates the additional import statements needed for the generated code.
     */
    public static String generateImports(boolean hasBlocks, boolean hasTabs,
            boolean hasItems, boolean hasSounds,
            boolean hasTools, boolean hasArmor) {
        StringBuilder sb = new StringBuilder();
        if (hasBlocks) {
            sb.append("""
                    import net.neoforged.neoforge.registries.DeferredRegister;
                    import net.neoforged.neoforge.registries.DeferredBlock;
                    import net.neoforged.neoforge.registries.DeferredItem;
                    import net.minecraft.world.level.block.Block;
                    import net.minecraft.world.level.block.state.BlockBehaviour;
                    import net.minecraft.world.level.block.SoundType;
                    import net.minecraft.world.item.BlockItem;
                    import net.minecraft.world.level.material.MapColor;
                    """);
        } else if (hasItems || hasTabs) {
            sb.append("""
                    import net.neoforged.neoforge.registries.DeferredRegister;
                    import net.neoforged.neoforge.registries.DeferredItem;
                    """);
        }
        if (hasSounds) {
            sb.append("""
                    import net.neoforged.neoforge.registries.DeferredRegister;
                    import net.minecraft.core.registries.Registries;
                    import net.minecraft.sounds.SoundEvent;
                    import net.minecraft.resources.ResourceLocation;
                    import net.neoforged.neoforge.registries.DeferredHolder;
                    """);
        }
        if (hasItems || hasTools || hasArmor) {
            sb.append("import net.minecraft.world.item.Rarity;\n");
        }
        if (hasItems) {
            sb.append("""
                    import net.minecraft.world.item.Item;
                    import net.minecraft.world.food.FoodProperties;
                    """);
        }
        if (hasTools) {
            sb.append("""
                    import net.minecraft.world.item.SwordItem;
                    import net.minecraft.world.item.PickaxeItem;
                    import net.minecraft.world.item.AxeItem;
                    import net.minecraft.world.item.ShovelItem;
                    import net.minecraft.world.item.HoeItem;
                    import net.minecraft.world.item.Tiers;
                    """);
        }
        if (hasArmor) {
            sb.append("""
                    import net.minecraft.world.item.ArmorItem;
                    import net.minecraft.world.item.ArmorMaterials;
                    import net.minecraft.world.item.ArmorMaterial;
                    import net.minecraft.resources.ResourceLocation;
                    import net.minecraft.core.Holder;
                    """);
        }
        if (hasTabs) {
            if (!hasBlocks && !hasSounds) {
                sb.append("import net.neoforged.neoforge.registries.DeferredRegister;\n");
            }
            sb.append("""
                    import net.minecraft.core.registries.Registries;
                    import net.minecraft.world.item.CreativeModeTab;
                    import net.minecraft.world.item.ItemStack;
                    import net.minecraft.network.chat.Component;
                    import net.minecraft.world.item.Items;
                    import net.neoforged.neoforge.registries.DeferredHolder;
                    """);
        }
        return sb.toString();
    }

    // ── Helpers ──

    private static String readFile(Path path) {
        try {
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            return "";
        }
    }

    private static int findMatchingBrace(String s, int openPos) {
        int depth = 0;
        for (int i = openPos; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '{')
                depth++;
            else if (c == '}') {
                depth--;
                if (depth == 0)
                    return i;
            }
        }
        return -1;
    }

    private static String parseString(String body, int start, String def) {
        int q1 = body.indexOf('"', start);
        if (q1 == -1)
            return def;
        int q2 = body.indexOf('"', q1 + 1);
        if (q2 == -1)
            return def;
        return body.substring(q1 + 1, q2);
    }

    private static void parseDouble(String body, int start, double[] out) {
        int end = start;
        while (end < body.length() && (Character.isDigit(body.charAt(end))
                || body.charAt(end) == '.' || body.charAt(end) == '-'))
            end++;
        if (end > start) {
            try {
                out[0] = Double.parseDouble(body.substring(start, end));
            } catch (NumberFormatException ignored) {
            }
        }
    }

    private static void parseInt(String body, int start, int[] out) {
        int end = start;
        while (end < body.length() && Character.isDigit(body.charAt(end)))
            end++;
        if (end > start) {
            try {
                out[0] = Integer.parseInt(body.substring(start, end));
            } catch (NumberFormatException ignored) {
            }
        }
    }

    private static boolean parseBool(String body, int start) {
        String trimmed = body.substring(start, Math.min(start + 10, body.length())).trim();
        return trimmed.startsWith("true");
    }

    private static String blockFloat(double v) {
        if (v == (long) v)
            return String.valueOf((long) v);
        return String.valueOf(v);
    }

    private static String escapeJavaString(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * Generates {@code assets/<modId>/sounds.json} in standard Minecraft
     * resource pack format from the registries sound definitions.
     * Returns null if there are no sounds.
     */
    private static String escapeJsonString(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    public static String generateSoundsJson(String modId, List<SoundDef> sounds) {
        if (sounds.isEmpty())
            return null;
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        for (int i = 0; i < sounds.size(); i++) {
            if (i > 0)
                sb.append(",\n");
            SoundDef s = sounds.get(i);
            sb.append("  \"").append(s.id()).append("\": {\n");
            sb.append("    \"sounds\": [\"").append(modId).append(":").append(s.id()).append("\"]");
            if (!s.subtitle().isEmpty()) {
                sb.append(",\n    \"subtitle\": \"").append(escapeJsonString(s.subtitle())).append("\"");
            }
            if (s.stream()) {
                sb.append(",\n    \"stream\": true");
            }
            sb.append("\n  }");
        }
        sb.append("\n}\n");
        return sb.toString();
    }

}

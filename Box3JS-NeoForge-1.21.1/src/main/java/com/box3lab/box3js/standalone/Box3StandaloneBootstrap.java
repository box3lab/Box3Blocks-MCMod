package com.box3lab.box3js.standalone;

import com.box3lab.box3js.Box3JSNetwork;
import com.box3lab.box3js.script.Box3ScriptEngine;
import com.mojang.logging.LogUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;
import org.slf4j.Logger;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Base class for standalone compiled Box3JS script mods.
 *
 * <p>Each standalone JAR includes a generated {@code @Mod} subclass of this
 * class with hardcoded {@code scriptResource} and {@code projectName}.
 * The JAR bundles:
 * <ul>
 *   <li>Bundled JS source ({@code box3script/<modId>/server.js})</li>
 *   <li>{@code META-INF/neoforge.mods.toml} declaring a dependency on box3js</li>
 *   <li>Optional {@code logo.png} for the mod icon</li>
 * </ul>
 *
 * <p>The Box3JS main mod ({@code box3js}) must be present in {@code mods/}
 * to provide the Rhino runtime and API bindings.
 */
public class Box3StandaloneBootstrap {

    private static final Logger LOGGER = LogUtils.getLogger();

    private final String scriptResource;
    private final String projectName;
    private final Map<String, Supplier<Block>> blockSuppliers;
    private final Map<String, Supplier<BlockItem>> blockItemSuppliers;
    private final Map<String, Supplier<net.minecraft.world.item.Item>> itemSuppliers;
    private final Map<String, Supplier<SoundEvent>> soundSuppliers;
    private Box3ScriptEngine engine;
    private String clientScriptSource;

    /**
     * Called by the generated {@code @Mod} subclass with hardcoded metadata.
     *
     * @param modEventBus      the mod's event bus (unused; we use NeoForge.EVENT_BUS)
     * @param modContainer     the mod container (for display name, etc.)
     * @param scriptResource   resource path to the bundled JS (e.g. {@code box3script/a/server.js})
     * @param projectName      unique project name for scope isolation
     * @param blockSuppliers   map of blockId → Block supplier, for {@code registries.getBlock()} JS API
     * @param blockItemSuppliers map of blockId → BlockItem supplier
     * @param itemSuppliers    map of itemId → Item supplier, for {@code registries.getItem()} JS API
     * @param soundSuppliers   map of soundId → SoundEvent supplier, for {@code registries.getSound()} JS API
     */
    protected Box3StandaloneBootstrap(IEventBus modEventBus, ModContainer modContainer,
            String scriptResource, String projectName,
            Map<String, Supplier<Block>> blockSuppliers,
            Map<String, Supplier<BlockItem>> blockItemSuppliers,
            Map<String, Supplier<net.minecraft.world.item.Item>> itemSuppliers,
            Map<String, Supplier<SoundEvent>> soundSuppliers) {
        this.scriptResource = scriptResource;
        this.projectName = projectName;
        this.blockSuppliers = blockSuppliers;
        this.blockItemSuppliers = blockItemSuppliers;
        this.itemSuppliers = itemSuppliers;
        this.soundSuppliers = soundSuppliers;
        LOGGER.info("Loaded standalone script: project={} resource={}", projectName, scriptResource);

        NeoForge.EVENT_BUS.addListener(this::onServerStarted);
        NeoForge.EVENT_BUS.addListener(this::onServerTick);

        // ── Player join / leave ──
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedInEvent event) -> {
            if (engine != null && event.getEntity() instanceof ServerPlayer sp) {
                engine.firePlayerJoin(sp);
            }
        });
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedOutEvent event) -> {
            if (engine != null && event.getEntity() instanceof ServerPlayer sp)
                engine.firePlayerLeave(sp);
        });

        // ── Block break / place ──
        NeoForge.EVENT_BUS.addListener((BlockEvent.BreakEvent event) -> {
            if (engine != null && event.getPlayer() instanceof ServerPlayer sp)
                engine.fireVoxelDestroy(sp, event.getPos());
        });
        NeoForge.EVENT_BUS.addListener((BlockEvent.EntityPlaceEvent event) -> {
            if (engine != null && event.getEntity() instanceof ServerPlayer sp)
                engine.fireBlockPlace(sp, event.getPos(), event.getPlacedBlock());
        });

        // ── Entity interact ──
        NeoForge.EVENT_BUS.addListener((PlayerInteractEvent.EntityInteract event) -> {
            if (engine != null && event.getEntity() instanceof ServerPlayer sp)
                engine.fireInteract(sp, event.getTarget());
        });

        // ── Block activate (right-click block) ──
        NeoForge.EVENT_BUS.addListener((PlayerInteractEvent.RightClickBlock event) -> {
            if (engine != null && event.getEntity() instanceof ServerPlayer sp) {
                engine.fireBlockActivate(sp, event.getPos(),
                    event.getLevel().getBlockState(event.getPos()));
                engine.fireActionButton(sp, "ACTION1");
            }
        });

        // ── Action buttons ──
        NeoForge.EVENT_BUS.addListener((PlayerInteractEvent.LeftClickBlock event) -> {
            if (engine != null && event.getEntity() instanceof ServerPlayer sp)
                engine.fireActionButton(sp, "ACTION0");
        });
        NeoForge.EVENT_BUS.addListener((PlayerInteractEvent.LeftClickEmpty event) -> {
            if (engine != null && event.getEntity() instanceof ServerPlayer sp)
                engine.fireActionButton(sp, "ACTION0");
        });
        NeoForge.EVENT_BUS.addListener((PlayerInteractEvent.RightClickItem event) -> {
            if (engine != null && event.getEntity() instanceof ServerPlayer sp)
                engine.fireActionButton(sp, "ACTION1");
        });
        NeoForge.EVENT_BUS.addListener((PlayerInteractEvent.RightClickEmpty event) -> {
            if (engine != null && event.getEntity() instanceof ServerPlayer sp)
                engine.fireActionButton(sp, "ACTION1");
        });

        // ── Chat ──
        NeoForge.EVENT_BUS.addListener((ServerChatEvent event) -> {
            if (engine != null && event.getPlayer() instanceof ServerPlayer sp) {
                if (engine.fireChat(sp, event.getMessage().getString()))
                    event.setCanceled(true);
            }
        });

        // ── Entity death / respawn / damage ──
        NeoForge.EVENT_BUS.addListener((LivingDeathEvent event) -> {
            if (engine != null)
                engine.fireEntityDeath(event.getEntity(), event.getSource().getEntity());
        });
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerRespawnEvent event) -> {
            if (engine != null && event.getEntity() instanceof ServerPlayer sp)
                engine.firePlayerRespawn(sp);
        });
        NeoForge.EVENT_BUS.addListener((LivingDamageEvent.Pre event) -> {
            if (engine != null)
                engine.fireEntityDamage(event.getEntity(), event.getNewDamage(),
                    event.getSource().getMsgId(), event.getSource().getEntity());
        });
    }



    private void onServerStarted(ServerStartedEvent event) {
        MinecraftServer server = event.getServer();
        Path configDir = server.getServerDirectory().resolve("config");

        engine = Box3ScriptEngine.createStandalone(server, projectName, configDir);

        // Read bundled JS source from JAR resource
        String jsSource;
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(scriptResource)) {
            if (is == null) {
                LOGGER.error("Script resource not found in JAR: {}", scriptResource);
                return;
            }
            jsSource = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            LOGGER.error("Failed to read script resource: {}", scriptResource, e);
            return;
        }

        // Read client script from JAR resource (optional)
        String clientResource = "box3script/" + projectName + "/client.js";
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(clientResource)) {
            if (is != null) {
                clientScriptSource = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                LOGGER.info("Client script bundled in JAR, will send to joining players");
            }
        } catch (Exception e) {
            LOGGER.debug("No client script in JAR: {}", e.getMessage());
        }

        Context cx = Context.enter();
        try {
            // esbuild cjs output references module.exports; define the CJS globals
            cx.evaluateString(engine.getScope(),
                "var module = { exports: {} }; var exports = module.exports;",
                "cjs-init", 1, null);
            cx.evaluateString(engine.getScope(), jsSource, scriptResource, 1, null);

            // Set up registries global if blocks or items are registered
            if ((blockSuppliers != null && !blockSuppliers.isEmpty())
                    || (itemSuppliers != null && !itemSuppliers.isEmpty())
                    || (soundSuppliers != null && !soundSuppliers.isEmpty())) {
                setupRegistriesGlobal(cx);
            }

            LOGGER.info("Standalone script '{}' loaded successfully", projectName);
        } catch (Exception e) {
            LOGGER.error("Failed to execute standalone script: {}", projectName, e);
        } finally {
            Context.exit();
        }

        // Register client script with main mod so it can send via its own payload
        if (clientScriptSource != null) {
            Box3JSNetwork.registerStandaloneClientScript(projectName, clientScriptSource);
        }
    }

    private void setupRegistriesGlobal(Context cx) {
        Scriptable scope = engine.getScope();
        ScriptableObject registriesObj = (ScriptableObject) cx.newObject(scope);

        var blockMap = blockSuppliers != null ? blockSuppliers : Map.<String, Supplier<Block>>of();
        var blockItemMap = blockItemSuppliers != null ? blockItemSuppliers : Map.<String, Supplier<BlockItem>>of();
        ScriptableObject.putProperty(registriesObj, "getBlock", new BaseFunction() {
            @Override
            public Object call(Context cx, Scriptable scope,
                               Scriptable thisObj, Object[] args) {
                if (args.length < 1) return null;
                String id = args[0].toString();
                Supplier<Block> bs = blockMap.get(id);
                Supplier<BlockItem> bis = blockItemMap.get(id);
                if (bs == null || bis == null) return null;

                ScriptableObject result = (ScriptableObject) cx.newObject(scope);
                ScriptableObject.putProperty(result, "block",
                    Context.javaToJS(bs.get(), scope));
                ScriptableObject.putProperty(result, "itemId",
                    projectName + ":" + id);
                return result;
            }
        });

        ScriptableObject.putProperty(registriesObj, "hasBlock", new BaseFunction() {
            @Override
            public Object call(Context cx, Scriptable scope,
                               Scriptable thisObj, Object[] args) {
                if (args.length < 1) return false;
                return blockMap.containsKey(args[0].toString());
            }
        });

        ScriptableObject.putProperty(registriesObj, "listBlocks", new BaseFunction() {
            @Override
            public Object call(Context cx, Scriptable scope,
                               Scriptable thisObj, Object[] args) {
                return Context.javaToJS(
                    blockMap.keySet().toArray(new String[0]), scope);
            }
        });

        var itemMap = itemSuppliers != null ? itemSuppliers : Map.<String, Supplier<net.minecraft.world.item.Item>>of();
        ScriptableObject.putProperty(registriesObj, "getItem", new BaseFunction() {
            @Override
            public Object call(Context cx, Scriptable scope,
                               Scriptable thisObj, Object[] args) {
                if (args.length < 1) return null;
                String id = args[0].toString();
                Supplier<net.minecraft.world.item.Item> is = itemMap.get(id);
                if (is == null) return null;
                ScriptableObject result = (ScriptableObject) cx.newObject(scope);
                ScriptableObject.putProperty(result, "item",
                    Context.javaToJS(is.get(), scope));
                ScriptableObject.putProperty(result, "itemId",
                    projectName + ":" + id);
                return result;
            }
        });

        ScriptableObject.putProperty(registriesObj, "hasItem", new BaseFunction() {
            @Override
            public Object call(Context cx, Scriptable scope,
                               Scriptable thisObj, Object[] args) {
                if (args.length < 1) return false;
                return itemMap.containsKey(args[0].toString());
            }
        });

        ScriptableObject.putProperty(registriesObj, "listItems", new BaseFunction() {
            @Override
            public Object call(Context cx, Scriptable scope,
                               Scriptable thisObj, Object[] args) {
                return Context.javaToJS(
                    itemMap.keySet().toArray(new String[0]), scope);
            }
        });

        var soundMap = soundSuppliers != null ? soundSuppliers : Map.<String, Supplier<SoundEvent>>of();
        ScriptableObject.putProperty(registriesObj, "getSound", new BaseFunction() {
            @Override
            public Object call(Context cx, Scriptable scope,
                               Scriptable thisObj, Object[] args) {
                if (args.length < 1) return null;
                String id = args[0].toString();
                if (!soundMap.containsKey(id)) return null;
                ScriptableObject result = (ScriptableObject) cx.newObject(scope);
                ScriptableObject.putProperty(result, "soundId",
                    projectName + ":" + id);
                return result;
            }
        });

        ScriptableObject.putProperty(registriesObj, "hasSound", new BaseFunction() {
            @Override
            public Object call(Context cx, Scriptable scope,
                               Scriptable thisObj, Object[] args) {
                if (args.length < 1) return false;
                return soundMap.containsKey(args[0].toString());
            }
        });

        ScriptableObject.putProperty(registriesObj, "listSounds", new BaseFunction() {
            @Override
            public Object call(Context cx, Scriptable scope,
                               Scriptable thisObj, Object[] args) {
                return Context.javaToJS(
                    soundMap.keySet().toArray(new String[0]), scope);
            }
        });

        ScriptableObject.putProperty(scope, "registries", registriesObj);
        LOGGER.info("Registries global set up with {} block(s), {} item(s), {} sound(s)",
            blockSuppliers != null ? blockSuppliers.size() : 0,
            itemMap.size(),
            soundMap.size());
    }

    private void onServerTick(ServerTickEvent.Post event) {
        if (engine != null) {
            engine.fireTick();
        }
    }
}

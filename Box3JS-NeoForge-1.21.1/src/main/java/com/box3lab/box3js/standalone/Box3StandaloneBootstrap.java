package com.box3lab.box3js.standalone;

import com.box3lab.box3js.Box3JSNetwork;
import com.box3lab.box3js.script.Box3ScriptEngine;
import com.mojang.logging.LogUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.MinecraftServer;
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
import net.neoforged.neoforge.network.PacketDistributor;
import org.mozilla.javascript.Context;
import org.slf4j.Logger;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

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
    private Box3ScriptEngine engine;
    private String clientScriptSource;

    /**
     * Called by the generated {@code @Mod} subclass with hardcoded metadata.
     *
     * @param modEventBus    the mod's event bus (unused; we use NeoForge.EVENT_BUS)
     * @param modContainer   the mod container (for display name, etc.)
     * @param scriptResource resource path to the bundled JS (e.g. {@code box3script/a/server.js})
     * @param projectName    unique project name for scope isolation
     */
    protected Box3StandaloneBootstrap(IEventBus modEventBus, ModContainer modContainer,
            String scriptResource, String projectName) {
        this.scriptResource = scriptResource;
        this.projectName = projectName;
        LOGGER.info("Loaded standalone script: project={} resource={}", projectName, scriptResource);

        NeoForge.EVENT_BUS.addListener(this::onServerStarted);
        NeoForge.EVENT_BUS.addListener(this::onServerTick);

        // ── Player join / leave ──
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedInEvent event) -> {
            if (engine != null && event.getEntity() instanceof ServerPlayer sp) {
                engine.firePlayerJoin(sp);
                sendClientScript(sp);
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
            LOGGER.info("Standalone script '{}' loaded successfully", projectName);
        } catch (Exception e) {
            LOGGER.error("Failed to execute standalone script: {}", projectName, e);
        } finally {
            Context.exit();
        }

        // Send client script to already-connected players
        if (clientScriptSource != null) {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                sendClientScript(player);
            }
        }
    }

    private void sendClientScript(ServerPlayer player) {
        if (clientScriptSource == null) return;
        PacketDistributor.sendToPlayer(player,
                new Box3JSNetwork.ClientScriptPayload(projectName, clientScriptSource));
        LOGGER.debug("Sent client script '{}' to {}", projectName, player.getName().getString());
    }

    private void onServerTick(ServerTickEvent.Post event) {
        if (engine != null) {
            engine.fireTick();
        }
    }
}

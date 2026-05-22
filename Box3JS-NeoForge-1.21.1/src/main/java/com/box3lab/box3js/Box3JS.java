package com.box3lab.box3js;

import com.box3lab.box3js.client.Box3JSClientEngine;
import com.box3lab.box3js.client.Box3JSGuiProxy;
import com.box3lab.box3js.registries.Box3JSRecipeManager;
import com.box3lab.box3js.script.Box3ScriptCommand;
import com.box3lab.box3js.script.Box3ScriptEngine;
import com.box3lab.box3js.script.Box3JSGuiServerHandler;
import com.mojang.logging.LogUtils;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.slf4j.Logger;

@Mod(Box3JS.MODID)
public class Box3JS {

    public static final String MODID = "box3js";
    public static final Logger LOGGER = LogUtils.getLogger();

    /** Tracks which connected players have Box3JS installed on their client. */
    public static final Set<UUID> clientsWithBox3JS = ConcurrentHashMap.newKeySet();

    public Box3JS(IEventBus modEventBus, ModContainer modContainer) {
        // Register custom payloads
        modEventBus.addListener(RegisterPayloadHandlersEvent.class, event -> {
            var registrar = event.registrar("1");

            // Server → Client: send client scripts on join (optional — client without mod can still connect)
            registrar.optional().playToClient(
                Box3JSNetwork.ClientScriptPayload.TYPE,
                Box3JSNetwork.ClientScriptPayload.STREAM_CODEC,
                (payload, context) -> Box3JSClientEngine.get()
                        .loadScript(payload.projectName(), payload.scriptSource())
            );

            // Server → Client: remote event from server (optional)
            registrar.optional().playToClient(
                Box3JSNetwork.ServerEventPayload.TYPE,
                Box3JSNetwork.ServerEventPayload.STREAM_CODEC,
                (payload, context) -> Box3JSClientEngine.get()
                        .fireClientEvent(payload.projectName(), payload.tick(), payload.eventJson())
            );

            // Client → Server: remote event from client (optional)
            registrar.optional().playToServer(
                Box3JSNetwork.ClientEventPayload.TYPE,
                Box3JSNetwork.ClientEventPayload.STREAM_CODEC,
                (payload, context) -> {
                    if (context.player() instanceof ServerPlayer sp) {
                        clientsWithBox3JS.add(sp.getUUID());
                        Box3ScriptEngine.get().handleClientEvent(
                                sp, payload.projectName(), payload.eventJson());
                    }
                }
            );

            // Client → Server: GUI operations (open, setItem, registerCallbacks, close)
            registrar.optional().playToServer(
                Box3JSNetwork.GUIServerboundPayload.TYPE,
                Box3JSNetwork.GUIServerboundPayload.STREAM_CODEC,
                (payload, context) -> {
                    if (context.player() instanceof ServerPlayer sp) {
                        clientsWithBox3JS.add(sp.getUUID());
                        switch (payload.actionType()) {
                            case 0 -> Box3JSGuiServerHandler.handleOpen(
                                sp, payload.title(), payload.rows(), payload.slotsJson());
                            case 1 -> Box3JSGuiServerHandler.handleSetItem(
                                sp, payload.slot(), payload.itemId(), payload.count());
                            case 2 -> Box3JSGuiServerHandler.handleRegisterCallbacks(
                                sp, payload.hasSlotClick(), payload.hasClose());
                            case 3 -> Box3JSGuiServerHandler.handleClose(sp);
                        }
                    }
                }
            );

            // Server → Client: GUI events (slot click, close) for client-side JS callbacks
            registrar.optional().playToClient(
                Box3JSNetwork.GUIClientboundPayload.TYPE,
                Box3JSNetwork.GUIClientboundPayload.STREAM_CODEC,
                (payload, context) -> {
                    Box3JSGuiProxy proxy = Box3JSClientEngine.get().getActiveGuiProxy();
                    if (proxy != null) {
                        switch (payload.eventType()) {
                            case 0 -> proxy.fireSlotClick(payload.slot());
                            case 1 -> proxy.fireClose();
                        }
                    }
                }
            );
        });

        // Script commands
        NeoForge.EVENT_BUS.addListener(Box3ScriptCommand::register);

        // Tick
        NeoForge.EVENT_BUS.addListener((ServerTickEvent.Post event) -> {
            Box3ScriptEngine.get().fireTick();
        });

        // Player join / leave
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedInEvent event) -> {
            Box3JSNetwork.sendClientScripts((ServerPlayer) event.getEntity());
            Box3ScriptEngine.get().firePlayerJoin((ServerPlayer) event.getEntity());
        });
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedOutEvent event) -> {
            if (event.getEntity() instanceof ServerPlayer sp) {
                clientsWithBox3JS.remove(sp.getUUID());
            }
            Box3ScriptEngine.get().firePlayerLeave((ServerPlayer) event.getEntity());
        });

        // Block break
        NeoForge.EVENT_BUS.addListener((BlockEvent.BreakEvent event) -> {
            if (event.getPlayer() instanceof ServerPlayer sp) {
                if (Box3ScriptEngine.get().fireVoxelDestroy(sp, event.getPos())) {
                    event.setCanceled(true);
                }
            }
        });

        // Block place
        NeoForge.EVENT_BUS.addListener((BlockEvent.EntityPlaceEvent event) -> {
            if (event.getEntity() instanceof ServerPlayer sp) {
                if (Box3ScriptEngine.get().fireBlockPlace(sp, event.getPos(), event.getPlacedBlock())) {
                    event.setCanceled(true);
                }
            }
        });

        // Interact (entity)
        NeoForge.EVENT_BUS.addListener((PlayerInteractEvent.EntityInteract event) -> {
            if (event.getEntity() instanceof ServerPlayer sp) {
                if (Box3ScriptEngine.get().fireInteract(sp, event.getTarget())) {
                    event.setCanceled(true);
                }
            }
        });

        // Right-click block
        NeoForge.EVENT_BUS.addListener((PlayerInteractEvent.RightClickBlock event) -> {
            if (event.getEntity() instanceof ServerPlayer sp) {
                if (Box3ScriptEngine.get().fireBlockActivate(sp, event.getPos(), event.getLevel().getBlockState(event.getPos()))) {
                    event.setCanceled(true);
                }
                Box3ScriptEngine.get().fireActionButton(sp, "ACTION1");
            }
        });

        // Left-click (ACTION0) — block and air
        NeoForge.EVENT_BUS.addListener((PlayerInteractEvent.LeftClickBlock event) -> {
            if (event.getEntity() instanceof ServerPlayer sp) {
                Box3ScriptEngine.get().fireActionButton(sp, "ACTION0");
            }
        });
        NeoForge.EVENT_BUS.addListener((PlayerInteractEvent.LeftClickEmpty event) -> {
            if (event.getEntity() instanceof ServerPlayer sp) {
                Box3ScriptEngine.get().fireActionButton(sp, "ACTION0");
            }
        });

        // Right-click (ACTION1) — item and empty (block already covered above)
        NeoForge.EVENT_BUS.addListener((PlayerInteractEvent.RightClickItem event) -> {
            if (event.getEntity() instanceof ServerPlayer sp) {
                Box3ScriptEngine.get().fireActionButton(sp, "ACTION1");
            }
        });
        NeoForge.EVENT_BUS.addListener((PlayerInteractEvent.RightClickEmpty event) -> {
            if (event.getEntity() instanceof ServerPlayer sp) {
                Box3ScriptEngine.get().fireActionButton(sp, "ACTION1");
            }
        });

        // Chat
        NeoForge.EVENT_BUS.addListener((ServerChatEvent event) -> {
            if (event.getPlayer() instanceof ServerPlayer sp) {
                if (Box3ScriptEngine.get().fireChat(sp, event.getMessage().getString())) {
                    event.setCanceled(true);
                }
            }
        });

        // Entity death
        NeoForge.EVENT_BUS.addListener((LivingDeathEvent event) -> {
            Box3ScriptEngine.get().fireEntityDeath(event.getEntity(), event.getSource().getEntity());
        });

        // Player respawn
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerRespawnEvent event) -> {
            if (event.getEntity() instanceof ServerPlayer sp) {
                Box3ScriptEngine.get().firePlayerRespawn(sp);
            }
        });

        // Entity damage
        NeoForge.EVENT_BUS.addListener((LivingDamageEvent.Pre event) -> {
            if (Box3ScriptEngine.get().fireEntityDamage(event.getEntity(),
                    event.getNewDamage(),
                    event.getSource().getMsgId(),
                    event.getSource().getEntity())) {
                event.setNewDamage(0);
            }
        });

        // Auto-load server scripts from config/box3/script/<project>/dist/server.js on server start
        NeoForge.EVENT_BUS.addListener((ServerStartedEvent event) -> {
            Box3ScriptEngine.get().autoLoad(event.getServer());
            Box3JSRecipeManager.init(event.getServer());
        });

        LOGGER.info("Box3JS script engine initialized.");
    }
}

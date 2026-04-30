package com.box3lab.box3js;

import com.box3lab.box3js.script.Box3ScriptCommand;
import com.box3lab.box3js.script.Box3ScriptEngine;
import com.mojang.logging.LogUtils;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
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

    public Box3JS(IEventBus modEventBus, ModContainer modContainer) {
        // Script commands
        NeoForge.EVENT_BUS.addListener(Box3ScriptCommand::register);

        // Tick
        NeoForge.EVENT_BUS.addListener((ServerTickEvent.Post event) -> {
            Box3ScriptEngine.get().fireTick();
        });

        // Player join / leave
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedInEvent event) -> {
            Box3ScriptEngine.get().firePlayerJoin((ServerPlayer) event.getEntity());
        });
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedOutEvent event) -> {
            Box3ScriptEngine.get().firePlayerLeave((ServerPlayer) event.getEntity());
        });

        // Block break
        NeoForge.EVENT_BUS.addListener((BlockEvent.BreakEvent event) -> {
            if (event.getPlayer() instanceof ServerPlayer sp) {
                Box3ScriptEngine.get().fireVoxelDestroy(sp, event.getPos());
            }
        });

        // Block place
        NeoForge.EVENT_BUS.addListener((BlockEvent.EntityPlaceEvent event) -> {
            if (event.getEntity() instanceof ServerPlayer sp) {
                Box3ScriptEngine.get().fireBlockPlace(sp, event.getPos(), event.getPlacedBlock());
            }
        });

        // Interact (entity)
        NeoForge.EVENT_BUS.addListener((PlayerInteractEvent.EntityInteract event) -> {
            if (event.getEntity() instanceof ServerPlayer sp) {
                Box3ScriptEngine.get().fireInteract(sp, event.getTarget());
            }
        });

        // Right-click block
        NeoForge.EVENT_BUS.addListener((PlayerInteractEvent.RightClickBlock event) -> {
            if (event.getEntity() instanceof ServerPlayer sp) {
                Box3ScriptEngine.get().fireBlockActivate(sp, event.getPos(), event.getLevel().getBlockState(event.getPos()));
            }
        });

        // Chat
        NeoForge.EVENT_BUS.addListener((ServerChatEvent event) -> {
            if (event.getPlayer() instanceof ServerPlayer sp) {
                Box3ScriptEngine.get().fireChat(sp, event.getMessage().getString());
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
            Box3ScriptEngine.get().fireEntityDamage(event.getEntity(),
                    event.getNewDamage(),
                    event.getSource().getMsgId(),
                    event.getSource().getEntity());
        });

        // Auto-load scripts from config/box3/script/<project>/app.js on server start
        NeoForge.EVENT_BUS.addListener((ServerStartedEvent event) -> {
            Box3ScriptEngine.get().autoLoad(event.getServer());
        });

        LOGGER.info("Box3JS script engine initialized.");
    }
}

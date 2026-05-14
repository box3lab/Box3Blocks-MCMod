package com.box3lab.box3js.script;

import com.box3lab.box3js.Box3JSNetwork;
import com.mojang.logging.LogUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.network.PacketDistributor;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;

/**
 * Handles client→server GUI packets on the server thread.
 * Creates containers, manages active GUIs per player, and forwards events back to the client.
 */
public final class Box3JSGuiServerHandler {

    private static final Logger LOGGER = LogUtils.getLogger();

    private Box3JSGuiServerHandler() {}

    /** Vanilla generic chest menu types mapped by row count (1-6). */
    private static final MenuType<?>[] CHEST_TYPES = {
        MenuType.GENERIC_9x1, MenuType.GENERIC_9x2, MenuType.GENERIC_9x3,
        MenuType.GENERIC_9x4, MenuType.GENERIC_9x5, MenuType.GENERIC_9x6
    };

    private static MenuType<?> chestType(int rows) {
        int idx = Math.clamp(rows, 1, 6) - 1;
        return CHEST_TYPES[idx];
    }

    private static final Map<UUID, ActiveGui> activeGuis = new ConcurrentHashMap<>();

    private static class ActiveGui {
        final Box3JSScriptContainerMenu menu;
        final Box3JSGuiController controller;
        final ServerPlayer player;
        boolean hasSlotClick;
        boolean hasClose;

        ActiveGui(Box3JSScriptContainerMenu menu, Box3JSGuiController controller, ServerPlayer player) {
            this.menu = menu;
            this.controller = controller;
            this.player = player;
        }
    }

    public static void handleOpen(ServerPlayer player, String title, int rows, String slotsJson) {
        // Close any existing GUI for this player
        handleClose(player);

        Box3JSGuiController controller = new Box3JSGuiController(null, player);

        MenuProvider provider = new SimpleMenuProvider((containerId, inv, p) -> {
            Box3JSScriptContainerMenu menu = new Box3JSScriptContainerMenu(
                chestType(rows), containerId, inv, rows, controller);
            controller.setMenu(menu);

            // Populate initial slots from JSON string like {"0":"minecraft:diamond","1":"minecraft:stone"}
            if (slotsJson != null && !slotsJson.isEmpty()) {
                String inner = slotsJson.trim();
                if (inner.startsWith("{") && inner.endsWith("}")) {
                    inner = inner.substring(1, inner.length() - 1);
                    for (String pair : inner.split(",")) {
                        pair = pair.trim();
                        if (pair.isEmpty()) continue;
                        int colon = pair.indexOf(':');
                        if (colon < 0) continue;
                        try {
                            String key = pair.substring(0, colon).trim();
                            if (key.startsWith("\"") && key.endsWith("\""))
                                key = key.substring(1, key.length() - 1);
                            int slot = Integer.parseInt(key);
                            String val = pair.substring(colon + 1).trim();
                            if (val.startsWith("\"") && val.endsWith("\""))
                                val = val.substring(1, val.length() - 1);
                            var item = Box3ScriptUtils.lookupItem(val);
                            if (item != null && slot >= 0 && slot < rows * 9) {
                                menu.getContainer().setItem(slot, new net.minecraft.world.item.ItemStack(item, 1));
                            }
                        } catch (NumberFormatException | IndexOutOfBoundsException e) {
                            LOGGER.debug("Ignoring invalid GUI slot entry '{}'", pair, e);
                        }
                    }
                }
            }

            return menu;
        }, Component.literal(title));

        player.openMenu(provider, buf -> buf.writeVarInt(rows));

        // After openMenu, the player's containerMenu is the new menu
        if (player.containerMenu instanceof Box3JSScriptContainerMenu menu) {
            ActiveGui gui = new ActiveGui(menu, controller, player);

            // Set up close notifier for cleanup + event forwarding
            controller.onCloseEvent(() -> {
                if (gui.hasClose) {
                    PacketDistributor.sendToPlayer(player,
                        new Box3JSNetwork.GUIClientboundPayload(1, 0));
                }
                activeGuis.remove(player.getUUID());
            });

            // Set up slot click notifier for event forwarding
            controller.onSlotClickEvent(slot -> {
                if (gui.hasSlotClick) {
                    PacketDistributor.sendToPlayer(player,
                        new Box3JSNetwork.GUIClientboundPayload(0, slot));
                }
            });

            activeGuis.put(player.getUUID(), gui);
        }
    }

    public static void handleSetItem(ServerPlayer player, int slot, String itemId, int count) {
        ActiveGui gui = activeGuis.get(player.getUUID());
        if (gui != null) {
            gui.controller.setItem(slot, itemId, count);
        }
    }

    public static void handleRegisterCallbacks(ServerPlayer player, boolean hasSlotClick, boolean hasClose) {
        ActiveGui gui = activeGuis.get(player.getUUID());
        if (gui != null) {
            gui.hasSlotClick = gui.hasSlotClick || hasSlotClick;
            gui.hasClose = gui.hasClose || hasClose;
        }
    }

    public static void handleClose(ServerPlayer player) {
        ActiveGui gui = activeGuis.remove(player.getUUID());
        if (gui != null) {
            player.closeContainer();
        }
    }
}

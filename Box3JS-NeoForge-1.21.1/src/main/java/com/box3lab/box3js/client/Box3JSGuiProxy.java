package com.box3lab.box3js.client;

import com.box3lab.box3js.Box3JSNetwork;
import com.box3lab.box3js.script.Box3ScriptUtils;
import com.box3lab.box3js.script.GameEventHandlerToken;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeObject;
import org.slf4j.Logger;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Client-side proxy returned to JS from gui.openGUI().
 * Stores callbacks locally and sends C→S packets for mutations.
 */
public class Box3JSGuiProxy {

    private static final Logger LOGGER = LogUtils.getLogger();

    private final List<Function> slotClickCallbacks = new CopyOnWriteArrayList<>();
    private final List<Function> closeCallbacks = new CopyOnWriteArrayList<>();
    private boolean slotClickRegistered;
    private boolean closeRegistered;

    // ---- Slot manipulation (C→S packets) ----

    public void setItem(int slot, String itemId) {
        setItem(slot, itemId, 1);
    }

    public void setItem(int slot, String itemId, int count) {
        PacketDistributor.sendToServer(
            new Box3JSNetwork.GUIServerboundPayload(1, "", 0, "", slot, itemId, Math.max(1, Math.min(count, 64)), false, false, "", false));
    }

    /** Overload with options: { count?, lore?: string[], enchanted?: boolean } */
    public void setItem(int slot, String itemId, NativeObject options) {
        int count = 1;
        String loreJson = "";
        boolean enchanted = false;
        if (options.containsKey("count")) {
            count = Math.max(1, Math.min(((Number) options.get("count")).intValue(), 64));
        }
        if (options.containsKey("lore")) {
            Object lore = options.get("lore");
            if (lore instanceof NativeArray arr) {
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < arr.getLength(); i++) {
                    if (i > 0) sb.append(",");
                    String line = arr.get(i).toString();
                    sb.append("\"").append(line.replace("\\", "\\\\").replace("\"", "\\\"")).append("\"");
                }
                sb.append("]");
                loreJson = sb.toString();
            }
        }
        if (options.containsKey("enchanted")) {
            enchanted = Box3ScriptUtils.coerceBool(options.get("enchanted"));
        }
        PacketDistributor.sendToServer(
            new Box3JSNetwork.GUIServerboundPayload(1, "", 0, "", slot, itemId, count, false, false, loreJson, enchanted));
    }

    public NativeObject getItem(int slot) {
        NativeObject result = new NativeObject();
        var mc = Minecraft.getInstance();
        if (mc.player == null || mc.player.containerMenu == null) {
            result.put("id", result, "minecraft:air");
            result.put("count", result, 0);
            return result;
        }
        if (slot < 0 || slot >= mc.player.containerMenu.slots.size()) {
            result.put("id", result, "minecraft:air");
            result.put("count", result, 0);
            return result;
        }
        ItemStack stack = mc.player.containerMenu.getSlot(slot).getItem();
        if (stack.isEmpty()) {
            result.put("id", result, "minecraft:air");
            result.put("count", result, 0);
        } else {
            var key = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem());
            result.put("id", result, key.toString());
            result.put("count", result, stack.getCount());
        }
        return result;
    }

    // ---- Callbacks ----

    public GameEventHandlerToken onSlotClick(Function callback) {
        slotClickCallbacks.add(callback);
        ensureCallbacksRegistered(true, false);
        return new GameEventHandlerToken(() -> slotClickCallbacks.remove(callback));
    }

    public GameEventHandlerToken onClose(Function callback) {
        closeCallbacks.add(callback);
        ensureCallbacksRegistered(false, true);
        return new GameEventHandlerToken(() -> closeCallbacks.remove(callback));
    }

    private void ensureCallbacksRegistered(boolean wantsSlotClick, boolean wantsClose) {
        boolean sendSlotClick = wantsSlotClick && !slotClickRegistered;
        boolean sendClose = wantsClose && !closeRegistered;
        if (!sendSlotClick && !sendClose) return;

        slotClickRegistered = slotClickRegistered || sendSlotClick;
        closeRegistered = closeRegistered || sendClose;
        PacketDistributor.sendToServer(
            new Box3JSNetwork.GUIServerboundPayload(2, "", 0, "", 0, "", 0,
                sendSlotClick, sendClose, "", false));
    }

    // ---- Close ----

    public void close() {
        Minecraft.getInstance().execute(() -> {
            if (Minecraft.getInstance().player != null) {
                Minecraft.getInstance().player.closeContainer();
            }
        });
        PacketDistributor.sendToServer(
            new Box3JSNetwork.GUIServerboundPayload(3, "", 0, "", 0, "", 0, false, false, "", false));
    }

    // ---- Internal: called from GUIClientboundPayload handler (on netty thread → render thread) ----

    public void fireSlotClick(int slot) {
        Minecraft.getInstance().execute(() -> {
            for (Function cb : slotClickCallbacks) {
                Context cx = Context.enter();
                try {
                    cb.call(cx, cb.getParentScope(), cb, new Object[] { slot });
                } catch (Exception e) {
                    LOGGER.error("onSlotClick callback error", e);
                } finally {
                    Context.exit();
                }
            }
        });
    }

    public void fireClose() {
        Minecraft.getInstance().execute(() -> {
            for (Function cb : closeCallbacks) {
                Context cx = Context.enter();
                try {
                    cb.call(cx, cb.getParentScope(), cb, new Object[] {});
                } catch (Exception e) {
                    LOGGER.error("onClose callback error", e);
                } finally {
                    Context.exit();
                }
            }
        });
    }
}

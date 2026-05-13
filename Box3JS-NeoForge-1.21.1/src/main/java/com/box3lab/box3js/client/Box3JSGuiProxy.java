package com.box3lab.box3js.client;

import com.box3lab.box3js.Box3JSNetwork;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
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
    private boolean callbacksRegistered;

    // ---- Slot manipulation (C→S packets) ----

    public void setItem(int slot, String itemId, int count) {
        PacketDistributor.sendToServer(
            new Box3JSNetwork.GUIServerboundPayload(1, "", 0, "", slot, itemId, Math.max(1, Math.min(count, 64)), false, false));
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

    public void onSlotClick(Function callback) {
        slotClickCallbacks.add(callback);
        ensureCallbacksRegistered();
    }

    public void onClose(Function callback) {
        closeCallbacks.add(callback);
        ensureCallbacksRegistered();
    }

    private void ensureCallbacksRegistered() {
        if (callbacksRegistered) return;
        callbacksRegistered = true;
        PacketDistributor.sendToServer(
            new Box3JSNetwork.GUIServerboundPayload(2, "", 0, "", 0, "", 0,
                !slotClickCallbacks.isEmpty(), !closeCallbacks.isEmpty()));
    }

    // ---- Close ----

    public void close() {
        Minecraft.getInstance().execute(() -> {
            if (Minecraft.getInstance().player != null) {
                Minecraft.getInstance().player.closeContainer();
            }
        });
        PacketDistributor.sendToServer(
            new Box3JSNetwork.GUIServerboundPayload(3, "", 0, "", 0, "", 0, false, false));
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

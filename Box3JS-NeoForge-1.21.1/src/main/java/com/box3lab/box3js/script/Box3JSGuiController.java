package com.box3lab.box3js.script;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.mozilla.javascript.NativeObject;

import java.util.function.Consumer;

/**
 * Server-side container manager for script GUIs. The menu delegates click/close
 * events here; the server handler wires these to S→C notification packets.
 */
public class Box3JSGuiController {

    private Box3JSScriptContainerMenu menu;
    private final ServerPlayer owner;
    private Consumer<Integer> slotClickNotifier;
    private Runnable closeNotifier;

    public Box3JSGuiController(Box3JSScriptContainerMenu menu, ServerPlayer owner) {
        this.menu = menu;
        this.owner = owner;
    }

    // ---- Slot manipulation ----

    void setMenu(Box3JSScriptContainerMenu menu) {
        this.menu = menu;
    }

    public void setItem(int slot, String itemId, int count) {
        if (menu == null) return;
        var item = Box3ScriptUtils.lookupItem(itemId);
        if (item == null) return;
        if (slot < 0 || slot >= menu.getContainer().getContainerSize()) return;
        menu.getContainer().setItem(slot, new ItemStack(item, Math.max(1, Math.min(count, 64))));
    }

    public NativeObject getItem(int slot) {
        NativeObject result = new NativeObject();
        if (menu == null || slot < 0 || slot >= menu.getContainer().getContainerSize()) {
            result.put("id", result, "minecraft:air");
            result.put("count", result, 0);
            return result;
        }
        ItemStack stack = menu.getContainer().getItem(slot);
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

    // ---- Event notifiers (set by server handler) ----

    public void onSlotClickEvent(Consumer<Integer> notifier) {
        this.slotClickNotifier = notifier;
    }

    public void onCloseEvent(Runnable notifier) {
        this.closeNotifier = notifier;
    }

    // ---- Close ----

    public void close() {
        owner.closeContainer();
    }

    // ---- Internal: called from Box3JSScriptContainerMenu ----

    boolean fireSlotClick(int slot) {
        if (slotClickNotifier != null) {
            slotClickNotifier.accept(slot);
        }
        return true; // always allow click; callback is client-side notification
    }

    void fireClose() {
        if (closeNotifier != null) {
            closeNotifier.run();
        }
    }
}

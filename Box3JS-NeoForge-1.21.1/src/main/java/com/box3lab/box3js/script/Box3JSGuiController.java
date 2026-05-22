package com.box3lab.box3js.script;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import org.mozilla.javascript.NativeObject;

import java.util.ArrayList;
import java.util.List;
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
        setItem(slot, itemId, count, "", false);
    }

    public void setItem(int slot, String itemId, int count, String loreJson, boolean enchanted) {
        if (menu == null) return;
        var item = Box3ScriptUtils.lookupItem(itemId);
        if (item == null) return;
        if (slot < 0 || slot >= menu.getContainer().getContainerSize()) return;
        ItemStack stack = new ItemStack(item, Math.max(1, Math.min(count, 64)));
        if (loreJson != null && !loreJson.isEmpty()) {
            List<Component> lines = new ArrayList<>();
            String inner = loreJson.trim();
            if (inner.startsWith("[") && inner.endsWith("]")) {
                inner = inner.substring(1, inner.length() - 1);
                for (String part : inner.split(",")) {
                    part = part.trim();
                    if (part.startsWith("\"") && part.endsWith("\"")) {
                        String line = part.substring(1, part.length() - 1)
                            .replace("\\\\", "\\").replace("\\\"", "\"");
                        lines.add(Component.literal(line));
                    }
                }
            }
            if (!lines.isEmpty()) {
                stack.set(DataComponents.LORE, new ItemLore(lines, lines));
            }
        }
        if (enchanted) {
            stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        }
        menu.getContainer().setItem(slot, stack);
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

package com.box3lab.box3js.script;

import com.box3lab.box3js.Box3JS;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.flag.FeatureFlags;
import net.neoforged.neoforge.network.IContainerFactory;

public class Box3JSScriptContainerMenu extends AbstractContainerMenu {

    public static final int SLOTS_PER_ROW = 9;

    private final SimpleContainer container;
    private final int rows;
    private Box3JSGuiController controller;

    public Box3JSScriptContainerMenu(MenuType<?> type, int containerId, Inventory playerInventory, int rows,
                                     Box3JSGuiController controller) {
        super(type, containerId);
        this.rows = rows;
        this.container = new SimpleContainer(rows * SLOTS_PER_ROW);
        this.controller = controller;

        // Container slots
        for (int i = 0; i < rows * SLOTS_PER_ROW; i++) {
            int col = i % SLOTS_PER_ROW;
            int row = i / SLOTS_PER_ROW;
            this.addSlot(new Slot(container, i, 8 + col * 18, 18 + row * 18));
        }

        // Player inventory
        addPlayerInventory(playerInventory, 8, 84 + (rows - 3) * 18);
    }

    public Box3JSGuiController getController() {
        return controller;
    }

    public void setController(Box3JSGuiController controller) {
        this.controller = controller;
    }

    public SimpleContainer getContainer() {
        return container;
    }

    public int getRows() {
        return rows;
    }

    @Override
    public void clicked(int slot, int button, ClickType clickType, Player player) {
        if (controller != null) {
            int scriptSlots = rows * SLOTS_PER_ROW;
            if (slot >= 0 && slot < scriptSlots) {
                controller.fireSlotClick(slot);
            }
        }
        super.clicked(slot, button, clickType, player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (controller != null) {
            controller.fireClose();
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        int scriptSlots = rows * SLOTS_PER_ROW;
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack stackInSlot = slot.getItem();
        ItemStack copy = stackInSlot.copy();

        if (index < scriptSlots) {
            // From script container → player inventory
            if (!this.moveItemStackTo(stackInSlot, scriptSlots, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            // From player inventory → script container
            if (!this.moveItemStackTo(stackInSlot, 0, scriptSlots, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (stackInSlot.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        return copy;
    }

    // ---- Factory for MenuType registration ----

    public static class Factory implements MenuType.MenuSupplier<Box3JSScriptContainerMenu>,
                                          IContainerFactory<Box3JSScriptContainerMenu> {
        @Override
        public Box3JSScriptContainerMenu create(int containerId, Inventory playerInventory) {
            // Default 3 rows — used as fallback if IContainerFactory path fails
            return new Box3JSScriptContainerMenu(
                Box3JS.SCRIPT_CONTAINER_MENU.get(), containerId, playerInventory, 3, null);
        }

        @Override
        public Box3JSScriptContainerMenu create(int containerId, Inventory playerInventory,
                                                 RegistryFriendlyByteBuf extraData) {
            int rows = extraData.readVarInt();
            return new Box3JSScriptContainerMenu(
                Box3JS.SCRIPT_CONTAINER_MENU.get(), containerId, playerInventory, rows, null);
        }
    }

    // ---- Player inventory helper ----

    private void addPlayerInventory(Inventory playerInventory, int x, int y) {
        // Hotbar (bottom row)
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, x + col * 18, y + 58));
        }
        // Main inventory (3 rows above hotbar)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, x + col * 18, y + row * 18));
            }
        }
    }
}

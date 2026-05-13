package com.box3lab.box3js.client.screen;

import com.box3lab.box3js.script.Box3JSScriptContainerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class Box3JSScriptContainerScreen extends AbstractContainerScreen<Box3JSScriptContainerMenu> {

    private static final ResourceLocation CONTAINER_BACKGROUND =
        ResourceLocation.fromNamespaceAndPath("minecraft", "textures/gui/container/generic_54.png");

    private final int rows;

    public Box3JSScriptContainerScreen(Box3JSScriptContainerMenu menu, Inventory playerInventory,
                                        Component title) {
        super(menu, playerInventory, title);
        this.rows = menu.getRows();
        this.imageHeight = 114 + this.rows * 18;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        // Draw chest background — only render the top part (dynamic rows)
        int containerRows = this.rows;
        int texHeight = 222; // Full 6-row texture height
        int renderHeight = 18 + containerRows * 18; // Top border + rows + gap above inventory

        // Top of the container (including top border)
        guiGraphics.blit(CONTAINER_BACKGROUND, x, y, 0, 0, this.imageWidth, 17);
        // Middle rows
        for (int row = 0; row < containerRows; row++) {
            guiGraphics.blit(CONTAINER_BACKGROUND, x, y + 17 + row * 18, 0, 17, this.imageWidth, 18);
        }
        // Gap + player inventory area
        int bottomStart = 17 + containerRows * 18;
        int bottomTexY = 215 - 96; // Transition area above player inventory in texture
        // Actually render the bottom inventory section from the 6-row texture
        int invSectionStart = 17 + containerRows * 18;
        // Copy the player inventory from the fixed texture
        for (int row = 0; row < 4; row++) {
            int srcY = 17 + 5 * 18 + row * 18; // Start from row 5 in the 6-row texture (skip rows)
            // Actually use the bottom inventory strip from the texture
            guiGraphics.blit(CONTAINER_BACKGROUND, x, y + invSectionStart + row * 18,
                0, 125 + row * 18, this.imageWidth, 18);
        }
        // Render the remaining lines below
        guiGraphics.blit(CONTAINER_BACKGROUND, x, y + invSectionStart + 4 * 18,
            0, 215 - 7, this.imageWidth, 7);
    }
}

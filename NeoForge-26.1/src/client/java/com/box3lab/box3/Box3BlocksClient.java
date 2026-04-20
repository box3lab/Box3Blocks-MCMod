package com.box3lab.box3;

import com.box3lab.box3.block.GlassVoxelBlock;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = Box3Blocks.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = Box3Blocks.MODID, value = Dist.CLIENT)
public class Box3BlocksClient {
    public Box3BlocksClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        Box3Blocks.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        event.enqueueWork(() -> Box3Blocks.REGISTERED_BLOCKS.entrySet().forEach(entry -> {
            String resourceName = entry.getKey();
            if (Box3Blocks.isTransparentBlock(resourceName) || entry.getValue().get() instanceof GlassVoxelBlock) {
                ItemBlockRenderTypes.setRenderLayer(entry.getValue().get(), ChunkSectionLayer.TRANSLUCENT);
            }
        }));
    }
}

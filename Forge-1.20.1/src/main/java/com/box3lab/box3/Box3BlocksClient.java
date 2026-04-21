package com.box3lab.box3;

import com.box3lab.box3.block.GlassVoxelBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = Box3Blocks.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class Box3BlocksClient {
    private Box3BlocksClient() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        Box3Blocks.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        event.enqueueWork(() -> Box3Blocks.REGISTERED_BLOCKS.forEach((name, registryObject) -> {
            if (Box3Blocks.isTransparentBlock(name) || registryObject.get() instanceof GlassVoxelBlock) {
                ItemBlockRenderTypes.setRenderLayer(registryObject.get(), RenderType.translucent());
            }
        }));
    }
}

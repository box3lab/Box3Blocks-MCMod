package com.box3lab.box3js;

import com.box3lab.box3js.standalone.Box3StandaloneBootstrap;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * Client-side handler that applies render types for blocks registered by
 * standalone script mods via
 * {@link Box3StandaloneBootstrap#addBlockRenderType}.
 *
 * <p>
 * The generated {@code @Mod} class cannot import client-only classes
 * ({@code ItemBlockRenderTypes}, {@code RenderType}) because the in-game
 * {@code javac} classpath is incomplete. This class lives in the main Box3JS
 * mod (already compiled) and drains the pending render-type registrations
 * during {@code FMLClientSetupEvent}.
 */
@EventBusSubscriber(modid = Box3JS.MODID, value = Dist.CLIENT)
public class Box3JSClientEvents {

    @SubscribeEvent
    @SuppressWarnings("deprecation")
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            var pending = Box3StandaloneBootstrap.drainPendingRenderTypes();
            if (pending.isEmpty()) return;

            for (var modEntry : pending.entrySet()) {
                for (var entry : modEntry.getValue()) {
                    Block block = entry.blockSupplier().get();
                    switch (entry.renderType()) {
                        case "cutout" -> ItemBlockRenderTypes.setRenderLayer(block, RenderType.cutout());
                        case "translucent" ->
                            ItemBlockRenderTypes.setRenderLayer(block, RenderType.translucent());
                    }
                }
            }

            Box3JS.LOGGER.info("Applied client render types for {} standalone mod(s)", pending.size());
        });
    }
}

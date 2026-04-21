package com.box3lab.box3;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * Forge 1.20.1 entry point for the Box3Blocks mod.
 */
@Mod(Box3Blocks.MODID)
public final class Box3Mod {
    @SuppressWarnings("removal")
    public Box3Mod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        Box3Blocks.init(modEventBus);
    }
}

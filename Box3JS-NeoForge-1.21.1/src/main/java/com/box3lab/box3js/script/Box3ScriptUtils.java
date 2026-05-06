package com.box3lab.box3js.script;

import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class Box3ScriptUtils {

    public static Item lookupItem(String id) {
        ResourceLocation rl = ResourceLocation.tryParse(id);
        if (rl == null) return null;
        return BuiltInRegistries.ITEM.getOptional(rl).orElse(null);
    }

    public static EntityType<?> lookupEntityType(String id) {
        ResourceLocation rl = ResourceLocation.tryParse(id);
        if (rl == null) return null;
        return BuiltInRegistries.ENTITY_TYPE.getOptional(rl).orElse(null);
    }

    public static Block lookupBlock(String id) {
        ResourceLocation rl = ResourceLocation.tryParse(id);
        if (rl == null) return null;
        return BuiltInRegistries.BLOCK.getOptional(rl).orElse(null);
    }

    public static Holder<MobEffect> lookupMobEffect(String id) {
        ResourceLocation rl = ResourceLocation.tryParse(id);
        if (rl == null) return null;
        return BuiltInRegistries.MOB_EFFECT.getHolder(rl).orElse(null);
    }

    public static Holder<SoundEvent> lookupSoundEvent(String id) {
        ResourceLocation rl = ResourceLocation.tryParse(id);
        if (rl == null) return null;
        return BuiltInRegistries.SOUND_EVENT.getHolder(rl).orElse(null);
    }

    public static ParticleOptions lookupParticle(String id) {
        ResourceLocation rl = ResourceLocation.tryParse(id);
        if (rl == null) return null;
        var type = BuiltInRegistries.PARTICLE_TYPE.getOptional(rl);
        if (type.isEmpty()) return null;
        try {
            return (ParticleOptions) type.get();
        } catch (ClassCastException ignored) {
            return null;
        }
    }

    public static Holder<Attribute> lookupAttribute(String id) {
        ResourceLocation rl = ResourceLocation.tryParse(id);
        if (rl == null) return null;
        return BuiltInRegistries.ATTRIBUTE.getHolder(rl).orElse(null);
    }

    public static boolean coerceBool(Object v) {
        return v instanceof Boolean b ? b : Boolean.parseBoolean(v.toString());
    }

    static String resolveScoreName(Object entityOrName) {
        if (entityOrName instanceof String s) return s;
        if (entityOrName instanceof Box3JSEntity e) return e.getEntity().getScoreboardName();
        if (entityOrName instanceof ServerPlayer sp) return sp.getScoreboardName();
        return null;
    }

    public static void lookAt(Entity entity, double x, double y, double z) {
        double dx = x - entity.getX();
        double dy = y - entity.getEyeY();
        double dz = z - entity.getZ();
        double hd = Math.sqrt(dx * dx + dz * dz);
        entity.setYRot((float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0));
        entity.setXRot((float) (-Math.toDegrees(Math.atan2(dy, hd))));
    }
}

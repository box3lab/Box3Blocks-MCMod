package com.box3lab.register.sound;

import java.util.Locale;

import net.minecraft.world.level.block.SoundType;

public final class CategorySoundTypes {
    private CategorySoundTypes() {
    }

    public static SoundType soundTypeForCategory(String category) {
        if (category == null) {
            return SoundType.STONE;
        }
        String c = category.toLowerCase(Locale.ROOT);

        return switch (c) {
            case "structure" -> SoundType.STONE;
            case "nature" -> SoundType.GRASS;

            case "symbol", "number", "letter", "color" -> SoundType.STONE;

            case "wood", "plant", "tree", "leaf", "leaves" -> SoundType.WOOD;
            case "metal", "machine" -> SoundType.METAL;
            case "glass" -> SoundType.GLASS;
            case "wool", "cloth" -> SoundType.WOOL;
            case "sand" -> SoundType.SAND;
            case "snow" -> SoundType.SNOW;
            case "slime" -> SoundType.SLIME_BLOCK;
            default -> SoundType.STONE;
        };
    }
}

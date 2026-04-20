package com.box3lab.box3.register;

import net.minecraft.world.level.block.SoundType;

import java.util.Locale;

public final class CategorySoundTypes {
    private CategorySoundTypes() {
    }

    public static SoundType soundTypeForCategory(String category) {
        if (category == null) {
            return SoundType.STONE;
        }
        String value = category.toLowerCase(Locale.ROOT);

        return switch (value) {
            case "structure" -> SoundType.STONE;
            case "nature", "element" -> SoundType.GRASS;
            case "symbol", "number", "letter", "color", "light" -> SoundType.STONE;
            case "wood", "plant", "tree", "leaf", "leaves" -> SoundType.WOOD;
            case "metal", "machine" -> SoundType.METAL;
            case "glass" -> SoundType.GLASS;
            case "wool", "cloth", "food" -> SoundType.WOOL;
            case "sand" -> SoundType.SAND;
            case "snow" -> SoundType.SNOW;
            case "slime" -> SoundType.SLIME_BLOCK;
            default -> SoundType.STONE;
        };
    }
}

package com.box3lab.box3.util;

import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static com.box3lab.box3.Box3Blocks.MODID;

public final class Box3ImportFiles {
    private Box3ImportFiles() {
    }

    public static Path getImportDir() {
        return FMLPaths.CONFIGDIR.get().resolve(MODID);
    }

    public static List<String> listJsonFiles() throws IOException {
        Path dir = getImportDir();
        if (!Files.exists(dir)) {
            return Collections.emptyList();
        }

        try (var stream = Files.list(dir)) {
            return stream
                    .filter(path -> path.getFileName().toString().endsWith(".gz"))
                    .map(path -> path.getFileName().toString())
                    .sorted()
                    .toList();
        }
    }
}

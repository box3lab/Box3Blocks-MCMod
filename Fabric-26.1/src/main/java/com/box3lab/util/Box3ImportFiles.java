package com.box3lab.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import net.fabricmc.loader.api.FabricLoader;

import static com.box3lab.Box3.MOD_ID;

public final class Box3ImportFiles {

    private Box3ImportFiles() {
    }

    public static Path getImportDir() {
        return FabricLoader.getInstance()
                .getConfigDir()
                .resolve(MOD_ID);
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

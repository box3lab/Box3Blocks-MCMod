package com.box3lab.box3js.script;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class Box3ScriptTemplate {

    private static final String[] FILES = {
        "gitignore.template",
        "package.json",
        "tsconfig.json",
        "src/app.ts",
        "types/globals.d.ts",
    };

    public static void copyTo(Path projectDir, String projectName) throws IOException {
        Files.createDirectories(projectDir);
        for (String relPath : FILES) {
            String destName = relPath.equals("gitignore.template") ? ".gitignore" : relPath;
            Path dest = projectDir.resolve(destName);
            Files.createDirectories(dest.getParent());
            String resourcePath = "/assets/box3js/template/" + relPath;
            try (InputStream in = Box3ScriptTemplate.class.getResourceAsStream(resourcePath)) {
                if (in == null) throw new IOException("Template file not found: " + resourcePath);
                Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
            }
            if (relPath.equals("src/app.ts")) {
                String content = Files.readString(dest);
                Files.writeString(dest, content.replace("PROJECT_NAME", projectName));
            }
        }
    }
}

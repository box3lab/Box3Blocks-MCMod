package com.box3lab.box3js.script;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.jar.JarFile;

public class Box3ScriptTemplate {

    private static final String RESOURCE_PREFIX = "/assets/box3js/template/";
    private static final String JAR_ENTRY_PREFIX = "assets/box3js/template/";

    public static void copyTo(Path projectDir, String projectName) throws IOException {
        Files.createDirectories(projectDir);
        URL known = Box3ScriptTemplate.class.getResource(RESOURCE_PREFIX + "package.json");
        if (known == null)
            throw new IOException("Template resource not found");

        if (known.getProtocol().equals("jar")) {
            copyFromJar(projectDir, projectName, known);
        } else {
            try {
                Path templateRoot = Path.of(known.toURI()).getParent();
                copyFromDir(projectDir, projectName, templateRoot);
            } catch (Exception e) {
                throw new IOException("Cannot resolve template directory from: " + known, e);
            }
        }
    }

    private static void copyFromDir(Path projectDir, String projectName, Path templateRoot) throws IOException {
        try (var stream = Files.walk(templateRoot)) {
            for (Path file : stream.filter(Files::isRegularFile).toList()) {
                Path rel = templateRoot.relativize(file);
                copyOne(projectDir, projectName, file, rel.toString());
            }
        }
    }

    private static void copyFromJar(Path projectDir, String projectName, URL known) throws IOException {
        String s = known.toString();
        int bang = s.lastIndexOf('!');
        if (bang < 0)
            throw new IOException("Malformed jar URL: " + s);
        String jarUri = s.substring(4, bang);
        Path jarPath;
        try {
            jarPath = Path.of(new URI(jarUri));
        } catch (Exception e) {
            throw new IOException("Cannot resolve jar path from: " + jarUri, e);
        }

        try (JarFile jar = new JarFile(jarPath.toFile())) {
            var entries = jar.entries();
            while (entries.hasMoreElements()) {
                var entry = entries.nextElement();
                String name = entry.getName();
                if (!name.startsWith(JAR_ENTRY_PREFIX) || name.endsWith("/"))
                    continue;
                String rel = name.substring(JAR_ENTRY_PREFIX.length());
                Path dest = destPath(projectDir, rel);
                Files.createDirectories(dest.getParent());
                try (InputStream in = jar.getInputStream(entry)) {
                    Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
                }
                replacePlaceholders(dest, projectName);
            }
        }
    }

    private static void copyOne(Path projectDir, String projectName, Path source, String relPath) throws IOException {
        Path dest = destPath(projectDir, relPath);
        Files.createDirectories(dest.getParent());
        Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING);
        replacePlaceholders(dest, projectName);
    }

    private static Path destPath(Path projectDir, String relPath) {
        String normalized = relPath.replace('\\', '/');
        if (normalized.equals("gitignore.template"))
            return projectDir.resolve(".gitignore");
        return projectDir.resolve(normalized);
    }

    private static void replacePlaceholders(Path dest, String projectName) throws IOException {
        String name = dest.getFileName().toString();
        if (name.endsWith(".json") || name.endsWith(".ts") || name.endsWith(".mjs")) {
            String content = Files.readString(dest);
            Files.writeString(dest, content.replace("PROJECT_NAME", projectName));
        }
    }
}

package com.box3lab.box3js.standalone;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.regex.Pattern;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

/**
 * Compiles a Box3JS TypeScript project into a lightweight NeoForge mod JAR.
 *
 * <p>
 * Script JARs (~50KB) contain only the generated {@code @Mod} entry point
 * and bundled JS source. They depend on the Box3JS mod ({@code box3js}) for
 * the Rhino runtime and API bindings — no classes are bundled.
 *
 * <h3>Script JAR structure</h3>
 *
 * <pre>{@code
 *   mygame.jar
 *   ├── META-INF/
 *   │   ├── MANIFEST.MF
 *   │   └── neoforge.mods.toml    ← depends on box3js
 *   ├── logo.png                   ← mod icon (if specified)
 *   └── box3script/mygame/
 *       ├── MygameMod.class        ← generated @Mod entry point
 *       ├── server.js              ← bundled server JS source
 *       └── client.js              ← bundled client JS source (if present)
 * }</pre>
 *
 * <h3>Deployment</h3>
 *
 * Place the script JAR alongside {@code box3js} (the main Box3JS mod) in
 * {@code mods/}. No other dependencies required.
 */
public class Box3ScriptCompiler {

    /** ModId of the main Box3JS mod that script JARs depend on. */
    public static final String BOX3JS_MOD_ID = "box3js";

    /** NeoForge modId regex: lowercase letter + 1-63 alphanumeric/underscores. */
    private static final Pattern MODID_PATTERN = Pattern.compile("^[a-z][a-z0-9_]{1,63}$");

    /**
     * Validates a modId against NeoForge naming rules.
     * @return null if valid, or an error message string if invalid
     */
    public static String validateModId(String modId) {
        if (modId == null || modId.isEmpty()) {
            return "modId cannot be empty";
        }
        if (!MODID_PATTERN.matcher(modId).matches()) {
            if (modId.length() < 2) {
                return "modId '" + modId + "' is too short (min 2 chars)";
            }
            if (modId.length() > 64) {
                return "modId '" + modId + "' is too long (max 64 chars)";
            }
            if (!Character.isLowerCase(modId.charAt(0))) {
                return "modId '" + modId + "' must start with a lowercase letter [a-z]";
            }
            return "modId '" + modId + "' contains invalid characters. "
                    + "Use only lowercase letters, digits, and underscores: [a-z][a-z0-9_]{1,63}";
        }
        return null;
    }

    private final Path projectDir;
    private final Path outputJar;
    private final String modId;
    private final String modName;
    private final String modVersion;
    private final String description;
    private final String author;
    private final String license;
    private final String homepage;
    private final String bugsUrl;
    private final String logoFile;
    private final String box3jsVersion;

    public Box3ScriptCompiler(Path projectDir, Path outputJar,
            String modId, String modName, String modVersion,
            String description, String author, String license,
            String homepage, String bugsUrl, String logoFile,
            String box3jsVersion) {
        this.projectDir = projectDir.toAbsolutePath();
        this.outputJar = outputJar.toAbsolutePath();
        this.modId = modId;
        this.modName = modName;
        this.modVersion = modVersion;
        this.description = description;
        this.author = author;
        this.license = license;
        this.homepage = homepage;
        this.bugsUrl = bugsUrl;
        this.logoFile = logoFile;
        this.box3jsVersion = box3jsVersion;
    }

    public void compile() throws Exception {
        String validationError = validateModId(modId);
        if (validationError != null) {
            throw new IllegalArgumentException("Invalid modId: " + validationError);
        }

        Path serverJs = projectDir.resolve("dist/server.js");
        if (!Files.exists(serverJs)) {
            throw new FileNotFoundException("dist/server.js not found in " + projectDir
                    + " — run 'npm run build' first");
        }

        Path workDir = projectDir.resolve("dist/.jar-build");
        deleteRecursive(workDir);
        Files.createDirectories(workDir);

        System.out.println("[1/4] Bundling JS source ...");
        bundleJsSource(serverJs, workDir);

        System.out.println("[2/4] Bundling assets ...");
        bundleLogo(workDir);
        // Generate sounds first as defaults, then bundle user assets
        // so user-provided files in assets/ override auto-generated ones
        var blocks = Box3JSRegistryGen.readBlocks(projectDir);
        var tabs = Box3JSRegistryGen.readCreativeTabs(projectDir);
        var items = Box3JSRegistryGen.readItems(projectDir);
        var sounds = Box3JSRegistryGen.readSounds(projectDir);
        generateSoundsFile(workDir, sounds);
        bundleAssets(workDir);

        System.out.println("[3/4] Generating & compiling @Mod entry point ...");
        Path genSrcDir = workDir.resolve("gen-src");
        generateModClass(genSrcDir, blocks, tabs, items, sounds);
        compileJava(genSrcDir, workDir);

        System.out.println("[4/4] Creating metadata + packaging ...");
        createMetadata(workDir);
        packageJar(workDir, outputJar);

        deleteRecursive(workDir);
        System.out.println("Done: " + outputJar);
    }

    // ── Step 1: Bundle JS source ──

    private void bundleJsSource(Path jsFile, Path workDir) throws IOException {
        String resourcePath = "box3script/" + modId + "/server.js";
        Path dest = workDir.resolve(resourcePath);
        Files.createDirectories(dest.getParent());
        Files.copy(jsFile, dest);
        System.out.println("  Bundled " + resourcePath);

        // Also bundle client script if present
        Path clientJs = projectDir.resolve("dist/client.js");
        if (Files.exists(clientJs)) {
            String clientResourcePath = "box3script/" + modId + "/client.js";
            Path clientDest = workDir.resolve(clientResourcePath);
            Files.copy(clientJs, clientDest);
            System.out.println("  Bundled " + clientResourcePath);
        }
    }

    // ── Step 2: Bundle logo ──

    private void bundleLogo(Path workDir) throws IOException {
        if (logoFile == null || logoFile.isEmpty())
            return;
        Path logoSrc = projectDir.resolve(logoFile);
        if (!Files.exists(logoSrc)) {
            System.out.println("  Logo not found: " + logoFile + " — skipping");
            return;
        }
        Path dest = workDir.resolve("logo.png");
        Files.copy(logoSrc, dest);
        System.out.println("  Bundled logo.png");
    }

    // ── Step 2b: Bundle assets (models/textures/blockstates) into JAR ──

    private void bundleAssets(Path workDir) throws IOException {
        Path src = projectDir.resolve("assets");
        if (!Files.isDirectory(src))
            return;

        Path dest = workDir.resolve("assets/" + modId);
        copyDir(src, dest);
        System.out.println("  Bundled assets/ → assets/" + modId);
    }

    private void generateSoundsFile(Path workDir,
            java.util.List<Box3JSRegistryGen.SoundDef> sounds) throws IOException {
        String soundsJson = Box3JSRegistryGen.generateSoundsJson(modId, sounds);
        if (soundsJson == null)
            return;
        Path assetsDir = workDir.resolve("assets/" + modId);
        Files.createDirectories(assetsDir);
        Files.writeString(assetsDir.resolve("sounds.json"), soundsJson);
        System.out.println("  Generated assets/" + modId + "/sounds.json");
    }

    // ── Step 3: Generate @Mod entry point ──

    private void generateModClass(Path genSrcDir,
            java.util.List<Box3JSRegistryGen.BlockDef> blocks,
            java.util.List<Box3JSRegistryGen.CreativeTabDef> tabs,
            java.util.List<Box3JSRegistryGen.ItemDef> items,
            java.util.List<Box3JSRegistryGen.SoundDef> sounds) throws IOException {
        String pkg = "box3script." + modId;
        String className = capitalize(modId) + "Mod";
        String resourcePath = "box3script/" + modId + "/server.js";

        boolean hasBlocks = !blocks.isEmpty();
        boolean hasTabs = !tabs.isEmpty();
        boolean hasItems = !items.isEmpty();
        boolean hasSounds = !sounds.isEmpty();
        boolean hasTools = items.stream().anyMatch(Box3JSRegistryGen.ItemDef::isTool);
        boolean hasArmor = items.stream().anyMatch(Box3JSRegistryGen.ItemDef::isArmor);

        // Generate registry Java code
        String[] registryCode = Box3JSRegistryGen.generateJavaCode(modId, blocks, tabs, items, sounds);
        String fieldDecls = registryCode[0];
        String constructorRegs = registryCode[1];
        String extraImports = Box3JSRegistryGen.generateImports(hasBlocks, hasTabs, hasItems, hasSounds, hasTools,
                hasArmor);

        // Client render type setup for cutout / translucent blocks
        String clientRenderCalls = Box3JSRegistryGen.generateClientRenderCalls(modId, blocks);

        // Generate supplier map builder methods
        StringBuilder mapMethods = new StringBuilder();
        if (hasBlocks) {
            mapMethods.append("""

                        private static java.util.Map<String, java.util.function.Supplier<Block>> buildBlockMap() {
                            java.util.Map<String, java.util.function.Supplier<Block>> map = new java.util.HashMap<>();
                    """);
            for (var b : blocks) {
                String field = b.id().toUpperCase();
                mapMethods.append("        map.put(\"").append(b.id())
                        .append("\", () -> ").append(field).append(".get());\n");
            }
            mapMethods.append("        return map;\n    }\n");

            mapMethods
                    .append("""

                                private static java.util.Map<String, java.util.function.Supplier<BlockItem>> buildBlockItemMap() {
                                    java.util.Map<String, java.util.function.Supplier<BlockItem>> map = new java.util.HashMap<>();
                            """);
            for (var b : blocks) {
                String field = b.id().toUpperCase();
                mapMethods.append("        map.put(\"").append(b.id())
                        .append("\", () -> ").append(field).append("_ITEM.get());\n");
            }
            mapMethods.append("        return map;\n    }\n");
        }

        if (hasItems) {
            mapMethods.append("""

                        private static java.util.Map<String, java.util.function.Supplier<Item>> buildItemMap() {
                            java.util.Map<String, java.util.function.Supplier<Item>> map = new java.util.HashMap<>();
                    """);
            for (var it : items) {
                String field = it.id().toUpperCase();
                mapMethods.append("        map.put(\"").append(it.id())
                        .append("\", () -> ").append(field).append(".get());\n");
            }
            mapMethods.append("        return map;\n    }\n");
        }

        if (hasSounds) {
            mapMethods
                    .append("""

                                private static java.util.Map<String, java.util.function.Supplier<SoundEvent>> buildSoundMap() {
                                    java.util.Map<String, java.util.function.Supplier<SoundEvent>> map = new java.util.HashMap<>();
                            """);
            for (var s : sounds) {
                String field = s.id().toUpperCase();
                mapMethods.append("        map.put(\"").append(s.id())
                        .append("\", () -> ").append(field).append(".get());\n");
            }
            mapMethods.append("        return map;\n    }\n");
        }

        // Build super() arguments
        StringBuilder superArgs = new StringBuilder();
        superArgs.append(", ").append(hasBlocks ? "buildBlockMap()" : "null");
        superArgs.append(", ").append(hasBlocks ? "buildBlockItemMap()" : "null");
        superArgs.append(", ").append(hasItems ? "buildItemMap()" : "null");
        superArgs.append(", ").append(hasSounds ? "buildSoundMap()" : "null");

        var hardcodedImports = new StringBuilder();
        if (hasItems)
            hardcodedImports.append("import net.minecraft.world.item.Item;\n");
        if (hasSounds)
            hardcodedImports.append("import net.minecraft.sounds.SoundEvent;\n");

        String src = String.format("""
                package %s;

                import com.box3lab.box3js.standalone.Box3StandaloneBootstrap;
                import net.neoforged.bus.api.IEventBus;
                import net.neoforged.fml.ModContainer;
                import net.neoforged.fml.common.Mod;
                import net.minecraft.world.level.block.Block;
                import net.minecraft.world.item.BlockItem;
                %s
                %s
                @Mod("%s")
                public class %s extends Box3StandaloneBootstrap {
                    %s
                    %s
                    public %s(IEventBus modEventBus, ModContainer modContainer) {
                        super(modEventBus, modContainer, "%s", "%s"%s);
                        %s
                %s    }
                }
                """, pkg, hardcodedImports, extraImports, modId, className,
                fieldDecls, mapMethods,
                className, resourcePath, modId, superArgs.toString(), constructorRegs,
                clientRenderCalls);

        Path out = genSrcDir.resolve(pkg.replace('.', '/')).resolve(className + ".java");
        Files.createDirectories(out.getParent());
        Files.writeString(out, src);
    }

    private void compileJava(Path genSrcDir, Path classesDir) throws Exception {
        LinkedHashSet<Path> cpEntries = new LinkedHashSet<>();

        collectFromClassLoader(cpEntries, getClass().getClassLoader());
        collectFromJavaPathProperty(cpEntries, "java.class.path", File.pathSeparator);
        collectFromJavaPathProperty(cpEntries, "jdk.module.path", File.pathSeparator);
        collectFromLegacyClasspathFile(cpEntries);
        collectFromCodeSource(cpEntries, Box3ScriptCompiler.class);
        collectFromCodeSource(cpEntries, Box3StandaloneBootstrap.class);
        collectFromRuntimeAnchors(cpEntries);

        if (cpEntries.isEmpty()) {
            throw new RuntimeException(
                    "No classpath entries resolved for generated @Mod compilation. "
                            + "Expected java.class.path or legacyClassPath.file to be available.");
        }

        List<File> sourceFiles = new ArrayList<>();
        try (var stream = Files.walk(genSrcDir)) {
            stream.filter(p -> p.toString().endsWith(".java"))
                    .forEach(p -> sourceFiles.add(p.toFile()));
        }

        JavaCompiler jc = ToolProvider.getSystemJavaCompiler();
        if (jc == null) {
            throw new RuntimeException("javax.tools.JavaCompiler not available — run with JDK (not JRE)");
        }

        try (StandardJavaFileManager fm = jc.getStandardFileManager(null, null, null)) {
            Iterable<? extends JavaFileObject> units = fm.getJavaFileObjectsFromFiles(sourceFiles);

            List<String> options = new ArrayList<>();
            options.add("-d");
            options.add(classesDir.toString());
            options.add("-proc:none");
            options.add("-classpath");
            options.add(String.join(File.pathSeparator,
                    cpEntries.stream().map(Path::toString).toList()));

            JavaCompiler.CompilationTask task = jc.getTask(
                    null, fm, null, options, null, units);
            if (!task.call()) {
                throw new RuntimeException("Java compilation failed for generated @Mod class");
            }
        }
        System.out.println("  Compiled " + sourceFiles.size() + " generated source(s)");
    }

    private static void collectFromClassLoader(Set<Path> out, ClassLoader cl) {
        if (!(cl instanceof URLClassLoader ucl)) {
            return;
        }
        for (URL url : ucl.getURLs()) {
            addPathFromLocation(out, url.toString());
        }
    }

    private static void collectFromJavaPathProperty(Set<Path> out, String prop, String separator) {
        String raw = System.getProperty(prop);
        if (raw == null || raw.isBlank()) {
            return;
        }
        for (String entry : raw.split(Pattern.quote(separator))) {
            if (entry == null || entry.isBlank()) {
                continue;
            }
            try {
                Path p = Path.of(entry.trim());
                if (Files.exists(p)) {
                    out.add(p);
                }
            } catch (Exception ignored) {
            }
        }
    }

    private static void collectFromLegacyClasspathFile(Set<Path> out) {
        String legacyFile = System.getProperty("legacyClassPath.file");
        if (legacyFile == null || legacyFile.isBlank()) {
            return;
        }
        Path file = Path.of(legacyFile.trim());
        if (!Files.isRegularFile(file)) {
            return;
        }

        try {
            String raw = Files.readString(file, StandardCharsets.UTF_8);
            for (String token : raw.split("\\s+")) {
                if (token == null || token.isBlank()) {
                    continue;
                }
                try {
                    Path p = Path.of(token.trim());
                    if (Files.exists(p)) {
                        out.add(p);
                    }
                } catch (Exception ignored) {
                }
            }
        } catch (IOException ignored) {
        }
    }

    private static void collectFromCodeSource(Set<Path> out, Class<?> type) {
        try {
            CodeSource cs = type.getProtectionDomain().getCodeSource();
            if (cs == null || cs.getLocation() == null) {
                return;
            }
            addPathFromLocation(out, cs.getLocation().toString());

            URL classRes = type.getResource(type.getSimpleName() + ".class");
            if (classRes != null) {
                addPathFromLocation(out, classRes.toString());
            }
        } catch (Exception ignored) {
        }
    }

    private static void addPathFromLocation(Set<Path> out, String location) {
        if (location == null || location.isBlank()) {
            return;
        }
        Path p = pathFromLocation(location);
        if (p != null && Files.exists(p)) {
            out.add(p);
        }
    }

    private static Path pathFromLocation(String location) {
        String s = location.trim();
        try {
            if (s.startsWith("jar:")) {
                s = s.substring("jar:".length());
            }
            if (s.startsWith("union:")) {
                s = s.substring("union:".length());
            }

            int bang = s.indexOf("!/");
            if (bang >= 0) {
                s = s.substring(0, bang);
            }

            int marker = s.indexOf("%23");
            if (marker >= 0) {
                s = s.substring(0, marker);
            }

            s = URLDecoder.decode(s, StandardCharsets.UTF_8);

            if (s.startsWith("file:")) {
                return Path.of(URI.create(s));
            }
            if (s.startsWith("/")) {
                return Path.of(s);
            }

            URI uri = URI.create(s);
            if ("file".equalsIgnoreCase(uri.getScheme())) {
                return Path.of(uri);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static void collectFromRuntimeAnchors(Set<Path> out) {
        String[] anchors = new String[] {
                "net.minecraft.world.level.block.Block",
                "net.minecraft.world.item.Item",
                "net.neoforged.fml.common.Mod",
                "net.neoforged.neoforge.registries.DeferredRegister",
                "net.neoforged.bus.api.IEventBus"
        };
        for (String name : anchors) {
            try {
                Class<?> c = Class.forName(name);
                collectFromCodeSource(out, c);
            } catch (Throwable ignored) {
            }
        }
    }

    // ── Step 4: Metadata + packaging ──

    private void createMetadata(Path workDir) throws IOException {
        String desc = (description != null && !description.isEmpty())
                ? description
                : "Box3JS standalone script mod";

        // neoforge.mods.toml
        Path toml = workDir.resolve("META-INF/neoforge.mods.toml");
        Files.createDirectories(toml.getParent());

        StringBuilder sb = new StringBuilder();
        sb.append("""
                modLoader = "javafml"
                loaderVersion = "[1,)"
                license = "%s"

                [[mods]]
                modId = "%s"
                version = "%s"
                displayName = "%s"
                description = "%s"
                """.formatted(license, modId, modVersion, modName, desc));

        if (author != null && !author.isEmpty())
            sb.append("credits = \"%s\"\n".formatted(author));
        if (homepage != null && !homepage.isEmpty())
            sb.append("displayURL = \"%s\"\n".formatted(homepage));
        if (bugsUrl != null && !bugsUrl.isEmpty())
            sb.append("issueTrackerURL = \"%s\"\n".formatted(bugsUrl));
        if (logoFile != null && !logoFile.isEmpty())
            sb.append("logoFile = \"logo.png\"\n");

        sb.append("""

                [[dependencies.%s]]
                modId = "neoforge"
                type = "required"
                versionRange = "[21.1,)"
                ordering = "NONE"
                side = "BOTH"

                [[dependencies.%s]]
                modId = "%s"
                type = "required"
                versionRange = "[%s,)"
                ordering = "NONE"
                side = "BOTH"
                """.formatted(modId, modId, BOX3JS_MOD_ID, box3jsVersion));

        Files.writeString(toml, sb.toString());
    }

    private void packageJar(Path workDir, Path outputJar) throws IOException {
        Files.createDirectories(outputJar.getParent());
        try (JarOutputStream jos = new JarOutputStream(
                new BufferedOutputStream(Files.newOutputStream(outputJar)),
                new Manifest())) {

            // META-INF/ first
            Path metaDir = workDir.resolve("META-INF");
            if (Files.isDirectory(metaDir)) {
                addDirToJar(jos, workDir, metaDir);
            }

            // logo.png at JAR root
            Path logo = workDir.resolve("logo.png");
            if (Files.exists(logo)) {
                jos.putNextEntry(new JarEntry("logo.png"));
                Files.copy(logo, jos);
                jos.closeEntry();
            }

            // Generated class + JS source
            Path box3scriptDir = workDir.resolve("box3script");
            if (Files.isDirectory(box3scriptDir)) {
                addDirToJar(jos, workDir, box3scriptDir);
            }

            // Assets (models, textures, blockstates)
            Path assetsDir = workDir.resolve("assets");
            if (Files.isDirectory(assetsDir)) {
                addDirToJar(jos, workDir, assetsDir);
            }
        }
    }

    private void addDirToJar(JarOutputStream jos, Path root, Path dir) throws IOException {
        try (var stream = Files.walk(dir)) {
            stream.filter(Files::isRegularFile)
                    .filter(f -> !f.getFileName().toString().equals(".DS_Store"))
                    .forEach(file -> {
                        try {
                            String entryName = root.relativize(file).toString().replace('\\', '/');
                            jos.putNextEntry(new JarEntry(entryName));
                            Files.copy(file, jos);
                            jos.closeEntry();
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }
    }

    // ── Helpers ──

    private static String capitalize(String s) {
        if (s.isEmpty())
            return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private static void copyDir(Path src, Path dest) throws IOException {
        try (var stream = Files.walk(src)) {
            stream.forEach(source -> {
                try {
                    Path target = dest.resolve(src.relativize(source));
                    if (Files.isDirectory(source)) {
                        Files.createDirectories(target);
                    } else {
                        Files.createDirectories(target.getParent());
                        if (!source.getFileName().toString().equals(".DS_Store"))
                            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }
    }

    private static void deleteRecursive(Path path) throws IOException {
        if (!Files.exists(path))
            return;
        try (var stream = Files.walk(path)) {
            stream.sorted(Comparator.reverseOrder()).forEach(p -> {
                try {
                    Files.delete(p);
                } catch (IOException ignored) {
                }
            });
        }
    }

    // ── Package.json ──

    private static final Pattern JSON_STRING = Pattern.compile("\"(\\w+)\"\\s*:\\s*\"([^\"]+)\"");

    private static final Pattern BUGS_URL = Pattern.compile("\"bugs\"\\s*:\\s*\\{[^}]*\"url\"\\s*:\\s*\"([^\"]+)\"");

    /**
     * Reads name, displayName, version, description, author, license, homepage,
     * bugsUrl from package.json.
     * Falls back to defaults if the file is absent.
     */
    public static String[] readPackageInfo(Path projectDir) {
        Path pkgJson = projectDir.resolve("package.json");
        String name = projectDir.getFileName().toString();
        String displayName = name;
        String version = "1.0.0";
        String description = "";
        String author = "";
        String license = "All Rights Reserved";
        String homepage = "";
        String bugsUrl = "";
        String logoFile = "";
        if (Files.exists(pkgJson)) {
            try {
                String raw = Files.readString(pkgJson, StandardCharsets.UTF_8);
                var m = JSON_STRING.matcher(raw);
                while (m.find()) {
                    switch (m.group(1)) {
                        case "name" -> name = m.group(2);
                        case "displayName" -> displayName = m.group(2);
                        case "version" -> version = m.group(2);
                        case "description" -> description = m.group(2);
                        case "author" -> author = m.group(2);
                        case "license" -> license = m.group(2);
                        case "homepage" -> homepage = m.group(2);
                        case "logoFile" -> logoFile = m.group(2);
                    }
                }
                var bm = BUGS_URL.matcher(raw);
                if (bm.find()) {
                    bugsUrl = bm.group(1);
                }
            } catch (IOException ignored) {
            }
        }
        return new String[] { name, displayName, version, description, author, license, homepage, bugsUrl, logoFile };
    }

    // ── CLI ──

    public static void main(String[] args) throws Exception {
        Map<String, String> opts = parseArgs(args);
        Path projectDir = Path.of(opts.getOrDefault("project", ".")).toAbsolutePath();

        String[] info = readPackageInfo(projectDir);
        String name = info[0];
        String displayName = info[1];
        String version = info[2];
        String description = info[3];
        String author = info[4];
        String license = info[5];
        String homepage = info[6];
        String bugsUrl = info[7];
        String logoFile = info[8];

        String modId = opts.getOrDefault("modId", name);
        String validationError = validateModId(modId);
        if (validationError != null) {
            System.err.println("Error: " + validationError);
            System.err.println("NeoForge modId must match: ^[a-z][a-z0-9_]{1,63}$");
            System.err.println("  - Must start with a lowercase letter");
            System.err.println("  - Use only lowercase letters, digits, underscores");
            System.err.println("  - Length: 2-64 characters");
            System.err.println("  - Fix: rename your project or use --modId <valid_id>");
            System.exit(1);
        }
        String modName = opts.getOrDefault("name", displayName);
        String modVersion = opts.getOrDefault("version", version);
        Path output = Path.of(opts.getOrDefault("output",
                projectDir.resolve("dist/" + modId + "-" + modVersion + ".jar").toString()));

        String box3jsVersion = opts.getOrDefault("box3jsVersion", "0");
        new Box3ScriptCompiler(projectDir, output, modId, modName, modVersion,
                description, author, license, homepage, bugsUrl, logoFile, box3jsVersion).compile();
    }

    private static Map<String, String> parseArgs(String[] args) {
        Map<String, String> opts = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("--")) {
                String key = args[i].substring(2);
                if (i + 1 < args.length && !args[i + 1].startsWith("--")) {
                    opts.put(key, args[++i]);
                }
            }
        }
        return opts;
    }
}

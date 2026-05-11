package com.box3lab.box3js.standalone;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.regex.Pattern;

/**
 * Compiles a Box3JS TypeScript project into a lightweight NeoForge mod JAR.
 *
 * <p>Script JARs (~50KB) contain only the generated {@code @Mod} entry point
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

        System.out.println("[2/4] Bundling logo ...");
        bundleLogo(workDir);

        System.out.println("[3/4] Generating & compiling @Mod entry point ...");
        Path genSrcDir = workDir.resolve("gen-src");
        generateModClass(genSrcDir);
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

    // ── Step 3: Generate @Mod entry point ──

    private void generateModClass(Path genSrcDir) throws IOException {
        String pkg = "box3script." + modId;
        String className = capitalize(modId) + "Mod";
        String resourcePath = "box3script/" + modId + "/server.js";

        String src = String.format("""
                package %s;

                import com.box3lab.box3js.standalone.Box3StandaloneBootstrap;
                import net.neoforged.bus.api.IEventBus;
                import net.neoforged.fml.ModContainer;
                import net.neoforged.fml.common.Mod;

                @Mod("%s")
                public class %s extends Box3StandaloneBootstrap {
                    public %s(IEventBus modEventBus, ModContainer modContainer) {
                        super(modEventBus, modContainer, "%s", "%s");
                    }
                }
                """, pkg, modId, className, className, resourcePath, modId);

        Path out = genSrcDir.resolve(pkg.replace('.', '/')).resolve(className + ".java");
        Files.createDirectories(out.getParent());
        Files.writeString(out, src);
    }

    private void compileJava(Path genSrcDir, Path classesDir) throws Exception {
        List<Path> cpEntries = new ArrayList<>();
        ClassLoader cl = getClass().getClassLoader();
        if (cl instanceof URLClassLoader ucl) {
            for (URL url : ucl.getURLs()) {
                try {
                    cpEntries.add(Path.of(url.toURI()));
                } catch (Exception ignored) {
                }
            }
        }
        if (cpEntries.isEmpty()) {
            String cp = System.getProperty("java.class.path");
            for (String entry : cp.split(File.pathSeparator)) {
                Path p = Path.of(entry);
                if (Files.exists(p))
                    cpEntries.add(p);
            }
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
            if (!cpEntries.isEmpty()) {
                options.add("-classpath");
                options.add(String.join(File.pathSeparator,
                        cpEntries.stream().map(Path::toString).toList()));
            }

            JavaCompiler.CompilationTask task = jc.getTask(
                    null, fm, null, options, null, units);
            if (!task.call()) {
                throw new RuntimeException("Java compilation failed for generated @Mod class");
            }
        }
        System.out.println("  Compiled " + sourceFiles.size() + " generated source(s)");
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
        }
    }

    private void addDirToJar(JarOutputStream jos, Path root, Path dir) throws IOException {
        try (var stream = Files.walk(dir)) {
            stream.filter(Files::isRegularFile).forEach(file -> {
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

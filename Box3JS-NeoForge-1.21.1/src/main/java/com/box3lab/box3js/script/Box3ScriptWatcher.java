package com.box3lab.box3js.script;

import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.box3lab.box3js.Box3JS;

import net.minecraft.server.MinecraftServer;

class Box3ScriptWatcher {

    private static final long DEBOUNCE_MS = 2000;

    private final MinecraftServer server;
    private final Box3ScriptEngine engine;
    private final Box3ScriptConfig config;
    private WatchService watchService;
    private ScheduledExecutorService scheduler;
    private Thread pollThread;
    private final Map<String, ScheduledFuture<?>> pending = new ConcurrentHashMap<>();
    private final Set<Path> watchedDirs = ConcurrentHashMap.newKeySet();
    private Path scriptRoot;
    private volatile boolean running;

    Box3ScriptWatcher(MinecraftServer server) {
        this.server = server;
        this.engine = Box3ScriptEngine.get();
        this.config = Box3ScriptConfig.get();
    }

    boolean isRunning() {
        return running;
    }

    void start() {
        if (running)
            return;
        try {
            watchService = FileSystems.getDefault().newWatchService();
            Path scriptDir = config.getScriptDir(server);
            if (!Files.exists(scriptDir)) {
                Files.createDirectories(scriptDir);
            }
            scriptRoot = scriptDir.toAbsolutePath().normalize();
            registerProjectDirs(scriptRoot);
            scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "Box3Script-Watcher");
                t.setDaemon(true);
                return t;
            });
            running = true;
            pollThread = new Thread(this::pollLoop, "Box3Script-Watcher-Poll");
            pollThread.setDaemon(true);
            pollThread.start();
            Box3JS.LOGGER.info("File watcher started (dist/ only) on {}", scriptDir);
        } catch (IOException e) {
            running = false;
            closeWatchService();
            Box3JS.LOGGER.error("Failed to start watcher", e);
        }
    }

    void stop() {
        running = false;
        closeWatchService();
        pending.values().forEach(future -> future.cancel(false));
        pending.clear();
        watchedDirs.clear();
        scriptRoot = null;
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }
        if (pollThread != null) {
            pollThread.interrupt();
            pollThread = null;
        }
        Box3JS.LOGGER.info("File watcher stopped");
    }

    private void closeWatchService() {
        if (watchService != null) {
            try {
                watchService.close();
            } catch (IOException e) {
                Box3JS.LOGGER.warn("Failed to close script watch service", e);
            }
            watchService = null;
        }
    }

    private void registerProjectDirs(Path scriptDir) throws IOException {
        registerDirectory(scriptDir);
        if (!Files.isDirectory(scriptDir))
            return;
        try (var dirs = Files.list(scriptDir)) {
            dirs.filter(Files::isDirectory).forEach(projectDir -> {
                try {
                    registerProjectDir(projectDir);
                } catch (IOException e) {
                    Box3JS.LOGGER.error("Failed to register project watch: {}", projectDir, e);
                }
            });
        }
    }

    private void registerProjectDir(Path projectDir) throws IOException {
        registerDirectory(projectDir);
        registerDistDir(projectDir.resolve("dist"));
    }

    private void registerDistDir(Path distDir) throws IOException {
        if (Files.isDirectory(distDir)) {
            registerDirectory(distDir);
        }
    }

    private void registerDirectory(Path dir) throws IOException {
        Path normalized = dir.toAbsolutePath().normalize();
        if (!Files.isDirectory(normalized) || !watchedDirs.add(normalized)) {
            return;
        }
        normalized.register(watchService, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);
        Box3JS.LOGGER.info("Watching: {}", normalized);
    }

    private void pollLoop() {
        while (running) {
            WatchKey key;
            try {
                key = watchService.poll(1, TimeUnit.SECONDS);
            } catch (InterruptedException | ClosedWatchServiceException e) {
                break;
            }
            if (key == null)
                continue;

            Path dir = ((Path) key.watchable()).toAbsolutePath().normalize();
            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();
                if (kind == OVERFLOW)
                    continue;

                Path changed = (Path) event.context();
                if (scriptRoot != null && dir.equals(scriptRoot)) {
                    if (kind != ENTRY_DELETE) {
                        Path projectDir = scriptRoot.resolve(changed);
                        if (Files.isDirectory(projectDir)) {
                            try {
                                registerProjectDir(projectDir);
                            } catch (IOException e) {
                                Box3JS.LOGGER.warn("Failed to watch new project directory: {}", projectDir, e);
                            }
                        }
                    }
                    continue;
                }

                if (scriptRoot != null && dir.getParent() != null && dir.getParent().equals(scriptRoot)) {
                    if (kind != ENTRY_DELETE && "dist".equals(changed.toString())) {
                        Path distDir = dir.resolve("dist");
                        try {
                            registerDistDir(distDir);
                        } catch (IOException e) {
                            Box3JS.LOGGER.warn("Failed to watch dist directory: {}", distDir, e);
                        }
                    }
                    continue;
                }

                if (!"dist".equals(dir.getFileName().toString()) || dir.getParent() == null) {
                    continue;
                }

                String project = dir.getParent().getFileName().toString();
                String fileName = ((Path) event.context()).toString();
                // Only react to JS output files in dist/
                if (!fileName.endsWith(".js"))
                    continue;

                if (kind == ENTRY_DELETE && fileName.equals("server.js")) {
                    // dist/server.js deleted — allow rebuild to recreate it
                    // debounce will pick up the next CREATE/MODIFY
                }
                debounceReload(project);
            }
            boolean valid = key.reset();
            if (!valid) {
                watchedDirs.remove(dir);
                if (dir.getParent() != null && "dist".equals(dir.getFileName().toString())) {
                    String project = dir.getParent().getFileName().toString();
                    Box3JS.LOGGER.warn("Watch key invalid for {}/dist, attempting re-register", project);
                    retryRegister(project);
                }
            }
        }
    }

    private void retryRegister(String project) {
        try {
            Path distDir = config.getScriptDir(server).resolve(project).resolve("dist");
            Thread.sleep(2000); // wait for rebuild tool to recreate
            registerDistDir(distDir);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            Box3JS.LOGGER.warn("Failed to re-register watch for project '{}'", project, e);
        }
    }

    private void debounceReload(String project) {
        if (!config.isEnabled(project))
            return;
        synchronized (pending) {
            ScheduledExecutorService activeScheduler = scheduler;
            if (!running || activeScheduler == null || activeScheduler.isShutdown())
                return;
            ScheduledFuture<?> existing = pending.remove(project);
            if (existing != null)
                existing.cancel(false);
            pending.put(project, activeScheduler.schedule(() -> {
                pending.remove(project);
                reloadProject(project);
            }, DEBOUNCE_MS, TimeUnit.MILLISECONDS));
        }
    }

    private void reloadProject(String project) {
        server.execute(() -> {
            if (!running || !config.isEnabled(project))
                return;
            if (engine.shouldPreferJarRuntime(project)) {
                Box3JS.LOGGER.info("Watcher skipped filesystem reload for '{}': script JAR has priority", project);
                return;
            }
            try {
                engine.setCurrentProject(project);
                try {
                    engine.removeProject(project);
                    engine.eval("require('./server')");
                    Box3JS.LOGGER.info("Watcher reloaded: {}", project);
                } finally {
                    engine.setCurrentProject(null);
                }
            } catch (Exception e) {
                Box3JS.LOGGER.error("Watcher reload failed for {}", project, e);
            }
        });
    }
}

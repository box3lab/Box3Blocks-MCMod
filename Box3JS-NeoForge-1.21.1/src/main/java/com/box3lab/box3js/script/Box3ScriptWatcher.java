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
    private final Map<String, ScheduledFuture<?>> pending = new ConcurrentHashMap<>();
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
            registerDistDirs(scriptDir);
            scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "Box3Script-Watcher");
                t.setDaemon(true);
                return t;
            });
            running = true;
            new Thread(this::pollLoop, "Box3Script-Watcher-Poll").start();
            Box3JS.LOGGER.info("File watcher started (dist/ only) on {}", scriptDir);
        } catch (IOException e) {
            Box3JS.LOGGER.error("Failed to start watcher", e);
        }
    }

    void stop() {
        running = false;
        if (watchService != null) {
            try {
                watchService.close();
            } catch (IOException ignored) {
            }
            watchService = null;
        }
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }
        pending.clear();
        Box3JS.LOGGER.info("File watcher stopped");
    }

    /** Register only dist/ directories under each project. */
    private void registerDistDirs(Path scriptDir) throws IOException {
        if (!Files.isDirectory(scriptDir))
            return;
        try (var dirs = Files.list(scriptDir)) {
            dirs.filter(Files::isDirectory).forEach(projectDir -> {
                Path distDir = projectDir.resolve("dist");
                if (Files.isDirectory(distDir)) {
                    try {
                        distDir.register(watchService, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);
                        Box3JS.LOGGER.info("Watching: {}", distDir);
                    } catch (IOException e) {
                        Box3JS.LOGGER.error("Failed to register watch: {}", distDir, e);
                    }
                }
            });
        }
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

            Path dir = (Path) key.watchable();
            String project = dir.getParent().getFileName().toString();
            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();
                if (kind == OVERFLOW)
                    continue;
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
                // dist/ was deleted — try re-registering
                Box3JS.LOGGER.warn("Watch key invalid for {}/dist, attempting re-register", project);
                retryRegister(project);
            }
        }
    }

    private void retryRegister(String project) {
        try {
            Path distDir = config.getScriptDir(server).resolve(project).resolve("dist");
            Thread.sleep(2000); // wait for rebuild tool to recreate
            if (Files.isDirectory(distDir)) {
                distDir.register(watchService, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);
                Box3JS.LOGGER.info("Re-registered watch: {}", distDir);
            }
        } catch (IOException | InterruptedException ignored) {
        }
    }

    private void debounceReload(String project) {
        if (!config.isEnabled(project))
            return;
        synchronized (pending) {
            ScheduledFuture<?> existing = pending.remove(project);
            if (existing != null)
                existing.cancel(false);
            pending.put(project, scheduler.schedule(() -> {
                pending.remove(project);
                reloadProject(project);
            }, DEBOUNCE_MS, TimeUnit.MILLISECONDS));
        }
    }

    private void reloadProject(String project) {
        server.execute(() -> {
            if (!running || !config.isEnabled(project))
                return;
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

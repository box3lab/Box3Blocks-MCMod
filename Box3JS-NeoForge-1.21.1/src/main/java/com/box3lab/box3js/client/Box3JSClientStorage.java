package com.box3lab.box3js.client;

import com.box3lab.box3js.script.Box3StorageTypes;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client-side persistent key-value storage with ValueEntry/ReturnValue/QueryList support.
 *
 * <p>Data is stored per-project under {@code <gameDir>/box3/client-storage/}.
 */
public class Box3JSClientStorage {

    private static final Gson GSON = new Gson();
    private static final Type MAP_TYPE = new TypeToken<Map<String, Box3StorageTypes.ValueEntry>>() {}.getType();

    private final Path baseDir;
    private final String projectName;
    private final Map<Path, Map<String, Box3StorageTypes.ValueEntry>> cache = new ConcurrentHashMap<>();

    public Box3JSClientStorage(java.io.File gameDir, String projectName) {
        this.baseDir = gameDir.toPath().resolve("box3").resolve("client-storage");
        this.projectName = projectName;
        try { Files.createDirectories(baseDir); } catch (IOException ignored) {}
    }

    public String getKey() { return ""; }

    public GameDataStorage getDataStorage(String name) {
        return new GameDataStorage(resolveName(name));
    }

    private String resolveName(String name) {
        return projectName != null ? projectName + "/" + name : name;
    }

    // ── GameDataStorage ──

    public class GameDataStorage {

        private final String name;
        private final Path path;
        private final Map<String, Box3StorageTypes.ValueEntry> data;

        GameDataStorage(String name) {
            this.name = name;
            String[] parts = name.split("/");
            Path dir = baseDir;
            for (int i = 0; i < parts.length - 1; i++) {
                String seg = sanitize(parts[i]);
                if (!seg.isEmpty()) dir = dir.resolve(seg);
            }
            String file = sanitize(parts[parts.length - 1]);
            if (file.isEmpty()) file = "default";
            this.path = dir.resolve(file + ".json");
            this.data = cache.computeIfAbsent(path, p -> {
                if (Files.exists(p)) {
                    try {
                        String json = Files.readString(p);
                        Map<String, Box3StorageTypes.ValueEntry> map = GSON.fromJson(json, MAP_TYPE);
                        return map != null ? Collections.synchronizedMap(new LinkedHashMap<>(map))
                                           : Collections.synchronizedMap(new LinkedHashMap<>());
                    } catch (IOException e) {
                        return Collections.synchronizedMap(new LinkedHashMap<>());
                    }
                }
                return Collections.synchronizedMap(new LinkedHashMap<>());
            });
        }

        private String sanitize(String s) {
            return s.replaceAll("[^a-zA-Z0-9_.\\-]", "_");
        }

        public String getKey() { return name; }

        private void persist() {
            try {
                Files.createDirectories(path.getParent());
                Files.writeString(path, GSON.toJson(data));
            } catch (IOException ignored) {}
        }

        // ── Basic API ──

        public void set(String key, Object value) {
            if (key == null) return;
            long now = System.currentTimeMillis();
            synchronized (data) {
                Box3StorageTypes.ValueEntry existing = data.get(key);
                if (existing != null) {
                    existing.value = value;
                    existing.updateTime = now;
                    existing.version = Long.toHexString(now) + "-" + Integer.toHexString(new Random().nextInt());
                } else {
                    data.put(key, new Box3StorageTypes.ValueEntry(value, now));
                }
                persist();
            }
        }

        public Object get(String key) {
            if (key == null) return null;
            synchronized (data) {
                Box3StorageTypes.ValueEntry entry = data.get(key);
                return entry != null ? entry.value : null;
            }
        }

        public String[] keys() {
            synchronized (data) {
                return data.keySet().toArray(new String[0]);
            }
        }

        public void update(String key, Function handler) {
            if (key == null || handler == null) return;
            synchronized (data) {
                Box3StorageTypes.ValueEntry entry = data.get(key);
                if (entry == null) return;
                long now = System.currentTimeMillis();
                Context cx = Context.enter();
                try {
                    entry.value = handler.call(cx, handler, handler, new Object[]{entry.value});
                } finally {
                    Context.exit();
                }
                entry.updateTime = now;
                entry.version = Long.toHexString(now) + "-" + Integer.toHexString(new Random().nextInt());
                persist();
            }
        }

        public Object remove(String key) {
            if (key == null) return null;
            synchronized (data) {
                Box3StorageTypes.ValueEntry entry = data.remove(key);
                if (entry != null) {
                    persist();
                    return entry.value;
                }
            }
            return null;
        }

        public double increment(String key, double value) {
            if (key == null) return 0;
            double delta = Double.isNaN(value) ? 1.0 : value;
            long now = System.currentTimeMillis();
            synchronized (data) {
                Box3StorageTypes.ValueEntry entry = data.get(key);
                if (entry != null) {
                    if (entry.value instanceof Number n) {
                        entry.value = n.doubleValue() + delta;
                    } else {
                        entry.value = delta;
                    }
                    entry.updateTime = now;
                    entry.version = Long.toHexString(now) + "-" + Integer.toHexString(new Random().nextInt());
                } else {
                    entry = new Box3StorageTypes.ValueEntry(delta, now);
                    data.put(key, entry);
                }
                persist();
                return ((Number) entry.value).doubleValue();
            }
        }

        public double increment(String key) {
            return increment(key, 1.0);
        }

        public Box3StorageTypes.QueryList list(Map<String, Object> options) {
            List<Box3StorageTypes.ReturnValue> results;
            synchronized (data) {
                results = new ArrayList<>();
                for (Map.Entry<String, Box3StorageTypes.ValueEntry> e : data.entrySet()) {
                    results.add(new Box3StorageTypes.ReturnValue(e.getKey(), e.getValue()));
                }
            }

            int cursor = 0;
            int pageSize = 100;
            boolean ascending = false;
            boolean doSort = false;
            Double max = null, min = null;
            String constraintTarget = null;

            if (options != null) {
                if (options.get("cursor") instanceof Number n) cursor = n.intValue();
                if (options.get("pageSize") instanceof Number n) {
                    pageSize = Math.max(1, Math.min(100, n.intValue()));
                }
                if (options.containsKey("ascending")) {
                    doSort = true;
                    ascending = Boolean.TRUE.equals(options.get("ascending"));
                }
                if (options.get("max") instanceof Number n) max = n.doubleValue();
                if (options.get("min") instanceof Number n) min = n.doubleValue();
                if (options.get("constraintTarget") instanceof String s) constraintTarget = s;
            }

            final String target = constraintTarget;
            final boolean asc = ascending;

            if (doSort) {
                results.sort((a, b) -> {
                    double va = extractSortValue(a.value, target);
                    double vb = extractSortValue(b.value, target);
                    int cmp = Double.compare(va, vb);
                    return asc ? cmp : -cmp;
                });
            }

            if (max != null || min != null) {
                List<Box3StorageTypes.ReturnValue> filtered = new ArrayList<>();
                for (Box3StorageTypes.ReturnValue rv : results) {
                    double v = extractSortValue(rv.value, target);
                    if (min != null && v < min) continue;
                    if (max != null && v > max) continue;
                    filtered.add(rv);
                }
                results = filtered;
            }

            return new Box3StorageTypes.QueryList(results, pageSize, Math.max(0, cursor));
        }

        private double extractSortValue(Object value, String target) {
            if (target == null || target.isEmpty()) {
                if (value instanceof Number n) return n.doubleValue();
                return 0;
            }
            Object current = value;
            for (String part : target.split("\\.")) {
                if (current instanceof Map<?, ?> m) {
                    current = m.get(part);
                } else {
                    return 0;
                }
            }
            if (current instanceof Number n) return n.doubleValue();
            return 0;
        }

        public void destroy() {
            synchronized (data) {
                cache.remove(path);
                try { Files.deleteIfExists(path); } catch (IOException ignored) {}
            }
        }
    }
}

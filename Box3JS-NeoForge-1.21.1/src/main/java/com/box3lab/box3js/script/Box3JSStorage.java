package com.box3lab.box3js.script;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.mozilla.javascript.Function;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Box3JSStorage {

    private static final Gson GSON = new Gson();
    private static final Type MAP_TYPE = new TypeToken<Map<String, ValueEntry>>() {}.getType();

    private final Path baseDir;
    private final Box3ScriptEngine engine;
    private final Map<Path, Map<String, ValueEntry>> cache = new ConcurrentHashMap<>();

    public Box3JSStorage(Path configDir, Box3ScriptEngine engine) {
        this.baseDir = configDir.resolve("box3").resolve("storage");
        this.engine = engine;
        try { Files.createDirectories(baseDir); } catch (IOException ignored) {}
    }

    // ---- GameStorage ----

    public String getKey() { return ""; }

    public GameDataStorage getDataStorage(String name) {
        return new GameDataStorage(resolveName(name));
    }

    /** Shared storage accessible by all projects. */
    public GameDataStorage getGroupStorage(String name) {
        return new GameDataStorage("__shared__/" + name);
    }

    /** Prefix with project namespace if running inside a project. */
    private String resolveName(String name) {
        String project = engine.getCurrentProject();
        return project != null ? project + "/" + name : name;
    }

    // ---- ValueEntry ----

    private static class ValueEntry {
        Object value;
        long updateTime;
        long createTime;
        String version;

        ValueEntry(Object value, long createTime) {
            this.value = value;
            this.createTime = createTime;
            this.updateTime = createTime;
            this.version = Long.toHexString(createTime) + "-" + Integer.toHexString(new Random().nextInt());
        }
    }

    // ---- ReturnValue ----

    public static class ReturnValue {
        public String key;
        public Object value;
        public double updateTime;
        public double createTime;
        public String version;

        ReturnValue(String key, ValueEntry entry) {
            this.key = key;
            this.value = entry.value;
            this.updateTime = entry.updateTime;
            this.createTime = entry.createTime;
            this.version = entry.version;
        }
    }

    // ---- QueryList ----

    public static class QueryList {
        public boolean isLastPage;
        private final List<ReturnValue> all;
        private final int pageSize;
        private int cursor;

        QueryList(List<ReturnValue> all, int pageSize, int cursor) {
            this.all = all;
            this.pageSize = pageSize;
            this.cursor = Math.max(0, cursor);
            this.isLastPage = this.cursor >= all.size();
        }

        public ReturnValue[] getCurrentPage() {
            int end = Math.min(cursor + pageSize, all.size());
            if (cursor >= all.size()) return new ReturnValue[0];
            return all.subList(cursor, end).toArray(new ReturnValue[0]);
        }

        public void nextPage() {
            cursor += pageSize;
            isLastPage = cursor >= all.size();
        }
    }

    // ---- GameDataStorage ----

    public class GameDataStorage {

        private final String name;
        private final Path path;
        private final Map<String, ValueEntry> data;

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
                        Map<String, ValueEntry> map = GSON.fromJson(json, MAP_TYPE);
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

        // ---- Persist ----

        private void persist() {
            try {
                Files.createDirectories(path.getParent());
                Files.writeString(path, GSON.toJson(data));
            } catch (IOException ignored) {}
        }

        // ---- Public API ----

        public void set(String key, Object value) {
            if (key == null) return;
            long now = System.currentTimeMillis();
            synchronized (data) {
                ValueEntry existing = data.get(key);
                if (existing != null) {
                    existing.value = value;
                    existing.updateTime = now;
                    existing.version = Long.toHexString(now) + "-" + Integer.toHexString(new Random().nextInt());
                } else {
                    data.put(key, new ValueEntry(value, now));
                }
                persist();
            }
        }

        public Object get(String key) {
            if (key == null) return null;
            synchronized (data) {
                ValueEntry entry = data.get(key);
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
                ValueEntry entry = data.get(key);
                if (entry == null) return;
                long now = System.currentTimeMillis();
                entry.value = engine.callFunction(handler, entry.value);
                entry.updateTime = now;
                entry.version = Long.toHexString(now) + "-" + Integer.toHexString(new Random().nextInt());
                persist();
            }
        }

        public Object remove(String key) {
            if (key == null) return null;
            synchronized (data) {
                ValueEntry entry = data.remove(key);
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
                ValueEntry entry = data.get(key);
                if (entry != null) {
                    if (entry.value instanceof Number n) {
                        entry.value = n.doubleValue() + delta;
                    } else {
                        entry.value = delta;
                    }
                    entry.updateTime = now;
                    entry.version = Long.toHexString(now) + "-" + Integer.toHexString(new Random().nextInt());
                } else {
                    entry = new ValueEntry(delta, now);
                    data.put(key, entry);
                }
                persist();
                return ((Number) entry.value).doubleValue();
            }
        }

        public double increment(String key) {
            return increment(key, 1.0);
        }

        public QueryList list(Map<String, Object> options) {
            List<ReturnValue> results;
            synchronized (data) {
                results = new ArrayList<>();
                for (Map.Entry<String, ValueEntry> e : data.entrySet()) {
                    results.add(new ReturnValue(e.getKey(), e.getValue()));
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
                List<ReturnValue> filtered = new ArrayList<>();
                for (ReturnValue rv : results) {
                    double v = extractSortValue(rv.value, target);
                    if (min != null && v < min) continue;
                    if (max != null && v > max) continue;
                    filtered.add(rv);
                }
                results = filtered;
            }

            return new QueryList(results, pageSize, Math.max(0, cursor));
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

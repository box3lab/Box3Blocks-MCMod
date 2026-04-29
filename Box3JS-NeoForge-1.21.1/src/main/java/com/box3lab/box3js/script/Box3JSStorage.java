package com.box3lab.box3js.script;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.mozilla.javascript.Function;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Box3JSStorage {

    private static final Gson GSON = new Gson();
    private static final Type MAP_TYPE = new TypeToken<Map<String, ValueEntry>>() {}.getType();

    private final Path baseDir;
    private final Box3ScriptEngine engine;

    public Box3JSStorage(Path configDir, Box3ScriptEngine engine) {
        this.baseDir = configDir.resolve("box3").resolve("storage");
        this.engine = engine;
        try { Files.createDirectories(baseDir); } catch (IOException ignored) {}
    }

    // ---- GameStorage ----

    /** storage.key — always empty for MC local storage */
    public String getKey() { return ""; }

    /** storage.getDataStorage(name): GameDataStorage */
    public GameDataStorage getDataStorage(String name) {
        return new GameDataStorage(name);
    }

    /** storage.getGroupStorage(name): GameDataStorage — same as getDataStorage in MC */
    public GameDataStorage getGroupStorage(String name) {
        return new GameDataStorage(name);
    }

    // ---- ValueEntry (internal metadata container) ----

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

    // ---- ReturnValue (JS-accessible) ----

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

    // ---- QueryList (JS-accessible) ----

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
            List<ReturnValue> slice = all.subList(cursor, end);
            return slice.toArray(new ReturnValue[0]);
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
        }

        private String sanitize(String s) {
            return s.replaceAll("[^a-zA-Z0-9_.\\-]", "_");
        }

        /** GameDataStorage.key — read-only space name */
        public String getKey() { return name; }

        // ---- Internal read/write ----

        private synchronized Map<String, ValueEntry> read() {
            if (!Files.exists(path)) return new LinkedHashMap<>();
            try {
                String json = Files.readString(path);
                Map<String, ValueEntry> map = GSON.fromJson(json, MAP_TYPE);
                return map != null ? new LinkedHashMap<>(map) : new LinkedHashMap<>();
            } catch (IOException e) {
                return new LinkedHashMap<>();
            }
        }

        private synchronized void write(Map<String, ValueEntry> map) {
            try {
                Files.createDirectories(path.getParent());
                Files.writeString(path, GSON.toJson(map));
            } catch (IOException ignored) {}
        }

        // ---- Public API ----

        /** set(key: string, value: JSONValue): void */
        public void set(String key, Object value) {
            if (key == null) return;
            Map<String, ValueEntry> map = read();
            ValueEntry existing = map.get(key);
            long now = System.currentTimeMillis();
            if (existing != null) {
                existing.value = value;
                existing.updateTime = now;
                existing.version = Long.toHexString(now) + "-" + Integer.toHexString(new Random().nextInt());
            } else {
                map.put(key, new ValueEntry(value, now));
            }
            write(map);
        }

        /** get(key: string): value — returns the stored value directly */
        public Object get(String key) {
            if (key == null) return null;
            Map<String, ValueEntry> map = read();
            ValueEntry entry = map.get(key);
            return entry != null ? entry.value : null;
        }

        /** update(key: string, handler: function(prevValue): value): void */
        public void update(String key, Function handler) {
            if (key == null || handler == null) return;
            Map<String, ValueEntry> map = read();
            ValueEntry entry = map.get(key);
            if (entry == null) return; // can't update non-existent key per Box3 spec
            Object newValue = engine.callFunction(handler, entry.value);
            long now = System.currentTimeMillis();
            entry.value = newValue;
            entry.updateTime = now;
            entry.version = Long.toHexString(now) + "-" + Integer.toHexString(new Random().nextInt());
            write(map);
        }

        /** remove(key: string): value — returns the old value */
        public Object remove(String key) {
            if (key == null) return null;
            Map<String, ValueEntry> map = read();
            ValueEntry entry = map.remove(key);
            if (entry != null) {
                write(map);
                return entry.value;
            }
            return null;
        }

        /** increment(key: string, value?: number): number — atomic increment, default delta=1 */
        public double increment(String key, double value) {
            if (key == null) return 0;
            // Rhino calls increment(key) with undefined for the second arg,
            // which maps to Double.NaN in Java. Handle that case.
            double delta = Double.isNaN(value) ? 1.0 : value;
            synchronized (this) {
                Map<String, ValueEntry> map = read();
                ValueEntry entry = map.get(key);
                long now = System.currentTimeMillis();
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
                    map.put(key, entry);
                }
                write(map);
                return ((Number) entry.value).doubleValue();
            }
        }

        // Overload for Rhino: when called with 1 arg
        public double increment(String key) {
            return increment(key, 1.0);
        }

        /** list(options: {cursor, pageSize?, ascending?, max?, min?, constraintTarget?}): QueryList */
        public QueryList list(Map<String, Object> options) {
            Map<String, ValueEntry> map = read();
            List<ReturnValue> results = new ArrayList<>();

            for (Map.Entry<String, ValueEntry> e : map.entrySet()) {
                results.add(new ReturnValue(e.getKey(), e.getValue()));
            }

            // Parse options
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

            // Sort
            if (doSort) {
                results.sort((a, b) -> {
                    double va = extractSortValue(a.value, target);
                    double vb = extractSortValue(b.value, target);
                    int cmp = Double.compare(va, vb);
                    return asc ? cmp : -cmp;
                });
            }

            // Filter by min/max
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
            // Navigate nested path like "a.b.c"
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

        /** destroy(): void — delete this data storage space */
        public void destroy() {
            try { Files.deleteIfExists(path); } catch (IOException ignored) {}
            synchronized (this) {
                try { Files.deleteIfExists(path); } catch (IOException ignored) {}
            }
        }
    }
}

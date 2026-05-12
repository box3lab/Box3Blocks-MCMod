package com.box3lab.box3js.script;

import java.util.*;

/**
 * Shared storage data types used by both server-side and client-side storage.
 */
public final class Box3StorageTypes {

    private Box3StorageTypes() {}

    // ── ValueEntry ──

    public static class ValueEntry {
        public Object value;
        public long updateTime;
        public long createTime;
        public String version;

        public ValueEntry(Object value, long createTime) {
            this.value = value;
            this.createTime = createTime;
            this.updateTime = createTime;
            this.version = Long.toHexString(createTime) + "-" + Integer.toHexString(new Random().nextInt());
        }
    }

    // ── ReturnValue ──

    public static class ReturnValue {
        public String key;
        public Object value;
        public double updateTime;
        public double createTime;
        public String version;

        public ReturnValue(String key, ValueEntry entry) {
            this.key = key;
            this.value = entry.value;
            this.updateTime = entry.updateTime;
            this.createTime = entry.createTime;
            this.version = entry.version;
        }
    }

    // ── QueryList ──

    public static class QueryList {
        public boolean isLastPage;
        private final List<ReturnValue> all;
        private final int pageSize;
        private int cursor;

        public QueryList(List<ReturnValue> all, int pageSize, int cursor) {
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
}

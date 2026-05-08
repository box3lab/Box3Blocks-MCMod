package com.box3lab.box3js.script;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Wraps a SQL query result set. Supports:
 * <ul>
 *   <li>{@code next()} — iterator-style row access</li>
 *   <li>{@code then(resolve, reject)} — thenable so it works with await (future)</li>
 *   <li>{@code rows} / {@code firstRow} — direct access</li>
 *   <li>{@code affectedRows} — for INSERT/UPDATE/DELETE</li>
 *   <li>{@code columnCount} — number of columns in the result</li>
 *   <li>{@code columnNames} — column name array</li>
 * </ul>
 *
 * <p>In Rhino ES5, scripts iterate via:
 * <pre>{@code
 *   var result = db.sql(["SELECT * FROM players"]);
 *   var row;
 *   while (!(row = result.next()).done) {
 *     console.log(row.value.name);
 *   }
 *   // or simply:
 *   var all = result.rows;
 * }</pre>
 */
public class Box3JSQueryResult {

    private final List<Map<String, Object>> rows;
    private final String[] columnNames;
    private final int affectedRows;
    private int cursor;

    /** Construct from a SELECT result set. */
    public Box3JSQueryResult(List<Map<String, Object>> rows, String[] columnNames) {
        this.rows = rows != null ? Collections.unmodifiableList(new ArrayList<>(rows)) : Collections.emptyList();
        this.columnNames = columnNames != null ? columnNames.clone() : new String[0];
        this.affectedRows = -1; // SELECT
        this.cursor = 0;
    }

    /** Construct for an INSERT/UPDATE/DELETE (no result set). */
    public Box3JSQueryResult(int affectedRows) {
        this.rows = Collections.emptyList();
        this.columnNames = new String[0];
        this.affectedRows = affectedRows;
        this.cursor = 0;
    }

    // ---- Iteration ----

    /**
     * Returns the next row as {@code {done: boolean, value: any}}.
     * After the last row, {@code done} is {@code true} and {@code value} is {@code null}.
     */
    public NativeObject next() {
        NativeObject obj = new NativeObject();
        if (cursor < rows.size()) {
            obj.put("done", obj, Boolean.FALSE);
            obj.put("value", obj, mapToNativeObject(rows.get(cursor)));
            cursor++;
        } else {
            obj.put("done", obj, Boolean.TRUE);
            obj.put("value", obj, null);
        }
        return obj;
    }

    /** Resets the internal cursor so {@link #next()} starts from the first row again. */
    public void reset() {
        cursor = 0;
    }

    // ---- Thenable ----

    /**
     * Makes this result thenable: {@code then(resolve, reject)} calls
     * {@code resolve(rows)} immediately with all rows as a Java array.
     * In Rhino ES5 the {@code resolve} callback is invoked synchronously.
     */
    public void then(Function resolve, Function reject) {
        if (resolve != null) {
            Context cx = Context.getCurrentContext();
            if (cx == null) { cx = Context.enter(); }
            try {
                resolve.call(cx, resolve, resolve, new Object[] { getRows() });
            } catch (Exception e) {
                if (reject != null) {
                    reject.call(cx, reject, reject, new Object[] { e.getMessage() });
                }
            } finally {
                if (cx != null) { /* leave */ }
            }
        }
    }

    // ---- Data accessors ----

    /** All rows as a NativeArray. Each row is a NativeObject mapping column name → value. */
    public Object getRows() {
        NativeArray arr = new NativeArray(rows.size());
        for (int i = 0; i < rows.size(); i++) {
            NativeObject row = mapToNativeObject(rows.get(i));
            arr.put(i, arr, row);
        }
        return arr;
    }

    /** The first row as a NativeObject, or null if empty. */
    public Object getFirstRow() {
        if (rows.isEmpty()) { return null; }
        return mapToNativeObject(rows.get(0));
    }

    /** Number of columns in the result. */
    public int getColumnCount() {
        return columnNames.length;
    }

    /** Column names as a NativeArray. */
    public Object getColumnNames() {
        NativeArray arr = new NativeArray(columnNames.length);
        for (int i = 0; i < columnNames.length; i++) {
            arr.put(i, arr, columnNames[i]);
        }
        return arr;
    }

    /**
     * Number of rows affected by INSERT/UPDATE/DELETE.
     * Returns -1 for SELECT queries (use {@link #getRowCount()} for those).
     */
    public int getAffectedRows() {
        return affectedRows;
    }

    /** Number of rows in the result set (SELECT queries). */
    public int getRowCount() {
        return rows.size();
    }

    /** True for SELECT queries that produced a result set. */
    public boolean isQuery() {
        return affectedRows < 0;
    }

    // ---- Internal ----

    /**
     * Converts a Java Map (from SQLite result row) to a NativeObject,
     * bypassing Context.javaToJS to avoid scope-chain NPEs in Rhino.
     */
    private static NativeObject mapToNativeObject(Map<String, Object> map) {
        NativeObject obj = new NativeObject();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            obj.put(entry.getKey(), obj, convertValue(entry.getValue()));
        }
        return obj;
    }

    /** Converts a SQLite column value to a Rhino-compatible value. */
    private static Object convertValue(Object value) {
        if (value == null) { return null; }
        if (value instanceof Number) { return value; }
        if (value instanceof Boolean) { return value; }
        if (value instanceof String) { return value; }
        if (value instanceof byte[] bytes) {
            NativeArray arr = new NativeArray(bytes.length);
            for (int i = 0; i < bytes.length; i++) {
                arr.put(i, arr, bytes[i] & 0xFF);
            }
            return arr;
        }
        return value.toString();
    }

    @Override
    public String toString() {
        if (isQuery()) {
            return "QueryResult{rows=" + rows.size() + ", cols=" + columnNames.length + "}";
        }
        return "QueryResult{affected=" + affectedRows + "}";
    }
}

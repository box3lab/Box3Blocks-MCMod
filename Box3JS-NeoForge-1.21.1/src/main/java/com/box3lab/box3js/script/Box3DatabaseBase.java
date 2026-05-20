package com.box3lab.box3js.script;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.mozilla.javascript.NativeArray;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

/**
 * Abstract base for server and client SQLite database wrappers.
 * Subclasses provide connection management; this class handles SQL execution,
 * parameter binding, and result-set reading.
 */
public abstract class Box3DatabaseBase {

    protected static final Logger LOGGER = LogUtils.getLogger();
    protected static final String SQLITE_DRIVER_CLASS = "org.sqlite.JDBC";
    protected static final String SQLITE_MISSING_HINT = "db API requires SQLite JDBC driver. Install the minecraft-sqlite-jdbc mod.";
    protected static final boolean SQLITE_AVAILABLE;

    static {
        boolean ok;
        try {
            Class.forName(SQLITE_DRIVER_CLASS);
            ok = true;
        } catch (ClassNotFoundException e) {
            ok = false;
            LOGGER.warn("{}", SQLITE_MISSING_HINT);
        }
        SQLITE_AVAILABLE = ok;
    }

    public static boolean isAvailable() {
        return SQLITE_AVAILABLE;
    }

    protected void ensureSqliteAvailable() {
        if (!SQLITE_AVAILABLE) {
            throw new IllegalStateException(SQLITE_MISSING_HINT);
        }
    }

    /** Subclasses provide the active JDBC connection. */
    protected abstract Connection getConnection();

    /**
     * Executes a SQL query or update.
     *
     * <p>
     * Two calling conventions are supported:
     * <ol>
     * <li>Regular: {@code db.sql("SELECT ... WHERE x = ?", value)}</li>
     * <li>Tagged template: {@code db.sql(["SELECT ... WHERE x = ", ""], value)}
     * — the string array fragments are joined with {@code ?} placeholders.</li>
     * </ol>
     */
    public Box3JSQueryResult sql(Object... args) {
        ensureSqliteAvailable();

        if (args.length == 0) {
            throw new IllegalArgumentException("db.sql() requires at least a SQL string argument");
        }

        String sql;
        Object[] params;

        if (args[0] instanceof String s) {
            sql = s;
            params = new Object[args.length - 1];
            System.arraycopy(args, 1, params, 0, params.length);
        } else if (args[0] instanceof NativeArray parts) {
            StringBuilder sb = new StringBuilder();
            long len = parts.getLength();
            int paramCount = args.length - 1;
            for (int i = 0; i < len; i++) {
                sb.append(parts.get(i).toString());
                if (i < paramCount) {
                    sb.append("?");
                }
            }
            sql = sb.toString();
            params = new Object[paramCount];
            System.arraycopy(args, 1, params, 0, params.length);
        } else {
            throw new IllegalArgumentException(
                    "db.sql(): first argument must be a SQL string or string array");
        }

        Connection conn = getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                bindParam(stmt, i + 1, params[i]);
            }

            boolean isQuery = stmt.execute();
            if (isQuery) {
                try (ResultSet rs = stmt.getResultSet()) {
                    return readResultSet(rs);
                }
            } else {
                int count = stmt.getUpdateCount();
                return new Box3JSQueryResult(count);
            }
        } catch (SQLException e) {
            String preview = sql == null ? "" : sql.strip();
            if (preview.length() > 120) {
                preview = preview.substring(0, 120) + "...";
            }
            LOGGER.error("SQL error: {} | sql={}", e.getMessage(), preview);
            throw new RuntimeException("SQL error: " + e.getMessage() + " | query: " + preview, e);
        }
    }

    protected void bindParam(PreparedStatement stmt, int index, Object value) throws SQLException {
        if (value == null || value == org.mozilla.javascript.Undefined.instance) {
            stmt.setNull(index, java.sql.Types.NULL);
        } else if (value instanceof Number n) {
            double d = n.doubleValue();
            if (d == Math.floor(d) && d <= Long.MAX_VALUE && d >= Long.MIN_VALUE) {
                stmt.setLong(index, (long) d);
            } else {
                stmt.setDouble(index, d);
            }
        } else if (value instanceof Boolean b) {
            stmt.setBoolean(index, b);
        } else if (value instanceof String s) {
            stmt.setString(index, s);
        } else if (value instanceof NativeArray arr) {
            byte[] bytes = new byte[(int) arr.getLength()];
            for (int i = 0; i < bytes.length; i++) {
                Object elem = arr.get(i);
                bytes[i] = (byte) (elem instanceof Number n ? n.intValue() : 0);
            }
            stmt.setBytes(index, bytes);
        } else {
            stmt.setString(index, value.toString());
        }
    }

    protected Box3JSQueryResult readResultSet(ResultSet rs) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        int colCount = meta.getColumnCount();
        String[] columnNames = new String[colCount];
        for (int i = 0; i < colCount; i++) {
            columnNames[i] = meta.getColumnName(i + 1);
        }

        List<Map<String, Object>> rows = new ArrayList<>();
        while (rs.next()) {
            Map<String, Object> row = new LinkedHashMap<>();
            for (int i = 0; i < colCount; i++) {
                Object value = rs.getObject(i + 1);
                row.put(columnNames[i], value);
            }
            rows.add(row);
        }

        return new Box3JSQueryResult(rows, columnNames);
    }
}

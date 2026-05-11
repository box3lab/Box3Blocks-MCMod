package com.box3lab.box3js.client;

import com.box3lab.box3js.script.Box3JSQueryResult;
import com.mojang.logging.LogUtils;
import org.mozilla.javascript.NativeArray;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Client-side SQLite database exposed to JS as the {@code db} global.
 *
 * <p>Database files are stored at {@code <gameDir>/box3/client-db/<project>.db}.
 */
public class Box3JSClientDatabase {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final String SQLITE_DRIVER_CLASS = "org.sqlite.JDBC";
    private static final String SQLITE_MISSING_HINT = "db API requires SQLite JDBC driver. Install the minecraft-sqlite-jdbc mod.";
    private static final boolean SQLITE_AVAILABLE;

    private final Path dataDir;
    private String projectName;
    private Connection connection;

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

    public Box3JSClientDatabase(java.io.File gameDir) {
        this.dataDir = gameDir.toPath().resolve("box3").resolve("client-db");
        try { Files.createDirectories(dataDir); } catch (IOException ignored) {}
    }

    public void setProjectName(String name) {
        if (!name.equals(this.projectName)) {
            close();
            this.projectName = name;
            this.connection = null;
        }
    }

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
            LOGGER.error("SQL error: {}", e.getMessage());
            throw new RuntimeException("SQL error: " + e.getMessage(), e);
        }
    }

    public void close() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOGGER.warn("Error closing client database: {}", e.getMessage());
            }
            connection = null;
        }
    }

    private Connection getConnection() {
        ensureSqliteAvailable();
        if (projectName == null) {
            throw new IllegalStateException("db: no active project context");
        }

        if (connection == null) {
            try {
                Path dbFile = dataDir.resolve(projectName + ".db");
                Files.createDirectories(dbFile.getParent());
                String url = "jdbc:sqlite:" + dbFile.toAbsolutePath().toString().replace('\\', '/');
                connection = DriverManager.getConnection(url);
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute("PRAGMA journal_mode=WAL");
                }
                LOGGER.info("Opened client database for project {}: {}", projectName, dbFile);
            } catch (IOException | SQLException e) {
                LOGGER.error("Failed to open client database: {}", e.getMessage());
                throw new RuntimeException("Failed to open database: " + e.getMessage(), e);
            }
        }

        try {
            if (connection.isClosed()) {
                connection = null;
                return getConnection();
            }
        } catch (SQLException e) {
            connection = null;
            return getConnection();
        }

        return connection;
    }

    private void bindParam(PreparedStatement stmt, int index, Object value) throws SQLException {
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

    /** @see Box3JSDatabase#isAvailable() */
    public static boolean isAvailable() {
        return SQLITE_AVAILABLE;
    }

    private static void ensureSqliteAvailable() {
        if (!SQLITE_AVAILABLE) {
            throw new IllegalStateException(SQLITE_MISSING_HINT);
        }
    }

    private Box3JSQueryResult readResultSet(ResultSet rs) throws SQLException {
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

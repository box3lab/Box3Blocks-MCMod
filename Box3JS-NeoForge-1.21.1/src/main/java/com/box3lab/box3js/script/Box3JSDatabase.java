package com.box3lab.box3js.script;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.mozilla.javascript.NativeArray;

import com.box3lab.box3js.Box3JS;

/**
 * Per-project SQLite database exposed to JS as the {@code db} global.
 *
 * <p>
 * Database files are stored at {@code config/box3/data/<project>.db}.
 * Connections are lazily opened on first use and closed when the project
 * is stopped or removed.
 *
 * <h3>JS Usage</h3>
 * 
 * <pre>{@code
 *   // Regular query with ? placeholders
 *   var result = db.sql("SELECT * FROM players WHERE score > ?", 100);
 *   var all = result.rows;
 *
 *   // Tagged template style (transpiled from TS template literals)
 *   var result = db.sql(["SELECT * FROM players WHERE id = ", ""], playerId);
 *
 *   // INSERT / UPDATE / DELETE
 *   var result = db.sql("INSERT INTO log (name, msg) VALUES (?, ?)", "Steve", "hello");
 *   console.log(result.affectedRows); // 1
 *
 *   // Thenable pattern
 *   db.sql("SELECT * FROM players").then(function(rows) {
 *     console.log(rows.length);
 *   });
 *
 *   // Iteration
 *   var result = db.sql("SELECT * FROM players");
 *   var row;
 *   while (!(row = result.next()).done) {
 *     console.log(row.value.name);
 *   }
 * }</pre>
 */
public class Box3JSDatabase {

    private static final String SQLITE_DRIVER_CLASS = "org.sqlite.JDBC";
    private static final String SQLITE_MISSING_HINT = "db API requires SQLite JDBC driver. Install the minecraft-sqlite-jdbc mod, then restart server.";
    private static final boolean SQLITE_AVAILABLE;

    private final Path dataDir;
    private final Box3ScriptEngine engine;
    private final Map<String, Connection> connections = new LinkedHashMap<>();

    static {
        boolean ok;
        try {
            Class.forName(SQLITE_DRIVER_CLASS);
            ok = true;
        } catch (ClassNotFoundException e) {
            ok = false;
            Box3JS.LOGGER.warn("{}", SQLITE_MISSING_HINT);
        }
        SQLITE_AVAILABLE = ok;
    }

    public Box3JSDatabase(Path configDir, Box3ScriptEngine engine) {
        this.dataDir = configDir.resolve("box3").resolve("data");
        this.engine = engine;
        try {
            Files.createDirectories(dataDir);
        } catch (java.io.IOException ignored) {
        }
    }

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
     *
     * @param args first element is either a String (SQL with ? placeholders)
     *             or a NativeArray of string fragments (tagged template style).
     *             Remaining elements are parameter values to bind.
     * @return the query result
     */
    public Box3JSQueryResult sql(Object... args) {
        ensureSqliteAvailable();

        if (args.length == 0) {
            throw new IllegalArgumentException("db.sql() requires at least a SQL string argument");
        }

        // Parse SQL and params from args
        String sql;
        Object[] params;

        if (args[0] instanceof String s) {
            sql = s;
            params = new Object[args.length - 1];
            System.arraycopy(args, 1, params, 0, params.length);
        } else if (args[0] instanceof NativeArray parts) {
            // Tagged template literal: join fragments with ? placeholders
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

            // Bind parameters
            for (int i = 0; i < params.length; i++) {
                bindParam(stmt, i + 1, params[i]);
            }

            // Execute
            boolean isQuery = stmt.execute();
            if (isQuery) {
                // SELECT or other query that returns a result set
                try (ResultSet rs = stmt.getResultSet()) {
                    return readResultSet(rs);
                }
            } else {
                // INSERT / UPDATE / DELETE
                int count = stmt.getUpdateCount();
                return new Box3JSQueryResult(count);
            }
        } catch (SQLException e) {
            Box3JS.LOGGER.error("SQL error: {}", e.getMessage());
            throw new RuntimeException("SQL error: " + e.getMessage(), e);
        }
    }

    /**
     * Closes the database connection for the given project.
     * Called when a project is stopped or removed.
     */
    public void closeProject(String project) {
        Connection conn = connections.remove(project);
        if (conn != null) {
            try {
                if (!conn.isClosed()) {
                    conn.close();
                }
                Box3JS.LOGGER.debug("Closed database for project: {}", project);
            } catch (SQLException e) {
                Box3JS.LOGGER.warn("Error closing database for {}: {}", project, e.getMessage());
            }
        }
    }

    /** Closes all open database connections. */
    public void closeAll() {
        for (var entry : new ArrayList<>(connections.entrySet())) {
            closeProject(entry.getKey());
        }
    }

    // ---- Internal ----

    private Connection getConnection() {
        ensureSqliteAvailable();

        String project = engine.getCurrentProject();
        if (project == null) {
            throw new IllegalStateException("db: no active project context");
        }

        return connections.computeIfAbsent(project, p -> {
            try {
                Path dbFile = dataDir.resolve(p + ".db");
                Files.createDirectories(dbFile.getParent());
                String url = "jdbc:sqlite:" + dbFile.toAbsolutePath().toString().replace('\\', '/');
                Connection conn = DriverManager.getConnection(url);
                // Enable WAL mode for better concurrent read performance
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("PRAGMA journal_mode=WAL");
                }
                Box3JS.LOGGER.info("Opened database for project {}: {}", p, dbFile);
                return conn;
            } catch (IOException | SQLException e) {
                Box3JS.LOGGER.error("Failed to open database for project {}: {}", p, e.getMessage());
                throw new RuntimeException("Failed to open database: " + e.getMessage(), e);
            }
        });
    }

    private void bindParam(PreparedStatement stmt, int index, Object value) throws SQLException {
        if (value == null || value == org.mozilla.javascript.Undefined.instance) {
            stmt.setNull(index, java.sql.Types.NULL);
        } else if (value instanceof Number n) {
            // Use double for all numbers (SQLite uses dynamic typing)
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
            // Uint8Array / byte array
            byte[] bytes = new byte[(int) arr.getLength()];
            for (int i = 0; i < bytes.length; i++) {
                Object elem = arr.get(i);
                bytes[i] = (byte) (elem instanceof Number n ? n.intValue() : 0);
            }
            stmt.setBytes(index, bytes);
        } else {
            // Fallback: convert to string
            stmt.setString(index, value.toString());
        }
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
                // SQLite JDBC returns byte[] for BLOB columns; keep as-is for JS
                row.put(columnNames[i], value);
            }
            rows.add(row);
        }

        return new Box3JSQueryResult(rows, columnNames);
    }
}

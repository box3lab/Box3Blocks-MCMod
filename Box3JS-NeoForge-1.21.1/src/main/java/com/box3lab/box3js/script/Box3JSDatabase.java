package com.box3lab.box3js.script;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

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
public class Box3JSDatabase extends Box3DatabaseBase {

    private static final Logger LOGGER = LogUtils.getLogger();

    private final Path dataDir;
    private final Box3ScriptEngine engine;
    private final Map<String, Connection> connections = new LinkedHashMap<>();

    public Box3JSDatabase(Path configDir, Box3ScriptEngine engine) {
        this.dataDir = configDir.resolve("box3").resolve("data");
        this.engine = engine;
        try {
            Files.createDirectories(dataDir);
        } catch (IOException e) {
            LOGGER.warn("Failed to create database directory: {}", dataDir, e);
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
                LOGGER.debug("Closed database for project: {}", project);
            } catch (SQLException e) {
                LOGGER.warn("Error closing database for {}: {}", project, e.getMessage());
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

    @Override
    protected Connection getConnection() {
        ensureSqliteAvailable();

        String project = engine.getCurrentProject();
        if (project == null) {
            throw new IllegalStateException("db: no active project context");
        }

        Connection conn = connections.get(project);
        if (conn != null) {
            try {
                if (!conn.isClosed()) {
                    return conn;
                }
                LOGGER.warn("Database connection was closed unexpectedly, reopening for project: {}", project);
            } catch (SQLException e) {
                LOGGER.warn("Failed to inspect database connection state for {}: {}", project, e.getMessage());
            }
            connections.remove(project);
        }

        Connection reopened = openProjectConnection(project);
        connections.put(project, reopened);
        return reopened;
    }

    private Connection openProjectConnection(String project) {
        try {
            Path dbFile = dataDir.resolve(project + ".db");
            Files.createDirectories(dbFile.getParent());
            String url = "jdbc:sqlite:" + dbFile.toAbsolutePath().toString().replace('\\', '/');
            Connection conn = DriverManager.getConnection(url);
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("PRAGMA journal_mode=WAL");
                stmt.execute("PRAGMA busy_timeout=5000");
                stmt.execute("PRAGMA foreign_keys=ON");
            }
            LOGGER.info("Opened database for project {}: {}", project, dbFile);
            return conn;
        } catch (IOException | SQLException e) {
            LOGGER.error("Failed to open database for project {}: {}", project, e.getMessage());
            throw new RuntimeException("Failed to open database for project '" + project + "': " + e.getMessage(), e);
        }
    }
}

package com.box3lab.box3js.client;

import com.box3lab.box3js.script.Box3DatabaseBase;
import com.box3lab.box3js.script.Box3JSQueryResult;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;

/**
 * Client-side SQLite database exposed to JS as the {@code db} global.
 *
 * <p>Database files are stored at {@code <gameDir>/box3/client-db/<project>.db}.
 */
public class Box3JSClientDatabase extends Box3DatabaseBase {

    private static final Logger LOGGER = LogUtils.getLogger();

    private final Path dataDir;
    private String projectName;
    private Connection connection;

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

    @Override
    protected Connection getConnection() {
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
}

package database;

import org.junit.jupiter.api.*;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for DatabaseInitializer.
 * Uses an in-memory SQLite database to verify that the schema
 * is created correctly and that running initialization multiple
 * times is safe (idempotent).
 */
class DatabaseInitializerTest {

    private Connection conn;

    @BeforeEach
    void setUp() throws Exception {
        // If your project already pulls in org.xerial:sqlite-jdbc, this will just work.
        conn = DriverManager.getConnection("jdbc:sqlite::memory:");
        try (Statement s = conn.createStatement()) {
            // Recommended when you rely on foreign keys
            s.execute("PRAGMA foreign_keys = ON;");
        }
        DatabaseInitializer.initializeSchema(conn);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (conn != null && !conn.isClosed()) {
            conn.close();
        }
    }

    @Test
    void createsAllTables() throws Exception {
        assertTrue(tableExists("User"));
        assertTrue(tableExists("Game"));
        assertTrue(tableExists("UserGameCollection"));
        assertTrue(tableExists("APIRequestLogs"));
        assertTrue(tableExists("SystemPerformanceReport"));
        assertTrue(tableExists("DatabaseIntegrityReport"));
    }

    @Test
    void createsExpectedIndexes() throws Exception {
        assertTrue(indexExists("idx_title_nocase"));
        assertTrue(indexExists("idx_game_release_date"));
        assertTrue(indexExists("idx_game_platform"));
        assertTrue(indexExists("idx_game_genre"));
        assertTrue(indexExists("idx_game_completion_status"));
        assertTrue(indexExists("idx_user_id"));
        assertTrue(indexExists("idx_usergame_userid_gameid"));
    }

    @Test
    void tablesHaveExpectedColumns() throws Exception {
        // User
        assertTrue(columnExists("User", "UserID"));
        assertTrue(columnExists("User", "Username"));
        assertTrue(columnExists("User", "Password"));
        assertTrue(columnExists("User", "Email"));
        assertTrue(columnExists("User", "Role"));

        // Game
        assertTrue(columnExists("Game", "GameID"));
        assertTrue(columnExists("Game", "Title"));
        assertTrue(columnExists("Game", "Developer"));
        assertTrue(columnExists("Game", "Publisher"));
        assertTrue(columnExists("Game", "ReleaseDate"));
        assertTrue(columnExists("Game", "Genre"));
        assertTrue(columnExists("Game", "CompletionStatus"));
        assertTrue(columnExists("Game", "Notes"));
        assertTrue(columnExists("Game", "CoverArt"));
        assertTrue(columnExists("Game", "Platform"));

        // UserGameCollection
        assertTrue(columnExists("UserGameCollection", "CollectionID"));
        assertTrue(columnExists("UserGameCollection", "UserID"));
        assertTrue(columnExists("UserGameCollection", "GameID"));
    }

    @Test
    void userGameCollectionHasForeignKeys() throws Exception {
        // Verify there are foreign keys defined from UserGameCollection to User and Game
        try (PreparedStatement ps = conn.prepareStatement("PRAGMA foreign_key_list('UserGameCollection')")) {
            try (ResultSet rs = ps.executeQuery()) {
                boolean fkToUser = false;
                boolean fkToGame = false;
                while (rs.next()) {
                    String table = rs.getString("table"); // referenced table
                    if ("User".equalsIgnoreCase(table)) fkToUser = true;
                    if ("Game".equalsIgnoreCase(table)) fkToGame = true;
                }
                assertTrue(fkToUser, "Expected FK from UserGameCollection to User");
                assertTrue(fkToGame, "Expected FK from UserGameCollection to Game");
            }
        }
    }

    @Test
    void initializationIsIdempotent() throws Exception {
        int beforeCount = objectCount();
        assertDoesNotThrow(() -> DatabaseInitializer.initializeSchema(conn));
        int afterCount = objectCount();
        assertEquals(beforeCount, afterCount, "Schema object count should not increase on re-initialization");
    }

    // ----- helpers -----

    private boolean tableExists(String name) throws SQLException {
        String sql = "SELECT name FROM sqlite_master WHERE type='table' AND name = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private boolean indexExists(String name) throws SQLException {
        String sql = "SELECT name FROM sqlite_master WHERE type='index' AND name = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private boolean columnExists(String table, String column) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("PRAGMA table_info('" + table + "')")) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String colName = rs.getString("name");
                    if (column.equalsIgnoreCase(colName)) {
                        return true;
                    }
                }
                return false; 
            }
        }
    }

    private int objectCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM sqlite_master WHERE type IN ('table','index','trigger','view')";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

}

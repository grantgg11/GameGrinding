package database;

import org.junit.jupiter.api.*;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * DatabaseInitializerTest contains unit tests that verify the schema bootstrap performed by
 * DatabaseInitializer using an in-memory SQLite database.
 *
 * This test class ensures that:
 * - Core objects exist: required tables (User, Game, UserGameCollection, APIRequestLogs,
 *   SystemPerformanceReport, DatabaseIntegrityReport) and supporting indexes are created.
 * - Table definitions match expectations: critical columns are present with correct names.
 * - Referential integrity is enforced: UserGameCollection declares foreign keys to User and Game.
 * - Initialization is idempotent: running initializeSchema multiple times does not duplicate or
 *   alter objects (object count remains constant).
 *
 * Tests run against a fresh in-memory connection per method, with PRAGMA foreign_keys enabled to
 * match production expectations for FK enforcement. Helper methods query sqlite_master and PRAGMA
 * views to assert object existence without relying on implementation internals.
 */
class DatabaseInitializerTest {

    private Connection conn;

    /**
     * Opens a fresh in-memory SQLite connection, enables foreign key enforcement, and
     * runs the schema initializer before each test to guarantee a known-good baseline.
     *
     * @throws Exception if the connection cannot be established or initialization fails
     */
    @BeforeEach
    void setUp() throws Exception {
        conn = DriverManager.getConnection("jdbc:sqlite::memory:");
        try (Statement s = conn.createStatement()) {
            s.execute("PRAGMA foreign_keys = ON;");
        }
        DatabaseInitializer.initializeSchema(conn);
    }

    /**
     * Closes the in-memory database connection after each test to free resources and
     * avoid cross-test interference.
     *
     * @throws Exception if closing the connection fails
     */
    @AfterEach
    void tearDown() throws Exception {
        if (conn != null && !conn.isClosed()) {
            conn.close();
        }
    }

    /**
     * Verifies that all expected top-level tables are created by the initializer.
     *
     * @throws Exception if a metadata query fails
     */
    @Test
    void createsAllTables() throws Exception {
        assertTrue(tableExists("User"));
        assertTrue(tableExists("Game"));
        assertTrue(tableExists("UserGameCollection"));
        assertTrue(tableExists("APIRequestLogs"));
        assertTrue(tableExists("SystemPerformanceReport"));
        assertTrue(tableExists("DatabaseIntegrityReport"));
    }

    /**
     * Verifies that all expected indexes exist to support fast lookups and sorting.
     *
     * @throws Exception if a metadata query fails
     */
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

    /**
     * Asserts that critical columns are present on the primary tables with expected names.
     *
     * @throws Exception if a metadata query fails
     */
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

    /**
     * Confirms that UserGameCollection declares foreign keys referencing User and Game,
     * ensuring referential integrity when rows are inserted/removed.
     *
     * @throws Exception if the PRAGMA query fails
     */
    @Test
    void userGameCollectionHasForeignKeys() throws Exception {
        try (PreparedStatement ps = conn.prepareStatement("PRAGMA foreign_key_list('UserGameCollection')")) {
            try (ResultSet rs = ps.executeQuery()) {
                boolean fkToUser = false;
                boolean fkToGame = false;
                while (rs.next()) {
                    String table = rs.getString("table"); 
                    if ("User".equalsIgnoreCase(table)) fkToUser = true;
                    if ("Game".equalsIgnoreCase(table)) fkToGame = true;
                }
                assertTrue(fkToUser, "Expected FK from UserGameCollection to User");
                assertTrue(fkToGame, "Expected FK from UserGameCollection to Game");
            }
        }
    }

    /**
     * Ensures that running the initializer more than once does not create duplicate
     * objects or otherwise change the object count (i.e., the process is idempotent).
     *
     * @throws Exception if querying sqlite_master fails
     */
    @Test
    void initializationIsIdempotent() throws Exception {
        int beforeCount = objectCount();
        assertDoesNotThrow(() -> DatabaseInitializer.initializeSchema(conn));
        int afterCount = objectCount();
        assertEquals(beforeCount, afterCount, "Schema object count should not increase on re-initialization");
    }

    // ----- helpers -----

    /**
     * Returns true if a table with the given name exists in the schema.
     *
     * @param name table name (case-sensitive as stored in sqlite_master)
     * @return true if the table exists; false otherwise
     * @throws SQLException if the metadata query fails
     */
    private boolean tableExists(String name) throws SQLException {
        String sql = "SELECT name FROM sqlite_master WHERE type='table' AND name = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Returns true if an index with the given name exists in the schema.
     *
     * @param name index name (case-sensitive as stored in sqlite_master)
     * @return true if the index exists; false otherwise
     * @throws SQLException if the metadata query fails
     */
    private boolean indexExists(String name) throws SQLException {
        String sql = "SELECT name FROM sqlite_master WHERE type='index' AND name = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Returns true if the specified column exists on the given table.
     *
     * @param table  table name
     * @param column column name to check for
     * @return true if the column exists; false otherwise
     * @throws SQLException if the PRAGMA query fails
     */
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

    /**
     * Counts all schema objects (tables, indexes, triggers, views) in sqlite_master.
     * Useful for idempotency checks before/after initialization.
     *
     * @return total object count
     * @throws SQLException if the metadata query fails
     */
    private int objectCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM sqlite_master WHERE type IN ('table','index','trigger','view')";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

}

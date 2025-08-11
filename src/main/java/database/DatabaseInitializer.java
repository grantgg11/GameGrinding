package database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * The DatabaseInitializer class is responsible for creating and initializing
 * the SQLite database schema required by the GameGrinding application.
 * 
 * It makes sure that all necessary tables, indexes, and constraints exist
 * before the application interacts with the database. This includes:
 * - User account data
 * - Game metadata
 * - User game collections
 * - API request logs
 * - System performance reports
 * - Database integrity reports
 * 
 * The schema creation process is designed so that it will not overwrite
 * existing tables or indexes.
 */
public class DatabaseInitializer {
	
	/**
	 * Initializes the database schema by creating the necessary tables and indexes if they do not already exist.
     * 
     * This method uses SQL CREATE TABLE IF NOT EXISTS and CREATE INDEX IF NOT EXISTS statements to make sure
     * that the schema is set up without overwriting existing data.
     * 
	 * @param conn the active connection to the SQLite database
	 */
    public static void initializeSchema(Connection conn) {
        try (Statement stmt = conn.createStatement()) {

            // User table
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS User (
                    UserID INTEGER PRIMARY KEY AUTOINCREMENT,
                    Username TEXT NOT NULL UNIQUE,
                    Password TEXT NOT NULL,
                    Email TEXT NOT NULL UNIQUE,
                    Role TEXT CHECK(Role IN ('User', 'Admin')) NOT NULL DEFAULT 'User',
                    securityAnswer1 TEXT,
                    securityAnswer2 TEXT,
                    securityAnswer3 TEXT
                );
            """);

            // Game table
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS Game (
                    GameID INTEGER PRIMARY KEY AUTOINCREMENT,
                    Title TEXT NOT NULL,
                    Developer TEXT,
                    Publisher TEXT,
                    ReleaseDate TEXT CHECK (length(ReleaseDate) = 10 AND ReleaseDate LIKE '____-__-__'),
                    Genre TEXT,
                    CompletionStatus TEXT CHECK(CompletionStatus IN ('Not Started', 'Playing', 'Completed')),
                    Notes TEXT,
                    CoverArt TEXT,
                    Platform TEXT
                );
            """);

            // Indexes for Game table
            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_title_nocase ON Game(Title COLLATE NOCASE);");
            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_game_release_date ON Game(ReleaseDate);");
            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_game_platform ON Game(Platform);");
            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_game_genre ON Game(Genre);");
            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_game_completion_status ON Game(CompletionStatus);");

            // UserGameCollection table
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS UserGameCollection (
                    CollectionID INTEGER PRIMARY KEY AUTOINCREMENT,
                    UserID INTEGER NOT NULL,
                    GameID INTEGER NOT NULL,
                    FOREIGN KEY (UserID) REFERENCES User (UserID),
                    FOREIGN KEY (GameID) REFERENCES Game (GameID)
                );
            """);

            // Indexes for UserGameCollection
            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_user_id ON UserGameCollection(UserID);");
            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_usergame_userid_gameid ON UserGameCollection(UserID, GameID);");

            // APIRequestLogs table
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS APIRequestLogs (
                    RequestID TEXT PRIMARY KEY,
                    UserID INTEGER NOT NULL,
                    Timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
                    APIEndpoint TEXT NOT NULL,
                    ResponseTime INTEGER CHECK (ResponseTime >= 0),
                    Status TEXT CHECK (Status IN ('Success', 'Failed')),
                    ErrorCode INTEGER,
                    FOREIGN KEY(UserID) REFERENCES User(UserID) ON DELETE CASCADE
                );
            """);

            // SystemPerformanceReport table
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS SystemPerformanceReport (
                    ReportID INTEGER PRIMARY KEY AUTOINCREMENT,
                    Timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
                    ScreenLoadTime INTEGER,
                    DbQueryTime INTEGER,
                    MemoryUsage INTEGER,
                    GcEvents INTEGER,
                    ExceptionsLogged TEXT
                );
            """);

            // DatabaseIntegrityReport table
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS DatabaseIntegrityReport (
                    ReportID INTEGER PRIMARY KEY AUTOINCREMENT,
                    Timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
                    TotalEntries INTEGER,
                    MissingDataEntries INTEGER,
                    DuplicationCount INTEGER,
                    FkIntegrity BOOLEAN CHECK (FkIntegrity IN (0, 1))
                );
            """);

            System.out.println("Database schema initialized.");

        } catch (SQLException e) {
            System.err.println("Error during schema initialization: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

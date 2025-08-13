package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *  DatabaseManager is responsible for managing database connections for the GameGrinding application.
 *  It supports two modes of operation: production mode, which uses a local SQLite database file stored
 *  on disk, and test mode, which uses an in-memory SQLite database for testing purposes.
 *    
 *  In production mode, each call to getConnection returns a new connection to the database file.
 *  In test mode, the class maintains a single shared connection to the in-memory database so that
 *  the data persists for the duration of the tests.
 *   
 *  This design helps separate real data from test data and makes sure that integration tests run quickly
 *  without altering production data. The class also provides a method to close the shared test connection
 *  when testing is complete.
 */
public class DatabaseManager {
    private static Connection sharedConnection; // Shared connection used in test mode to keep in-memory database data persistent across tests
    private static boolean testMode = false;    // Flag that determines whether the class operates in test mode

    private static final String PROD_DB_URL = "jdbc:sqlite:GameGrinding.db";						// JDBC URL for the production SQLite database file
    private static final String TEST_DB_URL = "jdbc:sqlite:file:memdb1?mode=memory&cache=shared";   // JDBC URL for the in-memory SQLite database used for testing

    /**
     * Enables test mode so that the database manager connects to an in-memory database instead of the production database
     */
    public static void enableTestMode() {
        testMode = true;
    }

    /**
     *  Returns a database connection.
     *  In test mode, returns the shared in-memory database connection.
     *  In production mode, returns a new connection to the database file.
     *  
     * @return a Connection object to the database, or null if the connection could not be established
     */
    public Connection getConnection() {
        try {
            if (testMode) {
                if (sharedConnection == null || sharedConnection.isClosed()) {
                    sharedConnection = DriverManager.getConnection(TEST_DB_URL);
                }
                return sharedConnection;
            } else { 
                return DriverManager.getConnection(PROD_DB_URL);
            }
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Closes the shared in-memory database connection if it exists and is open.
     * This method has no effect in production mode because production connections are created and closed by the caller.
     */
    public void closeConnection() {
        try {
            if (sharedConnection != null && !sharedConnection.isClosed()) {
                sharedConnection.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
}

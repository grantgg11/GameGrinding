package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DatabaseManager is a utility class that manages the connection to an SQLite database.
 */
public class DatabaseManager {
    private static Connection sharedConnection; // Connection object to manage the database connection
    private static boolean testMode = false;

    private static final String PROD_DB_URL = "jdbc:sqlite:GameGrinding.db";
    private static final String TEST_DB_URL = "jdbc:sqlite:file:memdb1?mode=memory&cache=shared";;

    public static void enableTestMode() {
        testMode = true;
    }

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

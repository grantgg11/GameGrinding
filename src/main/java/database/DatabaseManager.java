package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DatabaseManager is a utility class that manages the connection to an SQLite database.
 */
public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:/Users/Owner/Desktop/GameGrindingCapstone Project/GameGrinding.db"; // Path to my SQLite database file 
    private Connection connection; // Connection object to manage the database connection

    /**
	 * Constructor for DatabaseManager.
	 * Initializes the database connection.
	 */
    public Connection getConnection() {
        try {
            return DriverManager.getConnection(DB_URL);
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
            return null;
        }
    }

    
    /**
	 * Closes the database connection if it is open.
	 */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

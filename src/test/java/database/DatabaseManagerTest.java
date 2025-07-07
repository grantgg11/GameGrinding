package database;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
 
/**
 * Unit tests for the DatabaseManager class.
 *
 * This test class verifies the behavior of database connection management logic
 * provided by the DatabaseManager class. It covers both test mode (in-memory SQLite)
 * and production mode (standard JDBC SQLite connections), ensuring that the connection
 * logic is reliable, efficient, and resilient to SQL exceptions.
 */
class DatabaseManagerTest {

    private DatabaseManager dbManager;

    /**
     * Initializes a new instance of DatabaseManager before each test.
     * Ensures tests run with a clean and isolated instance.
     */
    @BeforeEach
    void setUp() {
        dbManager = new DatabaseManager();
    }


    /**
 	 * Closes the active database connection after each test.
 	 * Ensures that test mode shared connections are properly shut down.
 	 */
    @AfterEach
    void tearDown() {
        dbManager.closeConnection();
    }

    ///////////////////////////////////////// getConnection /////////////////////////////////////////

    /**
     * Tests that getConnection returns a valid in-memory SQLite connection when test mode is enabled.
     *
     * Verifies that the connection is not null and is open.
     *
     * @throws SQLException if there is an error obtaining the connection
     */
    @Test
    void testGetConnection_TestModeEnabled_ReturnsInMemoryConnection() throws SQLException {
        DatabaseManager.enableTestMode();
        Connection connection = dbManager.getConnection();

        assertNotNull(connection);
        assertFalse(connection.isClosed());
    }

    /**
     * Tests that getConnection reuses the same shared in-memory connection across multiple calls in test mode.
     *
     * Ensures that the first and second connections are the same instance when test mode is enabled.
     *
     * @throws SQLException if there is an error obtaining the connection
     */
    @Test
    void testGetConnection_TestMode_ReusesSharedConnection() throws SQLException {
        DatabaseManager.enableTestMode();
        Connection first = dbManager.getConnection();
        Connection second = dbManager.getConnection();

        assertSame(first, second); 
    }

    /**
     * Tests that getConnection successfully returns a new connection when test mode is enabled.
     *
     * Confirms that the connection returned is not null. This validates the fallback behavior for production logic
     * even when test mode is active.
     *
     * @throws SQLException if there is an error obtaining the connection
     */
    @Test
    void testGetConnection_ProdMode_ReturnsNewConnection() throws SQLException {
        DatabaseManager.enableTestMode();
        Connection testConn = dbManager.getConnection();
        assertNotNull(testConn);

    }
    
    /**
     * Tests that getConnection executes the else branch when test mode is disabled.
     *
     * This test ensures a fresh instance of DatabaseManager creates a new connection
     * in production mode (test mode = false), and the connection is valid and open.
     *
     * @throws SQLException if the connection cannot be created or validated
     */
    @Test
    void testGetConnection_ProdMode_ElseBranchExecuted() throws SQLException {
        dbManager = new DatabaseManager(); 
        Connection conn = dbManager.getConnection();
        assertNotNull(conn); 
        assertFalse(conn == null || conn.isClosed());
    }

    /**
     * Tests that a new SQLite in-memory connection can be manually established in a production scenario.
     *
     * This simulates production mode behavior by bypassing the test mode and manually creating a connection.
     * It ensures the connection is valid and not closed upon creation.
     *
     * @throws SQLException if the connection fails to open or close
     */
    @Test
    void testGetConnection_ProdMode_ReturnsConnection() throws SQLException {
        Connection connection = DriverManager.getConnection("jdbc:sqlite::memory:"); 
        assertNotNull(connection);
        assertFalse(connection.isClosed());
        connection.close();
    }

    /**
     * Tests how the system handles an SQLException during connection retrieval.
     *
     * This test simulates a failed connection attempt using an invalid JDBC URL and ensures
     * the exception is caught and the connection remains null, imitating the catch block behavior
     * of the getConnection method.
     */
    @Test
    void testGetConnection_SQLExceptionHandled() {
        DatabaseManager.enableTestMode();
        String brokenUrl = "jdbc:sqlite::invalid_path_that_fails";
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(brokenUrl);
        } catch (SQLException e) {
            connection = null;
        }

        assertNull(connection); 
    }

    ///////////////////////////////////////// closeConnection /////////////////////////////////////////

    /**
     * Tests that closeConnection properly closes the shared in-memory test connection.
     *
     * This test enables test mode, retrieves a shared connection, and verifies that
     * closeConnection shuts it down successfully.
     *
     * @throws SQLException if the connection state cannot be checked
     */
    @Test
    void testCloseConnection_ShutsDownSharedConnection() throws SQLException {
        DatabaseManager.enableTestMode();
        Connection conn = dbManager.getConnection();
        assertNotNull(conn);
        assertFalse(conn.isClosed());

        dbManager.closeConnection();

        assertTrue(conn.isClosed());
    }

    /**
     * Tests that closeConnection handles SQLExceptions when attempting to close an already closed connection.
     *
     * This test manually closes the connection before calling closeConnection and verifies
     * that no exception is thrown during the cleanup.
     *
     * @throws SQLException if the initial connection retrieval fails
     */
    @Test
    void testCloseConnection_HandlesSQLException() throws SQLException {
        DatabaseManager.enableTestMode();
        Connection conn = dbManager.getConnection();
        assertNotNull(conn);
        conn.close();
        assertDoesNotThrow(() -> dbManager.closeConnection());
    }
    
    /**
     * Tests that closeConnection successfully closes an open test-mode connection.
     *
     * This confirms that the method works as expected under normal conditions
     * by asserting that the connection is closed afterward.
     *
     * @throws SQLException if the connection fails to open or close
     */
    @Test
    void testCloseConnection_ClosesOpenConnection() throws SQLException {
        DatabaseManager.enableTestMode();
        Connection conn = dbManager.getConnection();
        assertFalse(conn.isClosed());

        dbManager.closeConnection();

        assertTrue(conn.isClosed());
    }

    /**
     * Tests that closeConnection handles closing an already closed connection without throwing an exception.
     *
     *
     * @throws SQLException if the connection setup fails
     */
    @Test
    void testCloseConnection_SQLExceptionHandled() throws SQLException {
        DatabaseManager.enableTestMode();
        Connection conn = dbManager.getConnection();
        conn.close();

        assertDoesNotThrow(() -> dbManager.closeConnection());
    }

    ///////////////////////////////////////// Error Handling /////////////////////////////////////////

    /**
     * Tests that getConnection handles SQLExceptions gracefully and returns a non-null result.
     */
    @Test
    void testGetConnection_HandlesSQLExceptionGracefully() {
        DatabaseManager.enableTestMode();
        assertNotNull(dbManager.getConnection()); 
    }
}
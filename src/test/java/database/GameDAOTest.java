package database;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never; 
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the GameDAO class.
 *
 * This test suite verifies the correct behavior of all public methods in the GameDAO class,
 * which handles SQL operations related to user-specific game data within the application.
 * Each test isolates DAO behavior using mocked JDBC components to ensure that SQL queries
 * are properly formed, executed, and handled under various success and failure scenarios.
 */
class GameDAOTest {

    private static GameDAO gameDAO;
    private static Connection sharedConnection;
    private PreparedStatement mockStatement;
    private ResultSet mockResultSet;
    private static DatabaseManager dbManager;
    private Connection mockConnection;
    
    /**
     * Initializes the shared in-memory test connection and GameDAO instance before all tests.
     *
     * This method is run once before any tests execute. It enables test mode for the
     * DatabaseManager and establishes a shared in-memory SQLite connection to simulate
     * production-like database interactions. The GameDAO instance is created using this
     * shared connection for integration-style testing.
     *
     * @throws SQLException if the test connection cannot be established
     */
    @BeforeAll
    static void initConnection() throws SQLException {
    	DatabaseManager.enableTestMode();
		dbManager = new DatabaseManager();
		sharedConnection = dbManager.getConnection();
		gameDAO = new GameDAO(sharedConnection);
	}
	
    /**
     * Sets up fresh mock objects before each individual test.
     *
     * This method mocks a JDBC Connection, PreparedStatement, and ResultSet.
     * It then creates a spy of the GameDAO using the mocked Connection,
     * allowing verification of interactions while keeping method logic intact.
     *
     * @throws SQLException if mocking or DAO construction fails
     */
    @BeforeEach
    void setUp() throws SQLException {
        mockConnection = mock(Connection.class);
        mockStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);

        gameDAO = spy(new GameDAO(mockConnection));
    }
    
    
    //////////////////////// testing getGameID ////////////////////////
    
    /**
     * Tests getGameID when the game exists in the database.
     *
     * This test verifies that getGameID correctly returns true when a game matching
     * the provided user ID and title exists in the result set. It mocks a successful
     * query execution and confirms that the correct parameters were set.
     *
     * @throws Exception if any unexpected database error occurs during execution
     */
    @Test
    void testGetGameID_GameExists() throws Exception {
        int userID = 1;
        String title = "Halo";

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        boolean result = gameDAO.getGameID(userID, title);
        assertTrue(result);
        verify(mockStatement).setInt(1, userID);
        verify(mockStatement).setString(2, title);
        verify(mockResultSet).next();
    }

    /**
     * Tests getGameID when the game does not exist in the database.
     *
     * This test ensures that getGameID returns false when the query executes successfully
     * but the ResultSet does not contain any matching rows for the given user ID and title.
     *
     * @throws Exception if any SQL operation throws unexpectedly
     */
    @Test
    void testGetGameID_GameDoesNotExist() throws Exception {
        int userID = 2;
        String title = "Minecraft";

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        boolean result = gameDAO.getGameID(userID, title);

        assertFalse(result);
        verify(mockStatement).setInt(1, userID);
        verify(mockStatement).setString(2, title);
        verify(mockResultSet).next();
    }
    
    /**
     * Tests getGameID when a SQLException occurs during statement preparation.
     *
     * This test simulates a failure in the prepareStatement phase and confirms that
     * getGameID handles the SQLException gracefully by returning false. It also verifies
     * that the prepareStatement method was called.
     *
     * @throws Exception if an unexpected error occurs during test execution
     */
    @Test
    void testGetGameID_SQLExceptionOnPrepareStatement() throws Exception {
        int userID = 3;
        String title = "ErrorGame";

        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Prepare failed"));

        boolean result = gameDAO.getGameID(userID, title);

        assertFalse(result);
        verify(mockConnection).prepareStatement(anyString());
    }

    /**
     * Tests getGameID when a SQLException is thrown during query execution.
     *
     * This test simulates a scenario where the SQL query is prepared successfully,
     * but an exception occurs during the executeQuery phase. It asserts that
     * getGameID returns false and that the exception is handled without propagation.
     *
     * @throws Exception if an unexpected error occurs during test execution
     */
    @Test
    void testGetGameID_SQLExceptionOnExecuteQuery() throws Exception {
        int userID = 4;
        String title = "CrashGame";

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenThrow(new SQLException("Execution failed"));

        boolean result = gameDAO.getGameID(userID, title);

        assertFalse(result);
        verify(mockStatement).setInt(1, userID);
        verify(mockStatement).setString(2, title);
        verify(mockStatement).executeQuery();
    }
    
    //////////////////////// testing getGamePltform ////////////////////////
    
    /**
     * Tests getGamePlatform when the platform exists in the database.
     *
     * This test verifies that getGamePlatform returns true when a matching platform
     * is found for the given user ID. It confirms that the query executes successfully
     * and the result set contains data.
     *
     * @throws SQLException if any unexpected SQL error occurs
     */
    @Test
    void testGetGamePlatform_PlatformExists() throws SQLException {
        int userID = 1;

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);

        boolean result = gameDAO.getGamePlatform(userID);

        assertTrue(result);
        verify(mockStatement).setInt(1, userID);
        verify(mockResultSet).next();
    }

    /**
     * Tests getGamePlatform when the platform does not exist in the database.
     *
     * This test ensures that getGamePlatform returns false when the result set does
     * not contain a matching platform record for the given user ID.
     *
     * @throws SQLException if any SQL operation throws unexpectedly
     */
    @Test
    void testGetGamePlatform_PlatformDoesNotExist() throws SQLException {
        int userID = 2;

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        boolean result = gameDAO.getGamePlatform(userID);

        assertFalse(result);
        verify(mockStatement).setInt(1, userID);
        verify(mockResultSet).next();
    }

    /**
     * Tests getGamePlatform when a SQLException occurs during statement preparation.
     *
     * This test simulates a failure during the prepareStatement phase and checks that
     * getGamePlatform safely returns false without propagating the exception.
     *
     * @throws SQLException if the test setup fails unexpectedly
     */
    @Test
    void testGetGamePlatform_SQLExceptionOnPrepareStatement() throws SQLException {
        int userID = 3;

        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Prepare failed"));

        boolean result = gameDAO.getGamePlatform(userID);

        assertFalse(result);
        verify(mockConnection).prepareStatement(anyString());
    }

    /**
     * Tests getGamePlatform when a SQLException is thrown during query execution.
     *
     * This test simulates a successful preparation of the SQL statement but throws a
     * SQLException during executeQuery. It verifies that getGamePlatform returns false
     * and the exception is handled gracefully.
     *
     * @throws SQLException if the test setup fails unexpectedly
     */
    @Test
    void testGetGamePlatform_SQLExceptionOnExecuteQuery() throws SQLException {
        int userID = 4;

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenThrow(new SQLException("Execute failed"));

        boolean result = gameDAO.getGamePlatform(userID);

        assertFalse(result);
        verify(mockStatement).setInt(1, userID);
        verify(mockStatement).executeQuery();
    }

    //////////////////////// testing updateCompletionStatus ////////////////////////
    
    /**
     * Tests updateCompletionStatus when the update operation is successful.
     *
     * This test verifies that the method correctly returns true when one row is updated
     * in the database for a user's game completion status.
     *
     * @throws SQLException if any unexpected error occurs during the test
     */
    @Test
    void testUpdateCompletionStatus_SuccessfulUpdate() throws SQLException {
        int userID = 1;
        int gameID = 10;
        String status = "Completed";

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeUpdate()).thenReturn(1);

        boolean result = gameDAO.updateCompletionStatus(userID, gameID, status);

        assertTrue(result);
        verify(mockStatement).setString(1, status);
        verify(mockStatement).setInt(2, userID);
        verify(mockStatement).setInt(3, gameID);
        verify(mockStatement).executeUpdate();
    }

    /**
     * Tests updateCompletionStatus when no rows are updated.
     *
     * This test ensures that the method returns false if the database update
     * does not affect any rows, indicating the game entry was not found or unchanged.
     *
     * @throws SQLException if any SQL operation fails unexpectedly
     */
    @Test
    void testUpdateCompletionStatus_NoRowsUpdated() throws SQLException {
        int userID = 2;
        int gameID = 20;
        String status = "In Progress";

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeUpdate()).thenReturn(0); 

        boolean result = gameDAO.updateCompletionStatus(userID, gameID, status);

        assertFalse(result);
        verify(mockStatement).setString(1, status);
        verify(mockStatement).setInt(2, userID);
        verify(mockStatement).setInt(3, gameID);
        verify(mockStatement).executeUpdate();
    }

    /**
     * Tests updateCompletionStatus when a SQLException occurs during statement preparation.
     *
     * This test confirms that the method returns false and does not throw an exception
     * if an error occurs while preparing the SQL update statement.
     *
     * @throws SQLException if an error is thrown during mocking or execution
     */
    @Test
    void testUpdateCompletionStatus_SQLExceptionOnPrepareStatement() throws SQLException {
        int userID = 3;
        int gameID = 30;
        String status = "Backlog";

        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Prepare failed"));

        boolean result = gameDAO.updateCompletionStatus(userID, gameID, status);

        assertFalse(result);
        verify(mockConnection).prepareStatement(anyString());
    }

    /**
     * Tests updateCompletionStatus when a SQLException is thrown during statement execution.
     *
     * This test simulates a scenario where the statement is prepared correctly, but an
     * exception occurs when calling executeUpdate. The method should return false.
     *
     * @throws SQLException if any part of the mock setup fails
     */
    @Test
    void testUpdateCompletionStatus_SQLExceptionOnExecuteUpdate() throws SQLException {
        int userID = 4;
        int gameID = 40;
        String status = "Dropped";

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeUpdate()).thenThrow(new SQLException("Execute failed"));

        boolean result = gameDAO.updateCompletionStatus(userID, gameID, status);

        assertFalse(result);
        verify(mockStatement).setString(1, status);
        verify(mockStatement).setInt(2, userID);
        verify(mockStatement).setInt(3, gameID);
        verify(mockStatement).executeUpdate();
    }
    
    
    /////////////////////// testing updateNotes ////////////////////////
    
    /**
     * Tests updateNotes when the note is successfully updated in the database.
     *
     * This test confirms that the method returns true when one row is updated, indicating
     * the user's note for the specified game was successfully saved.
     *
     * @throws SQLException if an unexpected error occurs during the test
     */
    @Test
    void testUpdateNotes_SuccessfulUpdate() throws SQLException {
		int userID = 1;
		int gameID = 10;
		String notes = "Great game!";

		when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
		when(mockStatement.executeUpdate()).thenReturn(1); 

		boolean result = gameDAO.updateNotes(userID, gameID, notes);

		assertTrue(result);
		verify(mockStatement).setString(1, notes);
		verify(mockStatement).setInt(2, userID);
		verify(mockStatement).setInt(3, gameID);
		verify(mockStatement).executeUpdate();
	}
    
    /**
     * Tests updateNotes when no rows are updated.
     *
     * This test verifies that the method returns false when the note update does not affect
     * any records, suggesting the game entry may not exist for the user.
     *
     * @throws SQLException if any SQL operation fails during execution
     */
    @Test
    void testUpdateNotes_NoRowsUpdated() throws SQLException {
    	int userID = 1;
    	int gameID = 20;
    	String notes = "Not my type of game";

		when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
		when(mockStatement.executeUpdate()).thenReturn(0);
		
		boolean result = gameDAO.updateNotes(userID, gameID, notes);
		
		assertFalse(result);
		verify(mockStatement).setString(1, notes);
		verify(mockStatement).setInt(2, userID);
		verify(mockStatement).setInt(3, gameID);
		verify(mockStatement).executeUpdate();
    }
    
    /**
     * Tests updateNotes when a SQLException occurs during statement preparation.
     *
     * This test simulates a failure in the prepareStatement phase and ensures the method
     * returns false without throwing an exception.
     *
     * @throws SQLException if an error is thrown during mocking
     */
    @Test
    void testUpdateNotes_SQLExceptionOnPrepareStatement() throws SQLException {
		int userID = 3;
		int gameID = 30;
		String notes = "Error game";
		
		when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Prepare failed"));
		boolean result = gameDAO.updateNotes(userID, gameID, notes);
		assertFalse(result);
		verify(mockConnection).prepareStatement(anyString());
    }
    
    /**
     * Tests updateNotes when a SQLException is thrown during update execution.
     *
     * This test ensures the method handles SQL exceptions during executeUpdate
     * and returns false to indicate failure.
     *
     * @throws SQLException if mocking or interaction fails
     */
    @Test
    void testUpdateNotes_SQLExceptionOnExecuteUpdate() throws SQLException {
    	int userID = 4;
    	int gameID = 40;
    	String notes = "Error game";
    	
    	when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
		when(mockStatement.executeUpdate()).thenThrow(new SQLException("Execute failed"));
		
		boolean result = gameDAO.updateNotes(userID, gameID, notes);
		assertFalse(result);
		verify(mockStatement).setString(1, notes);
		verify(mockStatement).setInt(2, userID);
		verify(mockStatement).setInt(3, gameID);
		verify(mockStatement).executeUpdate();
    }
    
    /////////////////////// testing updateCoverImageURl ////////////////////////
    
    /**
     * Tests updateCoverImageURL when the cover image URL is successfully updated.
     *
     * This test verifies that the method returns true when a user's cover image URL
     * is updated for the specified game entry in the database.
     *
     * @throws SQLException if any SQL error occurs during the test
     */
    @Test
    void testUpdateCoverImageURL_SuccessfulUpdate() throws SQLException {
    	int userID = 1;
    	int gameID = 10;
    	String coverImageURL = "http://example.com/image.jpg";
    	
    	when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
		when(mockStatement.executeUpdate()).thenReturn(1);
		
		boolean result = gameDAO.updateCoverImageURL(userID, gameID, coverImageURL);
		
		assertTrue(result);
		verify(mockStatement).setString(1, coverImageURL);
		verify(mockStatement).setInt(2, userID);
		verify(mockStatement).setInt(3, gameID);
		verify(mockStatement).executeUpdate();
    }
    
    /**
     * Tests updateCoverImageURL when no rows are updated.
     *
     * This test verifies that the method returns false if the database update does not
     * affect any records, which may indicate that the game entry does not exist.
     *
     * @throws SQLException if a SQL error occurs during the test
     */
    @Test 
    void testUpdateCoverImageURL_NoRowsUpdated() throws SQLException {
		int userID = 1;
		int gameID = 20;
		String coverImageURL = "http://example.com/image.jpg";
		
		when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
		when(mockStatement.executeUpdate()).thenReturn(0);
		boolean result = gameDAO.updateCoverImageURL(userID, gameID, coverImageURL);
		assertFalse(result);
		verify(mockStatement).setString(1, coverImageURL);
		verify(mockStatement).setInt(2, userID);
		verify(mockStatement).setInt(3, gameID);
		verify(mockStatement).executeUpdate();
	}
	
    /**
     * Tests updateCoverImageURL when a SQLException occurs during statement preparation.
     *
     * This test simulates a failure in the prepareStatement phase and verifies that the method
     * handles the exception gracefully and returns false.
     *
     * @throws SQLException if an error is thrown during mocking
     */
	@Test
	void testUpdateCoverImageURL_SQLExceptionOnPrepareStatement() throws SQLException {
		int userID = 3;
		int gameID = 30;
		String coverImageURL = "http://example.com/image.jpg";
		
		when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Prepare failed"));
		boolean result = gameDAO.updateCoverImageURL(userID, gameID, coverImageURL);
		assertFalse(result);
		verify(mockConnection).prepareStatement(anyString());
		
	}
	
	/**
	 * Tests updateCoverImageURL when a SQLException is thrown during update execution.
	 *
	 * This test ensures the method returns false and handles the exception without crashing
	 * when executeUpdate fails after successful statement preparation.
	 *
	 * @throws SQLException if any mock interaction fails
	 */
	@Test
	void testUpdateCoverImageURL_SQLExceptionOnExecuteUpdate() throws SQLException {
		int userID = 4;
		int gameID = 40;
		String coverImageURL = "http://example.com/image.jpg";
		
		when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
		when(mockStatement.executeUpdate()).thenThrow(new SQLException("Execute failed"));
		
		boolean result = gameDAO.updateCoverImageURL(userID, gameID, coverImageURL);
		assertFalse(result);
		verify(mockStatement).setString(1, coverImageURL);
		verify(mockStatement).setInt(2, userID);
		verify(mockStatement).setInt(3, gameID);
		verify(mockStatement).executeUpdate();
	}
	
	//////////////////////////// testing getCoverImageURL ////////////////////////////
	
	/**
	 * Tests getCoverImageURL when the cover image URL exists in the database.
	 *
	 * This test ensures that the correct URL is returned when a matching game record
	 * is found for the given user ID and game ID.
	 *
	 * @throws SQLException if a SQL error occurs during the test
	 */
    @Test
    void testGetCoverImageURL_Exists() throws SQLException {
        int userID = 1;
        int gameID = 101;
        String expectedURL = "http://image.com/cover.jpg";

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString("coverArt")).thenReturn(expectedURL);

        String result = gameDAO.getCoverImageURL(userID, gameID);

        assertEquals(expectedURL, result);
        verify(mockStatement).setInt(1, userID);
        verify(mockStatement).setInt(2, gameID);
        verify(mockResultSet).next();
        verify(mockResultSet).getString("coverArt");
    }

    /**
     * Tests getCoverImageURL when no matching cover image URL is found.
     *
     * This test verifies that the method returns null if no game entry matches the given
     * user ID and game ID, and getString is never called.
     *
     * @throws SQLException if an error occurs during the test setup or execution
     */
    @Test
    void testGetCoverImageURL_NotFound() throws SQLException {
        int userID = 2;
        int gameID = 202;

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false); 

        String result = gameDAO.getCoverImageURL(userID, gameID);

        assertNull(result);
        verify(mockStatement).setInt(1, userID);
        verify(mockStatement).setInt(2, gameID);
        verify(mockResultSet).next();
        verify(mockResultSet, never()).getString("coverArt");
    }

    /**
     * Tests getCoverImageURL when a SQLException is thrown during statement preparation.
     *
     * This test simulates a failure in the prepareStatement call and ensures the method
     * returns null without throwing an exception.
     *
     * @throws SQLException if mocking fails
     */
    @Test
    void testGetCoverImageURL_SQLExceptionOnPrepareStatement() throws SQLException {
        int userID = 3;
        int gameID = 303;

        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Prepare failed"));

        String result = gameDAO.getCoverImageURL(userID, gameID);

        assertNull(result);
        verify(mockConnection).prepareStatement(anyString());
    }

    /**
     * Tests getCoverImageURL when a SQLException is thrown during executeQuery.
     *
     * This test confirms that if a SQL exception occurs while executing the query,
     * the method returns null and handles the error gracefully.
     *
     * @throws SQLException if mocking fails
     */
    @Test
    void testGetCoverImageURL_SQLExceptionOnExecuteQuery() throws SQLException {
        int userID = 4;
        int gameID = 404;

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenThrow(new SQLException("Execute failed"));

        String result = gameDAO.getCoverImageURL(userID, gameID);

        assertNull(result);
        verify(mockStatement).setInt(1, userID);
        verify(mockStatement).setInt(2, gameID);
        verify(mockStatement).executeQuery();
    }
	
    /////////////////////////// testing updateReleaseDate //////////////////////////
    
    /**
     * Tests updateReleaseDate when the update is successful.
     *
     * This test ensures the method returns true when the release date is updated
     * for a specific game and user.
     *
     * @throws SQLException if any error occurs during the test
     */
    @Test
    void testUpdateReleaseDate_SuccessfulUpdate() throws SQLException {
		int userID = 1;
		int gameID = 10;
		String releaseDate = "2023-10-01";

		when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
		when(mockStatement.executeUpdate()).thenReturn(1);

		boolean result = gameDAO.updateReleaseDate(userID, gameID, releaseDate);

		assertTrue(result);
		verify(mockStatement).setString(1, releaseDate);
		verify(mockStatement).setInt(2, userID);
		verify(mockStatement).setInt(3, gameID);
		verify(mockStatement).executeUpdate();
	}
    
    /**
     * Tests updateReleaseDate when no rows are updated.
     *
     * This test verifies that the method returns false if the update did not
     * affect any rows, which could indicate the game or user does not exist.
     *
     * @throws SQLException if a SQL error occurs
     */
    @Test
    void testUpdateReleaseDate_NoRowsUpdated() throws SQLException {
		int userID = 1;
		int gameID = 20;
		String releaseDate = "2023-10-01";

		when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
		when(mockStatement.executeUpdate()).thenReturn(0);
		boolean result = gameDAO.updateReleaseDate(userID, gameID, releaseDate);
		assertFalse(result);
		verify(mockStatement).setString(1, releaseDate);
		verify(mockStatement).setInt(2, userID);
		verify(mockStatement).setInt(3, gameID);
		verify(mockStatement).executeUpdate();
    }
    
    /**
     * Tests updateReleaseDate when a SQLException occurs during statement preparation.
     *
     * This test ensures the method returns false and handles the exception gracefully
     * when prepareStatement fails.
     *
     * @throws SQLException if any SQL-related mock behavior fails
     */
    @Test
    void testUpdateReleaseDate_SQLExceptionOnPrepareStatement() throws SQLException {
    	int userID = 3;
    	int gameID = 30;
    	String releaseDate = "2023-10-01";
    	
		when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Prepare failed"));
		boolean result = gameDAO.updateReleaseDate(userID, gameID, releaseDate);
		
		assertFalse(result);
		verify(mockConnection).prepareStatement(anyString());
		
    }
    
    /**
     * Tests updateReleaseDate when a SQLException is thrown during executeUpdate.
     *
     * This test confirms that the method returns false and properly handles an exception
     * if an error occurs while executing the SQL update.
     *
     * @throws SQLException if mock setup fails
     */
    @Test
    void testUpdateReleaseDate_SQLExceptionOnExecuteUpdate() throws SQLException {
		int userID = 4;
		int gameID = 40;
		String releaseDate = "2023-10-01";
		
		when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
		when(mockStatement.executeUpdate()).thenThrow(new SQLException("Execute failed"));
		boolean result = gameDAO.updateReleaseDate(userID, gameID, releaseDate);
		
		assertFalse(result);
		verify(mockStatement).setString(1, releaseDate);
		verify(mockStatement).setInt(2, userID);
		verify(mockStatement).setInt(3, gameID);
    }
    
    ////////////////////////// testing updateGenre //////////////////////////
    
    /**
     * Tests updateGenre when the update is successful.
     *
     * This test ensures the method returns true when the genre for a specific game and user
     * is updated correctly in the database.
     *
     * @throws SQLException if a SQL error occurs
     */
    @Test
    void testUpdateGenre_SuccessfulUpdate() throws SQLException {
    	int userID = 1;
    	int gameID = 10;
    	String genre = "Action";

		when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
		when(mockStatement.executeUpdate()).thenReturn(1);
		boolean result = gameDAO.updateGenre(userID, gameID, genre);
		assertTrue(result);
		verify(mockStatement).setString(1, genre);
		verify(mockStatement).setInt(2, userID);
		verify(mockStatement).setInt(3, gameID);
		verify(mockStatement).executeUpdate();
    }
    
    /**
     * Tests updateGenre when no rows are updated.
     *
     * This test verifies that the method returns false if the update does not affect any
     * records, possibly due to a missing game or user entry.
     *
     * @throws SQLException if a SQL error occurs during the test
     */
    @Test
    void testUpdateGenre_NoRowsUpdated() throws SQLException {
		int userID = 1;
		int gameID = 20;
		String genre = "Adventure";
		when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
		when(mockStatement.executeUpdate()).thenReturn(0);
		boolean result = gameDAO.updateGenre(userID, gameID, genre);
		assertFalse(result);
		verify(mockStatement).setString(1, genre);
		verify(mockStatement).setInt(2, userID);
		verify(mockStatement).setInt(3, gameID);
		verify(mockStatement).executeUpdate();
	}
	
    /**
     * Tests updateGenre when a SQLException is thrown during prepareStatement.
     *
     * This test ensures that the method handles preparation failures gracefully and
     * returns false without throwing an exception.
     *
     * @throws SQLException if mock setup fails
     */
	@Test
	void testUpdateGenre_SQLExceptionOnPrepareStatement() throws SQLException {
		int userID = 3;
		int gameID = 30;
		String genre = "RPG";
		
		when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Prepare failed"));
		boolean result = gameDAO.updateGenre(userID, gameID, genre);
		assertFalse(result);
		verify(mockConnection).prepareStatement(anyString());
	}
	
	/**
	 * Tests updateGenre when a SQLException is thrown during executeUpdate.
	 *
	 * This test verifies that the method returns false when an error occurs
	 * while executing the SQL update.
	 *
	 * @throws SQLException if mock setup fails
	 */
	@Test
	void testUpdateGenre_SQLExceptionOnExecuteUpdate() throws SQLException {
		int userID = 4;
		int gameID = 40;
		String genre = "Strategy";
		
		when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
		when(mockStatement.executeUpdate()).thenThrow(new SQLException("Execute failed"));
		
		boolean result = gameDAO.updateGenre(userID, gameID, genre);
		assertFalse(result);
		verify(mockStatement).setString(1, genre);
		verify(mockStatement).setInt(2, userID);
		verify(mockStatement).setInt(3, gameID);
	}
	
	//////////////////////// testing updateDeveloper ////////////////////////
	
	/**
	 * Tests updateDeveloper when the update is successful.
	 *
	 * This test verifies that the method returns true when the developer name
	 * is successfully updated in the database for the given user and game.
	 *
	 * @throws SQLException if the SQL preparation or update fails
	 */
	@Test
	void testUpdateDeveloper_SuccessfulUpdate() throws SQLException {
		int userID = 1;
		int gameID = 10;
		String developer = "Epic Games";

		when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
		when(mockStatement.executeUpdate()).thenReturn(1);
		boolean result = gameDAO.updateDeveloper(userID, gameID, developer);
		assertTrue(result);
		verify(mockStatement).setString(1, developer);
		verify(mockStatement).setInt(2, userID);
		verify(mockStatement).setInt(3, gameID);
		verify(mockStatement).executeUpdate();
	}
	
	/**
	 * Tests updateDeveloper when no rows are updated.
	 *
	 * This test ensures the method returns false when the update statement
	 * does not affect any records, indicating the user or game may not exist.
	 *
	 * @throws SQLException if the SQL setup fails
	 */
	@Test 
	void testUpdateDeveloper_NoRowsUpdated() throws SQLException {
		int userID = 1;
		int gameID = 20;
		String developer = "Ubisoft";
		
		when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
		when(mockStatement.executeUpdate()).thenReturn(0);
		boolean result = gameDAO.updateDeveloper(userID, gameID, developer);
		assertFalse(result);
		verify(mockStatement).setString(1, developer);
		verify(mockStatement).setInt(2, userID);
		verify(mockStatement).setInt(3, gameID);
		verify(mockStatement).executeUpdate();
	}
	
	/**
	 * Tests updateDeveloper when a SQLException is thrown during prepareStatement.
	 *
	 * This test verifies that the method safely handles preparation failures
	 * and returns false without crashing.
	 *
	 * @throws SQLException simulated during prepareStatement
	 */
	@Test
	void testUpdateDeveloper_SQLExceptionOnPrepareStatement() throws SQLException{
		int userID = 3;
		int gameID = 30;
		String developer = "Valve";
		
		when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Prepare failed"));
		boolean result = gameDAO.updateDeveloper(userID, gameID, developer);
		
		assertFalse(result);
		verify(mockConnection).prepareStatement(anyString());
	}
	
	/**
	 * Tests updateDeveloper when a SQLException is thrown during prepareStatement.
	 *
	 * This test verifies that the method safely handles preparation failures
	 * and returns false without crashing.
	 *
	 * @throws SQLException simulated during prepareStatement
	 */
	@Test 
	void testUpdateDeveloper_SQLExceptionOnExecuteUpdate() throws SQLException {
		int userID = 4;
		int gameID = 40;
		String developer = "Naughty Dog";
		
		when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
		when(mockStatement.executeUpdate()).thenThrow(new SQLException("Execute failed"));
		
		boolean result = gameDAO.updateDeveloper(userID, gameID, developer);
		assertFalse(result);
		verify(mockStatement).setString(1, developer);
		verify(mockStatement).setInt(2, userID);
		verify(mockStatement).setInt(3, gameID);
	}
	
	//////////////////////// testing updatePublisher ////////////////////////
	
	/**
	 * Tests updatePublisher when the update is successful.
	 *
	 * This test confirms that the method returns true when the publisher
	 * value is updated correctly in the database.
	 *
	 * @throws SQLException if the SQL operation fails
	 */
	@Test
	void testUpdatePublisher_SuccessfulUpdate() throws SQLException {
		int userID = 1;
		int gameID = 10;
		String publisher = "Activision";

		when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
		when(mockStatement.executeUpdate()).thenReturn(1);
		boolean result = gameDAO.updatePublisher(userID, gameID, publisher);
		assertTrue(result);
		verify(mockStatement).setString(1, publisher);
		verify(mockStatement).setInt(2, userID);
		verify(mockStatement).setInt(3, gameID);
		verify(mockStatement).executeUpdate();
	}
	
	/**
	 * Tests updatePublisher when no rows are updated.
	 *
	 * This test checks that the method returns false if the update doesn't
	 * affect any rows, possibly due to invalid user or game IDs.
	 *
	 * @throws SQLException if the SQL setup fails
	 */
	@Test
	void testUpdatePublisher_NoRowsUpdated() throws SQLException {
		int userID = 1;
		int gameID = 20;
		String publisher = "Electronic Arts";

		when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
		when(mockStatement.executeUpdate()).thenReturn(0);
		boolean result = gameDAO.updatePublisher(userID, gameID, publisher);
		assertFalse(result);
		verify(mockStatement).setString(1, publisher);
		verify(mockStatement).setInt(2, userID);
		verify(mockStatement).setInt(3, gameID);
		verify(mockStatement).executeUpdate();
	}
	
	/**
	 * Tests updatePublisher when a SQLException is thrown during prepareStatement.
	 *
	 * This test ensures the method gracefully handles a failure during the preparation of the SQL
	 * statement and returns false without throwing an exception.
	 *
	 * @throws SQLException simulated during prepareStatement
	 */
	@Test
	void testUpdatePublisher_SQLExceptionOnPrepareStatement() throws SQLException {
		int userID = 3;
		int gameID = 30;
		String publisher = "Square Enix";

		when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Prepare failed"));
		boolean result = gameDAO.updatePublisher(userID, gameID, publisher);
		assertFalse(result);
		verify(mockConnection).prepareStatement(anyString());
	}
	
	/**
	 * Tests updatePublisher when a SQLException is thrown during executeUpdate.
	 *
	 * This test verifies that the method returns false when an error occurs
	 * while executing the SQL update for the publisher field.
	 *
	 * @throws SQLException simulated during executeUpdate
	 */
	@Test
	void testUpdatePublisher_SQLExceptionOnExecuteUpdate() throws SQLException {
		int userID = 4;
		int gameID = 40;
		String publisher = "Bandai Namco";

		when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
		when(mockStatement.executeUpdate()).thenThrow(new SQLException("Execute failed"));

		boolean result = gameDAO.updatePublisher(userID, gameID, publisher);
		assertFalse(result);
		verify(mockStatement).setString(1, publisher);
		verify(mockStatement).setInt(2, userID);
		verify(mockStatement).setInt(3, gameID);
	}
	
	//////////////////////// testing updateTitle ////////////////////////
	
	/**
	 * Tests updateTitle when the update is successful.
	 *
	 * This test checks that the method returns true when the title is successfully
	 * updated in the database for the specified game and user.
	 *
	 * @throws SQLException if a SQL error occurs during the test
	 */
	@Test
	void testUpdateTitle_SuccessfulUpdate() throws SQLException {
		int userID = 1;
		int gameID = 10;
		String title = "The Last of Us";

		when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
		when(mockStatement.executeUpdate()).thenReturn(1);
		boolean result = gameDAO.updateTitle(userID, gameID, title);
		assertTrue(result);
		verify(mockStatement).setString(1, title);
		verify(mockStatement).setInt(2, userID);
		verify(mockStatement).setInt(3, gameID);
		verify(mockStatement).executeUpdate();
	}
	
	/**
	 * Tests updateTitle when no rows are updated.
	 *
	 * This test ensures the method returns false if the update does not affect
	 * any rows, indicating the game entry might not exist.
	 *
	 * @throws SQLException if a SQL error occurs during the test
	 */
	@Test
	void testUpdateTitle_NoRowsUpdated() throws SQLException {
		int userID = 1;
		int gameID = 20;
		String title = "God of War";

		when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
		when(mockStatement.executeUpdate()).thenReturn(0);
		boolean result = gameDAO.updateTitle(userID, gameID, title);
		assertFalse(result);
		verify(mockStatement).setString(1, title);
		verify(mockStatement).setInt(2, userID);
		verify(mockStatement).setInt(3, gameID);
		verify(mockStatement).executeUpdate();
	}
	
	/**
	 * Tests updateTitle when a SQLException is thrown during prepareStatement.
	 *
	 * This test confirms that the method handles the SQL preparation failure
	 * correctly and returns false.
	 *
	 * @throws SQLException simulated during prepareStatement
	 */
	@Test 
	void testUpdateTitle_SQLExceptionOnPrepareStatement() throws SQLException {
		int userID = 3;
		int gameID = 30;
		String title = "Final Fantasy";

		when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Prepare failed"));
		boolean result = gameDAO.updateTitle(userID, gameID, title);
		assertFalse(result);
		verify(mockConnection).prepareStatement(anyString());
	}
	
	/**
	 * Tests updateTitle when a SQLException is thrown during executeUpdate.
	 *
	 * This test ensures the method properly handles an exception occurring during
	 * execution of the update statement and returns false.
	 *
	 * @throws SQLException simulated during executeUpdate
	 */
	@Test
	void testUpdateTitle_SQLExceptionOnExecuteUpdate() throws SQLException {
		int userID = 4;
		int gameID = 40;
		String title = "Zelda";

		when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
		when(mockStatement.executeUpdate()).thenThrow(new SQLException("Execute failed"));

		boolean result = gameDAO.updateTitle(userID, gameID, title);
		assertFalse(result);
		verify(mockStatement).setString(1, title);
		verify(mockStatement).setInt(2, userID);
		verify(mockStatement).setInt(3, gameID);
	}
	
	//////////////////////// testing updatePlatform ////////////////////////
	
	/**
	 * Tests updatePlatform when the update is successful.
	 *
	 * This test verifies that the method returns true when the platform field is
	 * successfully updated in the database for the specified user and game.
	 *
	 * @throws SQLException if any error occurs during the test
	 */
	@Test
	void testUpdatePlatform_SuccessfulUpdate() throws SQLException {
		int userID = 1;
		int gameID = 10;
		String platforms = "PC, PS5";

		when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
		when(mockStatement.executeUpdate()).thenReturn(1);
		boolean result = gameDAO.updatePlatform(userID, gameID, platforms);
		assertTrue(result);
		verify(mockStatement).setString(1, platforms);
		verify(mockStatement).setInt(2, userID);
		verify(mockStatement).setInt(3, gameID);
		verify(mockStatement).executeUpdate();
	}
	
	/**
	 * Tests updatePlatform when no rows are updated.
	 *
	 * This test ensures that the method returns false when the SQL update
	 * affects no rows, indicating the target game record may not exist.
	 *
	 * @throws SQLException if any error occurs during the test
	 */
	@Test
	void testUpdatePlatform_NoRowsUpdated() throws SQLException {
		int userID = 1;
		int gameID = 20;
		String platforms = "Xbox, Switch";

		when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
		when(mockStatement.executeUpdate()).thenReturn(0);
		boolean result = gameDAO.updatePlatform(userID, gameID, platforms);
		assertFalse(result);
		verify(mockStatement).setString(1, platforms);
		verify(mockStatement).setInt(2, userID);
		verify(mockStatement).setInt(3, gameID);
		verify(mockStatement).executeUpdate();
	}
	
	/**
	 * Tests updatePlatform when a SQLException is thrown during prepareStatement.
	 *
	 * This test ensures that the method handles the SQL preparation error gracefully
	 * and returns false without propagating the exception.
	 *
	 * @throws SQLException simulated during prepareStatement
	 */
	@Test
	void testUpdatePlatform_SQLExceptionOnPrepareStatement() throws SQLException {
		int userID = 3;
		int gameID = 30;
		String platforms = "PS4, Xbox";

		when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Prepare failed"));
		boolean result = gameDAO.updatePlatform(userID, gameID, platforms);
		assertFalse(result);
		verify(mockConnection).prepareStatement(anyString());
	}
	
	/**
	 * Tests updatePlatform when a SQLException is thrown during executeUpdate.
	 *
	 * This test ensures the method handles execution failure of the SQL statement
	 * and returns false as expected.
	 *
	 * @throws SQLException simulated during executeUpdate
	 */
	@Test
	void testUpdatePlatform_SQLExceptionOnExecuteUpdate() throws SQLException {
		int userID = 4;
		int gameID = 40;
		String platforms = "Switch, PC";

		when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
		when(mockStatement.executeUpdate()).thenThrow(new SQLException("Execute failed"));

		boolean result = gameDAO.updatePlatform(userID, gameID, platforms);
		assertFalse(result);
		verify(mockStatement).setString(1, platforms);
		verify(mockStatement).setInt(2, userID);
		verify(mockStatement).setInt(3, gameID);
	}
}


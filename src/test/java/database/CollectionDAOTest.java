package database;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn; 
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import models.game;

/**
 * Unit tests for the CollectionDAO class using JUnit 5 and Mockito.
 * This test class validates all public methods including:
 *     Adding games to the user's collection
 *     Removing games from the user's collection
 *     Searching, filtering, and sorting games
 *     Checking existence of games in the database or user collection
 *     Retrieving metadata like platforms and genres
 */
class CollectionDAOTest {

	private static CollectionDAO collectionDAO;
	private static DatabaseManager dbManager;
	private static Connection sharedConnection;
    private Connection mockConnection;
    private PreparedStatement mockStmt1;
    private PreparedStatement mockStmt2;
    private ResultSet mockGeneratedKeys;


    /**
     * Initializes a shared connection for integration-style constructor tests.
     * Runs once before all tests.
     *
     * @throws SQLException if test connection setup fails
     */
	@BeforeAll
	static void initConnection() throws SQLException {
	    DatabaseManager.enableTestMode();
	    dbManager = new DatabaseManager();
	    sharedConnection = dbManager.getConnection();
	    collectionDAO = new CollectionDAO(sharedConnection);
	}

    /**
     * Sets up fresh mocks and spies before each test case.
     *
     * @throws SQLException if mock setup fails
     */
    @BeforeEach
    void setUp() throws SQLException {
        mockConnection = mock(Connection.class);
        mockStmt1 = mock(PreparedStatement.class);
        mockStmt2 = mock(PreparedStatement.class);
        mockGeneratedKeys = mock(ResultSet.class);   
        collectionDAO = spy(new CollectionDAO(mockConnection));
    }
    
    ////////////////////// testing CollectionDAO constructor ///////////////////////
    
    /**
     * Tests that the default constructor of CollectionDAO properly initializes a database connection.
     * 
     * @throws Exception if connection setup fails
     */
    @Test
    void testDefaultConstructor_initializesConnection() throws Exception {
        CollectionDAO dao = new CollectionDAO();
        var connectionField = CollectionDAO.class.getDeclaredField("connection");
        connectionField.setAccessible(true);
        Connection conn = (Connection) connectionField.get(dao);
        assertNotNull(conn, "Connection should be initialized by default constructor");
        assertFalse(conn.isClosed(), "Connection should be open");
        conn.close();
    }

    
    ////////////////////// testing addGameToCollection //////////////////////
	
    /**
     * Tests successful addition of a manually entered game to the user’s collection.
     * 
     * @throws Exception if SQL operations fail
     */
    @Test
    void testAddGameToCollection_manualEntry_success() throws Exception {
        when(mockConnection.prepareStatement(anyString(), eq(PreparedStatement.RETURN_GENERATED_KEYS))).thenReturn(mockStmt1);
        when(mockStmt1.getGeneratedKeys()).thenReturn(mockGeneratedKeys);
        when(mockGeneratedKeys.next()).thenReturn(true);
        when(mockGeneratedKeys.getInt(1)).thenReturn(999);
        when(mockConnection.prepareStatement(contains("UserGameCollection"))).thenReturn(mockStmt2);

        doReturn(false).when(collectionDAO).userAlreadyHasGame(1, 999);
        boolean result = collectionDAO.addGameToCollection(1, 0, "Mock Game", "Dev", "Pub", "2024-01-01", "Genre", "PC", "In Progress", "notes", "cover");

        assertTrue(result);
        verify(mockStmt1).executeUpdate();
        verify(mockStmt2).executeUpdate();
        verify(mockConnection).commit();
    }
    
    /**
	 * Tests that adding a game with an existing GameID fails gracefully.
	 * 
	 * @throws Exception if SQL operations fail
	 */
    @Test
    void testAddGameToCollection_apiEntry_alreadyExists() throws Exception {
        doReturn(true).when(collectionDAO).gameExists(123);
        doReturn(true).when(collectionDAO).userAlreadyHasGame(2, 123);
        when(mockConnection.prepareStatement(contains("UserGameCollection"))).thenReturn(mockStmt2);
        boolean result = collectionDAO.addGameToCollection(2, 123, "API Game", "Dev", "Pub", "2024-01-01", "Genre", "PC", "Done", "notes", "cover");
        assertFalse(result); 
        verify(mockConnection).commit();
    }
    
    /**
     * Tests the case where inserting a new game fails due to a missing generated key.
     * 
     * @throws Exception if SQL operations fail
     */
    @Test
    void testAddGameToCollection_gameInsertFails_noGeneratedKey() throws Exception {
        when(mockConnection.prepareStatement(anyString(), eq(PreparedStatement.RETURN_GENERATED_KEYS))).thenReturn(mockStmt1);
        when(mockStmt1.getGeneratedKeys()).thenReturn(mockGeneratedKeys);
        when(mockGeneratedKeys.next()).thenReturn(false);
        boolean result = collectionDAO.addGameToCollection(3, 0, "Fail Game", "Dev", "Pub", "2024-01-01", "Genre", "PC", "Done", "notes", "cover");
        assertFalse(result);
    }

    /**
     * Tests the handling of SQL exceptions during the add game process.
     * 
     * @throws Exception if SQL operations fail
     */
    @Test
    void testAddGameToCollection_sqlExceptionOccurs() throws Exception {
        when(mockConnection.prepareStatement(anyString(), anyInt())).thenThrow(new SQLException("Test SQL exception"));

        boolean result = collectionDAO.addGameToCollection(4, 0, "Exception Game", "Dev", "Pub", "2024-01-01", "Genre", "PC", "Done", "notes", "cover");

        assertFalse(result);
    }
    
    /////////////////////// testing removeGameFromCollection //////////////////////
    
    /**
     * Tests successful removal of a game from both UserGameCollection and Game tables.
     * 
     * @throws Exception if SQL operations fail
     */
    @Test
    void testRemoveGameFromCollection_success() throws Exception {
        int userId = 1;
        int gameId = 100;

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStmt1).thenReturn(mockStmt2);
        when(mockStmt1.executeUpdate()).thenReturn(1);  
        when(mockStmt2.executeUpdate()).thenReturn(1);  

        boolean result = collectionDAO.removeGameFromCollection(userId, gameId);

        assertTrue(result, "Should return true when deletion succeeds");

        verify(mockConnection).setAutoCommit(false);
        verify(mockStmt1).setInt(1, userId);
        verify(mockStmt1).setInt(2, gameId);
        verify(mockStmt1).executeUpdate();

        verify(mockStmt2).setInt(1, gameId);
        verify(mockStmt2).executeUpdate();

        verify(mockConnection).commit();
    }

    /**
     * Tests rollback behavior when a SQLException occurs after auto-commit is disabled.
     * 
     * @throws Exception if SQL operations fail
     */
    @Test
    void testRemoveGameFromCollection_innerSQLException_triggersRollback() throws Exception {
        int userId = 2;
        int gameId = 200;

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStmt1);
        when(mockStmt1.executeUpdate()).thenThrow(new SQLException("Simulated inner error"));

        boolean result = collectionDAO.removeGameFromCollection(userId, gameId);

        assertFalse(result, "Should return false on exception");

        verify(mockConnection).setAutoCommit(false);
        verify(mockConnection).rollback();
        verify(mockConnection, never()).commit();
    }

    /**
     * Tests failure when the connection setup for transaction control fails.
     * 
     * @throws Exception if SQL operations fail
     */
    @Test
    void testRemoveGameFromCollection_outerSQLException() throws Exception {
        int userId = 3;
        int gameId = 300;

        doThrow(new SQLException("Simulated outer error")).when(mockConnection).setAutoCommit(false);

        boolean result = collectionDAO.removeGameFromCollection(userId, gameId);

        assertFalse(result, "Should return false if connection setup fails");

        verify(mockConnection).setAutoCommit(false);
        verify(mockConnection, never()).commit();
    }

    /**
     * Tests behavior when user-game is deleted but no game is deleted.
     * 
     * @throws Exception if SQL operations fail
     */
    @Test
    void testRemoveGameFromCollection_noRowsDeleted() throws Exception {
        int userId = 4;
        int gameId = 400;

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStmt1).thenReturn(mockStmt2);
        when(mockStmt1.executeUpdate()).thenReturn(1); // user collection entry deleted
        when(mockStmt2.executeUpdate()).thenReturn(0); // no game deleted

        boolean result = collectionDAO.removeGameFromCollection(userId, gameId);

        assertFalse(result, "Should return false if game was not deleted");

        verify(mockConnection).commit();
    }
    
    /////////////////////// testing getGameID ///////////////////////////////////////////////////
    

    /**
     * Tests getGameID returns true when a matching game is found.
     * 
     * @throws Exception if SQL operations fail
     */
    @Test
    void testGetGameID_gameExists() throws Exception {
		int userId = 1;
		String gameTitle = "Mock Game";

		when(mockConnection.prepareStatement(anyString())).thenReturn(mockStmt1);
		when(mockStmt1.executeQuery()).thenReturn(mockGeneratedKeys);
		when(mockGeneratedKeys.next()).thenReturn(true);
		when(mockGeneratedKeys.getInt("GameID")).thenReturn(123);
		boolean result = collectionDAO.getGameID(userId, gameTitle);

		assertTrue(result, "Should return true when game ID is found");
		verify(mockStmt1).setInt(1, userId);
		verify(mockStmt1).setString(2, gameTitle);
	}
    
    /**
	 * Tests getGameID returns false when no matching game is found.
	 * 
	 * @throws Exception if SQL operations fail
	 */
    @Test
    void testGetGameID_noGameFound() throws Exception {
    	int userId = 1;
    	String gameTitle = "Nonexistent Game";
    	
    	when(mockConnection.prepareStatement(anyString())).thenReturn(mockStmt1);
		when(mockStmt1.executeQuery()).thenReturn(mockGeneratedKeys);
		when(mockGeneratedKeys.next()).thenReturn(false);
		
		boolean result = collectionDAO.getGameID(userId, gameTitle);
		
		assertFalse(result, "Should return false when no game is found");
		verify(mockStmt1).setInt(1, userId);
		verify(mockStmt1).setString(2, gameTitle);
    }
    
    /**
	 * Tests getGameID returns false when an SQLException occurs.
	 * 
	 * @throws SQLException if SQL operations fail
	 */
    @Test
    void testGetGameID_sqlException() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("DB error"));
        boolean result = collectionDAO.getGameID(1, "Broken Query");
        assertFalse(result, "Should return false if SQLException is thrown.");
    }
    
    /////////////////////// testing getAllGamesInCollection ////////////////////////////////////////
    
    /**
     * Tests retrieval of all games in a user's collection when multiple games are present.
     * 
     * @throws Exception if SQL operations fail
     */
    @Test
    void testGetAllGamesInCollection_multipleGamesReturned() throws Exception {
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStmt1);
        when(mockStmt1.executeQuery()).thenReturn(mockResultSet);

        when(mockResultSet.next()).thenReturn(true, true, false);  
        when(mockResultSet.getInt("GameID")).thenReturn(1, 2);
        when(mockResultSet.getString("Title")).thenReturn("Game One", "Game Two");
        when(mockResultSet.getString("Developer")).thenReturn("Dev A", "Dev B");
        when(mockResultSet.getString("Publisher")).thenReturn("Pub A", "Pub B");
        when(mockResultSet.getString("ReleaseDate")).thenReturn("2023-01-01", "2024-02-02");
        when(mockResultSet.getString("Genre")).thenReturn("RPG", "Action");
        when(mockResultSet.getString("Platform")).thenReturn("PC", "Switch");
        when(mockResultSet.getString("CompletionStatus")).thenReturn("Completed", "Started");
        when(mockResultSet.getString("Notes")).thenReturn("Note A", "Note B");
        when(mockResultSet.getString("CoverArt")).thenReturn("img1.jpg", "img2.jpg");

        List<game> games = collectionDAO.getAllGamesInCollection(1);

        assertEquals(2, games.size(), "Should return 2 games");
        assertEquals("Game One", games.get(0).getTitle());
        assertEquals("Game Two", games.get(1).getTitle());

        verify(mockStmt1).setInt(1, 1);
    }

    /**
	 * Tests retrieval of all games in a user's collection when no games are found.
	 * 
	 * @throws Exception if SQL operations fail
	 */
    @Test
    void testGetAllGamesInCollection_noGamesFound() throws Exception {
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStmt1);
        when(mockStmt1.executeQuery()).thenReturn(mockResultSet);

        when(mockResultSet.next()).thenReturn(false);  

        List<game> games = collectionDAO.getAllGamesInCollection(99);

        assertNotNull(games, "Should return a non-null list");
        assertTrue(games.isEmpty(), "List should be empty for no results");

        verify(mockStmt1).setInt(1, 99);
    }

    /**
	 * Tests that an empty collection returns an empty list.
	 * 
	 * @throws Exception if SQL operations fail
	 */
    @Test
    void testGetAllGamesInCollection_sqlException() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Simulated error"));

        List<game> games = collectionDAO.getAllGamesInCollection(5);

        assertNotNull(games, "Should return a non-null list on exception");
        assertTrue(games.isEmpty(), "List should be empty on SQL exception");
    }
    
    /**
	 * Tests retrieval of all games in a user's collection with an invalid date format.
	 * 
	 * @throws Exception if SQL operations fail
	 */
    @Test
    void testGetAllGamesInCollection_invalidDateFormat() throws Exception {
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStmt1);
        when(mockStmt1.executeQuery()).thenReturn(mockResultSet);

        when(mockResultSet.next()).thenReturn(true, false);  
        when(mockResultSet.getInt("GameID")).thenReturn(1);
        when(mockResultSet.getString("Title")).thenReturn("Broken Date Game");
        when(mockResultSet.getString("Developer")).thenReturn("Dev X");
        when(mockResultSet.getString("Publisher")).thenReturn("Pub X");
        when(mockResultSet.getString("ReleaseDate")).thenReturn("not-a-date");  
        when(mockResultSet.getString("Genre")).thenReturn("Indie");
        when(mockResultSet.getString("Platform")).thenReturn("PC");
        when(mockResultSet.getString("CompletionStatus")).thenReturn("Not Started");
        when(mockResultSet.getString("Notes")).thenReturn("Check date");
        when(mockResultSet.getString("CoverArt")).thenReturn("img.jpg");

        List<game> games = collectionDAO.getAllGamesInCollection(42);

        assertEquals(1, games.size(), "One game should be returned");
        assertNull(games.get(0).getReleaseDate(), "Release date should be null due to parse failure");
    }
	
    /**
	 * Tests retrieval of all games in a user's collection with null or empty release dates.
	 * 
	 * @throws Exception if SQL operations fail
	 */
    @Test
    void testGetAllGamesInCollection_nullOrEmptyDate() throws Exception {
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStmt1);
        when(mockStmt1.executeQuery()).thenReturn(mockResultSet);

        when(mockResultSet.next()).thenReturn(true, true, false); 
        when(mockResultSet.getInt("GameID")).thenReturn(1, 2);
        when(mockResultSet.getString("Title")).thenReturn("Null Date Game", "Empty Date Game");
        when(mockResultSet.getString("Developer")).thenReturn("Dev1", "Dev2");
        when(mockResultSet.getString("Publisher")).thenReturn("Pub1", "Pub2");
        when(mockResultSet.getString("ReleaseDate")).thenReturn(null, ""); 
        when(mockResultSet.getString("Genre")).thenReturn("Puzzle", "RPG");
        when(mockResultSet.getString("Platform")).thenReturn("PC", "Switch");
        when(mockResultSet.getString("CompletionStatus")).thenReturn("Started", "Completed");
        when(mockResultSet.getString("Notes")).thenReturn("Note1", "Note2");
        when(mockResultSet.getString("CoverArt")).thenReturn("img1.jpg", "img2.jpg");

        List<game> games = collectionDAO.getAllGamesInCollection(100);

        assertEquals(2, games.size());
        assertNull(games.get(0).getReleaseDate(), "Should be null when ReleaseDate is null");
        assertNull(games.get(1).getReleaseDate(), "Should be null when ReleaseDate is empty string");
    }

    //////////////////////// testing searchCollection ///////////////////////////////////
    
    /**
	 * Tests searching the collection with a valid search term that matches multiple games.
	 * 
	 * @throws Exception if SQL operations fail
	 */
    @Test
    void testSearchCollection_multipleResults() throws Exception {
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStmt1);
        when(mockStmt1.executeQuery()).thenReturn(mockResultSet);

        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getInt("GameID")).thenReturn(1, 2);
        when(mockResultSet.getString("Title")).thenReturn("Game A", "Game B");
        when(mockResultSet.getString("Developer")).thenReturn("Dev A", "Dev B");
        when(mockResultSet.getString("Publisher")).thenReturn("Pub A", "Pub B");
        when(mockResultSet.getString("ReleaseDate")).thenReturn("2023-01-01", "2024-02-02");
        when(mockResultSet.getString("Genre")).thenReturn("Action", "RPG");
        when(mockResultSet.getString("Platform")).thenReturn("PC", "Switch");
        when(mockResultSet.getString("CompletionStatus")).thenReturn("Started", "Completed");
        when(mockResultSet.getString("Notes")).thenReturn("Notes A", "Notes B");
        when(mockResultSet.getString("CoverArt")).thenReturn("imgA.png", "imgB.png");

        List<game> results = collectionDAO.searchCollection(1, "Game");

        assertEquals(2, results.size());
        assertEquals("Game A", results.get(0).getTitle());
        assertEquals("Game B", results.get(1).getTitle());

        verify(mockStmt1).setInt(1, 1);
        verify(mockStmt1).setString(2, "%Game%");
    }

    /**
     * Tests searching the collection with a valid search term that does not matches a single game.
     * 
     * @throws Exception if SQL operations fail
     */
    @Test
    void testSearchCollection_noResults() throws Exception {
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStmt1);
        when(mockStmt1.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        List<game> results = collectionDAO.searchCollection(1, "MissingTitle");

        assertNotNull(results);
        assertTrue(results.isEmpty(), "Should return empty list when no matches are found.");
    }

    /**
	 * Tests handling of invalid release date during search parsing.
	 * 
	 * @throws Exception if SQL operations fail
	 */
    @Test
    void testSearchCollection_invalidDateFormat() throws Exception {
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStmt1);
        when(mockStmt1.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getInt("GameID")).thenReturn(1);
        when(mockResultSet.getString("Title")).thenReturn("Game X");
        when(mockResultSet.getString("Developer")).thenReturn("Dev X");
        when(mockResultSet.getString("Publisher")).thenReturn("Pub X");
        when(mockResultSet.getString("ReleaseDate")).thenReturn("invalid-date");
        when(mockResultSet.getString("Genre")).thenReturn("Action");
        when(mockResultSet.getString("Platform")).thenReturn("PC");
        when(mockResultSet.getString("CompletionStatus")).thenReturn("Done");
        when(mockResultSet.getString("Notes")).thenReturn("Check release date");
        when(mockResultSet.getString("CoverArt")).thenReturn("imgX.png");

        List<game> results = collectionDAO.searchCollection(2, "Game X");

        assertEquals(1, results.size());
        assertNull(results.get(0).getReleaseDate(), "Release date should be null due to parse failure.");
    }

    /**
     * Tests handling of null or empty release dates in search results.
     * 
     * @throws Exception if SQL operations fail
     */
    @Test
    void testSearchCollection_nullOrEmptyReleaseDate() throws Exception {
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStmt1);
        when(mockStmt1.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, false);

        when(mockResultSet.getInt("GameID")).thenReturn(1, 2);
        when(mockResultSet.getString("Title")).thenReturn("Game Null", "Game Empty");
        when(mockResultSet.getString("Developer")).thenReturn("Dev1", "Dev2");
        when(mockResultSet.getString("Publisher")).thenReturn("Pub1", "Pub2");
        when(mockResultSet.getString("ReleaseDate")).thenReturn(null, ""); 
        when(mockResultSet.getString("Genre")).thenReturn("Genre1", "Genre2");
        when(mockResultSet.getString("Platform")).thenReturn("PC", "Switch");
        when(mockResultSet.getString("CompletionStatus")).thenReturn("Done", "In Progress");
        when(mockResultSet.getString("Notes")).thenReturn("Note1", "Note2");
        when(mockResultSet.getString("CoverArt")).thenReturn("img1.png", "img2.png");

        List<game> results = collectionDAO.searchCollection(3, "Game");

        assertEquals(2, results.size());
        assertNull(results.get(0).getReleaseDate(), "Should be null for null ReleaseDate.");
        assertNull(results.get(1).getReleaseDate(), "Should be null for empty ReleaseDate.");
    }

    /**
     * Tests that SQL exceptions during search return an empty list.
     * 
     * @throws Exception if SQL operations fail
     */
    @Test	
    void testSearchCollection_sqlExceptionHandled() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("DB error"));

        List<game> results = collectionDAO.searchCollection(4, "Crash");

        assertNotNull(results);
        assertTrue(results.isEmpty(), "Should return empty list on SQL exception.");
    }

    /////////////////////////// testing sortCollection ///////////////////////////////////////
    
    /**
     * Tests sorting the collection by title without a search query.
     * 
     * @throws Exception if SQL operations fail
     */
    @Test
    void testSortCollection_byTitle_noSearchQuery() throws Exception {
        ResultSet rs = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStmt1);
        when(mockStmt1.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, false);
        when(rs.getInt("GameID")).thenReturn(1);
        when(rs.getString("Title")).thenReturn("Alpha");
        when(rs.getString("Developer")).thenReturn("Dev");
        when(rs.getString("Publisher")).thenReturn("Pub");
        when(rs.getString("ReleaseDate")).thenReturn("2023-01-01");
        when(rs.getString("Genre")).thenReturn("Action");
        when(rs.getString("Platform")).thenReturn("PC");
        when(rs.getString("CompletionStatus")).thenReturn("Completed");
        when(rs.getString("Notes")).thenReturn("Good");
        when(rs.getString("CoverArt")).thenReturn("cover.jpg");

        List<game> result = collectionDAO.sortCollection(1, "Title", null);
        assertEquals(1, result.size());
        assertEquals("Alpha", result.get(0).getTitle());
    }

    /**
	 * Tests sorting the collection by release date with a search query.
	 * 
	 * @throws Exception if SQL operations fail
	 */
    @Test
    void testSortCollection_byReleaseDate_withSearchQuery() throws Exception {
        ResultSet rs = mock(ResultSet.class);
        when(mockConnection.prepareStatement(contains("ReleaseDate"))).thenReturn(mockStmt1);
        when(mockStmt1.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, false);
        when(rs.getString("ReleaseDate")).thenReturn("2022-05-20");
        when(rs.getInt("GameID")).thenReturn(2);
        when(rs.getString("Title")).thenReturn("Zelda");
        when(rs.getString("Developer")).thenReturn("Nintendo");
        when(rs.getString("Publisher")).thenReturn("Nintendo");
        when(rs.getString("Genre")).thenReturn("Adventure");
        when(rs.getString("Platform")).thenReturn("Switch");
        when(rs.getString("CompletionStatus")).thenReturn("Done");
        when(rs.getString("Notes")).thenReturn("Classic");
        when(rs.getString("CoverArt")).thenReturn("zelda.png");

        List<game> result = collectionDAO.sortCollection(1, "Release Date", "Zelda");

        assertEquals(1, result.size());
        assertEquals("Zelda", result.get(0).getTitle());
    }
    
    /**
     * Tests sorting the collection by platform with an invalid date format.
     * 
     * @throws Exception if SQL operations fail
     */
    @Test
    void testSortCollection_byPlatform_invalidDate() throws Exception {
        ResultSet rs = mock(ResultSet.class);
        when(mockConnection.prepareStatement(contains("Platform"))).thenReturn(mockStmt1);
        when(mockStmt1.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, false);
        when(rs.getString("ReleaseDate")).thenReturn("invalid-date"); 
        when(rs.getInt("GameID")).thenReturn(3);
        when(rs.getString("Title")).thenReturn("Broken Date");
        when(rs.getString("Developer")).thenReturn("Dev");
        when(rs.getString("Publisher")).thenReturn("Pub");
        when(rs.getString("Genre")).thenReturn("Puzzle");
        when(rs.getString("Platform")).thenReturn("PC");
        when(rs.getString("CompletionStatus")).thenReturn("In Progress");
        when(rs.getString("Notes")).thenReturn("Date error");
        when(rs.getString("CoverArt")).thenReturn("img.png");

        List<game> result = collectionDAO.sortCollection(1, "Platform", null);

        assertEquals(1, result.size());
        assertNull(result.get(0).getReleaseDate());
    }
	
    /**
     * Tests sortCollection returns an empty list for no matches.
     * 
     * @throws Exception if SQL operations fail
     */
    @Test
    void testSortCollection_emptyResult() throws Exception {
        ResultSet rs = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStmt1);
        when(mockStmt1.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false); 
        List<game> result = collectionDAO.sortCollection(1, "Title", "NonExistent");

        assertTrue(result.isEmpty());
    }
    
    /**
     * Tests sortCollection returns an empty list for unknown sort criteria.
     */
    @Test
    void testSortCollection_invalidSortOption() {
        List<game> result = collectionDAO.sortCollection(1, "InvalidSort", null);
        assertTrue(result.isEmpty(), "Should return empty list for unknown sortBy value.");
    }


    /**
     * Tests that SQL exceptions during sorting return an empty list.
     * 
     * @throws Exception if SQL operations fail
     */
    @Test
    void testSortCollection_sqlException() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Simulated DB error"));

        List<game> result = collectionDAO.sortCollection(1, "Title", "Error");

        assertNotNull(result);
        assertTrue(result.isEmpty(), "Should return empty list on SQL exception.");
    }

    ///////////////////////////// testing getCollectionPlatform ///////////////////////////////////////
    
    /**
     *  Tests the getCollectionPlatform method when a platform filter is applied.
     * 
     * This test simulates a result set containing one platform match ("PC").
     * It verifies that the correct SQL query is used with the platform condition
     * and confirms that the returned list contains the expected platform.
     * 
     * @throws Exception if SQL operations fail
     */
    @Test
    void testGetCollectionPlatform_withFilter() throws Exception {
        ResultSet rs = mock(ResultSet.class);
        when(mockConnection.prepareStatement(contains("G.Platform = ?"))).thenReturn(mockStmt1);
        when(mockStmt1.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, false);
        when(rs.getString("Platform")).thenReturn("PC");

        List<String> result = collectionDAO.getCollectionPlatform(1, "PC");

        assertEquals(1, result.size());
        assertEquals("PC", result.get(0));
        verify(mockStmt1).setInt(1, 1);
        verify(mockStmt1).setString(2, "PC");
    }

    /**
     * Tests the getCollectionPlatform method when no platforms match the given filter.
     * 
     * This test simulates an empty result set and confirms that the method returns
     * an empty list. It checks that the DAO handles this edge case properly.
     * 
     * @throws Exception if SQL operations fail
     */
    @Test
    void testGetCollectionPlatform_noMatch() throws Exception {
        ResultSet rs = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStmt1);
        when(mockStmt1.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false); 

        List<String> result = collectionDAO.getCollectionPlatform(99, "Dreamcast");

        assertNotNull(result);
        assertTrue(result.isEmpty(), "Should return empty list when no platforms match.");
    }

    /**
     * Tests the getCollectionPlatform method when a SQL exception is thrown during query execution.
     * 
     * This test verifies that the method catches the exception and returns an empty list
     * instead of propagating the error. It ensures proper error handling in the DAO.
     *
     * @throws Exception if SQL operations fail
     */
    @Test
    void testGetCollectionPlatform_sqlException() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Simulated DB error"));

        List<String> result = collectionDAO.getCollectionPlatform(1, "PC");

        assertNotNull(result);
        assertTrue(result.isEmpty(), "Should return empty list when SQLException is thrown.");
    }

    /**
     * Tests the getCollectionPlatform method when no platform filter is provided.
     *
     * This test checks that the DAO constructs and executes a SQL query without a platform filter.
     * It simulates a result set with one platform ("PlayStation") and confirms that the platform
     * is correctly returned in the result list. It also verifies that no second query parameter is set.
     *
     * @throws Exception if SQL operations fail
     */
    @Test
    void testGetCollectionPlatform_queryWithoutFilter() throws Exception {
        ResultSet rs = mock(ResultSet.class);
        when(mockConnection.prepareStatement(
                eq("SELECT DISTINCT G.Platform FROM UserGameCollection UGC JOIN Game G ON UGC.GameID = G.GameID WHERE UGC.UserID = ?")
            )).thenReturn(mockStmt1);

        when(mockStmt1.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, false);
        when(rs.getString("Platform")).thenReturn("PlayStation");
        List<String> platforms = collectionDAO.getCollectionPlatform(42, null);

        assertEquals(1, platforms.size());
        assertEquals("PlayStation", platforms.get(0));
        verify(mockStmt1).setInt(1, 42);
        verify(mockStmt1, never()).setString(eq(2), anyString());
    }
    
    
    /////////////////////////// testing getCollectionGenres ///////////////////////////////////////
    
    /**
     * Tests the getCollectionGenres method when a genre filter is provided.
     *
     * This test simulates a result set containing one matching genre ("RPG") and verifies
     * that the correct genre is returned. It also confirms that the filter parameter is
     * properly set in the SQL query.
     *
     * @throws Exception if SQL operations fail
     */
    @Test
    void testGetCollectionGenres_withFilter_success() throws Exception {
        ResultSet rs = mock(ResultSet.class);
        when(mockConnection.prepareStatement(contains("G.Genre = ?"))).thenReturn(mockStmt1);
        when(mockStmt1.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, false);
        when(rs.getString("Genre")).thenReturn("RPG");

        List<String> genres = collectionDAO.getCollectionGenres(1, "RPG");

        assertEquals(1, genres.size());
        assertEquals("RPG", genres.get(0));
        verify(mockStmt1).setInt(1, 1);
        verify(mockStmt1).setString(2, "RPG");
    }

    /**
     * Tests the getCollectionGenres method when no genre filter is provided.
     *
     * This test simulates a result set with multiple genres ("Action" and "Adventure")
     * and verifies that both are returned. It confirms that the query does not attempt
     * to set a second parameter since no filter is specified.
     *
     * @throws Exception if mocking or DAO execution fails.
     */
    @Test
    void testGetCollectionGenres_withoutFilter_success() throws Exception {
        ResultSet rs = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStmt1);
        when(mockStmt1.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, true, false);
        when(rs.getString("Genre")).thenReturn("Action", "Adventure");

        List<String> genres = collectionDAO.getCollectionGenres(2, null);

        assertEquals(2, genres.size());
        assertTrue(genres.contains("Action"));
        assertTrue(genres.contains("Adventure"));

        verify(mockStmt1).setInt(1, 2);
        verify(mockStmt1, never()).setString(eq(2), anyString());
    }

    /**
     * Tests the getCollectionGenres method when no results are found.
     *
     * This test simulates an empty result set for a genre filter that doesn't match
     * any records in the database. It verifies that an empty list is returned.
     *
     * @throws Exception if mocking or DAO execution fails
     */
    @Test
    void testGetCollectionGenres_noResults() throws Exception {
        ResultSet rs = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStmt1);
        when(mockStmt1.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);
        List<String> genres = collectionDAO.getCollectionGenres(3, "Nonexistent Genre");

        assertNotNull(genres);
        assertTrue(genres.isEmpty(), "Expected empty list when no genres found");
    }

    /**
     * Tests the getCollectionGenres method when a SQL exception occurs.
     *
     * This test simulates a database failure during query preparation and ensures that
     * the DAO method handles the exception gracefully by returning an empty list.
     *
     * @throws Exception if mocking setup fails
     */
    @Test
    void testGetCollectionGenres_sqlException() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Simulated DB failure"));

        List<String> genres = collectionDAO.getCollectionGenres(4, "RPG");

        assertNotNull(genres);
        assertTrue(genres.isEmpty(), "Should return empty list on SQLException");
    }

    /////////////////////////////// testing filterCollection ///////////////////////////////////////
    
    /**
     * Tests the filterCollection method with all filters applied (genre, platform, and status).
     *
     * This test simulates a valid result set that matches all given filters. It verifies
     * that the game is correctly returned and its attributes, including the release date,
     * are parsed and matched accurately.
     *
     * @throws Exception if mocking or DAO execution fails
     */
    @Test
    void testFilterCollection_allFiltersApplied() throws Exception {
        ResultSet rs = mock(ResultSet.class);
        when(mockConnection.prepareStatement(contains("G.Genre IN"))).thenReturn(mockStmt1);
        when(mockStmt1.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, false);
        when(rs.getInt("GameID")).thenReturn(1);
        when(rs.getString("Title")).thenReturn("Game X");
        when(rs.getString("Developer")).thenReturn("Dev X");
        when(rs.getString("Publisher")).thenReturn("Pub X");
        when(rs.getString("ReleaseDate")).thenReturn("2023-05-10");
        when(rs.getString("Genre")).thenReturn("Adventure");
        when(rs.getString("Platform")).thenReturn("PC, Switch");
        when(rs.getString("CompletionStatus")).thenReturn("Completed");
        when(rs.getString("Notes")).thenReturn("Good game");
        when(rs.getString("CoverArt")).thenReturn("cover.jpg");

        List<game> result = collectionDAO.filterCollection(
            1,
            List.of("Adventure"),
            List.of("PC", "Switch"),
            List.of("Completed")
        );

        assertEquals(1, result.size());
        assertEquals("Game X", result.get(0).getTitle());
        assertEquals(LocalDate.of(2023, 5, 10), result.get(0).getReleaseDate());
    }

    /**
     * Tests the filterCollection method when no filters are applied.
     *
     * This test ensures that the method returns results based solely on the user ID,
     * simulating a general query with no genre, platform, or completion status filters.
     * It verifies correct handling of an empty result set.
     *
     * @throws Exception if mocking or DAO execution fails
     */
    @Test
    void testFilterCollection_noFilters() throws Exception {
        ResultSet rs = mock(ResultSet.class);
        when(mockConnection.prepareStatement(contains("UGC.UserID = ?"))).thenReturn(mockStmt1);
        when(mockStmt1.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false); 

        List<game> result = collectionDAO.filterCollection(2, null, null, null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Tests the filterCollection method when the release date is in an invalid format.
     *
     * This test simulates a record with a non-parsable date string ("not-a-date") and verifies
     * that the method handles the parsing error gracefully by setting the release date to null.
     *
     * @throws Exception if mocking or DAO execution fails
     */
    @Test
    void testFilterCollection_invalidDateHandled() throws Exception {
        ResultSet rs = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStmt1);
        when(mockStmt1.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, false);
        when(rs.getInt("GameID")).thenReturn(10);
        when(rs.getString("Title")).thenReturn("Corrupt Game");
        when(rs.getString("Developer")).thenReturn("BadDev");
        when(rs.getString("Publisher")).thenReturn("BadPub");
        when(rs.getString("ReleaseDate")).thenReturn("not-a-date");
        when(rs.getString("Genre")).thenReturn("Horror");
        when(rs.getString("Platform")).thenReturn("PC");
        when(rs.getString("CompletionStatus")).thenReturn("Not Started");
        when(rs.getString("Notes")).thenReturn("Bad date");
        when(rs.getString("CoverArt")).thenReturn("bad.png");

        List<game> result = collectionDAO.filterCollection(
            3,
            List.of("Horror"),
            List.of("PC"),
            List.of("Not Started")
        );

        assertEquals(1, result.size());
        assertNull(result.get(0).getReleaseDate(), "Release date should be null if unparseable.");
    }

    /**
     * Tests the filterCollection method when the release date is null or an empty string.
     *
     * This test simulates two records with null and empty string release dates and verifies
     * that the method does not throw an error and correctly sets the release date to null.
     *
     * @throws Exception if mocking or DAO execution fails
     */
    @Test
    void testFilterCollection_nullAndEmptyDatesHandled() throws Exception {
        ResultSet rs = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStmt1);
        when(mockStmt1.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, true, false);
        when(rs.getInt("GameID")).thenReturn(101, 102);
        when(rs.getString("Title")).thenReturn("Null Date Game", "Empty Date Game");
        when(rs.getString("Developer")).thenReturn("DevA", "DevB");
        when(rs.getString("Publisher")).thenReturn("PubA", "PubB");
        when(rs.getString("ReleaseDate")).thenReturn(null, "");
        when(rs.getString("Genre")).thenReturn("Action", "Action");
        when(rs.getString("Platform")).thenReturn("PC", "PC");
        when(rs.getString("CompletionStatus")).thenReturn("Completed", "Completed");
        when(rs.getString("Notes")).thenReturn("noteA", "noteB");
        when(rs.getString("CoverArt")).thenReturn("a.jpg", "b.jpg");

        List<game> result = collectionDAO.filterCollection(
            4,
            List.of("Action"),
            List.of("PC"),
            List.of("Completed")
        );

        assertEquals(2, result.size());
        assertNull(result.get(0).getReleaseDate());
        assertNull(result.get(1).getReleaseDate());
    }

    /**
     * Tests the filterCollection method when a SQL exception occurs during query preparation.
     *
     * This test simulates a database error and ensures that the method returns an empty list
     * instead of propagating the exception. This validates the DAO's exception handling.
     *
     * @throws Exception if mocking setup fails
     */
    @Test
    void testFilterCollection_sqlException() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("DB Crash"));

        List<game> result = collectionDAO.filterCollection(
            5,
            List.of("RPG"),
            List.of("Switch"),
            List.of("In Progress")
        );

        assertNotNull(result);
        assertTrue(result.isEmpty(), "Should return empty list on SQLException.");
    }

    /////////////////////////// testing gameExists ///////////////////////////////////////
    
    /**
     * Tests the gameExists method when a game is present in the database.
     *
     * This test simulates a successful query match and verifies that the method returns true
     * when the specified game ID exists in the Game table.
     *
     * @throws Exception if mocking or DAO execution fails
     */
    @Test
    void testGameExists_whenGameExists_returnsTrue() throws Exception {
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStmt1);
        when(mockStmt1.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);  

        boolean exists = collectionDAO.gameExists(101);

        assertTrue(exists, "Expected true when game exists in database");
        verify(mockStmt1).setInt(1, 101);
    }

    /**
     * Tests the gameExists method when the game does not exist in the database.
     *
     * This test simulates a result set that returns no rows and verifies that the method
     * correctly returns false when the specified game ID is not found.
     *
     * @throws Exception if mocking or DAO execution fails
     */
    @Test
    void testGameExists_whenGameDoesNotExist_returnsFalse() throws Exception {
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStmt1);
        when(mockStmt1.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);  

        boolean exists = collectionDAO.gameExists(202);

        assertFalse(exists, "Expected false when game is not found in database");
        verify(mockStmt1).setInt(1, 202);
    }

    /**
     * Tests the gameExists method when a SQL exception is thrown.
     *
     * This test simulates a database failure during query preparation and verifies that
     * the method returns false instead of propagating the exception.
     *
     * @throws Exception if mocking setup fails
     */
    @Test
    void testGameExists_whenSQLExceptionThrown_returnsFalse() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Simulated failure"));

        boolean exists = collectionDAO.gameExists(303);

        assertFalse(exists, "Expected false when SQLException occurs");
    }
    
	/////////////////////////// testing userAlreadyHasGame ///////////////////////////////////////
	
    /**
     * Tests the userAlreadyHasGame method when the user owns the specified game.
     *
     * This test simulates a successful query match and verifies that the method
     * returns true when the user has already added the game to their collection.
     *
     * @throws Exception if mocking or DAO execution fails
     */
    @Test
    void testUserAlreadyHasGame_gameExists_returnsTrue() throws Exception {
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStmt1);
        when(mockStmt1.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);

        boolean result = collectionDAO.userAlreadyHasGame(1, 101);

        assertTrue(result, "Expected true when user already owns the game");
        verify(mockStmt1).setInt(1, 1);
        verify(mockStmt1).setInt(2, 101);
    }

    /**
     * Tests the userAlreadyHasGame method when the user does not own the game.
     *
     * This test simulates an empty result set and verifies that the method returns false
     * when the user has not added the specified game to their collection.
     *
     * @throws Exception if mocking or DAO execution fails
     */
    @Test
    void testUserAlreadyHasGame_gameNotExists_returnsFalse() throws Exception {
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStmt1);
        when(mockStmt1.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false); 
        boolean result = collectionDAO.userAlreadyHasGame(2, 202);

        assertFalse(result, "Expected false when user does not own the game");
        verify(mockStmt1).setInt(1, 2);
        verify(mockStmt1).setInt(2, 202);
    }

    /**
     * Tests the userAlreadyHasGame method when a SQL exception is thrown.
     *
     * This test simulates a database failure during query preparation and confirms that
     * the method catches the exception and returns false.
     *
     * @throws Exception if mocking setup fails
     */
    @Test
    void testUserAlreadyHasGame_sqlException_returnsFalse() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Simulated DB error"));
 
        boolean result = collectionDAO.userAlreadyHasGame(3, 303);

        assertFalse(result, "Expected false when SQLException is thrown");
    }

}

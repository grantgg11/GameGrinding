package services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.hc.core5.http.ParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import database.CollectionDAO;
import models.game;
import security.AuthManager; 
import utils.AlertHelper;

/**
 * GameCollectionServiceTest contains unit tests for verifying the functionality of the
 * GameCollectionService class in the GameGrinding application.
 *
 * This test class ensures that:
 * - The singleton pattern is correctly implemented, returning the same instance across multiple calls.
 * - User session handling methods (setCurrentUserID and getCurrentUserID) store and retrieve IDs reliably.
 * - The addGameToCollection workflow validates input, handles null or invalid data, and interacts with the
 *   CollectionDAO appropriately for both successful and failed scenarios, including DAO errors.
 * - Collection retrieval methods (getUserCollection) correctly return game lists or empty lists in cases
 *   of invalid input, empty collections, or DAO null responses.
 * - Sorting methods (sortCollection and sortFilteredCollection) correctly order games by title, platform,
 *   or release date, handle null values gracefully, and avoid altering the list for invalid sort criteria.
 * - Game removal operations (removeGameFromCollection) validate IDs, handle DAO failures, and prevent
 *   unintended deletions for invalid input.
 * - Filtering methods (filterCollection) apply combinations of genre, platform, and completion status filters,
 *   handle empty or null filters correctly, and safely return results for invalid user IDs without DAO calls.
 * - Searching methods (searchCollection) retrieve results by title, handle empty or invalid input, and
 *   account for DAO null returns without crashing.
 *
 * The tests map directly to functional test cases in the project’s test plan, including:
 * - TC-4 (Add Game through API)
 * - TC-6 (Remove Game)
 * - TC-7 (View Collection)
 * - TC-10 (Search Game in Collection)
 * - TC-12 (Sort)
 * - TC-19 (Filter)
 *
 * Mockito is used to mock dependencies such as CollectionDAO and AlertHelper, isolating service logic from
 * database and UI components. Tests confirm both positive and negative scenarios, ensuring that
 * GameCollectionService provides consistent, fault-tolerant behavior for managing a user's game collection.
 */
class GameCollectionServiceTest {
	
    public GameCollectionService gameCollectionService;
    private CollectionDAO mockCollectionDAO;
    private AlertHelper mockAlertHelper;

    @BeforeEach
    void setUp() {

        mockCollectionDAO = mock(CollectionDAO.class);
        mockAlertHelper = mock(AlertHelper.class);


        doNothing().when(mockAlertHelper).showError(anyString(), anyString(), anyString());
        doNothing().when(mockAlertHelper).showInfo(anyString(), anyString(), anyString());

        AuthManager authManager = new AuthManager(); 
        gameCollectionService = new GameCollectionService(mockCollectionDAO, authManager, mockAlertHelper);
    }
    
    /**
     * Validates that the GameCollectionService class is a singleton.
     * Makes sure that the same instance is returned when getInstance() is called multiple times.
     */
    @Test
    public void testGetInstance() {
		GameCollectionService instance1 = GameCollectionService.getInstance();
		GameCollectionService instance2 = GameCollectionService.getInstance();
		assertSame(instance1, instance2, "Singleton instances should be the same");
	}
    
    /**
	 * Validates that the current user ID is set and retrieved correctly.
	 * Supports internal session management logic
	 */
    @Test
	void testSetCurrentUserID() {
		int userID = 123;
		gameCollectionService.setCurrentUserID(userID);
		assertEquals(userID, gameCollectionService.getCurrentUserID(), "Current user ID should match");
	}
    
    /**
     * Retrieves the user ID that was previously set. 
     * Makes sure that the setter and getter methods work correctly.
     */
	@Test
	void testGetCurrentUserID() {
		int userID = 123;
		gameCollectionService.setCurrentUserID(userID);
		assertEquals(userID, gameCollectionService.getCurrentUserID(), "Current user ID should match");
	}
	
	/**
	 * Simulates a successful game addition to the user's collection.
	 * Corresponds to TC-4 (Add Game through API) in the test plan. 
	 * 
	 * @throws ParseException Thrown if there is an error parsing the date.
	 * @throws InterruptedException Thrown if the thread is interrupted.
	 * @throws ExecutionException Thrown if there is an error executing the task.
	 */
	@Test
	void testSuccessfulGameAddition() throws ParseException, InterruptedException, ExecutionException {
		int userID = 123;
		game mockGame = new game();
		mockGame.setGameID(80);
		mockGame.setTitle("mockTitle");
		mockGame.setDeveloper("mockDeveloper");
		mockGame.setPublisher("mockPublisher");
		mockGame.setReleaseDate(LocalDate.of(2000, 1, 1));
		mockGame.setGenre("mockGenre");
		mockGame.setPlatform("mockPlatform");
		mockGame.setCoverImageUrl("mockURL");
		when(mockCollectionDAO.addGameToCollection(eq(userID), eq(80), eq("mockTitle"), eq("mockDeveloper"), eq("mockPublisher"), 
				eq("2000-01-01"), eq("mockGenre"), eq("mockPlatform"), eq("Not Started"), eq(" "), eq("mockURL"))).thenReturn(Boolean.valueOf(true));
		boolean result = gameCollectionService.addGameToCollection(mockGame, userID);
		assertTrue(result, "Expected game to be added successfully.");
		verify(mockCollectionDAO).addGameToCollection(anyInt(), anyInt(), anyString(), anyString(), anyString(),
			    anyString(), anyString(), anyString(), anyString(), anyString(), anyString());
	}
	
	/**
	 * Simulates a game addition with a null release date.
	 * Corresponds to TC-4 (Add Game through API) in the test plan. 
	 * 
	 * @throws ParseException Thrown if there is an error parsing the date.
	 * @throws InterruptedException Thrown if the thread is interrupted.
	 * @throws ExecutionException Thrown if there is an error executing the task.
	 */
	@Test
	void testNullReleaseDateGameAddition() throws ParseException, InterruptedException, ExecutionException {
		int userID = 123;
		game mockGame = new game();
		mockGame.setGameID(80);
		mockGame.setTitle("mockTitle");
		mockGame.setDeveloper("mockDeveloper");
		mockGame.setPublisher("mockPublisher");
		mockGame.setReleaseDate(null);
		mockGame.setGenre("mockGenre");
		mockGame.setPlatform("mockPlatform");
		mockGame.setCoverImageUrl("mockURL");
		when(mockCollectionDAO.addGameToCollection(eq(userID), eq(80), eq("mockTitle"), eq("mockDeveloper"), eq("mockPublisher"), 
				eq("Unknown"), eq("mockGenre"), eq("mockPlatform"), eq("Not Started"), eq(" "), eq("mockURL"))).thenReturn(Boolean.valueOf(true));
		boolean result = gameCollectionService.addGameToCollection(mockGame, userID);
		assertTrue(result, "Expected game to be added successfully with 'Unknown' release date.");
	    verify(mockCollectionDAO).addGameToCollection(anyInt(), anyInt(), anyString(), anyString(), anyString(),
	            anyString(), anyString(), anyString(), anyString(), anyString(), anyString());
	}
	
	/**
	 * Simulates a game addition with an invalid user ID.
	 * Checks that the game is not added and that the DAO method is never called.
	 * Negative test for TC-4 edge case when no valid session exists. 
	 * 
	 * @throws ParseException Thrown if there is an error parsing the date.
	 * @throws InterruptedException Thrown if the thread is interrupted.
	 * @throws ExecutionException Thrown if there is an error executing the task.
	 */
	@Test 
	void testInavlidUserIDGameAddition() throws ParseException, InterruptedException, ExecutionException {
		int userID = -1;
		game mockGame = new game();
		mockGame.setGameID(80);
		mockGame.setTitle("mockTitle");
		mockGame.setDeveloper("mockDeveloper");
		mockGame.setPublisher("mockPublisher");
		mockGame.setReleaseDate(LocalDate.of(2000, 1, 1));
		mockGame.setGenre("mockGenre");
		mockGame.setPlatform("mockPlatform");
		mockGame.setCoverImageUrl("mockURL");
		when(mockCollectionDAO.addGameToCollection(eq(userID), eq(80), eq("mockTitle"), eq("mockDeveloper"), eq("mockPublisher"), 
				eq("2000-01-01"), eq("mockGenre"), eq("mockPlatform"), eq("Not Started"), eq(" "), eq("mockURL"))).thenReturn(false);
		boolean result = gameCollectionService.addGameToCollection(mockGame, userID);
		assertFalse(result, "Expected game to not be added, since the user's collection cannot be found.");
		verify(mockCollectionDAO, never()).addGameToCollection(anyInt(), anyInt(), anyString(), anyString(), anyString(),
		        anyString(), anyString(), anyString(), anyString(), anyString(), anyString());
	}
	
	/**
	 * Simulates a scenario where the DAO fails during game addition.
	 * Confirms the system returns a failure status without crashing. 
	 * Corresponds to TC-4 (Add Game through API) in the test plan.
	 * 
	 * @throws ParseException Thrown if there is an error parsing the date.
	 * @throws InterruptedException Thrown if the thread is interrupted.
	 * @throws ExecutionException Thrown if there is an error executing the task.
	 */
	@Test
	void testGameAdditionFailWhenDAOReturnsFalse() throws ParseException, InterruptedException, ExecutionException {
		int userID = 123;
		game mockGame = new game();
		mockGame.setGameID(80);
		mockGame.setTitle("mockTitle");
		mockGame.setDeveloper("mockDeveloper");
		mockGame.setPublisher("mockPublisher");
		mockGame.setReleaseDate(LocalDate.of(2000, 1, 1));
		mockGame.setGenre("mockGenre");
		mockGame.setPlatform("mockPlatform");
		mockGame.setCoverImageUrl("mockURL");
		when(mockCollectionDAO.addGameToCollection(eq(userID), eq(80), eq("mockTitle"), eq("mockDeveloper"), eq("mockPublisher"), 
				eq("2000-01-01"), eq("mockGenre"), eq("mockPlatform"), eq("Not Started"), eq(" "), eq("mockURL"))).thenReturn(false);
		boolean result = gameCollectionService.addGameToCollection(mockGame, userID);
		assertFalse(result, "Expected failure when adding game because DAO failed");
		verify(mockCollectionDAO).addGameToCollection(anyInt(), anyInt(), anyString(), anyString(), anyString(),
	            anyString(), anyString(), anyString(), anyString(), anyString(), anyString());
	}
	/////////////////////////////  Testing getUserCollection(int userID)  //////////////////////////////////////
	
	/**
	 * Retrieves a game collection for a valid user ID.
	 * Should return a non-null list of games.
	 * Corresponds to TC-7 (View Collection) in the test plan.
	 */
	@Test
	void testSuccessfulGetUserCollection() {
		int userID = 123;
		when(mockCollectionDAO.getAllGamesInCollection(userID)).thenReturn(List.of(new game()));
		List<game> games = gameCollectionService.getUserCollection(userID);
		assertNotNull(games, "Expected non-null list of games");
		verify(mockCollectionDAO).getAllGamesInCollection(userID);
	}
	
	/**
	 * Tries to retrieve a game collection for an invalid user ID.
	 * Makes sure the system handles edge case and avoids DAO calls. 
	 * Negative test for TC-7 (View Collection) in the test plan.
	 */
	@Test
	void testGetUserCollectionWithInvalidUserID() {
		int userID = -1;
		List<game> games = gameCollectionService.getUserCollection(userID);
		assertNotNull(games, "Expected non-null list of games");
		assertTrue(games.isEmpty(), "Expected empty list for invalid user ID");
		verify(mockCollectionDAO, never()).getAllGamesInCollection(userID);
	}
	
	/**
	 * Simulates a scenario where the DAO returns an empty list for a valid user ID.
	 * Should return an empty list of games.
	 * This supports TC-7 in the test plan by verifying behavior when no data is present.
	 */
	@Test
	void testGetUserCollectionWithNoGames() {
		int userID = 123;
		when(mockCollectionDAO.getAllGamesInCollection(userID)).thenReturn(List.of());
		List<game> games = gameCollectionService.getUserCollection(userID);
		assertNotNull(games, "Expected non-null list of games");
		assertTrue(games.isEmpty(), "Expected empty list when no games in collection");
		assertEquals(0, games.size(), "Expected 0 games for user with no collection");
		verify(mockCollectionDAO).getAllGamesInCollection(userID);
	}
	
	/**
	 * Simulates a scenario where the DAO returns null for a valid user ID.
	 * Should return an empty list of games.
	 * This acts as a fault-tolerant check relevant to TC-7.
	 */
	@Test
	void testGetUserCollectionWhenDAOReturnsNull() {
		int userID = 123;
		when(mockCollectionDAO.getAllGamesInCollection(userID)).thenReturn(null);
		List<game> games = gameCollectionService.getUserCollection(userID);
		assertNotNull(games);
		assertTrue(games.isEmpty(), "Expected empty list when DAO returns null");
		verify(mockCollectionDAO).getAllGamesInCollection(userID);
	}
	//////////////////////// testing sortCollection()  //////////////////////////////////////////////
	
	/**
	 * Verifies that sorting the collection by a valid field ('Title') works correctly.
	 * Supports TC-12 (Sort Collection) in the test plan for sort functionality.
	 */
	@Test
	void testSortCollectionSuccess() {
		int userID = 123;
		String sortBy = "Title";
		String searchQuery = "";
		when(mockCollectionDAO.sortCollection(userID, sortBy, searchQuery)).thenReturn(List.of(new game()));
		List<game> sortedGames = gameCollectionService.sortCollection(userID, sortBy, searchQuery);
		assertNotNull(sortedGames, "Expected non-null list of sorted games");
		verify(mockCollectionDAO).sortCollection(userID, sortBy, searchQuery);
	}
	
	/**
	 * Verifies that sorting the collection by an invalid field returns an empty list.
	 * Makes sure the service does not crash.
	 * This is a negative test for TC-12 (Sort Collection) in the test plan.
	 */
	@Test
	void testSortCollectionWithInvalidSortBy() {
		int userID = 123;
		String sortBy = "InvalidSort";
		String searchQuery = "";
		when(mockCollectionDAO.sortCollection(userID, sortBy, searchQuery)).thenReturn(List.of());
		List<game> sortedGames = gameCollectionService.sortCollection(userID, sortBy, searchQuery);
		assertNotNull(sortedGames, "Expected non-null list of sorted games");
		assertTrue(sortedGames.isEmpty(), "Expected empty list for invalid sortBy option");
		verify(mockCollectionDAO).sortCollection(userID, sortBy, searchQuery);
	}
	
	/**
	 * Verifies that sorting the collection with an empty search query works correctly.
	 * This is a positive test for TC-12 (Sort Collection) in the test plan.
	 */
	@Test
	void testSortCollectionWithNonEmptySearchQuery() {
		int userID = 123;
		String sortBy = "Title";
		String searchQuery = "mockTitle";
		when(mockCollectionDAO.sortCollection(userID, sortBy, searchQuery)).thenReturn(List.of(new game()));
		List<game> sortedGames = gameCollectionService.sortCollection(userID, sortBy, searchQuery);
		assertNotNull(sortedGames, "Expected non-null list of sorted games");
		verify(mockCollectionDAO).sortCollection(userID, sortBy, searchQuery);
	}
	///////////////////////////  testing removeGameFromCollection()  //////////////////////////////////
	
	/**
	 * Verifies that removing a game from the collection works correctly.
	 * This is a positive test for TC-6 (Remove Game) in the test plan.
	 */
	@Test
	void testRemoveGameFromCollectionSuccess() {
		int gameID = 80;
		int userID = 123;
		game mockGame = new game();
		mockGame.setGameID(gameID);
		mockGame.setTitle("mockTitle");
		mockGame.setDeveloper("mockDeveloper");
		mockGame.setPublisher("mockPublisher");
		mockGame.setReleaseDate(LocalDate.of(2000, 1, 1));
		mockGame.setGenre("mockGenre");
		mockGame.setPlatform("mockPlatform");
		mockGame.setCoverImageUrl("mockURL");		
		when(mockCollectionDAO.removeGameFromCollection(userID, gameID)).thenReturn(true);
		boolean result = gameCollectionService.removeGameFromCollection(userID, gameID);
		assertTrue(result);
	}
	
	/**
	 * Validates that the system prevents game removal when the user ID is invalid.
	 * This is a negative test for TC-6 (Remove Game) in the test plan.
	 */
	@Test
	void testRemoveGameFromCollectionWithInvalidUserID() {
		int gameID = 80;
		int userID = -1;
		boolean result = gameCollectionService.removeGameFromCollection(userID, gameID);
		assertFalse(result, "Expected failure when removing game with invalid user ID");
		verify(mockCollectionDAO, never()).removeGameFromCollection(anyInt(), anyInt()); 
	}
	
	/**
	 * Validates that the system prevents game removal when the game ID is invalid.
	 * Edge case for TC-6 (Remove Game) in the test plan.
	 */
	@Test
	void testRemoveGameFromCollectionWithInvalidGameID() {
		int gameID = -1;
		int userID = 123;
		game mockGame = new game();
		mockGame.setGameID(gameID);
		mockGame.setTitle("mockTitle");
		mockGame.setDeveloper("mockDeveloper");
		mockGame.setPublisher("mockPublisher");
		mockGame.setReleaseDate(LocalDate.of(2000, 1, 1));
		mockGame.setGenre("mockGenre");
		mockGame.setPlatform("mockPlatform");
		mockGame.setCoverImageUrl("mockURL");		
		when(mockCollectionDAO.removeGameFromCollection(userID, gameID)).thenReturn(false);
		boolean result = gameCollectionService.removeGameFromCollection(userID, gameID);
		assertFalse(result, "Expected failure when removing game with invalid game ID");
		verify(mockCollectionDAO).removeGameFromCollection(userID, gameID);
	}
	
	/**
	 * Validates that the system prevents game removal when the DAO fails.
	 * Test robustness of TC-6 when deletion does not succeed.
	 */
	@Test
	void testRemoveGameFromCollectionWhenDAOReturnsFalse() {
		int gameID = 80;
		int userID = 123;
		game mockGame = new game();
		mockGame.setGameID(gameID);
		mockGame.setTitle("mockTitle");
		mockGame.setDeveloper("mockDeveloper");
		mockGame.setPublisher("mockPublisher");
		mockGame.setReleaseDate(LocalDate.of(2000, 1, 1));
		mockGame.setGenre("mockGenre");
		mockGame.setPlatform("mockPlatform");
		mockGame.setCoverImageUrl("mockURL");		
		when(mockCollectionDAO.removeGameFromCollection(userID, gameID)).thenReturn(false);
		boolean result = gameCollectionService.removeGameFromCollection(userID, gameID);
		assertFalse(result, "Expected failure when removing game because DAO failed");
		verify(mockCollectionDAO).removeGameFromCollection(userID, gameID);
	}
	
	//////////////////////////  testing filterCollection()  ////////////////////////////////
	
	/**
	 * Validates that filtering the collection works correctly.
	 * This is a positive test for TC-19 (Filter) in the test plan.
	 */
	@Test
	void testFilterCollectionSuccess() {
		int userID = 123;
		List<String> genres = List.of("mockGenre");
		List<String> platforms = List.of("mockPlatform");
		List<String> completionStatuses = List.of("Not Started");
		when(mockCollectionDAO.filterCollection(userID, genres, platforms, completionStatuses)).thenReturn(List.of(new game()));
		List<game> filteredGames = gameCollectionService.filterCollection(userID, genres, platforms, completionStatuses);
		assertNotNull(filteredGames, "Expected non-null list of filtered games");
		assertEquals(1, filteredGames.size(), "Expected one game in filtered list");
		verify(mockCollectionDAO).filterCollection(userID, genres, platforms, completionStatuses);
	}
	
	/**
	 * Validates that filtering the collection with an empty genre list works correctly.
	 * This is a positive test for TC-19 (Filter) in the test plan.
	 */
	@Test
	void testFilterCollectionWithEmptyGenres() {
		int userID = 123;
		List<String> genres = List.of();
		List<String> platforms = List.of("mockPlatform");
		List<String> completionStatuses = List.of("Not Started");
		when(mockCollectionDAO.filterCollection(userID, genres, platforms, completionStatuses)).thenReturn(List.of(new game()));
		List<game> filteredGames = gameCollectionService.filterCollection(userID, genres, platforms, completionStatuses);
		assertNotNull(filteredGames, "Expected non-null list of filtered games");
		assertEquals(1, filteredGames.size(), "Expected one game in filtered list");
		verify(mockCollectionDAO).filterCollection(userID, genres, platforms, completionStatuses);
	}
	
	/**
	 * Validates that filtering the collection with an empty platform list works correctly.
	 * This is a positive test for TC-19 (Filter) in the test plan.
	 */
	@Test
	void testFilterCollectionWithEmptyPlatforms() {
		int userID = 123;
		List<String> genres = List.of("mockGenre");
		List<String> platforms = List.of();
		List<String> completionStatuses = List.of("Not Started");
		when(mockCollectionDAO.filterCollection(userID, genres, platforms, completionStatuses)).thenReturn(List.of(new game()));
		List<game> filteredGames = gameCollectionService.filterCollection(userID, genres, platforms, completionStatuses);
		assertNotNull(filteredGames, "Expected non-null list of filtered games");
		assertEquals(1, filteredGames.size(), "Expected one game in filtered list");
		verify(mockCollectionDAO).filterCollection(userID, genres, platforms, completionStatuses);
	}
	
	/**
	 * Validates that filtering the collection with an empty completion status list works correctly.
	 * This is a positive test for TC-19 (Filter) in the test plan.
	 */
	@Test
	void testFilterCollectionWithEmptyCompletionStatuses() {
		int userID = 123;
		List<String> genres = List.of("mockGenre");
		List<String> platforms = List.of("mockPlatform");
		List<String> completionStatuses = List.of();
		when(mockCollectionDAO.filterCollection(userID, genres, platforms, completionStatuses)).thenReturn(List.of(new game()));
		List<game> filteredGames = gameCollectionService.filterCollection(userID, genres, platforms, completionStatuses);
		assertNotNull(filteredGames, "Expected non-null list of filtered games");
		assertEquals(1, filteredGames.size(), "Expected one game in filtered list");
		verify(mockCollectionDAO).filterCollection(userID, genres, platforms, completionStatuses);
	}
	
	/**
	 * Validates that providing no filter values still returns the full collection without errors. 
	 * This is a positive test for TC-19 (Filter) in the test plan.
	 */
	@Test
	void testFilterCollectionWithEmptyAllFilters() {
		int userID = 123;
		List<String> genres = List.of();
		List<String> platforms = List.of();
		List<String> completionStatuses = List.of();
		when(mockCollectionDAO.filterCollection(userID, genres, platforms, completionStatuses)).thenReturn(List.of(new game()));
		List<game> filteredGames = gameCollectionService.filterCollection(userID, genres, platforms, completionStatuses);
		assertNotNull(filteredGames, "Expected non-null list of filtered games");
		assertEquals(1, filteredGames.size(), "Expected one game in filtered list");
		verify(mockCollectionDAO).filterCollection(userID, genres, platforms, completionStatuses);
	}
	
	/**
	 * Verifies that filtering fails safely when an invalid user ID is provided.
	 * Makes sure that the DAO method is never called.
	 * This is a negative test for TC-19 (Filter) in the test plan.
	 */
	@Test
	void testFilterCollectionWithInvalidUserID() {
		int userID = -1;
		List<String> genres = List.of("mockGenre");
		List<String> platforms = List.of("mockPlatform");
		List<String> completionStatuses = List.of("Not Started");
		List<game> filteredGames = gameCollectionService.filterCollection(userID, genres, platforms, completionStatuses);
		assertNotNull(filteredGames, "Expected non-null list of filtered games");
		assertTrue(filteredGames.isEmpty(), "Expected empty list for invalid user ID");
		verify(mockCollectionDAO, never()).filterCollection(anyInt(), anyList(), anyList(), anyList());
	}
	////////////////////////////////  testing searchCollection()   ////////////////////////////////////
	
	/**
	 * Validates that searching the collection by title works correctly.
	 * This is a positive test for TC-10 (Search Game in Collection) in the test plan.
	 */
	@Test
	void testSearchCollectionWSuccess() {
		int userID = 123;
		String title = "Zelda";
		when(mockCollectionDAO.searchCollection(userID, title)).thenReturn(List.of(new game()));
		List<game> result = gameCollectionService.searchCollection(userID, title);
		assertNotNull(result, "Expected non-null list");
		assertEquals(1, result.size(), "Expected one game in the search result");
		verify(mockCollectionDAO).searchCollection(userID, title);
	}
	
	/**
	 * Confirms that the system returns an empty result set for invalid user ID in a search operation.
	 * This is a negative test for TC-10 (Search Game in Collection) in the test plan.
	 */
	@Test
	void testSearchCollectionWithInvalidUserID() {
		int userID = -1;
		String title = "Zelda";
		List<game> result = gameCollectionService.searchCollection(userID, title);
		assertNotNull(result, "Expected non-null list even with invalid user ID");
		assertTrue(result.isEmpty(), "Expected empty list for invalid user ID");
		verify(mockCollectionDAO, never()).searchCollection(anyInt(), anyString());
	}
	
	/**
	 * Verifies that an empty title query results in an empty list and does not cause errors.
	 * This is a negative test for TC-10 (Search Game in Collection) in the test plan.
	 */
	@Test
	void testSearchCollectionWithEmptyTitle() {
		int userID = 123;
		String title = "";

		when(mockCollectionDAO.searchCollection(userID, title)).thenReturn(List.of());
		List<game> result = gameCollectionService.searchCollection(userID, title);
		assertNotNull(result, "Expected non-null result for empty title");
		assertTrue(result.isEmpty(), "Expected empty list for empty title search");
		verify(mockCollectionDAO).searchCollection(userID, title);
	}
	
	/**
	 * Ensures the service handles null return values from the DAO during a search query.
	 * This is a fault-tolerant test for TC-10 (Search Game in Collection).
	 */
	@Test
	void testSearchCollectionWhenDAOReturnsNull() {
		int userID = 123;
		String title = "Zelda";
		when(mockCollectionDAO.searchCollection(userID, title)).thenReturn(null);
		List<game> result = gameCollectionService.searchCollection(userID, title);
		assertNull(result, "Expected null if DAO returns null (optional based on your design)");
	}
	
	///////////////////////////////  testing sortFilteredCollection()  ///////////////////////////////////
	
	/**
	 * Validates in-memory sorting of a game list by title in ascending order.
	 * Supports TC-12 (Sort Collection) at the logic level after filtering.
	 */
	@Test
	void testSortByTitle() {
		List<game> games = List.of(
			new game(1, "Zelda", null, null, null, null, "Switch", null, null, null),
			new game(2, "Animal Crossing", null, null, null, null, "Switch", null, null, null)
		);
		List<game> result = gameCollectionService.sortFilteredCollection(games, "Title");
		assertEquals("Animal Crossing", result.get(0).getTitle());
		assertEquals("Zelda", result.get(1).getTitle());
	}
	
	/**
	 * Tests in-memory sorting by platform name, confirming correct lexicographical order.
	 * Supports TC-12 (Sort Collection) at the logic level after filtering.
	 */
	@Test
	void testSortByPlatform() {
		List<game> games = List.of(
			new game(1, "Game A", null, null, null, null, "PlayStation", null, null, null),
			new game(2, "Game B", null, null, null, null, "Xbox", null, null, null)
		);
		List<game> result = gameCollectionService.sortFilteredCollection(games, "Platform");
		assertEquals("PlayStation", result.get(0).getPlatform());
		assertEquals("Xbox", result.get(1).getPlatform());
	}
	
	/**
	 * Tests in-memory sorting by release date, confirming correct chronological order.
	 * Supports TC-12 (Sort Collection) at the logic level after filtering.
	 */
	@Test
	void testSortByReleaseDate() {
		List<game> games = List.of(
			new game(1, "Game A", null, null, LocalDate.of(2021, 5, 1), null, null, null, null, null),
			new game(2, "Game B", null, null, LocalDate.of(2020, 1, 1), null, null, null, null, null)
		);
		List<game> result = gameCollectionService.sortFilteredCollection(games, "Release Date");
		assertEquals(LocalDate.of(2020, 1, 1), result.get(0).getReleaseDate());
		assertEquals(LocalDate.of(2021, 5, 1), result.get(1).getReleaseDate());
	}
	
	/**
	 * Ensures that null values in release dates do not break sorting and are ordered last.
	 * Supports TC-12 (Sort Collection) at the logic level after filtering.
	 */
	@Test
	void testSortByReleaseDateWithNulls() {
		List<game> games = List.of(
			new game(1, "Game A", null, null, null, null, null, null, null, null),
			new game(2, "Game B", null, null, LocalDate.of(2022, 1, 1), null, null, null, null, null)
		);
		List<game> result = gameCollectionService.sortFilteredCollection(games, "Release Date");
		assertEquals(LocalDate.of(2022, 1, 1), result.get(0).getReleaseDate());
		assertNull(result.get(1).getReleaseDate());
	}
	
	/**
	 * Tests that an invalid sort choice does not change the order of the list.
	 * This is a negative test for TC-12 (Sort Collection) in the test plan.
	 */
	@Test
	void testSortByInvalidChoiceReturnsSameOrder() {
		List<game> games = List.of(
			new game(1, "B", null, null, null, null, "PlatformA", null, null, null),
			new game(2, "A", null, null, null, null, "PlatformB", null, null, null)
		);
		List<game> result = gameCollectionService.sortFilteredCollection(games, "InvalidChoice");
		assertEquals("B", result.get(0).getTitle());
		assertEquals("A", result.get(1).getTitle());
	}
	
	/**
	 * Tests that sorting a null game list returns an empty list.
	 * This is a negative test for TC-12 (Sort Collection) in the test plan.
	 */
	@Test
	void testSortNullGameList() {
		List<game> result = gameCollectionService.sortFilteredCollection(null, "Title");
		assertNotNull(result, "Expected empty list when input is null");
		assertTrue(result.isEmpty(), "Expected empty result for null input list");
	}
}

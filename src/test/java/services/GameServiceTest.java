package services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
 
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import database.CollectionDAO;
import database.GameDAO;
import utils.AlertHelper;

/**
 * GameServiceTest contains unit tests for verifying the functionality of the
 * GameService class in the GameGrinding application.
 *
 * This test class ensures that:
 * - All game update operations (completion status, notes, cover image URL, release date,
 *   genre, developer, publisher, title, and platform) validate inputs, interact with the
 *   GameDAO correctly, and return expected results for both success and failure scenarios.
 * - Input validation correctly rejects invalid IDs, empty strings, malformed URLs, and
 *   improperly formatted dates without invoking DAO operations unnecessarily.
 * - Retrieval methods for cover image URLs, available platforms, and genres work as expected,
 *   returning DAO results, handling null or empty responses gracefully, and avoiding crashes.
 * - Platform and genre retrieval methods use the CollectionDAO to gather collection-specific
 *   data and handle invalid user IDs by returning empty lists without DAO calls.
 * - Data integrity is preserved by ensuring that update methods only proceed when inputs
 *   pass validation checks, preventing unintended modifications.
 *
 * The tests map to various functional test cases in the project’s test plan, validating both
 * positive and negative paths to confirm correct service behavior under a range of conditions.
 *
 * Mockito is used to mock GameDAO, CollectionDAO, and AlertHelper dependencies, isolating
 * the GameService logic from database and UI components. This allows the tests to focus
 * solely on the business logic and data validation responsibilities of the service layer.
 * The goal is to verify that GameService reliably acts as a controlled intermediary between
 * controllers and DAOs, ensuring proper data handling for user game collections.
 */
class GameServiceTest {

    private GameService gameServiceUnderTest;
    private GameDAO mockGameDAO;
    private CollectionDAO mockCollectionDAO;
    private AlertHelper mockAlertHelper;

    /**
     * Sets up the mock dependencies and initializes the GameService instance before each test.
     * Mocks the GameDAO, CollectionDAO, and AlertHelper, and disables real alert messages.
     */
    @BeforeEach
    public void setUp() {
        mockGameDAO = mock(GameDAO.class);
        mockAlertHelper = mock(AlertHelper.class);
        mockCollectionDAO = mock(CollectionDAO.class);
        doNothing().when(mockAlertHelper).showError(anyString(), anyString(), anyString());
        doNothing().when(mockAlertHelper).showInfo(anyString(), anyString(), anyString());

        gameServiceUnderTest = new GameService(mockGameDAO, mockCollectionDAO);
    }
    
    /**
     * Tests that getCoverImageURL returns the correct image URL
     * retrieved from the GameDAO for the given user and game ID.
     */
    @Test
    void testGetCoverImageURL() {
    	int userID = 1;
    	int gameID = 1;
    	String expectedURL = "http://example.com/image.jpg";
    	when(mockGameDAO.getCoverImageURL(userID, gameID)).thenReturn(expectedURL);
    	String actualURL = gameServiceUnderTest.getCoverImageURL(userID, gameID);
    	assertEquals(expectedURL, actualURL);
    	verify(mockGameDAO).getCoverImageURL(userID, gameID); 
    }
    
    //////////////////////// testing updateCompletionStatus() /////////////////////////////
    
    /**
     * Tests that updateCompletionStatus returns true when GameDAO successfully updates the status.
     */
    @Test 
    void testUpdateCompletionStatus_Success() {
		int userID = 1;
		int gameID = 1;
		String completionStatus = "Completed";		
		when(mockGameDAO.updateCompletionStatus(userID, gameID, completionStatus)).thenReturn(true);
		boolean result = gameServiceUnderTest.updateCompletionStatus(userID, gameID, completionStatus);		
		assertTrue(result);
		verify(mockGameDAO).updateCompletionStatus(userID, gameID, completionStatus);
	}
    
    /**
     * Tests that updateCompletionStatus returns false when GameDAO fails to update the status.
     */
    @Test
    void testUpdateCompletionStatus_Failed() {
    	int userID = 1;
    	int gameID = 1;
    	String completionStatus = "Completed";
    	when(mockGameDAO.updateCompletionStatus(userID, gameID, completionStatus)).thenReturn(false);
		boolean result = gameServiceUnderTest.updateCompletionStatus(userID, gameID, completionStatus);	
		assertFalse(result);
		verify(mockGameDAO).updateCompletionStatus(userID, gameID, completionStatus);   			
    }
    
    /**
     * Tests that updateCompletionStatus returns false when given an invalid completion status.
     */
    @Test
    void testUpdateCompletionStatus_InvalidCompletionStatus() {
    	int userID = 1;
    	int gameID = 1;
    	String completionStatus = "Thrown Away";
		boolean result = gameServiceUnderTest.updateCompletionStatus(userID, gameID, completionStatus);	
		assertFalse(result, "Expected failure when using an invalid completion status.");
    }
    
    ///////////////////////// testing updateNotes() //////////////////////////////////////
    
    /**
     * Tests that updateNotes returns true when GameDAO successfully updates the notes.
     */
    @Test
    void testUpdateNotes_Success() {
    	int userID = 1;
    	int gameID = 1; 
    	String notes = "This is a test.";
    	when(mockGameDAO.updateNotes(userID, gameID, notes)).thenReturn(true);
    	boolean result = gameServiceUnderTest.updateNotes(userID, gameID, notes);
    	assertTrue(result);
    	verify(mockGameDAO).updateNotes(userID, gameID, notes);
    }
    
    /**
     * Tests that updateNotes returns false when GameDAO fails to update the notes.
     */
    @Test
    void testUpdateNotes_Failed() {
    	int userID = 1;
    	int gameID = 1; 
    	String notes = "This is a test.";
    	when(mockGameDAO.updateNotes(userID, gameID, notes)).thenReturn(false);
    	boolean result = gameServiceUnderTest.updateNotes(userID, gameID, notes);
    	assertFalse(result, "Expected failure when updating notes");
    	verify(mockGameDAO).updateNotes(userID, gameID, notes);
    }
    
    ////////////////////// testing updateCoverImageURL() //////////////////////////////////
    
    /**
     * Tests that updateCoverImageURL returns true when the image URL is valid and DAO update succeeds.
     */
    @Test 
    void testUpdateCoverImageURL_Succes() {
    	int userID = 1;
    	int gameID = 1; 
    	String url = "http://example.com/image.jpg";
    	when(mockGameDAO.updateCoverImageURL(userID, gameID, url)).thenReturn(true);
    	boolean result = gameServiceUnderTest.updateCoverImageURL(userID, gameID, url);
    	assertTrue(result, "Cover image update should succeed when DAO returns true.");
    	verify(mockGameDAO).updateCoverImageURL(userID, gameID, url);    			
    }
    
    /**
     * Tests that updateCoverImageURL returns false when the DAO fails to update the image URL.
     */
    @Test
    void testUpdateCoverImageURL_Failure() {
        int userID = 1;
        int gameID = 1;
        String url = "http://example.com/image.jpg";
        when(mockGameDAO.updateCoverImageURL(userID, gameID, url)).thenReturn(false);
        boolean result = gameServiceUnderTest.updateCoverImageURL(userID, gameID, url);
        assertFalse(result, "Cover image update should fail when DAO returns false.");
    	verify(mockGameDAO).updateCoverImageURL(userID, gameID, url);  
    }
    
    /**
     * Tests that updateCoverImageURL returns false when the input URL is invalid.
     */
    @Test
    void testUpdateCoverImageURL_InvalidURL() {
		int userID = 1;
		int gameID = 1; 
		String url = "invalid-url";
		boolean result = gameServiceUnderTest.updateCoverImageURL(userID, gameID, url);
		assertFalse(result, "Expected failure when using an invalid URL.");
	}
    
    /**
     * Tests that updateCoverImageURL returns false when the input URL is empty.
     */
    @Test
    void testUpdateCoverImageURL_EmptyURL() {
    	int userID = 1;
    	int gameID = 1;
    	String url = "";
    	boolean result = gameServiceUnderTest.updateCoverImageURL(userID, gameID, url);
    	assertFalse(result, "Expected failure when using an empty URL.");
    }

    ////////////////// testing updateReleaseDate() //////////////////////////
    
    /**
     * Tests that updateReleaseDate returns true when the release date is valid and DAO update succeeds.
     */
    @Test
    void testUpdateReleaseDate_Success() {
		int userID = 1;
		int gameID = 1; 
		String releaseDate = "2023-10-01";
		when(mockGameDAO.updateReleaseDate(userID, gameID, releaseDate)).thenReturn(true);
		boolean result = gameServiceUnderTest.updateReleaseDate(userID, gameID, releaseDate);
		assertTrue(result);
		verify(mockGameDAO).updateReleaseDate(userID, gameID, releaseDate); 
	}
	
    /**
	 * Tests that updateReleaseDate returns false when the DAO fails to update the release date.
	 */
	@Test
	void testUpdateReleaseDate_Failure() {
		int userID = 1;
		int gameID = 1; 
		String releaseDate = "2023-10-01";
		when(mockGameDAO.updateReleaseDate(userID, gameID, releaseDate)).thenReturn(false);
		boolean result = gameServiceUnderTest.updateReleaseDate(userID, gameID, releaseDate);
		assertFalse(result);
		verify(mockGameDAO).updateReleaseDate(userID, gameID, releaseDate); 
	}
	
	/**
	 * Tests that updateReleaseDate returns false when the input release date is invalid.
	 */
	@Test
	void testUpdateReleaseDate_InvalidDate() {
		int userID = 1;
		int gameID = 1; 
		String releaseDate = "invalid-date";
		boolean result = gameServiceUnderTest.updateReleaseDate(userID, gameID, releaseDate);
		assertFalse(result, "Expected failure when using an invalid date.");
	}
	
	/**
	 * Tests that updateReleaseDate returns false when the release date is empty.
	 */
	@Test
	void testUpdateReleaseDate_EmptyDate() {
		int userID = 1;
		int gameID = 1; 
		String releaseDate = "";
		boolean result = gameServiceUnderTest.updateReleaseDate(userID, gameID, releaseDate);
		assertFalse(result, "Expected failure when using an empty date.");
	}
	
	////////////////////// testing updateGenre() ////////////////////////////
	
	/**
	 * Tests that updateGenre returns true when a valid genre is provided and DAO update succeeds.
	 */
	@Test
	void testUpdateGenre_Success() {
		int userID = 1;
		int gameID = 1; 
		String genre = "Action";
		when(mockGameDAO.updateGenre(userID, gameID, genre)).thenReturn(true);
		boolean result = gameServiceUnderTest.updateGenre(userID, gameID, genre);
		assertTrue(result);
		verify(mockGameDAO).updateGenre(userID, gameID, genre); 
	}
	
	/**
	 * Tests that updateGenre returns false when the DAO fails to update the genre.
	 */
	@Test
	void testUpdateGenre_Failure() {
		int userID = 1;
		int gameID = 1; 
		String genre = "Action";
		when(mockGameDAO.updateGenre(userID, gameID, genre)).thenReturn(false);
		boolean result = gameServiceUnderTest.updateGenre(userID, gameID, genre);
		assertFalse(result);
		verify(mockGameDAO).updateGenre(userID, gameID, genre); 
	}
	
	/**
	 * Tests that updateGenre returns false when the genre string is empty.
	 */
	@Test
	void testUpdateGenre_EmptyGenre() {
		int userID = 1;
		int gameID = 1; 
		String genre = "";
		boolean result = gameServiceUnderTest.updateGenre(userID, gameID, genre);
		assertFalse(result, "Expected failure when using an empty genre.");
	}
	
	/**
	 * Tests that updateGenre returns false when the user ID is invalid (negative).
	 */
	@Test
	void testUpdateGenre_InvalidUserID() {
		int userID = -1;
		int gameID = 1; 
		String genre = "Action";
		boolean result = gameServiceUnderTest.updateGenre(userID, gameID, genre);
		assertFalse(result, "Expected failure when using an invalid user ID.");
	}
	
	/**
	 * Tests that updateGenre returns false when an invalid game ID is provided.
	 */
	@Test
	void testUpdateGenre_InvalidGameID() {
		int userID = 1;
		int gameID = -1; 
		String genre = "Action";
		boolean result = gameServiceUnderTest.updateGenre(userID, gameID, genre);
		assertFalse(result, "Expected failure when using an invalid game ID.");
	}
	
	/////////////////////// testing updateDeveloper() ////////////////////////////
	
	/**
	 * Tests that updateDeveloper returns true when the developer update is successful.
	 */
	@Test
	void testUpdateDeveloper_Success() {
		int userID = 1;
		int gameID = 1; 
		String developer = "Game Studio";
		when(mockGameDAO.updateDeveloper(userID, gameID, developer)).thenReturn(true);
		boolean result = gameServiceUnderTest.updateDeveloper(userID, gameID, developer);
		assertTrue(result);
		verify(mockGameDAO).updateDeveloper(userID, gameID, developer); 
	}
	
	/**
	 * Tests that updateDeveloper returns false when the DAO update fails.
	 */
	@Test
	void testUpdateDeveloper_Failure() {
		int userID = 1;
		int gameID = 1; 
		String developer = "Game Studio";
		when(mockGameDAO.updateDeveloper(userID, gameID, developer)).thenReturn(false);
		boolean result = gameServiceUnderTest.updateDeveloper(userID, gameID, developer);
		assertFalse(result);
		verify(mockGameDAO).updateDeveloper(userID, gameID, developer); 
	}
	
	/**
	 * Tests that updateDeveloper returns false when the developer string is empty.
	 */
	@Test
	void testUpdateDeveloper_EmptyDeveloper() {
		int userID = 1;
		int gameID = 1; 
		String developer = "";
		boolean result = gameServiceUnderTest.updateDeveloper(userID, gameID, developer);
		assertFalse(result, "Expected failure when using an empty developer.");
	}
	
	/**
	 * Tests that updateDeveloper returns false when an invalid user ID is provided.
	 */
	@Test
	void testUpdateDeveloper_InvalidUserID() {
		int userID = -1;
		int gameID = 1; 
		String developer = "Game Studio";
		boolean result = gameServiceUnderTest.updateDeveloper(userID, gameID, developer);
		assertFalse(result, "Expected failure when using an invalid user ID.");
	}
	
	/**
	 * Tests that updateDeveloper returns false when an invalid game ID is provided.
	 */
	@Test
	void testUpdateDeveloper_InvalidGameID() {
		int userID = 1;
		int gameID = -1; 
		String developer = "Game Studio";
		boolean result = gameServiceUnderTest.updateDeveloper(userID, gameID, developer);
		assertFalse(result, "Expected failure when using an invalid game ID.");
	}
	
	///////////////////////// testing updatePublisher() ////////////////////////////
	
	/**
	 * Tests that updatePublisher returns true when the publisher update is successful.
	 */
	@Test
	void testUpdatePublisher_Success() {
		int userID = 1;
		int gameID = 1; 
		String publisher = "Game Publisher";
		when(mockGameDAO.updatePublisher(userID, gameID, publisher)).thenReturn(true);
		boolean result = gameServiceUnderTest.updatePublisher(userID, gameID, publisher);
		assertTrue(result);
		verify(mockGameDAO).updatePublisher(userID, gameID, publisher); 
	}
	
	/**
	 * Tests that updatePublisher returns false when the DAO update fails.
	 */
	@Test
	void testUpdatePublisher_Failure() {
		int userID = 1;
		int gameID = 1; 
		String publisher = "Game Publisher";
		when(mockGameDAO.updatePublisher(userID, gameID, publisher)).thenReturn(false);
		boolean result = gameServiceUnderTest.updatePublisher(userID, gameID, publisher);
		assertFalse(result);
		verify(mockGameDAO).updatePublisher(userID, gameID, publisher); 
	}
	
	/**
	 * Tests that updatePublisher returns false when the publisher string is empty.
	 */
	@Test
	void testUpdatePublisher_EmptyPublisher() {
		int userID = 1;
		int gameID = 1; 
		String publisher = "";
		boolean result = gameServiceUnderTest.updatePublisher(userID, gameID, publisher);
		assertFalse(result, "Expected failure when using an empty publisher.");
	}
	
	////////////////// testing updateTitle() ////////////////////////////
	
	/**
	 * Tests that updateTitle returns true when the title update is successful.
	 */
	@Test
	void testUpdateTitle_Success() {
		int userID = 1;
		int gameID = 1; 
		String title = "New Game Title";
		when(mockGameDAO.updateTitle(userID, gameID, title)).thenReturn(true);
		boolean result = gameServiceUnderTest.updateTitle(userID, gameID, title);
		assertTrue(result);
		verify(mockGameDAO).updateTitle(userID, gameID, title); 
	}
	
	/**
	 * Tests that updateTitle returns false when the DAO update fails.
	 */
	@Test
	void testUpdateTitle_Failure() {
		int userID = 1;
		int gameID = 1; 
		String title = "New Game Title";
		when(mockGameDAO.updateTitle(userID, gameID, title)).thenReturn(false);
		boolean result = gameServiceUnderTest.updateTitle(userID, gameID, title);
		assertFalse(result);
		verify(mockGameDAO).updateTitle(userID, gameID, title); 
	}
	
	/**
	 * Tests that updateTitle returns false when the title string is empty.
	 */
	@Test
	void testUpdateTitle_EmptyTitle() {
		int userID = 1;
		int gameID = 1; 
		String title = "";
		boolean result = gameServiceUnderTest.updateTitle(userID, gameID, title);
		assertFalse(result, "Expected failure when using an empty title.");
	}
	
	///////////////// testing updatePlatform() ////////////////////////////
	
	/**
	 * Tests that updatePlatform returns true when the platform update is successful.
	 */
	@Test
	void testUpdatePlatform_Success() {
		int userID = 1;
		int gameID = 1; 
		String platform = "PC";
		when(mockGameDAO.updatePlatform(userID, gameID, platform)).thenReturn(true);
		boolean result = gameServiceUnderTest.updatePlatform(userID, gameID, platform);
		assertTrue(result);
		verify(mockGameDAO).updatePlatform(userID, gameID, platform); 
	}
	
	/**
	 * Tests that updatePlatform returns false when the DAO update fails.
	 */
	@Test
	void testUpdatePlatform_Failure() {
		int userID = 1;
		int gameID = 1; 
		String platform = "PC";
		when(mockGameDAO.updatePlatform(userID, gameID, platform)).thenReturn(false);
		boolean result = gameServiceUnderTest.updatePlatform(userID, gameID, platform);
		assertFalse(result);
		verify(mockGameDAO).updatePlatform(userID, gameID, platform); 
	}
	
	/**
	 * Tests that updatePlatform returns false when an empty platform string is provided.
	 */
	@Test
	void testUpdatePlatform_EmptyPlatform() {
		int userID = 1;
		int gameID = 1; 
		String platform = "";
		boolean result = gameServiceUnderTest.updatePlatform(userID, gameID, platform);
		assertFalse(result, "Expected failure when using an empty platform.");
	}
	
	/////////////////// testing getPlatformsFromCollection() ////////////////////////////
	
	/**
	 * Tests that getPlatformsFromCollection returns the expected list of platforms when successful.
	 */
	@Test
	void testGetPlatformsFromCollection_Success() {
		int userID = 1;
		List<String> expectedPlatforms = List.of("PC", "PlayStation", "XBox");
		when(mockCollectionDAO.getCollectionPlatform(userID, null)).thenReturn(expectedPlatforms);
		List<String> result = gameServiceUnderTest.getPlatformsFromCollection(userID);
		assertEquals(expectedPlatforms, result);
		verify(mockCollectionDAO).getCollectionPlatform(userID, null); 
	}
	
	/**
	 * Tests that getPlatformsFromCollection returns an empty list when the DAO returns null.
	 */
	@Test
	void testGetPlatformsFromCollection_Failure() {
	    int userID = 1;
	    when(mockCollectionDAO.getCollectionPlatform(userID, null)).thenReturn(null);
	    List<String> result = gameServiceUnderTest.getPlatformsFromCollection(userID);
	    assertNotNull(result, "Result should not be null even if no platforms are found.");
	    assertTrue(result.isEmpty(), "Result list should be empty when no platforms are found.");
	    verify(mockCollectionDAO).getCollectionPlatform(userID, null);
	}
	
	/**
	 * Tests that getPlatformsFromCollection returns an empty list when an invalid user ID is provided.
	 */
	@Test
	void testGetPlatformsFromCollection_InvalidUserID() {
		int userID = -1;
		List<String> result = gameServiceUnderTest.getPlatformsFromCollection(userID);
		assertNotNull(result, "Result should not be null even if user ID is invalid.");
		assertTrue(result.isEmpty(), "Result list should be empty when user ID is invalid.");
	}
	
	/**
	 * Tests that getPlatformsFromCollection returns an empty list when the DAO returns an empty list.
	 */
	@Test
	void testGetPlatformsFromCollection_EmptyPlatformList() {
		int userID = 1;
		when(mockCollectionDAO.getCollectionPlatform(userID, null)).thenReturn(List.of());
		List<String> result = gameServiceUnderTest.getPlatformsFromCollection(userID);
		assertNotNull(result, "Result should not be null even if no platforms are found.");
		assertTrue(result.isEmpty(), "Result list should be empty when no platforms are found.");
		verify(mockCollectionDAO).getCollectionPlatform(userID, null); 
	}
	
	/**
	 * Tests that getPlatformsFromCollection returns an empty list when the DAO returns null.
	 */
	@Test
	void testGetPlatformsFromCollection_NullPlatformList() {
		int userID = 1;
		when(mockCollectionDAO.getCollectionPlatform(userID, null)).thenReturn(null);
		List<String> result = gameServiceUnderTest.getPlatformsFromCollection(userID);
		assertNotNull(result, "Result should not be null even if no platforms are found.");
		assertTrue(result.isEmpty(), "Result list should be empty when no platforms are found.");
		verify(mockCollectionDAO).getCollectionPlatform(userID, null); 
	}
	
	//////////////////// testing getGenreFromCollection() ////////////////////////////
	
	/**
	 * Tests that getGenreFromCollection returns the expected list of genres when successful.
	 */
	@Test
	void testGetGenreFromCollection_Success() {
		int userID = 1;
		List<String> expectedGenres = List.of("Action", "Adventure", "RPG");
		when(mockCollectionDAO.getCollectionGenres(userID, null)).thenReturn(expectedGenres);
		List<String> result = gameServiceUnderTest.getGenreFromCollection(userID);
		assertEquals(expectedGenres, result);
		verify(mockCollectionDAO).getCollectionGenres(userID, null); 
	}
	
	/**
	 * Tests that getGenreFromCollection returns an empty list when the DAO returns null.
	 */
	@Test
	void testGetGenreFromCollection_Failure() {
		int userID = 1;
		when(mockCollectionDAO.getCollectionGenres(userID, null)).thenReturn(null);
		List<String> result = gameServiceUnderTest.getGenreFromCollection(userID);
		assertNotNull(result, "Result should not be null even if no genres are found.");
		assertTrue(result.isEmpty(), "Result list should be empty when no genres are found.");
		verify(mockCollectionDAO).getCollectionGenres(userID, null); 
	}
	
	/**
	 * Tests that getGenreFromCollection returns an empty list when an invalid user ID is provided.
	 */
	@Test
	void testGetGenreFromCollection_InvalidUserID() {
		int userID = -1;
		List<String> result = gameServiceUnderTest.getGenreFromCollection(userID);
		assertNotNull(result, "Result should not be null even if user ID is invalid.");
		assertTrue(result.isEmpty(), "Result list should be empty when user ID is invalid.");
	}
	
	/**
	 * Tests that getGenreFromCollection returns an empty list when the DAO returns an empty list.
	 */
	@Test
	void testGetGenreFromCollection_EmptyGenreList() {
		int userID = 1;
		when(mockCollectionDAO.getCollectionGenres(userID, null)).thenReturn(List.of());
		List<String> result = gameServiceUnderTest.getGenreFromCollection(userID);
		assertNotNull(result, "Result should not be null even if no genres are found.");
		assertTrue(result.isEmpty(), "Result list should be empty when no genres are found.");
		verify(mockCollectionDAO).getCollectionGenres(userID, null); 
	}
	
	/**
	 * Tests that getGenreFromCollection returns an empty list when the DAO returns null.
	 */
	@Test
	void testGetGenreFromCollection_NullGenreList() {
		int userID = 1;
		when(mockCollectionDAO.getCollectionGenres(userID, null)).thenReturn(null);
		List<String> result = gameServiceUnderTest.getGenreFromCollection(userID);
		assertNotNull(result, "Result should not be null even if no genres are found.");
		assertTrue(result.isEmpty(), "Result list should be empty when no genres are found.");
		verify(mockCollectionDAO).getCollectionGenres(userID, null); 
	}
}

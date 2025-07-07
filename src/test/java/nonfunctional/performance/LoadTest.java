package nonfunctional.performance;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.junit.jupiter.api.Test;

import database.CollectionDAO;
import models.game;

/**
 * Performance test suite for validating that the application can handle large game collections (up to 5,000 games) efficiently.
 * These tests help verify non-functional requirement US-3:
 * The application must perform loading, searching, and sorting actions on a 5,000 game collection in under 3 seconds.
 */ 
class LoadTest {

	/**
	 * Tests the performance of loading 5,000 games from the database.
     * Verifies the operation completes in under 3 seconds and returns all games.
     * 
	 * @throws SQLException if an error occurs while accessing the in-memory database
	 */
	@Test
	void testLoad5000GamesPerformance() throws SQLException {
	    Connection connection = DriverManager.getConnection("jdbc:sqlite::memory:");
	    CollectionDAO dao = new CollectionDAO(connection);
	    int userID = 1;

	    Statement stmt = connection.createStatement();
	    stmt.executeUpdate("CREATE TABLE Game (GameID INTEGER PRIMARY KEY AUTOINCREMENT, Title TEXT, Developer TEXT, Publisher TEXT, ReleaseDate TEXT, Genre TEXT, CompletionStatus TEXT, Notes TEXT, CoverArt TEXT, Platform TEXT)");
	    stmt.executeUpdate("CREATE TABLE UserGameCollection (UserID INTEGER, GameID INTEGER)");

	    // Insert 5000 games
	    for (int i = 0; i < 5000; i++) {
	        dao.addGameToCollection(userID, 0, "Game " + i, "Dev", "Pub", "2024-01-01", "Action", "PC", "Not Started", "Notes", "");
	    }

	    long start = System.currentTimeMillis();
	    List<game> games = dao.getAllGamesInCollection(userID);
	    long end = System.currentTimeMillis();

	    long duration = end - start;
	    System.out.println("Loaded " + games.size() + " games in " + duration + "ms");

	    assertTrue(duration < 3000, "Load time should be under 3 seconds");
	    assertEquals(5000, games.size(), "Expected 5000 games in collection");
	}

    /**
     * Tests the performance of searching for a specific game title in a 5,000 game collection. 
     * Ensures the search executes in under 3 seconds.
     * 
     * @throws SQLException if an error occurs while accessing the in-memory database
     */
	@Test
	void testSearch5000GamesPerformance() throws SQLException {
	    Connection connection = DriverManager.getConnection("jdbc:sqlite::memory:");
	    CollectionDAO dao = new CollectionDAO(connection);
	    int userID = 1;

	    Statement stmt = connection.createStatement();
	    stmt.executeUpdate("CREATE TABLE Game (GameID INTEGER PRIMARY KEY AUTOINCREMENT, Title TEXT, Developer TEXT, Publisher TEXT, ReleaseDate TEXT, Genre TEXT, CompletionStatus TEXT, Notes TEXT, CoverArt TEXT, Platform TEXT)");
	    stmt.executeUpdate("CREATE TABLE UserGameCollection (UserID INTEGER, GameID INTEGER)");

	    // Insert 5000 games
	    for (int i = 0; i < 5000; i++) {
	        dao.addGameToCollection(userID, 0, "Game " + i, "Dev", "Pub", "2024-01-01", "Action", "PC", "Not Started", "", "");
	    }

	    long start = System.currentTimeMillis();
	    List<game> results = dao.searchCollection(userID, "Game 4999");
	    long end = System.currentTimeMillis();

	    long duration = end - start;
	    System.out.println("Search returned " + results.size() + " results in " + duration + "ms");

	    assertTrue(duration < 3000, "Search time should be under 3 seconds");
	    assertFalse(results.isEmpty(), "Search result should not be empty");
	}

    /**
     * Tests the performance of sorting 5,000 games alphabetically by title.
     * Verifies that the sort operation completes in under 3 seconds and that the first and last titles are correctly ordered.
     * 
     * @throws SQLException if an error occurs while accessing the in-memory database
     */
	@Test
	void testSort5000GamesPerformance() throws SQLException {
	    Connection connection = DriverManager.getConnection("jdbc:sqlite::memory:");
	    CollectionDAO dao = new CollectionDAO(connection);
	    int userID = 1;

	    Statement stmt = connection.createStatement();
	    stmt.executeUpdate("CREATE TABLE Game (GameID INTEGER PRIMARY KEY AUTOINCREMENT, Title TEXT, Developer TEXT, Publisher TEXT, ReleaseDate TEXT, Genre TEXT, CompletionStatus TEXT, Notes TEXT, CoverArt TEXT, Platform TEXT)");
	    stmt.executeUpdate("CREATE TABLE UserGameCollection (UserID INTEGER, GameID INTEGER)");

	    for (int i = 0; i < 5000; i++) {
	        String title = String.format("Game %05d", 5000 - i);
	        dao.addGameToCollection(userID, 0, title, "Dev", "Pub", "2024-01-01", "Action", "PC", "Not Started", "", "");
	    }


	    long start = System.currentTimeMillis();
	    List<game> sortedGames = dao.sortCollection(userID, "Title", "");
	    long end = System.currentTimeMillis();

	    long duration = end - start;
	    System.out.println("Sort returned " + sortedGames.size() + " results in " + duration + "ms");

	    assertTrue(duration < 3000, "Sort time should be under 3 seconds");
	    assertEquals(5000, sortedGames.size(), "Expected 5000 sorted games");

	    assertEquals("Game 00001", sortedGames.get(0).getTitle());
	    assertEquals("Game 05000", sortedGames.get(4999).getTitle());

	}


}

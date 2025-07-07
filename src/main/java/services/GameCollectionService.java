package services;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.hc.core5.http.ParseException;

import database.CollectionDAO;
import database.GameDAO;
import models.game;
import security.AuthManager;
import utils.AlertHelper;

/**
 * Service class responsible for managing user game collections.
 * Acts as a mediator between controllers/UI and the DAO layer.
 * Handles logic for adding, filtering, searching, and sorting games in a user's collection.
 */
public class GameCollectionService {
	
	private static GameCollectionService instance;         			// Singleton instance
	private CollectionDAO collectionDAO = new CollectionDAO();
	private int loggedInUserID = -1;								// ID of the currently logged-in user
	
    private GameDAO gameDAO;
    private AuthManager authManager;
    private AlertHelper alert;

    
	/** Default constructor */
	public GameCollectionService() {}
	 
    public GameCollectionService(CollectionDAO collectionDAO, AuthManager authManager, AlertHelper alert) {
        this.collectionDAO = collectionDAO;
        this.authManager = authManager;
        this.alert = alert;
    }
	
    /**
	 * Singleton pattern to ensure only one instance of GameCollectionService exists.
	 * 
	 * @return The singleton instance of GameCollectionService.
	 */
    public static GameCollectionService getInstance() {
        if (instance == null) {
            instance = new GameCollectionService();
        }
        return instance;
    }
	
    /**
	 * Sets the current user ID.
	 * 
	 * @param userID The ID of the logged-in user.
	 */
    public void setCurrentUserID(int userID) {
        this.loggedInUserID = userID;
    }
    
    /**
     * Gets the current user ID.
     * 
     * @return The ID of the logged-in user.
     */
    public int getCurrentUserID() {
        return loggedInUserID;
    }
    
	
    /**
     * Adds a game to the specified user's collection.
     * Formats the release date and uses default values for notes and status.
     *
     * @param game    The game to add.
     * @param userID  The user ID who owns the collection.
     * @return true if successfully added, false otherwise.
     * 
     * @throws ParseException if there is an error parsing the date.
     * @throws InterruptedException if the thread is interrupted.
     * @throws ExecutionException if there is an error during execution.
     */
	public boolean addGameToCollection(game game, int userID) throws ParseException, InterruptedException, ExecutionException {
		if (userID <= 0) {
			System.out.println("No valid user is logged in.");
			return false;
		}

	    String releaseDateString = "Unknown";
	    if (game.getReleaseDate() != null) {
	        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	        releaseDateString = game.getReleaseDate().format(formatter);
	    }
	    String completionStatus = "Not Started";
	    String notes = " ";

		return collectionDAO.addGameToCollection(
				userID,
				game.getGameID(),
				game.getTitle(),
				game.getDeveloper(),
				game.getPublisher(),
				releaseDateString,
				game.getGenre(),
				game.getPlatform(),
				completionStatus,
				notes,
				game.getCoverImageUrl()
		);
	}
	
    /**
     * Retrieves all games in a user's collection.
     *
     * @param userID The user ID.
     * @return A list of games in the collection.
     */
    public List<game> getUserCollection(int userID) { 
        if (userID <= 0) {
            System.out.println("Invalid user ID.");
            return List.of();
        }
        System.out.println("Fetching collection for user ID: " + userID);
        List<game> games = collectionDAO.getAllGamesInCollection(userID);
        return (games != null) ? games : List.of();
    }
    
    /**
	 * Sorts the user's game collection based on the specified criteria.
	 *
	 * @param userID The user ID.
	 * @param sortBy The sorting criteria (e.g., title, release date).
	 * @param searchQuery Optional search filter.
	 * @return A sorted list of games in the collection.
	 */
    public List<game> sortCollection(int userID, String sortBy, String searchQuery) {
        System.out.println("Sorting by " + sortBy + "...");
        return collectionDAO.sortCollection(userID, sortBy, searchQuery);
    }
    
    /**
     * Removes a game from the user's collection.
     * 
     * @param gameID The ID of the game to remove.
     * @param userID The user ID.
     */
    public boolean removeGameFromCollection(int userID, int gameID) {
		if (userID <= 0) {
			System.out.println("Invalid user ID.");
			return false;
		}
		return collectionDAO.removeGameFromCollection(userID, gameID);
	}
    
    /**
	 * Filters the user's game collection based on specified criteria.
	 *
	 * @param userID The user ID.
	 * @param genres List of genres to filter by.
	 * @param platforms List of platforms to filter by.
	 * @param completionStatuses List of completion statuses to filter by.
	 * @return A list of filtered games.
	 */
    public List<game> filterCollection(int userID, List<String> genres, List<String> platforms, List<String> completionStatuses) {
        System.out.println("Filtering collection for user ID: " + userID);
        
        if (userID <= 0) {
            System.err.println("ERROR: Invalid user ID. Aborting filter.");
            return new ArrayList<>();
        }
        return collectionDAO.filterCollection(userID, genres, platforms, completionStatuses);
    }
    
    /**
     * Searches a user's collection by game title.
     *
     * @param userID The user ID.
     * @param title  The title or partial title of the game.
     * @return List of matching games.
     */
    public List<game> searchCollection(int userID, String title){
    	System.out.println("Searching for " + title);
    	if(userID <= 0) {
    		System.err.println("ERROR: Invalid user ID. Aborting sorting. User ID is " + userID);
    		return new ArrayList<>();
    	}
    	return collectionDAO.searchCollection(userID, title);
    }
    
    /**
     * Sorts an already-filtered list of games based on the selected field.
     *
     * @param games The list of games to sort.
     * @param sortChoice The field to sort by (e.g., Title, Release Date, Platform).
     * @return Sorted list of games.
     */
    public List<game> sortFilteredCollection(List<game> games, String sortChoice) {
        if (games == null) return new ArrayList<>();

        return games.stream()
            .sorted((g1, g2) -> {
                switch (sortChoice) {
                    case "Title":
                    case "Alphabetical":
                        return g1.getTitle().compareToIgnoreCase(g2.getTitle());
                    case "Release Date":
                        LocalDate date1 = g1.getReleaseDate();
                        LocalDate date2 = g2.getReleaseDate();
                        if (date1 == null && date2 == null) return 0;
                        if (date1 == null) return 1; 
                        if (date2 == null) return -1;
                        return date1.compareTo(date2);
                    case "Platform":
                        return g1.getPlatform().compareToIgnoreCase(g2.getPlatform());
                    default:
                        return 0;
                }
            })
            .toList();
    }



}

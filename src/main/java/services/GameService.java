package services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import database.CollectionDAO;
import database.GameDAO;

/**
 * Service class for managing individual game metadata and field updates in a user's collection.
 * Also provides utilities for retrieving and processing platform and genre data from a user's games.
 */
public class GameService {

	
	private final GameDAO gameDAO = new GameDAO();
	private final CollectionDAO collectionDAO = new CollectionDAO();
	
	/** Default constructor */
	public GameService() {	}
	
    /**
     * Retrieves the cover image URL of a specific game.
     *
     * @param userID The ID of the user.
     * @param gameID The ID of the game.
     * @return The cover image URL.
     */
	public String getCoverImageURL(int userID, int gameID) {
		return gameDAO.getCoverImageURL(userID, gameID);
	}
	
	/**
	 * Updates the completion status of a specific game for a user.
	 * 
	 * @param userID The ID of the user.
	 * @param gameID The ID of the game.
	 * @param completionStatus The new completion status.
	 * @return true if the update was successful, false otherwise.
	 */
	public boolean updateCompletionStatus(int userID, int gameID, String completionStatus) {
		if( gameDAO.updateCompletionStatus(userID, gameID, completionStatus)) {
			System.out.println("Completion status updated successfully.");
			return true;
		} else {
			System.out.println("Failed to update completion status.");
			return false;
		}	
	}
	
	/**
	 * Updates the notes for a specific game for a user.
	 * 
	 * @param userID The ID of the user.
	 * @param gameID The ID of the game.
	 * @param notes The new notes.
	 * @return true if the update was successful, false otherwise.
	 */
	public boolean updateNotes(int userID, int gameID, String notes) {
		if(gameDAO.updateNotes(userID, gameID, notes)) {
			System.out.println("Notes updated successfully.");
			return true;
		} else {
			System.out.println("Failed to update notes.");
			return false;
		}
	}
	
	/**
	 * Updates the cover image URL for a specific game for a user.
	 * 
	 * @param userID The ID of the user.
	 * @param gameID The ID of the game.
	 * @param url The new cover image URL.
	 * @return true if the update was successful, false otherwise.
	 */
	public boolean updateCoverImageURL(int userID, int gameID, String url) {
        if (gameDAO.updateCoverImageURL(userID, gameID, url)) {
            System.out.println("Cover image updated successfully.");
            return true;
        } else {
            System.out.println("Failed to update cover image.");
            return false;
        }
    }
	
	/**
	 * Updates the release date for a specific game for a user.
	 * 
	 * @param userID The ID of the user.
	 * @param gameID The ID of the game.
	 * @param releaseDate The new release date.
	 * @return true if the update was successful, false otherwise.
	 */
    public boolean updateReleaseDate(int userID, int gameID, String releaseDate) {
        if (gameDAO.updateReleaseDate(userID, gameID, releaseDate)) {
            System.out.println("Release date updated successfully.");
            return true;
        } else {
            System.out.println("Failed to update release date.");
            return false;
        }
    }
    
    /**
	 * Updates the genre(s) for a specific game for a user.
	 * 
	 * @param userID The ID of the user.
	 * @param gameID The ID of the game.
	 * @param genre The new genre.
	 * @return true if the update was successful, false otherwise.
	 */
    public boolean updateGenre(int userID, int gameID, String genre) {
        if (gameDAO.updateGenre(userID, gameID, genre)) {
            System.out.println("Genre updated successfully.");
            return true;
        } else {
            System.out.println("Failed to update genre.");
            return false;
        }
    }
    
    /**
     * Updates the developer(s) for a specific game for a user.
     * 
     * @param userID The ID of the user.
     * @param gameID The ID of the game.
     * @param developer The new developer.
     * @return true if the update was successful, false otherwise.
     */
    public boolean updateDeveloper(int userID, int gameID, String developer) {
        if (gameDAO.updateDeveloper(userID, gameID, developer)) {
            System.out.println("Developer updated successfully.");
            return true;
        } else {
            System.out.println("Failed to update developer.");
            return false;
        }
    }
    
    /**
	 * Updates the publisher(s) for a specific game for a user.
	 * 
	 * @param userID The ID of the user.
	 * @param gameID The ID of the game.
	 * @param publisher The new publisher.
	 * @return true if the update was successful, false otherwise.
	 */
    public boolean updatePublisher(int userID, int gameID, String publisher) {
        if (gameDAO.updatePublisher(userID, gameID, publisher)) {
            System.out.println("Publisher updated successfully.");
            return true;
        } else {
            System.out.println("Failed to update publisher.");
            return false;
        }
    }
    
    /**
     * Updates the title for a specific game for a user.
     * 
     * @param userID The ID of the user.
     * @param gameID The ID of the game.
     * @param title The new title.
     * @return true if the update was successful, false otherwise.
     */
    public boolean updateTitle(int userID, int gameID, String title) {
        if (gameDAO.updateTitle(userID, gameID, title)) {
            System.out.println("Title updated successfully.");
            return true;
        } else {
            System.out.println("Failed to update title.");
            return false;
        }
    }
    
    /**
	 * Updates the platform(s) for a specific game for a user.
	 * 
	 * @param userID The ID of the user.
	 * @param gameID The ID of the game.
	 * @param platform The new platform.
	 * @return true if the update was successful, false otherwise.
	 */
    public boolean updatePlatform(int userID, int gameID, String platform) {
        if (gameDAO.updatePlatform(userID, gameID, platform)) {
            System.out.println("Platform updated successfully.");
            return true;
        } else {
            System.out.println("Failed to update platform.");
            return false;
        }
    }

	/**
	 * Retrieves a list of unique platforms from the user's game collection.
	 * 
	 * @param userID The ID of the user.
	 * @return A list of unique platforms.
	 */
	public List<String> getPlatformsFromCollection(int userID){
		List<String> platforms = new ArrayList<>(); 
		
		if(userID > 0) {
			platforms = collectionDAO.getCollectionPlatform(userID, null); 		
			Set<String> uniquePlatforms = new HashSet<>();
			//split up the platforms in the set
			for(String entry : platforms) {
				if(entry != null && !entry.isEmpty());{
					String[] splitPlatforms = entry.split(",\\s*");
					Collections.addAll(uniquePlatforms, splitPlatforms);
					
				}
			}
	        List<String> sortedUniquePlatforms = new ArrayList<>(uniquePlatforms);
	        Collections.sort(sortedUniquePlatforms);
	        return sortedUniquePlatforms;
		} else {
			System.out.println("Could not retrieve platforms from collectionDAO");
			return Collections.emptyList(); 
		}
				
	}
	
    /**
     * Retrieves and deduplicates genre names from a user's collection.
     *
     * @param userID The user ID.
     * @return A sorted list of unique genres.
     */
	public List<String> getGenreFromCollection(int userID){
		List<String> genres = new ArrayList<>(); 
		
		if(userID > 0) {
			genres = collectionDAO.getCollectionGenres(userID, null); 
			System.out.println("Genres in collection is: " + genres);
			
			Set<String> uniqueGenres = new HashSet<>();

			for(String entry : genres) {
				if(entry != null && !entry.isEmpty());{
					String[] splitGenres = entry.split(",\\s*");
					Collections.addAll(uniqueGenres, splitGenres);
					
				}
			}
	        List<String> sortedUniqueGenres = new ArrayList<>(uniqueGenres);
	        Collections.sort(sortedUniqueGenres);
	        return sortedUniqueGenres;
		} else {
			System.out.println("Could not retrieve genres from collectionDAO");
			return Collections.emptyList(); 
		}
				
	}
}

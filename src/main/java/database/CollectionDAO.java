package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import models.game;

/**
 * DAO class for managing game collections in the database.
 * Handles CRUD operations for games and user collections.
 */
public class CollectionDAO {
	private final Connection connection;
	private static final Logger logger = LoggerFactory.getLogger(CollectionDAO.class);
	

    public CollectionDAO() {
        this.connection = new DatabaseManager().getConnection();
    }

    // test-only constructor
    public CollectionDAO(Connection connection) {
        this.connection = connection;
    }

	
    /**
     * Adds a game to a user's collection. Supports both API and manual game entries.
     * 
     * @param userID            ID of the user
     * @param gameID            ID of the game (0 or negative for manual entry)
     * @param title             Title of the game
     * @param developer         Developer name
     * @param publisher         Publisher name
     * @param releaseDate       Release date in yyyy-MM-dd format
     * @param genre             Genre(s) of the game
     * @param platform          Platform(s) of the game
     * @param completionStatus  Completion status
     * @param notes             User notes
     * @param coverImageURL     Path or URL to cover image
     * @return true if successfully added, false otherwise
     */
	public boolean addGameToCollection(int userID, int gameID, String title, String developer, String publisher, String releaseDate, String genre, String platform, String completionStatus, String notes, String coverImageURL) {
	    boolean manualEntry = (gameID <= 0); // Manual entries will auto-generate gameID
	    String gameQuery;
	    if (manualEntry) {
	        gameQuery = "INSERT INTO Game (title, developer, publisher, releaseDate, genre, completionStatus, notes, coverArt, platform) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
	    } else {
	        gameQuery = "INSERT INTO Game (gameID, title, developer, publisher, releaseDate, genre, completionStatus, notes, coverArt, platform) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	    }

	    String userGameQuery = "INSERT INTO UserGameCollection (UserID, GameID) VALUES (?, ?)";

	    try {
	        connection.setAutoCommit(false);
	        int newGameID = gameID;

	        if (!manualEntry && gameExists(gameID)) {
	            System.out.println("Game already exists in Game table, skipping insert.");
	        } else {
	            try (PreparedStatement gameStmt = connection.prepareStatement(gameQuery, PreparedStatement.RETURN_GENERATED_KEYS)) {
	                int index = 1;
	                if (!manualEntry) gameStmt.setInt(index++, gameID);
	                gameStmt.setString(index++, title);
	                gameStmt.setString(index++, developer);
	                gameStmt.setString(index++, publisher);
	                gameStmt.setString(index++, releaseDate);
	                gameStmt.setString(index++, genre);
	                gameStmt.setString(index++, completionStatus);
	                gameStmt.setString(index++, notes);
	                gameStmt.setString(index++, coverImageURL);
	                gameStmt.setString(index++, platform);

	                gameStmt.executeUpdate();

	                if (manualEntry) {
	                    ResultSet generatedKeys = gameStmt.getGeneratedKeys();
	                    if (generatedKeys.next()) {
	                        newGameID = generatedKeys.getInt(1);
	                        System.out.println("Generated GameID for manual entry: " + newGameID);
	                    } else {
	                    	System.out.println("No generated key returned.");
	                        return false;
	                    }
	                }
	            }
	        }

	        // Check if user already owns the game
	        if (userAlreadyHasGame(userID, newGameID)) {
	            System.out.println("User already owns this game, skipping collection insert.");
	            connection.commit(); 
	            return false;
	        }

	        // Insert into UserGameCollection
	        try (PreparedStatement userGameStmt = connection.prepareStatement(userGameQuery)) {
	            userGameStmt.setInt(1, userID);
	            userGameStmt.setInt(2, newGameID);
	            userGameStmt.executeUpdate();
	        }

	        connection.commit();
	        return true;

	    } catch (SQLException e) {
	        logger.error("Error adding game to collection: ", e);
	        return false;
	    }
	}


	
	/**
	 * Removes a game from a user's collection and deletes the game from the database.
	 * 
	 * @param userID  ID of the user
	 * @param gameID  ID of the game to be removed
	 * @return true if successfully removed, false otherwise
	 */
	public boolean removeGameFromCollection(int userID, int gameID) {
	    String deleteUserGameCollectionQuery = "DELETE FROM UserGameCollection WHERE UserID = ? AND GameID = ?";
	    String deleteGameQuery = "DELETE FROM Game WHERE GameID = ?";

	    try {
	        connection.setAutoCommit(false); 
	        try (PreparedStatement statement1 = connection.prepareStatement(deleteUserGameCollectionQuery);
	              PreparedStatement statement2 = connection.prepareStatement(deleteGameQuery)) {
	            statement1.setInt(1, userID);
	            statement1.setInt(2, gameID);
	            statement1.executeUpdate();
	            statement2.setInt(1, gameID);
	            int rowsAffected = statement2.executeUpdate();
	            connection.commit();
	            return rowsAffected > 0;
	        } catch (SQLException e) {
	            connection.rollback(); 
	            logger.error("Error removing game from collection: ", e);
	        }
	    } catch (SQLException e) {
	        logger.error("Error connecting to database: ", e);
	    }
	    return false;
	}
	
	/**
	 * Checks if a game exists in the user's collection by its title.
	 * 
	 * @param userID  ID of the user
	 * @param title   Title of the game
	 * @return true if the game exists, false otherwise
	 */
	public boolean getGameID(int userID, String title) {
		String query = "SELECT gameID FROM UserGameCollection WHERE userID = ? AND title = ?";
		try (PreparedStatement statement = connection.prepareStatement(query)) {
			statement.setInt(1, userID);
			statement.setString(2, title);
			return statement.executeQuery().next();
		} catch (SQLException e) {
			logger.error("Error checking game ID: ", e);
		}
		return false;
	}
	
	/**
	 * Retrieves all games in a user's collection based on their user ID.
	 * 
	 * @param userID  ID of the user
	 * @return List of games in the user's collection
	 */
	public List<game> getAllGamesInCollection(int userID) {
	    String query = "SELECT G.* FROM UserGameCollection UGC JOIN Game G ON UGC.GameID = G.GameID WHERE UGC.UserID = ?";
	    List<game> games = new ArrayList<>();
	    
	    try (PreparedStatement statement = connection.prepareStatement(query)) {
	        
	        statement.setInt(1, userID);
	        ResultSet resultSet = statement.executeQuery();
	        
	        while (resultSet.next()) {            
	        	String releaseDateStr = resultSet.getString("ReleaseDate");
	            LocalDate releaseDate = null;
	            if (releaseDateStr != null && !releaseDateStr.isEmpty()) {
	                try {
	                    releaseDate = LocalDate.parse(releaseDateStr); // Convert String to LocalDate
	                } catch (DateTimeParseException e) {
	                    System.err.println("Error parsing date: " + releaseDateStr);
	                    releaseDate = null; // Set null if date is invalid
	                }
	            }
	            games.add(new game(
	                resultSet.getInt("GameID"), 
	                resultSet.getString("Title"), 
	                resultSet.getString("Developer"), 
	                resultSet.getString("Publisher"), 
	                releaseDate,
	                resultSet.getString("Genre"), 
	                resultSet.getString("Platform"),  
	                resultSet.getString("CompletionStatus"), 
	                resultSet.getString("Notes"), 
	                resultSet.getString("CoverArt") 
	            ));
	        }
	    } catch (SQLException e) {
	        logger.error("Error retrieving games from collection: ", e);
	    }
	    return games;
	}
	
	/**
	 * Searches for games in a user's collection based on the title.
	 * 
	 * @param userID  ID of the user
	 * @param title   Title of the game to search for
	 * @return List of games matching the search criteria
	 */
	public List<game> searchCollection(int userID, String title){
		String query = "SELECT G.* FROM UserGameCollection UGC JOIN Game G ON UGC.GameID = G.GameID WHERE UGC.UserID = ? AND G.Title LIKE ? COLLATE NOCASE";
		List<game> games = new ArrayList<>();
		
	    try (PreparedStatement statement = connection.prepareStatement(query)) {	    	
	    	statement.setInt(1, userID);
	    	statement.setString(2, "%" + title + "%"); 
	    	ResultSet resultSet = statement.executeQuery(); 
	    	
	        while (resultSet.next()) {	            
	        	String releaseDateStr = resultSet.getString("ReleaseDate");
	            LocalDate releaseDate = null;
	            if (releaseDateStr != null && !releaseDateStr.isEmpty()) {
	                try {
	                    releaseDate = LocalDate.parse(releaseDateStr); 
	                } catch (DateTimeParseException e) {
	                    System.err.println("Error parsing date: " + releaseDateStr);
	                    releaseDate = null; 
	                }
	            }
	            games.add(new game(
	                resultSet.getInt("GameID"), 
	                resultSet.getString("Title"), 
	                resultSet.getString("Developer"), 
	                resultSet.getString("Publisher"), 
	                releaseDate,
	                resultSet.getString("Genre"), 
	                resultSet.getString("Platform"),  
	                resultSet.getString("CompletionStatus"), 
	                resultSet.getString("Notes"), 
	                resultSet.getString("CoverArt") 
	            ));
	        }
	    }catch (SQLException e) {
	        logger.error("Error searching games in collection: ", e);
	    }
	    System.out.println("Found searched games in database: " + games);
	    return games;
	}
	
	/**
	 * Sorts the user's game collection based on the specified criteria.
	 * 
	 * @param userID      ID of the user
	 * @param sortBy      Sorting criteria (e.g., "Title", "Release Date", "Platform")
	 * @param searchQuery Search query for filtering games
	 * @return List of sorted games in the user's collection
	 */
	public List<game> sortCollection(int userID, String sortBy, String searchQuery) {
	    List<game> games = new ArrayList<>();

	   
	    String column;
	    switch (sortBy) {
	        case "Title":
	        case "Alphabetical":
	            column = "Title";
	            break;
	        case "Release Date":
	            column = "ReleaseDate";
	            break;
	        case "Platform":
	            column = "Platform";
	            break;
	        default:
	            System.out.println("Invalid sorting option: " + sortBy);
	            return games; 
	    }

	    String query = "SELECT G.* FROM UserGameCollection UGC JOIN Game G ON UGC.GameID = G.GameID WHERE UGC.UserID = ? ";
	    
	    if (searchQuery != null && !searchQuery.trim().isEmpty()) {
	        query += "AND G.Title LIKE ? COLLATE NOCASE ";
	    }

	    query += "ORDER BY " + column + " COLLATE NOCASE ASC";  
	    
	    try (PreparedStatement statement = connection.prepareStatement(query)) {
	           statement.setInt(1, userID);
	           if (searchQuery != null && !searchQuery.trim().isEmpty()) {
	               statement.setString(2, "%" + searchQuery + "%");
	           }

	           ResultSet resultSet = statement.executeQuery();
	           while (resultSet.next()) {
	               String dateString = resultSet.getString("ReleaseDate");
	               LocalDate releaseDate = null;
	               if (dateString != null && !dateString.isEmpty()) {
	                   try {
	                       releaseDate = LocalDate.parse(dateString, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
	                   } catch (DateTimeParseException e) {
	                       System.err.println("Error parsing date: " + dateString);
	                   }
	               }

	               games.add(new game(
	                   resultSet.getInt("GameID"),
	                   resultSet.getString("Title"),
	                   resultSet.getString("Developer"),
	                   resultSet.getString("Publisher"),
	                   releaseDate,
	                   resultSet.getString("Genre"),
	                   resultSet.getString("Platform"),
	                   resultSet.getString("CompletionStatus"),
	                   resultSet.getString("Notes"),
	                   resultSet.getString("CoverArt")
	               ));
	           }
	       } catch (SQLException e) {
	           logger.error("Error sorting games in collection: ", e);
	       }

	    return games; 
	}
	
	/**
	 * Retrieves distinct platforms from the user's game collection.
	 * 
	 * @param userID  ID of the user
	 * @param platform Platform filter (optional)
	 * @return List of distinct platforms in the user's collection
	 */
	public List<String> getCollectionPlatform(int userID, String platform) {
	    List<String> platforms = new ArrayList<>();
	    String query;
	    
	    // If a specific platform filter is provided, use it in the query.
	    if (platform != null && !platform.isEmpty()) {
	        query = "SELECT DISTINCT G.Platform FROM UserGameCollection UGC JOIN Game G ON UGC.GameID = G.GameID WHERE UGC.UserID = ? AND G.Platform = ?";
	    } else {
	        // Otherwise, return all distinct platforms in the user's collection.
	        query = "SELECT DISTINCT G.Platform FROM UserGameCollection UGC JOIN Game G ON UGC.GameID = G.GameID WHERE UGC.UserID = ?";
	    }
	    
	    try (PreparedStatement statement = connection.prepareStatement(query)) {	         
	         statement.setInt(1, userID);
	         if (platform != null && !platform.isEmpty()) {
	             statement.setString(2, platform);
	         }	         
	         ResultSet resultSet = statement.executeQuery();
	         while (resultSet.next()) {
	             platforms.add(resultSet.getString("Platform"));
	         }
	    } catch (SQLException e) {
	         logger.error("Error retrieving platforms from collection: ", e);
	    }
	    
	    return platforms;
	}	
	
	/**
	 * Retrieves distinct genres from the user's game collection.
	 * 
	 * @param userID  ID of the user
	 * @param genre   Genre filter (optional)
	 * @return List of distinct genres in the user's collection
	 */
	public List<String> getCollectionGenres(int userID, String genre) {
	    List<String> genres = new ArrayList<>();
	    String query;
	    
	    // If a specific platform filter is provided, use it in the query.
	    if (genre != null && !genre.isEmpty()) {
	        query = "SELECT DISTINCT G.Genre FROM UserGameCollection UGC JOIN Game G ON UGC.GameID = G.GameID WHERE UGC.UserID = ? AND G.Genre = ?";
	    } else {
	        // Otherwise, return all distinct platforms in the user's collection.
	        query = "SELECT DISTINCT G.Genre FROM UserGameCollection UGC JOIN Game G ON UGC.GameID = G.GameID WHERE UGC.UserID = ?";
	    }	    
	    try (PreparedStatement statement = connection.prepareStatement(query)) {	         
	         statement.setInt(1, userID);
	         if (genre != null && !genre.isEmpty()) {
	             statement.setString(2, genre);
	         }	         
	         ResultSet resultSet = statement.executeQuery();
	         while (resultSet.next()) {
	             genres.add(resultSet.getString("Genre"));
	         }
	    } catch (SQLException e) {
	         logger.error("Error retrieving genres from collection: ", e);
	    }
	    
	    return genres;
	}	
	
	/**
	 * Filters the user's game collection based on selected genres, platforms, and completion statuses.
	 * 
	 * @param userID            ID of the user
	 * @param genres            List of selected genres
	 * @param platforms         List of selected platforms
	 * @param completionStatuses List of selected completion statuses
	 * @return List of filtered games in the user's collection
	 */
	public List<game> filterCollection(int userID, List<String> genres, List<String> platforms, List<String> completionStatuses) {
	    List<game> games = new ArrayList<>();
	    StringBuilder query = new StringBuilder(
	        "SELECT G.* FROM UserGameCollection UGC JOIN Game G ON UGC.GameID = G.GameID WHERE UGC.UserID = ?"
	    );
	    List<String> conditions = new ArrayList<>();
	    List<Object> parameters = new ArrayList<>();
	    parameters.add(userID);
	    // Handle Genre Filtering
	    if (genres != null && !genres.isEmpty()) {
	        conditions.add("G.Genre IN (" + String.join(",", genres.stream().map(g -> "?").toArray(String[]::new)) + ")");
	        parameters.addAll(genres);
	    }
	    // Handle Platform Filtering (Matching any selected platform)
	    if (platforms != null && !platforms.isEmpty()) {
	        List<String> platformConditions = new ArrayList<>();
	        for (String platform : platforms) {
	            platformConditions.add("G.Platform LIKE ?");
	            parameters.add("%" + platform + "%"); // Matches "Wii" inside "Wii, Wii U"
	        }
	        conditions.add("(" + String.join(" OR ", platformConditions) + ")");
	    }
	    // Handle Completion Status Filtering
	    if (completionStatuses != null && !completionStatuses.isEmpty()) {
	        conditions.add("G.CompletionStatus IN (" + String.join(",", completionStatuses.stream().map(c -> "?").toArray(String[]::new)) + ")");
	        parameters.addAll(completionStatuses);
	    }

	    if (!conditions.isEmpty()) {
	        query.append(" AND ").append(String.join(" AND ", conditions));
	    }
	    try (PreparedStatement statement = connection.prepareStatement(query.toString())) {	        
	        // Set parameters dynamically
	        for (int i = 0; i < parameters.size(); i++) {
	            if (parameters.get(i) instanceof Integer) {
	                statement.setInt(i + 1, (Integer) parameters.get(i));
	            } else {
	                statement.setString(i + 1, (String) parameters.get(i));
	            }
	        }
	        System.out.println("Executing SQL Query: " + query);
	        System.out.println("With Parameters: " + parameters);
	        ResultSet resultSet = statement.executeQuery();
	        while (resultSet.next()) {
	            String rawDate = resultSet.getString("ReleaseDate");
	            LocalDate releaseDate = null;
	            if (rawDate != null && !rawDate.isEmpty()) {
	                try {
	                    releaseDate = LocalDate.parse(rawDate);
	                } catch (DateTimeParseException e) {
	                    logger.error("Error parsing date: " + rawDate);
	                }
	            }
	            games.add(new game(
	                resultSet.getInt("GameID"),
	                resultSet.getString("Title"),
	                resultSet.getString("Developer"),
	                resultSet.getString("Publisher"),
	                releaseDate,
	                resultSet.getString("Genre"),
	                resultSet.getString("Platform"),  
	                resultSet.getString("CompletionStatus"),
	                resultSet.getString("Notes"),
	                resultSet.getString("CoverArt")
	            ));
	        }
	    } catch (SQLException e) {
	        logger.error("Error filtering games in collection: ", e);
	    }
	    if (games.isEmpty()) {
	        System.out.println("No games found for the selected filters.");
	    }
	    return games;
	}	
	
	/**
	 * Checks if a game exists in the database by its ID.
	 * 
	 * @param gameID  ID of the game
	 * @return true if the game exists, false otherwise
	 */
	public boolean gameExists(int gameID) {
	    String query = "SELECT 1 FROM Game WHERE GameID = ?";
	    try (PreparedStatement stmt = connection.prepareStatement(query)) {
	        stmt.setInt(1, gameID);
	        return stmt.executeQuery().next();
	    } catch (SQLException e) {
	        logger.error("Error checking if game exists: ", e);
	        return false;
	    }
	}
	
	/**
	 * Checks if a user already has a game in their collection.
	 * 
	 * @param userID  ID of the user
	 * @param gameID  ID of the game
	 * @return true if the user already has the game, false otherwise
	 */
	public boolean userAlreadyHasGame(int userID, int gameID) {
	    String query = "SELECT 1 FROM UserGameCollection WHERE UserID = ? AND GameID = ?";
	    try (PreparedStatement stmt = connection.prepareStatement(query)) {
	        stmt.setInt(1, userID);
	        stmt.setInt(2, gameID);
	        return stmt.executeQuery().next();
	    } catch (SQLException e) {
	        logger.error("Error checking if user already has game: ", e);
	        return false;
	    }
	}

	/**
	 * Deletes a game by title from a specific user's collection.
	 * This is used for test cleanup to remove test games like "Manual Test Game".
	 *
	 * @param title  Title of the game
	 * @param userId ID of the user
	 * @return true if at least one row was deleted
	 */
	public boolean deleteGameByTitle(String title, int userId) {
	    String sql = "DELETE FROM UserGameCollection WHERE userID = ? AND gameID IN " +
	                 "(SELECT gameID FROM Game WHERE title = ?)";

	    String deleteOrphanedGames = "DELETE FROM Game WHERE gameID NOT IN (SELECT DISTINCT gameID FROM UserGameCollection)";

	    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
	        stmt.setInt(1, userId);
	        stmt.setString(2, title);
	        int affected = stmt.executeUpdate();

	        try (PreparedStatement cleanup = connection.prepareStatement(deleteOrphanedGames)) {
	            cleanup.executeUpdate();
	        }

	        return affected > 0;
	    } catch (SQLException e) {
	        logger.error("Error deleting game by title: ", e);
	        return false;
	    }
	}


	
	
}



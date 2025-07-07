package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Data Access Object (DAO) class for handling database operations related to the Game table.
 * This class uses the DatabaseManager to establish connections and perform CRUD operations 
 * for game attributes like title, platform, developer, notes, completion status, etc.
 */
public class GameDAO {
	
	private final DatabaseManager dbManager = new DatabaseManager();
	private final Connection connection;
	private static final Logger logger = LoggerFactory.getLogger(GameDAO.class.getName());
	
    public GameDAO() {
        this.connection = new DatabaseManager().getConnection();
    }

    public GameDAO(Connection connection) {
        this.connection = connection;
    }
	
    /**
     * Checks if a game exists for a specific user and title.
     * This method queries the Game table to determine if a game with the given title exists for the specified user.
     *
     * @param userID The user's ID.
     * @param title  The title of the game.
     * @return true if the game exists, false otherwise.
     */
	public boolean getGameID(int userID, String title) {
		String query = "SELECT gameID FROM Game WHERE userID = ? AND title = ?";
		try (PreparedStatement statement = connection.prepareStatement(query)) {
			statement.setInt(1, userID);
			statement.setString(2, title);
			return statement.executeQuery().next();
		} catch (SQLException e) {
			logger.error("Error while checking game ID: ", e);
		}
		return false;
	}
	
	/**
	 * Checks if a game exists for a specific user and platform.
	 * This method queries the Game table to determine if a game with the given platform exists for the specified user.
	 *
	 * @param userID The user's ID.
	 * @return true if the game exists, false otherwise.
	 */
	public boolean getGamePlatform(int userID) {
		String query = "SELECT platform FROM Game WHERE userID = ?";
		try (PreparedStatement statement = connection.prepareStatement(query)) {
			statement.setInt(1, userID);
			return statement.executeQuery().next();
		} catch (SQLException e) {
			logger.error("Error while checking game platform: ", e);
		}
		return false;
	}
	
	
    /**
     * This method updates the completion status of a game in the Game table based on the user's ID and the game's ID.
     *
     * @param userID           The user's ID.
     * @param gameID           The game's ID.
     * @param completionStatus The new completion status.
     * @return true if update was successful, false otherwise.
     */
	public boolean updateCompletionStatus(int userID, int gameID, String completionStatus) {
	    String query = """
	        UPDATE Game 
	        SET completionStatus = ? 
	        WHERE gameID = (
	            SELECT gameID FROM UserGameCollection WHERE userID = ? AND gameID = ?
	        )
	    """;

	    try (PreparedStatement statement = connection.prepareStatement(query)) {
	        
	        statement.setString(1, completionStatus);
	        statement.setInt(2, userID);
	        statement.setInt(3, gameID);
	        
	        return statement.executeUpdate() > 0; // Returns true if at least one row was updated
	    } catch (SQLException e) {
	        logger.error("Error while updating completion status: ", e);
	    }
	    return false;
	}
	
    /**
     * This method updates the notes of a game in the Game table based on the user's ID and the game's ID.
     *
     * @param userID The user's ID.
     * @param gameID The game's ID.
     * @param notes  The new notes to be saved.
     * @return true if update was successful, false otherwise.
     */
    public boolean updateNotes(int userID, int gameID, String notes) {
        String query = """
            UPDATE Game 
            SET notes = ? 
            WHERE gameID = (
                SELECT gameID FROM UserGameCollection WHERE userID = ? AND gameID = ?
            )
        """;
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, notes);
            statement.setInt(2, userID);
            statement.setInt(3, gameID);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error while updating notes: ", e);
        }
        return false;
    }
    
    /**
     * This method updates the cover image URL of a game in the Game table based on the user's ID and the game's ID.
     *
     * @param userID The user's ID.
     * @param gameID The game's ID.
     * @param notes  The new notes to be saved.
     * @return true if update was successful, false otherwise.
     */
    public boolean updateCoverImageURL(int userID, int gameID, String coverImageURL) {
        String query = """
            UPDATE Game
            SET coverArt = ?
            WHERE gameID = (
                SELECT gameID FROM UserGameCollection WHERE userID = ? AND gameID = ?
            )
        """;
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, coverImageURL);
            statement.setInt(2, userID);
            statement.setInt(3, gameID);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error while updating cover image URL: ", e);
        }
        return false;
    }
    
    /**
     * This method retrieves the cover image URL of a game in the Game table based on the user's ID and the game's ID.
     *
     * @param userID        The user's ID.
     * @param gameID        The game's ID.
     * @param coverImageURL The new cover image URL.
     * @return true if update was successful, false otherwise.
     */
    public String getCoverImageURL(int userID, int gameID) {
        String query = "SELECT coverArt FROM Game WHERE userID = ? AND gameID = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, userID);
            statement.setInt(2, gameID);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("coverArt");
            }
        } catch (SQLException e) {
            logger.error("Error while retrieving cover image URL: ", e);
        }
        return null;
    }
    
    /**
	 * This method updates the release date of a game in the Game table based on the user's ID and the game's ID.
	 *
	 * @param userID     The user's ID.
	 * @param gameID     The game's ID.
	 * @param releaseDate The new release date.
	 * @return true if update was successful, false otherwise.
	 */
    public boolean updateReleaseDate(int userID, int gameID, String releaseDate) {
        String query = """
            UPDATE Game
            SET releaseDate = ?
            WHERE gameID = (
                SELECT gameID FROM UserGameCollection WHERE userID = ? AND gameID = ?
            )
        """;
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, releaseDate);
            statement.setInt(2, userID);
            statement.setInt(3, gameID);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error while updating release date: ", e);
        }
        return false;
    }
    
    /**
     * This method updates the genre of a game in the Game table based on the user's ID and the game's ID.
     * 
     * @param userID The user's ID.
     * @param gameID The game's ID.
     * @param genre The new genre to be saved.
     * @return true if update was successful, false otherwise.
     */
    public boolean updateGenre(int userID, int gameID, String genre) {
        String query = """
            UPDATE Game
            SET genre = ?
            WHERE gameID = (
                SELECT gameID FROM UserGameCollection WHERE userID = ? AND gameID = ?
            )
        """;
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, genre);
            statement.setInt(2, userID);
            statement.setInt(3, gameID);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error while updating genre: ", e);
        }
        return false;
    }
    
    /**
	 * This method updates the developer of a game in the Game table based on the user's ID and the game's ID.
	 *
	 * @param userID    The user's ID.
	 * @param gameID    The game's ID.
	 * @param developer The new developer to be saved.
	 * @return true if update was successful, false otherwise.
	 */
    public boolean updateDeveloper(int userID, int gameID, String developer) {
        String query = """
            UPDATE Game
            SET developer = ?
            WHERE gameID = (
                SELECT gameID FROM UserGameCollection WHERE userID = ? AND gameID = ?
            )
        """;
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, developer);
            statement.setInt(2, userID);
            statement.setInt(3, gameID);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error while updating developer: ", e);
        }
        return false;
    }
    
    /**
     * This method updates the publisher of a game in the Game table based on the user's ID and the game's ID.
     * 
     * @param userID   The user's ID.
     * @param gameID  The game's ID.
     * @param publisher The new publisher to be saved.
     * @return true if update was successful, false otherwise.
     */
    public boolean updatePublisher(int userID, int gameID, String publisher) {
        String query = """
            UPDATE Game
            SET publisher = ?
            WHERE gameID = (
                SELECT gameID FROM UserGameCollection WHERE userID = ? AND gameID = ?
            )
        """;
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, publisher);
            statement.setInt(2, userID);
            statement.setInt(3, gameID);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error while updating publisher: ", e);
        }
        return false;
    }
    
    /**
	 * This method updates the title of a game in the Game table based on the user's ID and the game's ID.
	 * 
	 * @param userID The user's ID.
	 * @param gameID The game's ID.
	 * @param title The new title to be saved.
	 * @return true if update was successful, false otherwise.
	 */
    public boolean updateTitle(int userID, int gameID, String title) {
        String query = """
            UPDATE Game
            SET title = ?
            WHERE gameID = (
                SELECT gameID FROM UserGameCollection WHERE userID = ? AND gameID = ?
            )
        """;
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, title);
            statement.setInt(2, userID);
            statement.setInt(3, gameID);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error while updating title: ", e);
        }
        return false;
    }
    
    /**
	 * This method updates the platform of a game in the Game table based on the user's ID and the game's ID.
	 * 
	 * @param userID The user's ID.
	 * @param gameID The game's ID.
	 * @param platform The new platform to be saved.
	 * @return true if update was successful, false otherwise.
	 */
    public boolean updatePlatform(int userID, int gameID, String platform) {
        String query = """
            UPDATE Game
            SET platform = ?
            WHERE gameID = (
                SELECT gameID FROM UserGameCollection WHERE userID = ? AND gameID = ?
            )
        """;
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, platform);
            statement.setInt(2, userID);
            statement.setInt(3, gameID);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error while updating platform: ", e);
        }
        return false;
    }
    
} 

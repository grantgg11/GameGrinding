package database;

import models.user;
import security.Encryption;
import utils.KeyStorage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DAO class responsible for CRUD operations related to users.
 * Includes encrypted email handling, password hashing, and security questions.
 */
public class UserDAO {
	
	private final DatabaseManager dbManager = new DatabaseManager();
	private static final Logger logger = LoggerFactory.getLogger(ReportDAO.class);
	   
    /**
     * Retrieves a user by their email (encrypted match).
     *
     * @param email The user's plain-text email.
     * @return The user object if found, otherwise null.
     */
	public user getUserByEmail(String email) {
	    String query = "SELECT * FROM User WHERE email = ?";
	    try (Connection connection = dbManager.getConnection();
	         PreparedStatement statement = connection.prepareStatement(query)) {

	        // Encrypt the email before searching
	        SecretKey secretKey = KeyStorage.getEncryptionKey();
	        String encryptedEmail = Encryption.encrypt(email, secretKey);

	        statement.setString(1, encryptedEmail);
	        ResultSet resultSet = statement.executeQuery();

	        if (resultSet.next()) {
	        	int userID = resultSet.getInt("userID");
	        	String storedPassword = resultSet.getString("password");
	            System.out.println("Retrieved Hashed Password from DB: " + storedPassword);
	            
	            return new user(
	                userID,
	                resultSet.getString("username"),
	                resultSet.getString("email"), // Will still be encrypted
	                storedPassword,
	                resultSet.getString("role")
	            );
	        }
	    } catch (Exception e) {
	        logger.error("Error retrieving user by email: " + e.getMessage(), e);
	    }
	    return null;
	}
	
    /**
     * Retrieves a user's hashed password by their ID.
     *
     * @param userID The user's ID.
     * @return The hashed password string, or null if not found.
     */
	public String getUserPassword(int userID) {
		String query = "SELECT password FROM User WHERE userID = ?";
		try (Connection connection = dbManager.getConnection();
			 PreparedStatement statement = connection.prepareStatement(query)) {
			statement.setInt(1, userID);
			ResultSet resultSet = statement.executeQuery();
			if (resultSet.next()) {
				System.out.println("Retrieved Hashed Password from DB: " + resultSet.getString("password"));
				return resultSet.getString("password");
			}
		} catch (SQLException e) {
			logger.error("Error retrieving user password: " + e.getMessage(), e);
		}
		return null;
	}

	/**
	 * Retrieves a user by their ID.
	 *
	 * @param userID The user's ID.
	 * @return The user object if found, otherwise null.
	 */
	public user getUserByID(int userID) {
		String query = "SELECT * FROM User WHERE userID = ?";
		try (Connection connection = dbManager.getConnection();
			 PreparedStatement statement = connection.prepareStatement(query)) {
			statement.setInt(1, userID);
			ResultSet resultSet = statement.executeQuery();
			if (resultSet.next()) {
				return new user(resultSet.getInt("userID"), resultSet.getString("username"), resultSet.getString("email"), resultSet.getString("password"), resultSet.getString("role"));
			}
		} catch (SQLException e) {
			logger.error("Error retrieving user by ID: " + e.getMessage(), e);
		}
		return null;
	}
	
	/**
	 * Retrieves a user's security answers by their ID.
	 *
	 * @param userID The user's ID.
	 * @return The user object with security answers if found, otherwise null.
	 */
	public user getUserSecurityAnswers(int userID) {
		String query = "SELECT * FROM User WHERE userID = ?";
		try (Connection connection = dbManager.getConnection();
			 PreparedStatement statement = connection.prepareStatement(query)) {
			statement.setInt(1, userID);
			ResultSet resultSet = statement.executeQuery();
			if (resultSet.next()) {
				return new user(resultSet.getInt("userID"), resultSet.getString("username"), resultSet.getString("email"), resultSet.getString("password"), resultSet.getString("securityAnswer1"), resultSet.getString("securityAnswer2"), resultSet.getString("securityAnswer3"));
			}
		} catch (SQLException e) {
			logger.error("Error retrieving user security answers: " + e.getMessage(), e);
		}
		return null;
	}
	
    /**
     * Deletes a user by email.
     *
     * @param email The email of the user to delete.
     * @return true if a user was deleted, false otherwise.
     */
	public boolean deleteUserByEmail(String email) {
		String query = "DELETE FROM User WHERE email = ?";
		try (Connection connection = dbManager.getConnection();
			 PreparedStatement statement = connection.prepareStatement(query)) {
			statement.setString(1, email);
			return statement.executeUpdate() > 0;
		} catch (SQLException e) {
			logger.error("Error deleting user by email: " + e.getMessage(), e);
		}
		return false;
	}
	
	/**
	 * Inserts a new user into the database with encrypted email and hashed password.
	 *
	 * @param user The user object to insert.
	 * @return The ID of the newly created user, or -1 if the operation failed.
	 */
	public int insertUser(user user) {
	    String query = "INSERT INTO User (username, email, password, securityAnswer1, securityAnswer2, securityAnswer3) VALUES (?, ?, ?, ?, ?, ?)";
	    try (Connection connection = dbManager.getConnection();
	         PreparedStatement statement = connection.prepareStatement(query)) {
	        // Encrypt the email before searching
	        SecretKey secretKey = KeyStorage.getEncryptionKey();
	        String encryptedEmail = Encryption.encrypt(user.getEmail(), secretKey);	        
	        
	        statement.setString(1, user.getUsername());
	        statement.setString(2, encryptedEmail);
	        statement.setString(3, user.getPassword());
	        statement.setString(4, user.getSecurityAnswer1());
	        statement.setString(5, user.getSecurityAnswer2());
	        statement.setString(6, user.getSecurityAnswer3());

	        int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating user failed, no rows affected.");
            }

            // **Fix**: Retrieve generated userID
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);  // ✅ Return the new user ID
                } else {
                    throw new SQLException("Creating user failed, no ID obtained.");
                }
            }

        } catch (SQLException e) {
            logger.error("Error inserting user: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Error encrypting email: " + e.getMessage(), e);
        }
        return -1;  // Return -1 if user creation fails
	}

    /**
     * Updates a user's username and email.
     *
     * @param userID   The user's ID.
     * @param username New username.
     * @param email    New email.
     * @return true if update was successful, false otherwise.
     *
     * TODO: Encrypt email before storing for consistency and security.
     */
	public boolean updateUserAccount(int userID, String username, String email) {
		String query = "UPDATE User SET username = ?, email = ? WHERE userID = ?";
		try (Connection connection = dbManager.getConnection();
			 PreparedStatement statement = connection.prepareStatement(query)) {
			statement.setString(1, username);
			statement.setString(2, email);
			statement.setInt(3, userID);
			return statement.executeUpdate() > 0;
		} catch (SQLException e) {
			logger.error("Error updating user account: " + e.getMessage(), e);
		}
		return false;
	} 
	
	/**
	 * Updates a user's password.
	 *
	 * @param userID      The user's ID.
	 * @param newPassword The new password.
	 * @return true if update was successful, false otherwise.
	 */
	public boolean updateUserPassword(int userID, String newPassword) {
		String query = "UPDATE User SET password = ? WHERE userID = ?";
		try (Connection connection = dbManager.getConnection();
			 PreparedStatement statement = connection.prepareStatement(query)) {
			statement.setString(1, newPassword);
			statement.setInt(2, userID);
			return statement.executeUpdate() > 0;
		} catch (SQLException e) {
			logger.error("Error updating user password: " + e.getMessage(), e);
		}
		return false;
	
	}
}
	

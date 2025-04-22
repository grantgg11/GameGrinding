package security;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import database.UserDAO;
import models.user;
import services.userService;

/**
 * Handles user authentication for the application.
 * Responsible for verifying user credentials, managing the authenticated user state,
 * and communicating with supporting services (e.g., userService).
 */
public class AuthManager {
	

	private final Hasher hasher = new Hasher();
    private user authenticatedUser;	
	private final UserDAO userDAO = new UserDAO();
	 private static final Logger logger = LoggerFactory.getLogger(AuthManager.class);
	   

    /**
     * Attempts to authenticate a user based on the provided email and password.
     *
     * @param email    The plain-text email of the user (unencrypted here).
     * @param password The plain-text password entered by the user.
     * @return true if authentication is successful, false otherwise.
     */
	public boolean authenticateUser(String email, String password) {
	    try {
	    	// Retrieve user by encrypted email from the database
	        user founduser = userDAO.getUserByEmail(email);

	        if (founduser == null) {
	            System.out.println("No user found with this email: ");
	            return false;
	        }	        
	        // Verify password using secure hash comparison
	        boolean passwordMatch = hasher.verifyPassword(password, founduser.getPassword());
	        
	        System.out.println("Password Match: " + passwordMatch);
            if (passwordMatch) {
                this.authenticatedUser = founduser; // Store authenticated user
                if (founduser.getUserID() > 0) {
                    userService.getInstance().setCurrentUserID(founduser.getUserID());
                    System.out.println("User ID set in AuthManager: " + founduser.getUserID());
                } else {
                    System.err.println("Error: Invalid user ID retrieved from DB.");
                }
            }
	        return passwordMatch;

	    } catch (Exception e) {
	        logger.error("Authentication error: " + e.getMessage(), e);;
	        return false;
	    }
	}
	
	/**
	 * Returns the currently authenticated user.
	 * 
	 * @return The authenticated user object, or null if no user is authenticated.
	 */
    public user getAuthenticatedUser() {
        return authenticatedUser;
    }

	

}

package services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import database.UserDAO;
import models.user;
import security.AuthManager;
import security.Encryption;
import security.PasswordHasher;
import utils.AlertHelper;
import utils.KeyStorage;

/**
 * userService manages all user-related operations in the GameGrinding application,
 * serving as the service layer between controllers and the data access layer (UserDAO).
 * 
 * This class is responsible for:
 * - User registration with validation for email format, password strength, and
 *   required security question answers.
 * - Secure storage of user credentials and security answers using hashing and
 *   encryption for sensitive fields like passwords and email.
 * - Authentication of users, including setting and retrieving the currently
 *   logged-in user's information and role.
 * - Account management features such as updating usernames, emails, and passwords,
 *   while enforcing uniqueness checks and validating input.
 * - Password reset functionality for both authenticated users and users who have
 *   forgotten their password, using stored security question verification.
 * - Role checking for access control purposes.
 * - Logging out the current user and clearing session-related data.
 * - Providing methods to retrieve user details by ID or email.
 * 
 * Key features include:
 * - Integration with AuthManager for authentication logic.
 * - Integration with PasswordHasher for secure password handling.
 * - Integration with Encryption and KeyStorage for email encryption.
 * - Error reporting to the UI through AlertHelper.
 * - Centralized storage of the current user's state for consistent session handling.
 * 
 * This service ensures that controllers do not directly handle sensitive security
 * operations or database queries.
 */
public class userService {
	
	private static userService instance;
	private UserDAO userDAO = new UserDAO();
	private AuthManager authManager = new AuthManager();
	private AlertHelper alert = new AlertHelper();
	private static final Logger logger = LoggerFactory.getLogger(userService.class);
	   
	
	private user currentUser;
	private int loggedInUserID = -1;
	
	/**
	 * sets the current user ID
	 * 
	 * @param userID The ID of the logged-in user.
	 */
    public void setCurrentUserID(int userID) {
        if (userID > 0) { 
            this.loggedInUserID = userID; 
            System.out.println("User ID stored in userService: " + userID);
        } else {
            System.out.println("Error: Invalid User ID received in userService.");
        }
    }
    
    /**
	 * Gets the current user ID.
	 * 
	 * @return The ID of the logged-in user.
	 */
    public int getCurrentUserID() {
        return loggedInUserID;
    }
	
    public userService(UserDAO userDAO, AuthManager authManager, AlertHelper alert) {
        this.userDAO = userDAO;
        this.authManager = authManager;
        this.alert = alert;
    }

    public userService() {
	}

	public static void setInstance(userService newInstance) {
        instance = newInstance;
    }

    public static userService getInstance() {
        if (instance == null) {
            instance = new userService(new UserDAO(), new AuthManager(), new AlertHelper());
        }
        return instance;
    }
    
    /**
	 * Registers a new user with the provided details.
	 * 
	 * @param username The username of the new user.
	 * @param email The email of the new user.
	 * @param password The password of the new user.
	 * @param securityQuestion1 The first security question answer.
	 * @param securityQuestion2 The second security question answer.
	 * @param securityQuestion3 The third security question answer.
	 * @return true if registration is successful, false otherwise.
	 */
	public boolean registerUser(String username, String email, String password, String securityQuestion1, String securityQuestion2, String securityQuestion3) {
		try {
			StringBuilder errorMessages = new StringBuilder();
			
			if (!isEmailValid(email)) {
				errorMessages.append("- Please enter a valid email address.\n");
			}
			
			// Check if the encrypted email already exists in the database
			if (userDAO.getUserByEmail(email) != null) {
				errorMessages.append("- User with this email already exists.\n");
			}

			// Validate password strength
			if (!isPasswordStrong(password)) {
				 errorMessages.append("- Password must include one upper and lowercase letters, a number, a special character, and have 8 characters.\n");
			}

			// Check if security questions are filled
			if (securityQuestion1.isEmpty() || securityQuestion2.isEmpty() || securityQuestion3.isEmpty()) {
				 errorMessages.append("- All security questions must be filled in.\n");
			}
			
	        if (errorMessages.length() > 0) {
	            alert.showError("Registration Error", "Please fix the following issues:", errorMessages.toString());
	            return false;
	        }
	        
	        // Hash the password 
	        String hashedPassword = PasswordHasher.hashPassword(password);
			// Hash security answers
			String hashedSecurityQuestion1 = PasswordHasher.hashPassword(securityQuestion1);
			String hashedSecurityQuestion2 = PasswordHasher.hashPassword(securityQuestion2);
			String hashedSecurityQuestion3 = PasswordHasher.hashPassword(securityQuestion3);

			models.user newUser = new models.user(username, email, hashedPassword, 
                              hashedSecurityQuestion1, hashedSecurityQuestion2, hashedSecurityQuestion3);

	        int userID = userDAO.insertUser(newUser);
	        
	        if (userID != -1) {
	            System.out.println("User registered successfully with ID: " + userID);
	            return true;
	        } else {
	            System.out.println("Failed to register user.");
	            return false;
	        }

		} catch (Exception e) {
			logger.error("Error occurred during user registration: " + e.getMessage(), e);
			System.out.println("Error occurred during user registration: " + e.getMessage());
			return false;
		}
	}
	
	/**
	 * Authenticates a user with the provided email and password.
	 * 
	 * @param email The email of the user.
	 * @param password The password of the user.
	 * @return true if authentication is successful, false otherwise.
	 */
	public boolean authenticateUser(String email, String password) {
		if(email != null && !email.isEmpty()) {
				try {
					boolean isAuthenticated = authManager.authenticateUser(email, password);
		            if (isAuthenticated) {
		            	
		            	setCurrentUser(authManager.getAuthenticatedUser());
		                if (getCurrentUser() != null && getCurrentUser().getUserID() > 0) {
		                    setCurrentUserID(getCurrentUser().getUserID());
		                    System.out.println("Logged-in user role: " + getCurrentUser().getRole());

		                    } else {
		                        System.err.println("Error: Retrieved user ID is invalid in userService.");
		                }
		           }
		           return isAuthenticated;
				} catch (Exception e) {
					logger.error("Error occurred during user authentication: " + e.getMessage(), e);
				}
		} else {
			System.out.println("Invalid email.");
			return false;
			}
		return false;
	}
	
	/**
	 * Gets the current user's role.
	 * 
	 * @return The role of the current user.
	 */
	public String getCurrentUserRole() {
	    if (getCurrentUser() != null) {
	        return getCurrentUser().getRole();
	    }

	    if (loggedInUserID > 0) {
	        user fetchedUser = userDAO.getUserByID(loggedInUserID);
	        if (fetchedUser != null) {
	            setCurrentUser(fetchedUser); 
	            return getCurrentUser().getRole();
	        }
	    }

	    return "guest"; // fallback if nothing found
	}

	/**
	 * Validates password strength with upper/lowercase, number, symbol, and length checks.
	 * 
	 * @param password  The password to validate.
	 * @return true if the password is strong, false otherwise.
	 */
	public boolean isPasswordStrong(String password) {
		boolean hasUpper = false;
		boolean hasLower = false;
		boolean hasNumber = false;
		boolean hasSpecialCharacter = false;
		
		Set<Character> characterSet = new HashSet<Character>(
				Arrays.asList('!', '@', '#', '$', '%', '^', '&', '*', '(', ')', '-', '+'));
		
		for(char i: password.toCharArray()) {
			if(Character.isLowerCase(i)) {
				hasLower = true;}
			if(Character.isUpperCase(i)) {
				hasUpper = true;}
			if(Character.isDigit(i)) {
				hasNumber = true;}
			if(characterSet.contains(i)) {
				hasSpecialCharacter = true;}	
		}
		if(hasUpper && hasLower && hasNumber && hasSpecialCharacter && (password.length() >=8)) {
			return true;
		}
		return false;
	}
	
	/**
	 * Validates the email format using regex.
	 * 
	 * @param email The email to validate.
	 * @return true if the email format is valid, false otherwise.
	 */
	public boolean isEmailValid(String email) {
		return email != null && email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$");
	}
	
	/**
	 * Updates the user's account information.
	 * 
	 * @param userID The ID of the user.
	 * @param username The new username.
	 * @param email The new email.
	 * @return true if the update is successful, false otherwise.
	 */
	public boolean updateAccount(int userID, String username, String email) {
	    List<String> errors = new ArrayList<>();

	    if (username == null || username.isBlank()) {
	        errors.add("Username is required.");
	    }
	    if (email == null || email.isBlank()) {
	        errors.add("Email is required.");
	    } else if (!isEmailValid(email)) {
	        errors.add("Please enter a valid email address.");
	    }
	    
	    SecretKey secretKey = null;
	    String encryptedEmail = null;

	    try {
	        secretKey = KeyStorage.getEncryptionKey();
	        encryptedEmail = (email != null) ? Encryption.encrypt(email, secretKey) : null;
	    } catch (Exception e) {
	        logger.error("Failed to load encryption key or encrypt email", e);
	        errors.add("Internal error preparing account update.");
	    }
	    
	    if (encryptedEmail != null) {
	        try {
	            var existing = userDAO.getUserByEmail(encryptedEmail);
	            if (existing != null && existing.getUserID() != userID) {
	                errors.add("That email is already associated with another account.");
	            }
	        } catch (Exception e) {
	            logger.error("Email uniqueness check failed", e);
	            errors.add("Internal error checking email uniqueness.");
	        }
	    }
	    
	    try {
	        boolean isUpdated = userDAO.updateUserAccount(userID, username, encryptedEmail);
	        if (!isUpdated) {
	            errors.add("Failed to update your account. Please try again.");
	        }
	    } catch (Exception e) {
	        logger.error("Error occurred during user update: " + e.getMessage(), e);
	        errors.add("An unexpected error occurred while updating your account.");
	    }

	    if (!errors.isEmpty()) {
	        alert.showError("Account Update Failed", String.join("\n", errors), "");
	        return false;
	    }

	    alert.showInfo("Account Update Successful", "Your account has been updated successfully.", "");
	    return true;
	}
	
	/**
	 * Updates the user's password.
	 * 
	 * @param userID The ID of the user.
	 * @param newPassword The new password.
	 * @param inputtedCurrentPassword The current password.
	 * @return true if the update is successful, false otherwise.
	 */
	public PasswordUpdateResult updatePassword(int userID, String newPassword, String inputtedCurrentPassword) {
	    try {
	        String currentUserPassword = userDAO.getUserPassword(userID);
	        if (PasswordHasher.verifyPassword(inputtedCurrentPassword, currentUserPassword)) {
	            String hashedNewPassword = PasswordHasher.hashPassword(newPassword);
	            boolean isUpdated = userDAO.updateUserPassword(userID, hashedNewPassword);
	            return isUpdated ? PasswordUpdateResult.SUCCESS : PasswordUpdateResult.UPDATE_FAILED;
	        } else {
	            return PasswordUpdateResult.INCORRECT_CURRENT;
	        }
	    } catch (Exception e) {
	        logger.error("Error occurred during password update: " + e.getMessage(), e);
	        return PasswordUpdateResult.ERROR;
	    }
	}
	
	/**
	 * Enum representing possible results of a password update attempt.
	 */
	public enum PasswordUpdateResult {
	    SUCCESS,
	    INCORRECT_CURRENT,
	    UPDATE_FAILED,
	    ERROR
	}
	
	/**
	 * Updates the user's password when forgotten.
	 * 
	 * @param userID The ID of the user.
	 * @param newPassword The new password.
	 * @return true if the update is successful, false otherwise.
	 */
	public boolean updateForgottenPassword(int userID, String newPassword) {
		try {
	        String hashedNewPassword = PasswordHasher.hashPassword(newPassword);
	        boolean isUpdated = userDAO.updateUserPassword(userID, hashedNewPassword);
	        
	        if(isUpdated) {
	            System.out.println("Password updated successfully for user ID: " + userID);
	            return true;
	        } else {
	            System.out.println("Failed to update password for user ID: " + userID);
	            return false;
	        }
		}catch (Exception e) {
			logger.error("Error occurred during password update: " + e.getMessage(), e);
			System.out.println("Error occurred during password update: " + e.getMessage());
			return false;
		}
	}
	
	/**
	 * logs out the current user
	 * 
	 * @return true if logout is successful, false otherwise.
	 */
	public boolean logout() {
		if (loggedInUserID > 0) {
			System.out.println("Logging out user ID: " + loggedInUserID);
			loggedInUserID = -1; 
			return true;
		} else {
			System.out.println("No user is currently logged in.");
			return false;
		}
	}
	
	/**
	 * Get user object by user ID
	 * 
	 * @param userID The ID of the user.
	 * @return The user object if found, null otherwise.
	 */
	public user getUserByID(int userID) {
		user User = userDAO.getUserByID(userID);
		if(User != null) {
			return User;
		} else {
			System.out.println("User not found.");
			return null;
		}
	}
	
	/**
	 * Get user object by email
	 * 
	 * @param email The email of the user.
	 * @return The user object if found, null otherwise.
	 */
	public user getUserByEmail(String email) {
		if (email == null || email.isEmpty()) {
			System.out.println("Email is empty or null.");
			return null;
		}
		user User = userDAO.getUserByEmail(email);
		if(User != null) {
			return User;
		} else {
			System.out.println("User not found with email: " + email);
			
			return null;
		}
	}
	
	/**
	 * Verifies the security answers for a user.
	 * 
	 * @param userID The ID of the user.
	 * @param answer1 The first security answer.
	 * @param answer2 The second security answer.
	 * @param answer3 The third security answer.
	 * @return true if the answers match, false otherwise.
	 */
	public boolean verifySecurityAnswers(int userID, String answer1, String answer2, String answer3) {
		try {
			user User = userDAO.getUserSecurityAnswers(userID);
			System.out.println("Verifying security answers for user: " + User);
			if (User == null) {
				System.out.println("User not found for ID: " + userID);
				return false;
			}
			boolean isMatch1 = PasswordHasher.verifyPassword(answer1, User.getSecurityAnswer1());
			boolean isMatch2 = PasswordHasher.verifyPassword(answer2, User.getSecurityAnswer2());
			boolean isMatch3 = PasswordHasher.verifyPassword(answer3, User.getSecurityAnswer3());
			
			return isMatch1 && isMatch2 && isMatch3;
		} catch (Exception e) {
			logger.error("Error occurred during security answer verification: " + e.getMessage(), e);
			System.out.println("Error occurred during security answer verification: " + e.getMessage());
			return false;
		}
	}

	/**
	 * @return the currentUser
	 */
	public user getCurrentUser() {
		return currentUser;
	}

	/**
	 * @param currentUser the currentUser to set
	 */
	public void setCurrentUser(user currentUser) {
		this.currentUser = currentUser;
	}

}

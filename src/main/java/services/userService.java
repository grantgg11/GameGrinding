package services;

import java.util.Arrays;
import java.util.HashSet;
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
 * Service layer class for managing user authentication, registration, password updates,
 * role checks, and account maintenance in the application.
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
		// TODO Auto-generated constructor stub
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
			if (!isEmailValid(email)) {
				alert.showError("Invalid Email Format", "Please enter a valid email address.", "");
				return false;
			}
			
			// Check if the encrypted email already exists in the database
			if (userDAO.getUserByEmail(email) != null) {
				alert.showError("Email Already Exists", "User with this email already exists.", "");
				return false; 
			}

			// Validate password strength
			if (!isPasswordStrong(password)) {
				alert.showError("Weak Password", "Your password needs to have at least one upper case and lower case letter, a number, and a special character.", "");
				return false;
			}

			// Check if security questions are filled
			if (securityQuestion1.isEmpty() || securityQuestion2.isEmpty() || securityQuestion3.isEmpty()) {
				alert.showError("Security Questions Required", "Please fill in all security questions.", "");
				return false;
			}
	        // Hash the password 
	        System.out.println("Plaintext Password: " + password);
	        String hashedPassword = PasswordHasher.hashPassword(password);
	        System.out.println("Hashed Password (Stored in DB): " + hashedPassword);
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
		try {
			// Encrypt email before checking if it exists
			SecretKey secretKey = KeyStorage.getEncryptionKey();
			String encryptedEmail = Encryption.encrypt(email, secretKey);


			// Check if the encrypted email already exists in the database
			if (userDAO.getUserByEmail(encryptedEmail) != null) {
				System.out.println("User with this email already exists.");
				return false; 
			}
			
			// Validate email format
			if (!isEmailValid(email)) {
				alert.showError("Invalid Email Format", "Please enter a valid email address.", "");
				return false;
			}
	        boolean isUpdated = userDAO.updateUserAccount(userID, username, encryptedEmail);
	        
	        if (isUpdated) {
	            alert.showInfo("Update successful!", "Account updated successfully!", "");
	            return true;
	        } else {
	            alert.showError("Update Failed", "Failed to update your account. Please try again.", "");
	            return false;
	        }

		} catch (Exception e) {
			logger.error("Error occurred during user update: " + e.getMessage(), e);
			System.out.println("Error occurred during user update: " + e.getMessage());
			return false;
		}
	}
	
	/**
	 * Updates the user's password.
	 * 
	 * @param userID The ID of the user.
	 * @param newPassword The new password.
	 * @param inputtedCurrentPassword The current password.
	 * @return true if the update is successful, false otherwise.
	 */
	public boolean updatePassword(int userID, String newPassword, String inputtedCurrentPassword) {
		try {
			// Validate password strength
			if (!isPasswordStrong(newPassword)) {
				alert.showError("Weak Password", "Your password needs to have at least one upper case and lower case letter, a number, and a special character.", "");
				return false;
			}
			PasswordHasher passwordHasher = new PasswordHasher();		
			System.out.println("Plaintext Inputted Current Password: " + inputtedCurrentPassword);
			System.out.println("Looking up password for user ID: " + userID);

			String currentUserPassword = userDAO.getUserPassword(userID);
			
			if(PasswordHasher.verifyPassword(inputtedCurrentPassword, currentUserPassword)) {
				String hashedNewPassword = passwordHasher.hashPassword(newPassword);
				boolean isUpdated = userDAO.updateUserPassword(userID, hashedNewPassword);
		        if (isUpdated) {
		            alert.showInfo("Password Updated", "Password has been updated", "Password updated successfully!");
		            return true;
		        } else {
		            alert.showError("Password Update Failed", "Failed to update password. Please try again.", "");
		            return false;
		        }
			}else {
	            alert.showError("Incorrect Current Password", "The current password you entered is incorrect.", "");
	            return false;
	        
			}

		} catch (Exception e) {
			logger.error("Error occurred during password update: " + e.getMessage(), e);
			System.out.println("Error occurred during password update: " + e.getMessage());
			return false;
		}
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
	        PasswordHasher passwordHasher = new PasswordHasher();
	        String hashedNewPassword = passwordHasher.hashPassword(newPassword);

	        boolean isUpdated = userDAO.updateUserPassword(userID, hashedNewPassword);
	        if(isUpdated) {
	        	alert.showInfo("Password Updated", "Password has been updated", "Password updated successfully!");
	            System.out.println("Password updated successfully for user ID: " + userID);
	            return true;
	        } else {
	        	alert.showError("Password Update Failed","Password update failed", "Failed to update password. Please try again.");
	            System.out.println("Failed to update password for user ID: " + userID);
	            return false;
	        }
		}catch (Exception e) {
			logger.error("Error occurred during password update: " + e.getMessage(), e);
			System.out.println("Error occurred during password update: " + e.getMessage());
			alert.showError("Password Update Failed","Error", "An error occurred while updating the password. Please try again.");
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

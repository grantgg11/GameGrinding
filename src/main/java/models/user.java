package models;

import java.util.Objects;

/**
 * Represents a user in the application.
 * Stores user metadata such as username, email, password, and security questions.
 */
public class user {
	
	private int userID;					// Unique identifier for the user
	private String username;			// Username of the user
	private String email;				// Email of the user
	private String password;			// Password of the user
	private String securityAnswer1;		// Security answer to question 1
	private String securityAnswer2;		// Security answer to question 2
	private String securityAnswer3;		// Security answer to question 3
	private String role;				// Role of the user (e.g., admin, user)
	
	/** Default constructor */
	public user() {}
	
	/**
	 * Parameterized constructor for creating a user object with all fields.
	 * 
	 * @param userID
	 * @param username
	 * @param email
	 * @param password
	 * @param securityAnswer1
	 * @param securityAnswer2
	 * @param securityAnswer3
	 */
	public user(int userID, String username, String email, String password, String securityAnswer1, String securityAnswer2, String securityAnswer3) {
		this.userID = userID;
		this.username = username;
		this.email = email;
		this.password = password; 
		this.securityAnswer1 = securityAnswer1;
		this.securityAnswer2 = securityAnswer2;
		this.securityAnswer3 = securityAnswer3;
	}
	
	/**
	 * Parameterized constructor for creating a user object without userID.
	 * 
	 * @param username
	 * @param email
	 * @param password
	 * @param securityAnswer1
	 * @param securityAnswer2
	 * @param securityAnswer3
	 */
	public user(String username, String email, String password, String securityAnswer1, String securityAnswer2, String securityAnswer3) {
		this.username = username;
		this.email = email;
		this.password = password;	
		this.securityAnswer1 = securityAnswer1;
		this.securityAnswer2 = securityAnswer2;
		this.securityAnswer3 = securityAnswer3;
	}
	
	/**
	 * Minimal constructor for user settings.
	 * 
	 * @param username
	 * @param email
	 * @param password
	 */
	public user(String username, String email, String password) {
		this.username = username;
		this.email = email;
		this.password = password;	
	}
	
	/**
	 * Minimal constructor for display purposes
	 * 
	 * @param username
	 * @param email
	 */
	public user(String username, String email) {
		this.username = username;
		this.email = email;	
	}
	
	/**
	 * Minimal constructor for user login verification.
	 * 
	 * @param userID
	 * @param username
	 * @param email
	 */
	public user(int userID, String username, String email) {
		this.userID = userID;
		this.username = username;
		this.email = email;	
	}
	
	/**
	 * Minimal constructor for to .
	 * 
	 * @param userID
	 * @param username
	 */
	public user(int userID, String username) {
		this.userID = userID;
		this.username = username;	
	}
	
    /**
     * Constructor including ID, username, email, and password.
     * 
     * @param userID
     * @param username
     * @param email
     * @param password
     */
	public user(int userID, String username, String email, String password) {
		this.userID = userID;	
		this.username = username;
		this.email = email;
		this.password = password;
	}
	
	
	/**
	 * Minimal constructor for user settings.
	 * 
	 * @param userID
	 * @param username
	 * @param email
	 * @param password
	 * @param role
	 */
	public user(int userID, String username, String email, String password, String role) {
		this.userID = userID;
		this.username = username;
		this.email = email;
		this.password = password;
		this.role = role;
	}
	
	//------------------ Getters and Setters ------------------
	public int getUserID() {
		return userID;
	}

	public void setUserID(int userID) {
		this.userID = userID;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	public String getSecurityAnswer1() {
		return securityAnswer1;
	}
	public void setSecurityAnswer1(String securityAnswer1) {
		this.securityAnswer1 = securityAnswer1;
	}
	public String getSecurityAnswer2() {
		return securityAnswer2;
	}
	public void setSecurityAnswer2(String securityAnswer2) {
		this.securityAnswer2 = securityAnswer2;
	}
	public String getSecurityAnswer3() {
		return securityAnswer3;
	}
	public void setSecurityAnswer3(String securityAnswer3) {
		this.securityAnswer3 = securityAnswer3;
	}
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}
	
	
	
	public boolean isUsernameValid() {
		return username != null && username.length() >= 3 && username.length() <= 15;
	}
	

    /**
     * String representation for logging/debugging (excludes password and answers for security).
     * 
     * @return String representation of the user object.
     */
	@Override
	public String toString() {
		return "User{" +
				"userID=" + userID +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                '}';
	}
	
	/**
	 * Compares users by ID, username, and email.
	 * 
	 * @param o The object to compare with.
	 * @return true if the objects are equal, false otherwise.
	 */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        user user = (user) o;
        return userID == user.userID &&
                Objects.equals(username, user.username) &&
                Objects.equals(email, user.email);
    }
    
    /**
	 * Returns a hash code for the user object.
	 * 
	 * @return Hash code for the user object.
	 */
    @Override
    public int hashCode() {
        return Objects.hash(userID, username, email);
    }


	
}

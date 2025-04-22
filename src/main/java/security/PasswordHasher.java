package security;

import org.mindrot.jbcrypt.BCrypt;
/**
 * PasswordHasher class provides methods to hash passwords and verify them.
 * It uses the BCrypt hashing algorithm for secure password storage.
 */
public class PasswordHasher {

	/**
	 * Hashes a plain text password using BCrypt.
	 * 
	 * @param password The plain text password to hash.
	 * @return The hashed password.
	 */
	public String hashPassword(String password) {
		return BCrypt.hashpw(password, BCrypt.gensalt(12));
	}
	
	/**
	 * Verifies a plain text password against a hashed password.
	 * 
	 * @param plainPassword The plain text password to verify.
	 * @param hashedPassword The hashed password to compare against.
	 * @return True if the passwords match, false otherwise.
	 */
	public static boolean verifyPassword(String plainPassword, String hashedPassword) {
	    if (plainPassword == null || hashedPassword == null) {
	        return false;
	    }
	    try {
	        return BCrypt.checkpw(plainPassword, hashedPassword);
	    } catch (Exception e) {
	        e.printStackTrace();
	        return false;
	    }
	}

	
}

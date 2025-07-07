package security;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Security class for hashing and verifying passwords and security answers using BCrypt.
 * Designed to protect sensitive user authentication and recovery data in the application.
 */
public class Hasher {
	
    /**
     * Hashes a plain-text password using BCrypt with a salt factor of 12.
     *
     * @param password The plain-text password to hash.
     * @return A BCrypt-hashed password.
     */
	public String hashPassword(String password) {
		return BCrypt.hashpw(password, BCrypt.gensalt(12));
	}
	
	/**
     * Verifies that a user-entered password matches the stored hashed password.
     *
     * @param enteredPassword The plain-text password entered by the user.
     * @param hashedPassword  The stored BCrypt-hashed password.
     * @return true if the passwords match, false otherwise.
     */
	public boolean verifyPassword(String enteredPassword, String hashedPassword) {
        if (enteredPassword == null || enteredPassword.isEmpty() || hashedPassword == null || hashedPassword.isEmpty()) {
            System.out.println("Passwords cannot be null or empty.");
            return false;
        }
        return BCrypt.checkpw(enteredPassword, hashedPassword);
    }
	
    /**
     * Hashes a security question answer using normalized (lowercase, trimmed) input.
     * Uses the same BCrypt algorithm with a salt factor of 12.
     *
     * @param securityQuestion The answer to a security question (e.g., mother's maiden name).
     * @return The hashed version of the normalized answer, or null if the input is empty.
     */
	public String hashSecurityQuestion(String securityQuestion) {
		if (securityQuestion == null || securityQuestion.isEmpty() ) {
			System.out.println("Security question answer cannot be null or empty.");
			return null;
		}
		String normalizedAnswer = securityQuestion.toLowerCase().trim();
		return BCrypt.hashpw(normalizedAnswer, BCrypt.gensalt(12));	
	}
	
    /**
     * Verifies a user-entered security question answer against a stored hash.
     * Both answers are normalized before comparison.
     *
     * @param enteredAnswer The user-provided answer.
     * @param hashedAnswer  The stored hashed answer.
     * @return true if the normalized answers match, false otherwise.
     */
	public boolean verifySecurityQuestion(String enteredAnswer, String hashedAnswer) {
		if (enteredAnswer.isEmpty() || hashedAnswer.isEmpty()) {
			return false;
		}
		String normalizedAnswer = enteredAnswer.toLowerCase().trim();
		return BCrypt.checkpw(normalizedAnswer, hashedAnswer);
	}
}

package security;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the Hasher class, which provides secure hashing and verification methods
 * for passwords and security question answers.
 *
 * Tests include:
 * Password hashing and verification with normal, empty, and null input
 * Security question hashing and verification
 * Normalization handling (e.g., trimming, case insensitivity)
 * Edge cases such as empty or null input strings
 */
class HasherTest { 

	private Hasher hasher;
	
	/**
	 * Initializes the Hasher instance before each test.
	 */
	@BeforeEach
	void setUp(){
		hasher = new Hasher();
	}
	
	/**
	 * Verifies that a non-null hashed password is returned for a valid password input,
	 * and that the hash does not match the original password.
	 */
	@Test
	void testHashPassword_NotNull() {
        String password = "SecurePass123!";
        String hashed = hasher.hashPassword(password);
        assertNotNull(hashed, "Hashed password should not be null.");
        assertNotEquals(password, hashed, "Hashed password should not match original.");
	}
	
	/**
	 * Tests that hashing an empty password string returns a non-null hash
	 * that does not match the original.
	 */
	@Test
	void testHashPassword_EmptyString() {
		String password = "";
		String hashed = hasher.hashPassword(password);
		assertNotNull(hashed, "Hashed empty password should not be null.");
		assertNotEquals(password, hashed, "Hashed empty password should not match original.");
	}
	
	/**
	 * Verifies that hashing a null password input returns a non-null hash
	 * and does not equal the null input.
	 */
	@Test
	void testHashPassword_NullInput() {
		String password = null;
		String hashed = hasher.hashPassword(password);
		assertNotNull(hashed, "Hashed null password should not be null.");
		assertNotEquals(password, hashed, "Hashed null password should not match original.");
	}
	
	/**
	 * Tests successful verification of a hashed password against the original password.
	 */
	@Test
	void testVerifyPassword_Success() {
        String password = "MySecret123";
        String hashed = hasher.hashPassword(password);
        assertTrue(hasher.verifyPassword(password, hashed), "Correct password should match hash.");
	}
	
	/**
	 * Ensures that an incorrect password does not match the hashed password.
	 */
	@Test
	void testVerifyPassword_Failed() {
        String password = "RightPassword";
        String hashed = hasher.hashPassword(password);
        assertFalse(hasher.verifyPassword("WrongPassword", hashed), "Incorrect password should not match hash.");
	}
	
	/**
	 * Verifies that null inputs for password or hash result in failed verification.
	 */
	@Test
	void testVerifyPassword_NullInputs(){
        assertFalse(hasher.verifyPassword(null, null), "Null inputs should return false.");
        assertFalse(hasher.verifyPassword("password", null), "Null hash should return false.");
        assertFalse(hasher.verifyPassword(null, "hash"), "Null password should return false.");
	}
	
	/**
	 * Tests that empty passwords and empty hashes do not verify successfully.
	 */
	@Test
	void testVerifyPassword_EmptyStringBehavior() {
	    String password = "";
	    String hashed = hasher.hashPassword(password);
	    assertFalse(hasher.verifyPassword("", hashed), "Empty password should not be accepted.");
	    assertFalse(hasher.verifyPassword(password, ""), "Empty hash should not match any password.");
	}


	/**
	 * Verifies that a trimmed and normalized security question input produces a non-null hash.
	 */
	@Test 
	void testHashSecurityQuestion_NormalizedNotNull() {
        String input = "  MaidenName ";
        String hashed = hasher.hashSecurityQuestion(input);
        assertNotNull(hashed, "Hashed security question should not be null.");
	}
	
	/**
	 * Ensures that hashing an empty security question returns null.
	 */
	@Test
	void testHashSecurityQuestion_EmptyString() {
		String input = "";
		String hashed = hasher.hashSecurityQuestion(input);
		assertNull(hashed, "Hashing an empty string should return null.");
	}
	
	/**
	 * Verifies that hashing a null input for security question returns null.
	 */
	@Test
	void testHashSecurityQuestion_NullInput() {
		String input = null;
		String hashed = hasher.hashSecurityQuestion(input);
		assertNull(hashed, "Hashing a null string should return null.");
	}
	
	/**
	 * Tests successful normalization and hashing of a valid security question input.
	 */
	@Test
	void testHashSecurityQuestion_Success() {
		String input = "  FavoriteFruit  ";
		String hashed = hasher.hashSecurityQuestion(input);
		assertNotNull(hashed, "Hashed security question should not be null.");
		assertNotEquals(input, hashed, "Hashed security question should not match original.");
	}
	
	/**
	 * Verifies successful validation of a normalized security question answer.
	 */
    @Test
    void testVerifySecurityQuestion_Success() {
        String answer = "Pineapple";
        String hashed = hasher.hashSecurityQuestion(answer);
        assertTrue(hasher.verifySecurityQuestion("  pineapple  ", hashed), "Normalized input should match hashed answer.");
    }
    
    /**
     * Verifies that an incorrect security question answer does not match the stored hash.
     */
    @Test
    void testVerifySecurityQuestion_Incorrect() {
        String answer = "Pineapple";
        String hashed = hasher.hashSecurityQuestion(answer);
        assertFalse(hasher.verifySecurityQuestion("Strawberry", hashed), "Incorrect answer should not match hash.");
    }
    
    /**
     * Ensures that empty strings as input or hash for security question return false during verification.
     */
    @Test
    void testVerifySecurityQuestion_EmptyInput() {
        String hashed = hasher.hashSecurityQuestion("banana");
        assertFalse(hasher.verifySecurityQuestion("", hashed), "Empty entered answer should return false.");
        assertFalse(hasher.verifySecurityQuestion("banana", ""), "Empty stored hash should return false.");
    }
    
    /**
     * Additional test confirming that an empty string returns null when hashed for a security question.
     */
    @Test
    void testHashSecurityQuestion_EmptyReturnsNull() {
        assertNull(hasher.hashSecurityQuestion(""), "Empty string should return null hash.");
    }

}

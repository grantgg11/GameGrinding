package security;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for the PasswordHasher class, which provides password hashing and verification.
 *
 * This test class verifies:
 * That hashing produces non-null, non-empty, and altered output
 * That password verification works correctly for matching and non-matching inputs
 * That edge cases such as null inputs are handled safely
 */
class PasswordHasherTest { 

	/**
	 * Tests that hashing a password produces a valid, non-null, and non-empty hash
	 * that does not match the original password.
	 */
    @Test
    void testHashPasswordProducesValidHash() {
        String password = "securePassword123";
        String hash = PasswordHasher.hashPassword(password);
        assertNotNull(hash, "Hashed password should not be null");
        assertFalse(hash.isEmpty(), "Hashed password should not be empty");
        assertNotEquals(password, hash, "Hashed password should not match plain password");
    }
    
    /**
     * Tests that password verification succeeds when the correct password and its hash are provided.
     */
    @Test
    void testVerifyPasswordSuccess() {
        String password = "mySecretPass!";
        String hash = PasswordHasher.hashPassword(password);

        assertTrue(PasswordHasher.verifyPassword(password, hash), "Password should verify correctly");
    }

    /**
	 * Tests that password verification fails when a wrong password is provided.
	 */
    @Test
    void testVerifyPasswordFailure() {
        String original = "originalPassword";
        String wrong = "wrongPassword";
        String hash = PasswordHasher.hashPassword(original);

        assertFalse(PasswordHasher.verifyPassword(wrong, hash), "Wrong password should not verify");
    }

    /**
     * Tests that password verification returns false when the plain password is null.
     */
    @Test
    void testVerifyPasswordNullPlainPassword() {
        String hash = PasswordHasher.hashPassword("testPassword");

        assertFalse(PasswordHasher.verifyPassword(null, hash), "Null plain password should return false");
    }

    /**
     * Tests that password verification returns false when the hashed password is null.
     */
    @Test
    void testVerifyPasswordNullHashedPassword() {
        assertFalse(PasswordHasher.verifyPassword("testPassword", null), "Null hashed password should return false");
    }

    /**
     * Tests that password verification returns false when both the plain password and hash are null.
     */
    @Test
    void testVerifyPasswordBothNull() {
        assertFalse(PasswordHasher.verifyPassword(null, null), "Both inputs null should return false");
    }
}

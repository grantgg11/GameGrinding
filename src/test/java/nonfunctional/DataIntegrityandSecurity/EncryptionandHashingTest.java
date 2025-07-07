package nonfunctional.DataIntegrityandSecurity;


import static org.junit.jupiter.api.Assertions.*;

import javax.crypto.SecretKey;
 
import org.junit.jupiter.api.Test;

import security.Encryption;
import security.PasswordHasher;

/**
 * Non-functional tests for verifying that user account information and security answers
 * are encrypted and hashed properly to meet US-7:
 * 
 * US-7 – The application must encrypt user’s account information and the answers to the security questions.
 */
public class EncryptionandHashingTest {

    /**
     * Tests that email encryption and decryption using AES works correctly.
     * Ensures the encrypted value differs from the original and the decrypted value matches.
     */
    @Test
    void testEmailEncryptionDecryption_withAES() throws Exception {
        String originalEmail = "secureuser@example.com";
        SecretKey secretKey = Encryption.generateKey();
        String encryptedEmail = Encryption.encrypt(originalEmail, secretKey);
        new Encryption();
		String decryptedEmail = Encryption.decrypt(encryptedEmail, secretKey);

        assertNotNull(encryptedEmail, "Encrypted email should not be null");
        assertNotEquals(originalEmail, encryptedEmail, "Encrypted email should differ from original");
        assertEquals(originalEmail, decryptedEmail, "Decrypted email should match original");
    }

    /**
     * Tests password hashing using BCrypt and verifies it with the correct plain-text password.
     */
    @Test
    void testPasswordHashingAndVerification_withBCrypt() {
        String originalPassword = "StrongP@ssw0rd!";

        String hashedPassword = PasswordHasher.hashPassword(originalPassword);

        assertNotNull(hashedPassword, "Hashed password should not be null");
        assertTrue(PasswordHasher.verifyPassword(originalPassword, hashedPassword),
                "Password verification should return true for correct password");
    }

    /**
     * Tests hashing of a security answer using BCrypt and verifies it matches the original.
     */
    @Test
    void testSecurityAnswerHashingAndVerification_withBCrypt() {
        String answer = "RedFox42";

        String hashedAnswer = PasswordHasher.hashPassword(answer);

        assertNotNull(hashedAnswer, "Hashed answer should not be null");
        assertTrue(PasswordHasher.verifyPassword(answer, hashedAnswer),
                "Security answer verification should return true for correct answer");
    }
}
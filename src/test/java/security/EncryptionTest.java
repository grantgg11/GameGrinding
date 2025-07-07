package security;

import static org.junit.jupiter.api.Assertions.*;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
 
import javax.crypto.SecretKey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the Encryption utility class.
 *
 * This class tests encryption and decryption functionality using symmetric encryption.
 * It covers:
 * Key generation
 * Encryption and decryption of standard, empty, and Unicode strings
 * Handling of null inputs and invalid keys
 * Decryption with incorrect or malformed data
 *
 * All tests are executed using a valid SecretKey generated before each test.
 */

class EncryptionTest {
	
	private SecretKey secretKey;
	
	/**
	 * Generates a valid SecretKey for use in each test.
	 *
	 * @throws NoSuchAlgorithmException if the encryption algorithm is not available
	 */
	@BeforeEach
	void setUp() throws NoSuchAlgorithmException {
		secretKey = Encryption.generateKey();
	}
	
	/**
	 * Tests that the key generation function successfully returns a non-null SecretKey.
	 *
	 * @throws NoSuchAlgorithmException if key generation fails
	 */
	@Test
	void testGenerateKey() throws NoSuchAlgorithmException{
		assertNotNull(secretKey, "Generated key should no be null");
	}
	
	/**
	 * Verifies successful encryption and decryption of a standard string.
	 *
	 * @throws Exception if encryption or decryption fails
	 */
	@Test 
	void testEncryptDecrypt_Success() throws Exception{
		String originalData = "SensitiveInfo123";
        String encrypted = Encryption.encrypt(originalData, secretKey);
        assertNotNull(encrypted, "Encrypted data should not be null");
        String decrypted = Encryption.decrypt(encrypted, secretKey);
        assertEquals(originalData, decrypted, "Decrypted data should match the original");
	}
	
	/**
	 * Tests encryption and decryption of Unicode characters, including emojis and multibyte characters.
	 *
	 * @throws Exception if encryption or decryption fails
	 */
	@Test
	void testEncryptDecrypt_UnicodeCharacters() throws Exception {
	    String originalData = "こんにちは世界🌍";
	    String encrypted = Encryption.encrypt(originalData, secretKey);
	    assertNotNull(encrypted);
	    String decrypted = Encryption.decrypt(encrypted, secretKey);

	    assertEquals(originalData, decrypted, "Decrypted Unicode string should match the original");
	}
	
	/**
	 * Tests encryption and decryption of an empty string.
	 *
	 * @throws Exception if encryption or decryption fails
	 */
    @Test
    void testEncryptDecrypt_EmptyString() throws Exception {
        String originalData = "";
        String encrypted = Encryption.encrypt(originalData, secretKey);
        assertNotNull(encrypted, "Encrypted data should not be null");
        String decrypted = Encryption.decrypt(encrypted, secretKey);
        assertEquals(originalData, decrypted, "Decrypted empty string should match the original");
    }

    /**
     * Verifies that encrypting null input throws a NullPointerException.
     */
    @Test
    void testEncrypt_NullInput() {
        assertThrows(NullPointerException.class, () -> {
            Encryption.encrypt(null, secretKey);
        });
    }
    
    /**
     * Verifies that passing a null key to the encryption method throws InvalidKeyException.
     *
     * @throws Exception if thrown from encryption
     */
    @Test
    void testEncrypt_NullKey() throws Exception {
        String data = "TestData";
        assertThrows(InvalidKeyException.class, () -> {
            Encryption.encrypt(data, null);
        }, "Encrypting with a null key should throw InvalidKeyException");
    }

    /**
     * Tests that attempting to decrypt using the wrong key results in an exception.
     *
     * @throws Exception if encryption fails
     */
    @Test
    void testDecrypt_WithWrongKey() throws Exception {
        String originalData = "TestData";
        String encrypted = Encryption.encrypt(originalData, secretKey);
        SecretKey wrongKey = Encryption.generateKey();
        assertThrows(Exception.class, () -> {
            Encryption.decrypt(encrypted, wrongKey);
        }, "Decrypting with a wrong key should throw an exception");
    }
    
    /**
     * Verifies that decrypting with a null key throws InvalidKeyException.
     *
     * @throws Exception if encryption fails
     */
    @Test
    void testDecrypt_NullKey() throws Exception {
        String data = "HelloWorld";
        String encrypted = Encryption.encrypt(data, secretKey);
        assertThrows(InvalidKeyException.class, () -> {
            Encryption.decrypt(encrypted, null);
        }, "Decrypting with a null key should throw InvalidKeyException");
    }
    
    /**
     * Verifies that attempting to decrypt null encrypted data throws a NullPointerException.
     */
    @Test
    void testDecrypt_NullEncryptedData() {
        assertThrows(NullPointerException.class, () -> {
            Encryption.decrypt(null, secretKey);
        }, "Decrypting null input should throw NullPointerException");
    }
    
    /**
     * Verifies that decrypting an invalid Base64 string results in an exception.
     */
    @Test
    void testDecrypt_InvalidBase64String() {
        String invalidData = "thisIsNotBase64Encoded";
        assertThrows(Exception.class, () -> {
            Encryption.decrypt(invalidData, secretKey);
        }, "Decrypting invalid Base64 string should throw an exception");
    }

    /**
     * Tests successful decryption of data encrypted with a valid key.
     *
     * @throws Exception if encryption or decryption fails
     */
    @Test
    void testDecrypt_Success() throws Exception {
        SecretKey key = Encryption.generateKey();
        String original = "ThisIsASecret";
        String encrypted = Encryption.encrypt(original, key);
        String decrypted = Encryption.decrypt(encrypted, key);

        assertEquals(original, decrypted, "Decrypted value should match the original");
    }


}

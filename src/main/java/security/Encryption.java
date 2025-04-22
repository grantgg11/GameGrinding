package security;

import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

/**
 * Security class for AES-based symmetric encryption and decryption.
 * Used for encrypting sensitive user data (e.g., emails).
 */
public class Encryption {
	
	private static final String ALGORITHM = "AES"; // AES encryption algorithm
	
	/**
	 * Generates a new AES encryption key.
	 * 
	 * @return SecretKey object representing the generated key.
	 * @throws NoSuchAlgorithmException if the algorithm is not available.
	 */
	public static SecretKey generateKey() throws NoSuchAlgorithmException {
		KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
		keyGenerator.init(256);
		return keyGenerator.generateKey();
	}
	
	/**
	 * Encrypts the given data using the provided AES key.
	 * 
	 * @param data The data to encrypt.
	 * @param key The AES key to use for encryption.
	 * @return Base64-encoded string of the encrypted data.
	 * @throws Exception if encryption fails.
	 */
	public static String encrypt(String data, SecretKey key) throws Exception {
		Cipher cipher = Cipher.getInstance(ALGORITHM);
		cipher.init(Cipher.ENCRYPT_MODE, key);
		byte[] encryptedData = cipher.doFinal(data.getBytes());
		return Base64.getEncoder().encodeToString(encryptedData);
	}
	
	/**
	 * Decrypts the given encrypted data using the provided AES key.
	 * 
	 * @param encryptedData The Base64-encoded encrypted data.
	 * @param key The AES key to use for decryption.
	 * @return The decrypted data as a string.
	 * @throws Exception if decryption fails.
	 */
	public String decrypt(String encryptedData, SecretKey key) throws Exception {
		Cipher cipher = Cipher.getInstance(ALGORITHM);
		cipher.init(Cipher.DECRYPT_MODE, key);
		byte[] decryptedData = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
		return new String(decryptedData);
	}

	
}

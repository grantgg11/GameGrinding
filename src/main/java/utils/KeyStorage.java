package utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import security.Encryption;

/**
 * Utility class for securely storing, retrieving, and generating AES encryption keys.
 * The key is saved as a Base64-encoded file on disk.
 */
public class KeyStorage {
	
	// Path to where the AES key is saved
	private static final String KEY_FILE = "C:\\Users\\Owner\\gameGrindingCapstone\\GameGrinding\\encryption.key";
	
    /**
     * Stores a SecretKey to disk using Base64 encoding.
     *
     * @param key the AES key to store
     * @throws Exception if an I/O or encoding error occurs
     */
	public static void storeKey(SecretKey key) throws Exception {
		byte[] encoded = key.getEncoded();
		Files.write(Paths.get(KEY_FILE), Base64.getEncoder().encode(encoded));
	}
	
    /**
     * Loads a SecretKey from disk.
     *
     * @return the AES key from file
     * @throws Exception if the file doesn't exist, is invalid, or decoding fails
     */
	public static SecretKey loadKey() throws Exception {
	    Path path = Paths.get(KEY_FILE);
	    
	    if (!Files.exists(path) || Files.isDirectory(path)) {  
	        throw new IOException("Encryption key file not found: " + KEY_FILE);
	    }
		byte[] encoded = Base64.getDecoder().decode(Files.readAllBytes(Paths.get(KEY_FILE)));
        return new SecretKeySpec(encoded, "AES");
	}
	
	/**
	 * Retrieves the encryption key, generating a new one if it doesn't exist.
	 * This method is designed to ensure a valid key is always returned.
	 *
	 * @return the AES key
	 * @throws Exception if an I/O or encoding error occurs
	 */
	public static SecretKey getEncryptionKey() throws Exception {
	    Path path = Paths.get(KEY_FILE);

	    if (!Files.exists(path)) {
	        System.out.println("Encryption key file not found. Generating a new one...");
	        SecretKey newKey = Encryption.generateKey();
	        storeKey(newKey);  
	        return newKey;
	    }

	    byte[] encoded = Base64.getDecoder().decode(Files.readAllBytes(path));
	    return new SecretKeySpec(encoded, "AES");
	}


}

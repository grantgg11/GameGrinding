package utils;

import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.mockito.Mockito; 

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.file.*;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import security.Encryption;

/**
 * Unit tests for the KeyStorage utility class.
 *
 * Tests cover functionality for storing, loading, and retrieving AES encryption keys.
 * Includes mocking of file system behavior to isolate file I/O and validate base64 encoding/decoding.
 */

class KeyStorageTest {
	
	 private static Path tempKeyFile;
	 private static final byte[] dummyKeyBytes = new byte[16]; 

	 	/**
	 	 * Sets up a temporary file used for testing key storage and retrieval.
	 	 *
	 	 * @throws IOException if temporary file creation fails
	 	 */
	    @BeforeAll
	    static void setup() throws IOException {
	        tempKeyFile = Files.createTempFile("test_encryption", ".key");
	        tempKeyFile.toFile().deleteOnExit();
	    }
	    
	    /**
	     * Tests that storeKey correctly writes a Base64-encoded AES key to a file.
	     *
	     * @throws Exception if an I/O or encoding error occurs
	     */
	    @Test
	    void testStoreKey_WritesBase64EncodedKeyToFile() throws Exception {
	        SecretKey secretKey = new SecretKeySpec(dummyKeyBytes, "AES");

	        try (MockedStatic<Paths> mockedPaths = Mockito.mockStatic(Paths.class)) {
	            mockedPaths.when(() -> Paths.get(anyString())).thenReturn(tempKeyFile);

	            KeyStorage.storeKey(secretKey);

	            byte[] written = Files.readAllBytes(tempKeyFile);
	            byte[] decoded = Base64.getDecoder().decode(written);
	            assertArrayEquals(dummyKeyBytes, decoded);
	        }
	    }

	    /**
	     * Tests that loadKey reads and decodes a Base64-encoded AES key from file correctly.
	     *
	     * @throws Exception if reading or decoding the key fails
	     */
	    @Test
	    void testLoadKey_ReturnsCorrectSecretKey() throws Exception {
	        byte[] encoded = Base64.getEncoder().encode(dummyKeyBytes);
	        Files.write(tempKeyFile, encoded);

	        try (MockedStatic<Paths> mockedPaths = Mockito.mockStatic(Paths.class)) {
	            mockedPaths.when(() -> Paths.get(anyString())).thenReturn(tempKeyFile);

	            SecretKey loadedKey = KeyStorage.loadKey();
	            assertArrayEquals(dummyKeyBytes, loadedKey.getEncoded());
	            assertEquals("AES", loadedKey.getAlgorithm());
	        }
	    }

	    /**
	     * Tests that loadKey throws an IOException when the key file is missing.
	     */
	    @Test
	    void testLoadKey_ThrowsIfFileMissing() {
	        Path fakePath = Paths.get("nonexistent_file.key");

	        try (MockedStatic<Paths> mockedPaths = Mockito.mockStatic(Paths.class)) {
	            mockedPaths.when(() -> Paths.get(anyString())).thenReturn(fakePath);

	            assertThrows(IOException.class, KeyStorage::loadKey);
	        }
	    }

	    /**
	     * Tests that getEncryptionKey generates a new AES key and stores it when no key file exists.
	     *
	     * @throws Exception if key generation or storage fails
	     */
	    @Test
	    void testGetEncryptionKey_GeneratesAndStoresNewKeyIfMissing() throws Exception {
	        Path fakePath = Paths.get("missing.key");

	        SecretKey mockKey = new SecretKeySpec(dummyKeyBytes, "AES");

	        try (MockedStatic<Paths> mockedPaths = Mockito.mockStatic(Paths.class);
	             MockedStatic<Files> mockedFiles = Mockito.mockStatic(Files.class);
	             MockedStatic<Encryption> mockedEncryption = Mockito.mockStatic(Encryption.class)) {

	            mockedPaths.when(() -> Paths.get(anyString())).thenReturn(fakePath);
	            mockedFiles.when(() -> Files.exists(fakePath)).thenReturn(false);
	            mockedEncryption.when(Encryption::generateKey).thenReturn(mockKey);

	            mockedFiles.when(() -> Files.write(eq(fakePath), any(byte[].class))).thenAnswer(invocation -> {
	                byte[] toWrite = invocation.getArgument(1);
	                Files.write(tempKeyFile, toWrite);
	                return tempKeyFile;
	            });

	            SecretKey result = KeyStorage.getEncryptionKey();
	            assertNotNull(result);
	            assertArrayEquals(dummyKeyBytes, result.getEncoded());
	        }
	    }

	    /**
	     * Tests that getEncryptionKey loads an existing AES key from file if it already exists.
	     *
	     * @throws Exception if key loading fails
	     */
	    @Test
	    void testGetEncryptionKey_LoadsExistingKey() throws Exception {
	        byte[] encoded = Base64.getEncoder().encode(dummyKeyBytes);
	        Files.write(tempKeyFile, encoded);

	        try (MockedStatic<Paths> mockedPaths = Mockito.mockStatic(Paths.class);
	             MockedStatic<Files> mockedFiles = Mockito.mockStatic(Files.class)) {

	            mockedPaths.when(() -> Paths.get(anyString())).thenReturn(tempKeyFile);
	            mockedFiles.when(() -> Files.exists(tempKeyFile)).thenReturn(true);
	            mockedFiles.when(() -> Files.readAllBytes(tempKeyFile)).thenReturn(encoded);

	            SecretKey result = KeyStorage.getEncryptionKey();
	            assertNotNull(result);
	            assertArrayEquals(dummyKeyBytes, result.getEncoded());
	        }
	    }
}

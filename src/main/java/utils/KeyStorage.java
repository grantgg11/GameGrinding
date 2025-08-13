package utils;

import java.io.IOException;
import java.nio.file.*;
import java.util.Base64;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import security.Encryption;

/**
 * KeyStorage is a utility class responsible for securely managing the AES encryption key
 * used by the GameGrinding application.
 * 
 * This class handles:
 * - Determining the storage location for the AES key, with support for customizable
 *   paths via system properties for flexibility in testing or CI environments.
 * - Creating a new AES key if one does not already exist and storing it securely on disk.
 * - Loading an existing AES key from disk for use in encryption and decryption operations.
 * - Ensuring the key is stored in Base64-encoded format to maintain safe and consistent
 *   storage across different environments.
 * 
 * Key features:
 * - Default key storage location is the user's home directory under `.gamegrinding/encryption.key`,
 *   but this can be overridden using system properties.
 * - Ensures parent directories exist and attempts to set hidden attributes for improved
 *   security on Windows systems.
 * - Automatically generates and stores a new AES key if the key file is missing.
 * - Uses Java’s SecretKeySpec to reconstruct the AES key from stored Base64-encoded data.
 */
public class KeyStorage {

    private static final String KEY_PATH_PROP = "gamegrinding.keyPath";
    private static final String KEY_DIR_PROP  = "gamegrinding.keyDir";

    /**
     * Resolves the path to the encryption key file.
     * 
     * If the system property "gamegrinding.keyPath" is set, that path is used.
     * Otherwise, if "gamegrinding.keyDir" is set, the key is stored in that directory.
     * If neither is set, the default location is user.home/.gamegrinding/encryption.key.
     *
     * @return the resolved path to the encryption key file
     */
    private static Path keyPath() {
        String explicit = System.getProperty(KEY_PATH_PROP);
        if (explicit != null && !explicit.isBlank()) {
            return Paths.get(explicit);
        }
        String dir = System.getProperty(KEY_DIR_PROP);
        Path base = (dir != null && !dir.isBlank())
                ? Paths.get(dir)
                : Paths.get(System.getProperty("user.home"), ".gamegrinding");
        return base.resolve("encryption.key");
    }

    /**
     * Ensures the parent directory for the given path exists.
     * 
     * If the directory does not exist, it is created. On Windows, the folder is
     * marked as hidden if possible.
     *
     * @param path the path whose parent directory should be created
     * @throws IOException if the directory cannot be created
     */
    private static void ensureParentDir(Path path) throws IOException {
        Path parent = path.toAbsolutePath().getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
            try {
                Files.setAttribute(parent, "dos:hidden", true);
            } catch (UnsupportedOperationException | IOException ignored) {}
        }
    }

    /**
     * Stores the given AES encryption key to disk.
     * 
     * The key is encoded with Base64 before being written to the file.
     * If the file already exists, it will be overwritten.
     *
     * @param key the AES key to store
     * @throws Exception if an I/O or encoding error occurs
     */
    public static void storeKey(SecretKey key) throws Exception {
        Path path = keyPath();
        ensureParentDir(path);
        byte[] encoded = Base64.getEncoder().encode(key.getEncoded());
        Files.write(path, encoded, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    /**
     * Loads the AES encryption key from disk.
     * 
     * The key file must exist and contain valid Base64-encoded key data.
     *
     * @return the loaded AES key
     * @throws Exception if the file is missing, is a directory, or contains invalid data
     */
    public static SecretKey loadKey() throws Exception {
        Path path = keyPath();
        if (!Files.exists(path) || Files.isDirectory(path)) {
            throw new IOException("Encryption key file not found: " + path.toAbsolutePath());
        }
        byte[] decoded = Base64.getDecoder().decode(Files.readAllBytes(path));
        return new SecretKeySpec(decoded, "AES");
    }

    /**
     * Retrieves the AES encryption key, creating one if it does not already exist.
     * 
     * If the key file is missing, a new AES key is generated, stored to disk, and returned.
     * If the key file exists, it is loaded and returned.
     *
     * @return the AES key
     * @throws Exception if a key cannot be generated, stored, or loaded
     */
    public static SecretKey getEncryptionKey() throws Exception {
        Path path = keyPath();
        if (!Files.exists(path)) {
            System.out.println("Encryption key not found. Generating a new one at: " + path.toAbsolutePath());
            SecretKey newKey = Encryption.generateKey();
            storeKey(newKey);
            return newKey;
        }
        return loadKey();
    }
}

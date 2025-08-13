package security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import database.UserDAO;
import models.user;

/**
 * Unit tests for the AuthManager class.
 *
 * This test class verifies the authentication logic of the AuthManager, including:
 * Successful login with correct credentials
 * Failed login due to invalid password
 * Handling of non-existent users
 * Ensuring authenticated user is correctly set or cleared
 * Handling exceptions thrown during authentication
 *
 * The tests use Mockito to mock dependencies such as UserDAO and Hasher.
 */
public class AuthManagerTest {
 
    private AuthManager authManager;
    private UserDAO mockUserDAO;
    private Hasher mockHasher;

    /**
     * Initializes the AuthManager instance and its mocked dependencies
     * (UserDAO and Hasher) before each test.
     */
    @BeforeEach
    public void setUp() {
        mockUserDAO = mock(UserDAO.class);
        mockHasher = mock(Hasher.class);
        authManager = new AuthManager(mockUserDAO, mockHasher);
    }

    /**
     * Tests successful user authentication when the email exists and
     * the password verification returns true.
     * Verifies that the authenticated user is correctly set.
     */
    @Test
    public void testAuthenticateUser_SuccessfulLogin() {
        String email = "test@example.com";
        String password = "password123";
        user testUser = new user();
        testUser.setEmail(email);
        testUser.setPassword("hashedPassword");
        testUser.setUserID(101);

        when(mockUserDAO.getUserByEmail(email)).thenReturn(testUser);
        when(mockHasher.verifyPassword(password, "hashedPassword")).thenReturn(true);

        boolean result = authManager.authenticateUser(email, password);

        assertTrue(result);
        assertEquals(testUser, authManager.getAuthenticatedUser());
    }

    /**
     * Tests authentication failure when the provided password does not
     * match the stored hash. Ensures the authenticated user is not set.
     */
    @Test
    public void testAuthenticateUser_InvalidPassword() {
        String email = "test@example.com";
        String password = "wrongpassword";
        user testUser = new user();
        testUser.setEmail(email);
        testUser.setPassword("hashedPassword");

        when(mockUserDAO.getUserByEmail(email)).thenReturn(testUser);
        when(mockHasher.verifyPassword(password, "hashedPassword")).thenReturn(false);

        boolean result = authManager.authenticateUser(email, password);

        assertFalse(result);
        assertNull(authManager.getAuthenticatedUser());
    }

    /**
     * Tests authentication when the user is not found in the database.
     * Verifies that authentication fails and no user is set.
     */
    @Test
    public void testAuthenticateUser_UserNotFound() {
        String email = "nonexistent@example.com";
        String password = "any";
        when(mockUserDAO.getUserByEmail(email)).thenReturn(null);
        boolean result = authManager.authenticateUser(email, password);
        assertFalse(result);
        assertNull(authManager.getAuthenticatedUser());
    }
    
    /**
     * Verifies that the authenticated user is not set when the password
     * verification fails, even if the user exists in the database.
     */
    @Test
    public void testAuthenticateUser_FailedPasswordDoesNotSetUser() {
        String email = "test@example.com";
        String password = "wrongpassword";

        user testUser = new user();
        testUser.setEmail(email);
        testUser.setPassword("hashedPassword");

        when(mockUserDAO.getUserByEmail(email)).thenReturn(testUser);
        when(mockHasher.verifyPassword(password, "hashedPassword")).thenReturn(false);

        authManager.authenticateUser(email, password);

        assertNull(authManager.getAuthenticatedUser(), "Authenticated user should be null after failed login.");
    }

    /**
     * Tests that authentication gracefully handles exceptions thrown by the DAO.
     * Verifies that authentication fails and no user is set.
     */
    @Test
    public void testAuthenticateUser_ExceptionThrown() {
        String email = "test@example.com";
        String password = "password123";

        when(mockUserDAO.getUserByEmail(email)).thenThrow(new RuntimeException("DB error"));

        boolean result = authManager.authenticateUser(email, password);

        assertFalse(result, "Authentication should fail if an exception is thrown.");
        assertNull(authManager.getAuthenticatedUser(), "Authenticated user should remain null after exception.");
    }

    /**
     * Tests the getAuthenticatedUser method after a successful login.
     * Ensures it returns the correct user object.
     */
    @Test
    void testGetAuthenticatedUser_Success() {
        user testUser = new user(250, "testUsername", "testEmail@test.com", "TestPassword120!", "Admin");
        when(mockUserDAO.getUserByEmail("testEmail@test.com")).thenReturn(testUser);
        when(mockHasher.verifyPassword("TestPassword120!", testUser.getPassword())).thenReturn(true);
        authManager.authenticateUser("testEmail@test.com", "TestPassword120!");
        user result = authManager.getAuthenticatedUser();
        assertNotNull(result, "Authenticated user should not be null.");
        assertEquals(testUser, result, "Authenticated user should match the expected user.");
    }
}


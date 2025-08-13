package nonfunctional.DataIntegrityandSecurity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
 
import database.UserDAO;
import models.user;
import security.AuthManager;
import security.PasswordHasher;
import services.userService;
import utils.AlertHelper;

/**
 * SecureLoginProcessTest verifies the security of the user login process (US-8) in the
 * GameGrinding application.
 *
 * Tests cover:
 * - Successful login with valid credentials, ensuring password hashing, credential verification,
 *   and correct session state.
 * - Login failure for incorrect passwords, invalid email formats, and empty or null credentials.
 *
 * Using mocked UserDAO, AuthManager, and AlertHelper dependencies, these tests isolate authentication
 * logic from the database while ensuring secure input validation, proper session handling, and compliance
 * with non-functional security requirements.
 */
public class SecureLoginProcessTest {

    private userService service;
    private UserDAO mockUserDAO;
    private AuthManager mockAuthManager;
    private AlertHelper mockAlert;

    private final String testEmail = "user@example.com";
    private final String testPassword = "StrongP@ssw0rd!";
    private final String hashedPassword = PasswordHasher.hashPassword(testPassword);

    /**
	 * Sets up the userService with mocked dependencies before each test.
	 * This allows us to isolate the tests from actual database and authentication logic.
	 */
    @BeforeEach
    void setup() {
        mockUserDAO = mock(UserDAO.class);
        mockAuthManager = mock(AuthManager.class);
        mockAlert = mock(AlertHelper.class);

        service = new userService(mockUserDAO, mockAuthManager, mockAlert);
    }

    /**
     * Tests that a user can log in successfully with a valid email and password.
     * This simulates a secure login process by checking the email format,
     * hashing the password, and verifying credentials against the database.
     * @throws Exception thrown if authentication fails unexpectedly
     */
    @Test	
    void login_shouldSucceed_withValidEmailAndPassword() throws Exception {
        user dummyUser = new user();
        dummyUser.setUserID(1);
        dummyUser.setEmail(testEmail);
        dummyUser.setPassword(hashedPassword);
        dummyUser.setRole("User");

        when(mockAuthManager.authenticateUser(testEmail, testPassword)).thenReturn(true);
        when(mockAuthManager.getAuthenticatedUser()).thenReturn(dummyUser);

        boolean isAuthenticated = service.authenticateUser(testEmail, testPassword);

        assertTrue(isAuthenticated, "Login should succeed with correct email and password");
        assertEquals(1, service.getCurrentUserID(), "User ID should be set after successful login");
        assertEquals("User", service.getCurrentUserRole(), "Role should match expected user role");
    }

    /**
     * Tests that login fails when the password is incorrect.
     * 
     * @throws Exception thrown if authentication fails unexpectedly
     */
    @Test
    void login_shouldFail_withIncorrectPassword() throws Exception {
        when(mockAuthManager.authenticateUser(testEmail, "WrongPassword")).thenReturn(false);

        boolean isAuthenticated = service.authenticateUser(testEmail, "WrongPassword");

        assertFalse(isAuthenticated, "Login should fail with incorrect password");
        assertEquals(-1, service.getCurrentUserID(), "User ID should remain unset after failed login");
    }

    /**
     * Test login failure with invalid email format
     * Confirms that the application rejects non-email inputs
     */
    @Test
    void login_shouldFail_withInvalidEmailFormat() {
        boolean isAuthenticated = service.authenticateUser("invalid-email", testPassword);
        assertFalse(isAuthenticated, "Login should fail with invalid email format");
    }

    /**
     * Test login failure with empty or null credentials.
     * Ensures application validates inputs before attempting authentication.
     */
    @Test
    void login_shouldFail_withEmptyCredentials() {
        boolean result1 = service.authenticateUser("", "");
        boolean result2 = service.authenticateUser(null, null);
        assertFalse(result1, "Login should fail with empty credentials");
        assertFalse(result2, "Login should fail with null credentials");
    }
}
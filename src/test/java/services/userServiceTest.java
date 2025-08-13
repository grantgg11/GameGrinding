package services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import database.UserDAO;
import models.user;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import utils.AlertHelper;
import security.AuthManager;
import security.PasswordHasher;
import services.userService.PasswordUpdateResult;

 
/**
 * userServiceTest contains unit tests for verifying the functionality of the
 * userService class in the GameGrinding application.
 *
 * This test class ensures that:
 * - User registration (registerUser) enforces unique emails and usernames, validates email format,
 *   applies password strength rules, and prevents invalid data from reaching the UserDAO.
 * - Authentication (authenticateUser) correctly validates credentials, updates the current user session,
 *   and handles null, empty, or invalid input without crashing.
 * - Role retrieval (getCurrentUserRole) correctly returns the user’s role from memory or database,
 *   defaults to "guest" for invalid sessions, and supports admin/user role access control logic.
 * - Validation helpers (isPasswordStrong, isEmailValid) accurately enforce password complexity
 *   and proper email formatting, rejecting weak or improperly formatted inputs.
 * - Account updates (updateAccount) validate email uniqueness and format, perform database updates
 *   through UserDAO, and gracefully handle failures or exceptions.
 * - Password management methods:
 *   • updatePassword enforces verification of the current password, validates the new password,
 *     and persists the updated password hash to the database.
 *   • updateForgottenPassword securely hashes new passwords before storage, verifies hashes match
 *     the original input, and handles DAO failures or exceptions.
 * - User retrieval methods (getUserByID, getUserByEmail) return correct data when available,
 *   and handle missing or invalid inputs by returning null without throwing exceptions.
 * - Security question verification (verifySecurityAnswers) validates hashed answers against stored values,
 *   returns true only for full matches, and handles user-not-found or exception scenarios safely.
 *
 * The tests map to multiple functional and non-functional requirements from the test plan, including:
 * - TC-1 (Login)
 * - TC-2 (Register)
 * - TC-14 (Edit Profile)
 * - TC-20 (Error Handling)
 * - TC-26 (Invalid Login Attempt)
 * - TC-27 (Password Strength Enforcement / Security – Password Storage)
 * - TC-30 (Admin Role Access Restriction)
 *
 * Mockito is used to mock dependencies such as UserDAO, AuthManager, and AlertHelper, isolating
 * the service logic from database and UI components. PasswordHasher is used to verify secure
 * password storage and validation. The goal is to confirm that userService reliably enforces
 * security, validation, and business rules for user account management while handling errors gracefully.
 */
public class userServiceTest {

    private userService userServiceUnderTest;
    private UserDAO mockUserDAO;
    private AlertHelper mockAlertHelper;
    private AuthManager mockAuthManager;

    @BeforeEach
    public void setUp() {

        mockUserDAO = mock(UserDAO.class);
        mockAlertHelper = mock(AlertHelper.class);
        mockAuthManager = mock(AuthManager.class);

        doNothing().when(mockAlertHelper).showError(anyString(), anyString(), anyString());
        doNothing().when(mockAlertHelper).showInfo(anyString(), anyString(), anyString());
        
        userServiceUnderTest = new userService(mockUserDAO, mockAuthManager, mockAlertHelper);
        userService.setInstance(userServiceUnderTest);
    }
    
    ///////////////////////////// testing registerUser() /////////////////////////////
    
    /**
     * Verifies that a new user can register successfully when all fields are valid and email is unique. 
     * Supports TC-2 (Register).
     */
    @Test
    public void testRegisterUserSuccessful() {
        String username = "uniqueUsername123";
        String email = "test123@example.com";
        String password = "StrongPassword1!";
        String securityQuestion1 = "Answer1";
        String securityQuestion2 = "Answer2";
        String securityQuestion3 = "Answer3";
        when(mockUserDAO.getUserByEmail(email)).thenReturn(null);
        when(mockUserDAO.insertUser(any(user.class))).thenReturn(1);
        boolean result = userServiceUnderTest.registerUser(username, email, password,
                securityQuestion1, securityQuestion2, securityQuestion3);
        assertTrue(result, "Registration should succeed when the email is new and all fields are valid.");
    }
    
    /**
	 * Verifies that registration fails when the email already exists in the database.
	 * Supports TC-2 (Register).
	 */
    @Test
    public void testRegisterUser_EmailAlreadyExists() {
        String username = "duplicateUsername";
        String email = "existing@example.com";
        String password = "StrongPassword1!";
        String securityQuestion1 = "Answer1";
        String securityQuestion2 = "Answer2";
        String securityQuestion3 = "Answer3";
        when(mockUserDAO.getUserByEmail(email)).thenReturn(new user());
        when(mockUserDAO.insertUser(any(user.class))).thenReturn(-1);
        boolean result = userServiceUnderTest.registerUser(username, email, password,
                securityQuestion1, securityQuestion2, securityQuestion3);
        assertFalse(result, "Registration should fail when email already exists.");
    }
    
    /**
	 * Verifies that registration fails when the email format is invalid.
	 * Supports TC-2 (Register).
	 */
    @Test
    public void testRegisterUser_InvalidEmail() {
		String username = "testUser";
		String email = "invalid-email";
		String password = "StrongPassword1!";
		String securityQuestion1 = "Answer1";
		String securityQuestion2 = "Answer2";
		String securityQuestion3 = "Answer3";
		when(mockUserDAO.getUserByEmail(email)).thenReturn(null);
		when(mockUserDAO.insertUser(any(user.class))).thenReturn(1);
		boolean result = userServiceUnderTest.registerUser(username, email, password,
				securityQuestion1, securityQuestion2, securityQuestion3);
		assertFalse(result, "Registration should fail with invalid email format.");
	}
    
    /**
     * Verifies that registration fails when the password is weak.
     * Supports TC-27 (Password Strength Enforcement).
     */
    @Test
    public void testRegisterUser_WeakPassword() {
		String username = "weakPasswordUser";
		String email = "mytest@testemail.com";
		String password = "weak";
		String securityQuestion1 = "Answer1";
		String securityQuestion2 = "Answer2";
		String securityQuestion3 = "Answer3";
		when(mockUserDAO.getUserByEmail(email)).thenReturn(null);
		when(mockUserDAO.insertUser(any(user.class))).thenReturn(1);
		boolean result = userServiceUnderTest.registerUser(username, email, password,
				securityQuestion1, securityQuestion2, securityQuestion3);
		assertFalse(result, "Registration should fail with invalid email format.");
    }
    
    /**
	 * Verifies that registration fails when the username already exists in the database.
	 * Supports TC-2 (Register).
	 */
    @Test
    public void testRegisterUser_UsernameAlreadyExists() {
    	String username = "existingUsername";
		String email = "usernameExists@test.com";
		String password = "StrongPassword1!";
		String securityQuestion1 = "Answer1";
		String securityQuestion2 = "Answer2";
		String securityQuestion3 = "Answer3";
		when(mockUserDAO.getUserByEmail(email)).thenReturn(null);
		when(mockUserDAO.insertUser(any(user.class))).thenReturn(-1);		
		boolean result = userServiceUnderTest.registerUser(username, email, password,
				securityQuestion1, securityQuestion2, securityQuestion3);
	    assertFalse(result, "Registration should fail when username already exists in the database.");
    }
    
    ///////////////////////////// testing authenticateUser() /////////////////////////////
    
    /**
	 * Verifies that a user can authenticate successfully with valid credentials.
	 * Supports TC-1 (Login).
	 */
    @Test
    public void testAuthenticateUser_NullEmail() {
        boolean result = userServiceUnderTest.authenticateUser(null, "password123");
        assertFalse(result, "Authentication should fail when email is null.");
    }
    
    /*
	 * Verifies that a user cannot authenticate with an empty email.
	 * Supports TC-1 (Login).
	 */
    @Test
    public void testAuthenticateUser_EmptyEmail() {
        boolean result = userServiceUnderTest.authenticateUser("", "password123");
        assertFalse(result, "Authentication should fail when email is empty.");
    }
    
    /**
     * Verifies that a user cannot login with an invalid email and password.
     * Supports TC-1 (Login).
     */
    @Test
    public void testAuthenticateUser_InvalidCredentials() {
        when(mockAuthManager.authenticateUser("test@example.com", "wrongpassword")).thenReturn(false);
        boolean result = userServiceUnderTest.authenticateUser("test@example.com", "wrongpassword");
        assertFalse(result, "Authentication should fail with invalid credentials.");
    }
    
    /**
     * Simulates a successful authentication flow where credentials are valid, but no user object is returned.
     * Makes sure the system still handles edge cases gracefully.
     * Supports TC-1 (Login).
     */
    @Test
    public void testAuthenticateUser_SuccessButInvalidUser() {
        when(mockAuthManager.authenticateUser("test@example.com", "password123")).thenReturn(true);

        when(mockAuthManager.getAuthenticatedUser()).thenReturn(null);

        boolean result = userServiceUnderTest.authenticateUser("test@example.com", "password123");
        assertTrue(result, "Authentication should succeed even if user object is invalid (null).");
    }
    
    /**
     * Verifies that a user can authenticate successfully with valid credentials and a valid user object.
     * Supports TC-1 (Login).
     */
    @Test
    public void testAuthenticateUser_SuccessValidUser() {
        when(mockAuthManager.authenticateUser("test@example.com", "password123")).thenReturn(true);
        user fakeUser = new user();
        fakeUser.setUserID(42);
        fakeUser.setRole("admin");
        when(mockAuthManager.getAuthenticatedUser()).thenReturn(fakeUser);
        boolean result = userServiceUnderTest.authenticateUser("test@example.com", "password123");
        assertTrue(result, "Authentication should succeed when credentials and user object are valid.");
        assertEquals(42, userServiceUnderTest.getCurrentUserID(), "User ID should be set after successful authentication.");
    }
    
    //////////////////////////// testing getCurrentUserRole() /////////////////////////////
    
    /**
     * Returns the user's role if currentUser is already set in memory.
     * Test confirms internal role-checking logic.
     * Supports TC-30 (Admin Role Access Restriction).
     */
    @Test
    public void testGetCurrentUserRole_CurrentUserSet() {
        user fakeUser = new user();
        fakeUser.setUserID(1);
        fakeUser.setRole("admin");
        userServiceUnderTest.setCurrentUser(fakeUser);
        String role = userServiceUnderTest.getCurrentUserRole();
        assertEquals("admin", role, "Should return the role from currentUser if it is already set.");
    }

    /**
	 * Returns the user's role by fetching from the database if currentUser is not set.
	 * Supports TC-30 (Admin Role Access Restriction).
	 */
    @Test
    public void testGetCurrentUserRole_FetchFromDatabase() {
        user fetchedUser = new user();
        fetchedUser.setUserID(2);
        fetchedUser.setRole("user");
        userServiceUnderTest.setCurrentUser(null);
        userServiceUnderTest.setCurrentUserID(2);
        when(mockUserDAO.getUserByID(2)).thenReturn(fetchedUser);
        String role = userServiceUnderTest.getCurrentUserRole();
        assertEquals("user", role, "Should fetch user from DB and return role if currentUser is null.");
    }

    /**
	 * Verifies that the fallback role of 'guest' is returned when no valid user is set or retrieved.
	 * Supports TC-30 (Admin Role Access Restriction).
	 */
    @Test
    public void testGetCurrentUserRole_GuestFallback() {
        userServiceUnderTest.setCurrentUser(null);
        userServiceUnderTest.setCurrentUserID(-1); // invalid ID
        when(mockUserDAO.getUserByID(anyInt())).thenReturn(null); // no user found
        String role = userServiceUnderTest.getCurrentUserRole();
        assertEquals("guest", role, "Should return 'guest' when no user is logged in or found.");
    }
    //////////////////////////// testing isPasswordStrong() /////////////////////////////
    
    /**
     * Confirms that a password that meets all strength requirements passes validation.
     * Supports TC-27 (Password Strength Enforcement).
     */
    @Test
    public void testIsPasswordStrong_ValidPassword() {
        String strongPassword = "Strong1!";
        assertTrue(userServiceUnderTest.isPasswordStrong(strongPassword),
            "Password should be considered strong.");
    }
    
    /**
	 * Confirms that a password that is too short fails validation.
	 * Supports TC-27 (Password Strength Enforcement).
	 */
    @Test
    public void testIsPasswordStrong_TooShort() {
        String shortPassword = "S1!";
        assertFalse(userServiceUnderTest.isPasswordStrong(shortPassword),
            "Password that is too short should be considered weak.");
    }
    
    /**
     * Tests that a password without a special character fails validation.
     * Supports TC-27 (Password Strength Enforcement).
     */
    @Test
    public void testIsPasswordStrong_MissingSpecialCharacter() {
        String noSpecialCharPassword = "StrongPassword1";
        assertFalse(userServiceUnderTest.isPasswordStrong(noSpecialCharPassword),
            "Password missing special character should be considered weak.");
    }
    
    /**
	 * Tests that a password without a number fails validation.
	 * Supports TC-27 (Password Strength Enforcement).
	 */
    @Test
    public void testIsPasswordStrong_MissingNumber() {
        String noNumberPassword = "StrongPassword!";
        assertFalse(userServiceUnderTest.isPasswordStrong(noNumberPassword),
            "Password missing number should be considered weak.");
    }
    
    /**
     * Tests that a password without an uppercase letter fails validation.
     * Supports TC-27 (Password Strength Enforcement).
     */
    @Test
    public void testIsPasswordStrong_MissingUppercase() {
        String noUppercasePassword = "weakpassword1!";
        assertFalse(userServiceUnderTest.isPasswordStrong(noUppercasePassword),
            "Password missing uppercase letter should be considered weak.");
    }
    
    /**
	 * Tests that a password without a lowercase letter fails validation.
	 * Supports TC-27 (Password Strength Enforcement).
	 */
    @Test
    public void testIsPasswordStrong_MissingLowercase() {
        String noLowercasePassword = "WEAKPASSWORD1!";
        assertFalse(userServiceUnderTest.isPasswordStrong(noLowercasePassword),
            "Password missing lowercase letter should be considered weak.");
    }
    ////////////////////////// testing isEmailValid() /////////////////////////////
    
    /**
	 * Verifies that a properly formatted email is considered valid.
	 * Supports TC-2 (Register) and TC-14 (Edit Profile).
	 */
    @Test
    public void testIsEmailValid_ValidEmail() {
        String validEmail = "test@example.com";
        assertTrue(userServiceUnderTest.isEmailValid(validEmail),
            "Properly formatted email should be considered valid.");
    }
    
    /**
     * Validates that a null email string fails the validation check. 
     * Supports TC-2 (Register) and TC-20 (Error Handling).
     */
    @Test
    public void testIsEmailValid_NullEmail() {
        assertFalse(userServiceUnderTest.isEmailValid(null),
            "Null email should be considered invalid.");
    }
    
    /**
	 * Validates that an empty email string fails the validation check. 
	 * Supports TC-2 (Register) and TC-20 (Error Handling).
	 */
    @Test
    public void testIsEmailValid_EmptyEmail() {
        assertFalse(userServiceUnderTest.isEmailValid(""),
            "Empty email should be considered invalid.");
    }
    
    /**
     * Tests for detection of email address missing a domain name. 
     * Supports TC-2 (Register) and TC-20 (Error Handling).
     */
    @Test
    public void testIsEmailValid_InvalidFormat_NoDomain() {
        String invalidEmail = "user@";
        assertFalse(userServiceUnderTest.isEmailValid(invalidEmail),
            "Email with missing domain should be invalid.");
    }
    
    /**
	 * Tests for detection of email address missing a username. 
	 * Supports TC-2 (Register) and TC-20 (Error Handling).
	 */
    @Test
    public void testIsEmailValid_InvalidFormat_NoUsername() {
        String invalidEmail = "@domain.com";
        assertFalse(userServiceUnderTest.isEmailValid(invalidEmail),
            "Email with missing username should be invalid.");
    }
    
    /**
     * Checks that emails without an @ symbol are invalid.
     * Supports TC-2 (Register) and TC-20 (Error Handling).
     */
    @Test
    public void testIsEmailValid_InvalidFormat_NoAtSymbol() {
        String invalidEmail = "userdomain.com";
        assertFalse(userServiceUnderTest.isEmailValid(invalidEmail),
            "Email missing '@' symbol should be invalid.");
    }
    
    /**
     * Checks that emails with multiple @ symbols are invalid.
     * Supports TC-2 (Register) and TC-20 (Error Handling).
     */
    @Test
    public void testIsEmailValid_InvalidFormat_ExtraAtSymbol() {
		String invalidEmail = "user@@domain.com";
		assertFalse(userServiceUnderTest.isEmailValid(invalidEmail),
			"Email with multiple '@' symbols should be invalid.");
	}
    
    /**
	 * Checks that emails with multiple '.' symbols are invalid.
	 * Supports TC-2 (Register) and TC-20 (Error Handling).
	 */
    @Test
    public void testIsEmailValid_InvalidFormat_ExtraDot() {
    	String invalidEmail = "user@domain..com";
    	assertFalse(userServiceUnderTest.isEmailValid(invalidEmail),
			"Email with multiple '.' symbols should be invalid.");
    }
    
    ////////////////////////// testing updateAccount() /////////////////////////////
    
    /**
     * Tests that account update fails if the new email already exists in the database. 
     * Validates duplicate-checking logic for profile updates.
     * Supports TC-14 (Edit Profile) and TC-20 (Error Handling).
	 *
     * @throws Exception Thrown if any error occurs during the test.
     */
    @Test
    public void testUpdateAccount_EmailAlreadyExists() throws Exception {
        String email = "existing@example.com";
        int userID = 1;
        String username = "newUsername";
        when(mockUserDAO.getUserByEmail(anyString())).thenReturn(new user()); 
        boolean result = userServiceUnderTest.updateAccount(userID, username, email);
        assertFalse(result, "Update should fail if email already exists.");
    }
    
    /**
	 * Tests that account update fails if the new email format is invalid. 
	 * Validates email format-checking logic for profile updates.
	 * Supports TC-14 (Edit Profile) and TC-20 (Error Handling).
	 */
    @Test
    public void testUpdateAccount_InvalidEmailFormat() throws Exception {
        String email = "invalidemail";
        int userID = 2;
        String username = "user2";
        when(mockUserDAO.getUserByEmail(anyString())).thenReturn(null); // email is unique
        boolean result = userServiceUnderTest.updateAccount(userID, username, email);
        assertFalse(result, "Update should fail if email format is invalid.");
    }
    
    /**
     * Verifies that a valid username and email result in a successful account update.
     * Validates the update logic for profile changes.
     * Supports TC-14 (Edit Profile).
     * 
     * @throws Exception Thrown if any error occurs during the test.
     */
    @Test
    public void testUpdateAccount_UpdateSuccess() throws Exception {
        String email = "valid@example.com";
        int userID = 3;
        String username = "user3";
        when(mockUserDAO.getUserByEmail(anyString())).thenReturn(null);
        when(mockUserDAO.updateUserAccount(eq(userID), eq(username), anyString())).thenReturn(true);
        boolean result = userServiceUnderTest.updateAccount(userID, username, email);
        assertTrue(result, "Update should succeed if everything is valid.");
    }
    
    /**
	 * Tests that account update fails if the database update operation fails.
	 * Validates error handling for database failures during profile updates.
	 * Supports TC-14 (Edit Profile) and TC-20 (Error Handling).
	 * 
	 * @throws Exception Thrown if any error occurs during the test.
	 */
    @Test
    public void testUpdateAccount_UpdateFailure() throws Exception {
        String email = "valid2@example.com";
        int userID = 4;
        String username = "user4";
        when(mockUserDAO.getUserByEmail(anyString())).thenReturn(null);
        when(mockUserDAO.updateUserAccount(eq(userID), eq(username), anyString())).thenReturn(false);
        boolean result = userServiceUnderTest.updateAccount(userID, username, email);
        assertFalse(result, "Update should fail if database update fails.");
    }
    
    /**
     * Confirms that an exception during account update is caught and the operation fails gracefully.
     * Supports TC-20 (Error Handling).
     * 
     * @throws Exception Thrown if any error occurs during the test.
     */
    @Test
    public void testUpdateAccount_ExceptionThrown() throws Exception {
        String email = "valid3@example.com";
        int userID = 5;
        String username = "user5";
        when(mockUserDAO.getUserByEmail(anyString())).thenThrow(new RuntimeException("DB error"));
        boolean result = userServiceUnderTest.updateAccount(userID, username, email);
        assertFalse(result, "Update should fail and catch exception if anything goes wrong.");
    }
    
    ////////////////////////// testing updatePassword() /////////////////////////////
    
    /**
	 * Tests that a weak new password fails validation.
	 * Validates password strength-checking logic for password updates.
	 */
    @Disabled("Password strength is now validated in the controller, not in userService.")
    @Test
    public void testUpdatePassword_WeakNewPassword() {
        String weakPassword = "abc";
        int userID = 1;
        String inputtedCurrentPassword = "currentPassword123!";
        PasswordUpdateResult result = userServiceUnderTest.updatePassword(userID, weakPassword, inputtedCurrentPassword);
        // Old: assertFalse(result)
        // New: this path is controller-level; no assertion here.
        assertNotNull(result);
    }

    /**
     * Makes sure password update fails if the current password input by the user is incorrect. 
     * Supports TC-20 (Error Handling).
     */
    @Test
    public void testUpdatePassword_IncorrectCurrentPassword() {
        String strongNewPassword = "StrongPassword1!";
        int userID = 2;
        String inputtedCurrentPassword = "wrongPassword";

        when(mockUserDAO.getUserPassword(userID)).thenReturn(PasswordHasher.hashPassword("correctPassword123!"));

        PasswordUpdateResult result = userServiceUnderTest.updatePassword(userID, strongNewPassword, inputtedCurrentPassword);

        assertEquals(PasswordUpdateResult.INCORRECT_CURRENT, result, "Update should fail if the inputted current password is incorrect.");
    }

    /**
	 * Verifies that a valid new password and correct current password result in a successful update.
	 * Validates the update logic for password changes.
	 */
    @Test
    public void testUpdatePassword_UpdateSuccess() {
        String strongNewPassword = "StrongPassword1!";
        int userID = 3;
        String inputtedCurrentPassword = "correctPassword123!";

        when(mockUserDAO.getUserPassword(userID)).thenReturn(PasswordHasher.hashPassword(inputtedCurrentPassword));
        when(mockUserDAO.updateUserPassword(eq(userID), anyString())).thenReturn(true);

        PasswordUpdateResult result = userServiceUnderTest.updatePassword(userID, strongNewPassword, inputtedCurrentPassword);

        assertEquals(PasswordUpdateResult.SUCCESS, result, "Password update should succeed when all conditions are met.");
    }

    /**
     * Verifies that the update fails if the DAO fails to save the new password. 
     * Supports TC-20 (Error Handling).
     */
    @Test
    public void testUpdatePassword_UpdateFailure() {
        String strongNewPassword = "StrongPassword1!";
        int userID = 4;
        String inputtedCurrentPassword = "correctPassword123!";

        when(mockUserDAO.getUserPassword(userID)).thenReturn(PasswordHasher.hashPassword(inputtedCurrentPassword));
        when(mockUserDAO.updateUserPassword(eq(userID), anyString())).thenReturn(false);

        PasswordUpdateResult result = userServiceUnderTest.updatePassword(userID, strongNewPassword, inputtedCurrentPassword);

        assertEquals(PasswordUpdateResult.UPDATE_FAILED, result, "Password update should fail if database update fails.");
    }

    /**
	 * Tests that an exception during password update is caught and the operation fails gracefully.
	 * Supports TC-20 (Error Handling).
	 */
    @Test
    public void testUpdatePassword_ExceptionThrown() {
        String strongNewPassword = "StrongPassword1!";
        int userID = 5;
        String inputtedCurrentPassword = "correctPassword123!";

        when(mockUserDAO.getUserPassword(userID)) .thenThrow(new RuntimeException("DB error"));
        PasswordUpdateResult result = userServiceUnderTest.updatePassword(userID, strongNewPassword, inputtedCurrentPassword);
        assertEquals(PasswordUpdateResult.ERROR, result, "Password update should return ERROR if anything goes wrong.");
    }
    
    
    
    ///////////////////////////////// testing updateForgottenPassword() /////////////////////////////////
    
    /**
     * Ensures a successful DAO update returns true and that the stored password is a hash
     * (not the plaintext). Also verifies the hash matches the new password using PasswordHasher.
     * Supports TC-20 (Error Handling) and TC-27 (Security – Password Storage).
     */
    @Test
    public void testUpdateForgottenPassword_Success_StoresHashedPassword() {
        int userID = 42;
        String newPassword = "NewStrongPass1!";
        when(mockUserDAO.updateUserPassword(eq(userID), org.mockito.ArgumentMatchers.anyString()))
            .thenReturn(true);
        boolean result = userServiceUnderTest.updateForgottenPassword(userID, newPassword);
        assertTrue(result, "Expected true when DAO updateUserPassword succeeds.");

        ArgumentCaptor<String> hashCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockUserDAO).updateUserPassword(eq(userID), hashCaptor.capture());
        String storedHash = hashCaptor.getValue();

        assertNotEquals(newPassword, storedHash, "Password must not be stored as plaintext.");

        assertTrue(PasswordHasher.verifyPassword(newPassword, storedHash), "Stored hash should verify against the provided new password."
        );
    }

    /**
     * Verifies updateForgottenPassword returns false when the DAO fails to update the password.
     * Supports TC-20 (Error Handling).
     */
    @Test
    public void testUpdateForgottenPassword_DaoFailure_ReturnsFalse() {
        int userID = 7;
        String newPassword = "AnotherStrong1!";
        when(mockUserDAO.updateUserPassword(eq(userID), org.mockito.ArgumentMatchers.anyString())).thenReturn(false);
        boolean result = userServiceUnderTest.updateForgottenPassword(userID, newPassword);
        assertFalse(result, "Expected false when DAO updateUserPassword returns false.");
    }

    /**
     * Ensures exceptions thrown by the DAO are caught and the method fails gracefully (returns false).
     * Supports TC-20 (Error Handling).
     */
    @Test
    public void testUpdateForgottenPassword_ExceptionThrown() {
        int userID = 99;
        String newPassword = "Catcher1!";
        when(mockUserDAO.updateUserPassword(eq(userID), org.mockito.ArgumentMatchers.anyString())).thenThrow(new RuntimeException("DB error"));

        boolean result = userServiceUnderTest.updateForgottenPassword(userID, newPassword);
        assertFalse(result, "Expected false when an exception occurs during password update.");
    }

    /**
     * Explicitly validates that hashing is performed before persistence by capturing the DAO argument,
     * confirming it differs from plaintext, and verifying with PasswordHasher.
     * Supports TC-27 (Security – Password Storage).
     */
    @Test
    public void testUpdateForgottenPassword_UsesHashingBeforePersist() {
        int userID = 11;
        String newPassword = "HashCheck1@";

        when(mockUserDAO.updateUserPassword(eq(userID), org.mockito.ArgumentMatchers.anyString())).thenReturn(true);

        boolean result = userServiceUnderTest.updateForgottenPassword(userID, newPassword);
        assertTrue(result, "Expected true when DAO update succeeds.");

        ArgumentCaptor<String> hashCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockUserDAO).updateUserPassword(eq(userID), hashCaptor.capture());
        String storedHash = hashCaptor.getValue();

        assertNotEquals(newPassword, storedHash, "Password must not be stored in plaintext.");
        assertTrue(PasswordHasher.verifyPassword(newPassword, storedHash), "Hashed password persisted should verify against the original new password."
        );
    }
    
    /////////////////////// testing getUserByID() ///////////////////////
    
    /**
     * Makes sure that getUserByID() returns the correct user object when the user exists.
     */
    @Test
    public void testGetUserByID_UserExists_ReturnsUserObject() {
        int userID = 10;
        user mockUser = new user();
        mockUser.setUserID(userID);
        mockUser.setUsername("testUser");
        when(mockUserDAO.getUserByID(userID)).thenReturn(mockUser);

        user result = userServiceUnderTest.getUserByID(userID);

        assertNotNull(result, "Expected non-null user when the user exists.");
        assertEquals(userID, result.getUserID(), "Returned user ID should match the requested ID.");
        assertEquals("testUser", result.getUsername(), "Returned username should match.");
    }

    /**
     * Ensures getUserByID() returns null when no user exists for the given ID.
     * Supports TC-20 (Error Handling).
     */
    @Test
    public void testGetUserByID_UserDoesNotExist_ReturnsNull() {
        int userID = 99;
        when(mockUserDAO.getUserByID(userID)).thenReturn(null);

        user result = userServiceUnderTest.getUserByID(userID);

        assertNull(result, "Expected null when the user does not exist.");
    }
    
    /////////////////////////// testing getUserByEmail() ///////////////////////////
    
    
    /**
     * Makes sure that getUserByEmail() returns the correct user object when the email exists in the database.
     */
    @Test
    public void testGetUserByEmail_EmailExists_ReturnsUserObject() {
        String email = "test@example.com";
        user mockUser = new user();
        mockUser.setUserID(1);
        mockUser.setUsername("testUser");
        mockUser.setEmail(email);
        when(mockUserDAO.getUserByEmail(email)).thenReturn(mockUser);

        user result = userServiceUnderTest.getUserByEmail(email);

        assertNotNull(result, "Expected non-null user when the email exists.");
        assertEquals(email, result.getEmail(), "Returned email should match the requested email.");
        assertEquals("testUser", result.getUsername(), "Returned username should match.");
    }

    /**
     * Makes sure that getUserByEmail() returns null when the email does not exist in the database.
     * TC-20 (Error Handling).
     */
    @Test
    public void testGetUserByEmail_EmailDoesNotExist_ReturnsNull() {
        String email = "missing@example.com";
        when(mockUserDAO.getUserByEmail(email)).thenReturn(null);

        user result = userServiceUnderTest.getUserByEmail(email);

        assertNull(result, "Expected null when the email does not exist.");
    }

    /**
     * Ensures getUserByEmail() returns null when the provided email is null or empty.
     * Supports TC-20 (Error Handling).
     */
    @Test
    public void testGetUserByEmail_NullOrEmptyEmail_ReturnsNull() {
        assertNull(userServiceUnderTest.getUserByEmail(null), "Expected null when email is null.");
        assertNull(userServiceUnderTest.getUserByEmail(""), "Expected null when email is empty.");
    }
 
    
    /////////////////////// testing verifySecurityAnswers() ///////////////////////
    
    /**
     * Ensures verifySecurityAnswers() returns true when all three security answers match the stored values.
     */
    @Test
    public void testVerifySecurityAnswers_AllAnswersMatch_ReturnsTrue() {
        int userID = 1;
        String answer1 = "petName";
        String answer2 = "motherMaiden";
        String answer3 = "favoriteColor";

        user mockUser = new user();
        mockUser.setSecurityAnswer1(PasswordHasher.hashPassword(answer1));
        mockUser.setSecurityAnswer2(PasswordHasher.hashPassword(answer2));
        mockUser.setSecurityAnswer3(PasswordHasher.hashPassword(answer3));

        when(mockUserDAO.getUserSecurityAnswers(userID)).thenReturn(mockUser);

        boolean result = userServiceUnderTest.verifySecurityAnswers(userID, answer1, answer2, answer3);

        assertTrue(result, "Expected true when all answers match.");
    }

    /**
     * Makes sure that verifySecurityAnswers() returns false when at least one security answer does not match.
     */
    @Test
    public void testVerifySecurityAnswers_OneOrMoreAnswersMismatch_ReturnsFalse() {
        int userID = 2;
        String answer1 = "petName";
        String answer2 = "wrongAnswer";
        String answer3 = "favoriteColor";

        user mockUser = new user();
        mockUser.setSecurityAnswer1(PasswordHasher.hashPassword(answer1));
        mockUser.setSecurityAnswer2(PasswordHasher.hashPassword("motherMaiden"));
        mockUser.setSecurityAnswer3(PasswordHasher.hashPassword(answer3));

        when(mockUserDAO.getUserSecurityAnswers(userID)).thenReturn(mockUser);

        boolean result = userServiceUnderTest.verifySecurityAnswers(userID, answer1, answer2, answer3);

        assertFalse(result, "Expected false when one or more answers are incorrect.");
    }

    /**
     * Checks that verifySecurityAnswers() returns false when the user is not found in the database.
     * Supports TC-20 (Error Handling).
	 */
    @Test
    public void testVerifySecurityAnswers_UserNotFound_ReturnsFalse() {
        int userID = 99;
        when(mockUserDAO.getUserSecurityAnswers(userID)).thenReturn(null);

        boolean result = userServiceUnderTest.verifySecurityAnswers(userID, "a1", "a2", "a3");

        assertFalse(result, "Expected false when the user is not found.");
    }

    /**
     * Ensures verifySecurityAnswers() returns false when an exception occurs during verification.
     * Supports TC-20 (Error Handling).
     */
    @Test
    public void testVerifySecurityAnswers_ExceptionThrown_ReturnsFalse() {
        int userID = 3;
        when(mockUserDAO.getUserSecurityAnswers(userID)).thenThrow(new RuntimeException("DB error"));

        boolean result = userServiceUnderTest.verifySecurityAnswers(userID, "a1", "a2", "a3");

        assertFalse(result, "Expected false when an exception is thrown.");
    }

    
}


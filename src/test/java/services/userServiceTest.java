package services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import database.UserDAO;
import models.user;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utils.AlertHelper;
import security.AuthManager;
import security.PasswordHasher;

 
/**
 * Test class for userService.
 * This suite validates user registration, authentication, role access, email and password validation,
 * account updates, and password management features. 
 * These tests cover requirements and use case includes TC-1 (Login), TC-2 (Register), TC-14 (Edit Profile),
 * TC-20 (Error Handling), TC-26 (Invalid Login Attempt), TC-27 (Password Strength Enforcement), TC-30 (Admin Role Access Restriction).
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
        when(mockUserDAO.getUserByEmail(anyString())).thenReturn(new user()); // email already exists
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
	 * Supports TC-27 (Password Strength Enforcement).
	 */
    @Test
    public void testUpdatePassword_WeakNewPassword() {
        String weakPassword = "abc";
        int userID = 1;
        String inputtedCurrentPassword = "currentPassword123!";
        boolean result = userServiceUnderTest.updatePassword(userID, weakPassword, inputtedCurrentPassword);
        assertFalse(result, "Update should fail if the new password is weak.");
    }

    /**
     * Makes sure password update fails if the current password input by the user is incorrect. 
     * Supports TC-20 (Error Handling) and TC-27 (Password Strength Enforcement).
     */
    @Test
    public void testUpdatePassword_IncorrectCurrentPassword() {
        String strongNewPassword = "StrongPassword1!";
        int userID = 2;
        String inputtedCurrentPassword = "wrongPassword";
        when(mockUserDAO.getUserPassword(userID)).thenReturn(PasswordHasher.hashPassword("correctPassword123!"));
        boolean result = userServiceUnderTest.updatePassword(userID, strongNewPassword, inputtedCurrentPassword);
        assertFalse(result, "Update should fail if the inputted current password is incorrect.");
    }

    /**
	 * Verifies that a valid new password and correct current password result in a successful update.
	 * Validates the update logic for password changes.
	 * Supports TC-27 (Password Strength Enforcement).
	 */
    @Test
    public void testUpdatePassword_UpdateSuccess() {
        String strongNewPassword = "StrongPassword1!";
        int userID = 3;
        String inputtedCurrentPassword = "correctPassword123!";
        when(mockUserDAO.getUserPassword(userID)).thenReturn(PasswordHasher.hashPassword(inputtedCurrentPassword));
        when(mockUserDAO.updateUserPassword(eq(userID), anyString())).thenReturn(true);
        boolean result = userServiceUnderTest.updatePassword(userID, strongNewPassword, inputtedCurrentPassword);
        assertTrue(result, "Password update should succeed when all conditions are met.");
    }

    /**
     * Verifies that the update fails if the DAO fails to save the new password. 
     * Supports TC-20 (Error Handling) and TC-27 (Password Strength Enforcement).
     */
    @Test
    public void testUpdatePassword_UpdateFailure() {
        String strongNewPassword = "StrongPassword1!";
        int userID = 4;
        String inputtedCurrentPassword = "correctPassword123!";
        when(mockUserDAO.getUserPassword(userID)).thenReturn(PasswordHasher.hashPassword(inputtedCurrentPassword));
        when(mockUserDAO.updateUserPassword(eq(userID), anyString())).thenReturn(false);
        boolean result = userServiceUnderTest.updatePassword(userID, strongNewPassword, inputtedCurrentPassword);
        assertFalse(result, "Password update should fail if database update fails.");
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
        when(mockUserDAO.getUserPassword(userID)).thenThrow(new RuntimeException("DB error"));
        boolean result = userServiceUnderTest.updatePassword(userID, strongNewPassword, inputtedCurrentPassword);
        assertFalse(result, "Password update should fail and catch exception if anything goes wrong.");
    }
}


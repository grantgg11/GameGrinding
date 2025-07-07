package database;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
 
import javax.crypto.SecretKey;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import models.user;
import security.Encryption;
import utils.KeyStorage;

/**
 * Unit tests for the UserDAO class using JUnit 5 and Mockito.
 *
 * This test suite validates the behavior of methods in the UserDAO class that
 * interact with the database to manage user-related operations, such as retrieving,
 * inserting, updating, and deleting user records. The tests cover normal scenarios,
 * empty results, and exception handling to ensure robust and secure behavior.
 *
 */
class UserDAOTest {

	private static UserDAO mockUserDAO;
	private static Connection sharedConnection;
	private PreparedStatement mockStatement;
	private static DatabaseManager dbManager;
	private ResultSet mockResultSet;
	private Connection mockConnection;
	private static SecretKey mockKey;

	
	private final String testEmail = "test@example.com";
    private final String encryptedEmail = "encrypted@example.com";
	
    /**
     * Initializes shared test resources before any test methods are executed.
     * This includes enabling test mode for the DatabaseManager, establishing a shared
     * test connection, mocking the encryption key, and instantiating the UserDAO with
     * the shared connection.
     *
     * @throws SQLException if there is an error initializing the database connection
     */
	@BeforeAll
	static void initConnection() throws SQLException{
		DatabaseManager.enableTestMode();
		dbManager = new DatabaseManager();
		sharedConnection = dbManager.getConnection();
		mockKey = mock(SecretKey.class);
		mockUserDAO = new UserDAO(sharedConnection);
	}
	
	/**
	 * Sets up mock dependencies before each test.
	 * A new mocked Connection, PreparedStatement, and ResultSet are created,
	 * and a spy UserDAO instance is initialized with the mock connection.
	 *
	 * @throws SQLException if there is an error setting up mocks
	 */
	@BeforeEach
	void setUp() throws SQLException{
		mockConnection = mock(Connection.class);
		mockStatement = mock(PreparedStatement.class);
		mockResultSet = mock(ResultSet.class);		
		mockUserDAO = spy(new UserDAO(mockConnection));
	}
	
	///////////////////////////////// testing getUserByEmail ///////////////////////////////////////
	
	/**
	 * Tests the successful retrieval of a user by email when the user exists.
	 * Verifies that encryption is applied to the email, and the result is correctly
	 * mapped from the ResultSet.
	 *
	 * @throws Exception if any mocking or SQL operation fails
	 */
    @Test
    void testGetUserByEmail_UserFound() throws Exception {
        try (
            MockedStatic<KeyStorage> mockedKeyStorage = mockStatic(KeyStorage.class);
            MockedStatic<Encryption> mockedEncryption = mockStatic(Encryption.class)
        ) {
            mockedKeyStorage.when(KeyStorage::getEncryptionKey).thenReturn(mockKey);
            mockedEncryption.when(() -> Encryption.encrypt(testEmail, mockKey)).thenReturn(encryptedEmail);

            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(true);

            when(mockResultSet.getInt("userID")).thenReturn(42);
            when(mockResultSet.getString("username")).thenReturn("testUser");
            when(mockResultSet.getString("email")).thenReturn(encryptedEmail);
            when(mockResultSet.getString("password")).thenReturn("hashedpass123");
            when(mockResultSet.getString("role")).thenReturn("admin");

            user result = mockUserDAO.getUserByEmail(testEmail);

            assertNotNull(result);
            assertEquals(42, result.getUserID());
            assertEquals("testUser", result.getUsername());
            assertEquals(encryptedEmail, result.getEmail());
            assertEquals("hashedpass123", result.getPassword());
            assertEquals("admin", result.getRole());

            verify(mockStatement).setString(1, encryptedEmail);
        }
    }

    /**
     * Tests the scenario where no user is found for the given email.
     * Ensures the method returns null when the ResultSet is empty.
     *
     * @throws Exception if any mocking or SQL operation fails
     */
    @Test
    void testGetUserByEmail_UserNotFound() throws Exception {
        try (
            MockedStatic<KeyStorage> mockedKeyStorage = mockStatic(KeyStorage.class);
            MockedStatic<Encryption> mockedEncryption = mockStatic(Encryption.class)
        ) {
            mockedKeyStorage.when(KeyStorage::getEncryptionKey).thenReturn(mockKey);
            mockedEncryption.when(() -> Encryption.encrypt(testEmail, mockKey)).thenReturn(encryptedEmail);

            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(false);

            user result = mockUserDAO.getUserByEmail(testEmail);

            assertNull(result);
        }
    }

    /**
     * Tests the scenario where an exception occurs during the email encryption process.
     * Ensures the method handles the encryption failure gracefully and returns null.
     */
    @Test
    void testGetUserByEmail_ThrowsExceptionOnEncryption() {
        try (MockedStatic<KeyStorage> mockedKeyStorage = mockStatic(KeyStorage.class)) {
            mockedKeyStorage.when(KeyStorage::getEncryptionKey).thenReturn(mockKey);

            try (MockedStatic<Encryption> mockedEncryption = mockStatic(Encryption.class)) {
                mockedEncryption.when(() -> Encryption.encrypt(anyString(), any())).thenThrow(new RuntimeException("Encryption failed"));

                user result = mockUserDAO.getUserByEmail(testEmail);
                assertNull(result);
            }
        }
    }

    /**
     * Tests the scenario where a SQLException is thrown during the database query.
     * Ensures the method handles SQL failures gracefully and returns null.
     *
     * @throws Exception if any mocking or SQL operation fails
     */
    @Test
    void testGetUserByEmail_SQLExceptionDuringQuery() throws Exception {
        try (
            MockedStatic<KeyStorage> mockedKeyStorage = mockStatic(KeyStorage.class);
            MockedStatic<Encryption> mockedEncryption = mockStatic(Encryption.class)
        ) {
            mockedKeyStorage.when(KeyStorage::getEncryptionKey).thenReturn(mockKey);
            mockedEncryption.when(() -> Encryption.encrypt(testEmail, mockKey)).thenReturn(encryptedEmail);

            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeQuery()).thenThrow(new SQLException("Query failed"));

            user result = mockUserDAO.getUserByEmail(testEmail);

            assertNull(result);
        }
    }
    
    
    //////////////////////////////////////// testing getUserPassword ///////////////////////////////////////
    
    /**
     * Tests successful retrieval of a user's password when the user exists.
     * Verifies that the password is correctly extracted from the ResultSet.
     *
     * @throws Exception if any SQL or mocking failure occurs
     */
    @Test
    void testGetUserPassword_UserFound() throws Exception {
		when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
		when(mockStatement.executeQuery()).thenReturn(mockResultSet);
		when(mockResultSet.next()).thenReturn(true);
		when(mockResultSet.getString("password")).thenReturn("hashedpass123");

		String result = mockUserDAO.getUserPassword(42);

		assertEquals("hashedpass123", result);
	}
    
    /**
     * Tests retrieval of a user's password when no user is found in the database.
     * Verifies that the method returns null and query navigation is executed correctly.
     *
     * @throws Exception if any SQL or mocking failure occurs
     */
    @Test
    void testGetUserPassword_UserNotFound() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false); 

        String result = mockUserDAO.getUserPassword(42);

        assertNull(result);
        verify(mockStatement).setInt(1, 42);
        verify(mockResultSet).next();
    }

    /**
     * Tests behavior when a SQL exception occurs during the preparation of the statement.
     * Ensures that the method fails gracefully and returns null.
     *
     * @throws Exception if SQL preparation fails
     */
    @Test
    void testGetUserPassword_SQLExceptionOnPrepareStatement() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Prepare failed"));

        String result = mockUserDAO.getUserPassword(42);

        assertNull(result);
        verify(mockConnection).prepareStatement(anyString());
    }

    /**
     * Tests behavior when a SQL exception occurs during execution of the query.
     * Ensures that the method handles the exception and returns null.
     *
     * @throws Exception if query execution fails
     */
    @Test
    void testGetUserPassword_SQLExceptionOnExecuteQuery() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenThrow(new SQLException("Execute failed"));

        String result = mockUserDAO.getUserPassword(42);

        assertNull(result);
        verify(mockStatement).setInt(1, 42);
        verify(mockStatement).executeQuery();
    }
    
    //////////////////////////////////////// testing getUserByID ///////////////////////////////////////
    
    /**
     * Tests successful retrieval of a user by their user ID.
     * Verifies that all expected user fields are mapped correctly from the ResultSet.
     *
     * @throws Exception if any SQL or mocking failure occurs
     */
    @Test
    void testGetUserByID_UserFound() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);

        when(mockResultSet.getInt("userID")).thenReturn(101);
        when(mockResultSet.getString("username")).thenReturn("user101");
        when(mockResultSet.getString("email")).thenReturn("user101@example.com");
        when(mockResultSet.getString("password")).thenReturn("hashedPassword");
        when(mockResultSet.getString("role")).thenReturn("user");

        user result = mockUserDAO.getUserByID(101);

        assertNotNull(result);
        assertEquals(101, result.getUserID());
        assertEquals("user101", result.getUsername());
        assertEquals("user101@example.com", result.getEmail());
        assertEquals("hashedPassword", result.getPassword());
        assertEquals("user", result.getRole());

        verify(mockStatement).setInt(1, 101);
        verify(mockResultSet).next();
    }

    /**
     * Tests the scenario where no user is found for the given user ID.
     * Verifies that the method returns null and proper statement/query flow is followed.
     *
     * @throws Exception if any SQL or mocking failure occurs
     */
    @Test
    void testGetUserByID_UserNotFound() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        user result = mockUserDAO.getUserByID(999);

        assertNull(result);
        verify(mockStatement).setInt(1, 999);
        verify(mockResultSet).next();
    }

    /**
     * Tests behavior when a SQLException occurs while preparing the statement.
     * Ensures the method returns null and handles the exception gracefully.
     *
     * @throws Exception if statement preparation fails
     */
    @Test
    void testGetUserByID_SQLExceptionOnPrepareStatement() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Prepare failed"));

        user result = mockUserDAO.getUserByID(123);

        assertNull(result);
        verify(mockConnection).prepareStatement(anyString());
    }

    /**
     * Tests behavior when a SQLException occurs during execution of the query.
     * Verifies the method handles the exception and returns null.
     *
     * @throws Exception if query execution fails
     */
    @Test
    void testGetUserByID_SQLExceptionOnExecuteQuery() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenThrow(new SQLException("Execute failed"));

        user result = mockUserDAO.getUserByID(456);

        assertNull(result);
        verify(mockStatement).setInt(1, 456);
        verify(mockStatement).executeQuery();
    }
    
////////////////////////////////////getUserSecurityAnswers //////////////////////////////////////////

    /**
     * Tests successful retrieval of a user's security answers by user ID.
     * Verifies that all expected fields are extracted from the ResultSet correctly.
     *
     * @throws Exception if any SQL or mocking failure occurs
     */
    @Test
    void testGetUserSecurityAnswers_UserFound() throws Exception {
    	when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
    	when(mockStatement.executeQuery()).thenReturn(mockResultSet);
    	when(mockResultSet.next()).thenReturn(true);

    	when(mockResultSet.getInt("userID")).thenReturn(7);
    	when(mockResultSet.getString("username")).thenReturn("securityUser");
    	when(mockResultSet.getString("email")).thenReturn("secure@example.com");
    	when(mockResultSet.getString("password")).thenReturn("hashed123");
    	when(mockResultSet.getString("securityAnswer1")).thenReturn("dog");
    	when(mockResultSet.getString("securityAnswer2")).thenReturn("blue");
    	when(mockResultSet.getString("securityAnswer3")).thenReturn("pizza");

    	user result = mockUserDAO.getUserSecurityAnswers(7);

    	assertNotNull(result);
    	assertEquals(7, result.getUserID());
    	assertEquals("securityUser", result.getUsername());
    	assertEquals("secure@example.com", result.getEmail());
    	assertEquals("hashed123", result.getPassword());
    	assertEquals("dog", result.getSecurityAnswer1());
    	assertEquals("blue", result.getSecurityAnswer2());
    	assertEquals("pizza", result.getSecurityAnswer3());

    	verify(mockStatement).setInt(1, 7);
    	verify(mockResultSet).next();
    }

    /**
     * Tests retrieval of user security answers when no matching user is found.
     * Ensures the method returns null and the flow proceeds correctly.
     *
     * @throws Exception if any SQL or mocking failure occurs
     */
    @Test
    void testGetUserSecurityAnswers_UserNotFound() throws Exception {
    	when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
    	when(mockStatement.executeQuery()).thenReturn(mockResultSet);
    	when(mockResultSet.next()).thenReturn(false);

    	user result = mockUserDAO.getUserSecurityAnswers(99);

    	assertNull(result);
    	verify(mockStatement).setInt(1, 99);
    	verify(mockResultSet).next();
    }

    /**
     * Tests behavior when a SQLException is thrown during the preparation
     * of the SQL statement for fetching security answers.
     * Verifies that the method returns null and handles the exception.
     *
     * @throws Exception if preparation fails
     */
    @Test
    void testGetUserSecurityAnswers_SQLExceptionOnPrepareStatement() throws Exception {
    	when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Prepare failed"));

    	user result = mockUserDAO.getUserSecurityAnswers(123);

    	assertNull(result);
    	verify(mockConnection).prepareStatement(anyString());
    }

    /**
     * Tests behavior when a SQLException is thrown during execution
     * of the SQL query for fetching user security answers.
     * Verifies that the method returns null and exception is handled.
     *
     * @throws Exception if query execution fails
     */
    @Test
    void testGetUserSecurityAnswers_SQLExceptionOnExecuteQuery() throws Exception {
    	when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
    	when(mockStatement.executeQuery()).thenThrow(new SQLException("Execute failed"));

    	user result = mockUserDAO.getUserSecurityAnswers(456);

    	assertNull(result);
    	verify(mockStatement).setInt(1, 456);
    	verify(mockStatement).executeQuery();
    }
 
    //////////////////////////////////////// testing deleteUserByEmail ///////////////////////////////////////
    
    /**
     * Tests successful deletion of a user by email.
     * Verifies that the correct email is passed and deletion result is true.
     *
     * @throws Exception if database interaction fails
     */
    @Test
    void testDeleteUserByEmail_UserDeletedSuccessfully() throws Exception {
        String email = "delete@example.com";

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeUpdate()).thenReturn(1); 

        boolean result = mockUserDAO.deleteUserByEmail(email);

        assertTrue(result);
        verify(mockStatement).setString(1, email);
        verify(mockStatement).executeUpdate();
    }

    /**
     * Tests the case where no user is deleted (e.g., email does not exist).
     * Verifies that the method returns false.
     *
     * @throws Exception if SQL operation fails
     */
    @Test
    void testDeleteUserByEmail_NoUserDeleted() throws Exception {
        String email = "missing@example.com";

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeUpdate()).thenReturn(0); 

        boolean result = mockUserDAO.deleteUserByEmail(email);

        assertFalse(result);
        verify(mockStatement).setString(1, email);
        verify(mockStatement).executeUpdate();
    }

    /**
     * Tests behavior when a SQLException occurs during statement preparation.
     * Verifies that the method returns false and handles the exception.
     *
     * @throws Exception if preparation fails
     */
    @Test
    void testDeleteUserByEmail_SQLExceptionOnPrepareStatement() throws Exception {
        String email = "failprepare@example.com";

        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Prepare failed"));

        boolean result = mockUserDAO.deleteUserByEmail(email);

        assertFalse(result);
        verify(mockConnection).prepareStatement(anyString());
    }

    /**
     * Tests behavior when a SQLException occurs during update execution.
     * Verifies the method returns false and exception is handled.
     *
     * @throws Exception if execution fails
     */
    @Test
    void testDeleteUserByEmail_SQLExceptionOnExecuteUpdate() throws Exception {
        String email = "failexecute@example.com";

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeUpdate()).thenThrow(new SQLException("Execute failed"));

        boolean result = mockUserDAO.deleteUserByEmail(email);

        assertFalse(result);
        verify(mockStatement).setString(1, email);
        verify(mockStatement).executeUpdate();
    }
    
    //////////////////////////////////////// testing insertUser ///////////////////////////////////////
    
    /**
     * Tests that a new user is successfully inserted and a generated user ID is returned.
     * Mocks static encryption methods and ensures all fields are correctly set in the insert statement.
     *
     * @throws Exception if any step fails during test execution
     */
    @Test
    void testInsertUser_SuccessfulInsert() throws Exception {
        user newUser = new user("newuser", "new@example.com", "hashedPass", "ans1", "ans2", "ans3");

        ResultSet mockGeneratedKeys = mock(ResultSet.class);

        try (
            MockedStatic<KeyStorage> mockedKeyStorage = mockStatic(KeyStorage.class);
            MockedStatic<Encryption> mockedEncryption = mockStatic(Encryption.class)
        ) {
            mockedKeyStorage.when(KeyStorage::getEncryptionKey).thenReturn(mockKey);
            mockedEncryption.when(() -> Encryption.encrypt(newUser.getEmail(), mockKey)).thenReturn(encryptedEmail);

            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeUpdate()).thenReturn(1);
            when(mockStatement.getGeneratedKeys()).thenReturn(mockGeneratedKeys);
            when(mockGeneratedKeys.next()).thenReturn(true);
            when(mockGeneratedKeys.getInt(1)).thenReturn(101);

            int result = mockUserDAO.insertUser(newUser);

            assertEquals(101, result);
            verify(mockStatement).setString(1, "newuser");
            verify(mockStatement).setString(2, encryptedEmail);
            verify(mockStatement).setString(3, "hashedPass");
            verify(mockStatement).setString(4, "ans1");
            verify(mockStatement).setString(5, "ans2");
            verify(mockStatement).setString(6, "ans3");
        }
    }

    /**
     * Tests that insertUser() returns -1 when the insert affects no rows.
     * Simulates failure of insertion due to constraints or internal logic.
     */
    @Test
    void testInsertUser_InsertFails_NoRowsAffected() throws Exception {
        user newUser = new user("user", "fail@example.com", "pass", "a1", "a2", "a3");

        try (
            MockedStatic<KeyStorage> mockedKeyStorage = mockStatic(KeyStorage.class);
            MockedStatic<Encryption> mockedEncryption = mockStatic(Encryption.class)
        ) {
            mockedKeyStorage.when(KeyStorage::getEncryptionKey).thenReturn(mockKey);
            mockedEncryption.when(() -> Encryption.encrypt(newUser.getEmail(), mockKey)).thenReturn(encryptedEmail);

            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeUpdate()).thenReturn(0); 

            int result = mockUserDAO.insertUser(newUser);

            assertEquals(-1, result);
        }
    }

    /**
     * Tests that insertUser() returns -1 when no generated key is retrieved after successful insert.
     * 
     * @throws Exception if any mocking or SQL operation fails
     */
    @Test
    void testInsertUser_InsertSucceedsButNoGeneratedKeys() throws Exception {
        user newUser = new user("user", "nogk@example.com", "pass", "a1", "a2", "a3");
        ResultSet mockGeneratedKeys = mock(ResultSet.class);

        try (
            MockedStatic<KeyStorage> mockedKeyStorage = mockStatic(KeyStorage.class);
            MockedStatic<Encryption> mockedEncryption = mockStatic(Encryption.class)
        ) {
            mockedKeyStorage.when(KeyStorage::getEncryptionKey).thenReturn(mockKey);
            mockedEncryption.when(() -> Encryption.encrypt(newUser.getEmail(), mockKey)).thenReturn(encryptedEmail);

            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeUpdate()).thenReturn(1);
            when(mockStatement.getGeneratedKeys()).thenReturn(mockGeneratedKeys);
            when(mockGeneratedKeys.next()).thenReturn(false); 

            int result = mockUserDAO.insertUser(newUser);

            assertEquals(-1, result);
        }
    }

    /**
     * Tests behavior when a SQLException is thrown during the executeUpdate call in insertUser().
     * Should return -1 indicating a failure to insert.
     * 
     * @throws Exception if any mocking or SQL operation fails
     */
    @Test
    void testInsertUser_SQLExceptionDuringExecute() throws Exception {
        user newUser = new user("sqlfail", "fail@example.com", "pass", "a1", "a2", "a3");

        try (
            MockedStatic<KeyStorage> mockedKeyStorage = mockStatic(KeyStorage.class);
            MockedStatic<Encryption> mockedEncryption = mockStatic(Encryption.class)
        ) {
            mockedKeyStorage.when(KeyStorage::getEncryptionKey).thenReturn(mockKey);
            mockedEncryption.when(() -> Encryption.encrypt(newUser.getEmail(), mockKey)).thenReturn(encryptedEmail);

            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeUpdate()).thenThrow(new SQLException("Insert failed"));

            int result = mockUserDAO.insertUser(newUser);

            assertEquals(-1, result);
        }
    }

    /**
     * Tests behavior when an exception is thrown during encryption before the insert.
     * Ensures graceful fallback returning -1.
     * 
     * @throws Exception if any mocking or SQL operation fails
     */
    @Test
    void testInsertUser_ExceptionDuringEncryption() throws Exception {
        user newUser = new user("cryptofail", "failcrypt@example.com", "pass", "a1", "a2", "a3");

        try (
            MockedStatic<KeyStorage> mockedKeyStorage = mockStatic(KeyStorage.class);
            MockedStatic<Encryption> mockedEncryption = mockStatic(Encryption.class)
        ) {
            mockedKeyStorage.when(KeyStorage::getEncryptionKey).thenReturn(mockKey);
            mockedEncryption.when(() -> Encryption.encrypt(anyString(), any())).thenThrow(new RuntimeException("Encrypt fail"));

            int result = mockUserDAO.insertUser(newUser);

            assertEquals(-1, result);
        }
    }
    
    //////////////////////////////////////// testing updateUserAccount ///////////////////////////////////////
    
    /**
     * Tests successful update of a user's username and email by ID.
     * Verifies correct values are passed and update returns true.
     *
     * @throws Exception if any part of the update process fails
     */
    @Test
    void testUpdateUserAccount_SuccessfulUpdate() throws Exception {
        int userID = 1;
        String newUsername = "updatedUser";
        String newEmail = "updated@example.com";

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeUpdate()).thenReturn(1); 

        boolean result = mockUserDAO.updateUserAccount(userID, newUsername, newEmail);

        assertTrue(result);
        verify(mockStatement).setString(1, newUsername);
        verify(mockStatement).setString(2, newEmail);
        verify(mockStatement).setInt(3, userID);
        verify(mockStatement).executeUpdate();
    }

    /**
     * Tests that updateUserAccount() returns false when no rows are updated (e.g., invalid user ID).
     *
     * @throws Exception if statement preparation or execution fails
     */
    @Test
    void testUpdateUserAccount_NoRowsUpdate() throws Exception {
        int userID = 2;
        String newUsername = "noChange";
        String newEmail = "nochange@example.com";

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeUpdate()).thenReturn(0); 

        boolean result = mockUserDAO.updateUserAccount(userID, newUsername, newEmail);

        assertFalse(result);
        verify(mockStatement).setString(1, newUsername);
        verify(mockStatement).setString(2, newEmail);
        verify(mockStatement).setInt(3, userID);
        verify(mockStatement).executeUpdate();
    }

    /**
     * Tests that updateUserAccount() returns false when a SQLException is thrown
     * during the prepareStatement phase.
     *
     * @throws Exception if mocking fails or the DAO call throws an unexpected exception
     */
    @Test
    void testUpdateUserAccount_SQLExceptionOnPrepareStatement() throws Exception {
        int userID = 3;
        String newUsername = "failPrep";
        String newEmail = "failprep@example.com";

        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Prepare failed"));

        boolean result = mockUserDAO.updateUserAccount(userID, newUsername, newEmail);

        assertFalse(result);
        verify(mockConnection).prepareStatement(anyString());
    }

    /**
     * Tests that updateUserAccount() returns false when a SQLException is thrown
     * during the executeUpdate phase.
     *
     * @throws Exception if mocking fails or the DAO call throws an unexpected exception
     */
    @Test
    void testUpdateUserAccount_SQLExceptionOnExecuteUpdate() throws Exception {
        int userID = 4;
        String newUsername = "failExec";
        String newEmail = "failexec@example.com";

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeUpdate()).thenThrow(new SQLException("Execute failed"));

        boolean result = mockUserDAO.updateUserAccount(userID, newUsername, newEmail);

        assertFalse(result);
        verify(mockStatement).setString(1, newUsername);
        verify(mockStatement).setString(2, newEmail);
        verify(mockStatement).setInt(3, userID);
        verify(mockStatement).executeUpdate();
    }
    
    //////////////////////////////////////// testing updateUserPassword ///////////////////////////////////////
    
    /**
     * Tests that updateUserPassword() successfully updates the password and returns true
     * when one row is affected.
     *
     * @throws Exception if mocking fails or the DAO call throws an unexpected exception
     */
    @Test
    void testUpdateUserPassword_SuccessfulUpdate() throws Exception {
        int userID = 10;
        String newPassword = "secureHashedPassword";

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeUpdate()).thenReturn(1);

        boolean result = mockUserDAO.updateUserPassword(userID, newPassword);

        assertTrue(result);
        verify(mockStatement).setString(1, newPassword);
        verify(mockStatement).setInt(2, userID);
        verify(mockStatement).executeUpdate();
    }

    /**
     * Tests that updateUserPassword() returns false when no rows are affected.
     *
     * @throws Exception if mocking fails or the DAO call throws an unexpected exception
     */
    @Test
    void testUpdateUserPassword_NoRowsUpdated() throws Exception {
        int userID = 20;
        String newPassword = "unchangedPassword";

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeUpdate()).thenReturn(0);

        boolean result = mockUserDAO.updateUserPassword(userID, newPassword);

        assertFalse(result);
        verify(mockStatement).setString(1, newPassword);
        verify(mockStatement).setInt(2, userID);
        verify(mockStatement).executeUpdate();
    }

    /**
     * Tests that updateUserPassword() returns false if a SQLException occurs during
     * prepareStatement.
     *
     * @throws Exception if mocking fails or the DAO call throws an unexpected exception
     */
    @Test
    void testUpdateUserPassword_SQLExceptionOnPrepareStatement() throws Exception {
        int userID = 30;
        String newPassword = "failPreparePassword";

        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Prepare failed"));

        boolean result = mockUserDAO.updateUserPassword(userID, newPassword);

        assertFalse(result);
        verify(mockConnection).prepareStatement(anyString());
    }

    /**
     * Tests that updateUserPassword() returns false if a SQLException occurs during
     * executeUpdate.
     *
     * @throws Exception if mocking fails or the DAO call throws an unexpected exception
     */
    @Test
    void testUpdateUserPassword_SQLExceptionOnExecuteUpdate() throws Exception {
        int userID = 40;
        String newPassword = "failExecutePassword";

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeUpdate()).thenThrow(new SQLException("Execute failed"));

        boolean result = mockUserDAO.updateUserPassword(userID, newPassword);

        assertFalse(result);
        verify(mockStatement).setString(1, newPassword);
        verify(mockStatement).setInt(2, userID);
        verify(mockStatement).executeUpdate();
    }
}

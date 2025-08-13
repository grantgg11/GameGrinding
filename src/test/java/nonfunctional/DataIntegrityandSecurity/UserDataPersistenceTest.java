package nonfunctional.DataIntegrityandSecurity;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import models.user;
import database.UserDAO;
import org.junit.jupiter.api.*;

/**
 * UserDataPersistenceTest verifies non-functional requirement US-6: data persists across sessions.
 *
 * It uses an in-memory SQLite DB initialized once (@BeforeAll) and ordered tests to simulate
 * app close/reopen by creating two UserDAO “sessions”:
 * - testInsertUserInFirstSession (Order 1): inserts a user and records the returned ID.
 * - testRetrieveUserInNewSession (Order 2): creates a new DAO, retrieves by ID, and asserts
 *   username, password, and default role are intact.
 *
 * The suite confirms schema setup, cross-session persistence via a shared Connection, and
 * proper teardown of resources (@AfterAll).
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserDataPersistenceTest {

    private static Connection sharedConnection;
    private static int persistedUserID;

    /**
	 * Sets up an in-memory SQLite database for testing.
	 * Creates a User table with necessary fields.
	 * This simulates the application's database environment.
	 */
    @BeforeAll
    static void setupDatabase() throws Exception {
    	sharedConnection = DriverManager.getConnection("jdbc:sqlite::memory:");
        Statement stmt = sharedConnection.createStatement();
        stmt.execute("""
            CREATE TABLE User (
        		UserID INTEGER PRIMARY KEY AUTOINCREMENT,
        		Username TEXT NOT NULL UNIQUE,
        		Password TEXT NOT NULL,
        		Email TEXT NOT NULL UNIQUE,
        		Role TEXT CHECK(Role IN ('User', 'Admin')) NOT NULL DEFAULT 'User',
        		securityAnswer1 TEXT,
        		securityAnswer2 TEXT,
        		securityAnswer3 TEXT
        		);
        """);
        stmt.close();
    }

    /**
	 * Resets the persistedUserID before each test to ensure isolation.
	 * This prevents data from previous tests affecting the current test.
	 */
    @Test
    @Order(1)
    void testInsertUserInFirstSession() {
        UserDAO daoSession1 = new UserDAO(sharedConnection);

        user newUser = new user(
            0,
            "persistUser",
            "persist@example.com",
            "persistedPassword"
        );
        newUser.setSecurityAnswer1("red");
        newUser.setSecurityAnswer2("dog");
        newUser.setSecurityAnswer3("pizza");

        persistedUserID = daoSession1.insertUser(newUser);
        assertTrue(persistedUserID > 0, "User should be inserted and ID returned");
    }

    /**
     * Tests that the user data persists across sessions.
     * This simulates closing and reopening the app by using a new DAO instance.
     */
    @Test
    @Order(2)
    void testRetrieveUserInNewSession() {
        UserDAO daoSession2 = new UserDAO(sharedConnection);
        user retrieved = daoSession2.getUserByID(persistedUserID);

        assertNotNull(retrieved, "User data should persist and be retrievable in a new DAO session");
        assertEquals("persistUser", retrieved.getUsername());
        assertEquals("persistedPassword", retrieved.getPassword());
        assertEquals("User", retrieved.getRole());  

    }

    /**
     * Cleans up the database connection after all tests are done.
     * @throws Exception throws if there is an error closing the connection
     */
    @AfterAll
    static void tearDown() throws Exception {
        if (sharedConnection != null) sharedConnection.close();
    }
}

package database;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import models.APIRequestLog;
import models.DatabaseIntegrityReport; 
import models.SystemPerformanceLog;

/**
 * Unit test class for ReportDAO, using JUnit 5 and Mockito to validate all reporting-related database operations.
 *
 * This class covers insertion and retrieval operations for:
 * SystemPerformanceLog
 * APIRequestLog
 * DatabaseIntegrityReport
 *
 * It also verifies database aggregation methods such as counting total entries,
 * checking foreign key integrity, and identifying data anomalies like missing emails
 * or orphaned records.
 */
class ReportDAOTest {

	private static ReportDAO mockReportDAO;
	private static Connection sharedConnection;
	private PreparedStatement mockStatement;
	private static DatabaseManager dbManager;
	private Connection mockConnection;
	private ResultSet mockResultSet;
	
	/**
	 * Initializes the shared test database connection once before all tests.
	 * Enables test mode in DatabaseManager and prepares a shared ReportDAO instance.
	 *
	 * @throws SQLException if a database access error occurs
	 */
	@BeforeAll
	static void initConnection() throws SQLException{
		DatabaseManager.enableTestMode();
		dbManager = new DatabaseManager();
		sharedConnection = dbManager.getConnection();
		mockReportDAO = new ReportDAO(sharedConnection);
	}
	
	/**
	 * Sets up a fresh set of mocks before each test.
	 * Mocks the Connection, PreparedStatement, and ResultSet,
	 * and reinitializes the ReportDAO with the mocked connection.
	 *
	 * @throws SQLException if an error occurs during setup
	 */
	@BeforeEach
	void setUp() throws SQLException{
		mockConnection = mock(Connection.class);
		mockStatement = mock(PreparedStatement.class);
		mockResultSet = mock(ResultSet.class);
		
		mockReportDAO = new ReportDAO(mockConnection);
	}
	
	
	///////////////////////////// testing insertSystemPerformanceLog /////////////////////////////
	
	/**
	 * Tests successful insertion of a SystemPerformanceLog.
	 * Simulates a valid database operation where one row is inserted.
	 * Verifies that all prepared statement parameters are set correctly
	 * and that the method returns true.
	 *
	 * @throws Exception if any unexpected error occurs
	 */
	@Test
	void testInsertSystemPerformanceLog_SuccessfulInsert() throws Exception {
	    SystemPerformanceLog log = new SystemPerformanceLog(
	        200,    // screenLoadTime
	        120,    // dbQueryTime
	        300_000, // memoryUsage
	        3,      // gcEvents
	        "None"  // exceptionsLogged
	    );

	    when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
	    when(mockStatement.executeUpdate()).thenReturn(1); 

	    boolean result = mockReportDAO.insertSystemPerformanceLog(log);

	    assertTrue(result);
	    verify(mockStatement).setString(1, log.getTimestamp().toString());
	    verify(mockStatement).setInt(2, 200);
	    verify(mockStatement).setInt(3, 120);
	    verify(mockStatement).setInt(4, 300_000);
	    verify(mockStatement).setInt(5, 3);
	    verify(mockStatement).setString(6, "None");
	    verify(mockStatement).executeUpdate();
	}

	/**
	 * Tests the scenario where inserting a SystemPerformanceLog fails to insert any rows.
	 * Simulates a database operation where executeUpdate returns 0.
	 * Verifies that the method returns false when the insert is unsuccessful.
	 *
	 * @throws Exception if any unexpected error occurs
	 */
	@Test
	void testInsertSystemPerformanceLog_InsertFails() throws Exception {
	    SystemPerformanceLog log = new SystemPerformanceLog(100, 80, 250_000, 2, "OutOfMemoryError");

	    when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
	    when(mockStatement.executeUpdate()).thenReturn(0);

	    boolean result = mockReportDAO.insertSystemPerformanceLog(log);

	    assertFalse(result);
	}

	/**
	 * Tests handling of SQLException during preparation of the insert statement.
	 * Simulates an exception when preparing the statement.
	 * Verifies that the method returns false and the exception is properly caught.
	 *
	 * @throws Exception if any unexpected error occurs
	 */
	@Test
	void testInsertSystemPerformanceLog_SQLExceptionThrown() throws Exception {
	    SystemPerformanceLog log = new SystemPerformanceLog(150, 90, 280_000, 4, "IOException");

	    when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Prepare failed"));

	    boolean result = mockReportDAO.insertSystemPerformanceLog(log);

	    assertFalse(result);
	    verify(mockConnection).prepareStatement(anyString());
	}
	
	
	////////////////////////// testing insertAPIRequestLog //////////////////////////
	
	/**
	 * Tests successful insertion of an API request log with an error code.
	 * Simulates a valid insert where the errorCode is a non-null integer.
	 * Verifies that all values are set correctly and the method returns true.
	 *
	 * @throws Exception if any unexpected exception occurs
	 */
	@Test
	void testInsertAPIRequestLog_WithErrorCode() throws Exception {
	    String requestId = "req-123";
	    int userId = 42;
	    String endpoint = "/games/search";
	    int responseTime = 250;
	    String status = "success";
	    Integer errorCode = 500;

	    when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
	    when(mockStatement.executeUpdate()).thenReturn(1); 

	    boolean result = mockReportDAO.insertAPIRequestLog(requestId, userId, endpoint, responseTime, status, errorCode);

	    assertTrue(result);
	    verify(mockStatement).setString(eq(1), eq(requestId));
	    verify(mockStatement).setInt(eq(2), eq(userId));
	    verify(mockStatement).setString(eq(3), anyString()); 
	    verify(mockStatement).setString(eq(4), eq(endpoint));
	    verify(mockStatement).setInt(eq(5), eq(responseTime));
	    verify(mockStatement).setString(eq(6), eq(status));
	    verify(mockStatement).setInt(eq(7), eq(errorCode));
	    verify(mockStatement).executeUpdate();
	}

	/**
	 * Tests successful insertion of an API request log with a null error code.
	 * Ensures the errorCode is correctly set as SQL NULL in the prepared statement.
	 * Verifies that the method returns true even when optional values are missing.
	 *
	 * @throws Exception if any unexpected exception occurs
	 */
	@Test
	void testInsertAPIRequestLog_NullErrorCode() throws Exception {
	    String requestId = "req-456";
	    int userId = 24;
	    String endpoint = "/games/details";
	    int responseTime = 180;
	    String status = "ok";
	    Integer errorCode = null;

	    when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
	    when(mockStatement.executeUpdate()).thenReturn(1);

	    boolean result = mockReportDAO.insertAPIRequestLog(requestId, userId, endpoint, responseTime, status, errorCode);

	    assertTrue(result);
	    verify(mockStatement).setString(eq(1), eq(requestId));
	    verify(mockStatement).setInt(eq(2), eq(userId));
	    verify(mockStatement).setString(eq(3), anyString());
	    verify(mockStatement).setString(eq(4), eq(endpoint));
	    verify(mockStatement).setInt(eq(5), eq(responseTime));
	    verify(mockStatement).setString(eq(6), eq(status));
	    verify(mockStatement).setNull(eq(7), eq(Types.INTEGER));
	    verify(mockStatement).executeUpdate();
	}

	/**
	 * Tests failure to insert an API request log due to a SQLException during statement preparation.
	 * Simulates an exception being thrown when the statement is prepared.
	 * Verifies that the method returns false and handles the exception gracefully.
	 *
	 * @throws Exception if any unexpected exception occurs
	 */
	@Test
	void testInsertAPIRequestLog_SQLException() throws Exception {
	    String requestId = "req-fail";
	    int userId = 99;
	    String endpoint = "/games/error";
	    int responseTime = 100;
	    String status = "error";
	    Integer errorCode = 404;

	    when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("DB failure"));

	    boolean result = mockReportDAO.insertAPIRequestLog(requestId, userId, endpoint, responseTime, status, errorCode);

	    assertFalse(result);
	    verify(mockConnection).prepareStatement(anyString());
	}
	
	//////////////////////////// testing insertDatabaseIntegrityReport ////////////////////////////
	
	/**
	 * Tests successful insertion of a database integrity report.
	 * Verifies that all fields are correctly bound to the SQL statement
	 * and that the method executes without throwing an exception.
	 *
	 * @throws Exception if an unexpected error occurs
	 */
	@Test
	void testInsertDatabaseIntegrityReport_SuccessfulInsert() throws Exception {
	    DatabaseIntegrityReport report = new DatabaseIntegrityReport(
	        100,   // totalEntries
	        5,     // missingDataEntries
	        3,     // duplicationCount
	        true   // fkIntegrity
	    );

	    when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
	    assertDoesNotThrow(() -> mockReportDAO.insertDatabaseIntegrityReport(report));
	    verify(mockStatement).setInt(1, 100);
	    verify(mockStatement).setInt(2, 5);
	    verify(mockStatement).setInt(3, 3);
	    verify(mockStatement).setBoolean(4, true);
	    verify(mockStatement).executeUpdate();
	}

	/**
	 * Tests graceful handling of an SQLException when attempting to insert
	 * a database integrity report. Ensures that the method does not throw
	 * and that the exception is properly handled internally.
	 *
	 * @throws Exception if an unexpected error occurs
	 */
	@Test
	void testInsertDatabaseIntegrityReport_SQLExceptionCaught() throws Exception {
	    DatabaseIntegrityReport report = new DatabaseIntegrityReport(50, 2, 1, false);
	    when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("DB insert error"));
	    assertDoesNotThrow(() -> mockReportDAO.insertDatabaseIntegrityReport(report));
	    verify(mockConnection).prepareStatement(anyString());
	}
	
	//////////////////////////// testing countTotalGameEntries ////////////////////////////
	
	/**
	 * Tests counting total game entries when the result set contains a value.
	 * Simulates a query result with a single integer value and asserts the correct count.
	 *
	 * @throws Exception if an unexpected error occurs
	 */
	@Test
	void testCountTotalGameEntries_ResultSetHasValue() throws Exception {
	    when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
	    when(mockStatement.executeQuery()).thenReturn(mockResultSet);
	    when(mockResultSet.next()).thenReturn(true);
	    when(mockResultSet.getInt(1)).thenReturn(42);

	    int result = mockReportDAO.countTotalGameEntries();

	    assertEquals(42, result);
	    verify(mockStatement).executeQuery();
	    verify(mockResultSet).next();
	    verify(mockResultSet).getInt(1);
	}

	/**
	 * Tests counting total game entries when the result set is empty.
	 * Simulates a scenario with no matching records and expects a count of zero.
	 *
	 * @throws Exception if an unexpected error occurs
	 */
	@Test
	void testCountTotalGameEntries_ResultSetEmpty() throws Exception {
	    when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
	    when(mockStatement.executeQuery()).thenReturn(mockResultSet);
	    when(mockResultSet.next()).thenReturn(false); 

	    int result = mockReportDAO.countTotalGameEntries();

	    assertEquals(0, result);
	    verify(mockResultSet).next();
	    verify(mockStatement).executeQuery();
	}
	
	/**
	 * Tests that an SQLException thrown during the preparation of the SQL statement
	 * in countTotalGameEntries results in the exception being propagated.
	 *
	 * @throws Exception if an unexpected error occurs
	 */
	@Test
	void testCountTotalGameEntries_SQLExceptionThrown() throws Exception {
	    when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("DB failure"));

	    assertThrows(SQLException.class, () -> mockReportDAO.countTotalGameEntries());
	    verify(mockConnection).prepareStatement(anyString());
	}
	
	//////////////////////////// testing countMissingDataEntries ////////////////////////////
	
	/**
	 * Tests counting missing data entries when the result set contains a valid value.
	 * Ensures the correct value is returned and all interactions are verified.
	 *
	 * @throws Exception if an unexpected error occurs
	 */
	@Test
	void testCountMissingDataEntries_ResultSetHasValue() throws Exception {
	    when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
	    when(mockStatement.executeQuery()).thenReturn(mockResultSet);
	    when(mockResultSet.next()).thenReturn(true);
	    when(mockResultSet.getInt(1)).thenReturn(5);

	    int result = mockReportDAO.countMissingDataEntries();

	    assertEquals(5, result);
	    verify(mockStatement).executeQuery();
	    verify(mockResultSet).next();
	    verify(mockResultSet).getInt(1);
	}
	
	/**
	 * Tests counting missing data entries when the result set is empty.
	 * Expects a return value of 0 and verifies database interactions.
	 *
	 * @throws Exception if an unexpected error occurs
	 */
	@Test
	void testCountMissingDataEntries_ResultSetEmpty() throws Exception {
	    when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
	    when(mockStatement.executeQuery()).thenReturn(mockResultSet);
	    when(mockResultSet.next()).thenReturn(false); 

	    int result = mockReportDAO.countMissingDataEntries();

	    assertEquals(0, result);
	    verify(mockResultSet).next();
	    verify(mockStatement).executeQuery();
	}
	
	/**
	 * Tests that an SQLException thrown during the preparation of the SQL statement
	 * in countMissingDataEntries is correctly thrown by the method.
	 *
	 * @throws Exception if an unexpected error occurs
	 */
	@Test
	void testCountMissingDataEntries_SQLExceptionThrown() throws Exception {
	    when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("DB failure"));

	    assertThrows(SQLException.class, () -> mockReportDAO.countMissingDataEntries());
	    verify(mockConnection).prepareStatement(anyString());
	}
	
	//////////////////////////// testing countDuplicationCount ////////////////////////////
	
	/**
	 * Tests that countDuplicateGames returns the correct number when duplicates are present.
	 *
	 * @throws Exception if an error occurs during test execution
	 */
	@Test
	void testCountDuplicateGames_ResultSetHasValue() throws Exception {
	    when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
	    when(mockStatement.executeQuery()).thenReturn(mockResultSet);
	    when(mockResultSet.next()).thenReturn(true);
	    when(mockResultSet.getInt(1)).thenReturn(5);

	    int result = mockReportDAO.countDuplicateGames();

	    assertEquals(5, result);
	    verify(mockStatement).executeQuery();
	    verify(mockResultSet).next();
	    verify(mockResultSet).getInt(1);
	}

	/**
	 * Tests that countDuplicateGames returns 0 when no duplicate entries are found.
	 *
	 * @throws Exception if an error occurs during test execution
	 */
	@Test
	void testCountDuplicateGames_ResultSetEmpty() throws Exception {
	    when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
	    when(mockStatement.executeQuery()).thenReturn(mockResultSet);
	    when(mockResultSet.next()).thenReturn(false); 

	    int result = mockReportDAO.countDuplicateGames();

	    assertEquals(0, result);
	    verify(mockStatement).executeQuery();
	    verify(mockResultSet).next();
	}

	/**
	 * Tests that countDuplicateGames correctly throws an SQLException if the query preparation fails.
	 *
	 * @throws Exception if an error occurs during test execution
	 */
	@Test
	void testCountDuplicateGames_SQLExceptionThrown() throws Exception {
	    when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Simulated failure"));

	    assertThrows(SQLException.class, () -> mockReportDAO.countDuplicateGames());
	    verify(mockConnection).prepareStatement(anyString());
	}
	
	//////////////////////////// testing checkForeignKeyIntegrity ////////////////////////////
	
	/**
	 * Tests that checkForeignKeyIntegrity returns true when no foreign key violations exist.
	 *
	 * @throws Exception if an error occurs during test execution
	 */
	@Test
	void testCheckForeignKeyIntegrity_NoViolations() throws Exception {
	    when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
	    when(mockStatement.executeQuery()).thenReturn(mockResultSet);
	    when(mockResultSet.next()).thenReturn(false); 

	    boolean result = mockReportDAO.checkForeignKeyIntegrity();

	    assertTrue(result);
	    verify(mockStatement).executeQuery();
	    verify(mockResultSet).next();
	}

	/**
	 * Tests that checkForeignKeyIntegrity returns false when foreign key violations are present.
	 *
	 * @throws Exception if an error occurs during test execution
	 */
	@Test
	void testCheckForeignKeyIntegrity_ViolationsExist() throws Exception {
	    when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
	    when(mockStatement.executeQuery()).thenReturn(mockResultSet);
	    when(mockResultSet.next()).thenReturn(true); 

	    boolean result = mockReportDAO.checkForeignKeyIntegrity();

	    assertFalse(result);
	    verify(mockStatement).executeQuery();
	    verify(mockResultSet).next();
	}

	/**
	 * Tests that checkForeignKeyIntegrity throws an SQLException when query preparation fails.
	 *
	 * @throws Exception if an error occurs during test execution
	 */
	@Test
	void testCheckForeignKeyIntegrity_SQLExceptionThrown() throws Exception {
	    when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("SQL error"));

	    assertThrows(SQLException.class, () -> mockReportDAO.checkForeignKeyIntegrity());
	    verify(mockConnection).prepareStatement(anyString());
	}
	
	///////////////////////////////// testing countUsersWithMissingEmails ////////////////////////////
	
	/**
	 * Tests that countUsersWithMissingEmails returns the correct number when users with missing emails are found.
	 *
	 * @throws Exception if an error occurs during test execution
	 */
	@Test
	void testCountUsersWithMissingEmails_ResultSetHasValue() throws Exception {
	    when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
	    when(mockStatement.executeQuery()).thenReturn(mockResultSet);
	    when(mockResultSet.next()).thenReturn(true);
	    when(mockResultSet.getInt(1)).thenReturn(2);

	    int result = mockReportDAO.countUsersWithMissingEmails();

	    assertEquals(2, result);
	    verify(mockStatement).executeQuery();
	    verify(mockResultSet).next();
	    verify(mockResultSet).getInt(1);
	}

	/**
	 * Tests that countUsersWithMissingEmails returns 0 when no users with missing emails are found.
	 *
	 * @throws Exception if an error occurs during test execution
	 */
	@Test
	void testCountUsersWithMissingEmails_ResultSetEmpty() throws Exception {
	    when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
	    when(mockStatement.executeQuery()).thenReturn(mockResultSet);
	    when(mockResultSet.next()).thenReturn(false);

	    int result = mockReportDAO.countUsersWithMissingEmails();

	    assertEquals(0, result);
	    verify(mockStatement).executeQuery();
	    verify(mockResultSet).next();
	}

	/**
	 * Tests that countUsersWithMissingEmails throws an SQLException if the query preparation fails.
	 *
	 * @throws Exception if an error occurs during test execution
	 */
	@Test
	void testCountUsersWithMissingEmails_SQLExceptionThrown() throws Exception {
	    when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("SQL Error"));

	    assertThrows(SQLException.class, () -> mockReportDAO.countUsersWithMissingEmails());
	    verify(mockConnection).prepareStatement(anyString());
	}
	
	/////////////////////////// testing countOphanedGameCollections ////////////////////////////
	
	/**
	 * Tests that countOrphanedGameCollections returns the correct count when orphaned collection rows exist.
	 *
	 * @throws Exception if an error occurs during test execution
	 */
	@Test
	void testCountOrphanedGameCollections_ResultSetHasValue() throws Exception {
	    when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
	    when(mockStatement.executeQuery()).thenReturn(mockResultSet);
	    when(mockResultSet.next()).thenReturn(true);
	    when(mockResultSet.getInt(1)).thenReturn(7);

	    int result = mockReportDAO.countOrphanedGameCollections();

	    assertEquals(7, result);
	    verify(mockStatement).executeQuery();
	    verify(mockResultSet).next();
	    verify(mockResultSet).getInt(1);
	}

	/**
	 * Tests that countOrphanedGameCollections returns 0 when no orphaned game collections are found.
	 *
	 * @throws Exception if an error occurs during test execution
	 */
	@Test
	void testCountOrphanedGameCollections_ResultSetEmpty() throws Exception {
	    when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
	    when(mockStatement.executeQuery()).thenReturn(mockResultSet);
	    when(mockResultSet.next()).thenReturn(false);

	    int result = mockReportDAO.countOrphanedGameCollections();

	    assertEquals(0, result);
	    verify(mockStatement).executeQuery();
	    verify(mockResultSet).next();
	}

	/**
	 * Tests that countOrphanedGameCollections throws an SQLException when query preparation fails.
	 *
	 * @throws Exception if an error occurs during test execution
	 */
	@Test
	void testCountOrphanedGameCollections_SQLExceptionThrown() throws Exception {
	    when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("FK check failure"));

	    assertThrows(SQLException.class, () -> mockReportDAO.countOrphanedGameCollections());
	    verify(mockConnection).prepareStatement(anyString());
	}
	
	////////////////////////////////// testing getAllSystemPerformanceReports ////////////////////////////
	
	/**
	 * Tests that getAllSystemPerformanceReports returns a list with one SystemPerformanceLog object
	 * when a single report exists in the database.
	 *
	 * @throws Exception if an error occurs during test execution
	 */
	@Test
	void testGetAllSystemPerformanceReports_WithResults() throws Exception { 

	    Statement mockStmt = mock(Statement.class);
	    ResultSet mockResultSet = mock(ResultSet.class);
	    Connection mockConnection = mock(Connection.class);

	    ReportDAO reportDAO = new ReportDAO(mockConnection);

	    when(mockConnection.createStatement()).thenReturn(mockStmt);
	    when(mockStmt.executeQuery(anyString())).thenReturn(mockResultSet);

	    when(mockResultSet.next()).thenReturn(true, false);
	    when(mockResultSet.getInt("ReportID")).thenReturn(1);
	    when(mockResultSet.getString("Timestamp")).thenReturn("2024-05-20T10:00");
	    when(mockResultSet.getInt("ScreenLoadTime")).thenReturn(200);
	    when(mockResultSet.getInt("DbQueryTime")).thenReturn(120);
	    when(mockResultSet.getInt("MemoryUsage")).thenReturn(512);
	    when(mockResultSet.getInt("GcEvents")).thenReturn(2);
	    when(mockResultSet.getString("ExceptionsLogged")).thenReturn("None");
	    List<SystemPerformanceLog> reports = reportDAO.getAllSystemPerformanceReports();
	    assertEquals(1, reports.size());

	    SystemPerformanceLog log = reports.get(0);
	    assertEquals(1, log.getReportID());
	    assertEquals(LocalDateTime.parse("2024-05-20T10:00"), log.getTimestamp());
	    assertEquals(200, log.getScreenLoadTime());
	    assertEquals(120, log.getDbQueryTime());
	    assertEquals(512, log.getMemoryUsage());
	    assertEquals(2, log.getGcEvents());
	    assertEquals("None", log.getExceptionsLogged());

	    verify(mockStmt).executeQuery(anyString());
	}

	/**
	 * Tests that getAllSystemPerformanceReports returns an empty list when no performance logs are found.
	 *
	 * @throws Exception if an error occurs during test execution
	 */
	@Test
	void testGetAllSystemPerformanceReports_EmptyResultSet_ReturnsEmptyList() throws Exception {
	    Statement mockStmt = mock(Statement.class);

	    when(mockConnection.createStatement()).thenReturn(mockStmt);
	    when(mockStmt.executeQuery(anyString())).thenReturn(mockResultSet);
	    when(mockResultSet.next()).thenReturn(false);
	    List<SystemPerformanceLog> reports = mockReportDAO.getAllSystemPerformanceReports();

	    assertNotNull(reports);
	    assertTrue(reports.isEmpty());
	    verify(mockStmt).executeQuery(anyString());
	}

	/**
	 * Tests that getAllSystemPerformanceReports returns an empty list
	 * when an SQLException is thrown during statement creation.
	 *
	 * @throws Exception if an error occurs during test execution
	 */
	@Test
	void testGetAllSystemPerformanceReports_SQLException_ReturnsEmptyList() throws Exception {
	    when(mockConnection.createStatement()).thenThrow(new SQLException("Simulated failure"));

	    List<SystemPerformanceLog> reports = mockReportDAO.getAllSystemPerformanceReports();

	    assertNotNull(reports);
	    assertTrue(reports.isEmpty());
	    verify(mockConnection).createStatement();
	}
	
	///////////////////////////// testing getAllAPIRequestLogs ////////////////////////////
	
	/**
	 * Tests that getAllAPIRequestLogs returns a list containing one APIRequestLog
	 * when the result set contains one row with no error code.
	 *
	 * @throws Exception if an error occurs during test execution
	 */
	@Test
	void testGetAllAPIRequestLogs_WithResults() throws Exception {
		when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
    	when(mockStatement.executeQuery()).thenReturn(mockResultSet);

    	when(mockResultSet.next()).thenReturn(true, false); // One row
    	when(mockResultSet.getString("RequestID")).thenReturn("req123");
    	when(mockResultSet.getInt("UserID")).thenReturn(1);
    	when(mockResultSet.getString("Timestamp")).thenReturn("2024-05-20T10:00:00");
    	when(mockResultSet.getString("APIEndpoint")).thenReturn("/games/search");
    	when(mockResultSet.getInt("ResponseTime")).thenReturn(150);
    	when(mockResultSet.getString("Status")).thenReturn("success");
    	when(mockResultSet.getObject("ErrorCode")).thenReturn(null); 

    	List<APIRequestLog> logs = mockReportDAO.getAllAPIRequestLogs();

   		assertEquals(1, logs.size());

    	APIRequestLog log = logs.get(0);
    	assertEquals("req123", log.getRequestID());
    	assertEquals(1, log.getUserID());
    	assertEquals("2024-05-20T10:00:00", log.getTimestamp());
    	assertEquals("/games/search", log.getApiEndpoint());
    	assertEquals(150, log.getResponseTime());
    	assertEquals("success", log.getStatus());
    	assertNull(log.getErrorCode());

    	verify(mockStatement).executeQuery();
	}

	/**
	 * Tests that getAllAPIRequestLogs returns a list with one APIRequestLog
	 * that includes an error code when it is present in the result set.
	 *
	 * @throws Exception if an error occurs during test execution
	 */
	@Test
	void testGetAllAPIRequestLogs_WithErrorCode() throws Exception {
    	when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
    	when(mockStatement.executeQuery()).thenReturn(mockResultSet);

    	when(mockResultSet.next()).thenReturn(true, false);
    	when(mockResultSet.getString("RequestID")).thenReturn("req999");
    	when(mockResultSet.getInt("UserID")).thenReturn(2);
    	when(mockResultSet.getString("Timestamp")).thenReturn("2024-05-20T12:00:00");
    	when(mockResultSet.getString("APIEndpoint")).thenReturn("/games/details");
    	when(mockResultSet.getInt("ResponseTime")).thenReturn(400);
   		when(mockResultSet.getString("Status")).thenReturn("error");
    	when(mockResultSet.getObject("ErrorCode")).thenReturn(500);

    	List<APIRequestLog> logs = mockReportDAO.getAllAPIRequestLogs();

    	assertEquals(1, logs.size());
    	assertEquals(Integer.valueOf(500), logs.get(0).getErrorCode());
    	verify(mockStatement).executeQuery();
	}

	/**
	 * Tests that getAllAPIRequestLogs returns an empty list
	 * when the result set has no rows.
	 *
	 * @throws Exception if an error occurs during test execution
	 */
	@Test
	void testGetAllAPIRequestLogs_EmptyResultSet() throws Exception {
    	when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
    	when(mockStatement.executeQuery()).thenReturn(mockResultSet);
    	when(mockResultSet.next()).thenReturn(false); 

    	List<APIRequestLog> logs = mockReportDAO.getAllAPIRequestLogs();

    	assertNotNull(logs);
    	assertTrue(logs.isEmpty());
    	verify(mockStatement).executeQuery();
	}

	/**
	 * Tests that getAllAPIRequestLogs returns an empty list
	 * when an SQLException is thrown during statement preparation.
	 *
	 * @throws Exception if an error occurs during test execution
	 */
	@Test
	void testGetAllAPIRequestLogs_SQLException() throws Exception {
    	when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("DB error"));

    	List<APIRequestLog> logs = mockReportDAO.getAllAPIRequestLogs();

    	assertNotNull(logs);
    	assertTrue(logs.isEmpty());
    	verify(mockConnection).prepareStatement(anyString());
}

	
	///////////////////////////// testing getAllDatabaseIntegrityReports ////////////////////////////
	
	/**
	 * Tests that getAllDatabaseIntegrityReports returns a list with one report
	 * when the result set contains a single row with valid data.
	 *
	 * @throws Exception if any exception occurs during the test
	 */
	@Test
	void testGetAllDatabaseIntegrityReports_WithResults() throws Exception {
	    when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
	    when(mockStatement.executeQuery()).thenReturn(mockResultSet);

	    when(mockResultSet.next()).thenReturn(true, false); 
	    when(mockResultSet.getInt("ReportID")).thenReturn(1);
	    when(mockResultSet.getString("Timestamp")).thenReturn("2024-05-20T10:00:00");
	    when(mockResultSet.getInt("TotalEntries")).thenReturn(100);
	    when(mockResultSet.getInt("MissingDataEntries")).thenReturn(5);
	    when(mockResultSet.getInt("DuplicationCount")).thenReturn(3);
	    when(mockResultSet.getBoolean("FkIntegrity")).thenReturn(true);

	    List<DatabaseIntegrityReport> reports = mockReportDAO.getAllDatabaseIntegrityReports();

	    assertEquals(1, reports.size());

	    DatabaseIntegrityReport report = reports.get(0);
	    assertEquals(1, report.getReportID());
	    assertEquals("2024-05-20T10:00:00", report.getTimestamp());
	    assertEquals(100, report.getTotalEntries());
	    assertEquals(5, report.getMissingDataEntries());
	    assertEquals(3, report.getDuplicationCount());
	    assertTrue(report.isFkIntegrity());

	    verify(mockStatement).executeQuery();
	}

	/**
	 * Tests that getAllDatabaseIntegrityReports returns an empty list
	 * when the result set contains no records.
	 *
	 * @throws Exception if any exception occurs during the test
	 */
	@Test
	void testGetAllDatabaseIntegrityReports_EmptyResultSet_ReturnsEmptyList() throws Exception {
	    when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
	    when(mockStatement.executeQuery()).thenReturn(mockResultSet);
	    when(mockResultSet.next()).thenReturn(false);

	    List<DatabaseIntegrityReport> reports = mockReportDAO.getAllDatabaseIntegrityReports();

	    assertNotNull(reports); 
	    assertTrue(reports.isEmpty());
	    verify(mockStatement).executeQuery();
	}

	/**
	 * Tests that getAllDatabaseIntegrityReports returns an empty list
	 * when an SQLException is thrown during statement preparation.
	 *
	 * @throws Exception if any exception occurs during the test
	 */
	@Test
	void testGetAllDatabaseIntegrityReports_SQLException_ReturnsEmptyList() throws Exception {
	    when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Simulated SQL failure"));

	    List<DatabaseIntegrityReport> reports = mockReportDAO.getAllDatabaseIntegrityReports();

	    assertNotNull(reports);
	    assertTrue(reports.isEmpty());
	    verify(mockConnection).prepareStatement(anyString());
	}
}
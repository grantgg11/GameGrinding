package reports;

import database.ReportDAO;
import models.DatabaseIntegrityReport; 
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the DatabaseIntegrityLogger class.
 *
 * These tests verify that database integrity metrics are properly analyzed and logged
 * using a mocked ReportDAO. The logger collects statistics such as total entries,
 * missing data, duplicate records, and foreign key integrity status, then inserts
 * a report using DAO methods.
 *
 * Reflection is used to inject the mock DAO into the logger instance for isolation.
 */

class DatabaseIntegrityLoggerTest {

    private DatabaseIntegrityLogger integrityLogger;
    private ReportDAO mockDAO;

    /**
     * Sets up the test environment by creating a DatabaseIntegrityLogger instance and
     * injecting a mocked ReportDAO using reflection.
     *
     * @throws Exception if reflection access to the reportDAO field fails
     */
    @BeforeEach
    void setUp() throws Exception {
        integrityLogger = new DatabaseIntegrityLogger();
        mockDAO = mock(ReportDAO.class);

        Field daoField = DatabaseIntegrityLogger.class.getDeclaredField("reportDAO");
        daoField.setAccessible(true);
        daoField.set(integrityLogger, mockDAO);
    }

    /**
     * Tests that logDatabaseIntegrity correctly builds and inserts a report with the
     * given values. Verifies that the captured report contains the expected data.
     */
    @Test
    void testLogDatabaseIntegrity_ValidValues_InsertsReport() {
        integrityLogger.logDatabaseIntegrity(100, 5, 3, true);

        ArgumentCaptor<DatabaseIntegrityReport> captor = ArgumentCaptor.forClass(DatabaseIntegrityReport.class);
        verify(mockDAO).insertDatabaseIntegrityReport(captor.capture());

        DatabaseIntegrityReport report = captor.getValue();
        assertEquals(100, report.getTotalEntries());
        assertEquals(5, report.getMissingDataEntries());
        assertEquals(3, report.getDuplicationCount());
        assertTrue(report.isFkIntegrity());
    }
    
    /**
     * Tests that logDatabaseIntegrity does not throw an exception when the DAO
     * fails during report insertion. Verifies the insert was attempted.
     */
    @Test
    void testLogDatabaseIntegrity_DAOThrowsException() {
        doThrow(new RuntimeException("DB insert failed")).when(mockDAO).insertDatabaseIntegrityReport(any(DatabaseIntegrityReport.class));
        integrityLogger.logDatabaseIntegrity(10, 2, 1, false);
        verify(mockDAO).insertDatabaseIntegrityReport(any(DatabaseIntegrityReport.class));
    }

    /**
     * Tests that analyzeAndLogDatabaseIntegrity retrieves metrics using all DAO methods
     * and logs the correct values in the final report. Verifies accurate totals and insertion.
     *
     * @throws SQLException if any DAO method fails
     */
    @Test
    void testAnalyzeAndLogDatabaseIntegrity_CallsAllDAOAndLogs() throws SQLException {
        when(mockDAO.countTotalGameEntries()).thenReturn(50);
        when(mockDAO.countMissingDataEntries()).thenReturn(2);
        when(mockDAO.countDuplicateGames()).thenReturn(1);
        when(mockDAO.checkForeignKeyIntegrity()).thenReturn(true);
        when(mockDAO.countUsersWithMissingEmails()).thenReturn(1);
        when(mockDAO.countOrphanedGameCollections()).thenReturn(1);

        integrityLogger.analyzeAndLogDatabaseIntegrity();

        ArgumentCaptor<DatabaseIntegrityReport> captor = ArgumentCaptor.forClass(DatabaseIntegrityReport.class);
        verify(mockDAO).insertDatabaseIntegrityReport(captor.capture());

        DatabaseIntegrityReport report = captor.getValue();
        assertEquals(50, report.getTotalEntries());
        assertEquals(3, report.getMissingDataEntries());
        assertEquals(2, report.getDuplicationCount());
        assertTrue(report.isFkIntegrity());
    }
    
    /**
     * Tests that analyzeAndLogDatabaseIntegrity handles exceptions thrown by DAO methods
     * gracefully. The test simulates a failure on the first metric call and ensures
     * no exception is thrown externally.
     *
     * @throws SQLException if a DAO method fails
     */
    @Test
    void testAnalyzeAndLogDatabaseIntegrity_DAOThrowsException() throws SQLException {
        when(mockDAO.countTotalGameEntries()).thenThrow(new RuntimeException("Simulated DB read error"));
        integrityLogger.analyzeAndLogDatabaseIntegrity();
        verify(mockDAO).countTotalGameEntries();
    }

}

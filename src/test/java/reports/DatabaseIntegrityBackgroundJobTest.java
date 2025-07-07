package reports;


import static org.mockito.Mockito.*;

import java.lang.reflect.Field;

import org.junit.jupiter.api.BeforeEach; 
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Unit tests for the DatabaseIntegrityBackgroundJob class.
 *
 * This test ensures that the background job correctly triggers the
 * database integrity analysis and logging by calling the appropriate
 * method in the DatabaseIntegrityLogger.
 *
 * The DatabaseIntegrityLogger dependency is injected using reflection
 * to isolate the test from external dependencies.
 */
class DatabaseIntegrityBackgroundJobTest {

    private DatabaseIntegrityBackgroundJob backgroundJob;
    private DatabaseIntegrityLogger mockLogger;

    /**
     * Sets up the test environment by creating an instance of the background job
     * and injecting a mocked DatabaseIntegrityLogger using reflection.
     *
     * @throws Exception if the logger field cannot be accessed or set
     */
    @BeforeEach
    void setUp() throws Exception {
        backgroundJob = new DatabaseIntegrityBackgroundJob();
        mockLogger = Mockito.mock(DatabaseIntegrityLogger.class);
        Field loggerField = DatabaseIntegrityBackgroundJob.class.getDeclaredField("logger");
        loggerField.setAccessible(true);
        loggerField.set(backgroundJob, mockLogger);
    }

    /**
     * Verifies that runIntegrityCheck() invokes the analyzeAndLogDatabaseIntegrity()
     * method on the injected logger exactly once.
     */
    @Test
    void testRunIntegrityCheck_CallsAnalyzeAndLog() {
        backgroundJob.runIntegrityCheck();
        verify(mockLogger, times(1)).analyzeAndLogDatabaseIntegrity();
    }
}

package reports;

import database.ReportDAO;
import models.SystemPerformanceLog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the PerformanceTracker class.
 *
 * This test class verifies the behavior of performance logging and system
 * metrics collection in the PerformanceTracker. It uses a mocked ReportDAO
 * to validate database insertions and system metric retrieval methods.
 */

class PerformanceTrackerTest {
 
    private PerformanceTracker tracker;
    private ReportDAO mockDAO;

    /**
     * Initializes the test environment by creating a PerformanceTracker instance
     * with a mocked ReportDAO. This ensures database operations are isolated from
     * actual data sources.
     */
    @BeforeEach
    void setUp() {
        mockDAO = mock(ReportDAO.class);
        tracker = new PerformanceTracker(mockDAO);
    }

    /**
     * Tests that logPerformanceData successfully logs the performance data
     * when the DAO insert method returns true. Verifies that the insert method
     * was called with a SystemPerformanceLog object.
     */
    @Test
    void testLogPerformanceData_InsertsLogSuccessfully() {
        when(mockDAO.insertSystemPerformanceLog(any(SystemPerformanceLog.class))).thenReturn(true);
        tracker.logPerformanceData(300, 100, 256, 5, "No issues");
        verify(mockDAO).insertSystemPerformanceLog(any(SystemPerformanceLog.class));
    }

    /**
     * Tests that logPerformanceData handles a failed insertion by still calling
     * the DAO's insert method. Ensures the method attempts to insert data even
     * if the result is false.
     */
    @Test
    void testLogPerformanceData_InsertFails_LogsError() {
        when(mockDAO.insertSystemPerformanceLog(any(SystemPerformanceLog.class))).thenReturn(false);
        tracker.logPerformanceData(300, 100, 256, 5, "Error case");
        verify(mockDAO).insertSystemPerformanceLog(any(SystemPerformanceLog.class));
    }

    /**
     * Tests that getMemoryUsage returns a non-negative integer value representing
     * current memory usage. The method must never return a negative result.
     */
    @Test
    void testGetMemoryUsage_ReturnsNonNegativeValue() {
        int memory = tracker.getMemoryUsage();
        assertTrue(memory >= 0);
    }

    /**
     * Tests that getGCEvents returns a non-negative integer value representing
     * the number of garbage collection events. The method must never return a
     * negative result.
     */
    @Test
    void testGetGCEvents_ReturnsNonNegativeValue() {
        int gcEvents = tracker.getGCEvents();
        assertTrue(gcEvents >= 0);
    }

}

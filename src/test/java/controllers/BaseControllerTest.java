package controllers;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reports.PerformanceTracker;

/**
 * Unit tests for the BaseController abstract class using a concrete TestController subclass.
 *
 * The BaseController is responsible for handling the common behavior of setting the user ID,
 * calling `onUserDataLoad`, and logging performance metrics via the PerformanceTracker.
 * These tests validate correct behavior under various user ID and exception scenarios.
 */
class BaseControllerTest {

    private TestController controller;
    private PerformanceTracker mockTracker;
 

    /**
     * A concrete implementation of BaseController used specifically for testing.
     * It tracks whether onUserDataLoad() was successfully called and simulates throwing an exception if requested.
     */
    static class TestController extends BaseController {
        private boolean userDataLoaded = false;
        private boolean throwException = false;

        public TestController(PerformanceTracker tracker) {
            this.tracker = tracker;
        }

        @Override
        protected void onUserDataLoad() {
            if (throwException) {
                throw new RuntimeException("Test exception");
            }
            userDataLoaded = true;
        }

        public boolean isUserDataLoaded() {
            return userDataLoaded;
        }

        public void setThrowException(boolean throwException) {
            this.throwException = throwException;
        }
    }

    /**
     * Sets up the mock PerformanceTracker and a fresh instance of TestController before each test. 
     * Default values are returned from the tracker.
     */
    @BeforeEach
    void setUp() {
        mockTracker = mock(PerformanceTracker.class);
        controller = new TestController(mockTracker);
        when(mockTracker.getMemoryUsage()).thenReturn(100);
        when(mockTracker.getGCEvents()).thenReturn(1);
    }

    /**
     * Verifies that setting a valid user ID triggers onUserDataLoad and logs performance metrics with expected values.
     */
    @Test
    void testSetUserID_validUser_callsOnUserDataLoad_andLogsPerformance() {
        controller.setUserID(1);

        assert(controller.loggedInUserID == 1);
        assert(controller.isUserDataLoaded());

        verify(mockTracker).logPerformanceData(
            anyInt(), // screenLoadTime
            anyInt(), // dbQueryTime
            eq(100),  // memoryUsage
            eq(1),    // gcEvents
            isNull()  // exceptionMessage
        );
    }

    /**
	 * Tests that setting a user ID while an exception is thrown in onUserDataLoad logs the exception message.
	 */
    @Test
    void testSetUserID_onUserDataLoadThrows_exceptionIsLogged() {
        controller.setThrowException(true);
        controller.setUserID(1);

        verify(mockTracker).logPerformanceData(
            anyInt(),
            anyInt(),
            eq(100),
            eq(1),
            eq("Test exception")
        );
    }

    /**
	 * Tests that setting a user ID of 0 does not call onUserDataLoad and does not log performance data.
	 * This simulates an invalid user ID scenario.
	 */
    @Test
    void testSetUserID_invalidUser_doesNotCallOnUserDataLoad() {
        controller.setUserID(0);
        assert(controller.loggedInUserID == 0);
        assert(!controller.isUserDataLoaded());
        verify(mockTracker, never()).logPerformanceData(anyInt(), anyInt(), anyInt(), anyInt(), any());
    }
}

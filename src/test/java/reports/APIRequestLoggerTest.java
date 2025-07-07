package reports;

import static org.mockito.Mockito.*;

import database.ReportDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.lang.reflect.Field;

/**
 * Unit tests for the APIRequestLogger class.
 * 
 * These tests validate the behavior of logging API requests through the ReportDAO,
 * including successful and failed insertions, and ensure sensitive data (such as API keys)
 * is masked using the sanitizeEndpoint() method.
 * 
 * Mocks are injected using Mockito to isolate the behavior of the logger.
 */

class APIRequestLoggerTest {

    @Mock
    private ReportDAO mockReportDAO; 

    @InjectMocks
    private APIRequestLogger apiRequestLogger;

    @Captor
    private ArgumentCaptor<String> endpointCaptor;

    /**
     * Initializes mocks and injects the mock ReportDAO into the APIRequestLogger instance
     * using reflection before each test case.
     *
     * @throws Exception if field access or injection fails
     */
    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        apiRequestLogger = new APIRequestLogger();
        Field daoField = APIRequestLogger.class.getDeclaredField("reportDAO");
        daoField.setAccessible(true);
        daoField.set(apiRequestLogger, mockReportDAO);
    }

    /**
     * Verifies that logAPIRequest() successfully inserts a request record into the ReportDAO
     * when all arguments are valid and insertion returns true.
     */
    @Test
    void testLogAPIRequest_SuccessfulInsert() {
        when(mockReportDAO.insertAPIRequestLog(anyString(), anyInt(), anyString(), anyInt(), anyString(), any()))
                .thenReturn(true);
        apiRequestLogger.logAPIRequest(1, "https://api.example.com/games?api_key=12345", 120, "success", null);

        verify(mockReportDAO, times(1)).insertAPIRequestLog(
                anyString(),
                eq(1),
                eq("https://api.example.com/games?api_key=12345"),
                eq(120),
                eq("success"),
                isNull()
        );
    }

    /**
     * Verifies that logAPIRequest() is still called correctly even when the DAO insertion
     * fails (returns false), and the method handles the failure gracefully.
     */
    @Test
    void testLogAPIRequest_FailedInsert() {
        when(mockReportDAO.insertAPIRequestLog(anyString(), anyInt(), anyString(), anyInt(), anyString(), any())).thenReturn(false);
        apiRequestLogger.logAPIRequest(2, "https://api.example.com/search?api_key=abc", 300, "error", 500);
        verify(mockReportDAO).insertAPIRequestLog(
                anyString(),
                eq(2),
                eq("https://api.example.com/search?api_key=abc"),
                eq(300),
                eq("error"),
                eq(500)
        );
    }

    /**
     * Uses reflection to test the private sanitizeEndpoint() method, ensuring that any API key
     * in the query string is properly masked with asterisks.
     *
     * @throws Exception if the private method cannot be accessed or invoked
     */
    @Test
    void testSanitizeEndpoint_MasksApiKey() throws Exception {
        String input = "https://api.example.com/data?api_key=123&query=test";
        String expected = "https://api.example.com/data?api_key=***&query=test";
        var method = APIRequestLogger.class.getDeclaredMethod("sanitizeEndpoint", String.class);
        method.setAccessible(true);
        String result = (String) method.invoke(apiRequestLogger, input);
        assert result.equals(expected) : "Sanitized endpoint did not mask api_key correctly";
    }
}
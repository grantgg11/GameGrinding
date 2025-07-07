package utils;


import com.google.gson.JsonObject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.HttpURLConnection; 
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


/**
 * Unit tests for the JSONParser utility class.
 *
 * Tests cover different scenarios for sending HTTP GET requests, including success,
 * rate limiting, exceeding retry attempts, and error responses. Also validates
 * JSON parsing logic using GSON.
 */

@ExtendWith(MockitoExtension.class)
class JSONParserTest {

	/**
	 * Tests successful execution of sendGetRequest when a 200 OK response is returned.
	 *
	 * @throws Exception if mocking or stream reading fails
	 */
	@Test
	void testSendGetRequest_SuccessfulResponse() throws Exception {
	    String mockUrl = "http://mockapi.com/success";
	    String expectedJson = "{\"key\":\"value\"}";

	    HttpURLConnection mockConnection = mock(HttpURLConnection.class);

	    Class.forName("utils.JSONParser");

	    try (
	        MockedConstruction<URL> mockedURL = mockConstruction(URL.class,(mock, context) -> when(mock.openConnection()).thenReturn(mockConnection));
	        MockedStatic<JSONParser> mockedStatic = mockStatic(JSONParser.class, CALLS_REAL_METHODS)
	    ) {
	        mockedStatic.when(() -> JSONParser.delayRetry(anyInt())).thenAnswer(inv -> null);

	        when(mockConnection.getResponseCode()).thenReturn(200);
	        when(mockConnection.getInputStream()).thenReturn(new java.io.ByteArrayInputStream(expectedJson.getBytes()));

	        String result = JSONParser.sendGetRequest(mockUrl);
	        assertEquals(expectedJson, result);
	    }
	}

	/**
	 * Tests that sendGetRequest handles HTTP 429 rate limiting by retrying and eventually succeeding.
	 *
	 * @throws Exception if mocking or stream reading fails
	 */
    @Test
    void testSendGetRequest_HitsRateLimitThenSucceeds() throws Exception {
        String expectedJson = "{\"retry\":\"ok\"}";

        HttpURLConnection mockConnection = mock(HttpURLConnection.class);

        try (
            MockedConstruction<URL> mockedURL = mockConstruction(URL.class,(mock, context) -> when(mock.openConnection()).thenReturn(mockConnection));
            MockedStatic<JSONParser> mockedStatic = mockStatic(JSONParser.class, CALLS_REAL_METHODS)
        ) {
            mockedStatic.when(() -> JSONParser.delayRetry(anyInt())).thenAnswer(inv -> null);
            when(mockConnection.getResponseCode())
                    .thenReturn(429)
                    .thenReturn(429)
                    .thenReturn(200);

            when(mockConnection.getInputStream()).thenReturn(new java.io.ByteArrayInputStream(expectedJson.getBytes()));
            String result = JSONParser.sendGetRequest("http://mock.retry");
            assertEquals(expectedJson, result);
            mockedStatic.verify(() -> JSONParser.delayRetry(1100), times(2));
        }
    }

    /**
     * Tests that sendGetRequest throws IOException after exceeding the maximum number of retries for HTTP 429.
     *
     * @throws Exception if mocking fails
     */
    @Test
    void testSendGetRequest_ExceedsMaxRetries() throws Exception {
        String mockUrl = "http://mockapi.com/fail";

        HttpURLConnection mockConnection = mock(HttpURLConnection.class);
        Class.forName("utils.JSONParser");

        try (
            MockedConstruction<URL> mockedURL = mockConstruction(URL.class,(mock, context) -> when(mock.openConnection()).thenReturn(mockConnection));
            MockedStatic<JSONParser> mockedStatic = mockStatic(JSONParser.class, CALLS_REAL_METHODS)
        ) {
            mockedStatic.when(() -> JSONParser.delayRetry(anyInt())).thenAnswer(inv -> null);
            when(mockConnection.getResponseCode()).thenReturn(429);
            IOException exception = assertThrows(IOException.class, () -> JSONParser.sendGetRequest(mockUrl));
            assertTrue(exception.getMessage().contains("Max retries reached"));
            mockedStatic.verify(() -> JSONParser.delayRetry(1100), times(5));
        }
    }

    /**
     * Tests that sendGetRequest throws an IOException for non-retryable HTTP errors like 500.
     *
     * @throws Exception if mocking fails
     */
    @Test
    void testSendGetRequest_Non429Failure() throws Exception {
        String mockUrl = "http://mockapi.com/error";

        HttpURLConnection mockConnection = mock(HttpURLConnection.class);

        try (MockedConstruction<URL> mockedURL = mockConstruction(URL.class,(mock, context) -> when(mock.openConnection()).thenReturn(mockConnection))) {
            when(mockConnection.getResponseCode()).thenReturn(500);

            IOException exception = assertThrows(IOException.class, () -> JSONParser.sendGetRequest(mockUrl));
            assertTrue(exception.getMessage().contains("HTTP GET request failed with response code: 500"));
        }
    }
    
    /**
     * Tests that sendGetRequest throws an IOException for non-retryable HTTP errors like 500.
     *
     * @throws Exception if mocking fails
     */
    @Test
    void testSendGetRequest_InterruptedDuringRetry_ThrowsIOException() throws Exception {
        String mockUrl = "http://mockapi.com/interrupted";

        HttpURLConnection mockConnection = mock(HttpURLConnection.class);

        Class.forName("utils.JSONParser");

        try (
            MockedConstruction<URL> mockedURL = mockConstruction(URL.class,
                (mock, context) -> when(mock.openConnection()).thenReturn(mockConnection));
            MockedStatic<JSONParser> mockedStatic = mockStatic(JSONParser.class, CALLS_REAL_METHODS)
        ) {
            mockedStatic.when(() -> JSONParser.delayRetry(anyInt())).thenThrow(new InterruptedException("Simulated interruption"));

            when(mockConnection.getResponseCode()).thenReturn(429); 
            IOException exception = assertThrows(IOException.class, () -> JSONParser.sendGetRequest(mockUrl));

            assertEquals("Thread was interrupted during rate limit wait", exception.getMessage());
        }
    }

    /**
     * Tests that parseJson correctly parses a JSON string into a JsonObject.
     */
    @Test
    void testParseJson_ReturnsCorrectJsonObject() {
        String json = "{\"name\":\"GameGrinding\",\"platform\":\"PlayStation\"}";
        JsonObject obj = JSONParser.parseJson(json);

        assertEquals("GameGrinding", obj.get("name").getAsString());
        assertEquals("PlayStation", obj.get("platform").getAsString());
    }
}

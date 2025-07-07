package nonfunctional.ReliabilityandErrorHandling;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import controllers.TestUtils;
import services.mobyGamesAPIService;
import utils.AlertHelper; 
import utils.JSONParser;

import java.io.IOException;

/**
 * This test class verifies the application's error handling reliability by ensuring
 * that when an API failure occurs, the user is notified through an error alert 
 * within one second. It uses mocking and reflection to simulate failure scenarios
 * and capture interactions with the alert system.
 * 
 * This is in accordance with US-5: The application must handle API errors gracefully, displaying 
 * an error message to users within 1 second if the API is unavailable, and allowing other 
 * functions to remain accessible.
 */
class APIErrorHandlingTest {

    private mobyGamesAPIService apiService;
    private AlertHelper mockAlertHelper;

    /**
     * Sets up a fresh instance of the API service and injects a mocked AlertHelper before each test.
     * Uses reflection via TestUtils to replace the static alertHelper in the mobyGamesAPIService class.
     */
    @BeforeEach
    void setup() {
        apiService = spy(new mobyGamesAPIService());
        mockAlertHelper = mock(AlertHelper.class);
        TestUtils.setStaticField(mobyGamesAPIService.class, "alertHelper", mockAlertHelper);
    }

    /**
     * Simulates an API failure by mocking JSONParser to throw an IOException.
     * Then verifies that an error alert is shown to the user within 1 second
     * using Mockito's timeout feature.
     */
    @Test
    void testAPIErrorDisplaysUserAlertWithinOneSecond() {
        try (MockedStatic<JSONParser> mockedJsonParser = mockStatic(JSONParser.class)) {
            mockedJsonParser.when(() -> JSONParser.sendGetRequest(anyString()))
                            .thenThrow(new IOException("Simulated API failure"));
            try {
                apiService.searchGamesByTitles("thisWillFail");
            } catch (Exception ignored) {}
            verify(mockAlertHelper, timeout(1000)).showError(anyString(), anyString(), anyString());
            System.out.println("Alert displayed within timeout.");
        }
    }
}


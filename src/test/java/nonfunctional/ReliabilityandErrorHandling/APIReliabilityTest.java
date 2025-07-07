package nonfunctional.ReliabilityandErrorHandling;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify; 

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.hc.core5.http.ParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.mockito.MockedStatic;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import controllers.TestUtils;
import services.mobyGamesAPIService;
import utils.AlertHelper;
import utils.JSONParser;
import models.game;

/**
 * APIReliabilityTest evaluates the reliability and performance of the MobyGames API integration
 * in accordance with US-4: The application should retrieve accurate data 90% of the time within
 * 3 seconds and notify the user if response time exceeds the limit.
 */
@TestInstance(Lifecycle.PER_CLASS)
class APIReliabilityTest {

    private mobyGamesAPIService apiService;
    private AlertHelper mockAlertHelper;

    int successfulResponses = 0;
    int totalTests = 3; // Run 3 times for evaluation

    @BeforeEach
    void setup() {
        apiService = new mobyGamesAPIService();
        mockAlertHelper = mock(AlertHelper.class);
        TestUtils.setStaticField(mobyGamesAPIService.class, "alertHelper", mockAlertHelper);
    }

    /**
     * Repeatedly queries the API and verifies response accuracy and timing.
     * At least 90% of responses must be within 3 seconds and contain valid results.
     * @throws ExecutionException 
     * @throws InterruptedException 
     * @throws ParseException 
     */
    @RepeatedTest(3)
    void testAPIResponseTimeAndAccuracy() throws ParseException, InterruptedException, ExecutionException {
        String testTitle = "Elden Ring";

        long start = System.currentTimeMillis();
        List<game> results = apiService.searchGamesByTitles(testTitle);
        long end = System.currentTimeMillis();

        long duration = end - start;
        System.out.println("Request duration: " + duration + "ms, Results: " + results.size());

        if (duration <= 3000 && results != null && !results.isEmpty()) {
            successfulResponses++;
        }

        // Log slow or empty responses
        if (duration > 3000 || results == null || results.isEmpty()) {
            System.err.println("Warning: Slow or invalid response for title '" + testTitle + "'");
        }

        // Final assertion after all repetitions (only on last run)
        if (successfulResponses + 1 == totalTests) {
            double successRate = (successfulResponses * 100.0) / totalTests;
            assertTrue(successRate >= 90, "API success rate should be at least 90%, but was " + successRate + "%");
        }
    }
    
    /**
     * Verifies that the API triggers an alert when the response exceeds 3 seconds.
     */
    @Test
    void testAPIResponseExceeds3SecondsTriggersAlert() throws ParseException, InterruptedException, ExecutionException {
        String testTitle = "Elden Ring";

        try (MockedStatic<JSONParser> mockedJsonParser = mockStatic(JSONParser.class)) {
            // Simulate slow API response
            mockedJsonParser.when(() -> JSONParser.sendGetRequest(anyString())).thenAnswer(invocation -> {
                                Thread.sleep(3100);
                                return "{\"games\": []}";
                            });

            JsonObject fakeJson = new JsonObject();
            fakeJson.add("games", new JsonArray());
            mockedJsonParser.when(() -> JSONParser.parseJson(anyString())).thenReturn(fakeJson);

            List<game> results = apiService.searchGamesByTitles(testTitle);

            assertNotNull(results);
            verify(mockAlertHelper, atLeastOnce()).showError(anyString(), anyString(), anyString());
        }
    }
}

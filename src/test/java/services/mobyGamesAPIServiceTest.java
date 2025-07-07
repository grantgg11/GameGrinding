package services;


import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import models.game;
import org.apache.hc.core5.http.ParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import utils.AlertHelper;
import utils.JSONParser;


import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the mobyGamesAPIService class.
 *
 * This test class verifies:
 * Rate-limiting behavior through enforceRateLimit and simulated interruptions
 * API key loading from resources and fallbacks
 * Behavior of searchGamesByTitles under normal, error, and edge conditions
 * Fetching, parsing, and caching of game and platform data using mocked JSON responses
 * Robustness against IOException, null inputs, malformed JSON, and duplicate entries
 * Correct logging and alert triggering during failures
 *
 * Mocks and static mocking are used extensively for JSONParser, userService,
 * and AlertHelper to simulate real-world API and service behavior.
 */

class mobyGamesAPIServiceTest {

    private final String mockJson = "{\"games\":[{\"game_id\":1,\"title\":\"Test Game\"}]}";
    @Mock
    private AlertHelper mockAlertHelper;

    
    static class TestableService extends mobyGamesAPIService {
        static boolean interrupted = false;
        static boolean called = false;

        protected void sleep(int millis) throws InterruptedException {
            called = true;
            if (interrupted) throw new InterruptedException("Simulated");
        }
    }
    
    /**
     * Uses reflection to invoke private static loadApiKey() method.
     */
    private String invokeLoadApiKey() throws Exception {
        Method method = mobyGamesAPIService.class.getDeclaredMethod("loadApiKey");
        method.setAccessible(true);
        return (String) method.invoke(null);
    }
    
    @BeforeEach
    void setUp() {
    	 MockitoAnnotations.openMocks(this);
    }
    
    /**
     * Verifies that enforceRateLimit does not throw an exception
     * under normal operation using a mocked sleep method.
     */
    @Test
    void testEnforceRateLimit_Normal() {
        mobyGamesAPIService service = new mobyGamesAPIService() {
            boolean called = false;

            @Override
            protected void sleep(int millis) {
                called = true;
            }
        };

        assertDoesNotThrow(service::enforceRateLimit);
    }

    /**
     * Tests that the loadApiKey method successfully loads a valid key
     * and starts with the expected prefix.
     */
    @Test
    void testEnforceRateLimit_Interrupted() {
        mobyGamesAPIService service = new mobyGamesAPIService() {
            @Override
            protected void sleep(int millis) throws InterruptedException {
                throw new InterruptedException("Simulated");
            }
        };

        assertDoesNotThrow(service::enforceRateLimit);
    }

    /**
     * Tests successful loading of the API key using reflection.
     *
     * @throws Exception if the private method cannot be accessed or invoked.
     */
    @Test
    void testLoadApiKey_Success() throws Exception {
        String apiKey = invokeLoadApiKey();
        assertNotNull(apiKey);
        assertTrue(apiKey.startsWith("moby_")); 
    }

    /**
     * Tests that loading an API key with a null InputStream returns null.
     */
    @Test
    void testLoadApiKey_Failure() {
        String result = mobyGamesAPIService.loadApiKey((InputStream) null);
        assertNull(result);
    }

    /**
     * Tests that searchGamesByTitles returns a non-empty game list when the JSON
     * response includes a valid "games" array with game IDs.
     *
     * @throws IOException if the mock JSON parsing fails.
     * @throws ParseException if the mocked JSON format is invalid.
     * @throws ExecutionException if future task execution fails.
     * @throws InterruptedException if execution is interrupted.
     */
    @Test
    void testSearchGamesByTitles_ReturnsGames() throws IOException, ParseException, ExecutionException, InterruptedException {
        try (
            MockedStatic<JSONParser> mockedJsonParser = Mockito.mockStatic(JSONParser.class, CALLS_REAL_METHODS);
            MockedStatic<userService> mockedUserService = Mockito.mockStatic(userService.class)
        ) {
            mockedJsonParser.when(() -> JSONParser.sendGetRequest(anyString()))
                    .thenReturn(mockJson);
            
            JsonObject mockJsonObject = new JsonObject();
            JsonArray gameArray = new JsonArray();
            JsonObject gameEntry = new JsonObject();
            gameEntry.addProperty("game_id", 1);
            gameArray.add(gameEntry);
            mockJsonObject.add("games", gameArray);

            mockedJsonParser.when(() -> JSONParser.parseJson(mockJson)).thenReturn(mockJsonObject);
            userService mockUserServiceInstance = mock(userService.class);
            when(mockUserServiceInstance.getCurrentUserID()).thenReturn(1);
            mockedUserService.when(userService::getInstance).thenReturn(mockUserServiceInstance);
            mobyGamesAPIService service = new mobyGamesAPIService();
            List<game> result = service.searchGamesByTitles("Test");
            assertNotNull(result);
            assertFalse(result.isEmpty());
        }
    }
    
    /**
     * Tests that searchGamesByTitles returns an empty list when the JSON response
     * does not contain a "games" key.
     *
     * @throws Exception if the test setup or parsing fails.
     */
    @Test
    void testSearchGamesByTitles_NoGamesKey() throws Exception {
        try (
            MockedStatic<JSONParser> mockedJsonParser = Mockito.mockStatic(JSONParser.class, CALLS_REAL_METHODS);
            MockedStatic<userService> mockedUserService = Mockito.mockStatic(userService.class)
        ) {
            mockedJsonParser.when(() -> JSONParser.sendGetRequest(anyString()))
                    .thenReturn(mockJson);

            JsonObject mockJsonObject = new JsonObject(); 
            mockedJsonParser.when(() -> JSONParser.parseJson(mockJson)).thenReturn(mockJsonObject);

            userService mockUser = mock(userService.class);
            when(mockUser.getCurrentUserID()).thenReturn(1);
            mockedUserService.when(userService::getInstance).thenReturn(mockUser);

            mobyGamesAPIService service = new mobyGamesAPIService();
            List<game> result = service.searchGamesByTitles("Test");

            assertNotNull(result);
            assertTrue(result.isEmpty()); 
        }
    }
    
    /**
     * Tests that searchGamesByTitles returns an empty list when the JSON API
     * response is an empty string.
     *
     * @throws Exception if any mocked dependency throws during test execution.
     */
    @Test
    void testSearchGamesByTitles_EmptyResponse() throws Exception {
        try (
            MockedStatic<JSONParser> mockedJsonParser = Mockito.mockStatic(JSONParser.class, CALLS_REAL_METHODS);
            MockedStatic<userService> mockedUserService = Mockito.mockStatic(userService.class)
        ) {
            mockedJsonParser.when(() -> JSONParser.sendGetRequest(anyString())).thenReturn("");

            mockedJsonParser.when(() -> JSONParser.parseJson("")).thenReturn(new JsonObject());

            userService mockUser = mock(userService.class);
            when(mockUser.getCurrentUserID()).thenReturn(1);
            mockedUserService.when(userService::getInstance).thenReturn(mockUser);

            mobyGamesAPIService service = new mobyGamesAPIService();
            List<game> result = service.searchGamesByTitles("Test");

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }
    
    /**
     * Tests that searchGamesByTitles handles an IOException gracefully
     * and returns an empty list while suppressing user-facing alerts.
     *
     * @throws Exception if any mock configuration or method call fails.
     */
    @Test
    void testSearchGamesByTitles_IOException() throws Exception {
        try (
            MockedStatic<JSONParser> mockedJsonParser = Mockito.mockStatic(JSONParser.class, CALLS_REAL_METHODS);
            MockedStatic<userService> mockedUserService = Mockito.mockStatic(userService.class)
        ) {
            mockedJsonParser.when(() -> JSONParser.sendGetRequest(anyString())).thenThrow(new IOException("Simulated"));

            userService mockUser = mock(userService.class);
            when(mockUser.getCurrentUserID()).thenReturn(1);
            mockedUserService.when(userService::getInstance).thenReturn(mockUser);

            AlertHelper alertHelperSpy = spy(new AlertHelper());
            doNothing().when(alertHelperSpy).showError(any(), any(), any());
            mobyGamesAPIService.alertHelper = alertHelperSpy;
            mobyGamesAPIService service = new mobyGamesAPIService();
            List<game> result = service.searchGamesByTitles("Test");

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }


    /**
     * Tests that searchGamesByTitles correctly filters out duplicate game IDs from the JSON response.
     *
     * @throws Exception if mocking or processing fails.
     */
    @Test
    void testSearchGamesByTitles_DuplicateGameIDs() throws Exception {
        try (
            MockedStatic<JSONParser> mockedJsonParser = Mockito.mockStatic(JSONParser.class, CALLS_REAL_METHODS);
            MockedStatic<userService> mockedUserService = Mockito.mockStatic(userService.class)
        ) {
            mockedJsonParser.when(() -> JSONParser.sendGetRequest(anyString())).thenReturn(mockJson);

            JsonObject mockJsonObject = new JsonObject();
            JsonArray gameArray = new JsonArray();

            JsonObject game1 = new JsonObject();
            game1.addProperty("game_id", 1);
            JsonObject game2 = new JsonObject();
            game2.addProperty("game_id", 1);

            gameArray.add(game1);
            gameArray.add(game2);

            mockJsonObject.add("games", gameArray);
            mockedJsonParser.when(() -> JSONParser.parseJson(mockJson)).thenReturn(mockJsonObject);

            userService mockUser = mock(userService.class);
            when(mockUser.getCurrentUserID()).thenReturn(1);
            mockedUserService.when(userService::getInstance).thenReturn(mockUser);

            mobyGamesAPIService service = new mobyGamesAPIService();
            List<game> result = service.searchGamesByTitles("Test");

            assertNotNull(result);
            assertFalse(result.isEmpty());
            assertEquals(1, result.size()); 
        }
    }
    
    /**
     * Tests that searchGamesByTitles skips a game entry when fetchGameDetails returns null.
     *
     * @throws Exception if the test setup or method calls fail.
     */
    @Test
    void testSearchGamesByTitles_FetchGameDetailsReturnsNull() throws Exception {
        try (
            MockedStatic<JSONParser> mockedJsonParser = Mockito.mockStatic(JSONParser.class, CALLS_REAL_METHODS);
            MockedStatic<userService> mockedUserService = Mockito.mockStatic(userService.class)
        ) {
            String json = "{\"games\":[{\"game_id\":456}]}";
            JsonObject root = new JsonObject();
            JsonArray gamesArray = new JsonArray();
            JsonObject gameObject = new JsonObject();
            gameObject.addProperty("game_id", 456);
            gamesArray.add(gameObject);
            root.add("games", gamesArray);

            mockedJsonParser.when(() -> JSONParser.sendGetRequest(anyString())).thenReturn(json);
            mockedJsonParser.when(() -> JSONParser.parseJson(json)).thenReturn(root);

            userService mockUser = mock(userService.class);
            when(mockUser.getCurrentUserID()).thenReturn(1);
            mockedUserService.when(userService::getInstance).thenReturn(mockUser);

            mobyGamesAPIService service = spy(new mobyGamesAPIService());
            doReturn(null).when(service).fetchGameDetails(456);

            List<game> result = service.searchGamesByTitles("Test");
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    /**
     * Tests that searchGamesByTitles handles exceptions thrown by fetchGameDetails
     * without crashing and returns an empty list.
     *
     * @throws Exception if the mocking or method call fails.
     */
    @Test
    void testSearchGamesByTitles_FetchGameDetailsThrowsException() throws Exception {
        try (
            MockedStatic<JSONParser> mockedJsonParser = Mockito.mockStatic(JSONParser.class, CALLS_REAL_METHODS);
            MockedStatic<userService> mockedUserService = Mockito.mockStatic(userService.class)
        ) {
            String json = "{\"games\":[{\"game_id\":123}]}";
            JsonObject root = new JsonObject();
            JsonArray gamesArray = new JsonArray();
            JsonObject gameObject = new JsonObject();
            gameObject.addProperty("game_id", 123);
            gamesArray.add(gameObject);
            root.add("games", gamesArray);

            mockedJsonParser.when(() -> JSONParser.sendGetRequest(anyString())).thenReturn(json);
            mockedJsonParser.when(() -> JSONParser.parseJson(json)).thenReturn(root);

            userService mockUser = mock(userService.class);
            when(mockUser.getCurrentUserID()).thenReturn(1);
            mockedUserService.when(userService::getInstance).thenReturn(mockUser);

            mobyGamesAPIService service = spy(new mobyGamesAPIService());
            doThrow(new RuntimeException("Simulated")).when(service).fetchGameDetails(123);

            List<game> result = service.searchGamesByTitles("Test");
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }
    
    ////////////////////////// testing fetchGameDetails //////////////////////////
    
    /**
     * Tests that fetchGameDetails retrieves a game object from the in-memory cache
     * when the game is already cached, avoiding external API calls.
     *
     * @throws Exception if parsing the cached object fails.
     */
    @Test
    void testFetchGameDetails_FromCache() throws Exception {
        int gameID = 1;
        JsonObject cachedGame = new JsonObject();
        cachedGame.addProperty("game_id", gameID);
        mobyGamesAPIService.gameCache.put(gameID, cachedGame);

        mobyGamesAPIService service = Mockito.spy(new mobyGamesAPIService());
        doReturn(new game()).when(service).parseGame(cachedGame);

        game result = service.fetchGameDetails(gameID);
        assertNotNull(result);
    }

    /**
     * Tests that fetchGameDetails successfully retrieves and processes game details
     * by calling the MobyGames API, parsing the result, and enriching with platform data.
     *
     * @throws Exception if any mock, parsing, or processing fails.
     */
    @Test
    void testFetchGameDetails_Successful() throws Exception {
        int gameID = 2;
        String mockJson = "{\"game_id\":2}";

        JsonObject parsedGame = new JsonObject();
        parsedGame.addProperty("game_id", gameID);

        JsonObject mockPlatform = new JsonObject();
        mockPlatform.addProperty("platform_name", "PC");

        try (
            MockedStatic<JSONParser> mockedParser = mockStatic(JSONParser.class);
            MockedStatic<userService> mockedUserService = mockStatic(userService.class)
        ) {
            mockedParser.when(() -> JSONParser.sendGetRequest(anyString())).thenReturn(mockJson);
            mockedParser.when(() -> JSONParser.parseJson(mockJson)).thenReturn(parsedGame);

            userService mockUser = mock(userService.class);
            when(mockUser.getCurrentUserID()).thenReturn(1);
            mockedUserService.when(userService::getInstance).thenReturn(mockUser);

            mobyGamesAPIService service = Mockito.spy(new mobyGamesAPIService());
            doReturn(List.of(mockPlatform)).when(service).fetchGamePlatforms(gameID);
            doReturn(new game()).when(service).parseGame(any());

            game result = service.fetchGameDetails(gameID);
            assertNotNull(result);
        }
    }

    /**
     * Tests that fetchGameDetails returns null when an IOException occurs during the API call.
     *
     * @throws Exception if mocking setup fails
     */
    @Test
    void testFetchGameDetails_IOException() throws Exception {
        int gameID = 3;
        try (
            MockedStatic<JSONParser> mockedParser = mockStatic(JSONParser.class);
            MockedStatic<userService> mockedUserService = mockStatic(userService.class)
        ) {
            mockedParser.when(() -> JSONParser.sendGetRequest(anyString())).thenThrow(new IOException("Simulated"));

            userService mockUser = mock(userService.class);
            when(mockUser.getCurrentUserID()).thenReturn(1);
            mockedUserService.when(userService::getInstance).thenReturn(mockUser);

            mobyGamesAPIService service = Mockito.spy(new mobyGamesAPIService());
            game result = service.fetchGameDetails(gameID);

            assertNull(result);
        }
    }

    ////////////////////////////// testing parseGame //////////////////////////////
    
    /**
     * Tests that parseGame correctly populates a game object from minimal JSON data,
     * using default values for missing fields.
     *
     * @throws Exception if parsing or mocking fails
     */
    @Test
    void testParseGame_MinimalJson_UsesDefaults() throws Exception {
        JsonObject json = new JsonObject();
        json.addProperty("game_id", 1);
        json.addProperty("title", "Minimal Game");

        mobyGamesAPIService service = spy(new mobyGamesAPIService());
        doReturn(Collections.emptyList()).when(service).fetchGamePlatforms(1);

        game result = service.parseGame(json);

        assertEquals(1, result.getGameID());
        assertEquals("Minimal Game", result.getTitle());
        assertEquals("Unknown", result.getDeveloper());
        assertEquals("Unknown", result.getPublisher());
        assertEquals("Unknown", result.getGenre());
        assertEquals("Unknown", result.getPlatform());
        assertNull(result.getReleaseDate());
        assertEquals("", result.getCoverImageUrl());
    }

    /**
     * Tests that parseGame correctly extracts all game details from a fully populated JSON object,
     * including genre, platform, developer, publisher, release date, and cover image.
     *
     * @throws Exception if JSON processing or mock responses fail
     */
    @Test
    void testParseGame_FullJson_ReturnsPopulatedGame() throws Exception {
        JsonObject json = new JsonObject();
        json.addProperty("game_id", 2);
        json.addProperty("title", "Full Game");

        JsonObject genreObj = new JsonObject();
        genreObj.addProperty("genre_name", "Action");
        JsonArray genres = new JsonArray();
        genres.add(genreObj);
        json.add("genres", genres);

        JsonObject cover = new JsonObject();
        cover.addProperty("image", "http://image.url");
        json.add("sample_cover", cover);

        JsonObject platform = new JsonObject();
        platform.addProperty("first_release_date", "2001-05-20");
        JsonArray platformArray = new JsonArray();
        platformArray.add(platform);
        json.add("platforms", platformArray);

        JsonObject platformDetails = new JsonObject();
        platformDetails.addProperty("platform_name", "PC");
        platformDetails.addProperty("developer", "Epic");
        platformDetails.addProperty("publisher", "Games Inc");

        mobyGamesAPIService service = spy(new mobyGamesAPIService());
        doReturn(List.of(platformDetails)).when(service).fetchGamePlatforms(2);

        game result = service.parseGame(json);

        assertEquals("Full Game", result.getTitle());
        assertEquals("Action", result.getGenre());
        assertEquals("Epic", result.getDeveloper());
        assertEquals("Games Inc", result.getPublisher());
        assertEquals("PC", result.getPlatform());
        assertEquals("http://image.url", result.getCoverImageUrl());
        assertEquals(LocalDate.of(2001, 5, 20), result.getReleaseDate());
    }

    /**
     * Tests that parseGame handles an invalid release date format gracefully
     * by setting the release date to null instead of throwing an exception.
     *
     * @throws Exception if mocking or parsing fails
     */
    @Test
    void testParseGame_BadDate_GracefulFallback() throws Exception {
        JsonObject json = new JsonObject();
        json.addProperty("game_id", 3);
        json.addProperty("title", "Broken Date");

        JsonObject badDatePlatform = new JsonObject();
        badDatePlatform.addProperty("first_release_date", "bad-date-format");

        JsonArray platforms = new JsonArray();
        platforms.add(badDatePlatform);
        json.add("platforms", platforms);

        mobyGamesAPIService service = spy(new mobyGamesAPIService());
        doReturn(Collections.emptyList()).when(service).fetchGamePlatforms(3);

        game result = service.parseGame(json);

        assertEquals("Broken Date", result.getTitle());
        assertNull(result.getReleaseDate()); 
    }
    
    /**
     * Tests that parseGame correctly interprets a year-only release date
     * by converting it to January 1st of the specified year.
     *
     * @throws Exception if date parsing or mock logic fails
     */
    @Test
    void testParseGame_ReleaseYearOnly_ParsesToJanFirst() throws Exception {
        JsonObject json = new JsonObject();
        json.addProperty("game_id", 4);
        json.addProperty("title", "Year Only Game");

        JsonObject yearOnlyPlatform = new JsonObject();
        yearOnlyPlatform.addProperty("first_release_date", "1998");

        JsonArray platforms = new JsonArray();
        platforms.add(yearOnlyPlatform);
        json.add("platforms", platforms);

        mobyGamesAPIService service = spy(new mobyGamesAPIService());
        doReturn(Collections.emptyList()).when(service).fetchGamePlatforms(4);

        game result = service.parseGame(json);

        assertEquals(LocalDate.of(1998, 1, 1), result.getReleaseDate());
    }

    ///////////////////// testing fetchGamePlatforms //////////////////////
    
    /**
     * Tests that fetchGamePlatforms correctly returns a list of platform details
     * when the API response is valid and fetchPlatformDetails returns valid data.
     *
     * @throws Exception if mocking or parsing fails
     */
    @Test
    void testFetchGamePlatforms_ValidResponse_ReturnsList() throws Exception {
        int gameID = 1;

        JsonObject platformEntry = new JsonObject();
        platformEntry.addProperty("platform_id", 42);
        JsonArray platformsArray = new JsonArray();
        platformsArray.add(platformEntry);

        JsonObject mockResponse = new JsonObject();
        mockResponse.add("platforms", platformsArray);

        JsonObject detailedPlatform = new JsonObject();
        detailedPlatform.addProperty("platform_name", "PC");

        try (
            MockedStatic<JSONParser> mockedParser = mockStatic(JSONParser.class)
        ) {
            mockedParser.when(() -> JSONParser.sendGetRequest(anyString()))
                        .thenReturn("{\"platforms\":[{\"platform_id\":42}]}");
            mockedParser.when(() -> JSONParser.parseJson(anyString())).thenReturn(mockResponse);

            mobyGamesAPIService service = spy(new mobyGamesAPIService());
            mobyGamesAPIService.alertHelper = mockAlertHelper;
            doReturn(detailedPlatform).when(service).fetchPlatformDetails(gameID, 42);

            List<JsonObject> result = service.fetchGamePlatforms(gameID);

            assertEquals(1, result.size());
            assertEquals("PC", result.get(0).get("platform_name").getAsString());
        }
    }
	
    /**
     * Tests that fetchGamePlatforms handles an IOException gracefully
     * by showing an error alert and returning an empty list.
     *
     * @throws Exception if mocking setup fails
     */
    @Test
    void testFetchGamePlatforms_IOException_ShowsErrorAlert() throws Exception {
        int gameID = 2;

        try (
            MockedStatic<JSONParser> mockedParser = mockStatic(JSONParser.class)
        ) {
            mockedParser.when(() -> JSONParser.sendGetRequest(anyString()))
                        .thenThrow(new IOException("Simulated"));

            mobyGamesAPIService service = spy(new mobyGamesAPIService());
            mobyGamesAPIService.alertHelper = mockAlertHelper;

            List<JsonObject> result = service.fetchGamePlatforms(gameID);

            assertNotNull(result);
            assertTrue(result.isEmpty());

            verify(mockAlertHelper).showError(
                eq("Platform Fetch Error"),
                contains("Unable to load"),
                contains("Retrying")
            );
        }
    }

    /**
     * Tests that fetchGamePlatforms returns an empty list
     * when the JSON response does not contain a "platforms" key.
     *
     * @throws Exception if mocking or parsing fails
     */
    @Test
    void testFetchGamePlatforms_NoPlatformsField_ReturnsEmptyList() throws Exception {
        int gameID = 3;

        JsonObject mockJson = new JsonObject();  
        try (
            MockedStatic<JSONParser> mockedParser = mockStatic(JSONParser.class)
        ) {
            mockedParser.when(() -> JSONParser.sendGetRequest(anyString()))
                        .thenReturn("{}");
            mockedParser.when(() -> JSONParser.parseJson(anyString())).thenReturn(mockJson);

            mobyGamesAPIService service = spy(new mobyGamesAPIService());
            mobyGamesAPIService.alertHelper = mockAlertHelper;

            List<JsonObject> result = service.fetchGamePlatforms(gameID);

            assertTrue(result.isEmpty());
        }
    }

    /**
     * Tests that fetchGamePlatforms skips over platform entries
     * when fetchPlatformDetails returns null for a given platform.
     *
     * @throws Exception if mocking or parsing fails
     */
    @Test
    void testFetchGamePlatforms_NullPlatformDetails_SkipsEntry() throws Exception {
        int gameID = 4;

        JsonObject platformEntry = new JsonObject();
        platformEntry.addProperty("platform_id", 99);
        JsonArray platformsArray = new JsonArray();
        platformsArray.add(platformEntry);

        JsonObject responseJson = new JsonObject();
        responseJson.add("platforms", platformsArray);

        try (
            MockedStatic<JSONParser> mockedParser = mockStatic(JSONParser.class)
        ) {
            mockedParser.when(() -> JSONParser.sendGetRequest(anyString()))
                        .thenReturn("{\"platforms\":[{\"platform_id\":99}]}");
            mockedParser.when(() -> JSONParser.parseJson(anyString()))
                        .thenReturn(responseJson);

            mobyGamesAPIService service = spy(new mobyGamesAPIService());
            mobyGamesAPIService.alertHelper = mockAlertHelper;
            doReturn(null).when(service).fetchPlatformDetails(gameID, 99);

            List<JsonObject> result = service.fetchGamePlatforms(gameID);
            assertTrue(result.isEmpty());  // nothing should be added
        }
    }

    ///////////////////////// testing fetchPlatformDetails /////////////////////////
    
    /**
     * Tests that fetchPlatformDetails returns the cached platform details
     * if the requested platform ID is already stored in the cache.
     *
     * @throws Exception if method invocation fails
     */
    @Test
    void testFetchPlatformDetails_CacheHit_ReturnsCached() throws Exception {
        int gameID = 1, platformID = 10;

        JsonObject cachedPlatform = new JsonObject();
        cachedPlatform.addProperty("platform_name", "PC");

        mobyGamesAPIService service = spy(new mobyGamesAPIService());
        mobyGamesAPIService.alertHelper = mockAlertHelper;

        mobyGamesAPIService.platformCache.put(platformID, cachedPlatform);

        JsonObject result = service.fetchPlatformDetails(gameID, platformID);

        assertSame(cachedPlatform, result);
    }

    /**
     * Tests that fetchPlatformDetails successfully parses a valid JSON response,
     * extracts developer and publisher information, and stores the result in the cache.
     *
     * @throws Exception if mocking, parsing, or the method itself fails
     */
    @Test
    void testFetchPlatformDetails_ValidResponse_ParsesAndCaches() throws Exception {
        int gameID = 2, platformID = 20;

        String json = """
            {
                "releases": [{
                    "companies": [
                        {"role": "Developed by", "company_name": "Studio A"},
                        {"role": "Published by", "company_name": "Publisher X"}
                    ]
                }]
            }
            """;

        JsonObject parsedJson = new JsonObject();
        parsedJson.add("releases", new JsonArray());

        try (
            MockedStatic<JSONParser> mockedParser = mockStatic(JSONParser.class);
            MockedStatic<userService> mockedUserService = mockStatic(userService.class)
        ) {
            mockedParser.when(() -> JSONParser.sendGetRequest(anyString())).thenReturn(json);
            mockedParser.when(() -> JSONParser.parseJson(json)).thenCallRealMethod(); 

            userService mockUser = mock(userService.class);
            when(mockUser.getCurrentUserID()).thenReturn(5);
            mockedUserService.when(userService::getInstance).thenReturn(mockUser);

            mobyGamesAPIService service = spy(new mobyGamesAPIService());
            service.alertHelper = mockAlertHelper;

            JsonObject result = service.fetchPlatformDetails(gameID, platformID);

            assertNotNull(result);
            assertEquals("Studio A", result.get("developer").getAsString());
            assertEquals("Publisher X", result.get("publisher").getAsString());
            assertTrue(service.platformCache.containsKey(platformID));
        }
    }

    /**
     * Tests that fetchPlatformDetails handles an IOException by displaying
     * an error alert and returning null when the API request fails.
     *
     * @throws Exception if mocking fails
     */
    @Test
    void testFetchPlatformDetails_IOException_ShowsAlertAndReturnsNull() throws Exception {
        int gameID = 3, platformID = 30;

        try (
            MockedStatic<JSONParser> mockedParser = mockStatic(JSONParser.class)
        ) {
            mockedParser.when(() -> JSONParser.sendGetRequest(anyString()))
                        .thenThrow(new IOException("Simulated"));

            mobyGamesAPIService service = spy(new mobyGamesAPIService());
            service.alertHelper = mockAlertHelper;

            JsonObject result = service.fetchPlatformDetails(gameID, platformID);

            assertNull(result);
            verify(mockAlertHelper).showError(
                eq("Platform Details Error"),
                contains("Could not load"),
                contains("Please try again")
            );
        }
    }

    /**
     * Tests that fetchPlatformDetails returns default values ("Unknown") for developer
     * and publisher when the JSON response lacks a "companies" section.
     *
     * @throws Exception if mocking or parsing fails
     */
    @Test
    void testFetchPlatformDetails_NoCompanies_ReturnsUnknowns() throws Exception {
        int gameID = 4, platformID = 40;

        String json = """
            {
                "releases": [{}]
            }
            """;

        JsonObject parsed = new JsonObject();
        JsonArray releases = new JsonArray();
        releases.add(new JsonObject()); 
        parsed.add("releases", releases);

        try (
            MockedStatic<JSONParser> mockedParser = mockStatic(JSONParser.class);
            MockedStatic<userService> mockedUserService = mockStatic(userService.class)
        ) {
            mockedParser.when(() -> JSONParser.sendGetRequest(anyString())).thenReturn(json);
            mockedParser.when(() -> JSONParser.parseJson(json)).thenReturn(parsed);

            userService mockUser = mock(userService.class);
            when(mockUser.getCurrentUserID()).thenReturn(99);
            mockedUserService.when(userService::getInstance).thenReturn(mockUser);

            mobyGamesAPIService service = spy(new mobyGamesAPIService());
            service.alertHelper = mockAlertHelper;

            JsonObject result = service.fetchPlatformDetails(gameID, platformID);

            assertNotNull(result);
            assertEquals("Unknown", result.get("developer").getAsString());
            assertEquals("Unknown", result.get("publisher").getAsString());
        }
    }

    

}

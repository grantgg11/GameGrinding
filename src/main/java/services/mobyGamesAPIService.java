package services;

import org.apache.hc.core5.http.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import com.google.gson.*;

import utils.AlertHelper;
import utils.JSONParser;
import models.game;
import reports.APIRequestLogger;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.*;

/**
 * Service class to interact with the MobyGames API for retrieving and parsing video game metadata.
 * Supports game searching, fetching detailed game and platform info, rate limiting, caching, and API logging.
 */
public class mobyGamesAPIService {

    private static final String API_BASE_URL_V1 = "https://api.mobygames.com/v1/";    		// Base URL for MobyGames API
    private static final String API_KEY = loadApiKey();										// API key for authentication
    private static final Lock rateLimitLock = new ReentrantLock();							// Lock for rate limiting	
    private static final ExecutorService apiExecutor = Executors.newFixedThreadPool(3);		// Thread pool for API requests
    private static final Map<Integer, JsonObject> platformCache = new ConcurrentHashMap<>();// Cache for platform details
    private static final Map<Integer, JsonObject> gameCache = new ConcurrentHashMap<>();	// Cache for game details
    private final static APIRequestLogger apiLogger = new APIRequestLogger();				// Logger for API requests
    private static final Logger logger = LoggerFactory.getLogger(mobyGamesAPIService.class); // Logger for service class
    private static final AlertHelper alertHelper = new AlertHelper(); 						// Alert utility

    
    
    /**
     * Enforces a 1200ms delay between API requests to comply with rate limits.
     */
    private static void enforceRateLimit() {
        rateLimitLock.lock();
        try {
            Thread.sleep(1200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            rateLimitLock.unlock();
        }
    }
    
    /**
     * Loads the API key from a properties file.
     *
     * @return the API key string or null if loading fails.
     */
    private static String loadApiKey() {
        try (InputStream input = mobyGamesAPIService.class.getResourceAsStream("/api.properties")) {
            Properties prop = new Properties();
            prop.load(input);
            return prop.getProperty("MOBY_API_KEY");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
	 * Searches for games by title using the MobyGames API and asynchronously fetches detailed game data.
	 *
	 * @param query The title or keyword to search for.
	 * @return A list of game objects matching the search criteria.
	 * @throws ParseException If there is an error parsing the JSON response.
	 * @throws InterruptedException If the thread is interrupted during execution.
	 * @throws ExecutionException If there is an error during asynchronous execution.
	 */
    public List<game> searchGamesByTitles(String query) throws ParseException, InterruptedException, ExecutionException {
        List<game> gameList = new ArrayList<>();
        Set<Integer> processedGameIDs = new HashSet<>();

        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String url = API_BASE_URL_V1 + "games?api_key=" + API_KEY + "&title=" + encodedQuery + "&format=normal";

        enforceRateLimit();
        try {
        	long start = System.nanoTime();
            String jsonResponse = JSONParser.sendGetRequest(url);
            long end = System.nanoTime();
            int responseTimeMs = (int) ((end - start) / 1_000_000);
            String status = (jsonResponse != null && !jsonResponse.isEmpty()) ? "Success" : "Failed";
            Integer errorCode = (status.equals("Failed")) ? 500 : null; 
            int currentUserId = userService.getInstance().getCurrentUserID();
            apiLogger.logAPIRequest(currentUserId, url, responseTimeMs, status, errorCode);

            
            JsonObject jsonObject = JSONParser.parseJson(jsonResponse);

            if (jsonObject.has("games")) {
                JsonArray gamesArray = jsonObject.getAsJsonArray("games");
                List<CompletableFuture<game>> futures = new ArrayList<>();
                Semaphore semaphore = new Semaphore(2); // Limit concurrent requests to 2

                for (JsonElement gameElement : gamesArray) {
                    JsonObject gameObject = gameElement.getAsJsonObject();
                    int gameID = gameObject.get("game_id").getAsInt();

                    if (!processedGameIDs.contains(gameID)) {
                        futures.add(CompletableFuture.supplyAsync(() -> {
                            try {
                                semaphore.acquire();
                                return fetchGameDetails(gameID);
                            } catch (Exception e) {
                                e.printStackTrace();
                            } finally {
                                semaphore.release();
                            }
                            return null;
                        }, apiExecutor));
                        processedGameIDs.add(gameID);
                    }
                }

                for (CompletableFuture<game> future : futures) {
                    game fetchedGame = future.get();
                    if (fetchedGame != null) {
                        gameList.add(fetchedGame);
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Error fetching game data: " + e.getMessage(), e);
            alertHelper.showError( "API Error", "Unable to retrieve games from MobyGames.", "Please try searching again or consider adding the game manually.");
        }

        return gameList;
    }
    
    /**
     * Fetches detailed game information from the API.
     * 
     * @param gameID The ID of the game to fetch.
     * @return A game object containing detailed information about the game.
     * @throws ParseException If there is an error parsing the JSON response.
     */
    private static game fetchGameDetails(int gameID) throws ParseException {
        if (gameCache.containsKey(gameID)) {
            return parseGame(gameCache.get(gameID));
        }

        try {
            String url = API_BASE_URL_V1 + "games/" + gameID + "?api_key=" + API_KEY + "&format=normal";
            enforceRateLimit();
            long start = System.nanoTime();
            String jsonResponse = JSONParser.sendGetRequest(url);
            long end = System.nanoTime();
            int responseTime = (int)((end - start) / 1_000_000);
            String status = (jsonResponse != null && !jsonResponse.isEmpty()) ? "Success" : "Failed";
            Integer errorCode = (status.equals("Failed")) ? 500 : null;
            int currentUserId = userService.getInstance().getCurrentUserID();
            apiLogger.logAPIRequest(currentUserId, url, responseTime, status, errorCode);


            JsonObject gameObject = JSONParser.parseJson(jsonResponse);

            List<JsonObject> platforms = fetchGamePlatforms(gameID);
            JsonArray platformArray = new JsonArray();
            for (JsonObject platformDetails : platforms) {
                platformArray.add(platformDetails);
            }
            gameObject.add("platforms", platformArray);

            return parseGame(gameObject);
        } catch (IOException e) {
           logger.error("Error fetching game details: " + e.getMessage(), e);
           alertHelper.showError( "API Error", "Unable to retrieve games from MobyGames.", "Please try searching again or consider adding the game manually.");     
        } catch (Exception e) {
            logger.error("Error parsing game details: " + e.getMessage(), e);
            alertHelper.showError("Parsing Error", "An unexpected error occurred while loading game data.","Try searching again or manually add the game if the issue persists.");
        }

        return null;
    }
    
    /**
	 * Parses a JSON object to create a game object.
	 * 
	 * @param gameObject The JSON object containing game data.
	 * @return A game object with parsed data.
	 * @throws ParseException If there is an error parsing the JSON object.
	 */
    private static game parseGame(JsonObject gameObject) throws ParseException {
        int gameID = gameObject.has("game_id") ? gameObject.get("game_id").getAsInt() : 0;
        String title = gameObject.has("title") ? gameObject.get("title").getAsString() : "Unknown Title";

        String developer = "Unknown";
        String publisher = "Unknown";
        String platformNames = "Unknown";

        List<JsonObject> platformDetails = fetchGamePlatforms(gameID);
        List<String> platformList = new ArrayList<>();

        for (JsonObject platform : platformDetails) {
            if (platform.has("platform_name")) {
                platformList.add(platform.get("platform_name").getAsString());
            }
            if (platform.has("developer") && !platform.get("developer").getAsString().equals("Unknown")) {
                developer = platform.get("developer").getAsString();
            }
            if (platform.has("publisher") && !platform.get("publisher").getAsString().equals("Unknown")) {
                publisher = platform.get("publisher").getAsString();
            }
        }
        if (!platformList.isEmpty()) {
            platformNames = String.join(", ", platformList);
        }

        LocalDate releaseDate = null;
        if (gameObject.has("platforms")) {
            for (JsonElement e : gameObject.getAsJsonArray("platforms")) {
                JsonObject p = e.getAsJsonObject();
                if (p.has("first_release_date")) {
                    String dateStr = p.get("first_release_date").getAsString();
                    try {
                        releaseDate = dateStr.matches("\\d{4}") ? LocalDate.of(Integer.parseInt(dateStr), 1, 1) : LocalDate.parse(dateStr);
                    } catch (Exception ignored) {}
                }
            }
        }

        String genre = "Unknown";
        if (gameObject.has("genres")) {
            JsonArray genres = gameObject.getAsJsonArray("genres");
            if (genres.size() > 0 && genres.get(0).isJsonObject()) {
                genre = genres.get(0).getAsJsonObject().get("genre_name").getAsString();
            }
        }

        String coverImageURL = "";
        if (gameObject.has("sample_cover")) {
            JsonObject cover = gameObject.getAsJsonObject("sample_cover");
            if (cover.has("image")) coverImageURL = cover.get("image").getAsString();
        }

        return new game(gameID, title, developer, publisher, releaseDate, genre, platformNames, "Not Started", "", coverImageURL);
    }
    
    /**
     * Fetches the platforms for a specific game using the MobyGames API.
     * 
     * @param gameID The ID of the game.
     * @return A list of JSON objects representing the platforms for the game.
     * @throws ParseException If there is an error parsing the JSON response.
     */
    private static List<JsonObject> fetchGamePlatforms(int gameID) throws ParseException {
        List<JsonObject> result = new ArrayList<>();

        try {
            String url = API_BASE_URL_V1 + "games/" + gameID + "/platforms?api_key=" + API_KEY;
            enforceRateLimit();
            String jsonResponse = JSONParser.sendGetRequest(url);
            JsonObject json = JSONParser.parseJson(jsonResponse);

            if (json.has("platforms")) {
                JsonArray array = json.getAsJsonArray("platforms");
                for (JsonElement e : array) {
                    JsonObject platform = e.getAsJsonObject();
                    int pid = platform.get("platform_id").getAsInt();
                    JsonObject full = fetchPlatformDetails(gameID, pid);
                    if (full != null) result.add(full);
                }
            }
        } catch (IOException e) {
            logger.error("Error fetching game platforms: " + e.getMessage(), e);
            alertHelper.showError("Platform Fetch Error", "Unable to load platform data for the game.", "Retrying may help, or manually complete the missing details." );
        }

        return result;
    }
    
    /**
     * Fetches detailed platform information for a specific game and platform ID.
     * 
     * @param gameID The ID of the game.
     * @param platformID The ID of the platform.
     * @return A JSON object containing detailed platform information.
     * @throws ParseException If there is an error parsing the JSON response.
     */
    private static JsonObject fetchPlatformDetails(int gameID, int platformID) throws ParseException {
        if (platformCache.containsKey(platformID)) return platformCache.get(platformID);

        try {
            String url = API_BASE_URL_V1 + "games/" + gameID + "/platforms/" + platformID + "?api_key=" + API_KEY;
            enforceRateLimit();
            long start = System.nanoTime();
            String jsonResponse = JSONParser.sendGetRequest(url);
            long end = System.nanoTime();
            int responseTime = (int)((end - start) / 1_000_000);
            String status = (jsonResponse != null && !jsonResponse.isEmpty()) ? "Success" : "Failed";
            Integer errorCode = (status.equals("Failed")) ? 500 : null;
            int currentUserId = userService.getInstance().getCurrentUserID();
            apiLogger.logAPIRequest(currentUserId, url, responseTime, status, errorCode);

            JsonObject platform = JSONParser.parseJson(jsonResponse);

            String developer = "Unknown", publisher = "Unknown";
            if (platform.has("releases")) {
                for (JsonElement r : platform.getAsJsonArray("releases")) {
                    JsonObject release = r.getAsJsonObject();
                    if (release.has("companies")) {
                        for (JsonElement c : release.getAsJsonArray("companies")) {
                            JsonObject company = c.getAsJsonObject();
                            String role = company.get("role").getAsString();
                            String name = company.get("company_name").getAsString();
                            if (role.equalsIgnoreCase("Developed by")) developer = name;
                            if (role.equalsIgnoreCase("Published by")) publisher = name;
                        }
                    }
                }
            }
            platform.addProperty("developer", developer);
            platform.addProperty("publisher", publisher);

            platformCache.put(platformID, platform);
            return platform;
        } catch (IOException e) {
            logger.error("Error fetching platform details: " + e.getMessage(), e);
            alertHelper.showError("Platform Details Error","Could not load detailed platform information.","Please try again or manually add publisher/developer if needed.");
        }

        return null;
    }

}



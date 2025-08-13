package utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


/**
 * JSONParser provides utility methods for handling JSON-related operations in the GameGrinding application.
 * 
 * This class is responsible for:
 * - Sending HTTP GET requests to retrieve JSON data from external APIs, with built-in retry logic
 *   to handle rate-limiting responses (HTTP 429 Too Many Requests).
 * - Parsing raw JSON strings into JsonObject instances using Gson for easy data extraction.
 * - Providing a retry delay helper method to control wait times between retries.
 * 
 * Key features:
 * - Supports up to a configurable number of retries when API rate limits are encountered.
 * - Uses Java's HttpURLConnection for making HTTP requests.
 * - Integrates Gson's JsonParser for converting raw JSON into navigable JsonObject structures.
 * - Centralizes JSON request and parsing logic, reducing repetitive boilerplate code in services
 *   that interact with APIs.
 */
public class JSONParser {
		
    /**
     * Sends a GET request to the specified URL with automatic retry handling in case of HTTP 429 (Too Many Requests) errors.
     *
     * @param url the target API URL
     * @return the raw JSON response as a String
     * @throws IOException if the request fails due to I/O or if the max retry limit is reached
     */
	public static String sendGetRequest(String url) throws IOException {
	    int maxRetries = 5;
	    int retryDelay = 1100; // Always retry after 1 second

	    for (int attempt = 1; attempt <= maxRetries; attempt++) {
	        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
	        conn.setRequestMethod("GET");
	        conn.setRequestProperty("Accept", "application/json");

	        int responseCode = conn.getResponseCode();
	        System.out.println("Response Code: " + responseCode);

	        if (responseCode == 200) {
	        	// Successful response, parse and return it
	            try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
	                StringBuilder response = new StringBuilder();
	                String inputLine;
	                while ((inputLine = in.readLine()) != null) {
	                    response.append(inputLine);
	                }
	                return response.toString();
	            }
	        } else if (responseCode == 429) {
	        	// Rate limit hit, retry after a short delay
	            System.err.println("API Rate Limit Exceeded. Retrying in " + retryDelay + "ms... (Attempt " + attempt + "/" + maxRetries + ")");
	            try {
	            	delayRetry(retryDelay);
	            } catch (InterruptedException e) {
	                Thread.currentThread().interrupt();
	                throw new IOException("Thread was interrupted during rate limit wait");
	            }
	        } else {
	            conn.disconnect();
	            throw new IOException("HTTP GET request failed with response code: " + responseCode);
	        }
	    }
	    //  Final failure after all retries
	    throw new IOException("Max retries reached for GET request: " + url);
	}

    /**
     * Parses a raw JSON string into a JsonObject.
     *
     * @param jsonString the raw JSON string returned from an API
     * @return a parsed JsonObject that can be navigated with Gson
     */
    public static JsonObject parseJson(String jsonString) {
        return JsonParser.parseString(jsonString).getAsJsonObject();
    }
    
    public static void delayRetry(int millis) throws InterruptedException {
        Thread.sleep(millis);
    }
}

	




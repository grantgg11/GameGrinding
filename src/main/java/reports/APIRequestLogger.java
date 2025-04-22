package reports;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import database.ReportDAO;

/**
 * Utility class for logging API request metrics to the database.
 * Captures request details such as response time, endpoint, status, and error codes.
 * Integrates with ReportDAO for persistence.
 */
public class APIRequestLogger {
	
	private static final Logger logger = LoggerFactory.getLogger(APIRequestLogger.class);
    private final ReportDAO reportDAO;
    
    /** Default constructor */
    public APIRequestLogger() {
        this.reportDAO = new ReportDAO();
    }
    
    /**
	 * Logs an API request to the database with generated request ID.
	 *
	 * @param userId        The ID of the user making the request.
	 * @param endpoint      The API endpoint accessed.
	 * @param responseTime  The time taken for the request (in milliseconds).
	 * @param status        The status of the request (e.g., success, error).
	 * @param errorCode     Optional error code if the request failed.
	 */
    public void logAPIRequest(int userId, String endpoint, int responseTime, String status, Integer errorCode) {
        String requestId = UUID.randomUUID().toString(); // Generate a unique ID for the request
        String sanitizedEndpoint = sanitizeEndpoint(endpoint); // Redact sensitive info
        
        boolean success = reportDAO.insertAPIRequestLog(requestId, userId, endpoint, responseTime, status, errorCode);
        if (!success) {
        	logger.error("Failed to log API request to the database.");
        } else {
            logger.info("API request logged successfully.");
            logger.debug("Request ID: {}, User ID: {}, Endpoint: {}, Response Time: {}ms, Status: {}, Error Code: {}",
                    requestId, userId, sanitizedEndpoint, responseTime, status,
                    errorCode != null ? errorCode.toString() : "N/A");
        }
    }
    
    /**
     * Redacts sensitive query parameters from API endpoints before logging.
     * Currently masks the api_key query parameter.
     *
     * @param url The full API endpoint URL.
     * @return A sanitized version of the URL with sensitive fields masked.
     */
    private String sanitizeEndpoint(String url) {
        return url.replaceAll("(?i)([?&])api_key=[^&]*", "$1api_key=***");
    }

}

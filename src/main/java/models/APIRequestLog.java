package models;


/**
 * Model class representing a log entry for an API request.
 * Used for tracking endpoint usage, performance, and error reporting.
 */
public class APIRequestLog {
    private String requestID;    // Unique identifier for the request
    private int userID;			// ID of the user making the request
    private String timestamp;	// Timestamp of when the request was made
    private String apiEndpoint;	// The API endpoint that was accessed
    private int responseTime;	// Time taken to respond to the request (in milliseconds)
    private String status;		// Status of the request (e.g., success, error)
    private Integer errorCode;	// Error code if the request failed (null if no error)
    
    // Default constructor
    public APIRequestLog() {}
    
    /**
     * Full constructor for manually creating API request log entries.
     *
     * @param requestID    The unique request ID.
     * @param userID       The user ID associated with the request.
     * @param timestamp    The timestamp when the request was made.
     * @param apiEndpoint  The endpoint that was accessed.
     * @param responseTime The time the request took (ms).
     * @param status       The status of the request.
     * @param errorCode    Optional error code if the request failed.
     */
    public APIRequestLog(String requestID, int userID, String timestamp, String apiEndpoint, int responseTime, String status, Integer errorCode) {
        this.requestID = requestID;
        this.userID = userID;
        this.timestamp = timestamp;
        this.apiEndpoint = apiEndpoint;
        this.responseTime = responseTime;
        this.status = status;
        this.errorCode = errorCode;
    }
    
    //------------------ Getters and Setters ------------------
    
    public String getRequestID() {
        return requestID;
    }

    public void setRequestID(String requestID) {
        this.requestID = requestID;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getApiEndpoint() {
        return apiEndpoint;
    }

    public void setApiEndpoint(String apiEndpoint) {
        this.apiEndpoint = apiEndpoint;
    }

    public int getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(int responseTime) {
        this.responseTime = responseTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }
}

package models;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

/**
 * Represents a performance log entry that captures key system metrics during runtime.
 * This class is used to monitor and store performance data like screen load time, 
 * database query time, memory usage, garbage collection events, and logged exceptions.
 */
public class SystemPerformanceLog {
	
    private int reportID;				// Unique identifier for the report
    private LocalDateTime timestamp;	// Timestamp of when the report was generated
    private int screenLoadTime; 		// Time taken to load the screen (in milliseconds)
    private int dbQueryTime;    		// Time taken for database queries (in milliseconds)
    private int memoryUsage;    		// Memory usage (in MB)
    private int gcEvents;				// Number of garbage collection events
    private String exceptionsLogged;	// Any exceptions logged during the report period
    
    /** Default constructor */
    public SystemPerformanceLog() {}
    
    /**
     * Constructor to initialize a new performance log with current timestamp.
     *
     * @param screenLoadTime    Time taken for UI to load (ms).
     * @param dbQueryTime       Time taken for DB query (ms).
     * @param memoryUsage       Memory usage in MB.
     * @param gcEvents          Number of GC events.
     * @param exceptionsLogged  Optional exception log string.
     */
    public SystemPerformanceLog(int screenLoadTime, int dbQueryTime, int memoryUsage, int gcEvents, String exceptionsLogged) {
        this.timestamp = LocalDateTime.now();
        this.screenLoadTime = screenLoadTime;
        this.dbQueryTime = dbQueryTime;
        this.memoryUsage = memoryUsage;
        this.gcEvents = gcEvents;
        this.exceptionsLogged = exceptionsLogged;
    }

    //------------------ Getters and Setters ------------------
    
    public int getReportID() { 
    	return reportID; 
    	}
    
    public void setReportID(int reportID) { 
    	this.reportID = reportID; 
    	}

    public LocalDateTime getTimestamp() { 
    	return timestamp; 
    	}
    

    /**
     * Sets the timestamp from a string. If parsing fails, it falls back to the current time.
     * 
     * @param timestamp String in ISO_LOCAL_DATE_TIME format.
     */
    public void setTimestamp(String timestamp) {
        try {
            this.timestamp = LocalDateTime.parse(timestamp);
        } catch (DateTimeParseException e) {
            System.err.println("Failed to parse timestamp: " + timestamp);
            e.printStackTrace();
            this.timestamp = LocalDateTime.now(); // fallback value
        }
    }

    public int getScreenLoadTime() { 
    	return screenLoadTime; 
    	}
    
    public void setScreenLoadTime(int screenLoadTime) { 
    	this.screenLoadTime = screenLoadTime; 
    	}

    public int getDbQueryTime() { 
    	return dbQueryTime; 
    	}
    
    public void setDbQueryTime(int dbQueryTime) { 
    	this.dbQueryTime = dbQueryTime; 
    	}

    public int getMemoryUsage() { 
    	return memoryUsage; 
    	}
    
    public void setMemoryUsage(int memoryUsage) { 
    	this.memoryUsage = memoryUsage; 
    	}

    public int getGcEvents() { 
    	return gcEvents; 
    	}
    
    public void setGcEvents(int gcEvents) { 
    	this.gcEvents = gcEvents; 
    	}

    public String getExceptionsLogged() { 
    	return exceptionsLogged; 
    	}
    
    public void setExceptionsLogged(String exceptionsLogged) { 
    	this.exceptionsLogged = exceptionsLogged; 
    	}
}

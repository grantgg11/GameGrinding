package controllers;

import reports.PerformanceTracker;

/**
 * BaseController provides a foundation for all controller classes that require user-specific data loading and performance tracking.
 * 
 * It manages the common task of setting the logged-in user ID and measures performance for screen load and database interactions.
 */
public abstract class BaseController {
	
    protected int loggedInUserID; // ID of the currently logged-in user
    protected PerformanceTracker tracker = new PerformanceTracker(); // Tracks system performance metrics like load time, memory, and GC events. 
    
    /**
     * Sets the user ID for the current session and triggers data loading.
     * 
     * Also tracks the time taken to load the screen and any database-specific
     * user data, along with system resource usage and exceptions if any.
     * 
     * @param userID The ID of the currently logged-in user
     */
    public void setUserID(int userID) {
        long screenStart = System.nanoTime();
        System.out.println("BaseController.setUserID() with user ID: " + userID);

        String exceptionMessage = null;
        int dbQueryTime = 0;

        if (userID > 0) {
            this.loggedInUserID = userID;

            try {
                long dbStart = System.nanoTime();
                onUserDataLoad();  // load user-specific data
                long dbEnd = System.nanoTime();
                dbQueryTime = (int) ((dbEnd - dbStart) / 1_000_000);
            } catch (Exception e) {
                exceptionMessage = e.getMessage();
                e.printStackTrace();
            }

            long screenEnd = System.nanoTime();
            int screenLoadTime = (int) ((screenEnd - screenStart) / 1_000_000);

            tracker.logPerformanceData(
                screenLoadTime,
                dbQueryTime,
                tracker.getMemoryUsage(),
                tracker.getGCEvents(),
                exceptionMessage
            );

        } else {
            System.out.println("Invalid User ID provided to controller.");
        }
    }
    /**
     * Abstract method that child controllers implement to load
     * user-specific data when the user ID is set. 
     */
    protected abstract void onUserDataLoad();
    
    public int getUserID() {
		System.out.println("Current logged-in user ID: " + loggedInUserID);
		return loggedInUserID;
	}
}

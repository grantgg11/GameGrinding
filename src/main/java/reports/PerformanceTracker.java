package reports;

import models.SystemPerformanceLog;
import database.ReportDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is responsible for tracking and logging system performance metrics.
 * Includes memory usage, garbage collection events, screen load time, and database query durations.
 * Results are persisted into the database via ReportDAO.
 */
public class PerformanceTracker {

    private static final Logger logger = LoggerFactory.getLogger(PerformanceTracker.class);
    private final ReportDAO dao;
    
    // Default constructor
    public PerformanceTracker() {
        this.dao = new ReportDAO();
    }

    // Testable constructor
    public PerformanceTracker(ReportDAO dao) {
        this.dao = dao;
    }
    /**
     * Logs a system performance report to the database and outputs result to the log.
     *
     * @param screenLoadTime Time taken to load the screen (in milliseconds).
     * @param dbQueryTime    Time taken for database queries (in milliseconds).
     * @param memoryUsage    Memory usage at the time of logging (in megabytes).
     * @param gcEvents       Number of garbage collection events observed.
     * @param exceptionsLog  Optional log message or stack trace summary.
     */
    public void logPerformanceData(int screenLoadTime, int dbQueryTime, int memoryUsage, int gcEvents, String exceptionsLog) {
        SystemPerformanceLog log = new SystemPerformanceLog(screenLoadTime, dbQueryTime, memoryUsage, gcEvents, exceptionsLog);
        boolean success = dao.insertSystemPerformanceLog(log);

        if (success) {
            logger.info("System performance log inserted: ScreenLoadTime={}ms, DBQueryTime={}ms, Memory={}MB, GCEvents={}, Notes={}",
                    screenLoadTime, dbQueryTime, memoryUsage, gcEvents, exceptionsLog);
        } else {
            logger.error("Failed to insert system performance log.");
        }
    }
    
    /**
     * Returns the amount of memory currently used by the JVM.
     *
     * @return Used memory in megabytes.
     */
    public int getMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long usedMemoryBytes = runtime.totalMemory() - runtime.freeMemory();
        return (int) (usedMemoryBytes / (1024 * 1024)); // Convert bytes to MB
    }
    
    /**
     * Returns the total number of garbage collection (GC) events that have occurred.
     * Utilizes the Java Management API via ManagementFactory.
     *
     * @return Total number of GC events (across all GC beans).
     */
    public int getGCEvents() {
        return java.lang.management.ManagementFactory.getGarbageCollectorMXBeans().stream().mapToInt(bean -> (int) bean.getCollectionCount()).sum();
    }

}

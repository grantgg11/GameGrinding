package reports;

import models.DatabaseIntegrityReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import database.ReportDAO;

/**
 * Handles the logging and analysis of database integrity metrics.
 * Generates DatabaseIntegrityReport objects and persists them via ReportDAO.
 * Uses SLF4J for logging to capture report status and error details.
 */
public class DatabaseIntegrityLogger {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseIntegrityLogger.class);
    private final ReportDAO reportDAO;
    
    /**
	 * Default constructor initializes the ReportDAO.
	 */
    public DatabaseIntegrityLogger() {
        this.reportDAO = new ReportDAO();
    }
    
    /**
     * Creates and inserts a database integrity report using provided values.
     *
     * @param totalEntries        Total number of entries in the Game table.
     * @param missingDataEntries  Count of entries missing required fields.
     * @param duplicationCount    Count of duplicate entries.
     * @param fkIntegrity         Whether foreign key constraints are intact.
     */
    public void logDatabaseIntegrity(int totalEntries, int missingDataEntries, int duplicationCount, boolean fkIntegrity) {
        try {
            DatabaseIntegrityReport report = new DatabaseIntegrityReport();
            report.setTotalEntries(totalEntries);
            report.setMissingDataEntries(missingDataEntries);
            report.setDuplicationCount(duplicationCount);
            report.setFkIntegrity(fkIntegrity);

            reportDAO.insertDatabaseIntegrityReport(report);
            logger.info("Logged database integrity report: {}", report);

        } catch (Exception e) {
            logger.error("Failed to log database integrity report", e);
        }
    }
    
    /**
     * Automatically analyzes the database for integrity issues and logs a new report.
     * This includes counting:
     * - total game entries
     * - missing data
     * - duplicate titles
     * - orphaned relationships
     * - users with missing emails
     */
    public void analyzeAndLogDatabaseIntegrity() {
        try {
            int totalEntries = reportDAO.countTotalGameEntries();
            int missingEntries = reportDAO.countMissingDataEntries();
            int duplicationCount = reportDAO.countDuplicateGames();
            boolean foreignKeyIntegrity = reportDAO.checkForeignKeyIntegrity();
            int missingUserEmails = reportDAO.countUsersWithMissingEmails();
            int orphanedCollections = reportDAO.countOrphanedGameCollections();

            DatabaseIntegrityReport report = new DatabaseIntegrityReport();
            report.setTotalEntries(totalEntries);
            report.setMissingDataEntries(missingEntries + missingUserEmails);
            report.setDuplicationCount(duplicationCount + orphanedCollections); 
            report.setFkIntegrity(foreignKeyIntegrity);

            reportDAO.insertDatabaseIntegrityReport(report);
            logger.info("Database integrity analysis complete: {}", report);

        } catch (Exception e) {
            System.err.println("Failed to analyze database integrity: " + e.getMessage());
            logger.error("Failed to analyze database integrity", e);
        }
    }
}


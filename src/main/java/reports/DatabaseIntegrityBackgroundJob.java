package reports;


/**
 * A background job that triggers a database integrity check.
 * Designed to run on application start to analyze and log
 * potential issues like missing data, duplicates, or foreign key violations.
 */
public class DatabaseIntegrityBackgroundJob {

    private final DatabaseIntegrityLogger logger = new DatabaseIntegrityLogger();
    
    /**
     * Executes the database integrity check in the background.
     * This method triggers logging of key integrity metrics.
     */
    public void runIntegrityCheck() {
        System.out.println("Running database integrity check in background...");
        logger.analyzeAndLogDatabaseIntegrity();
    }
}

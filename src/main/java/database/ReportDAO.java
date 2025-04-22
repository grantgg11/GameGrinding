package database;

import models.APIRequestLog;
import models.DatabaseIntegrityReport;
import models.SystemPerformanceLog;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DAO class responsible for inserting, retrieving, and analyzing logs and reports 
 * from the GameGrinding database, including performance, API usage, and data integrity.
 */
public class ReportDAO {

    private final DatabaseManager dbManager = new DatabaseManager(); 
    private static final Logger logger = LoggerFactory.getLogger(ReportDAO.class);
    
    /**
     * Inserts a system performance log entry into the SystemPerformanceReport table.
     *
     * @param log The SystemPerformanceLog object containing performance data.
     * @return true if insertion was successful, false otherwise.
     */
    public boolean insertSystemPerformanceLog(SystemPerformanceLog log) {
        String sql = "INSERT INTO SystemPerformanceReport (Timestamp, ScreenLoadTime, DbQueryTime, MemoryUsage, GcEvents, ExceptionsLogged)" +
                     "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, log.getTimestamp().toString());
            stmt.setInt(2, log.getScreenLoadTime());
            stmt.setInt(3, log.getDbQueryTime());
            stmt.setInt(4, log.getMemoryUsage());
            stmt.setInt(5, log.getGcEvents());
            stmt.setString(6, log.getExceptionsLogged());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Failed to insert system performance log into database", e);
            return false;
        }
    }
    
    /**
     * Inserts a record of an API request into the APIRequestLogs table.
     * This method is used to track API usage and performance.
     *
     * @param requestId    Unique request identifier.
     * @param userId       The user who made the request.
     * @param apiEndpoint  The endpoint accessed.
     * @param responseTime Time taken for the API to respond (ms).
     * @param status       The result (e.g., success/failure).
     * @param errorCode    Optional error code (nullable).
     * @return true if insertion was successful, false otherwise.
     */
    public boolean insertAPIRequestLog(String requestId, int userId, String apiEndpoint, int responseTime, String status, Integer errorCode) {
        String query = "INSERT INTO APIRequestLogs (RequestID, UserID, Timestamp, APIEndpoint, ResponseTime, Status, ErrorCode) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, requestId);
            stmt.setInt(2, userId);
            stmt.setString(3, LocalDateTime.now().toString());
            stmt.setString(4, apiEndpoint);
            stmt.setInt(5, responseTime);
            stmt.setString(6, status);
            if (errorCode != null) {
                stmt.setInt(7, errorCode);
            } else {
                stmt.setNull(7, Types.INTEGER);
            }

            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            logger.error("Failed to insert API request log into database", e);
            return false;
        }
    }
    
    /**
     * Inserts a database integrity report based on analysis of data issues.
     * This method is used to track the health of the database.
     *
     * @param report A DatabaseIntegrityReport object with summary metrics.
     */
    public void insertDatabaseIntegrityReport(DatabaseIntegrityReport report) {
        String sql = "INSERT INTO DatabaseIntegrityReport (TotalEntries, MissingDataEntries, DuplicationCount, FkIntegrity) " +
                     "VALUES (?, ?, ?, ?)";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, report.getTotalEntries());
            stmt.setInt(2, report.getMissingDataEntries());
            stmt.setInt(3, report.getDuplicationCount());
            stmt.setBoolean(4, report.isFkIntegrity());

            stmt.executeUpdate();

        } catch (SQLException e) {
            logger.error("Failed to insert DatabaseIntegrityReport into database", e);
			 
            System.err.println("Error inserting DatabaseIntegrityReport: " + e.getMessage());
        }
    }
    
    /**
     * Counts all entries in the Game table.
     *
     * @return Total number of game records.
     */
    public int countTotalGameEntries() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Game";
        try (Connection conn = dbManager.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    /**
     * Counts entries in the Game table missing a title.
     *
     * @return Number of games with null or empty titles.
     */
    public int countMissingDataEntries() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Game WHERE title IS NULL OR title = ''";
        try (Connection conn = dbManager.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    /**
     * Counts entries in the Game table missing a title.
     *
     * @return Number of games with null or empty titles.
     */
    public int countDuplicateGames() throws SQLException {
        String sql = "SELECT COUNT(*) FROM (SELECT title FROM Game GROUP BY title HAVING COUNT(*) > 1)";
        try (Connection conn = dbManager.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    /**
     * Checks if all foreign key constraints are satisfied in the database.
     *
     * @return true if no violations found, false otherwise.
     */
    public boolean checkForeignKeyIntegrity() throws SQLException {
        String sql = "PRAGMA foreign_key_check";
        try (Connection conn = dbManager.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            return !rs.next(); // If no rows returned, FKs are fine
        }
    }
    
    /**
	 * Counts users with missing or empty email addresses.
	 *
	 * @return Number of users with null or empty email addresses.
	 */
    public int countUsersWithMissingEmails() throws SQLException {
        String sql = "SELECT COUNT(*) FROM user WHERE email IS NULL OR TRIM(email) = ''";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            return rs.next() ? rs.getInt(1) : 0;
        }
    }
    
    /**
     * Counts orphaned entries in UserGameCollection (i.e., missing corresponding user or game).
     *
     * @return Number of orphaned collection records.
     */
    public int countOrphanedGameCollections() throws SQLException {
        String sql = """
            SELECT COUNT(*)
            FROM UserGameCollection gc
            LEFT JOIN User u ON gc.UserID = u.UserID
            LEFT JOIN Game g ON gc.GameID = g.GameID
            WHERE u.UserID IS NULL OR g.GameID IS NULL
        """;
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            return rs.next() ? rs.getInt(1) : 0;
        }
    }
    
    /**
     * Retrieves all system performance logs from the database.
     *
     * @return List of SystemPerformanceLog objects.
     */
    public List<SystemPerformanceLog> getAllSystemPerformanceReports() {
        List<SystemPerformanceLog> list = new ArrayList<>();
        String query = "SELECT * FROM SystemPerformanceReport";
        try (Connection conn = dbManager.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                SystemPerformanceLog report = new SystemPerformanceLog();
                report.setReportID(rs.getInt("ReportID"));
                report.setTimestamp(rs.getString("Timestamp"));
                report.setScreenLoadTime(rs.getInt("ScreenLoadTime"));
                report.setDbQueryTime(rs.getInt("DbQueryTime"));
                report.setMemoryUsage(rs.getInt("MemoryUsage"));
                report.setGcEvents(rs.getInt("GcEvents"));
                report.setExceptionsLogged(rs.getString("ExceptionsLogged"));
                list.add(report);
            }
        } catch (SQLException e) {
            logger.error("Failed to retrieve system performance reports from database", e);
        }
        return list;
    }
    
    /**
     * Retrieves all API request logs from the database.
     * 
     * @return List of APIRequestLog objects.
     */
    public List<APIRequestLog> getAllAPIRequestLogs() {
        List<APIRequestLog> list = new ArrayList<>();
        String query = "SELECT * FROM APIRequestLogs";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
        	
            while (rs.next()) {
            	Integer errorCode = rs.getObject("ErrorCode") != null ? rs.getInt("ErrorCode") : null;
                APIRequestLog log = new APIRequestLog(
                    rs.getString("RequestID"),
                    rs.getInt("UserID"),
                    rs.getString("Timestamp"),
                    rs.getString("APIEndpoint"),
                    rs.getInt("ResponseTime"),
                    rs.getString("Status"),
                    errorCode
                );
                list.add(log);
            }
        } catch (SQLException e) {
        	logger.error("Failed to retrieve API request logs from database", e);
        }
        return list;
    }
    
    /**
	 * Retrieves all database integrity reports from the database.
	 * 
	 * @return List of DatabaseIntegrityReport objects.
	 */
    public List<DatabaseIntegrityReport> getAllDatabaseIntegrityReports() {
        List<DatabaseIntegrityReport> reports = new ArrayList<>();
        String query = "SELECT * FROM DatabaseIntegrityReport ORDER BY Timestamp DESC";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                DatabaseIntegrityReport report = new DatabaseIntegrityReport();
                report.setReportID(rs.getInt("ReportID"));
                report.setTimestamp(rs.getString("Timestamp"));
                report.setTotalEntries(rs.getInt("TotalEntries"));
                report.setMissingDataEntries(rs.getInt("MissingDataEntries"));
                report.setDuplicationCount(rs.getInt("DuplicationCount"));
                report.setFkIntegrity(rs.getBoolean("FkIntegrity"));

                reports.add(report);
            }

        } catch (SQLException e) {
            logger.error("Failed to retrieve database integrity reports from database", e);
            System.err.println("Error retrieving database integrity reports: " + e.getMessage());
        }

        return reports;
    }       
}

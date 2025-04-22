package utils;

import models.APIRequestLog;
import models.DatabaseIntegrityReport;
import models.SystemPerformanceLog;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Utility class for exporting different types of logs and reports to CSV format.
 * This class includes methods to export:
 * - System performance logs
 * - API request logs
 * - Database integrity reports
 */
public class CSVExporter {
	
	/**
	 *  Exports a list of SystemPerformanceLog entries to a CSV file.
	 * 
     * @param logs the list of system performance logs to export
     * @param filePath the file path where the CSV file will be saved
     * @throws IOException if an I/O error occurs while writing the file
	 */
    public void exportSystemPerformanceLogs(List<SystemPerformanceLog> logs, String filePath) throws IOException {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write("ReportID,Timestamp,ScreenLoadTime,DbQueryTime,MemoryUsage,GcEvents,ExceptionsLogged\n");
            for (SystemPerformanceLog log : logs) {
                writer.write(String.format("%d,%s,%d,%d,%d,%d,%s\n",
                        log.getReportID(),
                        log.getTimestamp(),
                        log.getScreenLoadTime(),
                        log.getDbQueryTime(),
                        log.getMemoryUsage(),
                        log.getGcEvents(),
                        log.getExceptionsLogged() != null ? log.getExceptionsLogged() : ""));
            }
        }
    }
    
    /**
     * Exports a list of APIRequestLog entries to a CSV file.
     *
     * @param logs     the list of API request logs to export
     * @param filePath the file path where the CSV file will be saved
     * @throws IOException if an I/O error occurs while writing the file
     */
    public void exportAPIRequestLogs(List<APIRequestLog> logs, String filePath) throws IOException {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write("RequestID,UserID,Timestamp,APIEndpoint,ResponseTime,Status,ErrorCode\n");
            for (APIRequestLog log : logs) {
                writer.write(String.format("%s,%d,%s,%s,%d,%s,%s\n",
                        log.getRequestID(),
                        log.getUserID(),
                        log.getTimestamp(),
                        log.getApiEndpoint(),
                        log.getResponseTime(),
                        log.getStatus(),
                        log.getErrorCode() != null ? log.getErrorCode().toString() : ""));
            }
        }
    }
    
    /**
     * Exports a list of DatabaseIntegrityReport entries to a CSV file.
     *
     * @param reports  the list of database integrity reports to export
     * @param filePath the file path where the CSV file will be saved
     * @throws IOException if an I/O error occurs while writing the file
     */
    public void exportDatabaseIntegrityReports(List<DatabaseIntegrityReport> reports, String filePath) throws IOException {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write("ReportID,Timestamp,TotalEntries,MissingDataEntries,DuplicationCount,FkIntegrity\n");
            for (DatabaseIntegrityReport report : reports) {
                writer.write(String.format("%d,%s,%d,%d,%d,%b\n",
                        report.getReportID(),
                        report.getTimestamp(),
                        report.getTotalEntries(),
                        report.getMissingDataEntries(),
                        report.getDuplicationCount(),
                        report.isFkIntegrity()));
            }
        }
    }
}

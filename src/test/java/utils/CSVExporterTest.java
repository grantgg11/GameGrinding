package utils;

import models.APIRequestLog;
import models.DatabaseIntegrityReport;
import models.SystemPerformanceLog;

import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime; 
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the CSVExporter utility class.
 *
 * These tests verify that data from SystemPerformanceLog, APIRequestLog, and 
 * DatabaseIntegrityReport objects is correctly exported to CSV files.
 * 
 * Each test uses a temporary file for safe, isolated testing.
 */
class CSVExporterTest {

    private CSVExporter exporter;
    private Path tempFile;

    /**
     * Sets up a fresh CSVExporter instance and a temporary file before each test.
     *
     * @throws IOException if the temporary file cannot be created
     */
    @BeforeEach
    void setUp() throws IOException {
        exporter = new CSVExporter();
        tempFile = Files.createTempFile("csv_export", ".csv");
    }

    /**
     * Cleans up by deleting the temporary file after each test.
     *
     * @throws IOException if the temporary file cannot be deleted
     */
    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(tempFile);
    }

    /**
     * Tests that the exportSystemPerformanceLogs method writes logs in the correct CSV format.
     * Verifies header correctness and proper formatting of a single log entry.
     *
     * @throws IOException if file reading or writing fails during the test
     */
    @Test
    void testExportSystemPerformanceLogs_WritesCorrectCSVFormat() throws IOException {
        SystemPerformanceLog log = new SystemPerformanceLog(1000, 300, 512, 2, "NullPointerException");
        log.setReportID(1);
        log.setTimestamp(LocalDateTime.of(2025, 5, 27, 15, 0).toString()); 

        exporter.exportSystemPerformanceLogs(List.of(log), tempFile.toString());

        List<String> lines = Files.readAllLines(tempFile);
        assertEquals(2, lines.size(), "CSV should contain header and one data line");

        assertEquals("ReportID,Timestamp,ScreenLoadTime,DbQueryTime,MemoryUsage,GcEvents,ExceptionsLogged", lines.get(0));

        String expectedDataLine = "1,2025-05-27T15:00,1000,300,512,2,NullPointerException";
        assertEquals(expectedDataLine, lines.get(1), "Data line should match expected format and values");
        assertTrue(lines.get(1).startsWith("1,2025-05-27T15:00"), "Timestamp and data should match");
        assertTrue(lines.get(1).contains("1000,300,512,2,NullPointerException"), "Data fields should be accurate");
    }

    /**
     * Tests that the exportAPIRequestLogs method writes logs in the correct CSV format.
     * Confirms proper formatting of all API request log fields.
     *
     * @throws IOException if file reading or writing fails during the test
     */
    @Test
    void testExportAPIRequestLogs_WritesCorrectCSVFormat() throws IOException {
        APIRequestLog log = new APIRequestLog("req123", 101, "2025-05-27T15:30:00", "/games", 450, "200 OK", null);

        exporter.exportAPIRequestLogs(List.of(log), tempFile.toString());

        List<String> lines = Files.readAllLines(tempFile);
        assertEquals(2, lines.size());
        assertEquals("RequestID,UserID,Timestamp,APIEndpoint,ResponseTime,Status,ErrorCode", lines.get(0));
        assertTrue(lines.get(1).contains("req123,101,2025-05-27T15:30:00,/games,450,200 OK,"));
    }

    /**
     * Tests that the exportDatabaseIntegrityReports method writes reports in the correct CSV format.
     * Verifies field order, data accuracy, and header content.
     *
     * @throws IOException if file reading or writing fails during the test
     */
    @Test
    void testExportDatabaseIntegrityReports_WritesCorrectCSVFormat() throws IOException {
        DatabaseIntegrityReport report = new DatabaseIntegrityReport(501, "2025-05-27T16:00:00", 1000, 5, 2, true);

        exporter.exportDatabaseIntegrityReports(List.of(report), tempFile.toString());

        List<String> lines = Files.readAllLines(tempFile);
        assertEquals(2, lines.size());
        assertEquals("ReportID,Timestamp,TotalEntries,MissingDataEntries,DuplicationCount,FkIntegrity", lines.get(0));
        assertTrue(lines.get(1).contains("501,2025-05-27T16:00:00,1000,5,2,true"));
    }
}

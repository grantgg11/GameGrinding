package models;

/**
 * Represents a database integrity report used to summarize the health of
 * the application's database, including checks for missing data,
 * duplication, and foreign key integrity.
 */
public class DatabaseIntegrityReport {
    private int reportID;			// Unique identifier for the report
    private String timestamp;		// Timestamp of when the report was generated
    private int totalEntries;		// Total number of entries in the database
    private int missingDataEntries;	// Number of entries with missing data
    private int duplicationCount;	// Number of duplicate entries
    private boolean fkIntegrity;	// Flag indicating if foreign key integrity is maintained
    
    /**
     * Default constructor.
     */
    public DatabaseIntegrityReport() {}
    
    /**
	 * Constructor for creating a report before persisting to the database.
	 *
	 * @param totalEntries        Total number of entries in the database.
	 * @param missingDataEntries  Number of entries with missing data.
	 * @param duplicationCount    Number of duplicate entries.
	 * @param fkIntegrity         Flag indicating if foreign key integrity is maintained.
	 */
    public DatabaseIntegrityReport(int totalEntries, int missingDataEntries, int duplicationCount, boolean fkIntegrity) {
        this.totalEntries = totalEntries;
        this.missingDataEntries = missingDataEntries;
        this.duplicationCount = duplicationCount;
        this.fkIntegrity = fkIntegrity;
    }
    
    /**
     * Full constructor including primary key and timestamp, typically used when retrieving reports from the database.
     *
     * @param reportID            Unique report ID.
     * @param timestamp           Timestamp of the report.
     * @param totalEntries        Total entries in the database.
     * @param missingDataEntries  Count of entries with missing data.
     * @param duplicationCount    Number of duplicate entries.
     * @param fkIntegrity         Result of foreign key check.
     */
	public DatabaseIntegrityReport(int reportID, String timestamp, int totalEntries, int missingDataEntries, int duplicationCount, boolean fkIntegrity) {
		this.reportID = reportID;
		this.timestamp = timestamp;
		this.totalEntries = totalEntries;
		this.missingDataEntries = missingDataEntries;
		this.duplicationCount = duplicationCount;
		this.fkIntegrity = fkIntegrity;
	}
	
	//------------------ Getters and Setters ------------------

    public int getReportID() { 
    	return reportID; 
    	
    }
    public void setReportID(int reportID) { 
    	this.reportID = reportID; 
    	}

    public String getTimestamp() { 
    	return timestamp; 
    	}
    public void setTimestamp(String timestamp) { 
    	this.timestamp = timestamp; 
    	}

    public int getTotalEntries() { 
    	return totalEntries; 
    	   }
    
    public void setTotalEntries(int totalEntries) { 
    	this.totalEntries = totalEntries; 
    	}

    public int getMissingDataEntries() { 
    	return missingDataEntries; 
    	}
    
    public void setMissingDataEntries(int missingDataEntries) { 
    	this.missingDataEntries = missingDataEntries; 
    	}

    public int getDuplicationCount() { 
    	return duplicationCount; 
    	}
    
    public void setDuplicationCount(int duplicationCount) { 
    	this.duplicationCount = duplicationCount; 
    	}

    public boolean isFkIntegrity() { 
    	return fkIntegrity; 
    	}
    
    public void setFkIntegrity(boolean fkIntegrity) { 
    	this.fkIntegrity = fkIntegrity; 
    	}
    
    /**
     * Returns a summary string of the report.
     */
    @Override
    public String toString() {
        return String.format(
            "DatabaseIntegrityReport{Total=%d, Missing=%d, Duplicates=%d, ForeignKeyOK=%s}",
            totalEntries, missingDataEntries, duplicationCount, fkIntegrity ? "YES" : "NO"
        );
    }

}

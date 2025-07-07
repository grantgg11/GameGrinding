package models;


import java.time.LocalDate;
import java.util.Objects;

/**
 * Represents a video game in the GameGrinding application.
 * Stores metadata such as developer, platform, completion status, and optional cover art.
 */
public class game {
	
	private int gameID;				// Unique identifier for the game
	private String title;			// Title of the game
	private String developer;		// Developer of the game
	private String publisher;		// Publisher of the game
	private LocalDate  releaseDate;	// Release date of the game
	private String genre;			// Genre of the game
	private String platform;		// Platform the game is played on
	private String completionStatus;// Completion status of the game
	private String notes;			// User notes about the game
	private String coverImageURL;	// URL for the cover image of the game
	
	/** Default constructor */
	public game() { }
	
    /**
     * Constructor for creating a fully defined game object with ID.
     * 
     * @param gameID            Unique ID of the game.
     * @param title             Game title.
     * @param developer         Developer name.
     * @param publisher         Publisher name.
     * @param releaseDate       Date of release.
     * @param genre             Genre category.
     * @param platform          Target platform.
     * @param completionStatus  Current user completion status.
     * @param notes             Optional notes from user.
     * @param coverImageURL     Image URL for cover art.
     */
	public game(int gameID, String title, String developer, String publisher, LocalDate releaseDate,
			String genre, String platform, String completionStatus, String notes, String coverImageURL) {
		this.gameID = gameID;
		this.title = title;
		this.developer = developer;
		this.publisher = publisher;
		this.releaseDate = releaseDate;
		this.genre = genre; 
		this.platform = platform;
		this.completionStatus = completionStatus;
		this.notes = notes;
		this.coverImageURL = coverImageURL; 
		}
	
	
	/**
	 * Constructor for creating a game object without an ID.
	 * 
	 * @param title             Game title.
	 * @param developer         Developer name.
	 * @param publisher         Publisher name.
	 * @param releaseDate       Date of release.
	 * @param genre             Genre category.
	 * @param platform          Target platform.
	 * @param completionStatus  Current user completion status.
	 * @param notes             Optional notes from user.
	 * @param coverImageURL     Image URL for cover art.
	 */
	public game(String title, String developer, String publisher, LocalDate releaseDate,
			String genre, String platform, String completionStatus, String notes, String coverImageURL) {
		this.title = title;
		this.developer = developer;
		this.publisher = publisher;
		this.releaseDate = releaseDate;
		this.genre = genre; 
		this.platform = platform;
		this.completionStatus = completionStatus;
		this.notes = notes;
		this.coverImageURL = coverImageURL;
		}

	//------------------ Getters and Setters ------------------
	
	public int getGameID() {
		return gameID;
	}

	public void setGameID(int gameID) {
		this.gameID = gameID;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDeveloper() {
		return developer;
	}

	public void setDeveloper(String developer) {
		this.developer = developer;
	}

	public String getPublisher() {
		return publisher;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	public LocalDate  getReleaseDate() {
		return releaseDate;
	}

	public void setReleaseDate(LocalDate releaseDate) {
		this.releaseDate = releaseDate;
	}
	
	public String getGenre() {
		return genre;
	}
	
	public void setGenre(String genre) {
		this.genre = genre;
	}
	
	public String getPlatform() {
		return platform;
	}
	
	public void setPlatform(String platform) {
		this.platform = platform;
	}
	
	public String getCompletionStatus() {
		return completionStatus;
	}
	
	public void setCompletionStatus(String completionStatus) {
		this.completionStatus = completionStatus;
	}
	
	public String getNotes() {
		return notes;
	}
	
	public void setNotes(String notes) {
		this.notes = notes;
	}
	
	public String getCoverImageUrl() {
	    return coverImageURL;
	}

	public void setCoverImageUrl(String coverImageUrl) {
	    this.coverImageURL = coverImageUrl;
	}

    /**
     * Compares games by ID, title, and core metadata.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        game game = (game) o;
        return gameID == game.gameID &&
                Objects.equals(title, game.title) &&
                Objects.equals(developer, game.developer) &&
                Objects.equals(publisher, game.publisher) &&
                Objects.equals(releaseDate, game.releaseDate) &&
                Objects.equals(genre, game.genre);
    }
    
    /**
	 * Generates a hash code for the game object.
	 */
    @Override
    public int hashCode() {
        return Objects.hash(gameID, title, developer, publisher, releaseDate, genre);
    }
           
}


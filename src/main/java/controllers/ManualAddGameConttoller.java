package controllers;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.hc.core5.http.ParseException;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import models.game;
import services.GameCollectionService;
import services.userService;
import utils.AlertHelper;

/**
 * Controller for manually adding a game to the collection.
 * Allows the user to input all details about a game and optionally attach a cover image.
 */
public class ManualAddGameConttoller extends BaseController {
	
    // ---------- FXML Fields ----------
    @FXML private TextField titleField;                   // Game title input
    @FXML private TextField developerField;               // Developer input
    @FXML private TextField publisherField;               // Publisher input
    @FXML private TextField releaseDateField;             // Release date input (must be yyyy-MM-dd)
    @FXML private TextField genreField;                   // Genre(s) input
    @FXML private TextField platformField;                // Platform(s) input
    @FXML private ChoiceBox<String> completionStatusField;// Completion status (Not Started, Playing, Completed)
    @FXML private TextArea notesField;                    // User notes
    @FXML private Button submitButton;                    // Button to submit and save game
    @FXML private Button browseImageButton;               // Opens file chooser for cover image
    @FXML private Button gameCollectionButton;            // Navigates to Game Collection page
    @FXML private Button settingsButton;                  // Navigates to Settings page
    @FXML private Button helpButton;                      // Navigates to Help page
    @FXML private Button logoutButton;                    // Logs out the user
    @FXML private Button refreashButton;                  // Refreshes the form
    @FXML private Button addAPIButton;                    // Navigates to Add Game from API page
    @FXML private Label selectedImageLabel;               // Shows name of selected image
    @FXML private ImageView coverImagePreview;            // Displays selected or default cover image

	// ---------- Services & State ----------
    private final GameCollectionService gameCollectionService = new GameCollectionService(); // Service to add games
    private final NavigationHelper navHelp = new NavigationHelper();                         // Handles screen transitions
    private final userService userService = new userService();                               // User session service
    private final AlertHelper alert = new AlertHelper();                                 // Alert handler
    private List<String> errors = new ArrayList<>();                                          // Collected validation errors
    private File selectedImageFile;                                                           // Selected image file
	
	/**
	 * Called when user data is loaded into the controller.
	 */
    @Override
    protected void onUserDataLoad() {
    }
    
    /**
     * Initializes the controller.
     * Sets up the completion status choice box and its action handler.
     */
    public void initialize() {
    	completionStatusField.getItems().addAll("Not Started", "Playing", "Completed");
        completionStatusField.setOnAction(event -> {
			String selectedStatus = completionStatusField.getValue();
			getCompletionStatusField(selectedStatus);
		});
    }
    
    // ---------- Input Field Getters with Validation ----------
    
    /** @return title if valid, otherwise adds error */
    @FXML
    protected String getTitleField() {
    	String title = titleField.getText();
    	System.out.println("Title for new game is " + title);
    	if(!(title == null) && !title.trim().isEmpty()) {
    		return title.trim();
    	}else {
    		System.out.println("Title is empty");
    		errors.add("Please enter the title of the game your would like to add");
    		return null;
    	}
    }
    
    /** @return developer if valid, otherwise adds error */
    @FXML
    protected String getDeveloperField() {
    	String developer = developerField.getText();
    	System.out.println("Developer for new game is " + developer);
    	if(!(developer == null) && !developer.trim().isEmpty()) {
    		return developer.trim();
    	}else {
    		System.out.println("Developer is empty");
    		errors.add("Please enter the developer of the game your would like to add");
    		return null;
    	}
    }
    
    /** @return publisher if valid, otherwise adds error */
    @FXML
    protected String getPublisherField() {
    	String publisher = publisherField.getText();
    	System.out.println("Publisher for new game is " + publisher);
    	if(!(publisher == null) && !publisher.trim().isEmpty()) {
    		return publisher.trim();
    	}else {
    		System.out.println("Publisher is empty");
    		errors.add("Please enter the publisher of the game your would like to add");
    		return null;
    	}
    }
    
    /**
     * Completion status is handled by ChoiceBox selection.
     * @param choice selected completion status
     * @return choice if not null, otherwise adds error
     */
    String getCompletionStatusField(String choice) {
    	System.out.println("Completion Status is set to " + choice);
    	if(!(choice == null)) {
    		return choice;
    	}else {
    		System.out.println("Completion Status is empty");
    		errors.add("Please enter the completion status of the game your would like to add");
    		return null;
    	}
    	}
    
    /** @return release date if valid, otherwise adds error */
    @FXML
    protected String getReleaseDateField() {
    	String releaseDate = releaseDateField.getText();
    	System.out.println("releaseDate for new game is " + releaseDate);
    	if(!(releaseDate == null) && !releaseDate.trim().isEmpty()) {
    		return releaseDate.trim();
    	}else {
    		System.out.println("releaseDate is empty");
    		errors.add("Please enter the Release Date of the game your would like to add");
    		return null;
    	}
    }
    
    /** @return genre(s) if valid, otherwise adds error */
    @FXML
    protected String getGenreField() {
    	String genre = genreField.getText();
    	System.out.println("Genre(s) for new game is " + genre);
    	if(!(genre == null) && !genre.trim().isEmpty()) {
    		return genre.trim();
    	}else {
    		System.out.println("Genre is empty");
    		errors.add("Please enter the genre of the game your would like to add");
    		return null;
    	}
    }
    
    /** @return platform(s) if valid, otherwise adds error */
    @FXML
    protected String getPlatformField() {
    	String platform = platformField.getText();
    	System.out.println("Platform(s) for new game is " + platform);
    	if(!(platform == null) && !platform.trim().isEmpty()) {
    		return platform.trim();
    	}else {
    		System.out.println("Platform is empty");
    		errors.add("Please enter the platform of the game your would like to add");
    		return null;
    	}
    }
    
    /** @return notes or empty string if null */
    @FXML
    protected String getNotesField() {
    	String notes = notesField.getText();
    	System.out.println("notes for new game is " + notes);
    	if(!(notes == null)) {
    		return notes;
    	}else {
    		System.out.println("Notes is empty");
    		notes = "";
    		return notes;
    	}
    }
    
    // ---------------------- IMAGE HANDLING ----------------------
    
    /**
     * Opens a file chooser to select an image for the cover art.
     * Updates the label and preview image accordingly.
     */
    @FXML
    protected void handleBrowseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Cover Art Image");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        
        File file = fileChooser.showOpenDialog(browseImageButton.getScene().getWindow());
        
        if (file != null) {
            selectedImageFile = file;
            selectedImageLabel.setText(file.getName());
            coverImagePreview.setImage(new Image(selectedImageFile.toURI().toString()));

        } else {
            selectedImageLabel.setText("No image selected");
        }
    }
    

    /**
	 * Handles the cover art field.
     * If an image is selected, it is copied to the resolved Images directory determined by the resolveImagesDir method.
     * If no image is selected, a placeholder image is randomly chosen from the classpath Images folder.
     * If the image copy fails, the original image file location is used as a fallback.
	 * @return the URL string to the cover art image
	 */
    @FXML
    protected String getCoverArtField() {
        Path imagesDir = resolveImagesDir();
        System.out.println("[CoverArt] imagesDir = " + imagesDir);

        // USER-SELECTED IMAGE
        if (selectedImageFile != null) {
            System.out.println("[CoverArt] selectedImageFile = " + selectedImageFile.getAbsolutePath());
            try {
                String original = selectedImageFile.getName();
                String base = original, ext = "";
                int dot = original.lastIndexOf('.');
                if (dot >= 0) { base = original.substring(0, dot); ext = original.substring(dot); }

                Path dest = imagesDir.resolve(original);
                if (Files.exists(dest)) {
                    dest = imagesDir.resolve(base + "_" + System.currentTimeMillis() + ext);
                }

                Files.copy(selectedImageFile.toPath(), dest, StandardCopyOption.COPY_ATTRIBUTES);
                String fileUrl = dest.toUri().toString(); // file:/...
                System.out.println("[CoverArt] COPIED to: " + dest + " | url=" + fileUrl);
                return fileUrl; 
            } catch (IOException e) {
                e.printStackTrace();
                errors.add("Failed to copy cover image to: " + imagesDir);
                try {
                    String fallback = selectedImageFile.toURI().toString();
                    System.out.println("[CoverArt] Using fallback original path: " + fallback);
                    return fallback;
                } catch (Exception ignore) { }
                return null;
            }
        }

        // PLACEHOLDER from classpath
        String[] placeholders = {"placeholder1gameGrinding.png", "placeholder2gameGrinding.png"};
        String pick = placeholders[(int)(Math.random() * placeholders.length)];
        String cpPath = "/Images/" + pick;

        URL url = getClass().getResource(cpPath);
        if (url != null) {
            String u = url.toExternalForm();
            System.out.println("[CoverArt] Using classpath placeholder: " + cpPath + " -> " + u);
            return u;
        } else {
            System.err.println("[CoverArt] Placeholder NOT FOUND on classpath: " + cpPath);
            errors.add("Placeholder image missing from resources.");
            return null;
        }
    }

    
    
    /**
     * Resolves the directory used to store cover art images.
     * The resolution priority is as follows:
     * 1. If the system property gamegrinding.images.dir is set, use that path.
     * 2. If an Images folder exists (or can be created) in the current working directory and is writable, use that folder.
     * 3. Otherwise, use the Images folder under the GameGrinding directory in the user's home folder.
     * 
     * @return the path to the resolved Images directory.
     */
    private Path resolveImagesDir() {
        //Highest priority: explicit override via JVM arg
        String override = System.getProperty("gamegrinding.images.dir");
        if (override != null && !override.isBlank()) {
            Path p = Paths.get(override);
            try { Files.createDirectories(p); } catch (IOException ignored) {}
            return p;
        }
        // 2) Project Images folder next to the working dir
        Path devImages = Paths.get(System.getProperty("user.dir"), "Images");
        if (Files.isDirectory(devImages) || createDirQuiet(devImages)) {
            // Only use this if it's writable (dev case)
            if (Files.isWritable(devImages)) return devImages;
        }
        // Fallback: user home (works on packaged installs)
        Path homeImages = Paths.get(System.getProperty("user.home"), "GameGrinding", "Images");
        createDirQuiet(homeImages);
        return homeImages;
    }

    /**
     * Creates a directory if it does not already exist.
     * This method suppresses IOExceptions and returns false instead of throwing an error.
     *
     * @param dir the directory path to create
     * @return true if the directory exists or was created successfully, false otherwise
     */
    private boolean createDirQuiet(Path dir) {
        try { Files.createDirectories(dir); return true; }
        catch (IOException e) { return false; }
    }




    // ---------------------- FORM HANDLING ----------------------
    
    /**
     * Validates all fields and constructs a Game object if valid.
     * 
     * @return constructed game object or null if validation failed
     */
    @FXML
    private game handleSubmit() {
        errors.clear();


        String title = getTitleField();
        String developer = getDeveloperField();
        String publisher = getPublisherField();
        String releaseDate = getReleaseDateField(); 
        String genre = getGenreField();
        String platform = getPlatformField();
        String notes = getNotesField();
        String completionStatus = getCompletionStatusField(completionStatusField.getValue());
        String coverArt = getCoverArtField();


        if (!errors.isEmpty()) {
            alert.showAlert("Missing or Invalid Fields", errors);
            return null;
        }

        LocalDate releaseDateParsed = null;
        if (releaseDate != null && !releaseDate.isEmpty()) {
            try {
                releaseDateParsed = LocalDate.parse(releaseDate);
            } catch (Exception e) {
                errors.add("Release date must be in yyyy-MM-dd format.");
                alert.showAlert("Invalid Release Date", errors);
                return null;
            }
        }
        return new game(0, title, developer, publisher, releaseDateParsed, genre, platform, completionStatus, notes, coverArt);
    }

    /**
     * Handles the click on the submit button. Validates and adds the new game to the user's collection.
     */
    @FXML
    private void handleSubmitButtonClick() {
        game newGame = handleSubmit();
        if (newGame != null && loggedInUserID > 0) {
            boolean added;
			try {
				added = gameCollectionService.addGameToCollection(newGame, loggedInUserID);
	            if (added) {
	                System.out.println("Game successfully added to the collection.");
	                navHelp.switchToGameCollection(loggedInUserID, submitButton);
	            } else {
	                alert.showAlert("Error", List.of("Failed to add game to collection."));
	            }
			} catch (ParseException | InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
        }
    }
    
    // ---------------------- NAVIGATION METHODS ----------------------
    
    /**
	 * Handles the action of clicking the Game Collection button.
	 * Navigates back to the game collection view.
	 */
    @FXML
    protected void handleGameCollectionButton() {
    	try {
    		navHelp.switchToGameCollection(loggedInUserID, gameCollectionButton);
    	}catch (Exception e) {
			e.printStackTrace();
		}
    }
    /**
     * Navigates to the Settings page when the Settings button is clicked
     */
    @FXML
    protected void handleSettingsButton() {
		try {
			navHelp.switchToSettingsPage(loggedInUserID, settingsButton);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
    /**
	 * Navigates to the Help page when the Help button is clicked
	 */
	@FXML
	protected void handleHelpButton() {
		try {
			navHelp.switchToHelpPage(loggedInUserID, helpButton);
		}catch (Exception e) {
			e.printStackTrace();
	}
	}
	/**
	 * Handles the action of clicking the logout button.
	 * Logs out the user and navigates back to the login page.
	 */	
	@FXML
	protected void handleLogoutButton() {
		try {
			userService.logout();
			System.out.println("User logged out successfully.");
			navHelp.switchToLoginPage(logoutButton);
		} catch (Exception e) {
			e.printStackTrace();
		} 
    }
	/**
	 * Handles the action of clicking the refresh button.
	 * Clears all input fields and errors.
	 */
	@FXML 
	protected void handleRefreshButton() {
		try {
			navHelp.switchToManualAddGamePage(loggedInUserID, refreashButton);
			System.out.println("Page refreshed successfully.");
		} catch (Exception e) {
			e.printStackTrace();
			alert.showAlert("Error", List.of("Failed to refresh the page."));
		}
	}
	/**
	 * Handles the action of clicking the Add Game API button.
	 * Navigates to the Add Game API page.
	 */
	@FXML
	protected void handleAddAPIButton() {
		try {
			navHelp.switchToAPISearchPage(loggedInUserID, addAPIButton);
		} catch (Exception e) {
			e.printStackTrace();
			alert.showAlert("Error", List.of("Failed to navigate to Add Game API page."));
		}
	}
}



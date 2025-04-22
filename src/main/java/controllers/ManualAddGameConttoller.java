package controllers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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
    private String getTitleField() {
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
    private String getDeveloperField() {
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
    private String getPublisherField() {
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
    private String getReleaseDateField() {
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
    private String getGenreField() {
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
    private String getPlatformField() {
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
    private String getNotesField() {
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
    private void handleBrowseImage() {
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
	 * If an image is selected, it copies it to the Images directory.
	 * If no image is selected, it randomly chooses a placeholder image.
	 * @return the path to the cover art image
	 */
    @FXML
    private String getCoverArtField() {
        File imagesDir = new File("Images");
        if (!imagesDir.exists()) {
            imagesDir.mkdir();
        }

        if (selectedImageFile != null) {
            try {
                String originalFileName = selectedImageFile.getName();
                File destFile = new File(imagesDir, originalFileName);
                
                if (destFile.exists()) {
                    String baseName = originalFileName.substring(0, originalFileName.lastIndexOf('.'));
                    String extension = originalFileName.substring(originalFileName.lastIndexOf('.'));
                    String newName = baseName + "_" + System.currentTimeMillis() + extension;
                    destFile = new File(imagesDir, newName);
                }
                Files.copy(selectedImageFile.toPath(), destFile.toPath());
                String relativePath = "Images/" + destFile.getName();
                return relativePath;
            } catch (IOException e) {
                e.printStackTrace();
                errors.add("Failed to copy cover image.");
                return null;
            }
        } else {
            // No image selected, randomly choose a placeholder
            String[] placeholders = {"placeholder1gameGrinding.png", "placeholder2gameGrinding.png"};
            int randomIndex = (int) (Math.random() * placeholders.length);
            String chosenPlaceholder = "Images/" + placeholders[randomIndex];
            File placeholderFile = new File(chosenPlaceholder);
            if (placeholderFile.exists()) {
                System.out.println("Using placeholder image: " + chosenPlaceholder);
                return chosenPlaceholder;
            } else {
                System.err.println("Placeholder image not found: " + chosenPlaceholder);
                errors.add("Placeholder image missing.");
                return null;
            }
        }
    }
    // ---------------------- FORM HANDLING ----------------------
    
    /**
     * Validates all fields and constructs a Game object if valid.
     * 
     * @return constructed game object or null if validation failed
     */
    @FXML
    private game handleSubmit() {
        // Clear previous errors
        errors.clear();

        // Gather input values
        String title = getTitleField();
        String developer = getDeveloperField();
        String publisher = getPublisherField();
        String releaseDate = getReleaseDateField(); 
        String genre = getGenreField();
        String platform = getPlatformField();
        String notes = getNotesField();
        String completionStatus = getCompletionStatusField(completionStatusField.getValue());
        String coverArt = getCoverArtField();

        // Check for errors
        if (!errors.isEmpty()) {
            alert.showAlert("Missing or Invalid Fields", errors);
            return null;
        }

        // Validate release date format
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
    private void handleGameCollectionButton() {
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
    private void handleSettingsButton() {
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
	private void handleHelpButton() {
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
	private void handleLogoutButton() {
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
	private void handleRefreshButton() {
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
	private void handleAddAPIButton() {
		try {
			navHelp.switchToAPISearchPage(loggedInUserID, addAPIButton);
		} catch (Exception e) {
			e.printStackTrace();
			alert.showAlert("Error", List.of("Failed to navigate to Add Game API page."));
		}
	}
}



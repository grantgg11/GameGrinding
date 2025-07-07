package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import models.game;
import services.GameService;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for the Edit Game screen.
 * 
 * This class handles the user interface interactions for editing game details,
 * including title, developer, publisher, release date, platform, genre, notes,
 * and completion status. It also allows users to upload a cover image for the game.
 */
public class EditGameController extends BaseController {
    @FXML private TextField titleField;
    @FXML private TextField developerField;
    @FXML private TextField publisherField;
    @FXML private TextField releaseDateField;
    @FXML private TextField platformField;
    @FXML private TextField genreField;
    @FXML private TextArea notesField;
    @FXML private ChoiceBox<String> completionStatusChoiceBox;
    @FXML private ImageView coverImageView;
    @FXML private Label selectedImageLabel;
    @FXML private Button saveButton;
    @FXML private Button browseImageButton;
    @FXML private Button backButton;
	@FXML private Button gameCollectionButton;
	@FXML private Button settingsButton;
	@FXML private Button helpButton;
	

    private final GameService gameService = new GameService();
    private final NavigationHelper navHelp = new NavigationHelper();
    private game currentGame; 										// The game object being edited
    private File selectedImageFile; 								// File object for the selected image
    private final List<String> errors = new ArrayList<>();			// List to store error messages	
    
    /**
	 * Called when user data is loaded into the controller.
	 * 
	 * This method is overridden to perform any additional setup or data loading
	 * specific to the EditGameController.
	 */
    @Override
    protected void onUserDataLoad() {
        System.out.println("onUserDataLoad in EditGameController: user ID = " + loggedInUserID);
    }
    
    /**
	 * Sets the game object to be edited and preloads its data into the form.
	 * 
	 * @param selectedGame The game object to be edited
	 */
    public void setGame(game selectedGame) {
        this.currentGame = selectedGame;
        preloadGameData();
    }
    
    /**
	 * Initializes the controller by setting up the completion status choice box.
	 */
    public void initialize() {
        completionStatusChoiceBox.getItems().addAll("Not Started", "Playing", "Completed");
    }
    
    /**
     * Loads current game details into the form fields.
     * Also handles loading and displaying the cover image.
     */
    private void preloadGameData() {
        if (currentGame == null) return;
        titleField.setText(currentGame.getTitle());
        developerField.setText(currentGame.getDeveloper());
        publisherField.setText(currentGame.getPublisher());
        releaseDateField.setText(currentGame.getReleaseDate() != null ? currentGame.getReleaseDate().toString() : "");
        platformField.setText(currentGame.getPlatform());
        genreField.setText(currentGame.getGenre());
        notesField.setText(currentGame.getNotes());
        completionStatusChoiceBox.setValue(currentGame.getCompletionStatus());

        String imageUrl = currentGame.getCoverImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Image image = imageUrl.startsWith("http") || imageUrl.startsWith("file:")
                    ? new Image(imageUrl, true)
                    : new Image(new File(imageUrl).toURI().toString(), true);
            coverImageView.setImage(image);
        }
    }
    
    /**
     * Opens a file chooser dialog to allow the user to select a new cover image for the game.
     * Updates the image preview and the game's image URL.
     */
    @FXML
    private void handleBrowseImage() {
    	FileChooser fileChooser = createFileChooser();
        fileChooser.setTitle("Select New Cover Image");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File file = fileChooser.showOpenDialog(browseImageButton.getScene().getWindow());
        if (file != null) {
            selectedImageFile = file;
            selectedImageLabel.setText(file.getName());
            coverImageView.setImage(new Image(selectedImageFile.toURI().toString()));
            currentGame.setCoverImageUrl(selectedImageFile.toURI().toString());
        }
    }
    
    /**
     * Creates and returns a new FileChooser instance.
     * 
     * This method is protected to allow overriding in tests for mocking purposes.
     * It is used in handleBrowseImage() to facilitate testable file selection behavior.
     * 
     * @return A new instance of FileChooser
     */
    protected FileChooser createFileChooser() {
        return new FileChooser();
    }

    /**
	 * Validates the input fields and saves the updated game details.
	 * 
	 * If any validation fails, an error message is displayed to the user.
	 */
    @FXML
    private void handleSaveButtonClick() {
        errors.clear();
        if (currentGame == null || loggedInUserID <= 0) return;

        int gameID = currentGame.getGameID();

        gameService.updateTitle(loggedInUserID, gameID, titleField.getText());
        gameService.updateDeveloper(loggedInUserID, gameID, developerField.getText());
        gameService.updatePublisher(loggedInUserID, gameID, publisherField.getText());
        gameService.updateReleaseDate(loggedInUserID, gameID, releaseDateField.getText());
        gameService.updatePlatform(loggedInUserID, gameID, platformField.getText());
        gameService.updateGenre(loggedInUserID, gameID, genreField.getText());
        gameService.updateNotes(loggedInUserID, gameID, notesField.getText());
        gameService.updateCompletionStatus(loggedInUserID, gameID, completionStatusChoiceBox.getValue());
        gameService.updateCoverImageURL(loggedInUserID, gameID, currentGame.getCoverImageUrl());
        
        System.out.println("Game updated successfully.");
        navHelp.switchToGameCollection(loggedInUserID, saveButton);
    }
    
    /**
     * Navigates to the game collection view when the Game Collection button is clicked.
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

}


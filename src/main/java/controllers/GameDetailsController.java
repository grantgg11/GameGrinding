package controllers;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import models.game;
import services.GameService;
import services.userService;
import utils.AlertHelper;


/**
 * Controller for the Game Details screen.
 * Displays detailed information about a selected game and allows users to
 * edit, delete, update completion status, and save notes.
 */
public class GameDetailsController extends BaseController {

    // ---------- FXML UI Components ----------
    @FXML private ImageView coverImage;                         // Displays the game's cover image
    @FXML private Label gameTitle;                              // Displays the game's title
    @FXML private Label developerLabel;                         // Developer(s) info
    @FXML private Label publisherLabel;                         // Publisher(s) info
    @FXML private Label releaseDateLabel;                       // Release date
    @FXML private Label genreLabel;                             // Genre(s)
    @FXML private Label platformLabel;                          // Platform(s)
    @FXML private Label completionStatusLabel;                  // Static label for completion status
    @FXML private TextArea notesTextArea;                       // Displays or allows editing notes
    @FXML private ChoiceBox<String> completionStatusChoiceBox;  // Dropdown to update completion status
    @FXML private Button editButton;                            // Button to navigate to Edit Game screen
    @FXML private Button deleteButton;                          // Button to delete the game
    @FXML private Button gameCollectionLabel;                   // Button to return to collection
    @FXML private Button saveNotesButton;                       // Button to save edited notes
    @FXML private Button settingsButton;                        // Navigate to settings
    @FXML private Button helpButton;                            // Navigate to help
    @FXML private Button logoutButton;                          // Logs user out
    @FXML private Button backButton;                            // Returns to AddGameAPI search results

    //---------- State/Service Fields ----------
    private boolean showBackButton = false;                     // Tracks if user came from API search
    private game previousGameFromAPISearch;                     // Stores game for restoring API state
    private userService userSer = new userService();            // Handles user auth and sessions
    private GameService gameService = new GameService();        // Handles game-related DB actions
    private NavigationHelper navHelp = new NavigationHelper();  // For navigating between views
    private AddGameAPIController previousController;            // Used for restoring API state
    private List<game> previousSearchResults;                   // Previous search results to restore
    private game currentGame;                                   // The game currently being viewed
    private Stage popupStage;   								// Stage for delete confirmation popup
    private AlertHelper alert = new AlertHelper();
    
    
    /**
     * Runs when user data is loaded into the controller.
     */
    @Override
    protected void onUserDataLoad() {
    	System.out.println();
        System.out.println("onUserDataLoad in GameDetailsController for user " + loggedInUserID);
    }
    
    /**
     * Sets the game to display in the details view.
     * Also populates the dropdown and UI content.
     * 
     * @param gameItem
     */
    public void setGame(game gameItem) {
        if (completionStatusLabel == null) {
            System.err.println("ERROR: UI components are not initialized yet!");
            return;
        }
        this.setCurrentGame(gameItem);
        //populate completion status dropdown
        completionStatusChoiceBox.getItems().addAll("Not Started", "Playing", "Completed");
        completionStatusChoiceBox.setValue(gameItem.getCompletionStatus());
        
        //Update the database when the user selects a new completion status
        completionStatusChoiceBox.setOnAction(event -> {
			String selectedStatus = completionStatusChoiceBox.getValue();
			gameItem.setCompletionStatus(selectedStatus);
			System.out.println("Completion status updated to: " + selectedStatus);
			updateCompletionStatus();
		});
        System.out.println("Game details updated for: " + gameItem.getTitle());
        this.setCurrentGame(gameItem);
        updateGameDetails();
    }
    
    /**
     * Stores the controller reference used during API-based game search.
     * 
     * @param controller The controller instance to set
     */   
    public void setPreviousController(AddGameAPIController controller) {
        this.previousController = controller;
    }
    
    /**
     *  Sets the previous search results from the AddGameAPIController.
     *  
     * @param results The list of previous search results
     */
    public void setPreviousSearchResults(List<game> results) {
        this.previousSearchResults = results;
    }

    /**
     * Updates the UI with the current game's details.
     */
    @FXML
    protected void updateGameDetails() {
        if (getCurrentGame() != null) {
            gameTitle.setText(getCurrentGame().getTitle());
            developerLabel.setWrapText(true);
            developerLabel.setMaxWidth(485); 
            developerLabel.setText("Developer(s): " + getCurrentGame().getDeveloper());

            publisherLabel.setWrapText(true);
            publisherLabel.setMaxWidth(485);
            publisherLabel.setText("Publisher(s): " + getCurrentGame().getPublisher());
            
            releaseDateLabel.setWrapText(true); 
            releaseDateLabel.setMaxWidth(485);
            releaseDateLabel.setText("Release Date: " + getCurrentGame().getReleaseDate());


            genreLabel.setWrapText(true);
            genreLabel.setMaxWidth(485);
            genreLabel.setText("Genre(s): " + getCurrentGame().getGenre());

            platformLabel.setWrapText(true);
            platformLabel.setMaxWidth(485);
            platformLabel.setText("Platform(s): " + formatPlatforms(getCurrentGame().getPlatform()));         

            
            String notes = (getCurrentGame().getNotes() != null) ? getCurrentGame().getNotes() : "No notes available.";
            completionStatusLabel.setText("Completion Status: ");
            notesTextArea.setText(notes);
    		notesTextArea.setWrapText(true);
    		
    		//Load the cover image
            if (getCurrentGame().getCoverImageUrl() != null && !getCurrentGame().getCoverImageUrl().isEmpty()) {
                String imagePath = getCurrentGame().getCoverImageUrl();
                Image image = null;

                try {
                	if (imagePath.startsWith("http://") || imagePath.startsWith("https://")) {
                	    image = new Image(imagePath, true);
                    } else {
                        File file = new File(imagePath);
                        if (file.exists()) {
                            image = new Image(file.toURI().toString());
                        } else {
                            System.err.println("Image file not found at: " + imagePath);
                        }
                    }

                    if (image != null && !image.isError()) {
                    	coverImage.setImage(image);
                    	coverImage.setPreserveRatio(true);
                    	coverImage.setSmooth(true);
                    	coverImage.setCache(true);
                    	coverImage.setFitWidth(516);  
                    	coverImage.setFitHeight(292);    
                    } else {
                        System.out.println("Image could not be loaded or is invalid.");
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Failed to load image from path: " + imagePath);
                }
            } else {
                System.out.println("No cover image available.");
            }

        }
    }
    
    /**
	 * Updates the completion status of the game in the database.
	 */
    protected void updateCompletionStatus() {
    	try {
    		System.out.println("Updating completion status for game: " + getCurrentGame().getTitle() + " to " + getCurrentGame().getCompletionStatus() + " for user ID: " + loggedInUserID);
    		gameService.updateCompletionStatus(loggedInUserID, getCurrentGame().getGameID(), getCurrentGame().getCompletionStatus());
    	}catch (Exception e) {
			e.printStackTrace();
			alert.showError("Completion Status Update Unsucessfull.", null, "Please try again. Completion Statuses can only be 'Not Started', 'Playing', and 'Completed'.");
			System.out.println("Error occurred while updating completion status: " + e.getMessage());
		}
	}
    
    /**
     * Saves the notes entered in the text area to the database.
     */
    @FXML
	protected void saveNotes() {
		String notes = notesTextArea.getText();
		getCurrentGame().setNotes(notes);
		System.out.println("Notes saved for game: " + getCurrentGame().getTitle());
		try {
			System.out.println("Saving notes for game: " + getCurrentGame().getTitle() + " for user ID: " + loggedInUserID);
			gameService.updateNotes(loggedInUserID, getCurrentGame().getGameID(), notes);
		} catch (Exception e) {
			e.printStackTrace();
			
			System.out.println("Error occurred while saving notes: " + e.getMessage());
		}		
	}
	
    /**
     * Formats the platforms string to display in a more readable format.
     * 
     * @param platforms The platforms string to format
     * @return Formatted string with platforms
     */
    private String formatPlatforms(String platforms) {
        if (platforms == null || platforms.isEmpty()) return "Unknown";

        String[] platformList = platforms.split(",\\s*"); 
        StringBuilder formatted = new StringBuilder();

        for (int i = 0; i < platformList.length; i++) {
            formatted.append(platformList[i]);

            // Insert a newline after every 5 platforms
            if ((i + 1) % 5 == 0 && i != platformList.length - 1) {
            	formatted.append(",").append(System.lineSeparator());
            } else if (i != platformList.length - 1) {
                formatted.append(", ");
            }
        }
        return formatted.toString();
    }
    
    /**
     * Opens a confirmation popup for deleting the selected game.
     * 
     * @param event The action event triggered by the delete button
     */
    @FXML
    protected void clickDeleteButton(ActionEvent event) {
        if (getCurrentGame() == null) {
            System.out.println("No game selected for deletion!");
            return;
        }
        System.out.println("Attempting to delete: " + getCurrentGame().getTitle() + " (GameID: " + getCurrentGame().getGameID() + ") for UserID: " + loggedInUserID);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/deleteGamePopup.fxml"));
            Parent root = loader.load();
            DeleteGamePopupController controller = loader.getController();
            if (controller != null) {
                controller.setUserID(loggedInUserID); 
                controller.setCurrentGameID(getCurrentGame().getGameID());
            } else {
                System.err.println("Error: DeleteGamePopupController is null!");
            }
            
            popupStage = new Stage(); 
            popupStage.setScene(new Scene(root));
            popupStage.setTitle("Delete Game Confirmation");
            popupStage.setResizable(false);

            // Get the parent window from the event source (button)
            Stage parentStage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            popupStage.initOwner(parentStage);
            popupStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error: Could not load AddGamePopup.fxml!");
        }
    }
    
    /**
     * Navigates to the Game Collection screen when the Game Collection button is clicked.
     */
    @FXML
    protected void handleGameCollectionButton() {
    	navHelp.switchToGameCollection(loggedInUserID, gameCollectionLabel);
    	System.out.println("Switching to Game Colleciton Page.");
    }
    
    /**
     * Navigates to the Edit Game screen when the Edit button is clicked.
     */
    @FXML 
    protected void handleEditButton() {
		if (getCurrentGame() == null) {
			System.out.println("No game selected for editing!");
			return;
		}
		System.out.println("Opening Edit Game window for: " + getCurrentGame().getTitle() + " (GameID: " + getCurrentGame().getGameID() + ")");
		navHelp.switchToEditGamePage(loggedInUserID, getCurrentGame(), editButton);
	}
    
    /**
	 * Navigates to the Settings page when the Settings button is clicked.
	 */
    @FXML
    protected void handleSettingsButton() {
    	navHelp.switchToSettingsPage(loggedInUserID, settingsButton);
    	System.out.println("Switching to Settings Page for User ID: " + loggedInUserID);
    }
    
    /**
     * Navigates to the Help page when the Help button is clicked.
     */
    @FXML 
    protected void handleHelpButton() {
    	navHelp.switchToHelpPage(loggedInUserID, helpButton);
    	System.out.println("Switching to Help Page for User ID: " + loggedInUserID);
    }
    
    /**
	 * Logs the user out and navigates to the login page.
	 */
    @FXML
    protected void handleLogoutButton() {
		System.out.println("Logging out user ID: " + loggedInUserID);
		userSer.logout();
		System.out.println("Logging out in game details controller.");
		navHelp.switchToLoginPage(logoutButton);
	}
    
    /**
	 * Navigates back to the AddGameAPI search results screen.
	 * Only visible if the user came from an API search.
	 */
    @FXML
    protected void handleBackButton() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/AddGameAPI.fxml"));
            Parent root = loader.load();
            AddGameAPIController controller = loader.getController();

            controller.setUserID(loggedInUserID); // reloads user
            controller.displayResults(previousSearchResults); // restore state
            if (previousSearchResults != null && !previousSearchResults.isEmpty()) {
                controller.setPreviousSearchResults(previousSearchResults);
                controller.displayResults(previousSearchResults);
            }
            
            Stage stage = (Stage) helpButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Search Games");
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Configures the view depending on whether the game came from an API search.
     *
     * @param isFromAPI true if the game was selected from the AddGameAPI screen
     * @param gameItem  the game passed in
     */
    public void setFromAPISearch(boolean isFromAPI, game gameItem) {
        this.showBackButton = isFromAPI;
        this.previousGameFromAPISearch = gameItem;

        if (backButton != null) {
            backButton.setVisible(isFromAPI);
            backButton.setManaged(isFromAPI);
        }

        if (editButton != null && deleteButton != null) {
            editButton.setVisible(!isFromAPI);
            editButton.setManaged(!isFromAPI);

            deleteButton.setVisible(!isFromAPI);
            deleteButton.setManaged(!isFromAPI);
        }
    }

	/**
	 * @return the currentGame
	 */
	public game getCurrentGame() {
		return currentGame;
	}

	/**
	 * @param currentGame the currentGame to set
	 */
	public void setCurrentGame(game currentGame) {
		this.currentGame = currentGame;
	}



}


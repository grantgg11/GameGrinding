package controllers;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.game;
import services.GameCollectionService;
import services.GameService;
import services.userService;

/**
 * Controller class for filtering the game collection by platform, genre, and completion status.
 * Allows users to select filtering options, apply them, and view the filtered results in the collection view.
 */
public class FilterCollectionController extends BaseController {
	
	//------------- FXML Components -------------
	@FXML private VBox platformButtonContainer;  	// VBox for dynamically added platform filter buttons 
	@FXML private VBox genreButtonContainer; 		// VBox for dynamically added genre filter buttons
	@FXML private VBox completionButtonContainer; 	// VBox for dynamically added completion status filter buttons
	@FXML private Button confirmButton;   			// Button to apply the filters and view the results
	@FXML private Button gameCollectionButton;  	// Navigation button to return to game collection
	@FXML private Button settingsButton;  			// Navigation button to go to settings
	@FXML private Button helpButton;     			// Navigation button to access help page
	@FXML private Button logoutButton; 				// Button to log the user out of the session
	
	//-------------- Services -------------
	private final GameService gameService = new GameService();
	private final GameCollectionService collectionService = new GameCollectionService();
	private final userService userSer = new userService();
	private final NavigationHelper navHelp = new NavigationHelper();
	
	//---------------Data Lists----------------
	private List<String> platforms; 
	private List<String> genres; 
	private final List<String> completionStatus = List.of("Not Started", "Playing", "Completed");
	
	
	private List<String> selectedPlatforms = new ArrayList<>(); 
	private List<String> selectedGenres = new ArrayList<>(); 
	private List<String> selectedCompletion = new ArrayList<>();
	
	/**
	 * Loads user-specific platform and genre options and creates corresponding filter buttons.
	 */
    @Override
    protected void onUserDataLoad() {
        System.out.println("onUserDataLoad in FilterCollectionController for user " + loggedInUserID);

        try {
            platforms = gameService.getPlatformsFromCollection(loggedInUserID);
            genres = gameService.getGenreFromCollection(loggedInUserID);

            if ((platforms == null || platforms.isEmpty()) && (genres == null || genres.isEmpty())) {
                System.out.println("No platforms or genres found for user " + loggedInUserID);
                return;
            }

            createPlatformButtons();
            createGenreButtons();
            createCompletionStatusButtons();

        } catch (Exception e) {
            System.err.println("Error initializing FilterCollectionController: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
	 * Creates toggle buttons for each platform in the user's collection.
	 * Each button allows the user to filter games by the selected platform.
	 */
    private void createPlatformButtons() {       
        platformButtonContainer.getChildren().clear(); 
        platformButtonContainer.setAlignment(javafx.geometry.Pos.CENTER);
        platformButtonContainer.setSpacing(10);        
        
        for (String platform : platforms) {
            ToggleButton toggleButton = new ToggleButton(platform);
            toggleButton.setPrefSize(150, 30);
            toggleButton.setText(platform);
            toggleButton.setStyle( defaultButtonStyle()); // Set initial style for the button           
            toggleButton.selectedProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue) { // Selected
                    System.out.println("Filtering games for platform: " + platform);
                    toggleButton.setStyle(selectedButtonStyle()); 
                    selectedPlatforms.add(platform);
                } else { // Deselected
                    System.out.println(platform + " has been deselected");
                    toggleButton.setStyle(defaultButtonStyle());
                    selectedPlatforms.remove(platform);
                }
            });
            platformButtonContainer.getChildren().add(toggleButton);
        }
        
    }
    
    /**
     * Creates toggle buttons for each genre in the user's collection.
     */
    private void createGenreButtons() {       
        genreButtonContainer.getChildren().clear(); 
        genreButtonContainer.setAlignment(javafx.geometry.Pos.CENTER);
        genreButtonContainer.setSpacing(10);        

        for (String genre : genres) {
            ToggleButton toggleButton = new ToggleButton(genre);
            toggleButton.setPrefSize(150, 30);
            toggleButton.setText(genre);
            toggleButton.setStyle(defaultButtonStyle()); // Set initial style for the button           
            toggleButton.selectedProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue) { // Selected
                    System.out.println("Filtering games for genre: " + genre);
                    toggleButton.setStyle(selectedButtonStyle()); 
                    selectedGenres.add(genre);
                } else { // Deselected
                    System.out.println(genre + " has been deselected");
                    toggleButton.setStyle(defaultButtonStyle());
                    selectedGenres.remove(genre);
                }
            });
            genreButtonContainer.getChildren().add(toggleButton);
        }  
    }
    
    /**
	 * Creates toggle buttons for each completion status option.
	 */
    private void createCompletionStatusButtons() {       
    	completionButtonContainer.getChildren().clear(); 
    	completionButtonContainer.setAlignment(javafx.geometry.Pos.CENTER);
    	completionButtonContainer.setSpacing(10);    
    	
        for (String completion : completionStatus) {
            ToggleButton toggleButton = new ToggleButton(completion);
            toggleButton.setPrefSize(150, 30);
            toggleButton.setText(completion);
            toggleButton.setStyle(defaultButtonStyle()); // Set initial style for the button 
            toggleButton.selectedProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue) { // Selected
                    System.out.println("Filtering games for completion status: " + completion);
                    toggleButton.setStyle(selectedButtonStyle()); 
                    selectedCompletion.add(completion);
                } else { // Deselected
                    System.out.println(completion + " has been deselected");
                    toggleButton.setStyle(defaultButtonStyle());
                    selectedCompletion.remove(completion);
                }
            });
            completionButtonContainer.getChildren().add(toggleButton);
        }
        
    }
    
    /**
	 * Handles the action of clicking the confirm button to apply the selected filters.
	 * 
	 * Filters the game collection based on the selected genres, platforms, and completion status.
	 */
    @FXML
    private void handleConfirmButton() {
        List<String> finalSelectedGenres = selectedGenres.isEmpty() ? null : new ArrayList<>(selectedGenres);
        List<String> finalSelectedPlatforms = selectedPlatforms.isEmpty() ? null : new ArrayList<>(selectedPlatforms);
        List<String> finalSelectedCompletion = selectedCompletion.isEmpty() ? null : new ArrayList<>(selectedCompletion);

        System.out.println("Applying Filters - Genres: " + finalSelectedGenres + ", Platforms: " + finalSelectedPlatforms + ", Completion: " + finalSelectedCompletion);

        List<game> filteredGames = collectionService.filterCollection(loggedInUserID, finalSelectedGenres, finalSelectedPlatforms, finalSelectedCompletion);

        if (filteredGames.isEmpty()) {
            System.out.println("No games match the filters.");
        } else {
            System.out.println("Filtered games count: " + filteredGames.size());
        }

        // Switch back to the collection page with the filtered list
        switchToGameCollection(filteredGames);
    }

    
    /**
     * Switches to the GameCollection view and passes the filtered games to it.
     * 
     * @param filteredGames List of games matching the selected filters
     */
    private void switchToGameCollection(List<game> filteredGames) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/GameCollection.fxml"));
            Parent gameCollectionRoot = loader.load();
            
            // Get the controller
            GameCollectionController gameCollectionController = loader.getController();
            if (gameCollectionController == null) {
                System.err.println("Error: GameCollectionController is null!");
                return;
            }

            // Pass filtered games to GameCollectionController
            System.out.println("Setting filtered collection with " + filteredGames.size() + " games.");
            gameCollectionController.setUserID(loggedInUserID);
            gameCollectionController.setFilteredCollection(filteredGames);


            // Switch to GameCollection scene
            Stage stage = (Stage) confirmButton.getScene().getWindow();
            Scene scene = new Scene(gameCollectionRoot);
            Image icon = new Image(getClass().getResourceAsStream("/Images/GameGrinding.png"));
    		stage.getIcons().add(icon);
    		stage.setWidth(1295);
    		stage.setHeight(830);
    		stage.setScene(scene);
    		stage.setTitle("Game Collection");
    		stage.setResizable(false);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error: Could not load GameCollection.fxml!");
        }
    }
    
    /**
	 * Handles the action of clicking the Game Collection button to return to the game collection view.
	 */
    @FXML 
    private void handleGameCollectionButton(){
    	navHelp.switchToGameCollection(loggedInUserID, gameCollectionButton);
    	System.out.println("Switching to Game Collection Page from FilterCollectionController.");
    }
    
    /**
     * Handles the action of clicking the Settings button to navigate to the settings page.
     */
    @FXML
    private void handleSettingsButton() {
		System.out.println("Switching to Settings Page from FilterCollectionController.");
		try {
			navHelp.switchToSettingsPage(loggedInUserID, settingsButton);
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Error: Could not switch to Settings page!");
		}
	}
    
    /**
	 * Handles the action of clicking the Help button to navigate to the help page.
	 */
    @FXML
    private void handleHelpButton() {
    	System.out.println("Switching to Help Page from FilterCollectionController.");
    	try {
			navHelp.switchToHelpPage(loggedInUserID, helpButton);
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Error: Could not switch to Help page!");
		}
    }
    
    /**
	 * Handles the action of clicking the Logout button to log out the user.
	 * 
	 * Logs out the user and switches to the login page.
	 */
    @FXML
    private void handleLogoutButton() {
		System.out.println("Logging out user " + loggedInUserID);
		userSer.logout();
		navHelp.switchToLoginPage(logoutButton);
	}
    
    // ---------- Style Helpers ----------
    private String defaultButtonStyle() {
        return "-fx-background-color: #0d314b;" +
               "-fx-border-color: white;" +
               "-fx-text-fill: white;" +
               "-fx-border-width: 2px;" +
               "-fx-background-radius: 6;" +
               "-fx-border-radius: 6;" +
               "-fx-padding: 10px;" +
               "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 5, 0.2, 0, 5);";
    }

    private String selectedButtonStyle() {
        return "-fx-background-color: #960000;" +
               "-fx-border-color: white;" +
               "-fx-text-fill: white;" +
               "-fx-border-width: 2px;" +
               "-fx-background-radius: 6;" +
               "-fx-border-radius: 6;" +
               "-fx-padding: 10px;" +
               "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 5, 0.2, 0, 5);";
    }


}

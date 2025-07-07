package controllers;

import javafx.scene.image.Image;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import models.game;
import reports.PerformanceTracker;
import services.GameCollectionService;
import services.userService;

/**
 * Controller for the Game Collection screen.
 * 
 * This class handles the user interface interactions for displaying and managing
 * the user's game collection, including sorting, searching, and filtering games.
 */
public class GameCollectionController extends BaseController {
	
	//----------- FXML Components -----------
    @FXML private TableView<game> gameTableView;                      // Optional table for tabular view (currently unused)
    @FXML private TableColumn<game, String> titleColumn;              // Table column for game title
    @FXML private TableColumn<game, String> genreColumn;              // Table column for genre
    @FXML private TableColumn<game, Integer> yearColumn;              // Table column for release year
    @FXML private Button addGameButton;                               // Opens popup to add a game
    @FXML private Button removeGameButton;                            // (Reserved for future use)
    @FXML private Button refreshButton;                               // Refreshes the game collection
    @FXML private FlowPane gameFlowPane;                              // Visual container for dynamic game cards
    @FXML private ScrollPane gameCollectionScrollPane;                // Scrollable area containing gameFlowPane
    @FXML private ListView<String> gameListView;                      // Reserved for alternate view options
    @FXML private ChoiceBox<String> sortChoiceBox;                    // Dropdown to sort collection
    @FXML private Button filterButton;                                // Navigates to filter page
    @FXML private TextField searchBar;                                // Input field for searching games
    @FXML private Button searchButton;                                // Executes the search
    @FXML private Button settingsButton;                              // Navigates to settings page
    @FXML private Button helpButton;                                  // Navigates to help page
    @FXML private Button logoutButton;                                // Logs out the current user
    private Stage popupStage;                                         // Reusable popup stage
	
    //-----------Services -----------------
    private final GameCollectionService gameCollectionService = new GameCollectionService(); // Retrieves user collection and applies filtering/sorting
    private final NavigationHelper navHelp = new NavigationHelper();                         // Navigation utility
    private PerformanceTracker tracker = new PerformanceTracker();                           // Logs performance metrics
    private userService userSer = new userService();                                          // User session and account management

	//-----------Data Collection-----------
    public List<game> userCollection = new ArrayList<>();            // Full collection for the current user
    public List<game> filteredGameCollection = new ArrayList<>();    // Filtered subset of collection
    private String selectedItem;                                     // Currently selected sort option

	/**
	 * Loads the user's game collection when user data is injected.
	 */
    @Override
    protected void onUserDataLoad() {
    	System.out.println();
        System.out.println("onUserDataLoad() called in GameCollectionController with user ID: " + loggedInUserID);
        userCollection = gameCollectionService.getUserCollection(loggedInUserID);
        loadUserCollection(userCollection);
    }

    /**
     * Initializes the sort dropdown and sets listener to sort the collection accordingly.
     */
    @FXML
    public void initialize() {
        sortChoiceBox.getItems().addAll("Title", "Release Date", "Platform", "Alphabetical");
        sortChoiceBox.setOnAction(event -> {
            selectedItem = sortChoiceBox.getValue();       
            System.out.println("Selected Sort Option: " + selectedItem);
            String searchQuery = "";
            if (filteredGameCollection != null && !filteredGameCollection.isEmpty()) {
                sortFilteredCollection();   // Sort the filtered collection
            } else {
                sortCollection();			// Sort the full collection
            }
        });    
    }
	
    /**
     * Dynamically loads game cards into the FlowPane UI.
     *
     * @param userCollection the list of games to display
     */
    protected void loadUserCollection(List<game> userCollection) {
        if (loggedInUserID <= 0) {
            System.out.println("No user is logged in.");
            return;
        }            
        gameFlowPane.getChildren().clear(); 
        gameFlowPane.setHgap(10); 
        gameFlowPane.setVgap(10);
        gameFlowPane.setPadding(new Insets(20));
        
        for (game g : userCollection) {
            Node gameBox = createGameBox(g);
            gameFlowPane.setPadding(new Insets(20));
            FlowPane.setMargin(gameBox, new Insets(15, 0, 0, 15)); 
            gameFlowPane.getChildren().add(gameBox);
        }
    }

    /**
     * Sorts the full user collection based on the selected item in the dropdown.
     */
    protected void sortCollection() { 
    	String searchQuery = searchBar.getText();
        List<game> sortCollection = gameCollectionService.sortCollection(loggedInUserID, selectedItem, searchQuery);
        if(sortCollection != null) {
			loadUserCollection(sortCollection);
		} else {
			System.out.println("Error occurred while sorting collection");
		}
    }
    
    /**
     * Sorts the filtered collection based on the selected dropdown value.
     *
     * @return true if sorting succeeded, false if empty or error
     */
    protected boolean sortFilteredCollection() {
        if (filteredGameCollection == null || filteredGameCollection.isEmpty()) {
            System.out.println("No games to sort.");
            return false;
        }

        String sortChoice = sortChoiceBox.getValue();
        List<game> sortedFiltered = gameCollectionService.sortFilteredCollection(filteredGameCollection, sortChoice);
        loadUserCollection(sortedFiltered);
        return true;
    }

    /**
     * Handles user search by title input.
     * Measures and logs the performance of the search.
     */
    @FXML
    protected void handleSearchButton() {
    	String title = searchBar.getText();
    	System.out.println("Searching for " + title + " in game colleciton controller");
    	try {
    		long start = System.nanoTime();
    		searchCollection(title);
    		long end = System.nanoTime();
    		System.out.println("Search completed in " + ((end - start) / 1_000_000) + " ms");
    		tracker.logPerformanceData((int)((end - start) / 1_000_000), -1,tracker.getMemoryUsage(), tracker.getGCEvents(), null);
    	}catch (Exception e) {
    		e.printStackTrace();
			System.out.println("Error occurred while searching for game: " + e.getMessage());
    	}
    }
    
    /**
     * Performs a database-level search for games by title and loads results.
     *
     * @param searchedGame game title to search
     */
    protected void searchCollection(String searchedGame) {
    	List<game> searchedUserCollection = gameCollectionService.searchCollection(loggedInUserID, searchedGame);
    	if(searchedUserCollection != null) {
			loadUserCollection(searchedUserCollection);
		} else {
			System.out.println("Error occurred while searching for game");
		}
    }
    
    /**
     * Dynamically creates a game card (StackPane) UI component for a single game.
     *
     * @param gameItem the game to create a UI card for
     * @return a styled StackPane node representing the game
     */
    private Node createGameBox(game gameItem) {
        // Load game cover image or fallback placeholder image 
        Image image = null;
        String imageUrl = gameItem.getCoverImageUrl();
        boolean imageLoaded = false;

        try {
            if (imageUrl != null && !imageUrl.isEmpty()) {
                if (imageUrl.startsWith("http") || imageUrl.startsWith("file:")) { //http for mobygames' cover image, file for user inputed images
                    image = new Image(imageUrl, true);
                } else {
                    File imageFile = new File(imageUrl);
                    if (imageFile.exists()) {
                        image = new Image(imageFile.toURI().toString(), true);
                    }
                }

                if (image != null && !image.isError()) {
                    imageLoaded = true;
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading image: " + imageUrl);
        }

        // Fallback if image failed or was invalid
        if (!imageLoaded) {
            String[] placeholders = {
                "/Images/placeholder1gameGrinding.png",
                "/Images/placeholder2gameGrinding.png"
            };
            
            int randomIndex = (int) (Math.random() * placeholders.length); //random index to randomly select placeholder
            String fallback = placeholders[randomIndex];

            try {
                image = new Image(getClass().getResource(fallback).toExternalForm());
                System.out.println("Using placeholder: " + fallback);
            } catch (Exception e) {
                System.err.println("Could not load placeholder image: " + fallback);
            }
        }

        // UI composition
        ImageView blurredBackground = new ImageView(image);
        blurredBackground.setFitWidth(180);
        blurredBackground.setFitHeight(180);
        blurredBackground.setPreserveRatio(false);
        blurredBackground.setEffect(new javafx.scene.effect.GaussianBlur(20));
        blurredBackground.setOpacity(0.6);

        ImageView coverImage = new ImageView(image);
        coverImage.setFitWidth(100);
        coverImage.setFitHeight(100);
        coverImage.setPreserveRatio(true);
        coverImage.setStyle( "-fx-border-color: #001F3F;" + "-fx-border-width: 3px;" + "-fx-border-radius: 6px;");

        Label title = new Label(gameItem.getTitle());
        title.setWrapText(true);
        title.setPrefWidth(150);  
        title.setMaxWidth(150);
        title.setAlignment(Pos.CENTER);
        title.setTextAlignment(TextAlignment.CENTER);
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: black;");
        title.setEllipsisString("...");
        title.setTooltip(new Tooltip(gameItem.getTitle()));
        title.setMouseTransparent(true);

        VBox gameBox = new VBox(10);
        gameBox.getStyleClass().add("gameBox"); 
        gameBox.setAlignment(Pos.CENTER);
        gameBox.setMinSize(180, 180);
        gameBox.setMaxSize(180, 180);

        String baseStyle = "-fx-background-color: rgba(255,255,255,0.3);"
                         + "-fx-background-radius: 10;"
                         + "-fx-padding: 10px;"
        				 + "-fx-border-color: #e1e1e1;" 
                         + "-fx-border-width: 5px;" 
                         + "-fx-border-radius: 10;"  
                         + "-fx-padding: 10px;";

        gameBox.setStyle(baseStyle);
        gameBox.getChildren().addAll(coverImage, title);
        gameBox.setCursor(Cursor.HAND);


        gameBox.setOnMouseEntered(e -> {
            gameBox.setStyle(baseStyle +
                "-fx-effect: dropshadow(gaussian, #001F3F, 10, 0.5, 0, 0);");
            title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: white;");
        });

        gameBox.setOnMouseExited(e -> {
            gameBox.setStyle(baseStyle);
            title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: black;");
        });


        // Click event to switch to game details
        gameBox.setOnMouseClicked((MouseEvent event) -> {
            System.out.println("Clicked on game: " + gameItem.getTitle());
            switchToGameDetails(gameItem);
        });

        // Stack blurred background + content
        StackPane stack = new StackPane(blurredBackground, gameBox);
        stack.setMinSize(180, 180);
        stack.setMaxSize(180, 180);
        stack.setStyle("-fx-background-radius: 10;");

        // Completed overlay
        if ("Completed".equalsIgnoreCase(gameItem.getCompletionStatus())) {
            ImageView completionIcon = new ImageView(
                new Image(getClass().getResource("/Images/gameTrophy.png").toExternalForm()) //Image for completed games
            );
            completionIcon.setFitWidth(30);
            completionIcon.setFitHeight(25);
            StackPane.setAlignment(completionIcon, Pos.TOP_RIGHT);
            StackPane.setMargin(completionIcon, new javafx.geometry.Insets(5));
            stack.getChildren().add(completionIcon);
        }

        return stack;
    }


    
    /**
     * Opens the add game popup window and assigns user ID to the controller.
     * 
     * @param event the event that triggered the popup
     */
    @FXML
    protected void clickAddGame(javafx.event.ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/AddGamePopup.fxml"));
            Parent root = loader.load();
            AddGamePopupController controller = loader.getController();
            if (controller != null) {
                controller.setUserID(loggedInUserID); 
            } else {
                System.err.println("Error: AddGamePopupController is null!");
            }
            
            popupStage = new Stage(); // Assign popupStage
            popupStage.setScene(new Scene(root));
            popupStage.setTitle("Add Game");
            popupStage.setResizable(false);

            // Get the parent window from the event source 
            Stage parentStage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            popupStage.initOwner(parentStage); // Set the parent stage as the owner of the popup
            popupStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error: Could not load AddGamePopup.fxml!");
        }
    }

    

    /**
	 * Handles the action of clicking the filter button.
	 * 
	 * Navigates to the filter page for advanced filtering options.
	 */
    @FXML
    public void handleFilterButton() {
    	if(loggedInUserID > 0) {
    		navHelp.switchToFilterPage(loggedInUserID, filterButton);
    		System.out.println("Switching to the filter page");
    	} else {
    		System.out.println("Invalid user ID: " + loggedInUserID);
    	}
    }
    
    /**
	 * Sets the filtered collection of games and updates the UI.
	 * 
	 * @param filteredGames the list of filtered games to display
	 * @return true if the collection was updated successfully
	 */
    public boolean setFilteredCollection(List<game> filteredGames) {
        System.out.println("Updating game collection UI with " + filteredGames.size() + " games.");
        filteredGameCollection = filteredGames;
        loadUserCollection(filteredGameCollection);
        System.out.println("Game collection updated successfully.");
        return true;
    }
    

    /**
     * Switches to the Game Details view for the selected game.
     * @param gameItem the game to display details for
     */
    private void switchToGameDetails(game gameItem) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/GameDetails.fxml"));
            if (loader.getLocation() == null) { 
                System.err.println("Error: GameDetails.fxml location is null! Check the path.");
                return;
            }
            
            Parent gameDetailsRoot = loader.load();
            GameDetailsController gameDetailsController = loader.getController();
            
            if (gameDetailsController != null) {
            	gameDetailsController.setUserID(loggedInUserID); 
            } else {
                System.err.println("Error: GameDetailController is null!");
            }
            
            
            gameDetailsController.setGame(gameItem);

            Stage stage = (Stage) gameCollectionScrollPane.getScene().getWindow();
            Scene scene = new Scene(gameDetailsRoot);
            stage.setScene(scene);
            stage.setTitle("Game Details");
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error: Could not load GameDetails.fxml! Check file location.");
        }
    }
    
    /**
	 * Handles the action of clicking the game collection navigation button.
	 * 
	 * Refreshes the game collection by reloading it from the database.
	 */
    @FXML
    protected void handleRefreshButton() {
        System.out.println("Refreshing game collection...");
        if (loggedInUserID > 0) {
            userCollection = gameCollectionService.getUserCollection(loggedInUserID);
            loadUserCollection(userCollection);
        } else {
            System.out.println("No user logged in during refresh.");
        }
    }

    /**
     * Handles the action of clicking the settings button.
     * Navigates to the settings page.
     */
    @FXML
    protected void handleSettingsButton() {
    	try{
    		navHelp.switchToSettingsPage(loggedInUserID, settingsButton);
    	}catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error occurred while switching to settings page: " + e.getMessage());
    	}
    }
    
    /**
	 * Handles the action of clicking the help button.
	 * Navigates to the help page.
	 */
    @FXML
    protected void handleHelpButton() {
		try {
			navHelp.switchToHelpPage(loggedInUserID, helpButton);
			System.out.println("Switching to Help Page.");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error occurred while switching to help page: " + e.getMessage());
		}
	}
    
    /**
	 * Handles the action of clicking the logout button.
	 * Logs out the user and navigates to the login page.
	 */
    @FXML
    protected void handleLogoutButton() {
		System.out.println("Logging out user ID: " + loggedInUserID);
		userSer.logout();
		navHelp.switchToLoginPage(logoutButton);
		System.out.println("Switched to login page after logout.");
    }
    
    public void clearCollection() {
        Platform.runLater(() -> {
            gameFlowPane.getChildren().clear();  
        });
    }


    
}

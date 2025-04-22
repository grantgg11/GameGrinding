package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import models.game;
import services.GameCollectionService;
import services.mobyGamesAPIService;
import services.userService;
import utils.AlertHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import org.apache.hc.core5.http.ParseException;

/**
 * Controller for the "Add Game through the API" screen.
 * This class handles searching for games using the MobyGames API,
 * displaying results, selecting games, and adding them to the user's collection. 
 */
public class AddGameAPIController extends BaseController {
    @FXML private TextField searchField;                        // Search input field for game titles
    @FXML private Button searchButton;                          // Button to trigger API search
    @FXML private FlowPane resultsFlowPane;                     // Container for displaying search result cards
    @FXML private ScrollPane scrollPane;                        // ScrollPane wrapping the results view
    @FXML private CheckBox checkBox;                            // Checkbox to select individual games (used in tile UI)
    @FXML private Button addButton;                             // Button to add selected games to user's collection
    @FXML private StackPane loadingOverlay;                     // Overlay to indicate loading state
    @FXML private Button gameCollectionButton;                  // Button to return to the user's game collection
    @FXML private Button settingsButton;                        // Button to open the settings page
    @FXML private Button helpButton;                            // Button to open the help page
    @FXML private Button manuallyAddButton;                     // Button to manually add a game
    @FXML private Button logoutButton;                          // Button to log out of the application
    @FXML private Button refreashButton;                        // Button to refresh the API search page

    private final mobyGamesAPIService apiService = new mobyGamesAPIService();             // Service for accessing the MobyGames API
    private final GameCollectionService gameCollectionService = new GameCollectionService(); // Service for adding games to the collection
    private final userService userSer = userService.getInstance();                       // Singleton user service for session management
    private final NavigationHelper navHelp = new NavigationHelper();                     // Helper for screen navigation
    private final AlertHelper alert = new AlertHelper();                                 // Helper for displaying alert dialogs

    private final List<game> selectedGames = new ArrayList<>();                          // List of games selected by the user for addition
    private List<game> previousSearchResults = new ArrayList<>();                        // Stores the last API search results for reuse
    private final List<Image> loadedImages = new ArrayList<>();                          // Tracks images loaded during result display for cleanup
    private String lastSearchQuery = "";                                                 // Stores the most recent search query string


    /**
     * Loads user ID and sets up controller-level state.
     */
    @Override
    protected void onUserDataLoad() {
        System.out.println("onUserDataLoad() in AddGameAPIController, user ID = " + loggedInUserID);
    }
    
    /**
     * Sets previous search results (used for navigating back from details view to the add game via API page).
     * @param results The list of previously searched games.
     */
    public void setPreviousSearchResults(List<game> results) {
        this.previousSearchResults = results;
    }
    
    /**
     * Handles search button action. Queries the API and displays results.
     * Shows a loading overlay while request is in progress.
     * Stores the search results for reuse when navigating between views.
     * 
     * @throws ParseException if the response from the API cannot be parsed
     * @throws InterruptedException if the search thread is interrupted
     * @throws ExecutionException if the API task encounters an execution error
     */
    @FXML
    private void handleSearch() throws ParseException, InterruptedException, ExecutionException {
        String query = searchField.getText().trim();       
        if (query.isEmpty()) {
            System.out.println("Search query is empty.");
            return;
        }
        
        lastSearchQuery = query; 
        loadingOverlay.setVisible(true);
        loadingOverlay.setMouseTransparent(false); 
        
        new Thread(() -> {
            try {
                List<game> searchResults = apiService.searchGamesByTitles(query);
                previousSearchResults = new ArrayList<>(searchResults); 
                

                javafx.application.Platform.runLater(() -> {
                    displayResults(searchResults);
                    loadingOverlay.setVisible(false);
                });

            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                loadingOverlay.setVisible(false);
                });
            }
        }).start();
    }
    
    /**
     * Displays a list of games in the FlowPane UI.
     * Limits display to 20 results and ensures cleanup of previous images.
     * 
     * @param games List of games returned from the API.
     */
    public void displayResults(List<game> games) {
    	if (games.size() > 20) {
    	    games = games.subList(0, 20);
    	}

        loadedImages.forEach(img -> {
            if (!img.isError() && img.getProgress() < 1.0) {
                img.cancel();
            }
        });
        loadedImages.clear();

        clearPreviousResults();

        resultsFlowPane.getChildren().clear();
        resultsFlowPane.setPrefWidth(scrollPane.getWidth());
        resultsFlowPane.setMaxWidth(Double.MAX_VALUE);
        resultsFlowPane.setHgap(20);
        resultsFlowPane.setVgap(20);
        resultsFlowPane.setAlignment(Pos.TOP_LEFT);

        Set<Integer> processedGameIDs = new HashSet<>();
        for (game g : games) {
            if (!processedGameIDs.contains(g.getGameID())) {
                VBox gameBox = createGameBox(g);
                resultsFlowPane.getChildren().add(gameBox);
                processedGameIDs.add(g.getGameID());
            }
        }
    }
    
    /**
     * Clears old UI elements and VBoxes to free memory.
     */
    private void clearPreviousResults() {
        resultsFlowPane.getChildren().forEach(node -> {
            if (node instanceof VBox box) {
                box.getChildren().clear();
            }
        });
        resultsFlowPane.getChildren().clear();
    }
    
    /**
     * Tries to load an image from a URL and falls back to a placeholder if unavailable.
     * 
     * @param url The image URL to load.
     * @return The loaded or fallback Image.
     */
    private Image loadImageWithFallback(String url) {
        try {
            if (url != null && !url.isEmpty()) {
                Image img = new Image(url, 120, 120, true, true, true);
                loadedImages.add(img);
                if (!img.isError()) return img;
            }
        } catch (Exception ignored) {}

        try {
            return new Image(getClass().getResource("/Images/placeholder1gameGrinding.png").toExternalForm());
        } catch (Exception e) {
            System.err.println("Failed to load fallback image.");
            return null;
        }
    }
    
    /**
     * Creates a stylized game tile with image, title, hover effects, and checkbox.
     * 
     * @param gameItem The game to display in the tile.
     * @return A VBox representing the game tile.
     */
    private VBox createGameBox(game gameItem) {
    	Image image = loadImageWithFallback(gameItem.getCoverImageUrl());

        ImageView blurredBackground = new ImageView(image);
        blurredBackground.setFitWidth(180);
        blurredBackground.setFitHeight(180);
        blurredBackground.setEffect(new javafx.scene.effect.GaussianBlur(20));
        blurredBackground.setOpacity(0.6);

        ImageView coverImage = new ImageView(image);
        coverImage.setFitWidth(100);
        coverImage.setFitHeight(100);
        coverImage.setPreserveRatio(true);
        coverImage.setStyle( "-fx-border-color: #001F3F;" + "-fx-border-width: 3px;" + "-fx-border-radius: 6px;");

        Label title = new Label(gameItem.getTitle());
        title.setWrapText(true); // No wrapping
        title.setPrefWidth(150);  // Set width constraint
        title.setMaxWidth(150);
        title.setAlignment(Pos.CENTER);
        title.setTextAlignment(TextAlignment.CENTER);
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: black;");
        title.setEllipsisString("...");
        title.setTooltip(new Tooltip(gameItem.getTitle()));
        title.setMouseTransparent(true);

        VBox gameBox = new VBox(10);
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

        CheckBox checkBox = new CheckBox();
        checkBox.setStyle("-fx-opacity: 0.85;");
        checkBox.setOnAction(e -> {
            if (checkBox.isSelected()) {
                selectedGames.add(gameItem);
                System.out.println("Selected: " + gameItem.getTitle());
            } else {
                selectedGames.remove(gameItem);
                System.out.println("Deselected: " + gameItem.getTitle());
            }
            e.consume(); // 🛑 Prevent checkbox click from bubbling up
        });

        StackPane.setAlignment(checkBox, Pos.TOP_RIGHT);
        StackPane.setMargin(checkBox, new javafx.geometry.Insets(5));

        StackPane stack = new StackPane(blurredBackground, gameBox, checkBox);
        stack.setMinSize(180, 180);
        stack.setMaxSize(180, 180);
        stack.setStyle("-fx-background-radius: 10;");

        stack.setOnMouseClicked((MouseEvent event) -> {
            if (!checkBox.isHover()) {
                switchToGameDetails(gameItem);
            }
        });

        VBox container = new VBox(stack);
        container.setAlignment(Pos.TOP_CENTER);
        return container;
    }


    
    /**
     * Handles the action of adding selected games to the user's collection.
     * Iterated through the list and adds it to the database
     * Once complete, the selection is cleared and the view switches to the Game Collection screen.
     * 
     * @throws ParseException if there's an error parsing data during addition
     * @throws InterruptedException if the operation is interrupted
     * @throws ExecutionException if the addition task encounters an execution error
     */
    @FXML
    private void handleAddSelectedGames() throws ParseException, InterruptedException, ExecutionException {
        if (selectedGames.isEmpty() || loggedInUserID <= 0) {
            System.out.println("No games selected or invalid user ID.");
            return;
        }

        System.out.println("Adding selected games to collection:");
        for (game g : selectedGames) {
            boolean added = gameCollectionService.addGameToCollection(g, loggedInUserID);
            System.out.println((added ? "Added: " : "Failed to add: ") + g.getTitle());
        }
        selectedGames.clear();
        navHelp.switchToGameCollection(loggedInUserID, addButton);
    }
    
    /**
     * Navigates to the game details screen.
     * Meant for when traveling from the add game via API page to save previous state.
     * 
     * @param gameItem The game to show details for.
     */
    private void switchToGameDetails(game gameItem) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/GameDetails.fxml"));
            Parent root = loader.load();
            GameDetailsController controller = loader.getController();

            controller.setGame(gameItem);
            controller.setPreviousController(this); 
            controller.setPreviousSearchResults(previousSearchResults);
            controller.setUserID(loggedInUserID);
            controller.setFromAPISearch(true, gameItem);

            Stage stage = (Stage) searchButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Game Details");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Navigation Button Handlers 
    
    @FXML
    private void handleGameCollectionButton() {
		navHelp.switchToGameCollection(loggedInUserID, gameCollectionButton);
	}

	@FXML
	private void handleSettingsButton() {
		navHelp.switchToSettingsPage(loggedInUserID, settingsButton);
	}

	@FXML
	private void handleHelpButton() {
		navHelp.switchToHelpPage(loggedInUserID, helpButton);
	}

	@FXML
	private void handleManuallyAddButton() {
		navHelp.switchToManualAddGamePage(loggedInUserID, manuallyAddButton);
	}

	@FXML
	private void handleLogoutButton() {
		if(userSer.logout()) {
			alert.showInfo("Logout Successful", "You are successfully logged out.", "Thank you for using GameGrinding!");
			navHelp.switchToLoginPage(logoutButton);			
		}else {
			alert.showError("Logout Failed", "Logout failed.", "Please try again.");
		}
	}
	@FXML
	private void handleRefreshButton() {
		navHelp.switchToAPISearchPage(loggedInUserID, refreashButton);
	}
    
}
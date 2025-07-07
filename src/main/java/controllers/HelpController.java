package controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;
import services.userService;
import utils.AlertHelper;

/**
 * Controller for the Help page.
 * Displays a set of expandable help topics using an Accordion interface to assist users
 * with using the GameGrinding application.
 */
public class HelpController extends BaseController {
 
	// ---------- FXML Components ----------
    @FXML private Button gameCollectionButton;    // Navigates to the Game Collection page
    @FXML private Button settingsButton;          // Navigates to the Settings page
    @FXML private Button refreshButton;           // Refreshes the Help view
    @FXML private Button logoutButton;            // Logs out the current user
    @FXML private Accordion helpAccordion;        // Accordion displaying help topics

	
    // ---------- Services ----------
    private NavigationHelper navHelper = new NavigationHelper();  // Handles view transitions
    private userService userSer = new userService();              // Handles user auth and session
    private AlertHelper alert = new AlertHelper();                // For showing alerts to users


    /**
     * Called when user data is loaded into the controller.
     */
    @Override
    protected void onUserDataLoad() {
        System.out.println("HelpController initialized with User ID: " + loggedInUserID);
    }

    /**
	 * Initializes the Help page by setting up help topics in the Accordion.
	 */
    @FXML
    public void initialize() {
    	//All help topics steps to add
        addHelpTopic("How to Add a Game Manually", new String[]{
            "Click the 'Add Game' button at the left column in the Game Collection Page.",
            "Once the Popup window opens, select the 'Add Manually' option.",
            "Fill in required fields like Title, Platform, Developer, etc.",
            "You can also add optional fields like Notes.",
            "Make sure to enter a valid release date in the format YYYY-MM-DD.",
            "If you don't have a cover image, you can leave it blank and a placeholder will be used.",
            "Click 'Submit' to add the game to your collection."
        });

        addHelpTopic("How Add Game from Database", new String[]{
        	"Click the 'Add Game' button at the left column in the Game Collection Page.",
        	"Once the Popup window opens, select the 'Search' option.",
            "Enter a game title and click on the search button.",
            "Once the search results are displayed, browse through the list to find your game(s).",
            "Make sure to click the checkbox in the top right corner of the game you want to add.",
            "Select as many games as you want to add to your collection.",
            "Once you have selected the games, click the 'Add Selected Games' button at the bottom of the page.",
            "Finally, it will redirect you back to the Game Collection Page, where you can view your new games!"
        });

        addHelpTopic("How to Edit or Delete a Game", new String[]{
            "In your collection, click a game to view its details.",
            "Use the Edit or Delete buttons to make changes.",
            "One you click 'Delete', a confirmation dialog will appear.",
            "On confirmation, the game will be removed from your collection.",
            "Once you click 'Edit', you will be redirected to a new page allowing you to modify the game details.",
            "After making changes, click 'Submit' to update the game in your collection.",
        });
        addHelpTopic("How to Filter Your Collection", new String[]{
        	"In the Game Collection Page, look for the filter button on the left side.",
			"You can filter by Genre, Platform, and Completion Status.",
			"Select your desired filters and click the 'Confirm Selection' button.",
			"Your collection will be updated to show only the games that match your criteria.",
			"To clear filters, simply click the 'Game Collection' button to refresh the page."
		});
        addHelpTopic("How to Search for a Game", new String[]{
			"In the Game Collection Page, find the search bar at the top.",
			"Type in the title of the game you want to search for.",
			"Click the search button.",
			"The collection will update to show only games that match your search query.",
			"To clear the search, simply click the 'Game Collection' button to refresh the page or search again with nothin in the search field."
		});
        addHelpTopic("How to Sort Your Collection", new String[]{
			"In the Game Collection Page, find the sort dropdown menu on the left side.",
			"You can sort by Title, Release Date, Platform, or Alphebetical.",
			"Select your desired sorting option.",
			"Your collection will be updated to reflect the new sorting order.",
			"To clear sorting, simply click the 'Game Collection' button to refresh the page."
		});
		addHelpTopic("How to Update Completion Status", new String[]{
			"In your collection, click on a game to view its details.",
			"Find the 'Completion Status' dropdown menu.",
			"Select the new status: Not Started, Playing, or Completed.",
			"The game will now reflect the updated completion status in your collection."
		});
		addHelpTopic("How to View Game Details", new String[]{
			"In your collection, click on any game to view its details.",
			"You will see information like Title, Developer, Publisher, Genre, Platform, and Notes.",
			"You can also see the cover image if available.",
			"To close the details view, simply navigate back to the Game Collection Page or any desired page.",
		});
		addHelpTopic("How to Add Notes to a Game", new String[]{
			"In your collection, click on a game to view its details.",
			"Find the 'Notes' text area.",
			"Type in your notes or comments about the game.",
			"Click 'Save Notes' to update the notes for that game.",
			"Your notes will now be saved and visible whenever you view that game's details."
		});
		addHelpTopic("How to Change your Username or Email", new String[]{
			"Navigate to the Settings Page by clicking the 'Settings' button on the left column.",
			"Once on the Settings Page, look for the 'My Account' section.",
			"Here, you can update your username or email address.",
			"Enter your new username or email in the respective fields.",
			"After making your changes, click the 'Save Changes' button at the bottom of the page.",
		});
		addHelpTopic("How to Change Your Password", new String[]{
			"Navigate to the Settings Page by clicking the 'Settings' button on the left column.",
			"Once on the Settings Page, look for the 'Reset Password' section.",
			"Enter your current password two times, then your new password and confirm it.",
			"Click the 'Save Password' button to save your changes.",
			"Make sure to remember your new password for future logins."
		});
		addHelpTopic("How to view Data Encryption Information", new String[] {
			"In the Settings Page, find the 'Data Encryption' section.",
			"Here, you will see information about how your data is encrypted.",		
		});		
        addHelpTopic("How to Logout", new String[]{
                "Click the 'Logout' button on the bottom left corner of any scree.",
                "Thats it! You will be redirected to the login screen."
            });
        // Default to the first pane being expanded
        applyAccordionDefaults();
		//Apply CSS styles to the Accordion
        Platform.runLater(() -> {
            if (helpAccordion.getScene() != null) {
                helpAccordion.getScene().getStylesheets().add(
                    getClass().getResource("/helpStyle.css").toExternalForm()
                );
            } else {
                System.err.println("Scene is null, stylesheet not applied.");
            }
        });

    }
    
    protected void applyAccordionDefaults() {
        if (helpAccordion.getPanes().isEmpty()) {
            helpAccordion.setExpandedPane(null);
        } else {
            helpAccordion.getPanes().get(0).setExpanded(true);
        }
    }

    
    /**
	 * Adds a help topic to the Accordion.
	 * 
	 * @param title The title of the help topic
	 * @param steps An array of strings representing the steps in the help topic
	 */
    protected void addHelpTopic(String title, String[] steps) {
        VBox content = new VBox(5);
        content.setPadding(new Insets(10));
        for (String step : steps) {
            content.getChildren().add(new Label(step));
        }

        TitledPane pane = new TitledPane(title, content);
        helpAccordion.getPanes().add(pane);
    }

    
    /**
     * Navigates to the Game Collection page when the button is clicked.
     */
	@FXML
	protected void handleGameCollectionButton() {
		System.out.println("Navigating to Game Collection...");
		try {
			navHelper.switchToGameCollection(loggedInUserID, gameCollectionButton);
		} catch (Exception e) {
			System.err.println("Error navigating to Game Collection: " + e.getMessage());
		}
	}
	
	/**
	 * Navigates to the Settings page when the Settings button is clicked.
	 */
	@FXML
	protected void handleSettingsButton() {
		System.out.println("Navigating to Settings...");
		try {
			navHelper.switchToSettingsPage(loggedInUserID, settingsButton);
		} catch (Exception e) {
			System.err.println("Error navigating to Settings: " + e.getMessage());
		}
	}
	
	/**
	 * Navigates to the Help page when the Help button is clicked.
	 */
	@FXML
	protected void handleRefreshButton() {
		System.out.println("Refreshing the view...");
		try {
			navHelper.switchToHelpPage(loggedInUserID, refreshButton);
		}catch (Exception e) {
			System.err.println("Error refreshing the view: " + e.getMessage());
		}
	}
	
	/**
	 * Logs out the user and navigates to the login page.
	 */
	@FXML
	protected void handleLogoutButton() {
		try{
			userSer.logout();
			navHelper.switchToLoginPage(logoutButton);
			System.out.println("User logged out successfully.");
			alert.showInfo("Logout Successful", "You have been logged out successfully.", "Thank you for using GameGrinding");
		} catch (Exception e) {
			System.err.println("Error during logout: " + e.getMessage());
			alert.showError("Logout Error", "An error occurred while logging out. Please try again.", e.getMessage());
		}
		
	}

}

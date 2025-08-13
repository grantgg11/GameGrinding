package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import services.GameCollectionService;

/**
 * Controller for the Delete Game popup window. 
 * Handles the confirmation and cancellation of game deletion.
 */
public class DeleteGamePopupController extends BaseController {

	@FXML private Button confirmButton; //Button to confirm deletion
	@FXML private Button cancelButton; //Button to cancel deletion
	

	//private final CollectionDAO collectionDAO = new CollectionDAO(); // DAO for database operations
	private final GameCollectionService gameCollectionService = new GameCollectionService(); // Service for game collection operations
	private final NavigationHelper navHelp = new NavigationHelper(); // Helper for navigation between views
	private int currentGameID = -1; // ID of the game to be deleted
	
	/**
	 * Called when user data is loaded into the controller.
	 */
    @Override
    protected void onUserDataLoad() {
        System.out.println("DeleteGamePopupController loaded with user ID: " + loggedInUserID);
    }
    
    /**
	 * Sets the current game ID for deletion.
	 * 
	 * @param gameID The ID of the game to be deleted
	 */
	public void setCurrentGameID(int gameID) {
		if(gameID > 0) {
			this.currentGameID = gameID;
			System.out.println("Game ID set in DeteleGamePopupController: " + gameID);
		}else {
			System.err.println("Error: Invalid Game ID received in DeleteGamePopupController: " + gameID);
		}
	}
	
	/**
	 * Handles the action of clicking the confirm button to delete a game.
	 * 
	 * Closes the popup and navigates back to the game collection view.
	 */
	@FXML
	private void handleConfirmButton() {
		if (loggedInUserID <= 0) { 
			System.out.println("Error: No valid user ID set.");
			return;
		}
		if(gameCollectionService.removeGameFromCollection(loggedInUserID, currentGameID)) {
			Stage popupStage = (Stage) confirmButton.getScene().getWindow();
			popupStage.close();
			Stage gameDetailStage = (Stage) popupStage.getOwner();
			gameDetailStage.close();
			navHelp.switchToGameCollection(loggedInUserID, confirmButton);
			System.out.println("Game Deleted Successfully");
		}else {
			System.out.println("Game deletion Unsuccessful");
		}
		
	}
	
	/**
	 * Handles the action of clicking the cancel button to close the popup.
	 * 
	 * Closes the popup without any action.
	 */
	@FXML
	private void handleCancelButton() {
		if(loggedInUserID <= 0) {
			System.out.println("Error: No valid user ID set.");
			return;
		}
		Stage popupStage = (Stage) cancelButton.getScene().getWindow();
		popupStage.close();
		System.out.println("User canceled delete game");
	}
	
}


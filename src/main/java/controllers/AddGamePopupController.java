package controllers;


import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import utils.LoadingOverlay;
import javafx.scene.image.Image;

/**
 * Controller for the popup window that prompts the user to choose between adding a game manually or via the MobyGames API.
 * 
 * This class handles the UI interactions for navigating from the popup to either the manual add screen or the API-based search screen.
 */
public class AddGamePopupController extends BaseController {
	
    @FXML protected Button apiButton;   // Button that leads to the API search option
    @FXML protected Button manualButton; // Button that leads to the manual add option
    

    /**
     * Called when user data is loaded into the controller.
     * Logs the loaded user ID for debugging.
     */
    @Override
    protected void onUserDataLoad() {
        System.out.println("AddGamePopupController received user ID: " + loggedInUserID);
    }
    
    /**
     * Handles the action of clicking the add manually button 
     * 
     * Closes the popup and transitions the main window to the manual game entry interface.
     */
    @FXML
    protected void handleManualAddButton() {
        Stage popupStage = (Stage) manualButton.getScene().getWindow();
        Stage parentStage = (Stage) popupStage.getOwner();

        
        popupStage.close(); 
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/ManualAddGame.fxml"));
            Parent root = loader.load();

            ManualAddGameConttoller controller = loader.getController();
            controller.setUserID(loggedInUserID);

            parentStage.setScene(new Scene(root));
            parentStage.setTitle("Add Game Manually");
            parentStage.setWidth(1295);
            parentStage.setHeight(830);
            parentStage.setResizable(false);

            Image icon = new Image(getClass().getResourceAsStream("/Images/GameGrinding.png"));
            parentStage.getIcons().add(icon);

            parentStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error: Could not load ManualAddGame.fxml!");
        }
    }

    /**
     * Handles the action of clicking the search button 
     * 
     * Closesa the popup, shows a loading overlay, and asynchronously loads the API search screen. 
     */
    @FXML
    protected void handleAPIAddGame() {
        if (loggedInUserID <= 0) {
            System.out.println("Error: No valid user ID set.");
            return;
        }

        Stage popupStage = (Stage) apiButton.getScene().getWindow();
        Stage parentStage = (Stage) popupStage.getOwner();

        popupStage.close();

        Stage loadingStage = LoadingOverlay.show(parentStage);

        new Thread(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/AddGameAPI.fxml"));
                Parent root = loader.load();

                AddGameAPIController controller = loader.getController();
                controller.setUserID(loggedInUserID);

                javafx.application.Platform.runLater(() -> {
                    parentStage.setScene(new Scene(root));
                    parentStage.setTitle("API Search");
                    parentStage.setWidth(1295);
                    parentStage.setHeight(830);
                    parentStage.setResizable(false);
                    parentStage.getIcons().add(new Image(getClass().getResourceAsStream("/Images/GameGrinding.png")));

                    loadingStage.close();
                    parentStage.show();
                });

            } catch (IOException e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(loadingStage::close);
            }
        }).start();
    }
}







        




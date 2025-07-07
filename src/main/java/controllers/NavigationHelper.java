package controllers;

import java.io.IOException;
import javafx.scene.control.Button;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.Window;
import models.game;

/**
 * Class that provides centralized navigation between different screens in the GameGrinding application.
 * 
 * Each method switches the scene or opens a new window, sets the controller,
 * passes the user ID or game data, and handles any errors in the transition.
 */
public class NavigationHelper {
	
    /**
     * Opens a new standalone window from a given FXML path.
     * 
     * @param fxmlPath the FXML file to load
     * @param title the title of the new window
     */
    public void openNewWindow(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = new Stage();
            
            Image icon = new Image(getClass().getResourceAsStream("/Images/GameGrinding.png"));
            ObservableList<Image> icons = stage.getIcons();
            if (icons != null) {
                icons.add(icon);
            }
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.setWidth(1295);
            stage.setHeight(830);
            stage.show();
            closeExistingPopups();
        } catch (IOException e) {
            e.printStackTrace();
            logError("Error: Could not load " + fxmlPath);
        }

    }
    
    protected void logError(String message) {
        System.err.println(message);
    }

    /**
     * Closes any popup window with title Add Game before opening another.
     */
    private void closeExistingPopups() {
        for (Window window : Stage.getWindows()) {
            if (window.isShowing() && window instanceof Stage && ((Stage) window).getTitle().equals("Add Game")) {
                ((Stage) window).close();
                break;
            }
        }  
    }
    
    /** Loads GameCollection.fxml and passes the user ID. */
    public void switchToGameCollection(int userID, Button button) {
    	try {
    		FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/GameCollection.fxml"));
    		Parent gameCollectionRoot = loader.load();
    		
            GameCollectionController gameCollectionController = loader.getController();

            if (gameCollectionController != null) {
            	gameCollectionController.setUserID(userID); 
                System.out.println("setUserID called on BaseController with user ID: " + userID);
            } else {
                System.err.println("GameCollectionController is null!");
            }
    		
    		Stage stage = (Stage) button.getScene().getWindow();
    		
    		Scene scene = new Scene(gameCollectionRoot);
    		Image icon = new Image(getClass().getResourceAsStream("/Images/GameGrinding.png"));
    		ObservableList<Image> icons = stage.getIcons();
    		if (icons != null) {
    		    icons.add(icon);
    		}

    		stage.setWidth(1295);
    		stage.setHeight(835);
    		stage.setScene(scene);
    		stage.setTitle("Game Collection");
    		stage.setResizable(false);
    		stage.show();
    	} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Error: Could not load GameCollection.fxml!");
		}
    }
    	
    /** Loads Settings.fxml and passes the user ID. */
        public void switchToSettingsPage(int userID, Button button) {
        	try {
        		FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Settings.fxml"));
        		Parent gameCollectionRoot = loader.load();
        		
                SettingsController settingsController = loader.getController();
              
                if (settingsController == null) {
                    System.err.println("Error: GameCollectionController is null!");
                    return;
                }
                settingsController.setUserID(userID);
                System.out.println("User ID passed to SettingsController: " + userID);
        		
        		Stage stage = (Stage) button.getScene().getWindow();
        		
        		Scene scene = new Scene(gameCollectionRoot);
        		Image icon = new Image(getClass().getResourceAsStream("/Images/GameGrinding.png"));
        		ObservableList<Image> icons = stage.getIcons();
        		if (icons != null) {
        		    icons.add(icon);
        		}

        		stage.setWidth(1295);
        		stage.setHeight(835);
        		stage.setScene(scene);
        		stage.setTitle("Settings");
        		stage.setResizable(false);
        		stage.show();
        	} catch (IOException e) {
    			e.printStackTrace();
    			System.err.println("Error: Could not load Settings.fxml!");
    		}
    }
        
        /** Loads Help.fxml and passes the user ID. */
        public void switchToHelpPage(int userID, Button button) {
			try {
				FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/HelpPage.fxml"));
				Parent helpRoot = loader.load();
				
				HelpController helpController = loader.getController();
				
				if (helpController == null) {
					System.err.println("Error: HelpController is null!");
					return;
				}
				helpController.setUserID(userID); 
				System.out.println("User ID passed to HelpController: " + userID);
				Stage stage = (Stage) button.getScene().getWindow();
				
				Scene scene = new Scene(helpRoot);
				Image icon = new Image(getClass().getResourceAsStream("/Images/GameGrinding.png"));
				ObservableList<Image> icons = stage.getIcons();
				if (icons != null) {
				    icons.add(icon);
				}

				stage.setWidth(1295);
				stage.setHeight(835);
				stage.setScene(scene);
				stage.setTitle("Help");
				stage.setResizable(false);
				stage.show();
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println("Error: Could not load Help.fxml!");
			}
        }
        
        /** Loads EditGame.fxml and passes the user ID and selected game. */
        public void switchToEditGamePage(int userID, game selectedGame, Button button) {
			try {
				FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/EditGame.fxml"));
				Parent editGameRoot = loader.load();
				
				EditGameController editGameController = loader.getController();
				
				if (editGameController == null) {
					System.err.println("Error: EditGameController is null!");
					return;
				}
				editGameController.setUserID(userID);
				editGameController.setGame(selectedGame);
				System.out.println("User ID: " + userID + ", Game ID passed to EditGameController: " + selectedGame.getGameID());
				
				Stage stage = (Stage) button.getScene().getWindow();
				
				Scene scene = new Scene(editGameRoot);
				Image icon = new Image(getClass().getResourceAsStream("/Images/GameGrinding.png"));
				ObservableList<Image> icons = stage.getIcons();
				if (icons != null) {
				    icons.add(icon);
				}
				stage.setWidth(1295);
				stage.setHeight(835);
				stage.setScene(scene);
				stage.setTitle("Edit Game");
				stage.setResizable(false);
				stage.show();
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println("Error: Could not load EditGame.fxml!");
			}
        }
        
        /** Loads ForgotPassword.fxml. */
        public void switchToForgotPasswordPage(Button button) {
			try {
				FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/ForgotPassword.fxml"));
				Parent forgotPasswordRoot = loader.load();
				
				Stage stage = (Stage) button.getScene().getWindow();
				
				Scene scene = new Scene(forgotPasswordRoot);
				Image icon = new Image(getClass().getResourceAsStream("/Images/GameGrinding.png"));
				ObservableList<Image> icons = stage.getIcons();
				if (icons != null) {
				    icons.add(icon);
				}

				stage.setWidth(1295);
				stage.setHeight(835);
				stage.setScene(scene);
				stage.setTitle("Forgot Password");
				stage.setResizable(false);
				stage.show();
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println("Error: Could not load ForgotPassword.fxml!");
			}
		}
        
        /** Loads Main.fxml. */
        public void switchToLoginPage(Button button) {
        	try {
        		FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Main.fxml"));
        		Parent forgotPasswordRoot = loader.load();
			
        		Stage stage = (Stage) button.getScene().getWindow();
			
        		Scene scene = new Scene(forgotPasswordRoot);
        		Image icon = new Image(getClass().getResourceAsStream("/Images/GameGrinding.png"));
        		ObservableList<Image> icons = stage.getIcons();
        		if (icons != null) {
        		    icons.add(icon);
        		}

        		stage.setWidth(1295);
        		stage.setHeight(835);
        		stage.setScene(scene);
        		stage.setTitle("Forgot Password");
        		stage.setResizable(false);
        		stage.show();
        	} catch (IOException e) {
        		e.printStackTrace();
        		System.err.println("Error: Could not load Main.fxml!");
		}
	}
        
    /** Loads ManualAddGame.fxml and passes the user ID. */
    public void switchToManualAddGamePage(int userID, Button button) {
    	try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/ManualAddGame.fxml"));
			Parent manualAddGameRoot = loader.load();
            ManualAddGameConttoller  manualAddGameController = loader.getController();
			manualAddGameController.setUserID(userID);
			System.out.println("User ID passed to ManualAddGameController: " + userID);
			Stage stage = (Stage) button.getScene().getWindow();
			
			Scene scene = new Scene(manualAddGameRoot);
			Image icon = new Image(getClass().getResourceAsStream("/Images/GameGrinding.png"));
			ObservableList<Image> icons = stage.getIcons();
			if (icons != null) {
			    icons.add(icon);
			}

			stage.setWidth(1295);
			stage.setHeight(835);
			stage.setScene(scene);
			stage.setTitle("Add Game Manually");
			stage.setResizable(false);
			stage.show();
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Error: Could not load ManualAddGame.fxml!");
		}
	}
    
    /** Loads AddGameAPI.fxml and passes the user ID. */
    public void switchToAPISearchPage(int userID, Button button) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/AddGameAPI.fxml"));
			Parent apiSearchRoot = loader.load();
            AddGameAPIController controller = loader.getController();
            controller.setUserID(userID);
            System.out.println("User ID passed to AddGameAPIController: " + userID);
			Stage stage = (Stage) button.getScene().getWindow();
			
			Scene scene = new Scene(apiSearchRoot);
			Image icon = new Image(getClass().getResourceAsStream("/Images/GameGrinding.png"));
			ObservableList<Image> icons = stage.getIcons();
			if (icons != null) {
			    icons.add(icon);
			}

			stage.setWidth(1295);
			stage.setHeight(835);
			stage.setScene(scene);
			stage.setTitle("API Search");
			stage.setResizable(false);
			stage.show();
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Error: Could not load AddGameAPI.fxml!");
		}
    }
    
    /** Loads FilterCollection.fxml and passes the user ID. */
    public void switchToFilterPage(int userID, Button filterButton) {
    	try {
    		FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/FilterCollection.fxml"));
    		Parent filterCollectionRoot = loader.load();
    		
            FilterCollectionController filterController = loader.getController();
          
            if (filterController == null) {
                System.err.println("Error: GameCollectionController is null!");
                return;
            }
            filterController.setUserID(userID);
            System.out.println("User ID passed to FilterCollectionController: " + userID);
    		
    		Stage stage = (Stage) filterButton.getScene().getWindow();
    		
    		Scene scene = new Scene(filterCollectionRoot);
    		Image icon = new Image(getClass().getResourceAsStream("/Images/GameGrinding.png"));
    		ObservableList<Image> icons = stage.getIcons();
    		if (icons != null) {
    		    icons.add(icon);
    		}

    		stage.setWidth(1295);
    		stage.setHeight(835);
    		stage.setScene(scene);
    		stage.setTitle("Filter Collection");
    		stage.setResizable(false);
    		stage.show();
    	} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Error: Could not load GameCollection.fxml!");
		}
    }
}      

    
   

    


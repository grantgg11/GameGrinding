package application;

import reports.DatabaseIntegrityBackgroundJob;

import java.sql.Connection;

import database.DatabaseInitializer;
import database.DatabaseManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;

/**
 * Entry point for the GameGrinding JavaFX application. 
 * 
 * This class sets up the primary stage, loads the main UI from FXML, 
 * and applies proper application settings. 
 * On start up it runs a database integrity check to make sure the data is valid. 
 */
public class Main extends Application {
	
	/**
	 * Starts the JavaFX application. 
	 * Loads the main FXML layout, configures the window, and launches the background
     * database integrity validation job.
	 * 
	 * @param primaryStage the main stage provided by the JavaFX runtime
	 */
	@Override
	public void start(Stage primaryStage) {
		try {
			System.out.println("START: Loading FXML...");
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Main.fxml"));
			Parent root = loader.load();
			System.out.println("END: FXML loaded successfully!");
			
			System.out.println("Initializing database schema...");
			Connection conn = new DatabaseManager().getConnection();
			DatabaseInitializer.initializeSchema(conn);
			
			Scene scene = new Scene(root, 400, 400);
			
			System.out.println("Loading icon...");
			Image icon = new Image(getClass().getResourceAsStream("/Images/GameGrinding.png"));
			primaryStage.getIcons().add(icon);
			primaryStage.setTitle("GameGrinding");
			primaryStage.setWidth(1295);
			primaryStage.setHeight(837);
			primaryStage.setScene(scene);
			primaryStage.setResizable(false);
			
			System.out.println("Showing stage...");
			primaryStage.show();

			System.out.println("Running DB integrity job...");
			runDatabaseIntegrityJobInBackground();

		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Error: Could not load Main.fxml!");
		    // Show a visible dialog in case user runs the EXE
		    javafx.application.Platform.runLater(() -> {
		        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
		                javafx.scene.control.Alert.AlertType.ERROR);
		        alert.setTitle("GameGrinding - Error");
		        alert.setHeaderText("An error occurred while launching the application.");
		        alert.setContentText(e.getMessage());
		        alert.showAndWait();
		    });
		    try (java.io.PrintWriter out = new java.io.PrintWriter("startup-error.log")) {
		        e.printStackTrace(out);
		    } catch (Exception ex) {
		        ex.printStackTrace();
		    }
		}
	}
	 
	/**
	 * Runs a background job to validate database integrity.
	 * Executes the database validation on a daemon thread to ensure it
     * does not block or delay the application UI from loading.
	 */
	private void runDatabaseIntegrityJobInBackground() {
		Thread backgroundJob = new Thread(() -> {
			try {
				System.out.println("Running background DB integrity check...");
				DatabaseIntegrityBackgroundJob job = new DatabaseIntegrityBackgroundJob();
				job.runIntegrityCheck();
			} catch (Exception e) {
				System.err.println("Failed to run background DB integrity check:");
				e.printStackTrace();
			}
		});
		backgroundJob.setDaemon(true); // Make sure this thread doesn't prevent app from closing
		backgroundJob.start();
	}
	
	/**
	 * Launches the application
	 * 
	 * @param args command-line arguments (not used)
	 */
	public static void main(String[] args) {
	    try {
	    	System.out.println("JavaFX Runtime Version: " + System.getProperty("javafx.runtime.version"));
	        launch(args);
	    } catch (Exception e) {
	        e.printStackTrace();
	        System.err.println("Fatal error: " + e.getMessage());
	    }
	}
}


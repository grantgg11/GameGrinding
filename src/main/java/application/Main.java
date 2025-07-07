package application;

import reports.DatabaseIntegrityBackgroundJob;
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
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Main.fxml"));
			Parent root = loader.load();

			Scene scene = new Scene(root, 400, 400);

			Image icon = new Image(getClass().getResourceAsStream("/Images/GameGrinding.png"));
			primaryStage.getIcons().add(icon);
			primaryStage.setTitle("GameGrinding");
			primaryStage.setWidth(1295);
			primaryStage.setHeight(837);
			primaryStage.setScene(scene);
			primaryStage.setResizable(false);

			primaryStage.show();

			runDatabaseIntegrityJobInBackground();

		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Error: Could not load Main.fxml!");
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
		launch(args);
	}
}


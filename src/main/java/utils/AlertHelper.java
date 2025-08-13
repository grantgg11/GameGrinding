package utils;

import java.util.List;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

/**
 * AlertHelper provides utility methods for displaying standardized JavaFX alert dialogs
 * throughout the GameGrinding application.
 * 
 * This class is responsible for:
 * - Displaying error alerts to inform the user when an operation fails or input is invalid.
 * - Displaying informational alerts to confirm successful actions or provide general updates.
 * - Displaying multi-line alerts when there are multiple messages to present to the user
 *   in a single dialog.
 * 
 * Key features:
 * - Uses JavaFX's Alert class with preconfigured alert types (ERROR and INFORMATION).
 * - Supports custom titles, headers, and content messages for flexibility.
 * - Allows passing multiple messages as a list, which are joined into a single multi-line string.
 * 
 * This class ensures that all alerts across the application have a consistent look and behavior,
 * improving user experience and reducing repetitive alert setup code in controllers and services.
 */
public class AlertHelper {
	

    /**
     * Displays an error alert with a title, header, and content message.
     *
     * @param title   the title of the alert window
     * @param header  the header text (can be null)
     * @param content the main content message of the alert
     */
	public void showError(String title, String header, String content) {
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setContentText(content);
		alert.showAndWait();
	}
	
    /**
     * Displays an informational alert with a title, header, and content message.
     *
     * @param title   the title of the alert window
     * @param header  the header text (can be null)
     * @param content the main content message of the alert
     */
	public void showInfo(String title, String header, String content) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setContentText(content);
		alert.showAndWait();
	}
	
    /**
     * Displays an error alert with a given title and a list of messages,
     * joined as multi-line content in the alert body.
     *
     * @param title   the title of the alert
     * @param content a list of strings that will be joined by newlines
     */
	public void showAlert(String title, List<String> content) {
		Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        String message = String.join("\n", content);
        alert.setContentText(message);
        alert.showAndWait();
	}
}

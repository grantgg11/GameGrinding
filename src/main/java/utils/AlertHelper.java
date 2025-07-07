package utils;

import java.util.List;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

/**
 * Utility class for displaying JavaFX alert dialogs in a standardized way.
 * Provides methods to show error, information, and custom multi-line alerts.
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

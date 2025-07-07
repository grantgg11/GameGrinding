package utils;

import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

/**
 * Unit tests for the AlertHelper utility class.
 * 
 * Verifies the creation and configuration of JavaFX Alert dialogs for different
 * alert types such as error, informational messages, and lists.
 * Uses Mockito's mockConstruction to intercept and inspect Alert instantiations.
 */

class AlertHelperTest {
 
	/**
	 * Tests the showError method to ensure that an error-type Alert is constructed
	 * and configured with the correct title, header, and content text.
	 * Verifies that the alert is displayed using showAndWait.
	 */
	  @Test
	    void testShowError() {
	        try (MockedConstruction<Alert> mocked = mockConstruction(Alert.class,
	                (mock, context) -> {
	                    when(mock.getAlertType()).thenReturn(AlertType.ERROR);
	                })) {

	            AlertHelper alertHelper = new AlertHelper();
	            alertHelper.showError("Error Title", "Header Text", "Something went wrong");

	            Alert mockAlert = mocked.constructed().get(0);
	            verify(mockAlert).setTitle("Error Title");
	            verify(mockAlert).setHeaderText("Header Text");
	            verify(mockAlert).setContentText("Something went wrong");
	            verify(mockAlert).showAndWait();
	        }
	    }

	  /**
	   * Tests the showInfo method to ensure that an informational Alert is constructed
	   * and configured with the correct title, header, and content text.
	   * Verifies that the alert is displayed using showAndWait.
	   */
	    @Test
	    void testShowInfo() {
	        try (MockedConstruction<Alert> mocked = mockConstruction(Alert.class,
	                (mock, context) -> {
	                    when(mock.getAlertType()).thenReturn(AlertType.INFORMATION);
	                })) {

	            AlertHelper alertHelper = new AlertHelper();
	            alertHelper.showInfo("Info Title", "Header Info", "Informational message");

	            Alert mockAlert = mocked.constructed().get(0);
	            verify(mockAlert).setTitle("Info Title");
	            verify(mockAlert).setHeaderText("Header Info");
	            verify(mockAlert).setContentText("Informational message");
	            verify(mockAlert).showAndWait();
	        }
	    }

	    /**
	     * Tests the showAlert method that takes a list of strings.
	     * Verifies that the Alert content displays the list items joined by newlines,
	     * and that the alert is displayed with the correct title and no header.
	     */
	    @Test
	    void testShowAlertList() {
	        try (MockedConstruction<Alert> mocked = mockConstruction(Alert.class)) {
	            AlertHelper alertHelper = new AlertHelper();
	            alertHelper.showAlert("List Title", Arrays.asList("Line 1", "Line 2", "Line 3"));

	            Alert mockAlert = mocked.constructed().get(0);
	            verify(mockAlert).setTitle("List Title");
	            verify(mockAlert).setHeaderText(null);
	            verify(mockAlert).setContentText("Line 1\nLine 2\nLine 3");
	            verify(mockAlert).showAndWait();
	        }
	    }
}

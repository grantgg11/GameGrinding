package controllers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import models.game;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;


/** 
 * Unit tests for the NavigationHelper class in the GameGrinding application.
 * This test class validates the navigation methods for switching between different pages
 * within the application, ensuring that they handle IOExceptions gracefully
 * and do not throw unexpected errors.
 */
@ExtendWith(JavaFXThreadingExtension.class)
class NavigationHelperTest {

    private NavigationHelper navHelper;
    private Button mockButton;
    private game mockGame;

    /**
	 * Initializes the NavigationHelper and mocks necessary components before each test.
	 * Sets up a mock button and stage to simulate navigation actions without launching the full UI.
	 */
    @BeforeEach
    void setup() {
        navHelper = new NavigationHelper();
        mockButton = mock(Button.class);
        Stage mockStage = mock(Stage.class);
        Scene mockScene = mock(Scene.class);
        when(mockButton.getScene()).thenReturn(mockScene);
        when(mockScene.getWindow()).thenReturn(mockStage);

        mockGame = mock(game.class);
        when(mockGame.getGameID()).thenReturn(42);
    }
    
    /**
     * Tests that calling switchToGameCollection does not throw an exception, confirming it handles IOException gracefully.
     */
    @Test
    void switchToGameCollection_shouldHandleIOException() {
        assertDoesNotThrow(() -> navHelper.switchToGameCollection(1, mockButton));
    }

    /**
     * Tests that calling switchToSettingsPage does not throw an exception, ensuring graceful error handling for FXML loading issues.
     */
    @Test
    void switchToSettingsPage_shouldHandleIOException() {
        assertDoesNotThrow(() -> navHelper.switchToSettingsPage(1, mockButton));
    }

    /**
     * Tests that calling switchToHelpPage completes without exception, validating that navigation to Help view is safe under error conditions.
     */
    @Test
    void switchToHelpPage_shouldHandleIOException() {
        assertDoesNotThrow(() -> navHelper.switchToHelpPage(1, mockButton));
    }

    /**
     * Tests that calling switchToEditGamePage does not throw, verifying it handles navigation and FXML errors even when passing game data.
     */
    @Test
    void switchToEditGamePage_shouldHandleIOException() {
        assertDoesNotThrow(() -> navHelper.switchToEditGamePage(1, mockGame, mockButton));
    }

    /**
     * Ensures switchToForgotPasswordPage handles IOExceptions without crashing.
     */
    @Test
    void switchToForgotPasswordPage_shouldHandleIOException() {
        assertDoesNotThrow(() -> navHelper.switchToForgotPasswordPage(mockButton));
    }

    /**
     * Verifies that switchToLoginPage executes safely without throwing, even if navigation fails internally.
     */
    @Test
    void switchToLoginPage_shouldHandleIOException() {
        assertDoesNotThrow(() -> navHelper.switchToLoginPage(mockButton));
    }

    /**
	 * Tests that switchToAddGamePage does not throw an exception, confirming it handles navigation errors gracefully.
	 */
    @Test
    void switchToManualAddGamePage_shouldHandleIOException() {
        assertDoesNotThrow(() -> navHelper.switchToManualAddGamePage(1, mockButton));
    }

    /**
	 * Tests that switchToGameDetailPage does not throw an exception, ensuring it handles navigation errors gracefully.
	 */
    @Test
    void switchToAPISearchPage_shouldHandleIOException() {
        assertDoesNotThrow(() -> navHelper.switchToAPISearchPage(1, mockButton));
    }

    /**
	 * Tests that switchToFilterPage does not throw an exception, confirming it handles navigation errors gracefully.
	 */
    @Test
    void switchToFilterPage_shouldHandleIOException() {
        assertDoesNotThrow(() -> navHelper.switchToFilterPage(1, mockButton));
    }
}

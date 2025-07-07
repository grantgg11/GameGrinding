package controllers;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import services.GameCollectionService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static controllers.TestUtils.*;

/**
 * Unit tests for the DeleteGamePopupController class.
 *
 * This controller manages a confirmation popup for deleting a game from the user's collection.
 * These tests verify the correct behavior for:
 * - Setting and validating the current game ID
 * - Handling confirm and cancel button actions
 * - Deleting a game and navigating appropriately
 * - Error handling when deletion fails or the user is invalid
 */
@ExtendWith(ApplicationExtension.class)
class DeleteGamePopupControllerTest {

    private DeleteGamePopupController controller;
    private GameCollectionService mockGameCollectionService;
    private NavigationHelper mockNavHelper;

    private Button confirmButton;
    private Button cancelButton;

    /**
     * Initializes the controller, injects mocked dependencies, and sets up private fields.
     * Ensures all controller internals are correctly configured before each test.
     */
    @BeforeEach
    void setUp() {
        controller = new DeleteGamePopupController() {
            {
                try {
                    setPrivateField(this, "gameCollectionService", mock(GameCollectionService.class));
                    setPrivateField(this, "navHelp", mock(NavigationHelper.class));
                } catch (Exception e) {
                    fail("Failed to inject private fields: " + e.getMessage());
                }
            }

            @Override
            protected void onUserDataLoad() {
            }
        };
          
        mockGameCollectionService = (GameCollectionService) getPrivateField(controller, "gameCollectionService");
        mockNavHelper = (NavigationHelper) getPrivateField(controller, "navHelp");

        confirmButton = mock(Button.class);
        cancelButton = mock(Button.class);

        setPrivateField(controller, "confirmButton", confirmButton);
        setPrivateField(controller, "cancelButton", cancelButton);

        controller.loggedInUserID = 1;
    }
    
    /**
     * Verifies that onUserDataLoad prints the correct user ID message to the console.
     */
    @Test
    void testOnUserDataLoad_printsUserIDMessage() {
        DeleteGamePopupController realController = new DeleteGamePopupController();
        realController.loggedInUserID = 42;

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        try {
            realController.onUserDataLoad();
            String output = outContent.toString().trim();
            assertTrue(output.contains("DeleteGamePopupController loaded with user ID: 42"));
        } finally {
            System.setOut(originalOut); 
        }
    }


    /**
     * Verifies that setting a valid game ID correctly stores the value.
     */
    @Test
    void testSetCurrentGameID_valid_setsID() {
        controller.setCurrentGameID(123);
        assertEquals(123, getPrivateField(controller, "currentGameID"));
    }

    /**
     * Ensures that providing an invalid game ID does not change the currentGameID.
     */
    @Test
    void testSetCurrentGameID_invalid_doesNotSetID() {
        controller.setCurrentGameID(-5);
        assertEquals(-1, getPrivateField(controller, "currentGameID"));
    }

    /**
	 * Tests that handleConfirmButton correctly closes the popup and navigates to the game collection
	 * when a valid user is logged in and the deletion is successful.
	 */
    @Test
    void testHandleConfirmButton_validUserAndSuccess_deletesAndNavigates() {
        controller.setCurrentGameID(100);

        Stage gameDetailStage = mock(Stage.class);
        Stage popupStage = mock(Stage.class);

        Scene scene = mock(Scene.class);
        when(confirmButton.getScene()).thenReturn(scene);
        when(scene.getWindow()).thenReturn(popupStage);
        when(popupStage.getOwner()).thenReturn(gameDetailStage);

        when(mockGameCollectionService.removeGameFromCollection(1, 100)).thenReturn(true);

        invokePrivateMethod(controller, "handleConfirmButton");

        verify(popupStage).close();
        verify(gameDetailStage).close();
        verify(mockNavHelper).switchToGameCollection(1, confirmButton);
    }

    /**
	 * Tests that handleConfirmButton does not navigate when the deletion fails.
	 */
    @Test
    void testHandleConfirmButton_deletionFails_doesNotNavigate() {
        controller.setCurrentGameID(101);
        when(mockGameCollectionService.removeGameFromCollection(1, 101)).thenReturn(false);
        invokePrivateMethod(controller, "handleConfirmButton");
        verify(mockNavHelper, never()).switchToGameCollection(anyInt(), any());
    }

    /**
	 * Tests that handleConfirmButton does not attempt to delete when the user ID is invalid.
	 */
    @Test
    void testHandleConfirmButton_invalidUser_skipsDeletion() {
        controller.loggedInUserID = -1;
        invokePrivateMethod(controller, "handleConfirmButton");
        verify(mockGameCollectionService, never()).removeGameFromCollection(anyInt(), anyInt());
    }

    /**
     * Verifies that clicking the cancel button with a valid user closes the popup.
     */
    @Test
    void testHandleCancelButton_validUser_closesPopup() {
        Stage popupStage = mock(Stage.class); 
        Scene scene = mock(Scene.class);

        when(cancelButton.getScene()).thenReturn(scene);
        when(scene.getWindow()).thenReturn(popupStage);
        invokePrivateMethod(controller, "handleCancelButton");
        verify(popupStage).close();
    }

    /**
     * Ensures that when an invalid user ID is set, clicking cancel still does not crash the application.
     */
    @Test
    void testHandleCancelButton_invalidUser_logsError() {
        controller.loggedInUserID = -1;
        invokePrivateMethod(controller, "handleCancelButton");
    }
}

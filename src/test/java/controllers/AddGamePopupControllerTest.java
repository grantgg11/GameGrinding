package controllers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.mockito.MockedConstruction;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.util.WaitForAsyncUtils;
import static org.junit.jupiter.api.parallel.Resources.SYSTEM_OUT;

/**
 * Unit tests for the AddGamePopupController class in the GameGrinding application.
 *
 * This test class validates the popup controller responsible for initiating game addition
 * either through manual entry or via the MobyGames API. It ensures proper stage behavior,
 * navigation logic, and error handling.
 */
@ExtendWith({ ApplicationExtension.class, JavaFXThreadingExtension.class }) 
@ResourceLock(SYSTEM_OUT)
class AddGamePopupControllerTest {

    private AddGamePopupController controller;

    /**
     * Initializes the controller and mocks buttons before each test.
     */
    @BeforeEach
    void setUp() {
        controller = new AddGamePopupController();
        controller.manualButton = mock(Button.class);
        controller.apiButton = mock(Button.class);
    }

    /**
	 * Ensures all JavaFX windows are closed before any tests run to prevent interference.
	 */
    @BeforeAll
    static void wipeUiOnceAtStart() {
        Platform.runLater(() -> {
            var snapshot = new ArrayList<>(Window.getWindows());
            for (Window w : snapshot) {
                if (w != null && w.isShowing()) {
                    if (w instanceof Stage s) s.close();
                    else w.hide();
                }
            }
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    /**
     * Verifies that onUserDataLoad executes without throwing and logs the user ID.
     */
    @Test
    void onUserDataLoad_shouldPrintUserID() {
        AddGamePopupController controller = new AddGamePopupController();
        TestUtils.setPrivateField(controller, "loggedInUserID", 42);

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        try {
            System.setOut(new PrintStream(outContent));
            controller.onUserDataLoad(); 
        } finally {
            System.setOut(originalOut);
        }

        String output = outContent.toString().trim();
        assertTrue(output.contains("AddGamePopupController received user ID: 42"),
            "Expected output to confirm received user ID.");
    }
    /**
     * Ensures that clicking the manual add button closes the popup and transitions to the ManualAddGame.fxml view without errors.
     */
    @Test
    void handleManualAddButton_shouldClosePopupAndOpenManualScreen() throws Exception {
        Platform.runLater(() -> {
            Button manualButton = new Button();
            VBox root = new VBox(manualButton);
            Scene popupScene = new Scene(root);
            Stage parentStage = new Stage();
            Stage popupStage = new Stage();

            popupStage.setScene(popupScene);
            popupStage.initOwner(parentStage);  
            popupStage.show(); 

            AddGamePopupController controller = new AddGamePopupController();
            controller.manualButton = manualButton;
            controller.loggedInUserID = 1;

            try (MockedConstruction<FXMLLoader> mocked = mockConstruction(FXMLLoader.class,
                    (mock, context) -> {
                        when(mock.load()).thenReturn(new VBox());
                        ManualAddGameConttoller mockManualController = mock(ManualAddGameConttoller.class);
                        when(mock.getController()).thenReturn(mockManualController);
                    })) {
                controller.handleManualAddButton();
            }
            assertFalse(popupStage.isShowing(), "Popup stage should be closed after clicking manual add");
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    /**
     * Validates that the controller handles IOExceptions during manual screen loading and prints an appropriate error message.
     */
    @Test
    void handleManualAddButton_shouldHandleIOException() throws Exception {
        Platform.runLater(() -> {
            Button manualButton = new Button();
            VBox root = new VBox(manualButton);
            Scene popupScene = new Scene(root);
            Stage parentStage = new Stage();
            Stage popupStage = new Stage();
            popupStage.setScene(popupScene);
            popupStage.initOwner(parentStage);
            popupStage.show();

            AddGamePopupController controller = new AddGamePopupController();
            controller.manualButton = manualButton;
            controller.loggedInUserID = 1;

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(out));

            try (MockedConstruction<FXMLLoader> mocked = mockConstruction(FXMLLoader.class,
                    (mock, context) -> {
                        when(mock.load()).thenThrow(new IOException("Test failure"));
                    })) {
                controller.handleManualAddButton();
            }
            System.out.flush(); 
            System.setOut(originalOut); 

            String output = out.toString().trim();
            assertTrue(output.contains("Error: Could not load ManualAddGame.fxml!"), "Expected error message not found. Output was:\n" + output);
        });
        WaitForAsyncUtils.waitForFxEvents();
    }


    /**
     * Ensures the controller gracefully handles cases where the user ID is invalid (zero or less) when attempting to use the API add screen.
     */
    @Test
    void handleAPIAddGame_shouldPrintErrorWhenUserIDInvalid() {
        controller.loggedInUserID = 0;
        assertDoesNotThrow(() -> controller.handleAPIAddGame());
    }

    /**
     * Tests that when a valid user ID is set, clicking the API add button loads the AddGameAPI.fxml screen successfully and does not crash.
     */
    @Test
    void handleAPIAddGame_shouldLoadAPIScreen_whenUserIDIsValid() throws Exception {
        Platform.runLater(() -> {
            try {
                Button apiButton = new Button();
                VBox root = new VBox(apiButton);
                Scene scene = new Scene(root);
                Stage parentStage = new Stage();
                Stage popupStage = new Stage();
                popupStage.setScene(scene);
                popupStage.initOwner(parentStage);
                popupStage.show();

                AddGamePopupController controller = new AddGamePopupController();
                controller.apiButton = apiButton;
                controller.loggedInUserID = 1;

                try (MockedConstruction<FXMLLoader> mockedLoader = mockConstruction(FXMLLoader.class,
                        (mock, context) -> {
                            try {
                                Parent mockRoot = new VBox();  
                                AddGameAPIController mockAPIController = mock(AddGameAPIController.class);

                                when(mock.load()).thenReturn(mockRoot);
                                when(mock.getController()).thenReturn(mockAPIController);
                            } catch (IOException e) {
                                fail("IOException during mock setup: " + e.getMessage());
                            }
                        })) {

                    ByteArrayOutputStream output = new ByteArrayOutputStream();
                    PrintStream originalOut = System.out;
                    System.setOut(new PrintStream(output));
                    controller.handleAPIAddGame();
                    System.out.flush();
                    System.setOut(originalOut);

                    String consoleOutput = output.toString();
                    assertFalse(consoleOutput.contains("Error"), "Console should not contain errors");
                }

            } catch (Exception e) {
                fail("Exception during test setup or execution: " + e.getMessage());
            }
        });

        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(500);
    }
    
    /**
     * Verifies that an appropriate error message is printed when attempting to access the API screen with an invalid user ID.
     */
    @Test
    void handleAPIAddGame_shouldPrintError_whenUserIDIsInvalid() {
        Platform.runLater(() -> {
            Button apiButton = new Button();
            VBox root = new VBox(apiButton);
            Scene scene = new Scene(root);
            Stage dummyParent = new Stage();
            Stage popupStage = new Stage();
            popupStage.setScene(scene);
            popupStage.initOwner(dummyParent);
            popupStage.show();

            AddGamePopupController controller = new AddGamePopupController();
            controller.apiButton = apiButton;
            controller.loggedInUserID = 0;

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(out));

            controller.handleAPIAddGame();

            System.out.flush();
            System.setOut(originalOut);

            String output = out.toString().trim();
            assertTrue(output.contains("Error: No valid user ID set."), "Expected error message not printed for invalid user ID. Output was:\n" + output);
        });
        WaitForAsyncUtils.waitForFxEvents();
    }
    
    /**
	 * Cleans up the UI state after each test by closing dialogs, windows, and releasing input.
	 * This ensures tests do not interfere with each other due to leftover UI elements.
	 * 
	 * @param robot The TestFX robot used to interact with the UI
	 */
    @AfterEach
    void resetUiAfterEach(org.testfx.api.FxRobot robot) {
        Set<Node> btns;
        try {
            btns = robot.lookup(".dialog-pane .button").queryAll();
        } catch (Exception e) {
            btns = Collections.emptySet();
        }
        for (Node b : btns) {
            try { robot.clickOn(b); } catch (Exception ignored) {}
        }
        WaitForAsyncUtils.waitForFxEvents();
        Platform.runLater(() -> {
            var snapshot = new ArrayList<>(Window.getWindows());
            for (Window w : snapshot) {
                if (w != null && w.isShowing()) {
                    if (w instanceof Stage s) s.close();
                    else w.hide();
                }
            }
        });
        WaitForAsyncUtils.waitForFxEvents();

        robot.release(new KeyCode[] {});
        robot.release(new MouseButton[] {});
    }

}

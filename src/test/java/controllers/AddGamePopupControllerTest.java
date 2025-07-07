package controllers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.util.WaitForAsyncUtils;
 
/**
 * Unit tests for the AddGamePopupController class in the GameGrinding application.
 *
 * This test class validates the popup controller responsible for initiating game addition
 * either through manual entry or via the MobyGames API. It ensures proper stage behavior,
 * navigation logic, and error handling.
 */
@ExtendWith({ApplicationExtension.class, JavaFXThreadingExtension.class})
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
     * Verifies that onUserDataLoad executes without throwing and logs the user ID.
     */
    @Test
    void onUserDataLoad_shouldPrintUserID() {
        AddGamePopupController controller = new AddGamePopupController();
        TestUtils.setPrivateField(controller, "loggedInUserID", 42); 

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent)); 
        assertDoesNotThrow(controller::onUserDataLoad, "onUserDataLoad should not throw an exception");

        System.setOut(originalOut);

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
}

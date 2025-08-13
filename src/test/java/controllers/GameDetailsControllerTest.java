package controllers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach; 
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.util.WaitForAsyncUtils;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import models.game;
import services.GameService;
import services.userService;
import utils.AlertHelper;

/**
 * Unit tests for the GameDetailsController class.
 * This controller manages the details view for a specific game,
 * including displaying game information, updating completion status,
 * editing game details, and handling user interactions.
 * These tests cover the main functionalities of the controller.
 * 
 */
@ExtendWith({ApplicationExtension.class, JavaFXThreadingExtension.class})
class GameDetailsControllerTest {

    private GameDetailsController controller;
    private ChoiceBox<String> choiceBox;
    private Label gameTitle;
    private Label developerLabel;
    private Label publisherLabel;
    private Label releaseDateLabel;
    private Label genreLabel;
    private Label platformLabel;
    private Label completionStatusLabel;
    private TextArea notesTextArea;
    private ImageView coverImage;
    private NavigationHelper mockNavHelper;
    private Button gameCollectionButton;
    private game mockGame;
    private Button editButton;

    /**
     * Initializes the test environment before each test case.
     * Creates a spy of the GameDetailsController and injects mocked dependencies
     * including navigation helpers, UI components, and game objects using reflection.
     * This setup simulates a logged-in user and pre-loads a mock game to test controller behavior.
     */
    @BeforeEach
    void setUp() {
        controller = spy(new GameDetailsController());
        mockNavHelper = mock(NavigationHelper.class);
        mockGame = mock(game.class);
        
        when(mockGame.getTitle()).thenReturn("Mock Game");
        when(mockGame.getGameID()).thenReturn(42);

        choiceBox = new ChoiceBox<>();
        gameTitle = new Label();
        developerLabel = new Label();
        publisherLabel = new Label();
        releaseDateLabel = new Label();
        genreLabel = new Label();
        platformLabel = new Label();
        completionStatusLabel = new Label();
        notesTextArea = new TextArea();
        coverImage = new ImageView();
        gameCollectionButton = new Button("Game Collection");
        editButton = new Button("Edit");

        TestUtils.setPrivateField(controller, "navHelp", mockNavHelper);
        TestUtils.setPrivateField(controller, "completionStatusLabel", completionStatusLabel);
        TestUtils.setPrivateField(controller, "completionStatusChoiceBox", choiceBox);
        TestUtils.setPrivateField(controller, "gameTitle", gameTitle);
        TestUtils.setPrivateField(controller, "developerLabel", developerLabel);
        TestUtils.setPrivateField(controller, "publisherLabel", publisherLabel);
        TestUtils.setPrivateField(controller, "releaseDateLabel", releaseDateLabel);
        TestUtils.setPrivateField(controller, "genreLabel", genreLabel);
        TestUtils.setPrivateField(controller, "platformLabel", platformLabel);
        TestUtils.setPrivateField(controller, "notesTextArea", notesTextArea);
        TestUtils.setPrivateField(controller, "coverImage", coverImage);
        TestUtils.setPrivateField(controller, "gameCollectionLabel", gameCollectionButton);
        TestUtils.setPrivateField(controller, "editButton", editButton);
        TestUtils.setPrivateField(controller, "currentGame", mockGame);
        TestUtils.setPrivateField(controller, "loggedInUserID", 7);
    }


    /**
     * Verifies that the correct user ID is printed to the console when onUserDataLoad is called.
     * 
     */
    @Test
    void testOnUserDataLoad_shouldPrintUserIdToConsole() {
        controller.loggedInUserID = 55;
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));
        try {
            controller.onUserDataLoad();
            String output = outContent.toString();
            assertTrue(output.contains("onUserDataLoad in GameDetailsController for user 55"),
                    "Expected System.out to contain the user ID log message.");
        } finally {
            System.setOut(originalOut);
        }
    }

    /**
     * Tests that the controller logs an error and returns early if UI components like labels are not initialized.
     */
    @Test
    void testSetGame_whenCompletionStatusLabelIsNull_shouldLogErrorAndReturn() {
        GameDetailsController controllerWithNull = new GameDetailsController();
        game mockGame = mock(game.class);

        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(errContent));

        try {
            controllerWithNull.setGame(mockGame);
            String output = errContent.toString();
            assertTrue(output.contains("ERROR: UI components are not initialized yet!"), "Expected error log when label is null.");
        } finally {
            System.setErr(originalErr);
        }
    }

    /**
     * Tests that the dropdown options and selected value are set correctly from a given game, and verifies that updateGameDetails() is triggered.
     */
    @Test
    void testSetGame_shouldPopulateCompletionStatusAndCallUpdateGameDetails() {
        game mockGame = mock(game.class);
        when(mockGame.getCompletionStatus()).thenReturn("Playing");
        when(mockGame.getTitle()).thenReturn("Halo");
        Platform.runLater(() -> {
            controller.setGame(mockGame);
            assertEquals(3, choiceBox.getItems().size(), "Dropdown should contain 3 options.");
            assertEquals("Playing", choiceBox.getValue(), "Dropdown value should match game status.");
            verify(controller).updateGameDetails();
        });
    }

    /**
     * Verifies that when the status dropdown is changed, the game object is updated and updateCompletionStatus() is called.
     */
    @Test
    void testSetGame_onStatusChange_shouldUpdateGameAndCallUpdateCompletionStatus() {
        game mockGame = mock(game.class);
        when(mockGame.getCompletionStatus()).thenReturn("Not Started");
        when(mockGame.getTitle()).thenReturn("Test Game");
        Platform.runLater(() -> {
            controller.setGame(mockGame);
            choiceBox.setValue("Completed");
            verify(mockGame).setCompletionStatus("Completed");
            verify(controller).updateCompletionStatus();
        });
    }

    /**
     * Ensures that the reference to a previous controller (e.g., AddGameAPIController) is correctly stored in the GameDetailsController.
     */
    @Test
    void testSetPreviousController_shouldStoreControllerReference() {
        AddGameAPIController mockController = mock(AddGameAPIController.class);
        controller.setPreviousController(mockController);
        AddGameAPIController actual = (AddGameAPIController)TestUtils.getPrivateField(controller, "previousController");
        assertSame(mockController, actual, "The controller should be stored correctly.");
    }

    /**
     * Ensures that a list of previously searched games is properly stored in the controller.
     */
    @Test
    void testSetPreviousSearchResults_shouldStoreGameList() {
        List<game> mockResults = List.of(mock(game.class), mock(game.class));
        controller.setPreviousSearchResults(mockResults);
        @SuppressWarnings("unchecked")
		List<game> actual = (List<game>)TestUtils.getPrivateField(controller, "previousSearchResults");
        assertSame(mockResults, actual, "The previous search results should be stored correctly.");
    }

    /**
     * Verifies that calling updateGameDetails() does nothing when no current game is set.
     */
    @Test
    void testUpdateGameDetails_whenCurrentGameIsNull_shouldDoNothing() {
        controller.updateGameDetails(); 
    }

    /**
     * Verifies that all UI labels and fields are populated correctly when a valid game is provided.
     */
    @Test
    void testUpdateGameDetails_shouldUpdateUIComponents() throws Exception {
        game mockGame = mock(game.class);
        when(mockGame.getTitle()).thenReturn("Test Game");
        when(mockGame.getDeveloper()).thenReturn("Test Dev");
        when(mockGame.getPublisher()).thenReturn("Test Pub");
        when(mockGame.getReleaseDate()).thenReturn(LocalDate.of(2020, 1, 1));
        when(mockGame.getGenre()).thenReturn("RPG");
        when(mockGame.getPlatform()).thenReturn("PC");
        when(mockGame.getCompletionStatus()).thenReturn("Completed"); 
        when(mockGame.getNotes()).thenReturn("Some notes.");
        when(mockGame.getCoverImageUrl()).thenReturn(null); // triggers placeholder

        controller.setCurrentGame(mockGame);

        org.testfx.api.FxToolkit.setupFixture(controller::updateGameDetails);
        org.testfx.util.WaitForAsyncUtils.waitForFxEvents();

        assertEquals("Test Game", gameTitle.getText());
        assertEquals("Developer(s): Test Dev", developerLabel.getText());
        assertEquals("Publisher(s): Test Pub", publisherLabel.getText());
        assertEquals("Release Date: 2020-01-01", releaseDateLabel.getText()); 
        assertEquals("Genre(s): RPG", genreLabel.getText());
        assertEquals("Platform(s): PC", platformLabel.getText());
        assertEquals("Some notes.", notesTextArea.getText());

        assertEquals("Completion Status: ", completionStatusLabel.getText());

        javafx.scene.image.Image img = coverImage.getImage();
        assertNotNull(img, "Cover image should default to a placeholder when URL is null.");
        String url = img.getUrl();
        if (url != null) {
            assertTrue(url.toLowerCase().contains("placeholder"),
                    "Expected a placeholder image URL, but was: " + url);
        } else {
            assertFalse(img.isError(), "Placeholder image should load without error.");
        }
    }
    
    /**
     * Tests that a cover image is loaded from a valid URL and displayed in the image view.
     */
    @Test
    void testUpdateGameDetails_withCoverImage() throws Exception {
		game mockGame = mock(game.class);
		when(mockGame.getTitle()).thenReturn("Test Game");
		when(mockGame.getCoverImageUrl()).thenReturn("http://example.com/image.png");
		TestUtils.setPrivateField(controller, "currentGame", mockGame);
		FxToolkit.setupFixture(() -> {
			controller.updateGameDetails();
			assertNotNull(coverImage.getImage(), "Cover image should be set.");
			assertEquals("Test Game", gameTitle.getText());
		});
	}
    
    /**
     * Tests that a locally stored image file is loaded and displayed correctly if it exists.
     */
    @Test
    void testUpdateGameDetails_ImageDoesNotStartWithHttp_fileExists() throws Exception {
        File tempImageFile = File.createTempFile("test-image", ".png");
        tempImageFile.deleteOnExit();
        try (InputStream is = getClass().getResourceAsStream("/Images/test.png")) {
            Files.copy(is, tempImageFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        String imagePath = tempImageFile.getAbsolutePath();
        game mockGame = mock(game.class);
		when(mockGame.getTitle()).thenReturn("Test Game");
        when(mockGame.getCoverImageUrl()).thenReturn(imagePath);
        TestUtils.setPrivateField(controller, "currentGame", mockGame);
        FxToolkit.setupFixture(() -> {
            controller.updateGameDetails();
            assertEquals("Test Game", gameTitle.getText());
            assertNotNull(coverImage.getImage(), "Cover image should be set.");
        });
    }

    /**
     * Ensures that when the image file path is invalid or doesn't exist, no image is displayed and an error is logged.
     */
    @Test
    void testUpdateGameDetails_ImageDoesNotStartWithHttp_fileDoesNotExist() throws Exception {
        String nonExistentImagePath = "nonexistent/image/path.png";

        game mockGame = mock(game.class);
        when(mockGame.getTitle()).thenReturn("Test Game");
        when(mockGame.getDeveloper()).thenReturn("Dev");
        when(mockGame.getPublisher()).thenReturn("Pub");
        when(mockGame.getReleaseDate()).thenReturn(null);
        when(mockGame.getGenre()).thenReturn("Action");
        when(mockGame.getPlatform()).thenReturn("PC");
        when(mockGame.getCompletionStatus()).thenReturn("Not Started");
        when(mockGame.getNotes()).thenReturn(null);
        when(mockGame.getCoverImageUrl()).thenReturn(nonExistentImagePath);

        controller.setCurrentGame(mockGame);

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));

        try {
            FxToolkit.setupFixture(controller::updateGameDetails);
            WaitForAsyncUtils.waitForFxEvents();
        } finally {
            System.setOut(originalOut);
            System.setErr(originalErr);
        }

        assertEquals("Test Game", gameTitle.getText());

        Image img = coverImage.getImage();
        assertNotNull(img, "Cover image should fall back to a placeholder when file doesn't exist.");
        assertFalse(img.isError(), "Placeholder image should load without error.");

        String out = outContent.toString();
        String err = errContent.toString();
        assertTrue(
            out.contains("Primary image failed; using placeholder.")
                || err.contains("Image could not be resolved:"),
            "Expected a fallback log, but out=[" + out + "], err=[" + err + "]"
        );
    }
    /**
     * Ensures that a fallback message is printed if the image path is malformed or fails to load.
     */
    @Test
    void testUpdateGameDetails_whenImageIsInvalid_shouldLogFallbackMessage() throws Exception {
        String invalidImagePath = "::::invalid::::path::::.png"; 

        game mockGame = mock(game.class);
        when(mockGame.getTitle()).thenReturn("Broken Game");
        when(mockGame.getDeveloper()).thenReturn("Dev");
        when(mockGame.getPublisher()).thenReturn("Pub");
        when(mockGame.getReleaseDate()).thenReturn(LocalDate.of(2022, 1, 1));
        when(mockGame.getGenre()).thenReturn("Action");
        when(mockGame.getPlatform()).thenReturn("PC");
        when(mockGame.getCompletionStatus()).thenReturn("Not Started");
        when(mockGame.getNotes()).thenReturn("Some notes.");
        when(mockGame.getCoverImageUrl()).thenReturn(invalidImagePath);

        
        TestUtils.setPrivateField(controller, "currentGame", mockGame);

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
        try {
            FxToolkit.setupFixture(controller::updateGameDetails);
            WaitForAsyncUtils.waitForFxEvents();
        } finally {
            System.setOut(originalOut);
            System.setErr(originalErr);
        }

        String out = outContent.toString();
        String err = errContent.toString();

        assertTrue(
            out.contains("Primary image failed; using placeholder.")
            || err.contains("No valid image or placeholder could be loaded."),
            "Expected a fallback log when image fails to load, but out=[" + out + "], err=[" + err + "]"
        );

        Image img = coverImage.getImage();
        assertNotNull(img, "Cover image should default to a placeholder when the primary image fails.");
        assertFalse(img.isError(), "Placeholder image should load without error.");
    }

    /**
	 * Tests that the completion status is updated correctly and the service is called.
	 * Also verifies that the update is logged to the console.
	 */
    @Test
    void testUpdateCompletionStatus_successfulUpdate_shouldCallServiceAndPrintLog() {
        game mockGame = mock(game.class);
        when(mockGame.getTitle()).thenReturn("Test Game");
        when(mockGame.getCompletionStatus()).thenReturn("Completed");
        when(mockGame.getGameID()).thenReturn(123);

        TestUtils.setPrivateField(controller, "loggedInUserID", 1);
        TestUtils.setPrivateField(controller, "currentGame", mockGame);
        GameService mockGameService = mock(GameService.class);
        AlertHelper mockAlert = mock(AlertHelper.class);
        TestUtils.setPrivateField(controller, "gameService", mockGameService);
        TestUtils.setPrivateField(controller, "alert", mockAlert);

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));
        controller.updateCompletionStatus();
        System.setOut(originalOut);
        verify(mockGameService).updateCompletionStatus(1, 123, "Completed");
        assertTrue(outContent.toString().contains("Updating completion status for game: Test Game to Completed for user ID: 1"));
    }

    /**
	 * Tests that when the completion status update fails, an error alert is shown and an error is logged.
	 * This simulates a failure in the GameService.
	 */
    @Test
    void testUpdateCompletionStatus_whenServiceThrowsException_shouldShowErrorAlert() {
        game mockGame = mock(game.class);
        when(mockGame.getTitle()).thenReturn("Error Game");
        when(mockGame.getCompletionStatus()).thenReturn("Playing");
        when(mockGame.getGameID()).thenReturn(999);

        TestUtils.setPrivateField(controller, "loggedInUserID", 77);
        TestUtils.setPrivateField(controller, "currentGame", mockGame);

        GameService mockGameService = mock(GameService.class);
        AlertHelper mockAlert = mock(AlertHelper.class);
        TestUtils.setPrivateField(controller, "gameService", mockGameService);
        TestUtils.setPrivateField(controller, "alert", mockAlert);

        doThrow(new RuntimeException("DB failure")).when(mockGameService).updateCompletionStatus(anyInt(), anyInt(), anyString());
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        controller.updateCompletionStatus();

        System.setOut(originalOut);

        verify(mockAlert).showError(eq("Completion Status Update Unsucessfull."), isNull(), contains("Please try again"));
        assertTrue(outContent.toString().contains("Error occurred while updating completion status"), "Should log error to console");
    }


    /**
	 * Tests that saving notes updates the game object and calls the GameService to persist changes.
	 * Also verifies that the correct log message is printed to the console.
	 */
    @Test
    void testSaveNotes_successfulSave_shouldUpdateGameAndCallService() {
        String notes = "These are test notes.";
        game mockGame = mock(game.class);
        when(mockGame.getTitle()).thenReturn("Test Game");
        when(mockGame.getGameID()).thenReturn(123);

        TestUtils.setPrivateField(controller, "loggedInUserID", 1);
        TestUtils.setPrivateField(controller, "currentGame", mockGame);
        TextArea mockTextArea = new TextArea(notes);
        TestUtils.setPrivateField(controller, "notesTextArea", mockTextArea);

        GameService mockGameService = mock(GameService.class);
        TestUtils.setPrivateField(controller, "gameService", mockGameService);

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        controller.saveNotes();

        System.setOut(originalOut);

        verify(mockGame).setNotes(notes);
        verify(mockGameService).updateNotes(1, 123, notes);
        assertTrue(outContent.toString().contains("Saving notes for game: Test Game for user ID: 1"));
    }

    /**
	 * Tests that when saving notes fails, an error is logged and the game notes are not updated.
	 * This simulates a failure in the GameService.
	 */
    @Test
    void testSaveNotes_whenExceptionThrown_shouldLogError() {
        String notes = "Failing notes";
        game mockGame = mock(game.class);
        when(mockGame.getTitle()).thenReturn("Broken Game");
        when(mockGame.getGameID()).thenReturn(999);

        TestUtils.setPrivateField(controller, "loggedInUserID", 55);
        TestUtils.setPrivateField(controller, "currentGame", mockGame);
        TextArea mockTextArea = new TextArea(notes);
        TestUtils.setPrivateField(controller, "notesTextArea", mockTextArea);

        GameService mockGameService = mock(GameService.class);
        doThrow(new RuntimeException("Database error")).when(mockGameService).updateNotes(anyInt(), anyInt(), anyString());
        TestUtils.setPrivateField(controller, "gameService", mockGameService);
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        controller.saveNotes();
        System.setOut(originalOut);

        verify(mockGame).setNotes(notes);
        assertTrue(outContent.toString().contains("Error occurred while saving notes: Database error"));
    }
    
    /**
	 * Tests the formatPlatforms method to ensure it formats platform strings correctly.
	 * It checks that platforms are split into lines after every 5 entries.
	 */
    @Test
    void testFormatPlatforms_shouldFormatWithNewlinesEvery5Entries() throws Exception {
        String input = "PC, Xbox, PlayStation, Switch, iOS, Android, Mac, Linux";
        String expectedOutput = String.join(System.lineSeparator(),
            "PC, Xbox, PlayStation, Switch, iOS,",
            "Android, Mac, Linux"
        );
        Method method = GameDetailsController.class.getDeclaredMethod("formatPlatforms", String.class);
        method.setAccessible(true);
        String actualOutput = ((String) method.invoke(controller, input)).trim();
        assertEquals(expectedOutput, actualOutput, "Should insert newline after every 5 platforms.");
    }

    /**
     * Verifies that the formatPlatforms method returns "Unknown" for null or empty input.
     */
    @Test
    void testFormatPlatforms_nullOrEmpty() throws Exception {
        Method method = GameDetailsController.class.getDeclaredMethod("formatPlatforms", String.class);
        method.setAccessible(true);
        assertEquals("Unknown", method.invoke(controller, (Object) null));
        assertEquals("Unknown", method.invoke(controller, ""));
    }

    /**
	 * Tests that the formatPlatforms method returns the input string unchanged if it contains fewer than 5 platforms.
	 */
    @Test
    void testFormatPlatforms_lessThanFivePlatforms() throws Exception {
        Method method = GameDetailsController.class.getDeclaredMethod("formatPlatforms", String.class);
        method.setAccessible(true);
        String input = "PC, Xbox, PlayStation";
        String expected = "PC, Xbox, PlayStation";
        assertEquals(expected, method.invoke(controller, input));
    }
 
    /**
     * Tests that a warning is printed when the delete button is clicked with no game selected.
     */
    @Test
    void testClickDeleteButton_whenCurrentGameIsNull_shouldPrintWarning() throws Exception {
        TestUtils.setPrivateField(controller, "currentGame", null); 
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        FxToolkit.setupFixture(() -> {
            controller.clickDeleteButton(new ActionEvent(new Button(), null));
        });
        String output = outContent.toString();
        assertTrue(output.contains("No game selected for deletion!"), "Should warn about missing game.");
    }

    /**
	 * Tests that clicking the delete button initializes a popup with the correct game title and ID.
	 * It verifies that the popup is shown and has the expected title.
	 */
    @Test
    void testClickDeleteButton_shouldInitializePopupWithCorrectValues() throws Exception {
        game mockGame = mock(game.class);
        when(mockGame.getTitle()).thenReturn("Test Game");
        when(mockGame.getGameID()).thenReturn(42);
        controller.setCurrentGame(mockGame);
        controller.loggedInUserID = 7;

        Button mockDeleteButton = new Button("Delete");
        Scene mockScene = new Scene(new StackPane(mockDeleteButton));
        Stage mockStage = new Stage();
        mockStage.setScene(mockScene);
        mockStage.show();

        FxToolkit.setupFixture(() -> {
            controller.clickDeleteButton(new ActionEvent(mockDeleteButton, null));
        });

        Stage popup = TestUtils.getPopupStage(controller);
        assertNotNull(popup, "Popup stage should be initialized.");
        assertTrue(popup.isShowing(), "Popup stage should be visible.");
        assertEquals("Delete Game Confirmation", popup.getTitle());
    }
    
    /**
	 * Tests that clicking the game collection button navigates to the game collection page.
	 * It verifies that the navigation helper is called with the correct parameters.
	 */
    @Test
    void testHandleGameCollectionButton_shouldCallNavigationHelper() {
        controller.loggedInUserID = 101;
        controller.handleGameCollectionButton();
        verify(mockNavHelper).switchToGameCollection(eq(101), eq(gameCollectionButton));
    }
   
    /**
	 * Tests that clicking the edit button navigates to the edit game page with the correct parameters.
	 * It verifies that the navigation helper is called with the current game ID and title.
	 */
    @Test
    void testHandleEditButton_whenCurrentGameIsNull_shouldPrintWarning() throws Exception {
        TestUtils.setPrivateField(controller, "editButton", new Button("Edit")); 
        TestUtils.setPrivateField(controller, "currentGame", null);

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));
        try {
            FxToolkit.setupFixture(() -> controller.handleEditButton());
        } finally {
            System.setOut(originalOut);
        }

        String output = outContent.toString();
        assertTrue(output.contains("No game selected for editing!"), "Expected warning message for null game.");
    }

    /**
	 * Tests that clicking the edit button with a valid game navigates to the edit game page.
	 * It verifies that the navigation helper is called with the correct game ID and title.
	 */
    @Test
    void testHandleEditButton_withValidGame_shouldCallNavigationHelper() throws Exception {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));
        try {
            FxToolkit.setupFixture(() -> controller.handleEditButton());
        } finally {
            System.setOut(originalOut);
        }
        verify(mockNavHelper).switchToEditGamePage(7, mockGame, editButton);
        assertTrue(outContent.toString().contains("Opening Edit Game window for: Mock Game"), "Expected log confirming edit navigation.");
    }

    /**
	 * Tests that clicking the settings button navigates to the settings page with the correct user ID.
	 * It verifies that the navigation helper is called with the logged-in user ID and the settings button.
	 */
    @Test
    void testHandleSettingsButton_shouldCallNavigationHelperAndPrintLog() {
        Button settingsButton = new Button("Settings");
        TestUtils.setPrivateField(controller, "settingsButton", settingsButton);
        controller.loggedInUserID = 88;
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        try {
            FxToolkit.setupFixture(() -> controller.handleSettingsButton());
        } catch (Exception e) {
            fail("Exception during FxToolkit execution: " + e.getMessage());
        } finally {
            System.setOut(originalOut);
        }
        verify(mockNavHelper).switchToSettingsPage(88, settingsButton);
        assertTrue(outContent.toString().contains("Switching to Settings Page for User ID: 88"));
    }
  
    /**
	 * Tests that clicking the help button navigates to the help page with the correct user ID.
	 * It verifies that the navigation helper is called with the logged-in user ID and the help button.
	 */
    @Test
    void testHandleHelpButton_shouldCallNavigationHelperAndPrintLog() {
        Button helpButton = new Button("Help");
        TestUtils.setPrivateField(controller, "helpButton", helpButton);
        controller.loggedInUserID = 99;

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        try {
            FxToolkit.setupFixture(() -> controller.handleHelpButton());
        } catch (Exception e) {
            fail("Exception during FxToolkit execution: " + e.getMessage());
        } finally {
            System.setOut(originalOut);
        }
        verify(mockNavHelper).switchToHelpPage(99, helpButton);
        assertTrue(outContent.toString().contains("Switching to Help Page for User ID: 99"));
    }

    /**
	 * Tests that clicking the logout button calls the logout service and switches to the login page.
	 * It verifies that the logout service is called with the correct user ID and that the navigation helper switches to the login page.
	 */
    @Test
    void testHandleLogoutButton_shouldCallLogoutAndSwitchToLoginPage() {
        Button logoutButton = new Button("Logout");
        TestUtils.setPrivateField(controller, "logoutButton", logoutButton);
        TestUtils.setPrivateField(controller, "loggedInUserID", 44);

        userService mockUserService = mock(userService.class);
        TestUtils.setPrivateField(controller, "userSer", mockUserService);

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        try {
            FxToolkit.setupFixture(() -> controller.handleLogoutButton());
        } catch (Exception e) {
            fail("Exception during logout test: " + e.getMessage());
        } finally {
            System.setOut(originalOut);
        }
        verify(mockUserService).logout();
        verify(mockNavHelper).switchToLoginPage(logoutButton);
        String output = outContent.toString();
        assertTrue(output.contains("Logging out user ID: 44"));
        assertTrue(output.contains("Logging out in game details controller."));
    }
}

package controllers;

import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import models.game;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import services.GameService;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static controllers.TestUtils.*;

/**
 * Unit tests for the EditGameController class.
 *
 * This test class validates the behavior of the controller responsible for editing
 * game details in the collection. The controller interacts with game data and handles
 * user input such as text fields, choice boxes, image browsing, and navigation.
 *
 * Areas tested include:
 * - Data preloading and UI field binding
 * - Saving edited game data
 * - Navigating between screens
 * - Image loading and file selection logic
 * - Edge cases like null input, missing files, and navigation failures
 */
@ExtendWith(ApplicationExtension.class)
class EditGameControllerTest { 

    private EditGameController controller;
    private GameService mockGameService;
    private NavigationHelper mockNavHelper;
    private game mockGame;

    /**
     * Initializes the controller and injects all required dependencies and mock fields.
     * Prepares a reusable game object for multiple test scenarios.
     */
    @BeforeEach
    void setUp() {
        controller = new EditGameController() {
            {
                try {
                    setPrivateField(this, "gameService", mock(GameService.class));
                    setPrivateField(this, "navHelp", mock(NavigationHelper.class));
                } catch (Exception e) {
                    fail("Mock injection failed: " + e.getMessage());
                }
            }

            @Override
            protected void onUserDataLoad() {
                System.out.println("onUserDataLoad in EditGameController: user ID = " + loggedInUserID);
            }
        };

        mockGameService = (GameService) getPrivateField(controller, "gameService");
        mockNavHelper = (NavigationHelper) getPrivateField(controller, "navHelp");

        controller.loggedInUserID = 1;

        mockGame = new game();
        mockGame.setGameID(10);
        mockGame.setTitle("Test Title");
        mockGame.setDeveloper("Test Dev");
        mockGame.setPublisher("Test Pub");
        mockGame.setPlatform("PC");
        mockGame.setGenre("Action");
        mockGame.setNotes("Note");
        mockGame.setCompletionStatus("Playing");
        mockGame.setCoverImageUrl("http://example.com/img.png");
        mockGame.setReleaseDate(LocalDate.of(2023, 1, 1));

        injectFieldsForForm();
    }
    
    /** Injects all required form controls into the controller via reflection. */
    private void injectFieldsForForm() {
        setPrivateField(controller, "titleField", new TextField());
        setPrivateField(controller, "developerField", new TextField());
        setPrivateField(controller, "publisherField", new TextField());
        setPrivateField(controller, "releaseDateField", new TextField());
        setPrivateField(controller, "platformField", new TextField());
        setPrivateField(controller, "genreField", new TextField());
        setPrivateField(controller, "notesField", new TextArea());
        setPrivateField(controller, "completionStatusChoiceBox", new ChoiceBox<>());
        setPrivateField(controller, "coverImageView", new ImageView());
        setPrivateField(controller, "selectedImageLabel", new Label());
        setPrivateField(controller, "saveButton", new Button());
        setPrivateField(controller, "gameCollectionButton", new Button());
        setPrivateField(controller, "settingsButton", new Button());
        setPrivateField(controller, "helpButton", new Button());
    }

    /** Verifies that onUserDataLoad prints the expected user ID message to the console. */
    @Test
    void testOnUserDataLoad_printsExpectedMessage() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream original = System.out;
        System.setOut(new PrintStream(out));
        try {
            controller.loggedInUserID = 42;
            controller.onUserDataLoad();
            assertTrue(out.toString().contains("EditGameController: user ID = 42"));
        } finally {
            System.setOut(original);
        }
    }

    /** Ensures that setGame correctly preloads all form fields with game data. */
    @Test
    void testSetGame_preloadsFields() {
        controller.setGame(mockGame);
        assertEquals("Test Title", ((TextField) getPrivateField(controller, "titleField")).getText());
        assertEquals("Test Dev", ((TextField) getPrivateField(controller, "developerField")).getText());
        assertEquals("Playing", ((ChoiceBox<?>) getPrivateField(controller, "completionStatusChoiceBox")).getValue());
    }

    /** Verifies that edited fields are saved and navigation to collection view is triggered. */
    @SuppressWarnings("unchecked")
	@Test
    void testHandleSaveButtonClick_updatesGameFieldsAndNavigates() {
        controller.setGame(mockGame);

        ((TextField) getPrivateField(controller, "titleField")).setText("New Title");
        ((TextField) getPrivateField(controller, "developerField")).setText("New Dev");
        ((ChoiceBox<String>) getPrivateField(controller, "completionStatusChoiceBox")).getItems().add("Completed");
        ((ChoiceBox<String>) getPrivateField(controller, "completionStatusChoiceBox")).setValue("Completed");

        invokePrivateMethod(controller, "handleSaveButtonClick");

        verify(mockGameService).updateTitle(1, 10, "New Title");
        verify(mockGameService).updateDeveloper(1, 10, "New Dev");
        verify(mockGameService).updateCompletionStatus(1, 10, "Completed");
        verify(mockNavHelper).switchToGameCollection(1, (Button) getPrivateField(controller, "saveButton"));
    }
    
    /** Tests that initialize correctly populates the completion status options. */
    @Test
    void testInitialize_populatesCompletionStatusChoices() {
        ChoiceBox<String> choiceBox = new ChoiceBox<>();
        setPrivateField(controller, "completionStatusChoiceBox", choiceBox);

        controller.initialize();

        assertEquals(3, choiceBox.getItems().size());
        assertTrue(choiceBox.getItems().contains("Not Started"));
        assertTrue(choiceBox.getItems().contains("Playing"));
        assertTrue(choiceBox.getItems().contains("Completed"));
    }

    /** Ensures preloadGameData properly loads a local image file as the game cover. */
    @Test
    void testPreloadGameData_usesFilePathForImageUrl() {
        game localImageGame = new game();
        localImageGame.setGameID(20);
        localImageGame.setTitle("Local Image Game");
        localImageGame.setDeveloper("Local Dev");
        localImageGame.setPublisher("Local Pub");
        localImageGame.setPlatform("Switch");
        localImageGame.setGenre("Adventure");
        localImageGame.setNotes("Some notes");
        localImageGame.setCompletionStatus("Completed");
        localImageGame.setReleaseDate(LocalDate.of(2021, 5, 5));

        localImageGame.setCoverImageUrl("src/test/resources/sample-image.png");

        setPrivateField(controller, "coverImageView", new ImageView());
        setPrivateField(controller, "completionStatusChoiceBox", new ChoiceBox<>());

        controller.setGame(localImageGame);

        ImageView imageView = (ImageView) getPrivateField(controller, "coverImageView");
        assertNotNull(imageView.getImage());

        String loadedImageUrl = imageView.getImage().getUrl();
        assertNotNull(loadedImageUrl);
        assertTrue(loadedImageUrl.startsWith("file:"), "Expected image to be loaded from file path, got: " + loadedImageUrl);
    }

    /** Verifies that when a file is selected via file chooser, the image is updated and stored. */
    @Test
    void testHandleBrowseImage_fileSelected_setsImageAndUrl() {
        File mockFile = new File("src/test/resources/sample-cover.jpg");

        game testGame = new game();
        setPrivateField(controller, "currentGame", testGame);

        Button browseButton = mock(Button.class);
        Scene scene = mock(Scene.class);
        Window window = mock(Window.class);
        when(browseButton.getScene()).thenReturn(scene);
        when(scene.getWindow()).thenReturn(window);

        FileChooser mockChooser = mock(FileChooser.class);
        when(mockChooser.getExtensionFilters()).thenReturn(FXCollections.observableArrayList()); 
        when(mockChooser.showOpenDialog(window)).thenReturn(mockFile);

        setPrivateField(controller, "browseImageButton", browseButton);
        setPrivateField(controller, "selectedImageLabel", new Label());
        setPrivateField(controller, "coverImageView", new ImageView());

        EditGameController spyController = spy(controller);
        doReturn(mockChooser).when(spyController).createFileChooser();

        invokePrivateMethod(spyController, "handleBrowseImage");

        ImageView imageView = (ImageView) getPrivateField(spyController, "coverImageView");
        Label label = (Label) getPrivateField(spyController, "selectedImageLabel");

        assertNotNull(imageView.getImage());
        assertEquals("sample-cover.jpg", label.getText());
        assertEquals(mockFile.toURI().toString(), testGame.getCoverImageUrl());
    }

    /** Ensures that if no file is selected, the image and label fields remain unchanged. */
    @Test
    void testHandleBrowseImage_fileNotSelected_doesNothing() {
        Button browseButton = mock(Button.class);
        Scene scene = mock(Scene.class);
        Window window = mock(Window.class);
        when(browseButton.getScene()).thenReturn(scene);
        when(scene.getWindow()).thenReturn(window);

        FileChooser mockChooser = mock(FileChooser.class);
        when(mockChooser.getExtensionFilters()).thenReturn(FXCollections.observableArrayList()); 
        when(mockChooser.showOpenDialog(window)).thenReturn(null);

        setPrivateField(controller, "browseImageButton", browseButton);
        setPrivateField(controller, "selectedImageLabel", new Label());
        setPrivateField(controller, "coverImageView", new ImageView());
        setPrivateField(controller, "currentGame", new game());

        EditGameController spyController = spy(controller);
        doReturn(mockChooser).when(spyController).createFileChooser();

        invokePrivateMethod(spyController, "handleBrowseImage");

        Label label = (Label) getPrivateField(spyController, "selectedImageLabel");
        ImageView imageView = (ImageView) getPrivateField(spyController, "coverImageView");
        game g = (game) getPrivateField(spyController, "currentGame");

        assertTrue(label.getText() == null || label.getText().isEmpty());
        assertNull(imageView.getImage());
        assertNull(g.getCoverImageUrl());
    }

    /** Validates that createFileChooser returns a new FileChooser instance. */
    @Test
    void testCreateFileChooser_returnsNewInstance() {
        FileChooser chooser = controller.createFileChooser();
        assertNotNull(chooser, "FileChooser should not be null");
        assertEquals(FileChooser.class, chooser.getClass(), "Expected a real FileChooser instance");
    }

    /** Ensures that clicking the Game Collection button navigates to the correct view. */
    @Test
    void testHandleGameCollectionButton_switchesView() {
        invokePrivateMethod(controller, "handleGameCollectionButton");
        verify(mockNavHelper).switchToGameCollection(1, (Button) getPrivateField(controller, "gameCollectionButton"));
    }
    
    /** Ensures that a thrown exception during Game Collection navigation is handled gracefully. */
    @Test
    void testHandleGameCollectionButton_whenExceptionThrown() {
        doThrow(new RuntimeException("Test Exception")).when(mockNavHelper).switchToGameCollection(anyInt(), any());
        assertDoesNotThrow(() -> invokePrivateMethod(controller, "handleGameCollectionButton"));
    }

    /** Ensures that clicking the Settings button switches to the Settings page. */
    @Test
    void testHandleSettingsButton_switchesView() {
        invokePrivateMethod(controller, "handleSettingsButton");
        verify(mockNavHelper).switchToSettingsPage(1, (Button) getPrivateField(controller, "settingsButton"));
    }

    /** Confirms that exceptions thrown during Settings navigation are caught and suppressed. */
    @Test
    void testHandleSettingsButton_whenExceptionThrown() {
        doThrow(new RuntimeException("Settings Navigation Failed")).when(mockNavHelper).switchToSettingsPage(anyInt(), any());
        assertDoesNotThrow(() -> invokePrivateMethod(controller, "handleSettingsButton"));
    }

    /** Ensures that the Help button transitions the user to the Help page. */
    @Test
    void testHandleHelpButton_switchesView() {
        invokePrivateMethod(controller, "handleHelpButton");
        verify(mockNavHelper).switchToHelpPage(1, (Button) getPrivateField(controller, "helpButton"));
    }
    
    /** Validates graceful handling of exceptions during Help navigation. */
    @Test
    void testHandleHelpButton_whenExceptionThrown() {
        doThrow(new RuntimeException("Help Navigation Failed")).when(mockNavHelper).switchToHelpPage(anyInt(), any());
        assertDoesNotThrow(() -> invokePrivateMethod(controller, "handleHelpButton"));
    }

}

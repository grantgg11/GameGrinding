package controllers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import javafx.scene.control.*; 
import javafx.scene.image.ImageView;
import services.userService;
import utils.AlertHelper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for the ManualAddGameConttoller class.
 *
 * This test class verifies the functionality of the ManualAddGameConttoller,
 * including input validation, navigation, and error handling.
 * It uses mock dependencies to isolate the controller's behavior.
 */
@ExtendWith(JavaFXThreadingExtension.class)
class ManualAddGameConttollerTest {

    private ManualAddGameConttoller controller;

    /**
     * Sets up a fresh instance of the controller and injects default mock UI components
     * before each test to ensure consistent behavior and isolated test cases.
     */
    @BeforeEach
    void setUp() throws Exception {
        controller = new ManualAddGameConttoller();

        TextField titleField = new TextField("Test Game");
        TextField developerField = new TextField("Test Dev");
        TextField publisherField = new TextField("Test Pub");
        TextField releaseDateField = new TextField("2023-01-01");
        TextField genreField = new TextField("Action");
        TextField platformField = new TextField("PC");
        TextArea notesField = new TextArea("Fun game");
        ChoiceBox<String> completionStatusField = new ChoiceBox<>();
        completionStatusField.getItems().addAll("Not Started", "Playing", "Completed");
        completionStatusField.setValue("Completed");

        Label selectedImageLabel = new Label();
        ImageView coverImagePreview = new ImageView();

        TestUtils.setPrivateField(controller, "titleField", titleField);
        TestUtils.setPrivateField(controller, "developerField", developerField);
        TestUtils.setPrivateField(controller, "publisherField", publisherField);
        TestUtils.setPrivateField(controller, "releaseDateField", releaseDateField);
        TestUtils.setPrivateField(controller, "genreField", genreField);
        TestUtils.setPrivateField(controller, "platformField", platformField);
        TestUtils.setPrivateField(controller, "notesField", notesField);
        TestUtils.setPrivateField(controller, "completionStatusField", completionStatusField);
        TestUtils.setPrivateField(controller, "selectedImageLabel", selectedImageLabel);
        TestUtils.setPrivateField(controller, "coverImagePreview", coverImagePreview);
    }
    
    ////////////////////////////// testing getTitleField //////////////////////////////
    
    /**
	 * Tests the getTitleField method with valid input.
	 * It should return the trimmed title without leading or trailing spaces.
	 * 
	 * @throws Exception if an error occurs during the test setup or execution
	 */
    @Test
    void testGetTitleField_withValidInput_returnsTrimmedTitle() throws Exception {
        TextField titleField = new TextField("  Test Game  ");
        TestUtils.setPrivateField(controller, "titleField", titleField);
        TestUtils.setPrivateField(controller, "errors", new ArrayList<>());

        String result = controller.getTitleField();

        assertEquals("Test Game", result);
    }

    /**
     * Verifies that getTitleField returns null and logs an error when input is empty.
     * 
     * @throws Exception if an error occurs during the test setup or execution
     */
    @Test
    void testGetTitleField_withEmptyInput_returnsNullAndAddsError() throws Exception {
        TextField titleField = new TextField("   ");
        List<String> errors = new ArrayList<>();

        TestUtils.setPrivateField(controller, "titleField", titleField);
        TestUtils.setPrivateField(controller, "errors", errors);

        String result = controller.getTitleField();

        assertNull(result);
        assertTrue(errors.contains("Please enter the title of the game your would like to add"));
    }

    /**
     * Verifies that getTitleField returns null and logs an error when input is null.
     * 
     * @throws Exception if an error occurs during the test setup or execution
     */
    @Test
    void testGetTitleField_withNullInput_returnsNullAndAddsError() throws Exception {
        TextField titleField = mock(TextField.class);
        when(titleField.getText()).thenReturn(null);
        List<String> errors = new ArrayList<>();

        TestUtils.setPrivateField(controller, "titleField", titleField);
        TestUtils.setPrivateField(controller, "errors", errors);

        String result = controller.getTitleField();

        assertNull(result);
        assertTrue(errors.contains("Please enter the title of the game your would like to add"));
    }
    
    ////////////////////////////// testing getDeveloperField //////////////////////////////

    /**
     * Verifies that getDeveloperField returns a trimmed developer name with valid input.
     * 
     * @throws Exception if an error occurs during the test setup or execution
     */
    @Test
    void testGetDeveloperField_withValidInput_returnsTrimmedDeveloper() throws Exception {
        TextField developerField = new TextField("  Dev Studios  ");
        TestUtils.setPrivateField(controller, "developerField", developerField);
        TestUtils.setPrivateField(controller, "errors", new ArrayList<>());

        String result = controller.getDeveloperField();

        assertEquals("Dev Studios", result);
    }

    /**
	 * Verifies that getDeveloperField returns null and logs an error when input is empty.
	 * 
	 * @throws Exception if an error occurs during the test setup or execution
	 */
    @Test
    void testGetDeveloperField_withEmptyInput_returnsNullAndAddsError() throws Exception {
        TextField developerField = new TextField("   ");
        List<String> errors = new ArrayList<>();

        TestUtils.setPrivateField(controller, "developerField", developerField);
        TestUtils.setPrivateField(controller, "errors", errors);

        String result = controller.getDeveloperField();

        assertNull(result);
        assertTrue(errors.contains("Please enter the developer of the game your would like to add"));
    }

    /**
	 * Verifies that getDeveloperField returns null and logs an error when input is null.
	 * 
	 * @throws Exception if an error occurs during the test setup or execution
	 */
    @Test
    void testGetDeveloperField_withNullInput_returnsNullAndAddsError() throws Exception {
        TextField developerField = mock(TextField.class);
        when(developerField.getText()).thenReturn(null);
        List<String> errors = new ArrayList<>();

        TestUtils.setPrivateField(controller, "developerField", developerField);
        TestUtils.setPrivateField(controller, "errors", errors);

        String result = controller.getDeveloperField();

        assertNull(result);
        assertTrue(errors.contains("Please enter the developer of the game your would like to add"));
    }
    
    ////////////////////////////// testing getPublisherField //////////////////////////////
    
    /**
	 * Verifies that getPublisherField returns a trimmed publisher name with valid input.
	 * 
	 * @throws Exception if an error occurs during the test setup or execution
	 */
    @Test
    void testGetPublisherField_withValidInput_returnsTrimmedPublisher() throws Exception {
        TextField publisherField = new TextField("  Epic Games  ");
        TestUtils.setPrivateField(controller, "publisherField", publisherField);
        TestUtils.setPrivateField(controller, "errors", new ArrayList<>());

        String result = controller.getPublisherField();

        assertEquals("Epic Games", result);
    }

    /**
     * Verifies that getPublisherField returns null and logs an error when input is empty.
     * 
     * @throws Exception if an error occurs during the test setup or execution
     */
    @Test
    void testGetPublisherField_withEmptyInput_returnsNullAndAddsError() throws Exception {
        TextField publisherField = new TextField("   ");
        List<String> errors = new ArrayList<>();

        TestUtils.setPrivateField(controller, "publisherField", publisherField);
        TestUtils.setPrivateField(controller, "errors", errors);

        String result = controller.getPublisherField();

        assertNull(result);
        assertTrue(errors.contains("Please enter the publisher of the game your would like to add"));
    }

    /**
	 * Verifies that getPublisherField returns null and logs an error when input is null.
	 * 
	 * @throws Exception if an error occurs during the test setup or execution
	 */
    @Test
    void testGetPublisherField_withNullInput_returnsNullAndAddsError() throws Exception {
        TextField publisherField = mock(TextField.class);
        when(publisherField.getText()).thenReturn(null);
        List<String> errors = new ArrayList<>();

        TestUtils.setPrivateField(controller, "publisherField", publisherField);
        TestUtils.setPrivateField(controller, "errors", errors);

        String result = controller.getPublisherField();

        assertNull(result);
        assertTrue(errors.contains("Please enter the publisher of the game your would like to add"));
    }

    ////////////////////////////// testing getCompletionStatusField //////////////////////////////
    
    /**
     * Verifies that getCompletionStatusField returns the selected choice when a valid choice is provided.
     * 
     * @throws Exception if an error occurs during the test setup or execution
     */
    @Test
    void testGetCompletionStatusField_withValidChoice_returnsChoice() throws Exception {
        List<String> errors = new ArrayList<>();
        TestUtils.setPrivateField(controller, "errors", errors);

        String result = controller.getCompletionStatusField("Completed");

        assertEquals("Completed", result);
        assertTrue(errors.isEmpty());
    }

    /**
	 * Verifies that getCompletionStatusField returns null and logs an error when no choice is selected.
	 * 
	 * @throws Exception if an error occurs during the test setup or execution
	 */
    @Test
    void testGetCompletionStatusField_withNullChoice_returnsNullAndAddsError() throws Exception {
        List<String> errors = new ArrayList<>();
        TestUtils.setPrivateField(controller, "errors", errors);

        String result = controller.getCompletionStatusField(null);

        assertNull(result);
        assertTrue(errors.contains("Please enter the completion status of the game your would like to add"));
    }

    ////////////////////////////////// testing getReleaseDateField //////////////////////////////
    
    /**
	 * Verifies that getReleaseDateField returns a trimmed date string when valid input is provided.
	 * 
	 * @throws Exception if an error occurs during the test setup or execution
	 */
    @Test
    void testGetReleaseDateField_withValidInput_returnsTrimmedDate() throws Exception {
        TextField releaseDateField = new TextField(" 2024-05-30 ");
        TestUtils.setPrivateField(controller, "releaseDateField", releaseDateField);
        TestUtils.setPrivateField(controller, "errors", new ArrayList<>());

        String result = controller.getReleaseDateField();

        assertEquals("2024-05-30", result);
    }

    /**
     * Verifies that getReleaseDateField returns null and logs an error when input is empty.
     * 
     * @throws Exception if an error occurs during the test setup or execution
     */
    @Test
    void testGetReleaseDateField_withEmptyInput_returnsNullAndAddsError() throws Exception {
        TextField releaseDateField = new TextField("   ");
        List<String> errors = new ArrayList<>();

        TestUtils.setPrivateField(controller, "releaseDateField", releaseDateField);
        TestUtils.setPrivateField(controller, "errors", errors);

        String result = controller.getReleaseDateField();

        assertNull(result);
        assertTrue(errors.contains("Please enter the Release Date of the game your would like to add"));
    }

    /**
     * Verifies that getReleaseDateField returns null and logs an error when input is null.
     * 
     * @throws Exception if an error occurs during the test setup or execution
     */
    @Test
    void testGetReleaseDateField_withNullInput_returnsNullAndAddsError() throws Exception {
        TextField releaseDateField = mock(TextField.class);
        when(releaseDateField.getText()).thenReturn(null);
        List<String> errors = new ArrayList<>();

        TestUtils.setPrivateField(controller, "releaseDateField", releaseDateField);
        TestUtils.setPrivateField(controller, "errors", errors);

        String result = controller.getReleaseDateField();

        assertNull(result);
        assertTrue(errors.contains("Please enter the Release Date of the game your would like to add"));
    }
	////////////////////////////////// testing getGenreField //////////////////////////////
	
    /**
	 * Verifies that getGenreField returns a trimmed genre string when valid input is provided.
	 * 
	 * @throws Exception if an error occurs during the test setup or execution
	 */
    @Test
    void testGetGenreField_withValidInput_returnsTrimmedGenre() throws Exception {
        TextField genreField = new TextField("  Action RPG  ");
        TestUtils.setPrivateField(controller, "genreField", genreField);
        TestUtils.setPrivateField(controller, "errors", new ArrayList<>());

        String result = controller.getGenreField();

        assertEquals("Action RPG", result);
    }

    /**
	 * Verifies that getGenreField returns null and logs an error when input is empty.
	 * 
	 * @throws Exception if an error occurs during the test setup or execution
	 */
    @Test
    void testGetGenreField_withEmptyInput_returnsNullAndAddsError() throws Exception {
        TextField genreField = new TextField("   ");
        List<String> errors = new ArrayList<>();

        TestUtils.setPrivateField(controller, "genreField", genreField);
        TestUtils.setPrivateField(controller, "errors", errors);

        String result = controller.getGenreField();

        assertNull(result);
        assertTrue(errors.contains("Please enter the genre of the game your would like to add"));
    }

    /**
     * Verifies that getGenreField returns null and logs an error when input is null.
     * 
     * @throws Exception if an error occurs during the test setup or execution
     */
    @Test
    void testGetGenreField_withNullInput_returnsNullAndAddsError() throws Exception {
        TextField genreField = mock(TextField.class);
        when(genreField.getText()).thenReturn(null);
        List<String> errors = new ArrayList<>();

        TestUtils.setPrivateField(controller, "genreField", genreField);
        TestUtils.setPrivateField(controller, "errors", errors);

        String result = controller.getGenreField();

        assertNull(result);
        assertTrue(errors.contains("Please enter the genre of the game your would like to add"));
    }
	////////////////////////////////// testing getPlatformField //////////////////////////////

    /**
	 * Verifies that getPlatformField returns a trimmed platform string when valid input is provided.
	 * 
	 * @throws Exception if an error occurs during the test setup or execution
	 */
    @Test
    void testGetPlatformField_withValidInput_returnsTrimmedPlatform() throws Exception {
        TextField platformField = new TextField("  PC, Switch  ");
        TestUtils.setPrivateField(controller, "platformField", platformField);
        TestUtils.setPrivateField(controller, "errors", new ArrayList<>());

        String result = controller.getPlatformField();

        assertEquals("PC, Switch", result);
    }

    /**
     * Verifies that getPlatformField returns null and logs an error when input is empty.
     * 
     * @throws Exception if an error occurs during the test setup or execution
     */
    @Test
    void testGetPlatformField_withEmptyInput_returnsNullAndAddsError() throws Exception {
        TextField platformField = new TextField("   ");
        List<String> errors = new ArrayList<>();

        TestUtils.setPrivateField(controller, "platformField", platformField);
        TestUtils.setPrivateField(controller, "errors", errors);

        String result = controller.getPlatformField();

        assertNull(result);
        assertTrue(errors.contains("Please enter the platform of the game your would like to add"));
    }

    /**
	 * Verifies that getPlatformField returns null and logs an error when input is null.
	 * 
	 * @throws Exception if an error occurs during the test setup or execution
	 */
    @Test
    void testGetPlatformField_withNullInput_returnsNullAndAddsError() throws Exception {
        TextField platformField = mock(TextField.class);
        when(platformField.getText()).thenReturn(null);
        List<String> errors = new ArrayList<>();

        TestUtils.setPrivateField(controller, "platformField", platformField);
        TestUtils.setPrivateField(controller, "errors", errors);

        String result = controller.getPlatformField();

        assertNull(result);
        assertTrue(errors.contains("Please enter the platform of the game your would like to add"));
    }
	////////////////////////////////// testing getNotesField //////////////////////////////
	
    /**
	 * Verifies that getNotesField returns the text from the notes field, handling null and empty cases.
	 * 
	 * @throws Exception if an error occurs during the test setup or execution
	 */
    @Test
    void testGetNotesField_withValidInput_returnsNotes() throws Exception {
        TextArea notesField = new TextArea("This game is awesome!");
        TestUtils.setPrivateField(controller, "notesField", notesField);

        String result = controller.getNotesField();

        assertEquals("This game is awesome!", result);
    }

    /**
     * Verifies that getNotesField returns an empty string when the notes field is empty.
     * 
     * @throws Exception if an error occurs during the test setup or execution
     */
    @Test
    void testGetNotesField_withEmptyInput_returnsEmptyString() throws Exception {
        TextArea notesField = new TextArea("");
        TestUtils.setPrivateField(controller, "notesField", notesField);

        String result = controller.getNotesField();

        assertEquals("", result);
    }

    /**
	 * Verifies that getNotesField returns an empty string when the notes field is null.
	 * 
	 * @throws Exception if an error occurs during the test setup or execution
	 */
    @Test
    void testGetNotesField_withNullInput_returnsEmptyString() throws Exception {
        TextArea notesField = mock(TextArea.class);
        when(notesField.getText()).thenReturn(null);
        TestUtils.setPrivateField(controller, "notesField", notesField);

        String result = controller.getNotesField();

        assertEquals("", result);
    }

    //////////////////////////// testing getCoverArtField //////////////////////////////////
    
    /**
	 * Tests the getCoverArtField method when a valid image file is selected.
	 * It should copy the image to the Images directory and return the relative path.
	 * 
	 * @throws Exception if an error occurs during the test setup or execution
	 */
    @Test
    void testGetCoverArtField_whenSelectedImageFileExistsAndCopySucceeds_returnsPath() throws Exception {
        File tempImage = File.createTempFile("game", ".jpg");
        tempImage.deleteOnExit();

        File dest = new File("Images/game.jpg");
        dest.getParentFile().mkdirs();
        Files.deleteIfExists(dest.toPath());

        TestUtils.setPrivateField(controller, "selectedImageFile", tempImage);
        TestUtils.setPrivateField(controller, "errors", new ArrayList<>());

        String result = controller.getCoverArtField();

        assertNotNull(result);
        assertTrue(result.startsWith("Images/"));
        assertFalse(result.contains("null"));
    }

    /**
     * Tests the getCoverArtField method when the selected image file copy fails.
     * It should return null and log an error message.
     * 
     * @throws Exception if an error occurs during the test setup or execution
     */
    @Test
    void testGetCoverArtField_whenSelectedImageFileCopyFails_returnsNullAndAddsError() throws Exception {
        File mockImageFile = mock(File.class);
        when(mockImageFile.getName()).thenReturn("game.jpg");
        when(mockImageFile.toPath()).thenReturn(new File("temp/game.jpg").toPath());

        TestUtils.setPrivateField(controller, "selectedImageFile", mockImageFile);
        List<String> errors = new ArrayList<>();
        TestUtils.setPrivateField(controller, "errors", errors);

        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.copy(any(Path.class), any(Path.class))).thenThrow(new IOException("Simulated copy failure"));

            String result = controller.getCoverArtField();

            assertNull(result);
            assertTrue(errors.contains("Failed to copy cover image."));
        }
    }


    /**
	 * Tests the getCoverArtField method when no image is selected.
	 * It should return null and log an error message.
	 * 
	 * @throws Exception if an error occurs during the test setup or execution
	 */
    @Test
    void testGetCoverArtField_whenNoSelectedImageAndPlaceholderExists_returnsPlaceholderPath() throws Exception {
        TestUtils.setPrivateField(controller, "selectedImageFile", null);
        TestUtils.setPrivateField(controller, "errors", new ArrayList<>());

        File imagesDir = new File("Images");
        if (!imagesDir.exists()) imagesDir.mkdirs();

        File placeholder = new File(imagesDir, "placeholder1gameGrinding.png");
        if (!placeholder.exists()) {
            boolean created = placeholder.createNewFile();
            assertTrue(created || placeholder.exists(), "Failed to create placeholder image");
        }
        String result = controller.getCoverArtField();
        assertNotNull(result);
        assertTrue(result.startsWith("Images/placeholder"));
    }

    /**
	 * Tests the getCoverArtField method when no image is selected and no placeholder exists.
	 * It should return null and log an error message.
	 * 
	 * @throws Exception if an error occurs during the test setup or execution
	 */
//    @Test
//    void testGetCoverArtField_whenNoSelectedImageAndPlaceholderMissing_returnsNullAndAddsError() throws Exception {
//        TestUtils.setPrivateField(controller, "selectedImageFile", null);
//        List<String> errors = new ArrayList<>();
//        TestUtils.setPrivateField(controller, "errors", errors);
//
//        new File("Images/placeholder1gameGrinding.png").delete();
//        new File("Images/placeholder2gameGrinding.png").delete();
//
//        String result = controller.getCoverArtField();
//
//        assertNull(result);
//        assertTrue(errors.contains("Placeholder image missing."));
//    }

    
    ///////////////////////////////testing buttons ///////////////////////////////////
    
    /**
	 * Tests the handleManualAddButton method to ensure it navigates to the ManualAddGame.fxml page.
	 * 
	 * @throws Exception if an error occurs during the test setup or execution
	 */
    @Test
    void testHandleGameCollectionButton_callsSwitchToGameCollection() throws Exception {
        NavigationHelper navMock = mock(NavigationHelper.class);
        Button gameCollectionButton = new Button();
        int userId = 42;

        TestUtils.setPrivateField(controller, "navHelp", navMock);
        TestUtils.setPrivateField(controller, "gameCollectionButton", gameCollectionButton);
        TestUtils.setPrivateField(controller, "loggedInUserID", userId);

        controller.handleGameCollectionButton();

        verify(navMock).switchToGameCollection(userId, gameCollectionButton);
    }

    /**
	 * Tests the handleGameCollectionButton method when an exception is thrown.
	 * It should not crash and should handle the exception gracefully.
	 * 
	 * @throws Exception if an error occurs during the test setup or execution
	 */
    @Test
    void testHandleGameCollectionButton_whenExceptionThrown_shouldNotCrash() throws Exception {
        NavigationHelper navMock = mock(NavigationHelper.class);
        Button gameCollectionButton = new Button();
        int userId = 42;

        doThrow(new RuntimeException("Simulated Error")).when(navMock).switchToGameCollection(userId, gameCollectionButton);

        TestUtils.setPrivateField(controller, "navHelp", navMock);
        TestUtils.setPrivateField(controller, "gameCollectionButton", gameCollectionButton);
        TestUtils.setPrivateField(controller, "loggedInUserID", userId);

        assertDoesNotThrow(() -> controller.handleGameCollectionButton());
    }

    /**
	 * Tests the handleSettingsButton method to ensure it navigates to the Settings page.
	 * 
	 * @throws Exception if an error occurs during the test setup or execution
	 */
    @Test
    void testHandleSettingsButton_callsSwitchToSettingsPage() throws Exception {
        NavigationHelper navMock = mock(NavigationHelper.class);
        Button settingsButton = new Button();
        int userId = 42;

        TestUtils.setPrivateField(controller, "navHelp", navMock);
        TestUtils.setPrivateField(controller, "settingsButton", settingsButton);
        TestUtils.setPrivateField(controller, "loggedInUserID", userId);

        controller.handleSettingsButton();

        verify(navMock).switchToSettingsPage(userId, settingsButton);
    }
	
    /**
     * Tests the handleSettingsButton method when an exception is thrown.
     * It should not crash and should handle the exception gracefully.
     * 
     * @throws Exception if an error occurs during the test setup or execution
     */
    @Test
    void testHandleSettingsButton_whenExceptionThrown_shouldNotCrash() throws Exception {
        NavigationHelper navMock = mock(NavigationHelper.class);
        Button settingsButton = new Button();
        int userId = 42;

        doThrow(new RuntimeException("Simulated Error")).when(navMock).switchToSettingsPage(userId, settingsButton);

        TestUtils.setPrivateField(controller, "navHelp", navMock);
        TestUtils.setPrivateField(controller, "settingsButton", settingsButton);
        TestUtils.setPrivateField(controller, "loggedInUserID", userId);

        assertDoesNotThrow(() -> controller.handleSettingsButton());
    }

    /**
	 * Tests the handleHelpButton method to ensure it navigates to the Help page.
	 * 
	 * @throws Exception if an error occurs during the test setup or execution
	 */
    @Test
    void testHandleHelpButton_callsSwitchToHelpPage() throws Exception {
        NavigationHelper navMock = mock(NavigationHelper.class);
        Button helpButton = new Button();
        int userId = 42;

        TestUtils.setPrivateField(controller, "navHelp", navMock);
        TestUtils.setPrivateField(controller, "helpButton", helpButton);
        TestUtils.setPrivateField(controller, "loggedInUserID", userId);

        controller.handleHelpButton();

        verify(navMock).switchToHelpPage(userId, helpButton);
    }

    /**
	 * Tests the handleHelpButton method when an exception is thrown.
	 * It should not crash and should handle the exception gracefully.
	 * 
	 * @throws Exception if an error occurs during the test setup or execution
	 */
    @Test
    void testHandleHelpButton_whenExceptionThrown_shouldNotCrash() throws Exception {
        NavigationHelper navMock = mock(NavigationHelper.class);
        Button helpButton = new Button();
        int userId = 42;

        doThrow(new RuntimeException("Simulated Error")).when(navMock).switchToHelpPage(userId, helpButton);

        TestUtils.setPrivateField(controller, "navHelp", navMock);
        TestUtils.setPrivateField(controller, "helpButton", helpButton);
        TestUtils.setPrivateField(controller, "loggedInUserID", userId);

        assertDoesNotThrow(() -> controller.handleHelpButton());
    }

    /**
	 * Tests the handleLogoutButton method to ensure it logs out the user and navigates to the login page.
	 * 
	 * @throws Exception if an error occurs during the test setup or execution
	 */
    @Test
    void testHandleLogoutButton_callsLogoutAndSwitchToLogin() throws Exception {
        NavigationHelper navMock = mock(NavigationHelper.class);
        userService userServiceMock = mock(userService.class);
        Button logoutButton = new Button();

        TestUtils.setPrivateField(controller, "navHelp", navMock);
        TestUtils.setPrivateField(controller, "userService", userServiceMock);
        TestUtils.setPrivateField(controller, "logoutButton", logoutButton);

        controller.handleLogoutButton();

        verify(userServiceMock).logout();
        verify(navMock).switchToLoginPage(logoutButton);
    }
    
    /**
     * Tests the handleLogoutButton method when an exception is thrown during logout.
     * It should not crash and should handle the exception gracefully.
     * 
     * @throws Exception if an error occurs during the test setup or execution
     */
    @Test
    void testHandleLogoutButton_whenExceptionThrown_shouldNotCrash() throws Exception {
        NavigationHelper navMock = mock(NavigationHelper.class);
        userService userServiceMock = mock(userService.class);
        Button logoutButton = new Button();

        doThrow(new RuntimeException("Simulated Error")).when(userServiceMock).logout();

        TestUtils.setPrivateField(controller, "navHelp", navMock);
        TestUtils.setPrivateField(controller, "userService", userServiceMock);
        TestUtils.setPrivateField(controller, "logoutButton", logoutButton);

        assertDoesNotThrow(() -> controller.handleLogoutButton());
    }

    /**
	 * Tests the handleRefreshButton method to ensure it navigates to the ManualAddGame page.
	 * 
	 * @throws Exception if an error occurs during the test setup or execution
	 */
    @Test
    void testHandleRefreshButton_callsSwitchToManualAddGamePage() throws Exception {
        NavigationHelper navMock = mock(NavigationHelper.class);
        AlertHelper alertMock = mock(AlertHelper.class);
        Button refreshButton = new Button();
        int userId = 42;

        TestUtils.setPrivateField(controller, "navHelp", navMock);
        TestUtils.setPrivateField(controller, "alert", alertMock);
        TestUtils.setPrivateField(controller, "refreashButton", refreshButton);
        TestUtils.setPrivateField(controller, "loggedInUserID", userId);

        controller.handleRefreshButton();

        verify(navMock).switchToManualAddGamePage(userId, refreshButton);
    }
    
    /**
	 * Tests the handleRefreshButton method when an exception is thrown.
	 * It should not crash and should handle the exception gracefully.
	 * 
	 * @throws Exception if an error occurs during the test setup or execution
	 */
    @Test
    void testHandleRefreshButton_whenExceptionThrown_shouldShowAlert() throws Exception {
        NavigationHelper navMock = mock(NavigationHelper.class);
        AlertHelper alertMock = mock(AlertHelper.class);
        Button refreshButton = new Button();
        int userId = 42;

        doThrow(new RuntimeException("Simulated Error")).when(navMock).switchToManualAddGamePage(userId, refreshButton);

        TestUtils.setPrivateField(controller, "navHelp", navMock);
        TestUtils.setPrivateField(controller, "alert", alertMock);
        TestUtils.setPrivateField(controller, "refreashButton", refreshButton);
        TestUtils.setPrivateField(controller, "loggedInUserID", userId);

        controller.handleRefreshButton();

        verify(alertMock).showAlert(eq("Error"), eq(List.of("Failed to refresh the page.")));
    }

    /**
	 * Tests the handleAddAPIButton method to ensure it navigates to the Add Game API page.
	 * 
	 * @throws Exception if an error occurs during the test setup or execution
	 */
    @Test
    void testHandleAddAPIButton_callsSwitchToAPISearchPage() throws Exception {
        NavigationHelper navMock = mock(NavigationHelper.class);
        AlertHelper alertMock = mock(AlertHelper.class);
        Button addAPIButton = new Button();
        int userId = 42;

        TestUtils.setPrivateField(controller, "navHelp", navMock);
        TestUtils.setPrivateField(controller, "alert", alertMock);
        TestUtils.setPrivateField(controller, "addAPIButton", addAPIButton);
        TestUtils.setPrivateField(controller, "loggedInUserID", userId);

        controller.handleAddAPIButton();

        verify(navMock).switchToAPISearchPage(userId, addAPIButton);
    }
    
    /**
     * Tests the handleAddAPIButton method when an exception is thrown.
     * It should not crash and should show an alert with the error message.
     * 
     * @throws Exception if an error occurs during the test setup or execution
     */
    @Test
    void testHandleAddAPIButton_whenExceptionThrown_shouldShowAlert() throws Exception {
        NavigationHelper navMock = mock(NavigationHelper.class);
        AlertHelper alertMock = mock(AlertHelper.class);
        Button addAPIButton = new Button();
        int userId = 42;

        doThrow(new RuntimeException("Simulated Error")).when(navMock).switchToAPISearchPage(userId, addAPIButton);

        TestUtils.setPrivateField(controller, "navHelp", navMock);
        TestUtils.setPrivateField(controller, "alert", alertMock);
        TestUtils.setPrivateField(controller, "addAPIButton", addAPIButton);
        TestUtils.setPrivateField(controller, "loggedInUserID", userId);

        controller.handleAddAPIButton();

        verify(alertMock).showAlert(eq("Error"), eq(List.of("Failed to navigate to Add Game API page.")));
    }




}

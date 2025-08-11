package controllers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import javafx.scene.control.*; 
import javafx.scene.image.ImageView;
import models.game;
import services.GameCollectionService;
import services.userService;
import utils.AlertHelper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
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
    
    //////////////////////////// testing handleBrowseImage //////////////////////////////////
    
    /** When a file is chosen, selectedImageFile, label, and preview image should update. */
    @Test
    void handleBrowseImage_whenFileSelected_updatesLabelPreviewAndSelectedFile() throws Exception {
        Button browseBtn = new Button("browse");
        TestUtils.setPrivateField(controller, "browseImageButton", browseBtn);
        javafx.stage.Stage stage = new javafx.stage.Stage();
        stage.setScene(new javafx.scene.Scene(new javafx.scene.layout.StackPane(browseBtn)));
        stage.show();

        File temp = File.createTempFile("cover", ".png");
        temp.deleteOnExit();

        Label selectedImageLabel = new Label();
        ImageView coverImagePreview = new ImageView();
        TestUtils.setPrivateField(controller, "selectedImageLabel", selectedImageLabel);
        TestUtils.setPrivateField(controller, "coverImagePreview", coverImagePreview);

        try (org.mockito.MockedConstruction<javafx.stage.FileChooser> mocked =
                 Mockito.mockConstruction(javafx.stage.FileChooser.class, (mock, ctx) -> {
                     var filters = javafx.collections.FXCollections.<javafx.stage.FileChooser.ExtensionFilter>observableArrayList();
                     when(mock.getExtensionFilters()).thenReturn(filters);
                     when(mock.showOpenDialog(any())).thenReturn(temp);
                 })) {

            controller.handleBrowseImage();
        }

        assertEquals(temp.getName(), selectedImageLabel.getText(), "Label should show chosen filename");
        assertNotNull(coverImagePreview.getImage(), "Preview image should be set");

        File selected = (File) TestUtils.getPrivateField(controller, "selectedImageFile");
        assertNotNull(selected, "selectedImageFile should be set");
        assertEquals(temp.getAbsolutePath(), selected.getAbsolutePath(), "Should store chosen file");
    }

    /** When user cancels, label should say 'No image selected' and no file/preview should be set. */
    @Test
    void handleBrowseImage_whenUserCancels_setsNoImageSelectedAndKeepsPreviewNull() throws Exception {
        Button browseBtn = new Button("browse");
        TestUtils.setPrivateField(controller, "browseImageButton", browseBtn);
        javafx.stage.Stage stage = new javafx.stage.Stage();
        stage.setScene(new javafx.scene.Scene(new javafx.scene.layout.StackPane(browseBtn)));
        stage.show();

        Label selectedImageLabel = new Label("old");
        ImageView coverImagePreview = new ImageView(); // starts null
        TestUtils.setPrivateField(controller, "selectedImageLabel", selectedImageLabel);
        TestUtils.setPrivateField(controller, "coverImagePreview", coverImagePreview);

        try (org.mockito.MockedConstruction<javafx.stage.FileChooser> mocked =
                 Mockito.mockConstruction(javafx.stage.FileChooser.class, (mock, ctx) -> {
                     var filters = javafx.collections.FXCollections.<javafx.stage.FileChooser.ExtensionFilter>observableArrayList();
                     when(mock.getExtensionFilters()).thenReturn(filters);
                     when(mock.showOpenDialog(any())).thenReturn(null); 
                 })) {
            controller.handleBrowseImage();
        }

        assertEquals("No image selected", selectedImageLabel.getText());
        assertNull(coverImagePreview.getImage(), "Preview should remain null when canceled");
        assertNull(TestUtils.getPrivateField(controller, "selectedImageFile"), "No file should be stored on cancel");
    }

    /** Verifies the FileChooser is configured with title and image extensions. */
    @Test
    void handleBrowseImage_configuresFileChooserTitleAndFilters() throws Exception {
        Button browseBtn = new Button("browse");
        TestUtils.setPrivateField(controller, "browseImageButton", browseBtn);
        javafx.stage.Stage stage = new javafx.stage.Stage();
        stage.setScene(new javafx.scene.Scene(new javafx.scene.layout.StackPane(browseBtn)));
        stage.show();

        Label selectedImageLabel = new Label();
        ImageView coverImagePreview = new ImageView();
        TestUtils.setPrivateField(controller, "selectedImageLabel", selectedImageLabel);
        TestUtils.setPrivateField(controller, "coverImagePreview", coverImagePreview);

        final java.util.concurrent.atomic.AtomicReference<javafx.collections.ObservableList<javafx.stage.FileChooser.ExtensionFilter>> filtersRef =
            new java.util.concurrent.atomic.AtomicReference<>();

        try (org.mockito.MockedConstruction<javafx.stage.FileChooser> mocked =
                 Mockito.mockConstruction(javafx.stage.FileChooser.class, (mock, ctx) -> {
                     var filters = javafx.collections.FXCollections.<javafx.stage.FileChooser.ExtensionFilter>observableArrayList();
                     filtersRef.set(filters);
                     when(mock.getExtensionFilters()).thenReturn(filters);
                     when(mock.showOpenDialog(any())).thenReturn(null);
                 })) {

            controller.handleBrowseImage();

            javafx.stage.FileChooser fc = mocked.constructed().get(0);
            verify(fc).setTitle("Select Cover Art Image");
        }

        var filters = filtersRef.get();
        assertNotNull(filters, "Filters list should be present");
        assertEquals(1, filters.size(), "Should add exactly one extension filter");

        var exts = filters.get(0).getExtensions();
        assertTrue(exts.contains("*.png"));
        assertTrue(exts.contains("*.jpg"));
        assertTrue(exts.contains("*.jpeg"));
        assertTrue(exts.contains("*.gif"));
    }


    //////////////////////////// testing getCoverArtField //////////////////////////////////
    
    /**
	 * Tests the getCoverArtField method when a valid image file is selected.
	 * It should copy the image to the Images directory and return the relative path.
	 * 
	 * @throws Exception if an error occurs during the test setup or execution
	 */
    @Test
    void testGetCoverArtField_whenSelectedImageFileExistsAndCopySucceeds_returnsFileUriInImagesDir() throws Exception {
        Path imagesDir = Files.createTempDirectory("gg-test-images");
        System.setProperty("gamegrinding.images.dir", imagesDir.toString());

        try {
            File tempImage = File.createTempFile("game", ".jpg");
            tempImage.deleteOnExit();

            TestUtils.setPrivateField(controller, "selectedImageFile", tempImage);
            TestUtils.setPrivateField(controller, "errors", new ArrayList<>());

            String result = controller.getCoverArtField();

            assertNotNull(result, "Expected a non-null file URI");
            assertTrue(result.startsWith("file:"), "Should return a file: URI, got: " + result);

            Path copied = Path.of(URI.create(result));
            assertTrue(Files.exists(copied), "Copied image should exist at: " + copied);
            assertEquals(imagesDir.normalize(), copied.getParent().normalize(),
                    "Image should be copied into the configured images directory");

            assertTrue(copied.getFileName().toString().endsWith(tempImage.getName()),
                    "Destination filename should be based on the original name");
        } finally {
            System.clearProperty("gamegrinding.images.dir");
        }
    }


    /**
     * Tests the getCoverArtField method when the selected image file copy fails.
     * It should return null and log an error message.
     * 
     * @throws Exception if an error occurs during the test setup or execution
     */
    @Test
    void testGetCoverArtField_whenSelectedImageFileCopyFails_returnsFallbackAndAddsError() throws Exception {
        Path tempDir = Files.createTempDirectory("coverart-test");
        File realImageFile = Files.createTempFile(tempDir, "game", ".jpg").toFile();

        TestUtils.setPrivateField(controller, "selectedImageFile", realImageFile);
        List<String> errors = new ArrayList<>();
        TestUtils.setPrivateField(controller, "errors", errors);

        try (MockedStatic<Files> mocked = Mockito.mockStatic(Files.class, Mockito.CALLS_REAL_METHODS)) {
            mocked.when(() -> Files.copy(any(Path.class), any(Path.class), any(CopyOption[].class))).thenThrow(new IOException("Simulated copy failure"));

            String result = controller.getCoverArtField();

            assertNotNull(result, "Should return fallback file URI when copy fails.");
            assertTrue(result.startsWith("file:"), "Expected file: URI fallback, got: " + result);
            assertTrue(result.endsWith(realImageFile.getName()), "Fallback URI should point to the original file name. Got: " + result);

            assertTrue(
                errors.stream().anyMatch(msg -> msg.startsWith("Failed to copy cover image to: ")),
                "Expected an error starting with 'Failed to copy cover image to: ', got: " + errors
            );
        }
    }


    /**
     * Tests the getCoverArtField method when no image is selected.
     * It should return a classpath URL to one of the packaged placeholder images.
	 * 
	 * @throws Exception if an error occurs during the test setup or execution
	 */
    @Test
    void testGetCoverArtField_whenNoSelectedImage_returnsClasspathPlaceholderUrl() throws Exception {
        TestUtils.setPrivateField(controller, "selectedImageFile", null);
        TestUtils.setPrivateField(controller, "errors", new ArrayList<>());

        String result = controller.getCoverArtField();

        assertNotNull(result, "Expected a non-null placeholder URL when no image is selected.");
        assertTrue(result.startsWith("file:") || result.startsWith("jar:"),
                "Expected a classpath-based URL (file: or jar:), but got: " + result);

        assertTrue(
            result.contains("/Images/placeholder1gameGrinding.png")
         || result.contains("/Images/placeholder2gameGrinding.png"),
            "Expected URL to point to one of the packaged placeholders, but got: " + result
        );
    }

    /**
     * If a file with the same name already exists in the images dir,
     * the method should generate a timestamped filename (base_<ts>.ext).
     * 
     * @throws Exception if an error occurs during the test setup or execution
     */
    @Test
    void testGetCoverArtField_whenDestAlreadyExists_shouldUseTimestampedName() throws Exception {
        Path imagesDir = Files.createTempDirectory("gg-test-images-exists");
        System.setProperty("gamegrinding.images.dir", imagesDir.toString());

        try {
            String originalName = "game.jpg";
            Files.createFile(imagesDir.resolve(originalName));

            Path source = Files.createTempFile("game-src", ".jpg");
            File mockFile = Mockito.mock(File.class);
            when(mockFile.getName()).thenReturn(originalName);
            when(mockFile.toPath()).thenReturn(source);
            when(mockFile.getAbsolutePath()).thenReturn(source.toAbsolutePath().toString());

            TestUtils.setPrivateField(controller, "selectedImageFile", mockFile);
            TestUtils.setPrivateField(controller, "errors", new ArrayList<>());

            String result = controller.getCoverArtField();

            assertNotNull(result, "Should return a file: URI for the copied image.");
            assertTrue(result.startsWith("file:"), "Expected a file: URI, got: " + result);

            Path copied = Path.of(URI.create(result));
            assertTrue(Files.exists(copied), "Timestamped copy should exist: " + copied);

            assertTrue(copied.getFileName().toString().matches("game_\\d+\\.jpg"),
                    "Expected timestamped filename, got: " + copied.getFileName());
            assertEquals(imagesDir.normalize(), copied.getParent().normalize(),
                    "Copy should be inside images dir");
        } finally {
            System.clearProperty("gamegrinding.images.dir");
        }
    }


    /**
     * Selected file has no extension. Ensure copy succeeds and filename is unchanged
     * (unless a collision forces timestamping).
     * @throws Exception if an error occurs during the test setup or execution
     */
    @Test
    void testGetCoverArtField_whenSelectedFileHasNoExtension_copySucceeds() throws Exception {
        Path imagesDir = Files.createTempDirectory("gg-test-noext");
        System.setProperty("gamegrinding.images.dir", imagesDir.toString());
        try {
            Path source = Files.createTempFile("cover_no_ext", "");
            File mockFile = Mockito.mock(File.class);
            when(mockFile.getName()).thenReturn("cover");
            when(mockFile.toPath()).thenReturn(source);
            when(mockFile.getAbsolutePath()).thenReturn(source.toAbsolutePath().toString());

            TestUtils.setPrivateField(controller, "selectedImageFile", mockFile);
            TestUtils.setPrivateField(controller, "errors", new ArrayList<>());

            String result = controller.getCoverArtField();

            assertNotNull(result);
            assertTrue(result.startsWith("file:"), "Expected file: URI, got: " + result);

            Path copied = Path.of(URI.create(result));
            assertTrue(Files.exists(copied), "Copied image should exist");
            String fn = copied.getFileName().toString();
            assertTrue(fn.equals("cover") || fn.matches("cover_\\d+"),
                    "Expected 'cover' or 'cover_<ts>', got: " + fn);
        } finally {
            System.clearProperty("gamegrinding.images.dir");
        }
    }

    /**
     * Copy fails AND obtaining the fallback URI also fails -> method should return null
     * and add an error. (Forces the rare inner catch branch.)
     * @throws Exception if an error occurs during the test setup or execution
     */
    @Test
    void testGetCoverArtField_whenCopyFails_andToURIFails_returnsNullAndAddsError() throws Exception {
        Path imagesDir = Files.createTempDirectory("gg-test-copyfail-urifail");
        System.setProperty("gamegrinding.images.dir", imagesDir.toString());
        try {
            Path source = Files.createTempFile("toUriFail", ".jpg");
            File mockFile = Mockito.mock(File.class);
            when(mockFile.getName()).thenReturn("toUriFail.jpg");
            when(mockFile.toPath()).thenReturn(source);
            when(mockFile.getAbsolutePath()).thenReturn(source.toAbsolutePath().toString());
            when(mockFile.toURI()).thenThrow(new RuntimeException("boom"));

            TestUtils.setPrivateField(controller, "selectedImageFile", mockFile);
            List<String> errors = new ArrayList<>();
            TestUtils.setPrivateField(controller, "errors", errors);

            try (MockedStatic<Files> mocked = Mockito.mockStatic(Files.class, Mockito.CALLS_REAL_METHODS)) {
                mocked.when(() -> Files.copy(any(Path.class), any(Path.class), any(CopyOption[].class)))
                      .thenThrow(new IOException("Simulated copy failure"));

                String result = controller.getCoverArtField();

                assertNull(result, "When both copy and fallback fail, should return null");
                assertTrue(errors.stream().anyMatch(s -> s.startsWith("Failed to copy cover image to: ")),
                        "Expected an error starting with 'Failed to copy cover image to: '");
            }
        } finally {
            System.clearProperty("gamegrinding.images.dir");
        }
    }
    
    /**
     * Selected file path does not exist on disk: copy throws (real NoSuchFileException),
     * so method should return the fallback file URI and add an error.
     * @throws Exception if an error occurs during the test setup or execution
     */
    @Test
    void testGetCoverArtField_whenSelectedFileMissing_returnsFallbackUriAndAddsError() throws Exception {
        Path imagesDir = Files.createTempDirectory("gg-test-missing-src");
        System.setProperty("gamegrinding.images.dir", imagesDir.toString());
        try {
 
            File missing = new File(imagesDir.toFile(), "definitely_missing.jpg");
            assertFalse(missing.exists());

            TestUtils.setPrivateField(controller, "selectedImageFile", missing);
            List<String> errors = new ArrayList<>();
            TestUtils.setPrivateField(controller, "errors", errors);

            String result = controller.getCoverArtField();
            assertNotNull(result, "Fallback to original file URI should still be returned");
            assertTrue(result.startsWith("file:"), "Expected file: URI fallback, got: " + result);
            assertTrue(result.endsWith("definitely_missing.jpg"), "Fallback should point to original filename");

            assertTrue(
                errors.stream().anyMatch(s -> s.startsWith("Failed to copy cover image to: ")),
                "Expected copy-failure error"
            );
        } finally {
            System.clearProperty("gamegrinding.images.dir");
        }
    }

    /**
     * No image selected and placeholders present: returns a classpath URL
     * and DOES NOT add any error messages.
     */
    @Test
    void testGetCoverArtField_whenNoSelectedImage_errorsRemainEmpty() {
        TestUtils.setPrivateField(controller, "selectedImageFile", null);
        List<String> errors = new ArrayList<>();
        TestUtils.setPrivateField(controller, "errors", errors);

        String result = controller.getCoverArtField();

        assertNotNull(result, "Expected placeholder URL");
        assertTrue(result.startsWith("file:") || result.startsWith("jar:"),
                "Expected classpath-based URL (file: or jar:), got: " + result);
        assertTrue(
            result.contains("/Images/placeholder1gameGrinding.png")
         || result.contains("/Images/placeholder2gameGrinding.png"),
            "Should reference one of the packaged placeholders"
        );
        assertTrue(errors.isEmpty(), "No errors should be added when using placeholders");
    }

    ///////////////////////////// testing resolveImagesDir /////////////////////////////////
    
    // Helper to invoke the private method
    private Path callResolveImagesDir() throws Exception {
        Method m = ManualAddGameConttoller.class.getDeclaredMethod("resolveImagesDir");
        m.setAccessible(true);
        return (Path) m.invoke(controller);
    }
    
    /**
     * Priority 1: If system property "gamegrinding.images.dir" is set,
     * it should be used and created if missing.
     */
    @Test
    void resolveImagesDir_usesOverrideProperty_andCreatesDir() throws Exception {
        String oldOverride = System.getProperty("gamegrinding.images.dir");
        Path tempRoot = Files.createTempDirectory("gg-test-override");
        Path override = tempRoot.resolve("customImages");
        try {
            System.setProperty("gamegrinding.images.dir", override.toString());

            Path resolved = callResolveImagesDir();

            assertEquals(override.normalize(), resolved.normalize());
            assertTrue(Files.exists(resolved), "Override dir should be created if missing.");
            assertTrue(Files.isDirectory(resolved));
        } finally {
            if (oldOverride == null) System.clearProperty("gamegrinding.images.dir");
            else System.setProperty("gamegrinding.images.dir", oldOverride);
        }
    }

    /**
     * Priority 1 edge: If createDirectories throws, the method still returns the override path.
     */
    @Test
    void resolveImagesDir_overrideCreateDirectoriesFails_stillReturnsOverride() throws Exception {
        String oldOverride = System.getProperty("gamegrinding.images.dir");
        Path tempRoot = Files.createTempDirectory("gg-test-override-fail");
        Path override = tempRoot.resolve("noCreate");
        try (MockedStatic<Files> mocked = Mockito.mockStatic(Files.class, Mockito.CALLS_REAL_METHODS)) {
            System.setProperty("gamegrinding.images.dir", override.toString());
            mocked.when(() -> Files.createDirectories(override)).thenThrow(new IOException("boom"));

            Path resolved = callResolveImagesDir();

            assertEquals(override.normalize(), resolved.normalize(),
                    "Should still return override even if creation fails");
        } finally {
            if (oldOverride == null) System.clearProperty("gamegrinding.images.dir");
            else System.setProperty("gamegrinding.images.dir", oldOverride);
        }
    }

    /**
     * Priority 2: If no override, use <user.dir>/Images when it exists (or can be created) and is writable.
     */
    @Test
    void resolveImagesDir_usesDevImagesWhenWritable() throws Exception {
        String oldOverride = System.getProperty("gamegrinding.images.dir");
        String oldUserDir = System.getProperty("user.dir");

        Path tempUserDir = Files.createTempDirectory("gg-test-userdir");
        Path devImages = tempUserDir.resolve("Images");
        try {
            System.clearProperty("gamegrinding.images.dir");
            System.setProperty("user.dir", tempUserDir.toString());

            Files.createDirectories(devImages);
            assertTrue(Files.isWritable(devImages));

            Path resolved = callResolveImagesDir();

            assertEquals(devImages.normalize(), resolved.normalize());
            assertTrue(Files.exists(resolved));
        } finally {
            if (oldOverride == null) System.clearProperty("gamegrinding.images.dir");
            else System.setProperty("gamegrinding.images.dir", oldOverride);
            if (oldUserDir == null) System.clearProperty("user.dir");
            else System.setProperty("user.dir", oldUserDir);
        }
    }

    /**
     * Priority 2 (createDirQuiet path): If Images doesn't exist but can be created, it should be returned.
     */
    @Test
    void resolveImagesDir_createsDevImagesWhenMissing_andReturnsIt() throws Exception {
        String oldOverride = System.getProperty("gamegrinding.images.dir");
        String oldUserDir = System.getProperty("user.dir");

        Path tempUserDir = Files.createTempDirectory("gg-test-create-dev");
        Path devImages = tempUserDir.resolve("Images");
        try {
            System.clearProperty("gamegrinding.images.dir");
            System.setProperty("user.dir", tempUserDir.toString());

            assertFalse(Files.exists(devImages));

            Path resolved = callResolveImagesDir();

            assertEquals(devImages.normalize(), resolved.normalize());
            assertTrue(Files.exists(resolved), "Dev Images should be created");
            assertTrue(Files.isDirectory(resolved));
        } finally {
            if (oldOverride == null) System.clearProperty("gamegrinding.images.dir");
            else System.setProperty("gamegrinding.images.dir", oldOverride);
            if (oldUserDir == null) System.clearProperty("user.dir");
            else System.setProperty("user.dir", oldUserDir);
        }
    }

    /**
     * Priority 3: If dev Images exists but NOT writable, falls back to <user.home>/GameGrinding/Images.
     * We mock Files.isWritable to return false for the devImages path.
     */
    @Test
    void resolveImagesDir_devImagesNotWritable_fallsBackToHomeImages() throws Exception {
        String oldOverride = System.getProperty("gamegrinding.images.dir");
        String oldUserDir = System.getProperty("user.dir");
        String oldUserHome = System.getProperty("user.home");

        Path tempUserDir = Files.createTempDirectory("gg-test-dev-nowrite");
        Path devImages = tempUserDir.resolve("Images");

        Path fakeHome = Files.createTempDirectory("gg-test-home");
        Path expectedHomeImages = fakeHome.resolve("GameGrinding").resolve("Images");

        try (MockedStatic<Files> mocked = Mockito.mockStatic(Files.class, Mockito.CALLS_REAL_METHODS)) {
            System.clearProperty("gamegrinding.images.dir");
            System.setProperty("user.dir", tempUserDir.toString());
            System.setProperty("user.home", fakeHome.toString());

            Files.createDirectories(devImages);
            mocked.when(() -> Files.isDirectory(devImages)).thenReturn(true);
            mocked.when(() -> Files.isWritable(devImages)).thenReturn(false);

            Path resolved = callResolveImagesDir();

            assertEquals(expectedHomeImages.normalize(), resolved.normalize(),
                    "Should fall back to user.home/GameGrinding/Images when dev dir not writable");
            assertTrue(Files.exists(resolved), "Home images dir should be created");
        } finally {
            if (oldOverride == null) System.clearProperty("gamegrinding.images.dir");
            else System.setProperty("gamegrinding.images.dir", oldOverride);
            if (oldUserDir == null) System.clearProperty("user.dir");
            else System.setProperty("user.dir", oldUserDir);
            if (oldUserHome == null) System.clearProperty("user.home");
            else System.setProperty("user.home", oldUserHome);
        }
    }

    /**
     * Blank override is ignored; falls through to devImages path.
     */
    @Test
    void resolveImagesDir_blankOverride_ignored_usesDevImages() throws Exception {
        String oldOverride = System.getProperty("gamegrinding.images.dir");
        String oldUserDir = System.getProperty("user.dir");

        Path tempUserDir = Files.createTempDirectory("gg-test-blank-override");
        Path devImages = tempUserDir.resolve("Images");
        try {
            System.setProperty("gamegrinding.images.dir", "   ");
            System.setProperty("user.dir", tempUserDir.toString());

            Path resolved = callResolveImagesDir();

            assertEquals(devImages.normalize(), resolved.normalize());
            assertTrue(Files.exists(resolved));
        } finally {
            if (oldOverride == null) System.clearProperty("gamegrinding.images.dir");
            else System.setProperty("gamegrinding.images.dir", oldOverride);
            if (oldUserDir == null) System.clearProperty("user.dir");
            else System.setProperty("user.dir", oldUserDir);
        }
    }
    
    /////////////////////////////// testing handleSubmit /////////////////////////////////
    
    // Helper: invoke the private handleSubmit()
    private game callHandleSubmit(ManualAddGameConttoller c) throws Exception {
        Method m = ManualAddGameConttoller.class.getDeclaredMethod("handleSubmit");
        m.setAccessible(true);
        return (game) m.invoke(c);
    }
    
    // Helper: build a controller with valid, non-empty UI fields
    private ManualAddGameConttoller buildControllerWithValidInputs(String dateStr, String completion) throws Exception {
        ManualAddGameConttoller ctrl = Mockito.spy(new ManualAddGameConttoller());

        TextField title = new TextField("My Title");
        TextField dev = new TextField("My Dev");
        TextField pub = new TextField("My Pub");
        TextField release = new TextField(dateStr);
        TextField genre = new TextField("Action");
        TextField platform = new TextField("PC");
        TextArea notes = new TextArea("Some notes");
        ChoiceBox<String> status = new ChoiceBox<>();
        status.setValue(completion);

        TestUtils.setPrivateField(ctrl, "titleField", title);
        TestUtils.setPrivateField(ctrl, "developerField", dev);
        TestUtils.setPrivateField(ctrl, "publisherField", pub);
        TestUtils.setPrivateField(ctrl, "releaseDateField", release);
        TestUtils.setPrivateField(ctrl, "genreField", genre);
        TestUtils.setPrivateField(ctrl, "platformField", platform);
        TestUtils.setPrivateField(ctrl, "notesField", notes);
        TestUtils.setPrivateField(ctrl, "completionStatusField", status);

        TestUtils.setPrivateField(ctrl, "errors", new ArrayList<String>());
        AlertHelper mockAlert = mock(AlertHelper.class);
        TestUtils.setPrivateField(ctrl, "alert", mockAlert);

        doReturn("file:/fake/cover.png").when(ctrl).getCoverArtField();

        return ctrl;
    }

    /**
     * Happy path: all fields valid, ISO date, completion set, cover art provided by stub.
     * Expects a non-null game with parsed LocalDate and copied values.
     */
    @Test
    void handleSubmit_validFields_returnsGame() throws Exception {
        ManualAddGameConttoller ctrl = buildControllerWithValidInputs("2024-12-01", "Completed");

        game result = callHandleSubmit(ctrl);

        assertNotNull(result, "Expected a constructed game");
        assertEquals("My Title", result.getTitle());
        assertEquals("My Dev", result.getDeveloper());
        assertEquals("My Pub", result.getPublisher());
        assertEquals(LocalDate.of(2024, 12, 1), result.getReleaseDate());
        assertEquals("Action", result.getGenre());
        assertEquals("PC", result.getPlatform());
        assertEquals("Completed", result.getCompletionStatus());
        assertEquals("Some notes", result.getNotes());
        assertEquals("file:/fake/cover.png", result.getCoverImageUrl());
    }

    /**
     * Missing required field (title). The getter adds an error, and handleSubmit
     * should show the "Missing or Invalid Fields" alert and return null.
     */
    @Test
    void handleSubmit_missingTitle_showsAlertAndReturnsNull() throws Exception {
        ManualAddGameConttoller ctrl = buildControllerWithValidInputs("2024-12-01", "Not Started");
        TextField empty = new TextField("");
        TestUtils.setPrivateField(ctrl, "titleField", empty);

        AlertHelper mockAlert = (AlertHelper) TestUtils.getPrivateField(ctrl, "alert");
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<String>> errorsCap = ArgumentCaptor.forClass(List.class);

        game result = callHandleSubmit(ctrl);

        assertNull(result, "Should return null when validation fails");
        verify(mockAlert).showAlert(eq("Missing or Invalid Fields"), errorsCap.capture());
        assertTrue(errorsCap.getValue().stream().anyMatch(s -> s.toLowerCase().contains("title")),
                "Errors should include a message about the missing title");
    }

    /**
     * Invalid release date format. No earlier validation errors, but parsing fails.
     * Should show "Invalid Release Date" alert and return null.
     */
    @Test
    void handleSubmit_invalidReleaseDate_showsAlertAndReturnsNull() throws Exception {
        ManualAddGameConttoller ctrl = buildControllerWithValidInputs("12/01/2024", "Playing"); // not ISO

        AlertHelper mockAlert = (AlertHelper) TestUtils.getPrivateField(ctrl, "alert");
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<String>> errorsCap = ArgumentCaptor.forClass(List.class);

        game result = callHandleSubmit(ctrl);

        assertNull(result);
        verify(mockAlert).showAlert(eq("Invalid Release Date"), errorsCap.capture());
        assertTrue(errorsCap.getValue().contains("Release date must be in yyyy-MM-dd format."), "Errors should include the specific invalid date message");
    }

    /**
     * Missing completion status (null value). Getter adds error and handleSubmit
     * should show "Missing or Invalid Fields" and return null.
     */
    @Test
    void handleSubmit_nullCompletionStatus_showsAlertAndReturnsNull() throws Exception {
        ManualAddGameConttoller ctrl = buildControllerWithValidInputs("2024-01-01", null);

        AlertHelper mockAlert = (AlertHelper) TestUtils.getPrivateField(ctrl, "alert");
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<String>> errorsCap = ArgumentCaptor.forClass(List.class);

        game result = callHandleSubmit(ctrl);

        assertNull(result);
        verify(mockAlert).showAlert(eq("Missing or Invalid Fields"), errorsCap.capture());
        assertTrue(errorsCap.getValue().stream().anyMatch(s -> s.toLowerCase().contains("completion status")), "Errors should include a message about completion status");
    }

    /**
     * Cover art can be null without causing validation failure.
     * Ensure the game still returns with a null coverImageUrl.
     */
    @Test
    void handleSubmit_coverArtNull_returnsGameWithNullCoverArt() throws Exception {
        ManualAddGameConttoller ctrl = buildControllerWithValidInputs("2023-05-05", "Not Started");
        doReturn(null).when(ctrl).getCoverArtField();

        game result = callHandleSubmit(ctrl);

        assertNotNull(result, "Game should still be created when cover art is null");
        assertNull(result.getCoverImageUrl(), "Cover image url should be null when getCoverArtField() returns null");
    }
    
    /////////////////////////////// testing handleSubmitButtonClick ///////////////////////////////
    
    private static void invokeHandleSubmitButtonClick(ManualAddGameConttoller ctrl) throws Exception {
        var m = ManualAddGameConttoller.class.getDeclaredMethod("handleSubmitButtonClick");
        m.setAccessible(true);
        m.invoke(ctrl);
    }
    
    /** When handleSubmit finds validation errors, it should show the alert and NOT add or navigate. */
    @Test
    void testHandleSubmitButtonClick_whenValidationErrors_showsAlertAndNoAddOrNavigate() throws Exception {
        ManualAddGameConttoller ctrl = spy(new ManualAddGameConttoller());

        TextField title = new TextField(""); // <- invalid
        TextField dev = new TextField("Dev");
        TextField pub = new TextField("Pub");
        TextField rel = new TextField("2024-01-01");
        TextField genre = new TextField("Action");
        TextField platform = new TextField("PC");
        TextArea notes = new TextArea("n");
        ChoiceBox<String> status = new ChoiceBox<>();
        status.setValue("Completed");
        Button submitBtn = new Button();

        TestUtils.setPrivateField(ctrl, "titleField", title);
        TestUtils.setPrivateField(ctrl, "developerField", dev);
        TestUtils.setPrivateField(ctrl, "publisherField", pub);
        TestUtils.setPrivateField(ctrl, "releaseDateField", rel);
        TestUtils.setPrivateField(ctrl, "genreField", genre);
        TestUtils.setPrivateField(ctrl, "platformField", platform);
        TestUtils.setPrivateField(ctrl, "notesField", notes);
        TestUtils.setPrivateField(ctrl, "completionStatusField", status);
        TestUtils.setPrivateField(ctrl, "submitButton", submitBtn);

        TestUtils.setPrivateField(ctrl, "errors", new ArrayList<String>());
        AlertHelper mockAlert = mock(AlertHelper.class);
        GameCollectionService mockSvc = mock(GameCollectionService.class);
        NavigationHelper mockNav = mock(NavigationHelper.class);
        TestUtils.setPrivateField(ctrl, "alert", mockAlert);
        TestUtils.setPrivateField(ctrl, "gameCollectionService", mockSvc);
        TestUtils.setPrivateField(ctrl, "navHelp", mockNav);

        doReturn("file:/fake.png").when(ctrl).getCoverArtField();

        TestUtils.setPrivateField(ctrl, "loggedInUserID", 1);

        var m = ManualAddGameConttoller.class.getDeclaredMethod("handleSubmitButtonClick");
        m.setAccessible(true);
        m.invoke(ctrl);


        verify(mockAlert).showAlert(eq("Missing or Invalid Fields"), anyList());
        verifyNoInteractions(mockSvc, mockNav);
    }

    /** When there is a valid game but no logged-in user, nothing should be added/navigated. */
    @Test
    void testHandleSubmitButtonClick_whenNoLoggedInUser_doesNothing() throws Exception {
        ManualAddGameConttoller ctrl = spy(new ManualAddGameConttoller());

        doReturn("T").when(ctrl).getTitleField();
        doReturn("D").when(ctrl).getDeveloperField();
        doReturn("P").when(ctrl).getPublisherField();
        doReturn("2024-01-01").when(ctrl).getReleaseDateField();
        doReturn("Action").when(ctrl).getGenreField();
        doReturn("PC").when(ctrl).getPlatformField();
        doReturn("Notes").when(ctrl).getNotesField();
        doAnswer(inv -> inv.getArgument(0)).when(ctrl).getCompletionStatusField(any()); // echo back value
        doReturn("file:/img.png").when(ctrl).getCoverArtField();

        ChoiceBox<String> status = new ChoiceBox<>();
        status.setValue("Completed");
        Button submitBtn = new Button();
        TestUtils.setPrivateField(ctrl, "completionStatusField", status);
        TestUtils.setPrivateField(ctrl, "submitButton", submitBtn);

        TestUtils.setPrivateField(ctrl, "errors", new ArrayList<String>());
        GameCollectionService mockSvc = mock(GameCollectionService.class);
        NavigationHelper mockNav = mock(NavigationHelper.class);
        AlertHelper mockAlert = mock(AlertHelper.class);
        TestUtils.setPrivateField(ctrl, "gameCollectionService", mockSvc);
        TestUtils.setPrivateField(ctrl, "navHelp", mockNav);
        TestUtils.setPrivateField(ctrl, "alert", mockAlert);

        TestUtils.setPrivateField(ctrl, "loggedInUserID", 0);

        invokeHandleSubmitButtonClick(ctrl);

        verifyNoInteractions(mockSvc, mockNav, mockAlert);
    }

    /** When addGameToCollection succeeds, it should navigate to the collection and not show an error alert. */
    @Test
    void testHandleSubmitButtonClick_whenAddSucceeds_navigatesToCollection() throws Exception {
        ManualAddGameConttoller ctrl = spy(new ManualAddGameConttoller());

        doReturn("T").when(ctrl).getTitleField();
        doReturn("D").when(ctrl).getDeveloperField();
        doReturn("P").when(ctrl).getPublisherField();
        doReturn("2024-01-01").when(ctrl).getReleaseDateField();
        doReturn("Action").when(ctrl).getGenreField();
        doReturn("PC").when(ctrl).getPlatformField();
        doReturn("Notes").when(ctrl).getNotesField();
        doAnswer(inv -> inv.getArgument(0)).when(ctrl).getCompletionStatusField(any());
        doReturn("file:/img.png").when(ctrl).getCoverArtField();

        ChoiceBox<String> status = new ChoiceBox<>();
        status.setValue("Completed");
        Button submitBtn = new Button();
        TestUtils.setPrivateField(ctrl, "completionStatusField", status);
        TestUtils.setPrivateField(ctrl, "submitButton", submitBtn);
        TestUtils.setPrivateField(ctrl, "errors", new ArrayList<String>());

        GameCollectionService mockSvc = mock(GameCollectionService.class);
        when(mockSvc.addGameToCollection(any(), eq(1))).thenReturn(true);
        NavigationHelper mockNav = mock(NavigationHelper.class);
        AlertHelper mockAlert = mock(AlertHelper.class);
        TestUtils.setPrivateField(ctrl, "gameCollectionService", mockSvc);
        TestUtils.setPrivateField(ctrl, "navHelp", mockNav);
        TestUtils.setPrivateField(ctrl, "alert", mockAlert);

        TestUtils.setPrivateField(ctrl, "loggedInUserID", 1);

        invokeHandleSubmitButtonClick(ctrl);

        verify(mockSvc).addGameToCollection(any(), eq(1));
        verify(mockNav).switchToGameCollection(eq(1), eq(submitBtn));
        verify(mockAlert, never()).showAlert(eq("Error"), anyList());
    }

    /** When addGameToCollection returns false, it should show an error alert and not navigate. */
    @Test
    void testHandleSubmitButtonClick_whenAddFails_showsErrorAlert() throws Exception {
        ManualAddGameConttoller ctrl = spy(new ManualAddGameConttoller());

        doReturn("T").when(ctrl).getTitleField();
        doReturn("D").when(ctrl).getDeveloperField();
        doReturn("P").when(ctrl).getPublisherField();
        doReturn("2024-01-01").when(ctrl).getReleaseDateField();
        doReturn("Action").when(ctrl).getGenreField();
        doReturn("PC").when(ctrl).getPlatformField();
        doReturn("Notes").when(ctrl).getNotesField();
        doAnswer(inv -> inv.getArgument(0)).when(ctrl).getCompletionStatusField(any());
        doReturn("file:/img.png").when(ctrl).getCoverArtField();

        ChoiceBox<String> status = new ChoiceBox<>();
        status.setValue("Completed");
        Button submitBtn = new Button();
        TestUtils.setPrivateField(ctrl, "completionStatusField", status);
        TestUtils.setPrivateField(ctrl, "submitButton", submitBtn);
        TestUtils.setPrivateField(ctrl, "errors", new ArrayList<String>());

        GameCollectionService mockSvc = mock(GameCollectionService.class);
        when(mockSvc.addGameToCollection(any(), eq(1))).thenReturn(false);
        NavigationHelper mockNav = mock(NavigationHelper.class);
        AlertHelper mockAlert = mock(AlertHelper.class);
        TestUtils.setPrivateField(ctrl, "gameCollectionService", mockSvc);
        TestUtils.setPrivateField(ctrl, "navHelp", mockNav);
        TestUtils.setPrivateField(ctrl, "alert", mockAlert);

        TestUtils.setPrivateField(ctrl, "loggedInUserID", 1);

        invokeHandleSubmitButtonClick(ctrl);

        verify(mockSvc).addGameToCollection(any(), eq(1));
        verify(mockAlert).showAlert(eq("Error"), argThat(list ->
            list != null && list.stream().anyMatch(s -> s.contains("Failed to add game to collection."))
        ));
        verify(mockNav, never()).switchToGameCollection(anyInt(), any());
    }

    /** If the service throws a checked exception, the controller should catch it and not navigate or alert. */
    @Test
    void testHandleSubmitButtonClick_whenServiceThrows_catchesAndDoesNotNavigateOrAlert() throws Exception {
        ManualAddGameConttoller ctrl = spy(new ManualAddGameConttoller());

        doReturn("T").when(ctrl).getTitleField();
        doReturn("D").when(ctrl).getDeveloperField();
        doReturn("P").when(ctrl).getPublisherField();
        doReturn("2024-01-01").when(ctrl).getReleaseDateField();
        doReturn("Action").when(ctrl).getGenreField();
        doReturn("PC").when(ctrl).getPlatformField();
        doReturn("Notes").when(ctrl).getNotesField();
        doAnswer(inv -> inv.getArgument(0)).when(ctrl).getCompletionStatusField(any());
        doReturn("file:/img.png").when(ctrl).getCoverArtField();

        ChoiceBox<String> status = new ChoiceBox<>();
        status.setValue("Completed");
        Button submitBtn = new Button();
        TestUtils.setPrivateField(ctrl, "completionStatusField", status);
        TestUtils.setPrivateField(ctrl, "submitButton", submitBtn);
        TestUtils.setPrivateField(ctrl, "errors", new ArrayList<String>());

        GameCollectionService mockSvc = mock(GameCollectionService.class);

        when(mockSvc.addGameToCollection(any(), eq(1))).thenThrow(new org.apache.hc.core5.http.ParseException("boom"));
        NavigationHelper mockNav = mock(NavigationHelper.class);
        AlertHelper mockAlert = mock(AlertHelper.class);
        TestUtils.setPrivateField(ctrl, "gameCollectionService", mockSvc);
        TestUtils.setPrivateField(ctrl, "navHelp", mockNav);
        TestUtils.setPrivateField(ctrl, "alert", mockAlert);

        TestUtils.setPrivateField(ctrl, "loggedInUserID", 1);

        invokeHandleSubmitButtonClick(ctrl);

        verify(mockSvc).addGameToCollection(any(), eq(1));
        verifyNoInteractions(mockNav);
        verify(mockAlert, never()).showAlert(any(), anyList());
    }

    
    
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

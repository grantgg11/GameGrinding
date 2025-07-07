package controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.game;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import services.GameCollectionService;
import services.GameService;
import services.userService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static controllers.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the FilterCollectionController class in the GameGrinding application.
 *
 * This controller allows users to filter their game collection by platform, genre,
 * and completion status. These tests verify:
 * - Loading user data and invoking UI setup methods
 * - Creation and behavior of toggle buttons for each filter type
 * - Handling of confirm action to apply filters and navigate
 * - Error and empty‐list scenarios in data loading and filtering
 * - Scene switching and FXML loader behavior under success and failure conditions
 */
@ExtendWith(ApplicationExtension.class) 
class FilterCollectionControllerTest {

    private FilterCollectionController controller;
    private GameService mockGameService;
    private GameCollectionService mockCollectionService;
    private userService mockUserService;
    private NavigationHelper mockNavHelper;

    /**
     * Sets up controller with mocked services and empty UI containers before each test.
     */
    @BeforeEach
    void setUp() {
        controller = new FilterCollectionController() {
            {
                try {
                    setPrivateField(this, "gameService", mock(GameService.class));
                    setPrivateField(this, "collectionService", mock(GameCollectionService.class));
                    setPrivateField(this, "userSer", mock(userService.class));
                    setPrivateField(this, "navHelp", mock(NavigationHelper.class));
                } catch (Exception e) {
                    fail("Injection failed: " + e.getMessage());
                }
            }
            @Override
            protected void onUserDataLoad() {
                super.onUserDataLoad(); 
            }
        };

        mockGameService = (GameService) getPrivateField(controller, "gameService");
        mockCollectionService = (GameCollectionService) getPrivateField(controller, "collectionService");
        mockUserService = (userService) getPrivateField(controller, "userSer");
        mockNavHelper = (NavigationHelper) getPrivateField(controller, "navHelp");

        controller.loggedInUserID = 1;

        // prepare UI containers and buttons
        setPrivateField(controller, "platformButtonContainer", new VBox());
        setPrivateField(controller, "genreButtonContainer", new VBox());
        setPrivateField(controller, "completionButtonContainer", new VBox());
        setPrivateField(controller, "confirmButton", new Button());
        setPrivateField(controller, "gameCollectionButton", new Button());
        setPrivateField(controller, "settingsButton", new Button());
        setPrivateField(controller, "helpButton", new Button());
        setPrivateField(controller, "logoutButton", new Button());
    }

    /**
     * Verifies that onUserDataLoad invokes creation of all filter button groups when non‐empty platform and genre lists are returned.
     */
    @Test
    void testOnUserDataLoad_withValidPlatformsAndGenres_callsCreationMethods() {
        List<String> mockPlatforms = List.of("PC", "Xbox");
        List<String> mockGenres = List.of("Action", "RPG");

        setPrivateField(controller, "loggedInUserID", 1);
        when(mockGameService.getPlatformsFromCollection(1)).thenReturn(mockPlatforms);
        when(mockGameService.getGenreFromCollection(1)).thenReturn(mockGenres);
        setPrivateField(controller, "gameService", mockGameService);
        FilterCollectionController spyController = spy(controller);
        doNothing().when(spyController).createPlatformButtons();
        doNothing().when(spyController).createGenreButtons();
        doNothing().when(spyController).createCompletionStatusButtons();
        invokePrivateMethod(spyController, "onUserDataLoad");

        verify(spyController).createPlatformButtons();
        verify(spyController).createGenreButtons();
        verify(spyController).createCompletionStatusButtons();
    }

    /**
     * Ensures onUserDataLoad handles empty platform and genre lists without error.
     */
    @Test
    void testOnUserDataLoad_withEmptyLists_printsNoPlatformsGenres() {
        when(mockGameService.getPlatformsFromCollection(1)).thenReturn(Collections.emptyList());
        when(mockGameService.getGenreFromCollection(1)).thenReturn(Collections.emptyList());

        assertDoesNotThrow(() -> controller.onUserDataLoad());
    }

    /**
     * Verifies that onUserDataLoad catches and logs exceptions from data service calls.
     */
    @Test
    void testOnUserDataLoad_withException_printsError() {
        when(mockGameService.getPlatformsFromCollection(1)).thenThrow(new RuntimeException("Simulated Failure"));
        assertDoesNotThrow(() -> controller.onUserDataLoad());
    }
    
    /**
     * Tests creation and toggle behavior of platform filter buttons.
     * Verifies that selecting and deselecting buttons updates selectedPlatforms list.
     */
    @SuppressWarnings("unchecked")
	@Test
    void testCreatePlatformButtons_behaviorAndSelection() {
        List<String> testPlatforms = List.of("PC", "Switch");
        setPrivateField(controller, "platforms", testPlatforms);
        VBox container = new VBox();
        setPrivateField(controller, "platformButtonContainer", container);

        List<String> selectedPlatforms = new ArrayList<>();
        setPrivateField(controller, "selectedPlatforms", selectedPlatforms);

        FilterCollectionController spyController = spy(controller);
        invokePrivateMethod(spyController, "createPlatformButtons");

        assertEquals(2, container.getChildren().size(), "Expected 2 toggle buttons for platforms");

        for (int i = 0; i < testPlatforms.size(); i++) {
            ToggleButton button = (ToggleButton) container.getChildren().get(i);
            assertEquals(testPlatforms.get(i), button.getText());
            assertEquals(150.0, button.getPrefWidth());
            assertEquals(30.0, button.getPrefHeight());

            button.setSelected(true);
			List<String> selected = (List<String>) getPrivateField(spyController, "selectedPlatforms");
            assertTrue(selected.contains(button.getText()), "Expected platform to be added on select");

            button.setSelected(false);
            selected = (List<String>) getPrivateField(spyController, "selectedPlatforms");
            assertFalse(selected.contains(button.getText()), "Expected platform to be removed on deselect");
        }
    }

    /**
     * Tests creation and toggle behavior of genre filter buttons.
     * Ensures selection and deselection update selectedGenres list.
     */
    @SuppressWarnings("unchecked")
    @Test
    void testCreateGenreButtons_behaviorAndSelection() {
        List<String> testGenres = List.of("RPG", "Action");
        setPrivateField(controller, "genres", testGenres);
        VBox container = new VBox();
        setPrivateField(controller, "genreButtonContainer", container);

        List<String> selectedGenres = new ArrayList<>();
        setPrivateField(controller, "selectedGenres", selectedGenres);

        FilterCollectionController spyController = spy(controller);
        invokePrivateMethod(spyController, "createGenreButtons");

        assertEquals(2, container.getChildren().size(), "Expected 2 toggle buttons for genres");
        for (int i = 0; i < testGenres.size(); i++) {
            ToggleButton button = (ToggleButton) container.getChildren().get(i);
            assertEquals(testGenres.get(i), button.getText());
            assertEquals(150.0, button.getPrefWidth());
            assertEquals(30.0, button.getPrefHeight());

            button.setSelected(true);
            List<String> selected = (List<String>) getPrivateField(spyController, "selectedGenres");
            assertTrue(selected.contains(button.getText()), "Genre should be added on selection");

            button.setSelected(false);
            selected = (List<String>) getPrivateField(spyController, "selectedGenres");
            assertFalse(selected.contains(button.getText()), "Genre should be removed on deselection");
        }
    }

    /**
     * Verifies creation and toggle behavior of completion status buttons.
     * Confirms selection and deselection update selectedCompletion list.
     */
    @SuppressWarnings("unchecked")
    @Test
    void testCreateCompletionStatusButtons_behaviorAndSelection() {
        VBox mockContainer = new VBox();
        setPrivateField(controller, "completionButtonContainer", mockContainer);

        List<String> selectedCompletion = new ArrayList<>();
        setPrivateField(controller, "selectedCompletion", selectedCompletion);
        FilterCollectionController spyController = spy(controller);
        invokePrivateMethod(spyController, "createCompletionStatusButtons");

        assertEquals(3, mockContainer.getChildren().size(), "Should create 3 completion status buttons");

        for (Node node : mockContainer.getChildren()) {
            ToggleButton button = (ToggleButton) node;
            assertTrue(List.of("Not Started", "Playing", "Completed").contains(button.getText()), "Button text must be valid status");
            assertEquals(150.0, button.getPrefWidth());
            assertEquals(30.0, button.getPrefHeight());

            button.setSelected(true);
            List<String> selected = (List<String>) getPrivateField(spyController, "selectedCompletion");
            assertTrue(selected.contains(button.getText()), "Completion status should be added on selection");

            button.setSelected(false);
            selected = (List<String>) getPrivateField(spyController, "selectedCompletion");
            assertFalse(selected.contains(button.getText()), "Completion status should be removed on deselection");
        }
    }

    /**
     * Ensures that handleConfirmButton applies filters via collectionService, initializes GameCollectionController, and passes filtered list.
     */
    @Test
    void testHandleConfirmButton_appliesFiltersAndSwitchesScene() throws Exception {
        List<game> filteredGames = List.of(new game(), new game());
        when(mockCollectionService.filterCollection(anyInt(), any(), any(), any()))
                .thenReturn(filteredGames);

        Button confirmButton = mock(Button.class);
        Scene scene = mock(Scene.class);
        Stage stage = mock(Stage.class);
        when(confirmButton.getScene()).thenReturn(scene);
        when(scene.getWindow()).thenReturn(stage);
        when(stage.getIcons()).thenReturn(FXCollections.observableArrayList()); 
        setPrivateField(controller, "confirmButton", confirmButton);

        FXMLLoader mockLoader = mock(FXMLLoader.class);

        Parent mockRoot = mock(Parent.class, invocation -> {
            if ("getStyleClass".equals(invocation.getMethod().getName())) {
                return FXCollections.observableArrayList(); 
            }
            return RETURNS_DEFAULTS.answer(invocation);
        });

        GameCollectionController mockGameCollectionController = mock(GameCollectionController.class);
        when(mockLoader.load()).thenReturn(mockRoot);
        when(mockLoader.getController()).thenReturn(mockGameCollectionController);

        FilterCollectionController spyController = spy(controller);
        doReturn(mockLoader).when(spyController).createGameCollectionLoader(); 

        invokePrivateMethod(spyController, "handleConfirmButton");

        verify(mockCollectionService).filterCollection(eq(1), any(), any(), any());
        verify(mockGameCollectionController).setFilteredCollection(filteredGames);
        verify(mockGameCollectionController).setUserID(1);
    }
    
    /**
     * Verifies that when no games match filters, a warning message is printed.
     */
    @Test
    void testHandleConfirmButton_noMatchingGames_printsNoMatchMessage() throws Exception {
        when(mockCollectionService.filterCollection(anyInt(), any(), any(), any()))
                .thenReturn(Collections.emptyList());
        Button confirmButton = mock(Button.class);
        Scene scene = mock(Scene.class);
        Stage stage = mock(Stage.class);

        when(stage.getIcons()).thenReturn(FXCollections.observableList(new ArrayList<>()));
        when(confirmButton.getScene()).thenReturn(scene);
        when(scene.getWindow()).thenReturn(stage);
        setPrivateField(controller, "confirmButton", confirmButton);

        FilterCollectionController spyController = spy(controller);
        FXMLLoader mockLoader = mock(FXMLLoader.class);
        Parent mockRoot = mock(Parent.class, invocation -> {
            if ("getStyleClass".equals(invocation.getMethod().getName())) {
                return FXCollections.observableArrayList();
            }
            return RETURNS_DEFAULTS.answer(invocation);
        });
        GameCollectionController mockGameCollectionController = mock(GameCollectionController.class);
        when(mockLoader.load()).thenReturn(mockRoot);
        when(mockLoader.getController()).thenReturn(mockGameCollectionController);
        doReturn(mockLoader).when(spyController).createGameCollectionLoader();

        setPrivateField(spyController, "collectionService", mockCollectionService);
        setPrivateField(spyController, "selectedGenres", new ArrayList<>());
        setPrivateField(spyController, "selectedPlatforms", new ArrayList<>());
        setPrivateField(spyController, "selectedCompletion", new ArrayList<>());
        spyController.loggedInUserID = 1;

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        invokePrivateMethod(spyController, "handleConfirmButton");

        verify(mockCollectionService).filterCollection(eq(1), isNull(), isNull(), isNull());
        assertTrue(outContent.toString().contains("No games match the filters."));
    }


    /**
     * Tests switchToGameCollection handles null controller by printing an error.
     */
    @Test
    void testSwitchToGameCollection_nullController_printsErrorAndExits() throws Exception {
        FXMLLoader mockLoader = mock(FXMLLoader.class);
        Parent mockRoot = mock(Parent.class, invocation -> {
            if ("getStyleClass".equals(invocation.getMethod().getName())) {
                return FXCollections.observableArrayList();
            }
            return RETURNS_DEFAULTS.answer(invocation);
        });

        when(mockLoader.load()).thenReturn(mockRoot);
        when(mockLoader.getController()).thenReturn(null);

        Button confirmButton = mock(Button.class);
        Scene mockScene = mock(Scene.class);
        Stage mockStage = mock(Stage.class);
        when(confirmButton.getScene()).thenReturn(mockScene);
        when(mockScene.getWindow()).thenReturn(mockStage);
        setPrivateField(controller, "confirmButton", confirmButton);

        FilterCollectionController spyController = spy(controller);
        doReturn(mockLoader).when(spyController).createGameCollectionLoader();

        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));

        invokePrivateMethod(
        	    spyController,
        	    "switchToGameCollection",
        	    new Class<?>[]{List.class},
        	    new Object[]{List.of(new game())}
        	);
        assertTrue(errContent.toString().contains("Error: GameCollectionController is null!"));
    }

    /**
     * Tests switchToGameCollection logs load failures when FXMLLoader throws IOException.
     */
    @Test
    void testSwitchToGameCollection_loaderThrowsIOException_printsError() throws Exception {
        FXMLLoader mockLoader = mock(FXMLLoader.class);
        when(mockLoader.load()).thenThrow(new IOException("FXML load failure"));

        Button confirmButton = mock(Button.class);
        Scene mockScene = mock(Scene.class);
        Stage mockStage = mock(Stage.class);
        when(confirmButton.getScene()).thenReturn(mockScene);
        when(mockScene.getWindow()).thenReturn(mockStage);
        setPrivateField(controller, "confirmButton", confirmButton);

        FilterCollectionController spyController = spy(controller);
        doReturn(mockLoader).when(spyController).createGameCollectionLoader();

        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));

        invokePrivateMethod(
        	    spyController,
        	    "switchToGameCollection",
        	    new Class<?>[]{List.class},
        	    new Object[]{List.of(new game())}
        	);
        assertTrue(errContent.toString().contains("Error: Could not load GameCollection.fxml!"));
    }

    /**
     * Verifies that createGameCollectionLoader returns a properly configured FXMLLoader.
     */
    @Test
    void testCreateGameCollectionLoader_returnsCorrectLoader() {
        FXMLLoader loader = controller.createGameCollectionLoader();

        assertNotNull(loader, "FXMLLoader should not be null");
        assertNotNull(loader.getLocation(), "FXMLLoader location should not be null");
        assertTrue(loader.getLocation().toString().endsWith("/views/GameCollection.fxml"), 
                   "Loader location should point to GameCollection.fxml");
    }

    /**
     * Confirms navigation to collection view on Game Collection button click.
     */
    @Test
    void testHandleGameCollectionButton_navigatesSuccessfully() {
        invokePrivateMethod(controller, "handleGameCollectionButton");
        verify(mockNavHelper).switchToGameCollection(eq(1), any());
    }

    /**
     * Verifies Settings button click triggers navigation without throwing.
     */
    @Test
    void testHandleSettingsButton_invokesNavigation() {
        Button mockSettingsButton = mock(Button.class);
        setPrivateField(controller, "settingsButton", mockSettingsButton);
        setPrivateField(controller, "loggedInUserID", 1);

        NavigationHelper mockNavHelper = mock(NavigationHelper.class);
        setPrivateField(controller, "navHelp", mockNavHelper);
        invokePrivateMethod(controller, "handleSettingsButton");
        verify(mockNavHelper).switchToSettingsPage(1, mockSettingsButton);
    }

    /**
     * Ensures exceptions in Settings navigation are caught and suppressed.
     */
    @Test
    void testHandleSettingsButton_whenExceptionThrown_shouldCatchIt() {
        doThrow(new RuntimeException("Settings failure")).when(mockNavHelper).switchToSettingsPage(anyInt(), any());
        assertDoesNotThrow(() -> invokePrivateMethod(controller, "handleSettingsButton"));
    }

    /**
	 * Tests that clicking the help button invokes the navigation helper to switch to the help page.
	 */
    @Test
    void testHandleHelpButton_invokesNavigation() {
        Button mockHelpButton = mock(Button.class);
        setPrivateField(controller, "helpButton", mockHelpButton);
        setPrivateField(controller, "loggedInUserID", 1);

        NavigationHelper mockNavHelper = mock(NavigationHelper.class);
        setPrivateField(controller, "navHelp", mockNavHelper);

        invokePrivateMethod(controller, "handleHelpButton");

        verify(mockNavHelper).switchToHelpPage(1, mockHelpButton);
    }

    /**
     * Ensures exceptions in Help navigation are caught and suppressed.
     */
    @Test
    void testHandleHelpButton_whenExceptionThrown_shouldCatchIt() {
        doThrow(new RuntimeException("Help failure")).when(mockNavHelper).switchToHelpPage(anyInt(), any());
        assertDoesNotThrow(() -> invokePrivateMethod(controller, "handleHelpButton"));
    }

    /**
     * Verifies logout button click logs out and switches to login page.
     */
    @Test
    void testHandleLogoutButton_callsLogoutAndSwitch() {
        invokePrivateMethod(controller, "handleLogoutButton");
        verify(mockUserService).logout();
        verify(mockNavHelper).switchToLoginPage(any());
    }




}

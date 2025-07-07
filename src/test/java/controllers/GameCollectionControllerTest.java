package controllers;


import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.control.ScrollPane;
import javafx.stage.Stage;

import models.game;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.util.WaitForAsyncUtils;
 
import reports.PerformanceTracker;
import services.GameCollectionService;
import services.userService;
/**
* Unit test class for GameCollectionController.
* 
* This test suite covers the major functional and UI-related behaviors of the
* game collection screen, including:
*     Initialization and UI setup
*     Loading and sorting the user's game collection
*     Search functionality and performance tracking
*     Dynamic game box creation and image loading
*     Navigation to related pages (Filter, Settings, Help, Logout)
*     Handling errors gracefully during runtime
*
* Dependencies such as {@code GameCollectionService}, {@code NavigationHelper}, and
* {@code PerformanceTracker} are mocked to isolate controller logic.
* JavaFX fields are injected via reflection for complete behavioral coverage.
*/
@ExtendWith({ApplicationExtension.class, JavaFXThreadingExtension.class})
class GameCollectionControllerTest {

    private GameCollectionController controller;
    private GameCollectionService mockGameService;
    private NavigationHelper mockNavHelper;
    private PerformanceTracker mockTracker;
    private userService mockUserService;

    private TextField searchBar;
    private ChoiceBox<String> sortChoiceBox;
    private FlowPane gameFlowPane;
    private ScrollPane gameCollectionScrollPane;
    private Button filterButton;
    private Button settingsButton;
    private Button helpButton;
    private Button logoutButton;

    /**
     * Sets up the test environment before each test case.
     * Initializes a new instance of the controller, mocks all required dependencies
     * including services and helpers, and injects them using reflection. Also sets up
     * mocked UI components such as buttons, fields, and panes, and preloads a valid
     * test user ID to simulate an authenticated session.
     */
    @BeforeEach
    void setUp() {
        controller = spy(new GameCollectionController());

        mockGameService = mock(GameCollectionService.class);
        mockNavHelper = mock(NavigationHelper.class);
        mockTracker = mock(PerformanceTracker.class);
        mockUserService = mock(userService.class);

        searchBar = new TextField();
        sortChoiceBox = new ChoiceBox<>();
        gameFlowPane = new FlowPane();
        gameCollectionScrollPane = new ScrollPane();
        filterButton = new Button();
        settingsButton = new Button();
        helpButton = new Button();
        logoutButton = new Button();



        TestUtils.setPrivateField(controller, "gameCollectionService", mockGameService);
        TestUtils.setPrivateField(controller, "navHelp", mockNavHelper);
        TestUtils.setPrivateField(controller, "tracker", mockTracker);
        TestUtils.setPrivateField(controller, "userSer", mockUserService);

        TestUtils.setPrivateField(controller, "searchBar", searchBar);
        TestUtils.setPrivateField(controller, "sortChoiceBox", sortChoiceBox);
        TestUtils.setPrivateField(controller, "gameFlowPane", gameFlowPane);
        TestUtils.setPrivateField(controller, "gameCollectionScrollPane", gameCollectionScrollPane);
        TestUtils.setPrivateField(controller, "filterButton", filterButton);
        TestUtils.setPrivateField(controller, "settingsButton", settingsButton);
        TestUtils.setPrivateField(controller, "helpButton", helpButton);
        TestUtils.setPrivateField(controller, "logoutButton", logoutButton);

        controller.setUserID(1);
    }
    
    /**
     * Verifies that the sort choice box listener is attached and responds to value changes during initialization.
     */
    @Test
    void testInitialize_shouldAttachSortChoiceListener() {
        Platform.runLater(() -> {
            controller.initialize();
            sortChoiceBox.setValue("Title");
            sortChoiceBox.getOnAction().handle(null);  
            assertEquals("Title", sortChoiceBox.getValue());
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    /**
     * Verifies that setting the user ID and calling onUserDataLoad logs the correct user ID to the console.
     */
    @Test
    void testOnUserDataLoad_shouldLogUserId() {
    	controller.setUserID(1);
		controller.onUserDataLoad();
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));
        try {
            controller.onUserDataLoad();
            String output = outContent.toString().trim();
            assertTrue(output.contains("onUserDataLoad() called in GameCollectionController with user ID: 1"), "Expected log output to include user ID.");
            assertEquals(1, controller.loggedInUserID, "Expected loggedInUserID to be set to 1");
        } finally {       
            System.setOut(originalOut);
        }	
	}
    
    /**
     * Tests that calling loadUserCollection triggers the GameCollectionService and updates the UI with the game list.
     */
    @Test
    void loadUserCollection_shouldCallGameServiceAndUpdateUI() {
		List<game> mockGames = new ArrayList<>();
		when(mockGameService.getUserCollection(1)).thenReturn(mockGames);
		controller.loadUserCollection(mockGames);
		verify(mockGameService).getUserCollection(1);	
		assertTrue(gameFlowPane.getChildren().isEmpty(), "Game flow pane should be empty initially");
	}
    
    /**
     * Ensures that no UI update occurs when the logged-in user ID is invalid (0).
     */
    @Test
    void testLoadUserCollection_shouldDoNothingIfUserIdIsInvalid() {
        controller.loggedInUserID = 0; 

        Platform.runLater(() -> {
            controller.loadUserCollection(List.of(mock(game.class)));
            assertTrue(gameFlowPane.getChildren().isEmpty(), "gameFlowPane should remain empty");
        });

        WaitForAsyncUtils.waitForFxEvents();
    }
    
    /**
     * Verifies that loadUserCollection correctly creates game boxes in the FlowPane for valid game entries.
     */
    @Test
    void testLoadUserCollection_shouldAddGameBoxesToFlowPane() {
        controller.loggedInUserID = 42;

        game mockGame = mock(game.class);
        when(mockGame.getTitle()).thenReturn("Test Game");
        when(mockGame.getCoverImageUrl()).thenReturn("/Images/placeholder1gameGrinding.png");

        List<game> games = List.of(mockGame, mockGame);

        Platform.runLater(() -> {
            controller.loadUserCollection(games);
            assertEquals(2, gameFlowPane.getChildren().size(), "Expected 2 game boxes to be loaded");
        });

        WaitForAsyncUtils.waitForFxEvents();
    }
    
    /**
     * Ensures that the layout and padding of the FlowPane are set correctly when loading the collection.
     */
    @Test
    void testLoadUserCollection_shouldSetLayoutAndPadding() {
        controller.loggedInUserID = 1;

        game mockGame = mock(game.class);
        when(mockGame.getTitle()).thenReturn("Test Game");
        when(mockGame.getCoverImageUrl()).thenReturn("/Images/placeholder1gameGrinding.png");

        Platform.runLater(() -> {
            controller.loadUserCollection(List.of(mockGame));          
            assertEquals(10, gameFlowPane.getHgap());
            assertEquals(10, gameFlowPane.getVgap());
            assertEquals(new Insets(20), gameFlowPane.getPadding());
        });

        WaitForAsyncUtils.waitForFxEvents();
    }

    /**
     * Tests that sorting the collection invokes the game service and reloads the sorted list.
     */
    @Test
    void sortCollection_shouldLoadUserCollection_whenSortReturnsList() {
        GameCollectionController freshController = new GameCollectionController();
        GameCollectionController spyController = spy(freshController);

        List<game> sortedGames = List.of(new game(), new game());

        FlowPane testGamePane = new FlowPane();
        TestUtils.setPrivateField(spyController, "gameFlowPane", testGamePane);
        TestUtils.setPrivateField(spyController, "gameCollectionService", mockGameService);
        TestUtils.setPrivateField(spyController, "searchBar", new TextField(""));
        TestUtils.setPrivateField(spyController, "selectedItem", "Title");

        spyController.setUserID(1);

        when(mockGameService.sortCollection(anyInt(), anyString(), anyString()))
                .thenReturn(sortedGames);

        Platform.runLater(() -> {
            spyController.sortCollection();
            verify(spyController).loadUserCollection(sortedGames);
        });

        WaitForAsyncUtils.waitForFxEvents();
    }


    /**
     * Verifies that an error is printed if sorting returns null and that the collection is not loaded.
     */
    @Test
    void sortCollection_shouldPrintError_whenSortReturnsNull() {
        GameCollectionController testController = new GameCollectionController() {
            @Override
            public void onUserDataLoad() { }
        };
        GameCollectionController spyController = spy(testController);

        FlowPane testGamePane = new FlowPane();
        TestUtils.setPrivateField(spyController, "gameFlowPane", testGamePane);
        TestUtils.setPrivateField(spyController, "gameCollectionService", mockGameService);
        TestUtils.setPrivateField(spyController, "searchBar", new TextField(""));
        TestUtils.setPrivateField(spyController, "selectedItem", "Title");
        spyController.setUserID(1);

        when(mockGameService.sortCollection(anyInt(), anyString(), anyString()))
                .thenReturn(null);

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        Platform.runLater(() -> {
            spyController.sortCollection();
            verify(spyController, never()).loadUserCollection(any());
            assertTrue(outContent.toString().contains("Error occurred while sorting collection"));
            System.setOut(originalOut);
        });

        WaitForAsyncUtils.waitForFxEvents();
    }

    /**
     * Ensures that the filtered game collection is sorted and reloaded when not empty.
     */
    @Test
    void sortFilteredCollection_shouldSortAndLoadCollection_whenFilteredCollectionIsNotEmpty() {
        GameCollectionController testController = new GameCollectionController();
        GameCollectionController spyController = spy(testController);

        List<game> mockFiltered = List.of(mock(game.class));
        List<game> mockSorted = List.of(mock(game.class));

        TestUtils.setPrivateField(spyController, "filteredGameCollection", mockFiltered);

        ChoiceBox<String> mockSortChoiceBox = new ChoiceBox<>();
        mockSortChoiceBox.setValue("Title");
        TestUtils.setPrivateField(spyController, "sortChoiceBox", mockSortChoiceBox);

        TestUtils.setPrivateField(spyController, "gameCollectionService", mockGameService);
        doNothing().when(spyController).loadUserCollection(mockSorted);
        when(mockGameService.sortFilteredCollection(eq(mockFiltered), eq("Title"))).thenReturn(mockSorted);

        boolean result = spyController.sortFilteredCollection();

        assertTrue(result, "Expected sortFilteredCollection() to return true");
        verify(mockGameService).sortFilteredCollection(mockFiltered, "Title");
        verify(spyController).loadUserCollection(mockSorted);
    }

    /**
     * Verifies that an error is printed and false is returned when the filtered collection is empty.
     */
    @Test
    void sortFilteredCollection_shouldReturnFalseAndPrintError_whenFilteredCollectionIsEmpty() {
        GameCollectionController testController = new GameCollectionController();
        GameCollectionController spyController = spy(testController);

        TestUtils.setPrivateField(spyController, "filteredGameCollection", new ArrayList<>());
        ChoiceBox<String> mockSortChoiceBox = new ChoiceBox<>();
        mockSortChoiceBox.setValue("Title");
        TestUtils.setPrivateField(spyController, "sortChoiceBox", mockSortChoiceBox);

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));
        boolean result = spyController.sortFilteredCollection();
        assertFalse(result, "Expected sortFilteredCollection() to return false");
        assertTrue(outContent.toString().contains("No games to sort."));
        System.setOut(originalOut);
    }

    /**
     * Tests the search button flow including successful search logging and performance tracking.
     */
    @Test
    void handleSearchButton_shouldSearchAndLogPerformance_whenSearchIsSuccessful() {
        GameCollectionController testController = new GameCollectionController();
        GameCollectionController spyController = spy(testController);

        TextField mockSearchBar = new TextField("Halo");
        TestUtils.setPrivateField(spyController, "searchBar", mockSearchBar);
        TestUtils.setPrivateField(spyController, "tracker", mockTracker);

        doNothing().when(spyController).searchCollection("Halo");
        when(mockTracker.getMemoryUsage()).thenReturn(100);
        when(mockTracker.getGCEvents()).thenReturn(1);
        doNothing().when(mockTracker).logPerformanceData(anyInt(), eq(-1), eq(100), eq(1), isNull());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(out));
        spyController.handleSearchButton();

        assertTrue(out.toString().contains("Searching for Halo in game colleciton controller"));
        assertTrue(out.toString().contains("Search completed in"));

        verify(spyController).searchCollection("Halo");
        verify(mockTracker).logPerformanceData(anyInt(), eq(-1), eq(100), eq(1), isNull());

        System.setOut(originalOut);
    }

    /**
     * Ensures that errors during the search process are properly caught and logged.
     */
    @Test
    void handleSearchButton_shouldPrintError_whenExceptionOccurs() {
        GameCollectionController testController = new GameCollectionController();
        GameCollectionController spyController = spy(testController);

        TextField mockSearchBar = new TextField("ErrorTest");
        TestUtils.setPrivateField(spyController, "searchBar", mockSearchBar);
        TestUtils.setPrivateField(spyController, "tracker", mockTracker);

        doThrow(new RuntimeException("Simulated failure")).when(spyController).searchCollection("ErrorTest");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(out));

        spyController.handleSearchButton();

        String output = out.toString();
        assertTrue(output.contains("Error occurred while searching for game: Simulated failure"));

        System.setOut(originalOut);
    }

    /**
     * Tests that a valid search result reloads the user collection with the found games.
     */
    @Test
    void searchCollection_shouldLoadUserCollection_whenSearchReturnsResults() {
        GameCollectionController spyController = spy(new GameCollectionController());

        List<game> mockResults = List.of(mock(game.class));

        TestUtils.setPrivateField(spyController, "gameCollectionService", mockGameService);
        TestUtils.setPrivateField(spyController, "gameFlowPane", new FlowPane()); 
        TestUtils.setPrivateField(spyController, "searchBar", new TextField());    
        TestUtils.setPrivateField(spyController, "gameCollectionScrollPane", new ScrollPane());
        spyController.setUserID(1);

        when(mockGameService.searchCollection(1, "Halo")).thenReturn(mockResults);

        Platform.runLater(() -> {
            spyController.searchCollection("Halo");

            verify(mockGameService).searchCollection(1, "Halo");
            verify(spyController).loadUserCollection(mockResults);
        });

        WaitForAsyncUtils.waitForFxEvents();  
    }

	/**
	 * Tests that an error is printed when the search returns null, indicating no results found.
	 */
    @Test
    void searchCollection_shouldPrintError_whenSearchReturnsNull() {
        GameCollectionController spyController = spy(new GameCollectionController());
        TestUtils.setPrivateField(spyController, "gameCollectionService", mockGameService);
        spyController.setUserID(1);
        when(mockGameService.searchCollection(1, "Halo")).thenReturn(null);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(out));

        spyController.searchCollection("Halo");
        String output = out.toString();
        assertTrue(output.contains("Error occurred while searching for game"));
        System.setOut(originalOut);
    }

    /**
     * Tests the creation of a game box StackPane for a valid game object.
     */
    @Test
    void testCreateGameBox_shouldReturnStackPaneWithGameDetails() {
        GameCollectionController controller = new GameCollectionController();

        game mockGame = mock(game.class);
        when(mockGame.getTitle()).thenReturn("Mock Game");
        when(mockGame.getCoverImageUrl()).thenReturn("/Images/placeholder1gameGrinding.png");
        when(mockGame.getCompletionStatus()).thenReturn("Completed");

        Platform.runLater(() -> {
        	Node node = (Node) TestUtils.invokePrivateMethod(
        		    controller,
        		    "createGameBox",
        		    new Class[]{game.class},
        		    new Object[]{mockGame},
        		    true
        		);
            assertNotNull(node, "Returned game box node should not be null.");
            assertTrue(node instanceof StackPane, "Returned node should be a StackPane.");

            StackPane stack = (StackPane) node;
            assertFalse(stack.getChildren().isEmpty(), "StackPane should contain children.");
        });

        WaitForAsyncUtils.waitForFxEvents();
    }

    /**
     * Verifies that HTTP URLs are correctly processed to load external images.
     */
    @Test
    void createGameBox_shouldLoadImageFromHttpUrl() throws Exception {
        GameCollectionController controller = new GameCollectionController();
        game mockGame = mock(game.class);
        when(mockGame.getTitle()).thenReturn("Mock Game");
        when(mockGame.getCoverImageUrl()).thenReturn("https://via.placeholder.com/150");

        Node node = (Node) TestUtils.invokePrivateMethod(
            controller,
            "createGameBox",
            new Class[]{game.class},
            new Object[]{mockGame},
            true
        );
        assertNotNull(node);
        assertTrue(node instanceof StackPane);
    }

    /**
     * Verifies that local file image paths with "file:" prefix are correctly processed.
     */
    @Test
    void createGameBox_shouldLoadImageFromLocalFilePrefix() {
        GameCollectionController controller = new GameCollectionController();
        game mockGame = mock(game.class);
        when(mockGame.getTitle()).thenReturn("Mock Game");

        String localFileUrl = new File("src/main/resources/Images/placeholder1gameGrinding.png").toURI().toString();
        when(mockGame.getCoverImageUrl()).thenReturn("file:" + localFileUrl);

        Node node = (Node) TestUtils.invokePrivateMethod(
            controller,
            "createGameBox",
            new Class[]{game.class},
            new Object[]{mockGame},
            true
        );
        assertNotNull(node);
    }

    /**
     * Tests direct file path handling for game images if the file exists.
     */
    @Test
    void createGameBox_shouldHandleDirectFilePathIfExists() {
        GameCollectionController controller = new GameCollectionController();
        game mockGame = mock(game.class);
        when(mockGame.getTitle()).thenReturn("Mock Game");
        File localFile = new File("src/test/resources/Images/test.png");
        assumeTrue(localFile.exists());
        when(mockGame.getCoverImageUrl()).thenReturn(localFile.getAbsolutePath());
        Node node = (Node) TestUtils.invokePrivateMethod(
            controller,
            "createGameBox",
            new Class[]{game.class},
            new Object[]{mockGame},
            true
        );
        assertNotNull(node);
    }
  
    /**
     * Ensures fallback behavior when an invalid image URL is provided.
     */
    @Test
    void createGameBox_shouldFallbackWhenInvalidUrl() {
        GameCollectionController controller = new GameCollectionController();
        game mockGame = mock(game.class);
        when(mockGame.getTitle()).thenReturn("Mock Game");
        when(mockGame.getCoverImageUrl()).thenReturn("invalid://url");
        Node node = (Node) TestUtils.invokePrivateMethod(
            controller,
            "createGameBox",
            new Class[]{game.class},
            new Object[]{mockGame},
            true
        );
        assertNotNull(node);  
    }

    /**
     * Verifies that fallback logic works when image URL is null or empty.
     */
    @Test
    void createGameBox_shouldFallbackWhenUrlIsNullOrEmpty() {
        GameCollectionController controller = new GameCollectionController();
        game mockGame = mock(game.class);
        when(mockGame.getTitle()).thenReturn("Mock Game");
        when(mockGame.getCoverImageUrl()).thenReturn(null);
        Node node = (Node) TestUtils.invokePrivateMethod(
            controller,
            "createGameBox",
            new Class[]{game.class},
            new Object[]{mockGame},
            true
        );
        assertNotNull(node);
    }
    
    /**
     * Tests fallback image logic when image file is missing or broken.
     */
    @Test
    void createGameBox_shouldUseFallbackPlaceholderEvenWhenMissingImage() {
        GameCollectionController controller = new GameCollectionController();
        game mockGame = mock(game.class);
        when(mockGame.getCoverImageUrl()).thenReturn("invalid://broken-path");
        when(mockGame.getTitle()).thenReturn("Fallback Game");
        Node result = (Node) TestUtils.invokePrivateMethod(
            controller,
            "createGameBox",
            new Class[]{game.class},
            new Object[]{mockGame},
            true
        );
        assertNotNull(result, "createGameBox should return a non-null Node despite missing image");
    }

    /**
     * Tests that clicking the Add Game button loads the popup window and sets the user ID correctly.
     */
    @Test
    void clickAddGame_shouldLoadPopupAndSetUserId() throws Exception {
        GameCollectionController controller = new GameCollectionController();
        controller.setUserID(99);

        Button sourceButton = new Button();
        StackPane root = new StackPane(sourceButton); 
        Scene scene = new Scene(root);
        Stage mockParentStage = new Stage();
        mockParentStage.setScene(scene);
        mockParentStage.show(); 

        javafx.event.ActionEvent mockEvent = mock(javafx.event.ActionEvent.class);
        when(mockEvent.getSource()).thenReturn(sourceButton);

        Platform.runLater(() -> {
            try {
                controller.clickAddGame(mockEvent);
                Stage popupStage = TestUtils.getPopupStage(controller); 
                assertNotNull(popupStage, "Popup stage should not be null");
                assertTrue(popupStage.isShowing(), "Popup stage should be showing");
                assertEquals("Add Game", popupStage.getTitle());
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/AddGamePopup.fxml"));
                loader.load();
                AddGamePopupController actualController = loader.getController();
            } catch (Exception e) {
                fail("Exception during test: " + e.getMessage());
            }
        });
        WaitForAsyncUtils.waitForFxEvents(); 
    }

    /**
     * Verifies that clicking the Filter button invokes the appropriate navigation method.
     */
    @Test
    void testHandleFilterButton_shouldCallNavigationHelper() {
        controller.handleFilterButton();
        verify(mockNavHelper).switchToFilterPage(1, filterButton);
    }
 
    /**
     * Ensures that the refresh button triggers reloading of the user's game collection.
     */
    @Test
    void testHandleRefreshButton_shouldReloadUserCollection() {
        Platform.runLater(() -> {
            controller.setUserID(1);
            List<game> refreshedGames = List.of(mock(game.class));
            controller.onUserDataLoad();
            reset(mockGameService);
            when(mockGameService.getUserCollection(1)).thenReturn(refreshedGames);
            controller.handleRefreshButton();
            verify(mockGameService, times(1)).getUserCollection(1);
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    /**
     * Verifies that clicking the Settings button invokes the settings page navigation.
     */
    @Test
    void testHandleSettingsButton_shouldCallNavigationHelper() {
        controller.handleSettingsButton();
        verify(mockNavHelper).switchToSettingsPage(1, settingsButton);
    }
    
    /**
     * Ensures that exceptions during settings navigation are logged properly.
     */
    @Test
    void handleSettingsButton_shouldPrintError_whenExceptionOccurs() {
        doThrow(new RuntimeException("Simulated settings failure")).when(mockNavHelper).switchToSettingsPage(anyInt(), any(Button.class));
        controller.setUserID(1);  

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            try {
                controller.handleSettingsButton();
                System.out.flush();

                String output = outContent.toString();
                assertTrue(output.contains("Error occurred while switching to settings page: Simulated settings failure"), "Expected error message to be printed");
            } finally {
                latch.countDown(); 
            } 
        });
    }

    /**
     * Tests navigation to the Help page from the Game Collection screen.
     */
    @Test
    void testHandleHelpButton_shouldCallNavigationHelper() {
        controller.handleHelpButton();
        verify(mockNavHelper).switchToHelpPage(1, helpButton);
    }
    
    /**
     * Verifies that the logout button logs out the user and switches to the login page.
     */
    @Test
    void testHandleLogoutButton_shouldLogoutAndSwitchPage() {
        controller.handleLogoutButton();
        verify(mockUserService).logout();
        verify(mockNavHelper).switchToLoginPage(logoutButton);
    }

    /**
     * Ensures that the filtered game collection is updated correctly and returns true.
     */
    @Test
    void testSetFilteredCollection_shouldUpdateAndReturnTrue() {
        List<game> mockFiltered = new ArrayList<>();
        boolean result = controller.setFilteredCollection(mockFiltered);
        assertTrue(result);
    }
}
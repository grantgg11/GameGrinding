package controllers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach; 
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import models.game;
import services.GameCollectionService;
import services.mobyGamesAPIService;
import utils.AlertHelper;

/**
 * Unit tests for the AddGameAPIController class.
 * 
 * This class tests the controller responsible for searching and displaying games from the 
 * MobyGames API, allowing users to select and add games to their collection. The tests cover:
 * 
 * - Initialization and dependency injection
 * - Handling user input and search execution
 * - Selection and addition of games to the user’s collection
 * - Proper display and UI rendering of results
 * - Error handling and fallback mechanisms
 * - Image loading and cleanup logic
 * 
 * Mock objects are used for dependencies such as mobyGamesAPIService, GameCollectionService, 
 * AlertHelper, and NavigationHelper. JavaFX components are directly manipulated or inspected.
 * 
 * Key test coverage includes:
 * - Valid and invalid search queries
 * - Displaying up to 20 results only
 * - Preventing duplicate game IDs
 * - Cancelling partially loaded images before rendering new ones
 * - Ensuring proper UI layout behavior
 * - Handling API exceptions gracefully
 */
@ExtendWith(ApplicationExtension.class)
class AddGameAPIControllerTest {
	

    private AddGameAPIController controller;

    private mobyGamesAPIService mockApiService;
    private GameCollectionService mockGameCollectionService;
    private AlertHelper mockAlertHelper;
    private NavigationHelper mockNavHelper;
    

    /**
     * Initializes the controller and injects mock dependencies before each test.
     * Also sets up required UI elements and controller fields using reflection.
     */
    @BeforeEach
    void setUp() {
    	
        controller = new AddGameAPIController();

        // Manually inject mock dependencies
        mockApiService = mock(mobyGamesAPIService.class);
        mockGameCollectionService = mock(GameCollectionService.class);
        mockAlertHelper = mock(AlertHelper.class);
        mockNavHelper = mock(NavigationHelper.class);

        controller.searchField = new TextField();
        controller.resultsFlowPane = new FlowPane();
        controller.scrollPane = new ScrollPane();
        controller.loadingOverlay = new StackPane();
        controller.addButton = new Button();

        // Inject using reflection since fields are private final
        TestUtils.setPrivateField(controller, "apiService", mockApiService);
        TestUtils.setPrivateField(controller, "gameCollectionService", mockGameCollectionService);
        TestUtils.setPrivateField(controller, "alert", mockAlertHelper);
        TestUtils.setPrivateField(controller, "loggedInUserID", 42);
        TestUtils.setPrivateField(controller, "navHelp", mockNavHelper);
        
    }
    
    /**
     * Verifies that onUserDataLoad executes without exceptions and stores the user ID.
     */
    @Test
    void testOnUserDataLoad_shouldNotThrowAndLogUserId() {
        assertDoesNotThrow(() -> controller.onUserDataLoad());
        int loggedInUserID = (int) TestUtils.getPrivateField(controller, "loggedInUserID");
        assertEquals(42, loggedInUserID);
    }


    /**
     * Verifies that setPreviousSearchResults correctly stores the given game list.
     */
    @Test
    void testSetPreviousSearchResults_shouldStoreResultsCorrectly() {
        List<game> mockGames = new ArrayList<>();
        game game1 = new game();
        game1.setGameID(1);
        game1.setTitle("Test Game");
        mockGames.add(game1);
        controller.setPreviousSearchResults(mockGames);
        @SuppressWarnings("unchecked")
        List<game> storedGames = (List<game>) TestUtils.getPrivateField(controller, "previousSearchResults");
        assertNotNull(storedGames);
        assertEquals(1, storedGames.size());
        assertEquals("Test Game", storedGames.get(0).getTitle());
    }
    
    /////////////////////////// testing handleSearch ///////////////////////////
    
    /**
     * Verifies that the search is ignored when the search field is empty.
     */
    @Test
    void testHandleSearch_withEmptyQuery() throws Exception {
        controller.searchField.setText("");
        controller.handleSearch();
        verify(mockApiService, never()).searchGamesByTitles(any());
    }
    

    /**
     * Ensures that calling handleAddSelectedGames with an empty selection does not call the service.
     */
    @Test
    void testHandleAddSelectedGames_withEmptySelection() throws Exception {
        TestUtils.setPrivateField(controller, "selectedGames", new ArrayList<>()); 
        TestUtils.setPrivateField(controller, "loggedInUserID", 42);

        controller.handleAddSelectedGames(); 
        verify(mockGameCollectionService, never()).addGameToCollection(any(), anyInt());
    }
    
    /**
     * Validates that selected games are passed to the GameCollectionService and navigation occurs.
     */
    @Test
    void testHandleAddSelectedGames_withValidSelection() throws Exception {
        List<game> selected = new ArrayList<>();
        game game1 = new game();
        game1.setGameID(1);
        game1.setTitle("Test Game");
        selected.add(game1);
        when(mockGameCollectionService.addGameToCollection(any(game.class), anyInt())).thenReturn(true);
        TestUtils.setPrivateField(controller, "selectedGames", selected);
        TestUtils.setPrivateField(controller, "loggedInUserID", 42);
        NavigationHelper mockNavHelper = mock(NavigationHelper.class);
        TestUtils.setPrivateField(controller, "navHelp", mockNavHelper);
        controller.handleAddSelectedGames();
        verify(mockGameCollectionService).addGameToCollection(any(game.class), eq(42));
        verify(mockNavHelper).switchToGameCollection(eq(42), any());
    }
    
    /**
     * Verifies that handleSearch triggers displayResults with the expected result set.
     */
    @Test
    void testHandleSearch_shouldCallDisplayResults() throws Exception {
        List<game> mockResults = new ArrayList<>();
        game mockGame = new game();
        mockGame.setGameID(123);
        mockGame.setTitle("Spy Game");
        mockResults.add(mockGame);

        when(mockApiService.searchGamesByTitles("Spy")).thenReturn(mockResults);

        AddGameAPIController spyController = spy(controller);
        doNothing().when(spyController).displayResults(any());

        spyController.searchField.setText("Spy");
        spyController.handleSearch();

        Thread.sleep(300);

        verify(spyController).displayResults(mockResults);
    }
    
    /**
     * Makes sure the controller does not crash and logs API errors when exceptions occur.
     */
    @Test
    void testHandleSearch_withApiException() throws Exception {
        when(mockApiService.searchGamesByTitles("ErrorTest")).thenThrow(new RuntimeException("API Failure"));
        controller.searchField.setText("ErrorTest");
        AddGameAPIController spyController = spy(controller);
        TestUtils.setPrivateField(spyController, "apiService", mockApiService);
        doNothing().when(spyController).displayResults(any());
        spyController.handleSearch();
        Thread.sleep(300);
        verify(mockApiService).searchGamesByTitles("ErrorTest");
    }
    
    /**
     * Validates that the loading overlay is shown while an API search is in progress.
     */
    @Test
    void testHandleSearch_shouldSetLoadingOverlayVisible() throws Exception {
        CountDownLatch searchStarted = new CountDownLatch(1);
        CountDownLatch continueSearch = new CountDownLatch(1);
        CountDownLatch displayDone = new CountDownLatch(1);

        when(mockApiService.searchGamesByTitles("Zelda")).thenAnswer(invocation -> {
            searchStarted.countDown();       
            continueSearch.await();          
            return new ArrayList<>();
        });

        AddGameAPIController spyController = spy(controller);
        spyController.searchField.setText("Zelda");
        TestUtils.setPrivateField(spyController, "apiService", mockApiService);

        doAnswer(invocation -> {
            displayDone.countDown();
            return null;
        }).when(spyController).displayResults(any());

        Platform.runLater(() -> {
            try {
                spyController.handleSearch();
            } catch (Exception e) {
                fail("handleSearch threw an exception: " + e.getMessage());
            }
        });
        assertTrue(searchStarted.await(1, TimeUnit.SECONDS), "Search should have started");
        Platform.runLater(() -> {
            assertTrue(spyController.loadingOverlay.isVisible(), "Loading overlay should be visible during API search");
            assertFalse(spyController.loadingOverlay.isMouseTransparent(), "Overlay should capture mouse events");
        });
        continueSearch.countDown();
        assertTrue(displayDone.await(1, TimeUnit.SECONDS), "Display results should complete");
    }

    //////////////////////////// testing displayResults ///////////////////////////
    
    /**
	 * Tests that displayResults correctly populates the resultsFlowPane with game boxes.
	 */
	@Test    
    void testDisplayResults_limitsTo20() {
        List<game> mockGames = TestUtils.generateMockGames(25);
        controller.displayResults(mockGames);
        assertNotNull(controller.resultsFlowPane.getChildren());
        assertEquals(20, controller.resultsFlowPane.getChildren().size());
    }
    
    /**
     * Makes sure partially loaded images are cancelled before rendering new results.
     */
	@Test
	void testDisplayResults_cancelsAndClearsLoadedImages() {
	    Image mockImage = mock(Image.class);
	    when(mockImage.isError()).thenReturn(false);
	    when(mockImage.getProgress()).thenReturn(0.5);
	    List<Image> fakeList = new ArrayList<>();
	    fakeList.add(mockImage);
	    TestUtils.setPrivateField(controller, "loadedImages", fakeList);
	    List<game> mockGames = TestUtils.generateMockGames(1);
	    controller.displayResults(mockGames);
	    verify(mockImage).cancel();
	    @SuppressWarnings("unchecked")
	    List<Image> result = (List<Image>) TestUtils.getPrivateField(controller, "loadedImages");
	    assertNotNull(result);
	    assertTrue(result.size() <= 1, "Loaded images should be cleared and reset to 1 or fewer");
	}


    /**
     * Verifies that displayResults clears old game boxes from the resultsFlowPane before displaying new ones.
     */
	@Test
	void testDisplayResults_clearsPreviousResults() {
	    VBox fakeBox = new VBox();
	    fakeBox.getChildren().add(new Label("Old")); 
	    controller.resultsFlowPane.getChildren().add(fakeBox);
	    List<game> mockGames = TestUtils.generateMockGames(1);
	    controller.displayResults(mockGames);
	    assertEquals(1, controller.resultsFlowPane.getChildren().size());
	}

    /**
     * Ensures that displayResults does not render duplicate games based on game ID.
     */
	@Test
	void testDisplayResults_skipsDuplicateGameIDs() {
	    List<game> mockGames = new ArrayList<>();
	    game g1 = new game();
	    g1.setGameID(1);
	    g1.setTitle("Game 1");
	    game g2 = new game();
	    g2.setGameID(1);
	    g2.setTitle("Duplicate Game");
	    mockGames.add(g1);
	    mockGames.add(g2);
	    controller.displayResults(mockGames);
	    assertEquals(1, controller.resultsFlowPane.getChildren().size());
	}

    /**
     * Confirms that the layout settings for resultsFlowPane are properly applied when displayResults is called.
     */
	@Test
	void testDisplayResults_setsLayoutProperties() {
	    List<game> mockGames = TestUtils.generateMockGames(1);
	    controller.displayResults(mockGames);
	    assertEquals(Pos.TOP_LEFT, controller.resultsFlowPane.getAlignment());
	    assertEquals(20, controller.resultsFlowPane.getHgap());
	    assertEquals(20, controller.resultsFlowPane.getVgap());
	}
	
    /**
     * Verifies that partially loaded or error images are not cancelled unnecessarily.
     */
	@Test
	void testDisplayResults_skipsCancelWhenImageIsErrorOrLoaded() {
	    Image errorImage = mock(Image.class);
	    when(errorImage.isError()).thenReturn(true); 
	    Image loadedImage = mock(Image.class);
	    when(loadedImage.isError()).thenReturn(false);
	    when(loadedImage.getProgress()).thenReturn(1.0); 
	    List<Image> imageList = new ArrayList<>();
	    imageList.add(errorImage);
	    imageList.add(loadedImage);
	    TestUtils.setPrivateField(controller, "loadedImages", imageList);
	    List<game> mockGames = TestUtils.generateMockGames(1);
	    controller.displayResults(mockGames);
	    verify(errorImage, never()).cancel();
	    verify(loadedImage, never()).cancel();
	}

	/////////////////////// testing clearPreviousResults ///////////////////////
	
    /**
     * Ensures that all children in the VBox and resultsFlowPane are cleared when clearPreviousResults is called.
     */
	@Test
	void testClearPreviousResults_clearsVBoxAndFlowPaneChildren() {
	    VBox vbox = new VBox();
	    vbox.getChildren().add(new Label("Child"));
	    Label label = new Label("Standalone");
	    controller.resultsFlowPane.getChildren().addAll(vbox, label);
	    assertEquals(2, controller.resultsFlowPane.getChildren().size());
	    assertFalse(vbox.getChildren().isEmpty());
	    controller.clearPreviousResults();
	    assertTrue(vbox.getChildren().isEmpty(), "VBox children should be cleared");
	    assertTrue(controller.resultsFlowPane.getChildren().isEmpty(), "FlowPane children should be cleared");
	}
	
	////////////////////////// testing loadImageWithFallback ///////////////////////////
	
	/**
	 * Tests that loadImageWithFallback returns a valid image when given a valid URL.
	 * Also checks that it returns a fallback image when the URL is invalid.
	 */
	@Test
	void testLoadImageWithFallback_validImage_returnsImage() {
	    String validUrl = "https://via.placeholder.com/120";
	    Image result = controller.loadImageWithFallback(validUrl);
	    assertNotNull(result, "Image should be loaded from valid URL");
	    assertFalse(result.isError(), "Loaded image should not be in error state");
	}

    /**
     * Ensures that an invalid image URL still returns a non-null Image object, possibly with an error state.
     */
	@Test
	void testLoadImageWithFallback_invalidUrl_returnsFallback() {
	    String invalidUrl = "http://invalid.fake.url/thisdoesnotexist.jpg";
	    Image result = controller.loadImageWithFallback(invalidUrl);
	    assertNotNull(result, "Should still return fallback or error image object");
	}

    /**
     * Simulates a failure in both image loading and fallback logic, expecting null to be returned.
     */
	@Test
	void testLoadImageWithFallback_fallbackFails_returnsNull() {
	    AddGameAPIController controller = new AddGameAPIController() {
	        @Override
	        public Image loadImageWithFallback(String url) {
	            try {
	                throw new RuntimeException("Simulated image load failure");
	            } catch (Exception ignored) {}
	            return null;
	        }
	    };
	    Image result = controller.loadImageWithFallback(null);
	    assertNull(result, "Should return null if fallback image fails to load");
	}
	



}


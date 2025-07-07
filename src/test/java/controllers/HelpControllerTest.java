package controllers;

import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.application.Platform;

import org.junit.jupiter.api.BeforeEach; 
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;

import services.userService;
import utils.AlertHelper;

/**
 * Unit tests for the HelpController class in the GameGrinding application.
 *
 * This test class validates the HelpController's functionality, including navigation,
 * accordion behavior, and user interaction handling.
 */
@ExtendWith({ApplicationExtension.class, JavaFXThreadingExtension.class})
class HelpControllerTest {

    private HelpController controller;
    private NavigationHelper mockNavHelper;
    private userService mockUserService;
    private AlertHelper mockAlert;
    private Button gameCollectionButton;
    private Button settingsButton;
    private Button refreshButton;
    private Button logoutButton;
    private Accordion helpAccordion;

    /**
     * Sets up the test environment before each test.
     * Mocks dependencies and injects them into the controller using reflection.
     * Initializes UI components required for testing HelpController behavior.
     */
    @BeforeEach
    void setUp() {
        controller = new HelpController();
        mockNavHelper = mock(NavigationHelper.class);
        mockUserService = mock(userService.class);
        mockAlert = mock(AlertHelper.class);

        TestUtils.setPrivateField(controller, "navHelper", mockNavHelper);
        TestUtils.setPrivateField(controller, "userSer", mockUserService);
        TestUtils.setPrivateField(controller, "alert", mockAlert);

        gameCollectionButton = new Button();
        settingsButton = new Button();
        refreshButton = new Button();
        logoutButton = new Button();
        helpAccordion = new Accordion();

        TestUtils.setPrivateField(controller, "gameCollectionButton", gameCollectionButton);
        TestUtils.setPrivateField(controller, "settingsButton", settingsButton);
        TestUtils.setPrivateField(controller, "refreshButton", refreshButton);
        TestUtils.setPrivateField(controller, "logoutButton", logoutButton);
        TestUtils.setPrivateField(controller, "helpAccordion", helpAccordion);

    }
    
    /**
     * Verifies that onUserDataLoad logs the correct user ID to System.out.
     */
    @Test
    void testOnUserDataLoad_shouldPrintUserId() {
        HelpController controller = new HelpController();
        controller.loggedInUserID = 42;
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));
        try {
            controller.onUserDataLoad();
            String output = outContent.toString().trim();
            assertTrue(output.contains("HelpController initialized with User ID: 42"), "Expected log output to include user ID.");
        } finally {
            System.setOut(originalOut);
        }
    }

    /**
     * Tests that the initialize method correctly adds help topics to the Accordion, and ensures the first help topic is expanded by default.
     */
    @Test
    void testInitialize_shouldAddHelpTopicsToAccordion() {
        Platform.runLater(() -> {
            controller.initialize();
            assertFalse(helpAccordion.getPanes().isEmpty(), "Accordion should have help topics loaded.");
            assertTrue(helpAccordion.getPanes().get(0).isExpanded(), "First help topic should be expanded.");
        });
    }
    
    /**
     * Ensures applyAccordionDefaults() sets the expanded pane to null when the Accordion has no panes.
     */
    @Test
    void testApplyAccordionDefaults_whenEmpty_shouldSetExpandedPaneToNull() {
        Accordion accordion = new Accordion();
        HelpController controller = new HelpController();
        TestUtils.setPrivateField(controller, "helpAccordion", accordion);
        Platform.runLater(() -> {
            controller.applyAccordionDefaults();
            assertNull(accordion.getExpandedPane(), "Expected no pane to be expanded when accordion is empty.");
        });
    }

    /**
	 * Tests that applyAccordionDefaults() expands the first pane when the Accordion has panes.
	 * This ensures that the first help topic is visible by default.
	 */
    @Test
    void testApplyAccordionDefaults_whenNotEmpty_shouldExpandFirstPane() {
        Accordion accordion = new Accordion();
        TitledPane p1 = new TitledPane("Help1", new VBox());
        TitledPane p2 = new TitledPane("Help2", new VBox());
        accordion.getPanes().addAll(p1, p2);

        HelpController controller = new HelpController();
        TestUtils.setPrivateField(controller, "helpAccordion", accordion);

        Platform.runLater(() -> {
            controller.applyAccordionDefaults();
            assertTrue(p1.isExpanded(), "First pane should be expanded when panes are available.");
        });
    }

    /**
	 * Tests that the initialize method applies the correct stylesheet to the Accordion.
	 * This ensures that the UI styling is applied without errors.
	 */
    @Test
    void testInitialize_shouldAddStylesheetWithoutError() {
        HelpController controller = new HelpController();
        Accordion accordion = new Accordion();

        StackPane root = new StackPane(accordion);
        Scene scene = new Scene(root);
        Stage stage = new Stage();

        Platform.runLater(() -> {
            stage.setScene(scene);
            stage.show();
        });
        TestUtils.setPrivateField(controller, "helpAccordion", accordion);
        assertDoesNotThrow(() -> {
            Platform.runLater(controller::initialize);
        }, "Applying stylesheet should not throw an error.");
    }

    /**
	 * Tests that the handleGameCollectionButton method calls the navigation helper to switch to the game collection view.
	 * Also verifies that it handles exceptions correctly by logging an error message.
	 */
    @Test
    void testHandleGameCollectionButton_shouldCallNavigationHelper() {
        controller.loggedInUserID = 101;
        controller.handleGameCollectionButton();
        verify(mockNavHelper).switchToGameCollection(eq(101), eq(gameCollectionButton));
    }
    
    /**
	 * Tests that the handleGameCollectionButton method handles exceptions correctly by logging an error message.
	 * This ensures that any navigation errors are captured and reported to the user.
	 */
    @Test
    void testHandleGameCollectionButton_whenException_shouldLogError() {
        controller.loggedInUserID = 101;
        doThrow(new RuntimeException("Navigation failed"))
            .when(mockNavHelper)
            .switchToGameCollection(eq(101), eq(gameCollectionButton));
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(errContent));
        try {
            controller.handleGameCollectionButton();
            String output = errContent.toString().trim();
            assertTrue(output.contains("Error navigating to Game Collection: Navigation failed"), "Expected System.err to contain the exception message.");
        } finally {
            System.setErr(originalErr);
        }
    }

    /**
	 * Tests that the handleSettingsButton method calls the navigation helper to switch to the settings view.
	 * Also verifies that it handles exceptions correctly by logging an error message.
	 */
    @Test
    void testHandleSettingsButton_shouldCallNavigationHelper() {
    	controller.loggedInUserID = 101;
    	controller.handleSettingsButton();
    	verify(mockNavHelper).switchToSettingsPage(eq(101), eq(settingsButton));
    }
    
    /**
     * Tests that the handleSettingsButton method handles exceptions correctly by logging an error message.
     * This ensures that any navigation errors are captured and reported to the user.
     */
    @Test
    void testHandleSettingsButton_whenException_shouldLogError() {
    	controller.loggedInUserID = 101;
    	doThrow(new RuntimeException("Settings navigation failed")).when(mockNavHelper).switchToSettingsPage(eq(101), eq(settingsButton));

    	ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    	PrintStream originalErr = System.err;
    	System.setErr(new PrintStream(errContent));
    	try {
    		controller.handleSettingsButton();
    		String output = errContent.toString().trim();
    		assertTrue(output.contains("Error navigating to Settings: Settings navigation failed"), "Expected error message in System.err");
    	} finally {
    		System.setErr(originalErr); 
    	}
    }

    /**
	 * Tests that the handleRefreshButton method calls the navigation helper to refresh the help page.
	 * Also verifies that it handles exceptions correctly by logging an error message.
	 */
    @Test
    void testHandleRefreshButton_shouldCallNavigationHelper() {
        controller.loggedInUserID = 101;
        controller.handleRefreshButton();
        verify(mockNavHelper).switchToHelpPage(eq(101), eq(refreshButton));
    }
    /**
	 * Tests that the handleRefreshButton method handles exceptions correctly by logging an error message.
	 * This ensures that any navigation errors are captured and reported to the user.
	 */
    @Test
    void testHandleRefreshButton_whenException_shouldLogError() {
        controller.loggedInUserID = 101;
        doThrow(new RuntimeException("Simulated refresh failure")).when(mockNavHelper).switchToHelpPage(eq(101), eq(refreshButton));

        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(errContent));

        try {
            controller.handleRefreshButton();
            String output = errContent.toString().trim();
            assertTrue(output.contains("Error refreshing the view: Simulated refresh failure"),"Expected System.err to contain the exception message.");
        } finally {
            System.setErr(originalErr);
        }
    }

    /**
	 * Tests that the handleLogoutButton method calls the user service to log out and navigates to the login page.
	 * Also verifies that it shows a success message upon successful logout.
	 */
    @Test
    void testHandleLogoutButton_shouldLogoutAndNavigate() {
        controller.handleLogoutButton();

        verify(mockUserService).logout();
        verify(mockNavHelper).switchToLoginPage(logoutButton);
        verify(mockAlert).showInfo(
            eq("Logout Successful"),
            eq("You have been logged out successfully."),
            eq("Thank you for using GameGrinding")
        );
    }

    /**
     * Tests that the handleLogoutButton method handles exceptions correctly by showing an error message.
     * This ensures that any logout errors are captured and reported to the user.
     */
    @Test
    void testHandleLogoutButton_whenException_shouldShowError() {
        doThrow(new RuntimeException("Simulated error")).when(mockUserService).logout();
        controller.handleLogoutButton();
        verify(mockAlert).showError(
            eq("Logout Error"),
            eq("An error occurred while logging out. Please try again."),
            eq("Simulated error")
        );
    }
}

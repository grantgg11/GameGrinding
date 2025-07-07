package nonfunctional.UsabilityAndAccessibility;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import controllers.GameCollectionController;
import controllers.TestUtils;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

/**
 * Non-functional UI test class for validating:
 * 
 * • US-10 – The interface shall be designed to allow users to complete primary actions within 3 clicks of the main screen.
 * • US-11 – Key features should be accessible within 2-3 steps.
 *
 * These tests simulate user interaction using TestFX to make sure that major features such as
 * adding games, editing game details, navigating to Help and Settings, and logging out
 * are accessible in 2 to 3 steps/clicks from the main Game Collection screen.
 */

@ExtendWith(ApplicationExtension.class)
public class PrimaryActionClickPathTest {

    /**
     * Initializes the Game Collection screen and preloads a test game collection for use in UI tests.
     * This method is executed before each test.
     */
    @Start
    private void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/GameCollection.fxml"));
        Parent root = loader.load();
        GameCollectionController controller = loader.getController();
        if (controller == null) {
            throw new IllegalStateException("GameCollectionController could not be initialized from FXML.");
        }

        controller.setUserID(1);
        TestUtils.preloadGameCollection(controller);

        Platform.runLater(() -> {
            primaryStage.setScene(new Scene(root));
            primaryStage.setTitle("Test Game Collection");
            primaryStage.show();
        });

        WaitForAsyncUtils.waitForFxEvents();
    }

    /**
     * Validates that users can reach the Add Game (API Search) screen within 3 clicks.
     */
    @Test
    void testAddGameAction_withinThreeClicks(FxRobot robot) {
        robot.clickOn("#addGameButton");       // 1st click
        robot.clickOn("#apiButton");           // 2nd click
        boolean reached = robot.lookup("#searchBar").tryQuery().isPresent(); // 3rd interaction
        assertTrue(reached, "Should reach Add Game search view within 2-3 clicks");
    }

    /**
     * Validates that users can access the Manual Add Game screen within 3 clicks.
     */ 
    @Test
    void testManualAddGame_withinThreeClicks(FxRobot robot) {
        robot.clickOn("#addGameButton");       // 1st click
        robot.clickOn("#manualButton");        // 2nd click
        WaitForAsyncUtils.waitForFxEvents();
        boolean reached = robot.lookup("#titleField").tryQuery().isPresent(); // 3rd interaction
        assertTrue(reached, "Should reach Manual Add Game screen within 2-3 clicks.");
    }

    /**
     * Validates that users can view game details from the main screen within 3 clicks.
     */
    @Test
    void testViewGameDetails_withinThreeClicks(FxRobot robot) {
        robot.clickOn("#gameFlowPane");        // 1st click (FlowPane container)
        robot.clickOn(".gameBox");             // 2nd click (game card)
        boolean reached = robot.lookup("#gameTitle").tryQuery().isPresent(); // detail view label
        assertTrue(reached, "Should reach Game Details screen within 2-3 clicks");
    }

    /**
     * Validates that users can access the Edit Game screen from the Game Details screen within 3 clicks.
     */
    @Test
    void testEditGame_withinThreeClicks(FxRobot robot) {
        robot.clickOn("#gameFlowPane");        // 1st click
        robot.clickOn(".gameBox");             // 2nd click
        robot.clickOn("#editButton");          // 3rd click
        boolean reached = robot.lookup("#titleField").tryQuery().isPresent(); // Edit Game screen field
        assertTrue(reached, "Should reach Edit Game screen within 3 clicks");
    }

    /**
     * Validates that users can access and interact with the Settings page and view encryption info within 3 clicks.
     */
    @Test
    void testSettingsPageAccordionClick_withinThreeClicks(FxRobot robot) {
        robot.clickOn("#settingsButton");      // 1st click
        WaitForAsyncUtils.waitForFxEvents();
        boolean clicked = robot.lookup(".label").queryAll().stream()
            .filter(node -> node instanceof Label && ((Label) node).getText().contains("How We Protect Your Data"))
            .findFirst()
            .map(label -> {
                robot.clickOn(label);          // 2nd click
                return true;
            }).orElse(false);

        assertTrue(clicked, "Should reach and interact with encryption info within 2-3 clicks");
    }

    /**
     * Validates that users can reach the Help page and expand a specific help topic within 3 clicks.
     */
    @Test
    void testHelpPageAccordionClick_withinThreeClicks(FxRobot robot) {
        robot.clickOn("#helpButton");          // 1st click
        WaitForAsyncUtils.waitForFxEvents();
        boolean clicked = robot.lookup(".titled-pane").queryAll().stream()
            .filter(node -> node instanceof TitledPane &&
                    ((TitledPane) node).getText().contains("How to Add a Game Manually"))
            .findFirst()
            .map(tp -> {
                robot.clickOn(tp);             // 2nd click
                return true;
            }).orElse(false);

        assertTrue(clicked, "Should navigate to Help and expand a topic within 2-3 clicks");
    }

    /**
     * Validates that users can logout and reach the Forgot Password screen within 3 clicks.
     */
    @Test
    void testLogoutAndClickForgotPassword_withinThreeClicks(FxRobot robot) {
        robot.clickOn("#logoutButton");        // 1st click
        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("#forgotPasswordButton"); // 2nd click
        boolean reached = robot.lookup("#emailField").tryQuery().isPresent(); // 3rd interaction
        assertTrue(reached, "Should reach Forgot Password screen within 2-3 clicks from logout.");
    }

    /**
     * Validates that users can logout and reach the Create Account screen within 3 clicks.
     */
    @Test
    void testLogoutAndClickCreateAccoung_withinThreeClicks(FxRobot robot) {
        robot.clickOn("#logoutButton");        // 1st click
        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("#createAccountButton"); // 2nd click
        boolean reached = robot.lookup("#emailField").tryQuery().isPresent(); // 3rd interaction
        assertTrue(reached, "Should reach Create Account screen within 2-3 clicks from logout.");
    }
}
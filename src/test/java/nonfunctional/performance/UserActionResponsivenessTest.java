package nonfunctional.performance;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils; 

import controllers.GameCollectionController;
import controllers.TestUtils;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * Non-functional UI Test for User Story US-2.
 * 
 * This test class verifies that key user interactions in the GameGrinding application
 * respond within 0.5 seconds, ensuring a responsive user experience. It uses manual polling
 * to confirm the presence of expected UI elements triggered by user actions.
 */
@ExtendWith(ApplicationExtension.class)
public class UserActionResponsivenessTest {

    /**
     * Initializes the JavaFX application with the Game Collection screen as the starting point.
     * This method runs once before any test method.
     *
     * @param stage the primary test stage
     * @throws Exception if the FXML or controller fails to load
     */
    @Start
    private void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/GameCollection.fxml"));
        Parent root = loader.load();
        GameCollectionController controller = loader.getController();
        controller.setUserID(1);
        TestUtils.preloadGameCollection(controller);

        Platform.runLater(() -> {
            stage.setScene(new Scene(root));
            stage.setTitle("User Action Responsiveness Test");
            stage.show();
        });

        WaitForAsyncUtils.waitForFxEvents();
    }
    
    /**
	 * Ensures a clean state before all tests by closing any open windows.
	 * This prevents interference from other test classes that may have left windows open.
	 */
    @BeforeAll
    static void beforeAllUIReset() {
        Platform.runLater(() -> {
            var snapshot = new ArrayList<>(Window.getWindows());
            for (Window w : snapshot) {
                if (w != null && w.isShowing()) {
                    if (w instanceof Stage s) s.close();
                    else w.hide();
                }
            }
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    /**
     * Verifies that clicking the Settings button results in feedback within 0.5 seconds.
     *
     * @param robot the TestFX robot used to simulate UI interactions
     */
    @Test
    void testSettingsButtonRespondsQuickly(FxRobot robot) {
        long startTime = System.nanoTime();

        robot.clickOn("#settingsButton");
        boolean responded = waitForNode(robot, "#usernameField", 500);

        long durationMs = (System.nanoTime() - startTime) / 1_000_000;
        assertTrue(responded, "Settings screen did not respond visually within 0.5 seconds");
        System.out.println("Settings button response time: " + durationMs + " ms");
    }

    /**
     * Verifies that clicking the Help button results in feedback within 0.5 seconds.
     *
     * @param robot the TestFX robot used to simulate UI interactions
     */
    @Test
    void testHelpButtonRespondsQuickly(FxRobot robot) {
        long startTime = System.nanoTime();

        robot.clickOn("#helpButton");
        boolean responded = waitForNode(robot, ".titled-pane", 500);

        long durationMs = (System.nanoTime() - startTime) / 1_000_000;
        assertTrue(responded, "Help screen did not respond visually within 0.5 seconds");
        System.out.println("Help button response time: " + durationMs + " ms");
    }

    /**
     * Verifies that clicking the Add Game button results in feedback within 0.5 seconds.
     *
     * @param robot the TestFX robot used to simulate UI interactions
     */
    @Test
    void testAddGameButtonRespondsQuickly(FxRobot robot) {
        long startTime = System.nanoTime();

        robot.clickOn("#addGameButton");
        boolean responded = waitForNode(robot, "#apiButton", 500);

        long durationMs = (System.nanoTime() - startTime) / 1_000_000;
        assertTrue(responded, "Add Game popup did not respond visually within 0.5 seconds");
        System.out.println("Add Game button response time: " + durationMs + " ms");
    }

    /**
     * Helper method that polls for the presence of a UI element within a timeout.
     *
     * @param robot     the TestFX robot instance
     * @param query     the CSS selector or fx:id of the UI element
     * @param timeoutMs the time limit in milliseconds to wait
     * @return true if the element appears within the timeout; false otherwise
     */
    private boolean waitForNode(FxRobot robot, String query, long timeoutMs) {
        long start = System.nanoTime();
        long pollIntervalMs = 50;
        long elapsed = 0;

        while (elapsed < timeoutMs) {
            if (robot.lookup(query).tryQuery().isPresent()) {
                return true;
            }
            try {
                Thread.sleep(pollIntervalMs);
            } catch (InterruptedException ignored) {}
            elapsed = (System.nanoTime() - start) / 1_000_000;
        }

        return false;
    }
    
    /**
	 * Resets the UI state after each test by closing dialogs and windows,
	 * and releasing any stuck input events to ensure a clean slate for the next test.
	 *
	 * @param robot the TestFX robot used to simulate UI interactions
	 */
    @AfterEach
    void resetUIAfterEach(FxRobot robot) {
        Set<Node> buttons;
        try {
            buttons = robot.lookup(".dialog-pane .button").queryAll();
        } catch (Exception e) {
            buttons = Collections.emptySet();
        }
        for (Node btn : buttons) {
            try { robot.clickOn(btn); } catch (Exception ignored) {}
        }
        WaitForAsyncUtils.waitForFxEvents();
        Platform.runLater(() -> {
            var snapshot = new ArrayList<>(Window.getWindows());
            for (Window w : snapshot) {
                if (w != null && w.isShowing()) {
                    if (w instanceof Stage s) s.close();
                    else w.hide();
                }
            }
        });
        WaitForAsyncUtils.waitForFxEvents();
        robot.release(new KeyCode[] {});
        robot.release(new MouseButton[] {});
    }

}

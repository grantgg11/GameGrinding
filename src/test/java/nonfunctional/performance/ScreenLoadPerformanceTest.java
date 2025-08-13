package nonfunctional.performance;

import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;

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
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * ScreenLoadPerformanceTest validates non-functional requirement US-1: key UI screens
 * must load within 2 seconds under typical conditions.
 *
 * Using TestFX with assertTimeoutPreemptively, it measures load times for:
 * - Add Game (API) and Manual Add Game (with per-step timing logs)
 * - Filter Collection, Game Details, Settings, and Help screens
 *
 * The suite bootstraps GameCollection via @Start, preloads sample data, waits for FX events,
 * and asserts both reachability of key nodes and sub-2s load targets.
 */
@ExtendWith(ApplicationExtension.class)
public class ScreenLoadPerformanceTest {

	/**
	 * Initializes the JavaFX application and loads the GameCollection view.
	 * This method is called before any tests are run to set up the initial state.
	 * 
	 * @param primaryStage The primary stage for the JavaFX application.
	 * @throws Exception If there is an error loading the FXML or initializing the controller.
	 */
    @Start
    private void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/GameCollection.fxml"));
        Parent root = loader.load();
        GameCollectionController controller = loader.getController();
        controller.setUserID(1);
        TestUtils.preloadGameCollection(controller);

        Platform.runLater(() -> {
            primaryStage.setScene(new Scene(root));
            primaryStage.setTitle("GameGrinding Performance Test");
            primaryStage.show();
        });

        WaitForAsyncUtils.waitForFxEvents();
    }
    
    /**
     * Tests that the Add Game (API-based) screen loads within 2 seconds.
     */
    @Test
    void testAddGameAPILoadsUnder2Seconds(FxRobot robot) {
        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
            robot.clickOn("#addGameButton");
            robot.clickOn("#apiButton");
            WaitForAsyncUtils.waitForFxEvents();
        }, "Add Game API screen should load within 2 seconds");
    }

    /**
     * Tests that the Manual Add Game screen loads in under 2 seconds.
     * Also prints individual step durations for troubleshooting slowness.
     */
    @Test
    void testManualAddGameLoadsUnder2Seconds(FxRobot robot) {
        long totalStart = System.nanoTime();

        long step1Start = System.nanoTime();
        robot.clickOn("#addGameButton");
        long step1End = System.nanoTime();
        System.out.println("Click on Add Game Button took: " + ((step1End - step1Start) / 1_000_000) + " ms");

        long step2Start = System.nanoTime();
        robot.clickOn("#manualButton");
        long step2End = System.nanoTime();
        System.out.println("Click on Manual Button took: " + ((step2End - step2Start) / 1_000_000) + " ms");

        long waitStart = System.nanoTime();
        WaitForAsyncUtils.waitForFxEvents();
        long waitEnd = System.nanoTime();
        System.out.println("Waiting for FX events took: " + ((waitEnd - waitStart) / 1_000_000) + " ms");

        long lookupStart = System.nanoTime();
        boolean reached = robot.lookup("#titleField").tryQuery().isPresent();
        long lookupEnd = System.nanoTime();
        System.out.println("Lookup took: " + ((lookupEnd - lookupStart) / 1_000_000) + " ms");

        long totalEnd = System.nanoTime();
        long totalDuration = (totalEnd - totalStart) / 1_000_000;
        System.out.println("Total time to load Manual Add Game screen: " + totalDuration + " ms");

        assertTrue(reached, "Manual Add Game screen did not load properly.");
        assertTrue(totalDuration < 2000, "Manual Add Game screen should load in under 2 seconds but took " + totalDuration + " ms");
    }

    /**
     * Tests that the Filter Collection screen loads within 2 seconds.
     */
    @Test
    void testFilterScreenLoadsUnder2Seconds(FxRobot robot) {
        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
            robot.clickOn("#filterButton");
            WaitForAsyncUtils.waitForFxEvents();
        }, "Filter screen should load within 2 seconds");
    }

    /**
     * Tests that the Game Details screen (after selecting a game) loads within 2 seconds.
     */
    @Test
    void testGameDetailsLoadUnder2Seconds(FxRobot robot) {
        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
            robot.clickOn(".gameBox");
            WaitForAsyncUtils.waitForFxEvents();
        }, "Game Details screen should load within 2 seconds");
    }

    /**
     * Tests that the Settings screen loads within 2 seconds.
     */
    @Test
    void testSettingsLoadsUnder2Seconds(FxRobot robot) {
        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
            robot.clickOn("#settingsButton");
            WaitForAsyncUtils.waitForFxEvents();
        }, "Settings screen should load within 2 seconds");
    }

    /**
     * Tests that the Help screen loads within 2 seconds.
     */
    @Test
    void testHelpLoadsUnder2Seconds(FxRobot robot) {
        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
            robot.clickOn("#helpButton");
            WaitForAsyncUtils.waitForFxEvents();
        }, "Help screen should load within 2 seconds");
    }
}
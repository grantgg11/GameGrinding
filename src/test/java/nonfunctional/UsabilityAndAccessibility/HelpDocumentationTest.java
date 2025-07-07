package nonfunctional.UsabilityAndAccessibility;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

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
import javafx.scene.control.TitledPane;
import javafx.stage.Stage;

/**
 * Non-functional UI test (US-12): 
 * Validates that basic user documentation is accessible from the main menu and 
 * includes all primary features, with instructions completable within ~3 minutes of reading.
 */
@ExtendWith(ApplicationExtension.class)
public class HelpDocumentationTest {

    /**
     * JavaFX TestFX @Start method:
     * Sets up the primary stage for testing by loading the GameCollection.fxml and initializing a test user collection.
     * This represents the main application entry point.
     *
     * @param primaryStage the JavaFX stage to initialize
     * @throws Exception if FXML loading fails
     */
    @Start
    private void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/GameCollection.fxml"));
        Parent root = loader.load();

        GameCollectionController controller = loader.getController();
        if (controller == null) {
            throw new IllegalStateException("GameCollectionController could not be initialized.");
        }

        controller.setUserID(1);
        TestUtils.preloadGameCollection(controller);

        Platform.runLater(() -> {
            primaryStage.setScene(new Scene(root));
            primaryStage.setTitle("GameGrinding - Test");
            primaryStage.show();
        });

        WaitForAsyncUtils.waitForFxEvents();
    }

    /**
     * Test method to validate that the Help page contains all required user documentation topics.
     */
    @Test
    void testHelpDocumentationIncludesAllPrimaryTopics(FxRobot robot) {
        robot.clickOn("#helpButton");
        WaitForAsyncUtils.waitForFxEvents();
        List<String> expectedTopics = List.of(
            "How to Add a Game Manually",
            "How Add Game from Database",
            "How to Edit or Delete a Game",
            "How to Filter Your Collection",
            "How to Search for a Game",
            "How to Sort Your Collection",
            "How to Update Completion Status",
            "How to View Game Details",
            "How to Add Notes to a Game",
            "How to Change your Username or Email",
            "How to Change Your Password",
            "How to view Data Encryption Information",
            "How to Logout"
        );
        boolean allTopicsPresent = expectedTopics.stream().allMatch(expected ->
            robot.lookup(".titled-pane").queryAll().stream()
                .anyMatch(tp -> ((TitledPane) tp).getText().contains(expected))
        );

        assertTrue(allTopicsPresent, "All expected help topics should be present in the Help page.");
    }
}
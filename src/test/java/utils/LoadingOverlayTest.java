package utils;


import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.stage.Stage;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test; 

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the LoadingOverlay utility class.
 *
 * This class ensures that the overlay is properly created and displayed using JavaFX stages.
 * It initializes the JavaFX runtime and verifies that the overlay window behaves as expected.
 */

public class LoadingOverlayTest {

	/**
	 * Initializes the JavaFX runtime environment required to run JavaFX components in headless test mode.
	 * This setup is necessary before any JavaFX-dependent tests are executed.
	 */
    @BeforeAll
    static void initJFX() {
        new JFXPanel();
    }

    /**
     * Tests that the show method of LoadingOverlay returns a non-null Stage and ensures
     * the overlay is visible on screen after invocation.
     *
     * @throws Exception if the JavaFX thread sleep or execution fails
     */
    @Test
    void testShow_ReturnsNonNullStageAndShowsOverlay() throws Exception {
        Stage parentStage = mock(Stage.class);

        final Stage[] overlayStage = new Stage[1];

        Platform.runLater(() -> {
            Stage result = LoadingOverlay.show(parentStage);
            assertNotNull(result, "Returned Stage should not be null");
            assertTrue(result.isShowing(), "Overlay Stage should be showing");
            overlayStage[0] = result;
        });

        Thread.sleep(500);
        Platform.runLater(() -> {
            if (overlayStage[0] != null) {
                overlayStage[0].close();
            }
        });
    }
}

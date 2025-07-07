package utils;

import javafx.scene.Scene;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Utility class for displaying a translucent, centered loading spinner overlay
 * in the application while background tasks are being performed.
 */
public class LoadingOverlay {
	
    /**
     * Displays a modal loading spinner overlay on top of the given parent stage.
     *
     * @param parentStage the stage that will be blocked while the loading overlay is active
     * @return the Stage representing the loading overlay, which can be closed via loadingStage.close()
     */
    public static Stage show(Stage parentStage) {
        ProgressIndicator spinner = new ProgressIndicator();
        spinner.setPrefSize(100, 100);
        spinner.setStyle("-fx-progress-color: #960000;");

        StackPane root = new StackPane(spinner);
        root.setStyle("-fx-background-color: transparent;"); 
        root.setPrefSize(200, 200);

        Scene scene = new Scene(root);
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT); 
        Stage loadingStage = new Stage(StageStyle.TRANSPARENT);
        loadingStage.initModality(Modality.WINDOW_MODAL);
        loadingStage.initOwner(parentStage);
        loadingStage.setScene(scene);
        loadingStage.setAlwaysOnTop(true);
        loadingStage.centerOnScreen();
        loadingStage.show();

        return loadingStage;
    }
}


package system;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.crypto.SecretKey;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.game;
import models.user;
import security.Encryption;
import services.GameCollectionService;
import services.GameService;
import services.userService;
import utils.KeyStorage;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import controllers.AddGameAPIController;
import controllers.BaseController;
import controllers.FilterCollectionController;
import controllers.ForgotPasswordController;
import controllers.GameCollectionController;
import controllers.LoginController;
import controllers.NavigationHelper;
import controllers.RegistrationController;
import controllers.SettingsController;
import controllers.TestUtils;
import database.CollectionDAO;
import database.UserDAO;

/**
 * System tests for GameGrinding application.
 * 
 * Verifies full-stack integration of features such as manual game addition, API-based game addition,
 * sorting, filtering, searching, account settings updates, password resets, and user registration.
 * Utilizes JavaFX UI with TestFX and service/database layer coordination.
 */
@ExtendWith(ApplicationExtension.class)
public class AddGameSystemTest {

    private GameCollectionController controller;
    private NavigationHelper nav;

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
        controller = loader.getController();
        controller.setUserID(1);
        TestUtils.preloadGameCollection(controller);

        Platform.runLater(() -> {
            stage.setScene(new Scene(root));
            stage.setTitle("GameGrinding - Add Game System Test");
            stage.show();
        });

        WaitForAsyncUtils.waitForFxEvents();
    }

    /**
	 * Cleans up the database after each test by deleting any test users created.
	 * This ensures a clean state for subsequent tests.
	 */
    @BeforeEach
    void deleteUserBeforeTest() {
        try {
            SecretKey key = KeyStorage.getEncryptionKey();
            String encryptedEmail = Encryption.encrypt("test_settings@example.com", key);
            UserDAO userDAO = new UserDAO();
            userDAO.deleteUserByEmail(encryptedEmail);
            String encryptedEmail2 = Encryption.encrypt("reset_password@example.com", key);
            userDAO.deleteUserByEmail(encryptedEmail2);
            String encryptedEmail3 = Encryption.encrypt("editgame_test@example.com", key);
            userDAO.deleteUserByEmail(encryptedEmail3);
            String encryptedEmail4 = Encryption.encrypt("deletegame_test@example.com", key);
            userDAO.deleteUserByEmail(encryptedEmail4);   
            String encryptedHelpEmail = Encryption.encrypt("helptest@example.com", KeyStorage.getEncryptionKey());
            userDAO.deleteUserByEmail(encryptedHelpEmail);
            
            System.out.println("Pre-test user deleted successfully.");
        } catch (Exception e) {
            System.err.println("Error during pre-test user deletion: " + e.getMessage());
        }
    }

    /**
     * ST-01: Verifies that a game can be manually added through the UI and appears in the collection.
     */
    @Test
    void testManualAddGame(FxRobot robot) throws Exception {
        robot.clickOn("#addGameButton");
        robot.clickOn("#manualButton");

        WaitForAsyncUtils.waitForFxEvents();

        robot.clickOn("#titleField").write("Manual Test Game");
        robot.clickOn("#developerField").write("Manual Dev");
        robot.clickOn("#publisherField").write("Manual Pub");
        robot.clickOn("#releaseDateField").write("2024-12-25");
        robot.clickOn("#genreField").write("Indie");
        robot.clickOn("#platformField").write("PC");
        robot.clickOn("#notesField").write("Test notes");
        robot.clickOn("#completionStatusField").clickOn("Completed");

        robot.clickOn("#submitButton");

        Set<Node> nodes = robot.lookup(".gameBox").queryAll();

        boolean gameAppeared = nodes.stream().anyMatch(node -> {
            if (node instanceof VBox) {
                VBox box = (VBox) node;
                return box.getChildren().stream().anyMatch(child ->
                    child instanceof Label &&
                    ((Label) child).getText().contains("Manual Test Game")
                );
            }
            return false;
        });

        assertTrue(gameAppeared, "Manual game should be added and appear in the collection.");

    }


    /**
     * ST-02: Verifies that a game can be searched and added using the API and appears in the collection.
     */
    @Test
    void testAPIAddGame(FxRobot robot) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/AddGameAPI.fxml"));
        Parent apiRoot = loader.load();
        AddGameAPIController apiController = loader.getController();
        apiController.setUserID(1);

        Stage stage = (Stage) robot.window(0);
        Platform.runLater(() -> {
            stage.setScene(new Scene(apiRoot));
            stage.show();
        });
        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("#searchField").write("Elden Ring: Nightreign");
        robot.clickOn("#searchButton");
        Set<Node> vboxes = Set.of();
        boolean vboxAppeared = false;
        for (int i = 0; i < 20; i++) {
            vboxes = robot.lookup(node -> node instanceof VBox).queryAll();
            if (!vboxes.isEmpty()) {
                vboxAppeared = true;
                break;
            }
            Thread.sleep(1000); 
        }
        assertTrue(vboxAppeared, "Expected at least one .vbox result to appear from API search");
        boolean matchingGameFound = vboxes.stream().anyMatch(node -> {
            VBox box = (VBox) node;
            return box.getChildren().stream().anyMatch(child ->
                child instanceof Label &&
                ((Label) child).getText().toLowerCase().contains("elden ring")
            );
        });

        assertTrue(matchingGameFound, "Expected 'Elden Ring' to appear in search results");
        VBox firstVBox = (VBox) vboxes.iterator().next();
        StackPane stack = (StackPane) firstVBox.getChildren().get(0); 
        CheckBox checkBox = null;
        for (Node node : stack.getChildren()) {
            if (node instanceof CheckBox cb) {
                checkBox = cb;
                break;
            }
        }

        assertTrue(checkBox != null, "Checkbox should exist in the tile");
        robot.clickOn(checkBox);
        WaitForAsyncUtils.waitForFxEvents();

        robot.clickOn("#addButton");
        Thread.sleep(1000); 
        WaitForAsyncUtils.waitForFxEvents();
        

        Set<Node> resultNodes = robot.lookup(".gameBox").queryAll();
        boolean gameInCollection = resultNodes.stream().anyMatch(node -> {
            if (node instanceof VBox box) {
                return box.getChildren().stream().anyMatch(child ->
                    child instanceof Label &&
                    ((Label) child).getText().toLowerCase().contains("elden ring")
                );
            }
            return false;
        });

        assertTrue(gameInCollection, "API-added game should appear in the collection.");
    }
    
    
    /**
     * ST-03: Test searching for a game through the API, viewing game details, and returning to results.
     */
    @Test
    void testNavigateToGameDetailsAndBackFromAPISearch(FxRobot robot) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/AddGameAPI.fxml"));
        Parent apiRoot = loader.load();
        AddGameAPIController apiController = loader.getController();
        apiController.setUserID(1);

        Stage stage = (Stage) robot.window(0);
        Platform.runLater(() -> {
            stage.setScene(new Scene(apiRoot));
            stage.show();
        });
        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("#searchField").write("Elden Ring: Nightreign");
        robot.clickOn("#searchButton");

        Set<Node> resultVBoxes = Set.of();
        for (int i = 0; i < 20; i++) {
            resultVBoxes = robot.lookup(node -> node instanceof VBox && node.lookup(".check-box") != null).queryAll();
            if (!resultVBoxes.isEmpty()) break;
            Thread.sleep(5000);
        }

        assertFalse(resultVBoxes.isEmpty(), "Expected at least one search result VBox");

        VBox resultContainer = (VBox) resultVBoxes.iterator().next();
        StackPane stack = (StackPane) resultContainer.getChildren().get(0);
        robot.clickOn(stack);
        WaitForAsyncUtils.waitForFxEvents();

        Label titleLabel = robot.lookup("#gameTitle").queryAs(Label.class);
        assertNotNull(titleLabel, "Expected to be on Game Details screen");
        assertTrue(titleLabel.getText().toLowerCase().contains("elden ring"), "Expected details for 'Elden Ring'");

        robot.clickOn("#backButton");
        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(5000);

        TextField searchField = robot.lookup("#searchField").queryAs(TextField.class);
        assertNotNull(searchField, "Expected to return to AddGameAPI screen with search field visible");
    }


    /**
     * ST-04: Test filtering the game collection by platform using FilterCollectionController.
     */
    @Test
    void testFilterByPlatform(FxRobot robot) throws Exception {
        GameService gameService = new GameService();
        GameCollectionService collectionService = new GameCollectionService();
       
        game testGame = new game();
        testGame.setTitle("Legend of Zelda: Breath of the Wild");
        testGame.setPlatform("Nintendo Switch");
        testGame.setGenre("Adventure");
        testGame.setReleaseDate(LocalDate.of(2021, 6, 1));
        testGame.setCoverImageUrl("Images/placeholder2gameGrinding.png");
        testGame.setCompletionStatus("Not Started");
        
        game testGame2 = new game();
        testGame2.setTitle("Tetris");
        testGame2.setPlatform("PC");
        testGame2.setGenre("Puzzle");
        testGame2.setReleaseDate(LocalDate.of(1984, 6, 6));
        testGame2.setCoverImageUrl("Images/placeholder1gameGrinding.png");  
        testGame2.setCompletionStatus("Completed");
        

        collectionService.addGameToCollection(testGame, 1);
        collectionService.addGameToCollection(testGame2, 1);

        robot.clickOn("#refreashButton");
        Thread.sleep(1000);
        robot.clickOn("#filterButton");
        WaitForAsyncUtils.waitForFxEvents();

        ToggleButton selectedPlatformButton = null;
        for (int i = 0; i < 20; i++) {
            List<ToggleButton> platformButtons = robot.lookup(".toggle-button")
                .queryAllAs(ToggleButton.class)
                .stream()
                .filter(btn -> btn.getText() != null && !btn.getText().isBlank())
                .toList();

            if (!platformButtons.isEmpty()) {
                selectedPlatformButton = platformButtons.stream()
                    .filter(btn -> btn.getText().equalsIgnoreCase("Nintendo Switch"))
                    .findFirst()
                    .orElse(null);
                break;
            }

            Thread.sleep(500);
        }

        assertNotNull(selectedPlatformButton, "Expected 'Nintendo Switch' platform button to exist for filtering");
        final String selectedPlatformText = selectedPlatformButton.getText().toLowerCase();

        robot.clickOn(selectedPlatformButton);
        WaitForAsyncUtils.waitForFxEvents();

        robot.clickOn("#confirmButton");
        Thread.sleep(3000);
        WaitForAsyncUtils.waitForFxEvents();

        Set<Node> gameBoxes = robot.lookup(".gameBox").queryAll();

        assertFalse(gameBoxes.isEmpty(), "Expected filtered game boxes to be displayed after applying platform filter");

        boolean onlyExpectedGameVisible = gameBoxes.stream().allMatch(box -> {
            if (box instanceof VBox vbox) {
                return vbox.getChildren().stream().anyMatch(child ->
                    child instanceof Label &&
                    ((Label) child).getText().toLowerCase().contains("Legend of Zelda: Breath of the Wild".toLowerCase())
                );
            }
            return false;
        });

        assertTrue(onlyExpectedGameVisible, "Only the 'Legend of Zelda: Breath of the Wild' should appear after filtering by 'Nintendo Switch'");
        assertEquals(1, gameBoxes.size(), "Only one game should be displayed after filtering.");
    }


    /**
     * ST-05: Test searching for a game by title in the collection view.
     */
    @Test
    void testSearchGameInCollection(FxRobot robot) throws Exception {
        GameCollectionService collectionService = new GameCollectionService();

        game game1 = new game();
        game1.setTitle("Legend of Zelda");
        game1.setPlatform("Switch");
        game1.setGenre("Adventure");
        game1.setCompletionStatus("Completed");
        game1.setCoverImageUrl("Images/placeholder2gameGrinding.png");
        game1.setReleaseDate(LocalDate.of(2017, 3, 3));

        game game2 = new game();
        game2.setTitle("Super Mario Odyssey");
        game2.setPlatform("Switch");
        game2.setGenre("Platformer");
        game2.setCompletionStatus("Not Started");
        game2.setCoverImageUrl("Images/placeholder1gameGrinding.png");
        game2.setReleaseDate(LocalDate.of(2017, 10, 27));

        game game3 = new game();
        game3.setTitle("Hollow Knight");
        game3.setPlatform("PC");
        game3.setGenre("Metroidvania");
        game3.setCompletionStatus("Playing");
        game3.setCoverImageUrl("Images/placeholder2gameGrinding.png");
        game3.setReleaseDate(LocalDate.of(2017, 2, 24));

        collectionService.addGameToCollection(game1, 1);
        collectionService.addGameToCollection(game2, 1);
        collectionService.addGameToCollection(game3, 1);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/GameCollection.fxml"));
        Parent root = loader.load();
        GameCollectionController controller = loader.getController();
        controller.setUserID(1);

        Stage stage = (Stage) robot.window(0);
        Platform.runLater(() -> {
            stage.setScene(new Scene(root));
            stage.show();
        });
        WaitForAsyncUtils.waitForFxEvents();

        robot.clickOn("#searchBar").write("Hollow");
        robot.clickOn("#searchButton"); 

        Thread.sleep(1000);
        WaitForAsyncUtils.waitForFxEvents();

        Set<Node> gameBoxes = robot.lookup(".gameBox").queryAll();

        assertFalse(gameBoxes.isEmpty(), "Search results should not be empty");

        boolean onlyHollowKnightShown = gameBoxes.stream().allMatch(box -> {
            if (box instanceof VBox vbox) {
                return vbox.getChildren().stream().anyMatch(child ->
                    child instanceof Label &&
                    ((Label) child).getText().toLowerCase().contains("hollow knight")
                );
            }
            return false;
        });

        assertTrue(onlyHollowKnightShown, "Only 'Hollow Knight' should appear after searching 'Hollow'");
        assertEquals(1, gameBoxes.size(), "Only one game should be displayed in search results.");
    }


    /**
     * ST-06: Test sorting the game collection by release date (descending).
     */
    @Test
    void testSortByReleaseDateInCollection(FxRobot robot) throws Exception {
        GameCollectionService collectionService = new GameCollectionService();

        game oldGame = new game();
        oldGame.setTitle("Pac-Man");
        oldGame.setPlatform("Arcade");
        oldGame.setGenre("Arcade");
        oldGame.setCompletionStatus("Completed");
        oldGame.setCoverImageUrl("Images/placeholder1gameGrinding.png");
        oldGame.setReleaseDate(LocalDate.of(1980, 5, 22));

        game midGame = new game();
        midGame.setTitle("Skyrim");
        midGame.setPlatform("PC");
        midGame.setGenre("RPG");
        midGame.setCompletionStatus("Playing");
        midGame.setCoverImageUrl("Images/placeholder2gameGrinding.png");
        midGame.setReleaseDate(LocalDate.of(2011, 11, 11));

        game newGame = new game();
        newGame.setTitle("Elden Ring");
        newGame.setPlatform("PlayStation 5");
        newGame.setGenre("Action RPG");
        newGame.setCompletionStatus("Not Started");
        newGame.setCoverImageUrl("Images/placeholder1gameGrinding.png");
        newGame.setReleaseDate(LocalDate.of(2022, 2, 25));

        collectionService.addGameToCollection(midGame, 1);
        collectionService.addGameToCollection(newGame, 1);
        collectionService.addGameToCollection(oldGame, 1);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/GameCollection.fxml"));
        Parent root = loader.load();
        GameCollectionController controller = loader.getController();
        controller.setUserID(1);

        Stage stage = (Stage) robot.window(0);
        Platform.runLater(() -> {
            stage.setScene(new Scene(root));
            stage.show();
        });
        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("#sortChoiceBox");
        WaitForAsyncUtils.waitForFxEvents();

        robot.clickOn("Release Date"); 
        WaitForAsyncUtils.waitForFxEvents();

        Thread.sleep(1000); 
        WaitForAsyncUtils.waitForFxEvents();

        List<String> actualTitles = robot.lookup(".gameBox").queryAll().stream()
            .filter(node -> node instanceof VBox)
            .map(node -> (VBox) node)
            .map(vbox -> vbox.getChildren().stream()
                .filter(child -> child instanceof Label)
                .map(child -> ((Label) child).getText())
                .findFirst().orElse(""))
            .toList();

        List<String> expectedOrder = List.of("Pac-Man", "Skyrim", "Elden Ring");

        assertEquals(expectedOrder, actualTitles, "Games should be sorted by release date descending");
    }


    /**
     * ST-07: Test sorting the game collection by title.
     */
    @Test
    void testSortByTitleInCollection(FxRobot robot) throws Exception {
        GameCollectionService collectionService = new GameCollectionService();

        game oldGame = new game();
        oldGame.setTitle("Pac-Man");
        oldGame.setPlatform("Arcade");
        oldGame.setGenre("Arcade");
        oldGame.setCompletionStatus("Completed");
        oldGame.setCoverImageUrl("Images/placeholder1gameGrinding.png");
        oldGame.setReleaseDate(LocalDate.of(1980, 5, 22));

        game midGame = new game();
        midGame.setTitle("Skyrim");
        midGame.setPlatform("PC");
        midGame.setGenre("RPG");
        midGame.setCompletionStatus("Playing");
        midGame.setCoverImageUrl("Images/placeholder2gameGrinding.png");
        midGame.setReleaseDate(LocalDate.of(2011, 11, 11));

        game newGame = new game();
        newGame.setTitle("Elden Ring");
        newGame.setPlatform("PlayStation 5");
        newGame.setGenre("Action RPG");
        newGame.setCompletionStatus("Not Started");
        newGame.setCoverImageUrl("Images/placeholder1gameGrinding.png");
        newGame.setReleaseDate(LocalDate.of(2022, 2, 25));

        collectionService.addGameToCollection(midGame, 1);
        collectionService.addGameToCollection(newGame, 1);
        collectionService.addGameToCollection(oldGame, 1);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/GameCollection.fxml"));
        Parent root = loader.load();
        GameCollectionController controller = loader.getController();
        controller.setUserID(1);

        Stage stage = (Stage) robot.window(0);
        Platform.runLater(() -> {
            stage.setScene(new Scene(root));
            stage.show();
        });
        WaitForAsyncUtils.waitForFxEvents();

        robot.clickOn("#sortChoiceBox");
        WaitForAsyncUtils.waitForFxEvents();

        robot.clickOn("Title");  
        WaitForAsyncUtils.waitForFxEvents();

        Thread.sleep(1000);
        WaitForAsyncUtils.waitForFxEvents();

        List<String> actualTitles = robot.lookup(".gameBox").queryAll().stream()
            .filter(node -> node instanceof VBox)
            .map(node -> (VBox) node)
            .map(vbox -> vbox.getChildren().stream()
                .filter(child -> child instanceof Label)
                .map(child -> ((Label) child).getText())
                .findFirst().orElse(""))
            .toList();

        List<String> expectedOrder = List.of("Elden Ring", "Pac-Man", "Skyrim");

        assertEquals(expectedOrder, actualTitles, "Games should be sorted by Title descending");
    }
    
    /**
	 * ST-08: Test sorting the game collection by platform.
	 */
    @Test
    void testSortByPlatformInCollection(FxRobot robot) throws Exception {
        GameCollectionService collectionService = new GameCollectionService();

        game oldGame = new game();
        oldGame.setTitle("Pac-Man");
        oldGame.setPlatform("Arcade");
        oldGame.setGenre("Arcade");
        oldGame.setCompletionStatus("Completed");
        oldGame.setCoverImageUrl("Images/placeholder1gameGrinding.png");
        oldGame.setReleaseDate(LocalDate.of(1980, 5, 22));

        game midGame = new game();
        midGame.setTitle("Skyrim");
        midGame.setPlatform("PC");
        midGame.setGenre("RPG");
        midGame.setCompletionStatus("Playing");
        midGame.setCoverImageUrl("Images/placeholder2gameGrinding.png");
        midGame.setReleaseDate(LocalDate.of(2011, 11, 11));

        game newGame = new game();
        newGame.setTitle("Elden Ring");
        newGame.setPlatform("PlayStation 5");
        newGame.setGenre("Action RPG");
        newGame.setCompletionStatus("Not Started");
        newGame.setCoverImageUrl("Images/placeholder1gameGrinding.png");
        newGame.setReleaseDate(LocalDate.of(2022, 2, 25));

        collectionService.addGameToCollection(midGame, 1);
        collectionService.addGameToCollection(newGame, 1);
        collectionService.addGameToCollection(oldGame, 1);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/GameCollection.fxml"));
        Parent root = loader.load();
        GameCollectionController controller = loader.getController();
        controller.setUserID(1);

        Stage stage = (Stage) robot.window(0);
        Platform.runLater(() -> {
            stage.setScene(new Scene(root));
            stage.show();
        });
        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("#sortChoiceBox");
        WaitForAsyncUtils.waitForFxEvents();

        robot.clickOn("Platform"); 
        WaitForAsyncUtils.waitForFxEvents();

        Thread.sleep(1000); 
        WaitForAsyncUtils.waitForFxEvents();

        List<String> actualTitles = robot.lookup(".gameBox").queryAll().stream()
            .filter(node -> node instanceof VBox)
            .map(node -> (VBox) node)
            .map(vbox -> vbox.getChildren().stream()
                .filter(child -> child instanceof Label)
                .map(child -> ((Label) child).getText())
                .findFirst().orElse(""))
            .toList();

        List<String> expectedOrder = List.of("Pac-Man", "Skyrim", "Elden Ring");

        assertEquals(expectedOrder, actualTitles, "Games should be sorted by Platform ascending");
    }


    /**
     * ST-09: Test changing username and email from the Settings page.
     */
    @Test
    void testChangeUsernameAndEmail_fromSettingsPage(FxRobot robot) throws Exception {
        SecretKey key = KeyStorage.getEncryptionKey();

        String originalUsername = "TestUser_Settings";
        String originalEmail = "test_settings@example.com";
        String password = "Pass123!@#";

        userService userService = new userService();

        boolean registered = userService.registerUser(
            originalUsername, originalEmail, password, "dog", "blue", "pizza"
        );
        assertTrue(registered, "User should be registered successfully.");

        user registeredUser = userService.getUserByEmail(originalEmail);
        assertNotNull(registeredUser, "Registered user should be retrievable.");
        int userID = registeredUser.getUserID();

        userService.setCurrentUserID(userID);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Settings.fxml"));
        Parent root = loader.load();
        SettingsController controller = loader.getController();
        controller.setUserID(userID); 

        Stage stage = (Stage) robot.window(0);
        Platform.runLater(() -> {
            stage.setScene(new Scene(root));
            stage.show();
        });
        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(1000); 

        String updatedUsername = "UpdatedUserSettings";
        String updatedEmail = "updated@example.com";

        robot.clickOn("#usernameField").eraseText(50).write(updatedUsername);
        robot.doubleClickOn("#emailField").eraseText(100).write(updatedEmail);
        WaitForAsyncUtils.waitForFxEvents();

        robot.clickOn("#saveAccountButton");
        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(1000);

        user updatedUser = userService.getUserByID(userID);
        assertNotNull(updatedUser, "Updated user should still exist in DB.");

        String decryptedEmail = Encryption.decrypt(updatedUser.getEmail(), key);

        assertEquals(updatedUsername, updatedUser.getUsername(), "Username should be updated.");
        assertEquals(updatedEmail, decryptedEmail, "Email should be updated and encrypted.");
    }
    /**
     * ST-10: Test resetting password from the Settings page.
     */
    @Test
    void testResetPassword_fromSettingsPage(FxRobot robot) throws Exception {
        String username = "ResetTestUser";
        String email = "reset_password@example.com";
        String originalPassword = "OldPass123!";
        String newPassword = "NewPass456!";

        userService userService = new userService();
        boolean registered = userService.registerUser(username, email, originalPassword, "cat", "green", "burger");
        assertTrue(registered, "User should be registered successfully.");

        user testUser = userService.getUserByEmail(email);
        assertNotNull(testUser, "Registered user should exist.");
        int userID = testUser.getUserID();
        userService.setCurrentUserID(userID);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Settings.fxml"));
        Parent root = loader.load();
        SettingsController controller = loader.getController();
        controller.setUserID(userID);

        Stage stage = (Stage) robot.window(0);
        Platform.runLater(() -> {
            stage.setScene(new Scene(root));
            stage.show();
        });
        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(1000);

        robot.clickOn("#currentPasswordField").write(originalPassword);
        robot.clickOn("#currentPasswordField2").write(originalPassword);
        robot.clickOn("#newPasswordField").write(newPassword);
        robot.clickOn("#confirmPasswordField").write(newPassword);
        robot.clickOn("#savePasswordButton");
        
        

        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(1000); 
        
        FxRobot finalRobot = robot;
        WaitForAsyncUtils.waitFor(3, TimeUnit.SECONDS, () ->
            !finalRobot.lookup(".dialog-pane .button").queryAll().isEmpty()
        );

        boolean loggedInUser = userService.authenticateUser(email, newPassword);
        assertTrue(loggedInUser, "Logged in user should be authenticated.");
    }


    /**
     * ST-11: Test user account creation from the Registration page.
     */
    @Test
    void testCreateAccount_fromRegistrationPage(FxRobot robot) throws Exception {
        String username = "TestRegisterUser";
        String email = "register_test@example.com";
        String password = "StrongPass123!";
        String q1 = "Blue";
        String q2 = "Pizza";
        String q3 = "Cat";

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Main.fxml"));
        Parent root = loader.load();
        LoginController controller = loader.getController();

        Stage stage = (Stage) robot.window(0);
        Platform.runLater(() -> {
            stage.setScene(new Scene(root));
            stage.show();
        });
        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(1000);
        robot.clickOn("#createAccountButton");

        robot.clickOn("#usernameField").write(username);
        robot.clickOn("#emailField").write(email);
        robot.clickOn("#passwordField").write(password);
        robot.clickOn("#SecurityQuestion1").write(q1);
        robot.clickOn("#SecurityQuestion2").write(q2);
        robot.clickOn("#SecurityQuestion3").write(q3);

        robot.clickOn("#registerButton");
        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(1000);

        userService userService = new userService();
        user newUser = userService.getUserByEmail(email);
        assertNotNull(newUser, "Newly registered user should exist in DB.");
        assertEquals(username, newUser.getUsername(), "Usernames should match.");
    }

    /**
     * ST-12: Test the forgot password flow, then login with the new password.
     */
    @Test
    void testForgotPassword_thenLogin(FxRobot robot) throws Exception {
        String username = "ForgotPassUser";
        String email = "forgot_test@example.com";
        String oldPassword = "OldPassword123!";
        String newPassword = "NewPassword@123";
        String q1 = "blue";
        String q2 = "pizza";
        String q3 = "cat";

        userService userService = new userService();
        boolean registered = userService.registerUser(username, email, oldPassword, q1, q2, q3);
        assertTrue(registered, "User should be registered successfully.");

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Main.fxml"));
        Parent root = loader.load();
        LoginController controller = loader.getController();

        Stage stage = (Stage) robot.window(0);
        Platform.runLater(() -> {
            stage.setScene(new Scene(root));
            stage.show();
        });
        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(1000); 
        robot.clickOn("#forgotPasswordButton");
        robot.clickOn("#emailField").write(email);
        robot.clickOn("#secuirityAnswerField1").write(q1);
        robot.clickOn("#secuirityAnswerField2").write(q2);
        robot.clickOn("#secuirityAnswerField3").write(q3);
        robot.clickOn("#PasswordField").write(newPassword);
        robot.clickOn("#confirmPasswordField").write(newPassword);

        robot.clickOn("#submitButton");
        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(1000);

        WaitForAsyncUtils.sleep(500, TimeUnit.MILLISECONDS);
        FxRobot finalRobot = robot;
        WaitForAsyncUtils.waitFor(3, TimeUnit.SECONDS, () ->
            !finalRobot.lookup(".dialog-pane .button").queryAll().isEmpty()
        );

        robot.clickOn(".dialog-pane .button");
        WaitForAsyncUtils.waitForFxEvents();

        WaitForAsyncUtils.sleep(500, TimeUnit.MILLISECONDS); 
        FxRobot finalRobot2 = robot;
        WaitForAsyncUtils.waitFor(3, TimeUnit.SECONDS, () ->
            !finalRobot2.lookup(".dialog-pane .button").queryAll().isEmpty()
        );
        robot.clickOn(".dialog-pane .button");
        WaitForAsyncUtils.waitForFxEvents();

        FXMLLoader loginLoader = new FXMLLoader(getClass().getResource("/views/Main.fxml"));
        Parent loginRoot = loginLoader.load();
        LoginController loginController = loginLoader.getController();

        Platform.runLater(() -> {
            stage.setScene(new Scene(loginRoot));
            stage.show();
        });
        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(1000); 

        robot.clickOn("#emailField").write(email);
        robot.clickOn("#passwordField").write(newPassword);
        robot.clickOn("#submitButton");
        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(1500); 

        WaitForAsyncUtils.sleep(1, TimeUnit.SECONDS);
        boolean gameCollectionLoaded = robot.lookup("#addGameButton").tryQuery().isPresent();

        assertTrue(gameCollectionLoaded, "User should be navigated to Game Collection screen after login.");

    }
    
    /**
     * ST-13: Create a new user, prepopulate 3 games, open Edit screen on one game, and make changes.
     */
    @Test
    void testEditGameFromCollection(FxRobot robot) throws Exception {
        String username = "EditGameTestUser";
        String email = "editgame_test@example.com";
        String password = "TestPass!123";
        userService userService = new userService();
        boolean registered = userService.registerUser(username, email, password, "dog", "blue", "pizza");
        assertTrue(registered, "User should be registered.");

        user newUser = userService.getUserByEmail(email);
        assertNotNull(newUser, "New user should exist.");
        int userID = newUser.getUserID(); 
        userService.setCurrentUserID(userID);

        GameCollectionService collectionService = new GameCollectionService();

        game g1 = new game("Spiderman", "PS5", "Action", LocalDate.of(2020, 9, 10), "Insomniac", "Sony", "Finished game", "Completed", "Images/placeholder1gameGrinding.png");
        game g2 = new game("Celeste", "PC", "Platformer", LocalDate.of(2018, 1, 25), "Matt Makes Games", "Self-Published", "Hard but rewarding", "Playing", "Images/placeholder2gameGrinding.png");
        game g3 = new game("Firewatch", "PC", "Adventure", LocalDate.of(2016, 2, 9), "Campo Santo", "Panic", "Emotional story", "Not Started", "Images/placeholder1gameGrinding.png");

        collectionService.addGameToCollection(g1, userID);
        collectionService.addGameToCollection(g2, userID);
        collectionService.addGameToCollection(g3, userID);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Main.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage) robot.window(0);
        Platform.runLater(() -> {
            stage.setScene(new Scene(root));
            stage.show();
        });
        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(1000);

        robot.clickOn("#emailField").write(email);
        robot.clickOn("#passwordField").write(password);
        robot.clickOn("#submitButton");

        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(2000); 
        
        VBox targetBox = robot.lookup(".gameBox").queryAll().stream()
            .filter(node -> node instanceof VBox)
            .map(node -> (VBox) node)
            .filter(vbox -> vbox.getChildren().stream()
                .anyMatch(child -> child instanceof Label && ((Label) child).getText().toLowerCase().contains("celeste")))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Celeste game box not found"));

        robot.clickOn(targetBox);
        WaitForAsyncUtils.waitForFxEvents();

        robot.clickOn("#editButton");
        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(1000);

        robot.clickOn("#titleField").eraseText(10).write("Celeste (Updated)");
        robot.clickOn("#platformField").eraseText(10).write("Steam");
        robot.clickOn("#notesField").write(" - Now 100% completed!");
        robot.clickOn("#completionStatusChoiceBox").clickOn("Completed");

        robot.clickOn("#saveButton");
        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(1000);

        Set<Node> updatedBoxes = robot.lookup(".gameBox").queryAll();
        boolean updatedGamePresent = updatedBoxes.stream().anyMatch(node -> {
            if (node instanceof VBox vbox) {
                return vbox.getChildren().stream().anyMatch(child ->
                    child instanceof Label &&
                    ((Label) child).getText().toLowerCase().contains("celeste (updated)")
                );
            }
            return false;
        });

        assertTrue(updatedGamePresent, "Updated game title should appear in collection.");
    }

    /** 
     * ST-14: Create a new user, prepopulate 3 games, open Delete popup on one game, and confirm deletion.
     */
    @Test
    void testDeleteGameFromCollection(FxRobot robot) throws Exception {
        String username = "DeleteGameTestUser";
        String email = "deletegame_test@example.com";
        String password = "DelPass!321";
        userService userService = new userService();
        boolean registered = userService.registerUser(username, email, password, "dog", "blue", "pizza");
        assertTrue(registered, "User should be registered.");

        user newUser = userService.getUserByEmail(email);
        assertNotNull(newUser, "New user should exist.");
        int userID = newUser.getUserID();
        userService.setCurrentUserID(userID);

        GameCollectionService collectionService = new GameCollectionService();
        game g1 = new game("Spiderman", "PS5", "Action", LocalDate.of(2020, 9, 10), "Insomniac", "Sony", "Finished game", "Completed", "Images/placeholder1gameGrinding.png");
        game g2 = new game("Celeste", "PC", "Platformer", LocalDate.of(2018, 1, 25), "Matt Makes Games", "Self-Published", "Hard but rewarding", "Playing", "Images/placeholder2gameGrinding.png");
        game g3 = new game("Firewatch", "PC", "Adventure", LocalDate.of(2016, 2, 9), "Campo Santo", "Panic", "Emotional story", "Not Started", "Images/placeholder1gameGrinding.png");
        collectionService.addGameToCollection(g1, userID);
        collectionService.addGameToCollection(g2, userID);
        collectionService.addGameToCollection(g3, userID);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Main.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) robot.window(0);
        Platform.runLater(() -> {
            stage.setScene(new Scene(root));
            stage.show();
        });
        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(1000);

        robot.clickOn("#emailField").write(email);
        robot.clickOn("#passwordField").write(password);
        robot.clickOn("#submitButton");

        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(2000);

        VBox targetBox = robot.lookup(".gameBox").queryAll().stream()
            .filter(node -> node instanceof VBox)
            .map(node -> (VBox) node)
            .filter(vbox -> vbox.getChildren().stream()
                .anyMatch(child -> child instanceof Label && ((Label) child).getText().toLowerCase().contains("celeste")))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Celeste game box not found"));

        robot.clickOn(targetBox);
        WaitForAsyncUtils.waitForFxEvents();

        robot.clickOn("#deleteButton");
        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(1000);

        robot.clickOn("#confirmButton");
        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(2000); 

        Set<Node> updatedBoxes = robot.lookup(".gameBox").queryAll();
        boolean celesteStillPresent = updatedBoxes.stream().anyMatch(node -> {
            if (node instanceof VBox vbox) {
                return vbox.getChildren().stream().anyMatch(child ->
                    child instanceof Label &&
                    ((Label) child).getText().toLowerCase().contains("celeste")
                );
            }
            return false;
        });

        assertFalse(celesteStillPresent, "Celeste should no longer appear in the collection after deletion.");
    }

    /**
     * ST-15: Create a new user, log in, navigate to Help page, expand help topics, and return to Game Collection.
     */
    @Test
    void testNavigateHelpTopicsAndReturnToCollection(FxRobot robot) throws Exception {
        String username = "HelpTestUser";
        String email = "helptest@example.com";
        String password = "HelpPass123!";
        userService userService = new userService();
        boolean registered = userService.registerUser(username, email, password, "cat", "green", "pizza");
        assertTrue(registered, "User should be registered.");

        user newUser = userService.getUserByEmail(email);
        int userID = newUser.getUserID();
        userService.setCurrentUserID(userID);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Main.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) robot.window(0);
        Platform.runLater(() -> {
            stage.setScene(new Scene(root));
            stage.show();
        });
        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(1000);

        robot.clickOn("#emailField").write(email);
        robot.clickOn("#passwordField").write(password);
        robot.clickOn("#submitButton");
        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(2000);

        robot.clickOn("#helpButton");
        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(1000);

        List<TitledPane> panes = robot.lookup(".titled-pane").queryAllAs(TitledPane.class).stream().toList();
        assertTrue(panes.size() >= 3, "Expected at least 3 help topics available.");

        robot.clickOn(panes.get(0)); 
        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(500);

        robot.clickOn(panes.get(1));
        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(500);

        robot.clickOn(panes.get(2)); 
        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(500);

        robot.clickOn("#gameCollectionButton");
        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(1500);

        boolean collectionScreenShown = robot.lookup("#addGameButton").tryQuery().isPresent();
        assertTrue(collectionScreenShown, "Expected to be back on Game Collection screen after returning from Help page.");
    }

    
    /**
     * Cleans up the database after each test by deleting any test games and users created.
     */
    @AfterEach
    void cleanup() {
        CollectionDAO dao = new CollectionDAO();
        UserDAO userDAO = new UserDAO();
        dao.deleteGameByTitle("Manual Test Game", 1);
        dao.deleteGameByTitle("Elden Ring: Nightreign", 1);
        dao.deleteGameByTitle("Tetris", 1);
        dao.deleteGameByTitle("Legend of Zelda", 1);
        dao.deleteGameByTitle("Super Mario Odyssey", 1);
        dao.deleteGameByTitle("Hollow Knight", 1);
        dao.deleteGameByTitle("Elden Ring", 1);
        dao.deleteGameByTitle("Skyrim", 1);
        dao.deleteGameByTitle("Pac-Man", 1);
        dao.deleteGameByTitle("Legend of Zelda: Breath of the Wild", 1);
        dao.deleteGameByTitle("Spiderman", 1);
        dao.deleteGameByTitle("Celeste", 1);
        dao.deleteGameByTitle("Celeste (Updated)", 1);
        dao.deleteGameByTitle("Firewatch", 1);
        Platform.runLater(() -> controller.clearCollection());
        WaitForAsyncUtils.waitForFxEvents();
        try {
            SecretKey key = KeyStorage.getEncryptionKey();
            String encryptedEmail = Encryption.encrypt("updated@example.com", key);
            userDAO.deleteUserByEmail(encryptedEmail);
            String encryptedEmail3 = Encryption.encrypt("register_test@example.com", key);
            userDAO.deleteUserByEmail(encryptedEmail3);
            String encryptedEmail4 = Encryption.encrypt("forgot_test@example.com", key);
            userDAO.deleteUserByEmail(encryptedEmail4);
        } catch (Exception e) {
            System.err.println("Error deleting test user: " + e.getMessage());
        }
    }
    
    

}

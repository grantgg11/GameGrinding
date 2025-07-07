package controllers;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;
 
import javafx.scene.control.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import services.userService;
import utils.AlertHelper;



/**
 * Unit tests for the LoginController class.
 *
 * This test class verifies the login flow, input validation,
 * error handling, and navigation logic for the login screen of the GameGrinding application.
 * It uses mock dependencies for user authentication, alert handling, and navigation.
 * JavaFX fields are injected manually to simulate controller behavior without full UI launch.
 */
@ExtendWith(JavaFXThreadingExtension.class)
class LoginControllerTest {

    private LoginController controller;

    /**
	 * Initializes the controller and injects mock dependencies before each test.
	 * Sets up JavaFX controls to simulate the login screen environment.
	 */
    @BeforeEach
    void setUp() {
        controller = new LoginController();

        TextField emailField = new TextField();
        PasswordField passwordField = new PasswordField();
        Button submitButton = new Button();
        Label errorLabel = new Label();
        Button createAccountButton = new Button();
        Button forgotPasswordButton = new Button();

        TestUtils.setPrivateField(controller, "emailField", emailField);
        TestUtils.setPrivateField(controller, "passwordField", passwordField);
        TestUtils.setPrivateField(controller, "submitButton", submitButton);
        TestUtils.setPrivateField(controller, "errorLabel", errorLabel);
        TestUtils.setPrivateField(controller, "createAccountButton", createAccountButton);
        TestUtils.setPrivateField(controller, "forgotPasswordButton", forgotPasswordButton);
    }

    /**
	 * Verifies that the controller initializes without throwing exceptions and clears any previous error messages.
	 */
    @Test
    void testInitialize_shouldClearErrorLabel() {
        Label errorLabel = new Label("Previous error");
        TestUtils.setPrivateField(controller, "errorLabel", errorLabel);
        controller.initialize();
        assertEquals("", errorLabel.getText());
    }

    /**
     * Tests the handleLogin method with empty email and password fields.
     * Verifies that it shows an error message prompting the user to fill in all fields.
     */
    @Test
    void testHandleLogin_withEmptyFields_shouldShowError() {
        controller.handleLogin();
        Label errorLabel = (Label) TestUtils.getPrivateField(controller, "errorLabel");
        assertEquals("Please fill in all fields.", errorLabel.getText());
    }

    /**
	 * Tests the handleLogin method with a valid email and password.
	 * Verifies that it navigates to the game collection if authentication is successful.
	 * Also checks that the current user ID is set correctly.
	 */
    @Test
    void testHandleLogin_withValidCredentials_shouldNavigate() throws Exception {
        TextField emailField = new TextField("test@example.com");
        PasswordField passwordField = new PasswordField();
        passwordField.setText("password123");
        Button submitButton = new Button();

        userService mockUserService = mock(userService.class);
        NavigationHelper mockNavHelper = mock(NavigationHelper.class);

        when(mockUserService.authenticateUser("test@example.com", "password123")).thenReturn(true);
        when(mockUserService.getCurrentUserID()).thenReturn(42);

        TestUtils.setPrivateField(controller, "emailField", emailField);
        TestUtils.setPrivateField(controller, "passwordField", passwordField);
        TestUtils.setPrivateField(controller, "submitButton", submitButton);
        TestUtils.setPrivateField(controller, "userService", mockUserService);
        TestUtils.setPrivateField(controller, "navHelp", mockNavHelper);

        controller.handleLogin();
        verify(mockNavHelper).switchToGameCollection(42, submitButton);
    }

    /**
     * Verifies that submitting invalid login credentials shows an error alert.
     */
    @Test
    void testHandleLogin_withInvalidCredentials_shouldShowAlert() {
        TextField emailField = new TextField("wrong@example.com");
        PasswordField passwordField = new PasswordField();
        passwordField.setText("wrongpass");

        userService mockUserService = mock(userService.class);
        AlertHelper mockAlert = mock(AlertHelper.class);

        when(mockUserService.authenticateUser("wrong@example.com", "wrongpass")).thenReturn(false);

        TestUtils.setPrivateField(controller, "emailField", emailField);
        TestUtils.setPrivateField(controller, "passwordField", passwordField);
        TestUtils.setPrivateField(controller, "userService", mockUserService);
        TestUtils.setPrivateField(controller, "alert", mockAlert);

        controller.handleLogin();
        verify(mockAlert).showError(any(), any(), any());
    }


    /**
	 * Tests the openCreateAccount method to ensure it navigates to the create account page.
	 */
    @Test
    void testOpenForgotPassword_whenNavigationFails_shouldSetErrorLabel() {
        Button forgotPasswordButton = new Button();
        Label errorLabel = new Label();
        NavigationHelper mockNav = mock(NavigationHelper.class);

        doThrow(new RuntimeException("Simulated failure")).when(mockNav).switchToForgotPasswordPage(forgotPasswordButton);

        TestUtils.setPrivateField(controller, "forgotPasswordButton", forgotPasswordButton);
        TestUtils.setPrivateField(controller, "errorLabel", errorLabel);
        TestUtils.setPrivateField(controller, "navHelp", mockNav);

        controller.openForgotPassword();

        assertEquals("Failed to open Forgot Password page.", errorLabel.getText());
    }
}

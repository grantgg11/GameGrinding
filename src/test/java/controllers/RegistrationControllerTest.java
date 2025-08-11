package controllers;

import javafx.scene.control.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import services.userService;
import utils.AlertHelper;
import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the RegistrationController class. 
 *
 * This test class verifies the registration flow, input validation,
 * error handling, and navigation logic for the registration screen of the GameGrinding application.
 * It uses mock dependencies for user registration, alert handling, and navigation.
 * JavaFX fields are injected manually to simulate controller behavior without full UI launch.
 */
@ExtendWith(JavaFXThreadingExtension.class)
class RegistrationControllerTest {

    private RegistrationController controller;
    private userService mockUserService;
    private NavigationHelper mockNavHelper;
    private AlertHelper mockAlert;

    /**
     * Sets up the test environment before each test case.
     * Initializes the RegistrationController instance and injects mocked
     * dependencies such as userService, NavigationHelper, and AlertHelper.
     */
    @BeforeEach
    void setUp() {
        controller = new RegistrationController();

        mockUserService = mock(userService.class);
        mockNavHelper = mock(NavigationHelper.class);
        mockAlert = mock(AlertHelper.class);

        TestUtils.setPrivateField(controller, "userService", mockUserService);
        TestUtils.setPrivateField(controller, "navHelper", mockNavHelper);
        TestUtils.setPrivateField(controller, "alert", mockAlert);
    }
    
    /**
     * Verifies that the error label is cleared when the controller is initialized.
     */
    @Test
    void testInitialize_shouldClearErrorLabel() {
        Label error = new Label("Previous error");
        TestUtils.setPrivateField(controller, "errorLabel", error);
        controller.initialize();
        assertEquals("", error.getText(), "Error label should be cleared on initialization");
    }
    
    ////////////////////// testing handleRegister /////////////////////////
    
    /**
     * Tests the behavior of handleRegister when all input fields are empty.
     * Expects an error alert to be shown indicating that all fields are required.
     */
    @Test
    void testHandleRegister_allFieldsEmpty_shouldShowError() {
        TestUtils.setPrivateField(controller, "usernameField", new TextField(""));
        TestUtils.setPrivateField(controller, "emailField", new TextField(""));
        TestUtils.setPrivateField(controller, "passwordField", new PasswordField());
        TestUtils.setPrivateField(controller, "SecurityQuestion1", new TextField(""));
        TestUtils.setPrivateField(controller, "SecurityQuestion2", new TextField(""));
        TestUtils.setPrivateField(controller, "SecurityQuestion3", new TextField(""));

        controller.handleRegister();

        verify(mockAlert).showError(eq("Error"), eq("All fields are required."), anyString());
    }

    /**
     * Tests successful registration flow where all fields are valid and registration is successful.
     * Verifies that a success alert is shown and the user is navigated to the login page.
     */
    @Test
    void testHandleRegister_successfulRegistration_shouldNavigateAndShowSuccess() throws Exception {
        TextField username = new TextField("testuser");
        TextField email = new TextField("test@example.com");
        PasswordField password = new PasswordField();
        password.setText("pass123");
        TextField q1 = new TextField("ans1");
        TextField q2 = new TextField("ans2");
        TextField q3 = new TextField("ans3");
        Button registerBtn = new Button("Register");

        TestUtils.setPrivateField(controller, "usernameField", username);
        TestUtils.setPrivateField(controller, "emailField", email);
        TestUtils.setPrivateField(controller, "passwordField", password);
        TestUtils.setPrivateField(controller, "SecurityQuestion1", q1);
        TestUtils.setPrivateField(controller, "SecurityQuestion2", q2);
        TestUtils.setPrivateField(controller, "SecurityQuestion3", q3);
        TestUtils.setPrivateField(controller, "registerButton", registerBtn);

        when(mockUserService.registerUser(anyString(), anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(true);

        controller.handleRegister();

        verify(mockAlert).showInfo(contains("Success"), contains("Successful"), contains("log in"));
        verify(mockNavHelper).switchToLoginPage(registerBtn);
    }

    /**
     * Tests the scenario where registration fails (e.g., duplicate user).
     * Verifies NO alert is shown and a failure message is printed to the console.
     */
    @Test
    void testHandleRegister_registrationFails_shouldPrintFailureAndNoAlert() throws Exception {
        TextField username = new TextField("testuser");
        TextField email = new TextField("test@example.com");
        PasswordField password = new PasswordField();
        password.setText("pass123");
        TextField q1 = new TextField("ans1");
        TextField q2 = new TextField("ans2");
        TextField q3 = new TextField("ans3");

        TestUtils.setPrivateField(controller, "usernameField", username);
        TestUtils.setPrivateField(controller, "emailField", email);
        TestUtils.setPrivateField(controller, "passwordField", password);
        TestUtils.setPrivateField(controller, "SecurityQuestion1", q1);
        TestUtils.setPrivateField(controller, "SecurityQuestion2", q2);
        TestUtils.setPrivateField(controller, "SecurityQuestion3", q3);

        userService mockUserService = mock(userService.class);
        AlertHelper mockAlert = mock(AlertHelper.class);
        TestUtils.setPrivateField(controller, "userService", mockUserService);
        TestUtils.setPrivateField(controller, "alert", mockAlert);

        when(mockUserService.registerUser(anyString(), anyString(), anyString(),anyString(), anyString(), anyString())).thenReturn(false);


        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        try {
            org.testfx.api.FxToolkit.setupFixture(controller::handleRegister);
            org.testfx.util.WaitForAsyncUtils.waitForFxEvents();
        } finally {
            System.setOut(originalOut);
        }

        verify(mockUserService).registerUser(
            eq("testuser"), eq("test@example.com"), eq("pass123"),
            eq("ans1"), eq("ans2"), eq("ans3")
        );

        verifyNoInteractions(mockAlert);

        String output = outContent.toString();
        assertTrue(output.contains("Registration failed!"), "Expected console output to contain 'Registration failed!' but got: " + output);
    }


    /**
     * Tests that an exception during registration is handled gracefully and an appropriate error alert is displayed.
     */
    @Test
    void testHandleRegister_exceptionDuringRegistration_shouldShowError() throws Exception {
        TextField username = new TextField("testuser");
        TextField email = new TextField("test@example.com");
        PasswordField password = new PasswordField();
        password.setText("pass123");
        TextField q1 = new TextField("ans1");
        TextField q2 = new TextField("ans2");
        TextField q3 = new TextField("ans3");

        TestUtils.setPrivateField(controller, "usernameField", username);
        TestUtils.setPrivateField(controller, "emailField", email);
        TestUtils.setPrivateField(controller, "passwordField", password);
        TestUtils.setPrivateField(controller, "SecurityQuestion1", q1);
        TestUtils.setPrivateField(controller, "SecurityQuestion2", q2);
        TestUtils.setPrivateField(controller, "SecurityQuestion3", q3);

        when(mockUserService.registerUser(anyString(), anyString(), anyString(), anyString(), anyString(), anyString()))
            .thenThrow(new RuntimeException("Registration exception"));

        controller.handleRegister();

        verify(mockAlert).showError(eq("Error"), eq("Registration Error"), contains("error"));
    }
    
    /////////////////////////////// testing handleBackButton ///////////////////////////////
    
    /**
     * Tests that clicking the back button triggers navigation back to the login page.
     */
    @Test
    void testHandleBackButton_shouldNavigateToLoginPage() {
        Button backBtn = new Button("Back");
        TestUtils.setPrivateField(controller, "backButton", backBtn);

        controller.handleBackButton();

        verify(mockNavHelper).switchToLoginPage(backBtn);
    }



}
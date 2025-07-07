package controllers;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import models.user;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.util.WaitForAsyncUtils;

import utils.AlertHelper;
import services.userService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ForgotPasswordController in the GameGrinding application.
 * 
 * This test class verifies the behavior of the Forgot Password UI, focusing on:
 * - Field validation logic for enabling/disabling the Submit button.
 * - Correct branching logic during password reset attempts.
 * - Interaction with dependencies like userService, AlertHelper, and NavigationHelper.
 */
@ExtendWith({ApplicationExtension.class, JavaFXThreadingExtension.class})
class ForgotPasswordControllerTest {

    private ForgotPasswordController controller;
    private userService mockUserService;
    private AlertHelper mockAlert;
    private NavigationHelper mockNav;
    
    private TextField emailField;
    private TextField sec1;
    private TextField sec2;
    private TextField sec3;
    private TextField passwordField;
    private TextField confirmPasswordField;
    private Button submitButton;
    private Button backButton;

    /**
     * Sets up the test environment with a mocked controller and injected UI components and services.
     */
    @BeforeEach
    void setUp() {
        controller = new ForgotPasswordController();
        mockUserService = mock(userService.class);
        mockAlert = mock(AlertHelper.class);
        mockNav = mock(NavigationHelper.class);

        controller.userSer = mockUserService;
        controller.alert = mockAlert;
        controller.navHelper = mockNav;

        emailField = new TextField();
        sec1 = new TextField();
        sec2 = new TextField();
        sec3 = new TextField();
        passwordField = new TextField();
        confirmPasswordField = new TextField();
        submitButton = new Button();
        backButton = new Button();

        // Inject UI fields
        TestUtils.setPrivateField(controller, "emailField", emailField);
        TestUtils.setPrivateField(controller, "secuirityAnswerField1", sec1);
        TestUtils.setPrivateField(controller, "secuirityAnswerField2", sec2);
        TestUtils.setPrivateField(controller, "secuirityAnswerField3", sec3);
        TestUtils.setPrivateField(controller, "PasswordField", passwordField);
        TestUtils.setPrivateField(controller, "confirmPasswordField", confirmPasswordField);
        TestUtils.setPrivateField(controller, "submitButton", submitButton);
        TestUtils.setPrivateField(controller, "backButton", backButton);
    }
    /** Verifies that the Submit button is disabled by default upon controller initialization. */
    @Test
    void initialize_shouldDisableSubmitButtonInitially() {
        controller.initialize();
        assertTrue(submitButton.isDisabled(), "Submit button should be disabled initially");
    }
    
    /** Ensures the Submit button remains disabled if any required input field is empty. */
    @Test
    void checkFields_shouldDisableSubmitButton_whenAnyFieldIsEmpty() {
        controller.initialize();
        emailField.setText("test@example.com");
        sec1.setText("answer1");
        sec2.setText("answer2");
        sec3.setText("");
        passwordField.setText("Password123!");
        confirmPasswordField.setText("Password123!");

        Platform.runLater(() -> {
            assertTrue(submitButton.isDisabled(), "Submit button should be disabled when a field is empty");
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** Confirms that the Submit button is enabled only when all fields are filled. */
    @Test
    void checkFields_shouldEnableSubmitButton_whenAllFieldsAreFilled() {
        controller.initialize();

        emailField.setText("test@example.com");
        sec1.setText("answer1");
        sec2.setText("answer2");
        sec3.setText("answer3");
        passwordField.setText("Password123!");
        confirmPasswordField.setText("Password123!");

        Platform.runLater(() -> {
            assertFalse(submitButton.isDisabled(), "Submit button should be enabled when all fields are filled");
        });

        WaitForAsyncUtils.waitForFxEvents();
    }

    /** Validates that the email is correctly set via setEmail(). */
    @Test
    void setEmail_shouldSetEmailFieldText() {
        controller.setEmail("test@example.com");
        assertEquals("test@example.com", emailField.getText(), "Email field should be set by setEmail()");
    }

    /** Ensures an error is shown if the reset button is clicked with empty fields. */
    @Test
    void handleResetPassword_shouldShowError_whenFieldsAreEmpty() {
        controller.handleResetPassword();
        verify(mockAlert).showError(any(), any(), contains("Please fill in all fields"));
    }

    /** Shows an error when the provided email does not match any user. */
    @Test
    void handleResetPassword_shouldShowError_whenUserNotFound() {
        emailField.setText("test@example.com");
        sec1.setText("a"); sec2.setText("b"); sec3.setText("c");
        passwordField.setText("Password123!");
        confirmPasswordField.setText("Password123!");
        when(mockUserService.getUserByEmail("test@example.com")).thenReturn(null);
        controller.handleResetPassword();
        verify(mockAlert).showError(any(), any(), contains("No user found"));
    }

    /** Shows an error when the security answers do not match the stored ones. */
    @Test
    void handleResetPassword_shouldShowError_whenSecurityAnswersFail() {
        mockValidForm();
        user mockUser = new user();
        mockUser.setUserID(42);
        when(mockUserService.getUserByEmail(any())).thenReturn(mockUser);
        when(mockUserService.verifySecurityAnswers(eq(42), any(), any(), any())).thenReturn(false);

        controller.handleResetPassword();

        verify(mockAlert).showError(
        	    eq("Error"),
        	    eq("Security Answers Mismatch"),
        	    eq("At security answers are incorrect.")
        	);
    }

    /** Shows an error if the entered passwords do not match. */
    @Test
    void handleResetPassword_shouldShowError_whenPasswordsDoNotMatch() {
        mockValidForm();
        confirmPasswordField.setText("Mismatch123!");

        user mockUser = new user();
        mockUser.setUserID(42);
        when(mockUserService.getUserByEmail(any())).thenReturn(mockUser);
        when(mockUserService.verifySecurityAnswers(anyInt(), any(), any(), any())).thenReturn(true);

        controller.handleResetPassword();
        verify(mockAlert).showError(
                eq("Error"),
                eq("Password Mismatch"),
                eq("The new passwords do not match.")
            );
    }

    /** Shows an error when the password does not meet the strength criteria. */
    @Test
    void handleResetPassword_shouldShowError_whenPasswordIsWeak() {
        mockValidForm();
        passwordField.setText("weak");
        confirmPasswordField.setText("weak");

        user mockUser = new user();
        mockUser.setUserID(42);
        when(mockUserService.getUserByEmail(any())).thenReturn(mockUser);
        when(mockUserService.verifySecurityAnswers(anyInt(), any(), any(), any())).thenReturn(true);
        when(mockUserService.isPasswordStrong("weak")).thenReturn(false);

        controller.handleResetPassword();
        verify(mockAlert).showError(any(), any(), contains("Password must be at least 8 characters"));
    }

    /** Shows success message and navigates to login screen if password is reset successfully. */
    @Test
    void handleResetPassword_shouldShowSuccess_whenPasswordIsUpdated() {
        mockValidForm();
        user mockUser = new user();
        mockUser.setUserID(42);

        when(mockUserService.getUserByEmail(any())).thenReturn(mockUser);
        when(mockUserService.verifySecurityAnswers(anyInt(), any(), any(), any())).thenReturn(true);
        when(mockUserService.isPasswordStrong(any())).thenReturn(true);
        when(mockUserService.updateForgottenPassword(eq(42), any())).thenReturn(true);

        controller.handleResetPassword();
        verify(mockAlert).showInfo(any(), any(), contains("Your password has been reset successfully"));
        verify(mockNav).switchToLoginPage(submitButton);
    }

    /** Displays an error if password update fails in the backend. */
    @Test
    void handleResetPassword_shouldShowError_whenPasswordUpdateFails() {
        mockValidForm(); 
        user mockUser = new user();
        mockUser.setUserID(42);

        when(mockUserService.getUserByEmail(any())).thenReturn(mockUser);
        when(mockUserService.verifySecurityAnswers(anyInt(), any(), any(), any())).thenReturn(true);
        when(mockUserService.isPasswordStrong(any())).thenReturn(true);
        when(mockUserService.updateForgottenPassword(eq(42), any())).thenReturn(false);

        controller.handleResetPassword();
        verify(mockAlert).showError(any(), any(), contains("There was an error updating your password"));
    }

    /** Ensures the back button navigates the user back to the login page. */
    @Test
    void handleBackButton_shouldNavigateToLogin() {
        controller.handleBackButton();
        verify(mockNav).switchToLoginPage(backButton);
    }

    /**
     * Helper method to populate all form fields with valid data.
     */
    private void mockValidForm() {
        emailField.setText("test@example.com");
        sec1.setText("a"); sec2.setText("b"); sec3.setText("c");
        passwordField.setText("Password123!");
        confirmPasswordField.setText("Password123!");
    }
}

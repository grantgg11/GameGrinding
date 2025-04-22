package controllers;

import services.userService;
import utils.AlertHelper;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Controller class for handling the user registration process.
 * 
 * Provides logic to collect user credentials and security answers,
 * validates inputs, registers the user through the userService,
 * and navigates back to the login screen upon success.
 */
public class RegistrationController {
	
    // ---------------------- FXML UI Components ----------------------
    @FXML private TextField usernameField;           // Input field for username
    @FXML private TextField emailField;              // Input field for user email
    @FXML private PasswordField passwordField;       // Input field for user password
    @FXML private TextField SecurityQuestion1;       // First security question input
    @FXML private TextField SecurityQuestion2;       // Second security question input
    @FXML private TextField SecurityQuestion3;       // Third security question input
    @FXML private Button registerButton;             // Button to submit registration
    @FXML private Label errorLabel;                  // Label to show form errors
    @FXML private Button backButton;                 // Button to return to login page

    // ---------------------- Services and Helpers ----------------------
    private final userService userService = new userService();  // Business logic service for user registration
    private final NavigationHelper navHelper = new NavigationHelper(); // Utility for screen transitions
    private final AlertHelper alert = new AlertHelper(); // Utility for displaying alerts
    
    
    /**
     * Initializes the controller. Clears error label at startup.
     */
	@FXML
	private void initialize() {
		errorLabel.setText("");
	}
	
	/**
	 * Handles the action of clicking the register button.
	 * 
	 * Validates input fields, calls the userService to register the user,
	 * and navigates back to the login screen upon success.
	 */
	@FXML
	private void handleRegister() {
		String username = usernameField.getText();
		String email = emailField.getText();
		String password = passwordField.getText();
		String securityQuestion1 = SecurityQuestion1.getText();
		String securityQuestion2 = SecurityQuestion2.getText();
		String securityQuestion3 = SecurityQuestion3.getText();
		
		// Input validation
		if (username.isEmpty() || email.isEmpty() || password.isEmpty() || SecurityQuestion1.getText().isEmpty() || SecurityQuestion2.getText().isEmpty() || SecurityQuestion3.getText().isEmpty()) {
			alert.showError("Error", "All fields are required.", "Please fill in all fields.");
			return;
		}
		
		try {
			if(userService.registerUser(username, email, password, securityQuestion1, securityQuestion2, securityQuestion3)) {
				System.out.println("Registration successful!");
				alert.showInfo("Success", "Registration Successful", "You can now log in with your credentials.");
				navHelper.switchToLoginPage(registerButton);
			} else {
				alert.showError("Error", "Registration Failed", "Username or email already exists.");
			}
		} catch (Exception e) {
			e.printStackTrace();
			alert.showError("Error", "Registration Error", "An error occurred during registration.");
		}
	}
	
	/**
	 * Handles the action of clicking the back button.
	 * 
	 * Navigates back to the login screen.
	 */
	@FXML
	private void handleBackButton() {
		navHelper.switchToLoginPage(backButton);
		System.out.println("Back to login page");
	}

}

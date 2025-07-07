package controllers;


import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import services.userService;
import utils.AlertHelper;

/**
 * Controller for the Login screen.
 * Handles user authentication and navigation to other pages such as registration, password reset, and game collection page.
 */
public class LoginController {
	
    // ---------- FXML UI Components ----------
    @FXML private TextField emailField;               // Field for user email input
    @FXML private PasswordField passwordField;        // Field for user password input
    @FXML private Button submitButton;                // Login button
    @FXML private Label errorLabel;                   // Displays validation or login error messages
    @FXML private Button createAccountButton;         // Button to navigate to registration
    @FXML private Button forgotPasswordButton;        // Button to navigate to password recovery

    // ---------- Services ----------
    private final userService userService = new userService();     // Handles authentication and user management
    private final AlertHelper alert = new AlertHelper();       // Displays alerts for login issues
    private final NavigationHelper navHelp = new NavigationHelper(); // Handles screen transitions
    
    /**
     * Initializes the LoginController.
     * Clears any lingering error messages on startup.
     */
    @FXML protected void initialize() {
    	errorLabel.setText("");
	}
    
    /**
	 * Handles the action of clicking the login button.
	 * Validates user input and attempts to authenticate the user.
	 * 
	 * If successful, navigates to the game collection page.
	 * If failed, displays an error message.
	 */
    @FXML
    protected void handleLogin() {
    		System.out.println("Submit button pressed");
    		String email = emailField.getText();
    		String password = passwordField.getText();
    		
    		//Validate input
    		if (email.isEmpty() || password.isEmpty()) {
    			showError("Please fill in all fields.");
    			return;
    		}
        
    		try {
    			if(userService.authenticateUser(email, password)) {
    				System.out.println("Login successful!");
    				int userID = userService.getCurrentUserID();
    				if (userID > 0) {
    	                navHelp.switchToGameCollection(userID, submitButton);
    	            } else {
    	                System.err.println("Error: Retrieved invalid user ID from userService.");
    	            }
    			} else {
    				alert.showError("Login Failed", "Invalid email or password. Please try again.", "");
    			}
    		}catch (Exception e) {
    			e.printStackTrace();
    		}
    }
        
    /**
	 * Handles the action of clicking the Create Account button.
	 * Navigates to the registration page.
	 */
    @FXML
    protected void openRegistration() {
        	System.out.println("Create Account button pressed");
        	
			try {
				FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Registration.fxml"));
				Parent registrationRoot = loader.load();
				
				Stage stage = (Stage) createAccountButton.getScene().getWindow();
				
				Scene scene = new Scene(registrationRoot);
				stage.setScene(scene);
				stage.setTitle("Create Account");
				stage.show();
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println("Error: Could not load Registration.fxml!");
			}
    }
    
    /**
     * Opens the Forgot Password screen when the Forgot Password button is clicked.
     */
    @FXML
    protected void openForgotPassword() {
		System.out.println("Forgot Password button pressed");
		try {
			navHelp.switchToForgotPasswordPage(forgotPasswordButton);
			} catch (Exception e) {
				e.printStackTrace();
				System.err.println("Error: Could not switch to Forgot Password page!");
				showError("Failed to open Forgot Password page.");
			}
		
	}

    /**
     * Displays an error message in the error label.
     *
     * @param message the message to show
     */
    private void showError(String message) {
		errorLabel.setText(message);
	} 
}


package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import models.user;
import services.userService;
import utils.AlertHelper;

/**
 * Controller for the Forgot Password screen.
 * 
 * This class handles the user interface interactions for resetting a user's password
 * using security questions and answers.
 */
public class ForgotPasswordController {
	
	//-----------FXML Fields-------------------
	@FXML private TextField emailField;				//User's registered email
	@FXML private TextField secuirityAnswerField1;	//First security question answer input
	@FXML private TextField secuirityAnswerField2;	//Second security question answer input
	@FXML private TextField secuirityAnswerField3;	//Third security question answer input
	@FXML private TextField PasswordField;			//New password input
	@FXML private TextField confirmPasswordField;	//Confirm new password input
	@FXML private Button submitButton;				//Submit button to reset password
	@FXML private Button backButton;				//Back button to navigate to login page
	
	//-----------Services-------------------
	public AlertHelper alert = new AlertHelper();	// Utility for displaying alert messages
	public userService userSer = new userService(); // Service for user authentication and data
	public NavigationHelper navHelper = new NavigationHelper(); // Handles scene transitions

	
	/**
	 * Initializes the ForgotPasswordController.
     * Disables the submit button by default and enables it only when all fields are filled.   
	 */
	public void initialize() {
	    submitButton.setDisable(true);

	    // Add listeners to required fields to enable the button dynamically
	    emailField.textProperty().addListener((obs, oldVal, newVal) -> checkFields());
	    secuirityAnswerField1.textProperty().addListener((obs, oldVal, newVal) -> checkFields());
	    secuirityAnswerField2.textProperty().addListener((obs, oldVal, newVal) -> checkFields());
	    secuirityAnswerField3.textProperty().addListener((obs, oldVal, newVal) -> checkFields());
	    PasswordField.textProperty().addListener((obs, oldVal, newVal) -> checkFields());
	    confirmPasswordField.textProperty().addListener((obs, oldVal, newVal) -> checkFields());
	}
	
	/**
	 * Checks whether all input fields are filled.
     * Enables the submit button if true; otherwise disables it.
	 */
	private void checkFields() {
	    boolean allFilled = 
	        !emailField.getText().isEmpty() &&
	        !secuirityAnswerField1.getText().isEmpty() &&
	        !secuirityAnswerField2.getText().isEmpty() &&
	        !secuirityAnswerField3.getText().isEmpty() &&
	        !PasswordField.getText().isEmpty() &&
	        !confirmPasswordField.getText().isEmpty();

	    submitButton.setDisable(!allFilled);
	}

	/**
	 * Sets the email field with the provided email address.
	 * 
	 * @param email The email address to set in the email field
	 */		
	public void setEmail(String email) {
		emailField.setText(email);
	}
	
	/**
	 * Handles the action of clicking the submit button to reset the password.
	 * 
	 * Validates the input fields and updates the password if all checks pass.
	 * - Validates required fields
     * - Checks if user exists
     * - Verifies security answers
     * - Checks password match and strength
     * - Updates the password in the database
	 */
	@FXML
	protected void handleResetPassword() {
		String email = emailField.getText();
		String answer1 = secuirityAnswerField1.getText();
		String answer2 = secuirityAnswerField2.getText();
		String answer3 = secuirityAnswerField3.getText();
		String newPassword = PasswordField.getText();
		String confirmPassword = confirmPasswordField.getText();
		
		StringBuilder errorMessages = new StringBuilder();
		
		// Validation: Ensure no field is left empty
		if (email.isEmpty() || answer1.isEmpty() || answer2.isEmpty() || answer3.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
			errorMessages.append("- All fields must be filled out.\n");
		}
		//Retrieve the user by email
		user currentUser = userSer.getUserByEmail(email);
		
	    if (!email.isEmpty() && currentUser == null) {
	        errorMessages.append("- No user found with the provided email.\n");
	    }
		
	    // Only check security answers if user exists
	    if (currentUser != null && !userSer.verifySecurityAnswers(currentUser.getUserID(), answer1, answer2, answer3)) {
	        errorMessages.append("- One or more security answers are incorrect.\n");
	    }
		
	    // Check if the new password and confirm password match
	    if (!newPassword.equals(confirmPassword)) {
	        errorMessages.append("- New password and confirmation do not match.\n");
	    }
	    
	    // Check if the new password is strong enough
	    if (!userSer.isPasswordStrong(newPassword)) {
	        errorMessages.append("- Password must be at least 8 characters long, contain uppercase and lowercase letters, numbers, and special characters.\n");
	    }
	    
	    // If there are validation errors, show one grouped alert
	    if (errorMessages.length() > 0) {
	        System.out.println("Validation failed:\n" + errorMessages);
	        alert.showError("Error", "Password Reset Failed", errorMessages.toString());
	        return;
	    }
	    
		// Update the password in the database
		if(userSer.updateForgottenPassword(currentUser.getUserID(), newPassword)) {
			alert.showInfo("Success", "Password Reset Successful", "Your password has been reset successfully. You can now log in with your new password.");
			navHelper.switchToLoginPage(submitButton);
		} else {
			alert.showError("Error", "Password Update Failed", "There was an error updating your password. Please try again later.");
			return;
		}

		System.out.println("Password reset for: " + email); 
	}
	
	/**
	 * Handles the action of clicking the back button to navigate to the login page.
	 * 
	 * Closes the current window and opens the login page.
	 */
	@FXML 
	protected void handleBackButton() {
		navHelper.switchToLoginPage(backButton);
		System.out.println("Navigating back to login page.");
	}
	

}

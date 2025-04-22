package controllers;

import java.util.List;

import javax.crypto.SecretKey;

import database.ReportDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import models.APIRequestLog;
import models.DatabaseIntegrityReport;
import models.SystemPerformanceLog;
import models.user;
import security.Encryption;
import services.userService;
import utils.AlertHelper;
import utils.CSVExporter;
import utils.KeyStorage;

/**
 * Controller for managing the Settings page.
 * 
 * Handles user profile updates (username, email, password), 
 * encryption information display, navigation, and admin report exports.
 */
public class SettingsController extends BaseController {

    // ---------------------- FXML UI Components ----------------------
    @FXML private TextField usernameField;              // Input for username
    @FXML private TextField emailField;                 // Input for email (encrypted at rest)
    @FXML private PasswordField newPasswordField;       // Input for new password
    @FXML private PasswordField confirmPasswordField;   // Input for confirming new password
    @FXML private TextField currentPasswordField;       // First confirmation of current password
    @FXML private TextField currentPasswordField2;      // Second confirmation of current password
    @FXML private Button saveAccountButton;             // Button to save username/email updates
    @FXML private Button savePasswordButton;            // Button to save password changes
    @FXML private Button collectionPageButton;          // Nav to game collection
    @FXML private Button settingsButton;                // Refresh current page
    @FXML private Button helpButton;                    // Nav to help page
    @FXML private Button reportsButton;                 // Export reports (admin only)
    @FXML private VBox encryptionVBox;                  // Container for encryption info
    @FXML private Button logoutButton;                  // Logout trigger

    // ---------------------- Services & Helpers ----------------------
    private final userService userSer = new userService();
    private final NavigationHelper navHelp = new NavigationHelper();
    private final Encryption encryption = new Encryption();
    private final AlertHelper alertHelper = new AlertHelper();
	
    /**
     * Initializes the Settings screen UI.
     * Populates encryption section
     * Controls visibility of the Reports button based on user role.
     */
	@FXML
	private void initialize() {
		populateEncryptionInfo();
	    javafx.application.Platform.runLater(() -> {
	        if (reportsButton == null) {
	            System.err.println("reportsButton is null in initialize!");
	            return;
	        }

	        String role = userService.getInstance().getCurrentUserRole();
	        System.out.println("Role during settings init: " + role);

	        if (!"Admin".equalsIgnoreCase(role)) {
	            reportsButton.setVisible(false);
	            reportsButton.setManaged(false);
	        } else {
	            reportsButton.setVisible(true);
	            reportsButton.setManaged(true);
	        }
	    });
	}
	
	/**
	 * Called when user data is loaded into the controller.
	 * 
	 * Preloads user information (username, email) into the UI fields.
	 * Decrypts email using the stored encryption key.
	 */
    @Override
    protected void onUserDataLoad() {
        try {
            user currentUser = userSer.getUserByID(loggedInUserID);
            if (currentUser != null) {
                usernameField.setText(currentUser.getUsername());
                SecretKey secretKey = KeyStorage.getEncryptionKey();
                String email = encryption.decrypt(currentUser.getEmail(), secretKey);
                emailField.setText(email);
            } else {
                System.out.println("User not found for ID: " + loggedInUserID);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to preload user info: " + e.getMessage());
        }
    }

    /**
	 * Handles the action of clicking the save account button.
	 * 
	 * Validates input fields and updates username/email in the database.
	 */
    @FXML
    private void handleSaveAccount() {
		String username = usernameField.getText();
		String email = emailField.getText();
		
		if (username.isEmpty() || email.isEmpty()) {
			alertHelper.showError("Account Update Failed", "All fields are required.", "");
			return;
		}		
		if (userSer.updateAccount(loggedInUserID, username, email)) {
			alertHelper.showInfo("Account Update Successful", "Your account has been updated successfully.", "");
		} else {
			System.out.println("Account update failed.");
		}
	}
    
    /**
     * Handles password change logic.
     * Requires matching current and new passwords, and validates for completeness.
     */
    @FXML 
    private void handleSavePassword() {
		String newPassword = newPasswordField.getText();
		String confirmPassword = confirmPasswordField.getText();
		String currentPassword = currentPasswordField.getText();
		String currentPassword2 = currentPasswordField2.getText();
		
		if (newPassword.isEmpty() || confirmPassword.isEmpty() || currentPassword.isEmpty() || currentPassword2.isEmpty()) {
			alertHelper.showError("Password Update Failed", "All fields are required.", "");            
			return;
		}
		if (!newPassword.equals(confirmPassword)) {
			alertHelper.showError("Password Update Failed", "New Passwords do not match.", "");           
			return;
		}
		if (!currentPassword.equals(currentPassword2)) {
			alertHelper.showError("Password Update Failed", "Current Passwords do not match.", "");
			return;
		}
		if (userSer.updatePassword(loggedInUserID, newPassword, currentPassword)) {
			alertHelper.showInfo("Password Update Successful", "Your password has been updated successfully.", "");
		} else {
			System.out.println("Password update failed.");
			alertHelper.showError("Password Update Failed", "Current password is incorrect.", "");
		}
    }
	
    /**
     * Dynamically generates encryption policy and security info for users.
     * This content is displayed in the Data Encryption section of the settings page.
     */
    private void populateEncryptionInfo() {
        String[] sections = {
            "How We Protect Your Data",
            "At GameGrinding, the security and privacy of your personal information is a top priority. We implement multiple layers of protection using industry-standard encryption techniques to ensure your data remains secure both in transit and at rest.",

            "In Transit",
            "All communication with external services, such as the MobyGames API, is performed over secure HTTPS connections. This makes sure that any data transferred over the internet is encrypted and cannot be intercepted by unauthorized third parties.",

            "At Rest",
            "Your sensitive data stored on your device is protected using the Advanced Encryption Standard (AES) with 256-bit keys. This includes:\n\n• Emails are encrypted using AES-256 before being saved to the local database.\n\n• Passwords and security answers are hashed using the bcrypt algorithm. We choose this since it is a widely adopted industry standard resistant to brute-force attacks. We never store your password in plaintext.\n\n• Security questions are also hashed using bcrypt to add another level of protection in account recovery scenarios.",

            "Authentication Security",
            "When you log in, your credentials are validated by comparing your entered password with the securely stored hash. This means we never decrypt or reveal your actual password so that your authentication is safe from tampering or replay attacks."
        };

        for (int i = 0; i < sections.length; i++) {
            Label label = new Label(sections[i]);
            label.setWrapText(true);
            if (i % 2 == 0) {
                label.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            } else {
                label.setStyle("-fx-font-size: 12px;");
            }
            encryptionVBox.getChildren().add(label);
        }
    }
    
    // ---------------------- Navigation Buttons ----------------------
		
    /**
	 * Handles the action of clicking the collection page button.
	 * 
	 * Navigates to the game collection page.
	 */
	@FXML
	private void handleCollectionPageButton() {
		try {
			navHelp.switchToGameCollection(loggedInUserID, collectionPageButton);
			System.out.println("Switching to collection page for user: " + loggedInUserID);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error occurred while switching to Collection Page: " + e.getMessage());
		}	
    }
	
	/**
	 * Handles the action of clicking the settings button.
	 * 
	 * Refreshes the current settings page.
	 */
	@FXML
	private void handleSettingsButton() {
		try {
			navHelp.switchToSettingsPage(loggedInUserID, settingsButton);
			System.out.println("Switching to settings page for user: " + loggedInUserID);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error occurred while switching to Settings Page: " + e.getMessage());
		}	
    }
	
	/**
	 * Handles the action of clicking the help button.
	 * Navigates to the help page.
	 */
    @FXML
    private void handleHelpButton() {
		try {
			navHelp.switchToHelpPage(loggedInUserID, helpButton);
			System.out.println("Switching to Help Page.");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error occurred while switching to help page: " + e.getMessage());
		}
	}
    
    /**
	 * Handles the action of clicking the reports button.
	 * Only visible to admin users.
	 * Exports all reports to CSV files.
	 */
    @FXML
    private void handleReportButton() {
        System.out.println("Exporting all reports to CSV...");

        ReportDAO reportDAO = new ReportDAO();  // Use your existing DAO
        CSVExporter exporter = new CSVExporter();

        try {
            // Fetch data
            List<SystemPerformanceLog> perfLogs = reportDAO.getAllSystemPerformanceReports();
            List<APIRequestLog> apiLogs = reportDAO.getAllAPIRequestLogs();
            List<DatabaseIntegrityReport> dbReports = reportDAO.getAllDatabaseIntegrityReports();

            // Define file paths 
            String perfPath = "exports/SystemPerformanceLogs.csv";
            String apiPath = "exports/APIRequestLogs.csv";
            String integrityPath = "exports/DatabaseIntegrityReports.csv";

            // Create the export directory if it doesn't exist
            java.io.File exportDir = new java.io.File("exports");
            if (!exportDir.exists()) {
                exportDir.mkdir();
            }

            // Export each report
            exporter.exportSystemPerformanceLogs(perfLogs, perfPath);
            exporter.exportAPIRequestLogs(apiLogs, apiPath);
            exporter.exportDatabaseIntegrityReports(dbReports, integrityPath);
            alertHelper.showInfo("Export Complete", "All reports have been exported successfully.", "Reports have been saved in the 'exports' folder.");
        } catch (Exception e) {
            e.printStackTrace();
            alertHelper.showError("Export Failed", "An error occurred while exporting reports", "");

        }
    }
    
    /**
     * Handles the action of clicking the logout button.
     * Logs out the user and navigates to the login page.
     */
    @FXML 
    private void handleLogoutButton() {
		try {
			userSer.logout();
			navHelp.switchToLoginPage(logoutButton);
			System.out.println("User logged out successfully.");
			alertHelper.showInfo("Logout Successful", "You have been logged out successfully.", "Thank you for using GameGrinding");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error during logout: " + e.getMessage());
			alertHelper.showError("Logout Error", "An error occurred while logging out. Please try again.", e.getMessage());
		}
	}

 

}

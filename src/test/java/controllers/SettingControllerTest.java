package controllers;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.util.List;

import javax.crypto.SecretKey;
 
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.control.PasswordField;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import models.APIRequestLog;
import models.DatabaseIntegrityReport;
import models.SystemPerformanceLog;
import models.user;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import database.ReportDAO;
import security.Encryption;
import services.userService;
import utils.AlertHelper;
import utils.CSVExporter;
import utils.KeyStorage;

/**
 * Test class for SettingsController. Verifies the functionality of 
 * user settings management, encryption info display, navigation logic, 
 * account/password updates, and report exporting using mocked dependencies.
 */
@ExtendWith(JavaFXThreadingExtension.class)
class SettingControllerTest {

    private SettingsController controller;

    private userService mockUserService;
    private Encryption mockEncryption;
    private AlertHelper mockAlertHelper;

    private TextField usernameField;
    private TextField emailField;
    private PasswordField newPasswordField;
    private PasswordField confirmPasswordField;
    private TextField currentPasswordField;
    private TextField currentPasswordField2;

    /**
	 * Initializes the SettingsController and injects mocked dependencies before each test.
	 * Sets up JavaFX controls to simulate the settings screen environment.
	 */
    @BeforeEach
    void setUp() {
        controller = new SettingsController();

        mockUserService = mock(userService.class);
        mockEncryption = mock(Encryption.class);
        mockAlertHelper = mock(AlertHelper.class);

        TestUtils.setPrivateField(controller, "userSer", mockUserService);
        TestUtils.setPrivateField(controller, "encryption", mockEncryption);
        TestUtils.setPrivateField(controller, "alertHelper", mockAlertHelper);

        usernameField = new TextField();
        emailField = new TextField();
        newPasswordField = new PasswordField();
        confirmPasswordField = new PasswordField();
        currentPasswordField = new TextField();
        currentPasswordField2 = new TextField();

        controller.setUsernameField(usernameField);
        TestUtils.setPrivateField(controller, "emailField", emailField);
        TestUtils.setPrivateField(controller, "newPasswordField", newPasswordField);
        TestUtils.setPrivateField(controller, "confirmPasswordField", confirmPasswordField);
        TestUtils.setPrivateField(controller, "currentPasswordField", currentPasswordField);
        TestUtils.setPrivateField(controller, "currentPasswordField2", currentPasswordField2);

        TestUtils.setPrivateField(controller, "loggedInUserID", 1);
    }
    ///////////////////////// testing initialize /////////////////////////
    
    /**
	 * Tests the initialize method to ensure it clears any previous error messages.
	 * Also verifies that the reports button is hidden by default.
	 * 
	 * @throws Exception if any error occurs during initialization 
	 */
    @Test
    void testInitialize_withAdminRole_shouldShowReportsButton() throws Exception {
        Button reportsBtn = new Button();
        VBox encryptionBox = new VBox();

        TestUtils.setPrivateField(controller, "reportsButton", reportsBtn);
        TestUtils.setPrivateField(controller, "encryptionVBox", encryptionBox);

        try (MockedStatic<userService> mockStaticUserService = Mockito.mockStatic(userService.class)) {
            userService mockUserService = mock(userService.class);
            mockStaticUserService.when(userService::getInstance).thenReturn(mockUserService);
            when(mockUserService.getCurrentUserRole()).thenReturn("Admin");

            Platform.runLater(() -> controller.initialize());
            Thread.sleep(200); 

            assertTrue(reportsBtn.isVisible());
            assertTrue(reportsBtn.isManaged());
        }
    }
        
    /**
     * Tests the initialize method to not throw any exceptions when the reports button is null.
     */
    @Test
    void testInitialize_withNullReportsButton_shouldNotThrow() {
        assertDoesNotThrow(() -> Platform.runLater(() -> controller.initialize()));
    }
 

    ///////////////////////// testing onUserDataLoad /////////////////////////
    
    /**
	 * Tests the onUserDataLoad method to ensure it correctly loads user data and decrypts the email.
	 * Verifies that the username and email fields are set correctly after loading.
	 * 
	 * @throws Exception if any error occurs during user data loading or decryption
	 */
    @Test
    void testOnUserDataLoad_userFound_successfulDecryption() throws Exception {
        SettingsController controller = new SettingsController();
        TestUtils.setPrivateField(controller, "loggedInUserID", 1); 

        TextField mockUsernameField = new TextField();
        TextField mockEmailField = new TextField();
        controller.setUsernameField(mockUsernameField);
        TestUtils.setPrivateField(controller, "emailField", mockEmailField);

        user mockUser = mock(user.class);
        when(mockUser.getUsername()).thenReturn("testuser");
        when(mockUser.getEmail()).thenReturn("encrypted@example.com");

        userService mockUserService = mock(userService.class);
        when(mockUserService.getUserByID(1)).thenReturn(mockUser);
        TestUtils.setPrivateField(controller, "userSer", mockUserService);

        SecretKey mockKey = mock(SecretKey.class);

        try (MockedStatic<KeyStorage> mockedKeyStorage = mockStatic(KeyStorage.class)) {
            mockedKeyStorage.when(KeyStorage::getEncryptionKey).thenReturn(mockKey);

            try (MockedStatic<Encryption> mockedEncryption = mockStatic(Encryption.class)) {
                mockedEncryption.when(() -> Encryption.decrypt("encrypted@example.com", mockKey)).thenReturn("decrypted@example.com");
                controller.onUserDataLoad();
                assertEquals("testuser", controller.getUsernameField().getText(), "Username should be set correctly");
                assertEquals("decrypted@example.com", mockEmailField.getText(), "Email should be decrypted and set");
            }
        }
    }
    
    /**
     * Tests the onUserDataLoad method when the user is not found.
     * Verifies that the username and email fields remain empty.
     * 
     * @throws Exception if any error occurs during user data loading
     */
    @Test
    void testOnUserDataLoad_userNotFound_shouldNotSetFields() throws Exception {
        when(mockUserService.getUserByID(1)).thenReturn(null);

        controller.onUserDataLoad();

        assertEquals("", controller.getUsernameField().getText());
        assertEquals("", emailField.getText()); 
    }

    /**
	 * Tests the onUserDataLoad method when the user service throws an exception.
	 * Verifies that the method handles the exception gracefully and does not crash.
	 * 
	 * @throws Exception if any error occurs during user data loading
	 */
    @Test
    void testOnUserDataLoad_userServiceThrowsException_shouldHandleGracefully() throws Exception {
        when(mockUserService.getUserByID(1)).thenThrow(new RuntimeException("Database failure"));

        assertDoesNotThrow(() -> controller.onUserDataLoad());

        assertEquals("", controller.getUsernameField().getText());
        assertEquals("", emailField.getText());
    }

    /**
	 * Tests the onUserDataLoad method when decryption throws an exception.
	 * Verifies that the method handles the exception gracefully and does not crash.
	 * 
	 * @throws Exception if any error occurs during user data loading or decryption
	 */
    @Test
    void testOnUserDataLoad_decryptionThrowsException_shouldHandleGracefully() throws Exception {
        SettingsController controller = new SettingsController();
        TestUtils.setPrivateField(controller, "loggedInUserID", 1);

        TextField mockUsernameField = new TextField();
        TextField mockEmailField = new TextField();
        controller.setUsernameField(mockUsernameField);
        TestUtils.setPrivateField(controller, "emailField", mockEmailField);

        user mockUser = mock(user.class);
        when(mockUser.getUsername()).thenReturn("testuser");
        when(mockUser.getEmail()).thenReturn("encrypted@example.com");

        userService mockUserService = mock(userService.class);
        when(mockUserService.getUserByID(1)).thenReturn(mockUser);
        TestUtils.setPrivateField(controller, "userSer", mockUserService);

        SecretKey mockKey = mock(SecretKey.class);
        try (MockedStatic<KeyStorage> mockedKeyStorage = mockStatic(KeyStorage.class)) {
            mockedKeyStorage.when(KeyStorage::getEncryptionKey).thenReturn(mockKey);

            try (MockedStatic<Encryption> mockedEncryption = mockStatic(Encryption.class)) {
                mockedEncryption.when(() -> Encryption.decrypt("encrypted@example.com", mockKey)).thenThrow(new RuntimeException("Decryption error"));

                assertDoesNotThrow(controller::onUserDataLoad, "Method should handle decryption exception gracefully");

                assertEquals("testuser", controller.getUsernameField().getText(), "Username should be set even if decryption fails");
                assertEquals("", mockEmailField.getText(), "Email field should remain empty on decryption failure");
            }
        }
    }
    
	///////////////////////// testing handleSaveAccount /////////////////////////

    /**
	 * Tests the handleSaveAccount method with empty fields.
	 * Verifies that it shows an error alert indicating that all fields are required.
	 */
    @Test
    void testHandleSaveAccount_withEmptyFields_shouldShowError() {
        controller.setUsernameField(new TextField(""));
        emailField.setText("");
        controller.handleSaveAccount();
        verify(mockAlertHelper).showError(eq("Account Update Failed"), eq("All fields are required."), anyString());
    }

    /**
     * Tests the handleSaveAccount method when successfully updating the account.
     * Verifies that it shows an info alert indicating a successful update.
     */
    @Test
    void testHandleSaveAccount_successfulUpdate_shouldShowInfo() {
        controller.setUsernameField(new TextField("updatedUser"));
        emailField.setText("updated@example.com");
        when(mockUserService.updateAccount(1, "updatedUser", "updated@example.com")).thenReturn(true);
        controller.handleSaveAccount();
        verify(mockAlertHelper).showInfo(eq("Account Update Successful"), anyString(), anyString());
    }
    
    /**
	 * Tests the handleSaveAccount method when the update fails.
	 * Verifies that it shows an error alert indicating the failure.
	 */
    @Test
    void testHandleSaveAccount_updateFails_shouldShowAlert() {
        controller.setUsernameField(new TextField("badUser"));
        emailField.setText("bad@example.com");

        when(mockUserService.updateAccount(1, "badUser", "bad@example.com")).thenReturn(false);

        controller.handleSaveAccount();

        verify(mockUserService).updateAccount(1, "badUser", "bad@example.com");
        verify(mockAlertHelper, never()).showInfo(any(), any(), any());
        verify(mockAlertHelper, never()).showError(any(), any(), any());
    }

    ///////////////////////// testing handleSavePassword /////////////////////////
    
    /**
     * Tests the handleSavePassword method with empty fields.
     * Verifies that it shows an error alert indicating that all fields are required.
     */
    @Test
    void testHandleSavePassword_fieldsEmpty_shouldShowError() {
        newPasswordField.setText("");
        confirmPasswordField.setText("new123");
        currentPasswordField.setText("old123");
        currentPasswordField2.setText("old123");

        controller.handleSavePassword();
        verify(mockAlertHelper).showError(eq("Password Update Failed"), eq("All fields are required."), anyString());
        verifyNoInteractions(mockUserService);
    }

    /**
	 * Tests the handleSavePassword method with mismatched new passwords.
	 * Verifies that it shows an error alert indicating the mismatch.
	 */
    @Test
    void testHandleSavePassword_newPasswordsMismatch_shouldShowError() {
        newPasswordField.setText("new123");
        confirmPasswordField.setText("new456");
        currentPasswordField.setText("old123");
        currentPasswordField2.setText("old123");

        controller.handleSavePassword();

        verify(mockAlertHelper).showError(eq("Password Update Failed"), eq("New Passwords do not match."), anyString());
    }

    /**
     * Tests the handleSavePassword method with mismatched current passwords.
     * Verifies that it shows an error alert indicating the mismatch.
     */
    @Test
    void testHandleSavePassword_currentPasswordsMismatch_shouldShowError() {
        newPasswordField.setText("new123");
        confirmPasswordField.setText("new123");
        currentPasswordField.setText("old123");
        currentPasswordField2.setText("wrongOld123");

        controller.handleSavePassword();

        verify(mockAlertHelper).showError(eq("Password Update Failed"), eq("Current Passwords do not match."), anyString());
    }

    /**
	 * Tests the handleSavePassword method when the update is successful.
	 * Verifies that it shows an info alert indicating a successful update.
	 */
    @Test
    void testHandleSavePassword_updateSuccessful_shouldShowInfo() {
        newPasswordField.setText("new123");
        confirmPasswordField.setText("new123");
        currentPasswordField.setText("old123");
        currentPasswordField2.setText("old123");

        when(mockUserService.updatePassword(1, "new123", "old123")).thenReturn(true);

        controller.handleSavePassword();

        verify(mockUserService).updatePassword(1, "new123", "old123");
        verify(mockAlertHelper).showInfo(eq("Password Update Successful"), contains("updated"), anyString());
    }

    /**
     * Tests the handleSavePassword method when the update fails due to incorrect current password.
     * Verifies that it shows an error alert indicating the failure.
     */
    @Test
    void testHandleSavePassword_updateFails_shouldShowError() {
        newPasswordField.setText("new123");
        confirmPasswordField.setText("new123");
        currentPasswordField.setText("old123");
        currentPasswordField2.setText("old123");

        when(mockUserService.updatePassword(1, "new123", "old123")).thenReturn(false);

        controller.handleSavePassword();

        verify(mockAlertHelper).showError(eq("Password Update Failed"), eq("Current password is incorrect."), anyString());
    }

    ///////////////////////// testing populateEncryptionInfo /////////////////////////
    
    /**
	 * Tests the populateEncryptionInfo method to ensure it adds all labels with correct styles.
	 * Verifies that the VBox contains 8 labels with alternating styles for headings and content.
	 */
    @Test
    void testPopulateEncryptionInfo_shouldAddAllLabelsWithCorrectStyles() {
        VBox encryptionVBox = new VBox(); // real VBox
        TestUtils.setPrivateField(controller, "encryptionVBox", encryptionVBox);

        TestUtils.invokePrivateMethod(controller, "populateEncryptionInfo");

        assertEquals(8, encryptionVBox.getChildren().size());

        for (int i = 0; i < 8; i++) {
            Node node = encryptionVBox.getChildren().get(i);
            assertTrue(node instanceof Label, "Node should be a Label");
            Label label = (Label) node;

            assertTrue(label.isWrapText(), "Label should have wrapText enabled");
            if (i % 2 == 0) {
                assertTrue(label.getStyle().contains("bold"), "Even index should be bold");
                assertTrue(label.getStyle().contains("14px"), "Even index should have font-size 14px");
            } else {
                assertTrue(label.getStyle().contains("12px"), "Odd index should have font-size 12px");
            }
        }
    }
    
    ////////////////////// testing handleCollectionPageButton /////////////////////////
    
    /**
	 * Tests the handleCollectionPageButton method to ensure it navigates to the game collection page.
	 * Verifies that the navigation helper is called with the correct parameters.
	 * 
	 * @throws Exception if any error occurs during navigation
	 */
    @Test
    void testHandleCollectionPageButton_success_shouldNavigateToCollectionPage() throws Exception {
        Button fakeButton = new Button("Fake Collection Button");
        TestUtils.setPrivateField(controller, "collectionPageButton", fakeButton);

        NavigationHelper mockNavHelp = mock(NavigationHelper.class);
        TestUtils.setPrivateField(controller, "navHelp", mockNavHelp);

        assertDoesNotThrow(() -> controller.handleCollectionPageButton());

        verify(mockNavHelp).switchToGameCollection(1, fakeButton);
    }

    /**
     * Tests the handleCollectionPageButton method when navigation fails.
     * Verifies that it handles the exception gracefully without crashing.
     * @throws Exception if any error occurs during navigation
     */
    @Test
    void testHandleCollectionPageButton_navigationThrowsException() throws Exception {
        Button fakeButton = new Button("Fake Collection Button");
        TestUtils.setPrivateField(controller, "collectionPageButton", fakeButton);

        NavigationHelper mockNavHelp = mock(NavigationHelper.class);
        doThrow(new RuntimeException("Navigation failed")).when(mockNavHelp).switchToGameCollection(anyInt(), any());

        TestUtils.setPrivateField(controller, "navHelp", mockNavHelp);

        assertDoesNotThrow(() -> controller.handleCollectionPageButton());
    }
    
    ////////////////////// testing handleSettingsButton /////////////////////////

    /**
	 * Tests the handleSettingsButton method to ensure it navigates to the settings page.
	 * Verifies that the navigation helper is called with the correct parameters.
	 * 
	 * @throws Exception if any error occurs during navigation
	 */
    @Test
    void testHandleSettingsButton_success_shouldNavigateToSettingsPage() throws Exception {
        Button fakeSettingsButton = new Button("Fake Settings Button");
        TestUtils.setPrivateField(controller, "settingsButton", fakeSettingsButton);

        NavigationHelper mockNavHelp = mock(NavigationHelper.class);
        TestUtils.setPrivateField(controller, "navHelp", mockNavHelp);

        assertDoesNotThrow(() -> controller.handleSettingsButton());

        verify(mockNavHelp).switchToSettingsPage(1, fakeSettingsButton);
    }

    /**
	 * Tests the handleSettingsButton method when navigation fails.
	 * Verifies that it handles the exception gracefully without crashing.
	 * 
	 * @throws Exception if any error occurs during navigation
	 */
    @Test
    void testHandleSettingsButton_navigationThrowsException() throws Exception {
        Button fakeSettingsButton = new Button("Fake Settings Button");
        TestUtils.setPrivateField(controller, "settingsButton", fakeSettingsButton);

        NavigationHelper mockNavHelp = mock(NavigationHelper.class);
        doThrow(new RuntimeException("Navigation failed"))
            .when(mockNavHelp).switchToSettingsPage(anyInt(), any());

        TestUtils.setPrivateField(controller, "navHelp", mockNavHelp);

        assertDoesNotThrow(() -> controller.handleSettingsButton());
    }

    ///////////////////// testing handleHelpButton /////////////////////////
    
    /**
	 * Tests the handleHelpButton method to ensure it navigates to the help page.
	 * Verifies that the navigation helper is called with the correct parameters.
	 * 
	 * @throws Exception if any error occurs during navigation
	 */
    @Test
    void testHandleHelpButton_success_shouldNavigateToHelpPage() throws Exception {
        Button helpBtn = new Button("Help");
        TestUtils.setPrivateField(controller, "helpButton", helpBtn);

        NavigationHelper mockNavHelp = mock(NavigationHelper.class);
        TestUtils.setPrivateField(controller, "navHelp", mockNavHelp);

        assertDoesNotThrow(() -> controller.handleHelpButton());

        verify(mockNavHelp).switchToHelpPage(1, helpBtn);
    }

    /**
     * Tests the handleHelpButton method when navigation fails.
     * Verifies that it handles the exception gracefully without crashing.
     * 
     * @throws Exception if any error occurs during navigation
     */
    @Test
    void testHandleHelpButton_navigationThrowsException() throws Exception {
        Button helpBtn = new Button("Help");
        TestUtils.setPrivateField(controller, "helpButton", helpBtn);

        NavigationHelper mockNavHelp = mock(NavigationHelper.class);
        doThrow(new RuntimeException("Navigation failed")).when(mockNavHelp)
            .switchToHelpPage(anyInt(), any());

        TestUtils.setPrivateField(controller, "navHelp", mockNavHelp);

        assertDoesNotThrow(() -> controller.handleHelpButton());
    }

    ///////////////////// testing handleReportButton /////////////////////////
    
    /**
	 * Tests the handleReportButton method to ensure it exports reports successfully.
	 * Verifies that the CSVExporter is called with the correct parameters and that an info alert is shown.
	 * 
	 * @throws Exception if any error occurs during report handling
	 */
    @Test
    void testHandleReportButton_success_shouldExportReports() throws Exception {
        List<SystemPerformanceLog> perfLogs = List.of(new SystemPerformanceLog());
        List<APIRequestLog> apiLogs = List.of(new APIRequestLog());
        List<DatabaseIntegrityReport> dbReports = List.of(new DatabaseIntegrityReport());

        ReportDAO mockReportDAO = mock(ReportDAO.class);
        CSVExporter mockExporter = mock(CSVExporter.class);
        AlertHelper mockAlertHelper = mock(AlertHelper.class);

        when(mockReportDAO.getAllSystemPerformanceReports()).thenReturn(perfLogs);
        when(mockReportDAO.getAllAPIRequestLogs()).thenReturn(apiLogs);
        when(mockReportDAO.getAllDatabaseIntegrityReports()).thenReturn(dbReports);

        TestUtils.setPrivateField(controller, "reportDAO", mockReportDAO);
        TestUtils.setPrivateField(controller, "csvExporter", mockExporter);
        TestUtils.setPrivateField(controller, "alertHelper", mockAlertHelper);

        controller.handleReportButton();

        verify(mockExporter).exportSystemPerformanceLogs(eq(perfLogs), contains("SystemPerformanceLogs.csv"));
        verify(mockExporter).exportAPIRequestLogs(eq(apiLogs), contains("APIRequestLogs.csv"));
        verify(mockExporter).exportDatabaseIntegrityReports(eq(dbReports), contains("DatabaseIntegrityReports.csv"));

        verify(mockAlertHelper).showInfo(contains("Export Complete"), anyString(), contains("exports"));
    }
    
    /**
	 * Tests the handleReportButton method when the export is canceled.
	 * Verifies that it shows an info alert indicating the cancellation.
	 */
    @Test
    void testHandleReportButton_exportThrowsException_shouldShowError() throws Exception {
        ReportDAO mockReportDAO = mock(ReportDAO.class);
        CSVExporter mockExporter = mock(CSVExporter.class);
        AlertHelper mockAlertHelper = mock(AlertHelper.class);

        when(mockReportDAO.getAllSystemPerformanceReports()).thenThrow(new RuntimeException("DB error"));

        TestUtils.setPrivateField(controller, "reportDAO", mockReportDAO);
        TestUtils.setPrivateField(controller, "csvExporter", mockExporter);
        TestUtils.setPrivateField(controller, "alertHelper", mockAlertHelper);
        assertDoesNotThrow(() -> controller.handleReportButton());
        verify(mockAlertHelper).showError(eq("Export Failed"), contains("error"), anyString());
        verifyNoInteractions(mockExporter);
    }
    
    /**
	 * Tests the handleReportButton method when the export directory does not exist.
	 * Verifies that it creates the directory and shows an info alert indicating success.
	 * 
	 * @throws Exception if any error occurs during report handling
	 */
    @Test
    void testHandleReportButton_exportDirDoesNotExist_shouldCreateDirectory() throws Exception {
        File tempDir = mock(File.class);
        when(tempDir.exists()).thenReturn(false);
        when(tempDir.mkdir()).thenReturn(true);

        ReportDAO mockDAO = mock(ReportDAO.class);
        CSVExporter mockExporter = mock(CSVExporter.class);
        AlertHelper mockAlertHelper = mock(AlertHelper.class);

        when(mockDAO.getAllSystemPerformanceReports()).thenReturn(List.of());
        when(mockDAO.getAllAPIRequestLogs()).thenReturn(List.of());
        when(mockDAO.getAllDatabaseIntegrityReports()).thenReturn(List.of());

        TestUtils.setPrivateField(controller, "reportDAO", mockDAO);
        TestUtils.setPrivateField(controller, "csvExporter", mockExporter);
        TestUtils.setPrivateField(controller, "alertHelper", mockAlertHelper);
        TestUtils.setPrivateField(controller, "exportDir", tempDir);

        controller.handleReportButton();

        verify(tempDir).exists();
        verify(tempDir).mkdir();
        verify(mockAlertHelper).showInfo(contains("Export Complete"), any(), any());
    }

    ///////////////////////////// testing handleLogoutButton /////////////////////////////
    
    /**
	 * Tests the handleLogoutButton method to ensure it logs out the user and navigates to the login page.
	 * Verifies that the user service's logout method is called and an info alert is shown.
	 * 
	 * @throws Exception if any error occurs during logout handling
	 */
    @Test
    void testHandleLogoutButton_success_shouldLogoutAndShowInfo() throws Exception {
        Button mockLogoutButton = new Button("Logout");
        TestUtils.setPrivateField(controller, "logoutButton", mockLogoutButton);

        userService mockUserService = mock(userService.class);
        NavigationHelper mockNavHelp = mock(NavigationHelper.class);
        AlertHelper mockAlertHelper = mock(AlertHelper.class);

        TestUtils.setPrivateField(controller, "userSer", mockUserService);
        TestUtils.setPrivateField(controller, "navHelp", mockNavHelp);
        TestUtils.setPrivateField(controller, "alertHelper", mockAlertHelper);

        controller.handleLogoutButton();

        verify(mockUserService).logout();
        verify(mockNavHelp).switchToLoginPage(mockLogoutButton);
        verify(mockAlertHelper).showInfo(eq("Logout Successful"), contains("logged out"), contains("GameGrinding"));
    }

    /**
     * Tests the handleLogoutButton method when logout fails.
     * Verifies that it shows an error alert indicating the failure and does not navigate to the login page.
     * 
     * @throws Exception if any error occurs during logout handling
     */
    @Test
    void testHandleLogoutButton_logoutFails_shouldShowError() throws Exception {
        Button mockLogoutButton = new Button("Logout");
        TestUtils.setPrivateField(controller, "logoutButton", mockLogoutButton);

        userService mockUserService = mock(userService.class);
        NavigationHelper mockNavHelp = mock(NavigationHelper.class);
        AlertHelper mockAlertHelper = mock(AlertHelper.class);

        doThrow(new RuntimeException("Logout failure")).when(mockUserService).logout();

        TestUtils.setPrivateField(controller, "userSer", mockUserService);
        TestUtils.setPrivateField(controller, "navHelp", mockNavHelp);
        TestUtils.setPrivateField(controller, "alertHelper", mockAlertHelper);

        assertDoesNotThrow(() -> controller.handleLogoutButton());

        verify(mockUserService).logout();
        verify(mockAlertHelper).showError(eq("Logout Error"), contains("error"), contains("Logout failure"));
        verify(mockNavHelp, never()).switchToLoginPage(any());
    }

    /**
	 * Tests the handleLogoutButton method when navigation fails after logout.
	 * Verifies that it shows an error alert indicating the navigation failure.
	 * 
	 * @throws Exception if any error occurs during logout handling
	 */
    @Test
    void testHandleLogoutButton_navigationFails_shouldShowError() throws Exception {
        Button mockLogoutButton = new Button("Logout");
        TestUtils.setPrivateField(controller, "logoutButton", mockLogoutButton);

        userService mockUserService = mock(userService.class);
        NavigationHelper mockNavHelp = mock(NavigationHelper.class);
        AlertHelper mockAlertHelper = mock(AlertHelper.class);

        doThrow(new RuntimeException("Navigation failure"))
            .when(mockNavHelp).switchToLoginPage(mockLogoutButton);

        TestUtils.setPrivateField(controller, "userSer", mockUserService);
        TestUtils.setPrivateField(controller, "navHelp", mockNavHelp);
        TestUtils.setPrivateField(controller, "alertHelper", mockAlertHelper);

        assertDoesNotThrow(() -> controller.handleLogoutButton());

        verify(mockUserService).logout();
        verify(mockNavHelp).switchToLoginPage(mockLogoutButton);
        verify(mockAlertHelper).showError(eq("Logout Error"), contains("logging out"), contains("Navigation failure"));
    }


}
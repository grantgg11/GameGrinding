package nonfunctional.DataIntegrityandSecurity;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import services.userService;

/**
 * Non-functional test class for validating password security policy (US-9).
 * Ensures passwords meet required strength criteria: 
 * - Minimum of 8 characters
 * - At least one uppercase letter
 * - At least one lowercase letter
 * - At least one number
 * - At least one special character
 */
public class PasswordPolicyValidationTest { 

    private final userService service = new userService();

    /**
     * Test case: Password meets all strength criteria.
     * Expected: Passes the strength validation.
     */
    @Test
    void password_shouldPass_whenMeetsAllCriteria() {
        String securePassword = "Str0ng@Pass";
        assertTrue(service.isPasswordStrong(securePassword), "Password should pass when it meets all criteria");
    }

    /**
     * Test case: Password is fewer than 8 characters.
     * Expected: Fails due to insufficient length.
     */
    @Test
    void password_shouldFail_whenTooShort() {
        String shortPassword = "S@1a";
        assertFalse(service.isPasswordStrong(shortPassword), "Password should fail if fewer than 8 characters");
    }

    /**
     * Test case: Password lacks an uppercase letter.
     * Expected: Fails due to missing uppercase character.
     */
    @Test
    void password_shouldFail_whenMissingUppercase() {
        String password = "weak@pass1";
        assertFalse(service.isPasswordStrong(password), "Password should fail without uppercase letter");
    }

    /**
     * Test case: Password lacks a lowercase letter.
     * Expected: Fails due to missing lowercase character.
     */
    @Test
    void password_shouldFail_whenMissingLowercase() {
        String password = "STRONG@123";
        assertFalse(service.isPasswordStrong(password), "Password should fail without lowercase letter");
    }

    /**
     * Test case: Password lacks a numeric digit.
     * Expected: Fails due to missing number.
     */
    @Test
    void password_shouldFail_whenMissingNumber() {
        String password = "NoNum@Pass";
        assertFalse(service.isPasswordStrong(password), "Password should fail without a numeric digit");
    }

    /**
     * Test case: Password lacks a special character.
     * Expected: Fails due to missing symbol.
     */
    @Test
    void password_shouldFail_whenMissingSpecialCharacter() {
        String password = "Strong123";
        assertFalse(service.isPasswordStrong(password), "Password should fail without a special character");
    }

    /**
     * Test case: Password is exactly 8 characters and meets all criteria.
     * Expected: Passes the strength validation.
     */
    @Test
    void password_shouldPass_withBoundaryLength() {
        String password = "A1b@4567";
        assertTrue(service.isPasswordStrong(password), "Password with exactly 8 characters meeting all criteria should pass");
    }
}

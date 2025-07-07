package AllTestRunner;

import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

/**
 * Test Suite Runner for the GameGrinding application.
 * This class uses JUnit Platform Suite annotations to define a test suite that
 * automatically discovers and runs all JUnit 5 (Jupiter) tests across key packages 
 * within the application. This central runner allows for consolidated test execution 
 * across the full application stack, enabling regression and integration testing.
 */
@Suite
@IncludeEngines("junit-jupiter")
@SelectPackages({
    "database",
    "controllers",
    "utils",
    "models",
    "services",
    "reports",
    "security"
     
})
public class AllTestRunner {
}

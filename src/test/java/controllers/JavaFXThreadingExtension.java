package controllers;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import org.junit.jupiter.api.extension.*;

import java.util.concurrent.CountDownLatch;

/**
 * JUnit 5 extension to ensure that all test methods involving JavaFX components 
 * are executed on the JavaFX Application Thread.
 *
 * This is necessary because JavaFX UI elements must be accessed and modified
 * on the JavaFX thread, and JUnit tests by default run on a separate thread.
 *
 * The extension performs the following:
 *   Initializes the JavaFX runtime once per test suite using JFXPanel.
 *   Ensures that each test method is invoked on the JavaFX thread via Platform#runLater(Runnable).
 */
public class JavaFXThreadingExtension implements BeforeAllCallback, InvocationInterceptor {

    private static boolean javafxInitialized = false;

    /**
     * Initializes the JavaFX runtime before all tests in the test class are run.
     * This uses JFXPanel to bootstrap JavaFX and blocks until the platform is ready.
     *
     * @param context the current extension context; never {@code null}
     * @throws Exception if any error occurs during JavaFX initialization
     */
    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        if (!javafxInitialized) {
            CountDownLatch latch = new CountDownLatch(1);
            new JFXPanel(); 
            Platform.runLater(latch::countDown);
            latch.await();
            javafxInitialized = true;
        }
    }

    /**
     * Intercepts test method execution and reroutes it to run on the JavaFX Application Thread.
     * If the test method throws an exception, it is captured and re-thrown after JavaFX execution completes.
     *
     * @param invocation the invocation to proceed
     * @param invocationContext context of the reflective invocation
     * @param extensionContext context of the current test extension
     * @throws Throwable any exception thrown by the test method
     */
    @Override
    public void interceptTestMethod(Invocation<Void> invocation, ReflectiveInvocationContext<java.lang.reflect.Method> invocationContext,
                                    ExtensionContext extensionContext) throws Throwable {
        CountDownLatch latch = new CountDownLatch(1);
        Throwable[] exception = new Throwable[1];
        Platform.runLater(() -> {
            try {
                invocation.proceed();
            } catch (Throwable t) {
                exception[0] = t;
            } finally {
                latch.countDown();
            }
        });
        latch.await();
        if (exception[0] != null) throw exception[0];
    }
}

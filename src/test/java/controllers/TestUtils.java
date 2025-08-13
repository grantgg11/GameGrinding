package controllers;

import models.game;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.testfx.util.WaitForAsyncUtils;

import javafx.application.Platform;
import javafx.stage.Stage; 

/**
 * TestUtils provides reflection helpers, JavaFX threading utilities, and test data builders
 * used across unit and UI (TestFX) tests in the GameGrinding application.
 *
 * This utility class supports:
 * - Reflection-based field access:
 *   - setPrivateField(...) to inject dependencies or UI nodes into controllers under test.
 *   - getPrivateField(...) to retrieve internal state for assertions.
 *   - setStaticField(...) to override static fields during tests.
 * - Reflection-based method invocation:
 *   - invokePrivateMethod(...) overloads to call private methods with/without parameters and
 *     optionally capture return values, walking the class hierarchy as needed.
 * - JavaFX/test orchestration:
 *   - preloadGameCollection(...) to seed a GameCollectionController on the JavaFX Application
 *     Thread using Platform.runLater(...) and WaitForAsyncUtils to flush FX events deterministically.
 *   - getPopupStage(...) to extract a private Stage reference for popup-based UI assertions.
 * - Test data factories:
 *   - generateMockGames(int) to build lists of lightweight game objects.
 *   - createTestGame() to return a canonical single game instance for focused scenarios.
 *
 * Error handling in reflection helpers wraps checked exceptions in RuntimeException with clear
 * context, simplifying call sites while preserving failure details for debugging.
 */
public class TestUtils {
	
    /**
     * Sets a private field of a given object using reflection.
     *
     * @param target    The object whose field is to be set.
     * @param fieldName The name of the private field.
     * @param value     The value to set.
     */
	public static void setPrivateField(Object target, String fieldName, Object value) {
	    try {
	        Class<?> clazz = target.getClass();
	        Field field = null;
	        while (clazz != null) {
	            try {
	                field = clazz.getDeclaredField(fieldName);
	                break;
	            } catch (NoSuchFieldException e) {
	                clazz = clazz.getSuperclass();
	            }
	        }

	        if (field == null) {
	            throw new NoSuchFieldException("Field '" + fieldName + "' not found in class hierarchy.");
	        }

	        field.setAccessible(true);
	        field.set(target, value);
	    } catch (Exception e) {
	        throw new RuntimeException(e);
	    }
	}


    /**
     * Generates a list of mock game objects for testing.
     *
     * @param count The number of mock games to generate.
     * @return A list of mock game objects.
     */
    public static List<game> generateMockGames(int count) {
        List<game> list = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            game g = new game();
            g.setGameID(i);
            g.setTitle("Mock Game " + i);
            g.setCoverImageUrl("http://example.com/img" + i + ".png");
            list.add(g);
        }
        return list;
    }
    
    /**
     * Retrieves the value of a private field from an object using reflection.
     *
     * @param target    The object from which to retrieve the field.
     * @param fieldName The name of the private field.
     * @return The value of the field.
     */
    public static Object getPrivateField(Object target, String fieldName) {
        try {
            Class<?> clazz = target.getClass();
            Field field = null;

            while (clazz != null) {
                try {
                    field = clazz.getDeclaredField(fieldName);
                    break;
                } catch (NoSuchFieldException e) {
                    clazz = clazz.getSuperclass();
                }
            }

            if (field == null) {
                throw new NoSuchFieldException("Field '" + fieldName + "' not found.");
            }

            field.setAccessible(true);
            return field.get(target);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Invokes a private method with no parameters using reflection.
     *
     * @param target     The object containing the method.
     * @param methodName The name of the method to invoke.
     */
    public static void invokePrivateMethod(Object target, String methodName) {
        try {
            Class<?> clazz = target.getClass();
            Method method = null;
            while (clazz != null) {
                try {
                    method = clazz.getDeclaredMethod(methodName);
                    break;
                } catch (NoSuchMethodException e) {
                    clazz = clazz.getSuperclass();
                }
            }

            if (method == null) {
                throw new NoSuchMethodException("Method '" + methodName + "' not found in class hierarchy.");
            }

            method.setAccessible(true);
            method.invoke(target);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke method: " + methodName, e);
        }
    }
    
    /**
     * Invokes a private method with parameters using reflection.
     *
     * @param target         The object containing the method.
     * @param methodName     The name of the method to invoke.
     * @param parameterTypes The parameter types of the method.
     * @param args           The arguments to pass to the method.
     */
    public static void invokePrivateMethod(Object target, String methodName, Class<?>[] parameterTypes, Object[] args) {
        try {
            Class<?> clazz = target.getClass();
            Method method = null;

            while (clazz != null) {
                try {
                    method = clazz.getDeclaredMethod(methodName, parameterTypes);
                    break;
                } catch (NoSuchMethodException e) {
                    clazz = clazz.getSuperclass();
                }
            }

            if (method == null) {
                throw new NoSuchMethodException("Method '" + methodName + "' not found in class hierarchy.");
            }

            method.setAccessible(true);
            method.invoke(target, args);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke method: " + methodName, e);
        }
    }
    
    /**
     * Invokes a private method with parameters and returns a value.
     *
     * @param target         The object containing the method.
     * @param methodName     The name of the method to invoke.
     * @param parameterTypes The parameter types of the method.
     * @param args           The arguments to pass to the method.
     * @param returnValue    True if a return value is expected.
     * @return The result of the method invocation.
     */
    public static Object invokePrivateMethod(Object target, String methodName, Class<?>[] parameterTypes, Object[] args, boolean returnValue) {
        try {
            Class<?> clazz = target.getClass();
            Method method = null;
            while (clazz != null) {
                try {
                    method = clazz.getDeclaredMethod(methodName, parameterTypes);
                    break;
                } catch (NoSuchMethodException e) {
                    clazz = clazz.getSuperclass();
                }
            }

            if (method == null) {
                throw new NoSuchMethodException("Method '" + methodName + "' not found in class hierarchy.");
            }

            method.setAccessible(true);
            return method.invoke(target, args);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke method: " + methodName, e);
        }
    }



    /**
     * Gets the popupStage from the given controller.
     *
     * @param controller The controller that holds a private Stage field.
     * @return The Stage instance of the popup, or null if not found.
     */
    public static Stage getPopupStage(Object controller) {
        Object stage = getPrivateField(controller, "popupStage");
        return (stage instanceof Stage) ? (Stage) stage : null;
    }
    
    /**
     * Sets a static field of a class using reflection.
     *
     * @param targetClass The class containing the static field.
     * @param fieldName   The name of the field to set.
     * @param value       The value to assign to the field.
     */
    public static void setStaticField(Class<?> targetClass, String fieldName, Object value) {
        try {
            Field field = targetClass.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(null, value); // 'null' because it's a static field
        } catch (Exception e) {
            throw new RuntimeException("Failed to set static field '" + fieldName + "'", e);
        }
    }


    /**
     * Creates a sample game object for testing purposes.
     *
     * @return A test game instance.
     */
    public static game createTestGame() {
            game g = new game();
            g.setGameID(1);
            g.setTitle("Test Game");
            g.setPlatform("PC");
            g.setGenre("Adventure");
            g.setCoverImageUrl("");
            g.setCompletionStatus("Not Started");
            return g;
        }

    /**
     * Preloads a GameCollectionController with a test game collection
     * and calls the loadUserCollection method on the JavaFX thread.
     *
     * @param controller The controller to preload with test data.
     */
   public static void preloadGameCollection(GameCollectionController controller) {
            List<game> testGames = List.of(createTestGame());

            Platform.runLater(() -> {
                controller.userCollection = testGames;
                controller.loadUserCollection(testGames);
            });

            WaitForAsyncUtils.waitForFxEvents();
        }
}

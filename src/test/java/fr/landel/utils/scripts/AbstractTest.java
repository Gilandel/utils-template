/*-
 * #%L
 * utils-scripts
 * %%
 * Copyright (C) 2016 - 2017 Gilles Landel
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package fr.landel.utils.scripts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.function.BiFunction;
import java.util.regex.Pattern;

import org.junit.ComparisonFailure;

import fr.landel.utils.assertor.ParameterAssertor;
import fr.landel.utils.commons.expect.Expect;
import fr.landel.utils.commons.function.ThrowableSupplier;
import fr.landel.utils.commons.function.TriFunction;

/**
 * Abstract for tests
 *
 * @since Jul 31, 2016
 * @author Gilles
 *
 */
public abstract class AbstractTest {

    /**
     * Map an {@link IllegalArgumentException} into and {@link AssertionError}
     */
    public static final BiFunction<String, List<ParameterAssertor<?>>, AssertionError> JUNIT_THROWABLE = (message,
            parameters) -> new AssertionError(message);;

    /**
     * Function to manage the creation of Junit exception
     */
    private static final TriFunction<Boolean, String, String, AssertionError> JUNIT_ERROR = (catched, expected, actual) -> {
        if (catched) {
            return new ComparisonFailure("The exception message don't match.", expected, actual);
        } else {
            return new AssertionError("The expected exception never comes up.");
        }
    };

    /**
     * Check that the consumed code throws the specified exception.
     * 
     * <pre>
     * assertException(() -&gt; {
     *     // throw new IllegalArgumentException("parameter cannot be null");
     *     getMyType(null);
     * }, IllegalArgumentException.class);
     * </pre>
     * 
     * @param consumer
     *            The consumer (required, not null)
     * @param expectedException
     *            The expected exception type (required, not null)
     * @param <T>
     *            The generic expected exception type
     */
    public static <T extends Throwable> void assertException(final ThrowableSupplier<Throwable> consumer,
            final Class<T> expectedException) {

        Expect.exception(consumer, expectedException, JUNIT_ERROR);
    }

    /**
     * Check that the consumed code raise the specified exception, also check
     * the message.
     * 
     * <pre>
     * assertException(() -&gt; {
     *     // throw new IllegalArgumentException("parameter cannot be null");
     *     getMyType(null);
     * }, IllegalArgumentException.class, "parameter cannot be null");
     * </pre>
     * 
     * @param consumer
     *            The consumer (required, not null)
     * @param expectedException
     *            The expected exception type (required, not null)
     * @param expectedMessage
     *            The expected exception message
     * @param <T>
     *            The generic expected exception type
     */
    public static <T extends Throwable> void assertException(final ThrowableSupplier<Throwable> consumer, final Class<T> expectedException,
            final String expectedMessage) {

        Expect.exception(consumer, expectedException, expectedMessage, JUNIT_ERROR);
    }

    /**
     * Check that the consumed code raise the specified exception, also check
     * the message with the specified pattern.
     * 
     * <pre>
     * assertException(() -&gt; {
     *     // throw new IllegalArgumentException("parameter cannot be null");
     *     getMyType(null);
     * }, IllegalArgumentException.class, Pattern.compile("^parameter");
     * </pre>
     * 
     * @param consumer
     *            The consumer (required, not null)
     * @param expectedException
     *            The expected exception type (required, not null)
     * @param messagePattern
     *            The message pattern
     * @param <T>
     *            The generic expected exception type
     */
    public static <T extends Throwable> void assertException(final ThrowableSupplier<Throwable> consumer, final Class<T> expectedException,
            final Pattern messagePattern) {

        Expect.exception(consumer, expectedException, messagePattern, JUNIT_ERROR);
    }

    /**
     * Utility method to check private constructor of utility class
     * 
     * @param clazz
     *            the class to check
     * @return true if ok
     * @throws AssertionError
     *             if preconditions mismatch
     */
    public static boolean checkPrivateConstructor(final Class<?> clazz) {
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        assertEquals(1, constructors.length);
        assertTrue(Modifier.isPrivate(constructors[0].getModifiers()));

        constructors[0].setAccessible(true);

        try {
            constructors[0].newInstance();
            return false;
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            assertTrue(InvocationTargetException.class.isAssignableFrom(e.getClass()));
            final InvocationTargetException exception = (InvocationTargetException) e;
            assertNotNull(exception.getTargetException());
            return UnsupportedOperationException.class.isAssignableFrom(exception.getTargetException().getClass());
        }
    }
}

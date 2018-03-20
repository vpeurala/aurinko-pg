package org.aurinkopg.coverage;

import org.aurinkopg.GlobalConstants;
import org.aurinkopg.locale.FinnishLocaleUtil;
import org.aurinkopg.locale.MissingFinnishLocaleException;
import org.aurinkopg.postgresql.SqlExecutor;
import org.junit.Test;

import java.lang.reflect.Constructor;

/**
 * This is a test suite which invokes all those things which never get invoked
 * in production, but which Java forces us to write to satisfy the compiler.
 * <p>
 * The reason for that is that we want to have 100% test coverage. Before you
 * think that I am an idiot, read ahead.
 * <p>
 * To be clear - we don't aim for 100% test coverage because that would be
 * somehow a sensible goal in itself. It is totally ok that some branches of code
 * are not covered by automated tests - Java is a language which forces you to
 * write some code that is unreachable in reality, but the compiler does not
 * understand that.
 * <p>
 * The reason I like to have 100% test coverage is that it reveals dead code easily;
 * if the coverage drops below 100%, the most common reason for that is that
 * some piece of code is not used anymore in real life either, not just tests.
 * And then that piece of dead code can be removed.
 * <p>
 * Having 100% test coverage is, in my opinion, the easiest dead code detector
 * to setup and use; there exist some separate products for dead code
 * detection, but they are usually time-consuming to configure and not
 * totally reliable. Having 100% test coverage is easier, because you would
 * write tests anyway, and in a well-tested project the test coverage would
 * be pretty high anyway, so having to write a few "stupid" tests, which don't
 * add any real world value by themselves, is in my opinion the easiest way
 * to have a reliable way to detect and remove dead code.
 */
public class InvokeImpossibleThingsForCodeCoverageTest {
    /**
     * Some classes are just collections of static functions.
     * Classes of that kind should normally have a private constructor,
     * so that no one accidentally instantiates them.
     * <p>
     * This test calls those private constructors via reflection so
     * that we don't get a hole in our code coverage reports because of that.
     * <p>
     * This is a known problem which must be solved somehow in every project.
     * <p>
     * See the javadoc of this class for an explanation of why we do this.
     */
    @Test
    public void invokeDefaultConstructorsOfClassesWhichAreNotMeantToBeInstantiated() throws Exception {
        invokeDefaultConstructor(FinnishLocaleUtil.class);
        invokeDefaultConstructor(GlobalConstants.class);
        invokeDefaultConstructor(SqlExecutor.class);
    }

    private <T> void invokeDefaultConstructor(Class<T> klass) throws Exception {
        Constructor<T> constructor = klass.getDeclaredConstructor();
        constructor.setAccessible(true);
        constructor.newInstance();
    }

    /**
     * MissingFinnishLocaleException cannot be thrown on a machine where
     * the Finnish locale is installed. See the javadoc of this class
     * for an explanation of why we do this.
     */
    @Test
    public void throwMissingFinnishLocaleException() throws Exception {
        try {
            throw new MissingFinnishLocaleException();
        } catch (MissingFinnishLocaleException e) {
            // Pass.
        }
    }
}

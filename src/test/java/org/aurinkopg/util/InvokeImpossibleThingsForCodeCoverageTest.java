package org.aurinkopg.util;

import org.junit.Test;

import java.lang.reflect.Constructor;

public class InvokeImpossibleThingsForCodeCoverageTest {
    @Test
    public void invokeDefaultConstructorsOfClassesWhichAreNotMeantToBeInstantiated() throws Exception {
        Constructor<FinnishLocaleUtil> constructor = FinnishLocaleUtil.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        constructor.newInstance();
    }
}

package org.aurinkopg.util;

public class ObjectUtils {
    public static void assertNotNull(Object o, String message) {
        if (o == null) {
            throw new IllegalArgumentException(message);
        }
    }
}

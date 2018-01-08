package org.aurinkopg.util;

public class MissingFinnishLocaleException extends RuntimeException {
    public MissingFinnishLocaleException() {
        super("Finnish locale is not found on this computer.");
    }
}

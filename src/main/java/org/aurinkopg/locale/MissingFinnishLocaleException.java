package org.aurinkopg.locale;

public class MissingFinnishLocaleException extends RuntimeException {
    public MissingFinnishLocaleException() {
        super("Finnish locale is not found on this computer.");
    }
}

package org.aurinkopg.locale;

import java.time.ZoneId;
import java.util.Arrays;
import java.util.Locale;
import java.util.TimeZone;

public class FinnishLocaleUtil {
    /**
     * Not meant to be instantiated.
     */
    private FinnishLocaleUtil() {}

    public static Locale finnishLocale() {
        return Arrays.
            stream(Locale.getAvailableLocales()).
            filter((locale) -> locale.getISO3Country().equals("FIN")).
            findAny().
            orElseThrow(MissingFinnishLocaleException::new);
    }

    public static ZoneId finnishTimeZoneId() {
        return ZoneId.of("Europe/Helsinki");
    }

    public static TimeZone finnishTimeZone() {
        return TimeZone.getTimeZone(finnishTimeZoneId());
    }
}

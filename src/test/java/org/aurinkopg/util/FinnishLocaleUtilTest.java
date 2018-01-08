package org.aurinkopg.util;

import org.junit.Test;

import java.time.ZoneId;
import java.util.Locale;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class FinnishLocaleUtilTest {
    @Test
    public void finnishLocaleIsFound() {
        Locale locale = FinnishLocaleUtil.finnishLocale();
        assertNotNull(locale);
        assertEquals("FIN", locale.getISO3Country());
        assertEquals("fin", locale.getISO3Language());
        assertEquals("FI", locale.getCountry());
        assertEquals("Finland", locale.getDisplayCountry());
        assertEquals("Finnish", locale.getDisplayLanguage());
        assertEquals("Finnish (Finland)", locale.getDisplayName());
        assertEquals("", locale.getDisplayScript());
        assertEquals("", locale.getDisplayVariant());
        assertEquals("fi-FI", locale.toLanguageTag());
    }

    @Test
    public void finnishTimeZoneIdIsFound() {
        ZoneId zoneId = FinnishLocaleUtil.finnishTimeZoneId();
        assertNotNull(zoneId);
    }

    @Test
    public void finnishTimeZoneIsFound() {
        TimeZone timeZone = FinnishLocaleUtil.finnishTimeZone();
        assertNotNull(timeZone);
    }
}

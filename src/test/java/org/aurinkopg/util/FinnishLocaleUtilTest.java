package org.aurinkopg.util;

import org.junit.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.TimeZone;

import static org.aurinkopg.util.FinnishLocaleUtil.finnishLocale;
import static org.aurinkopg.util.FinnishLocaleUtil.finnishTimeZoneId;
import static org.junit.Assert.*;

public class FinnishLocaleUtilTest {
    private static final LocalDateTime INDEPENDENCE_DAY_2017 = LocalDateTime.of(2017, 12, 6, 18, 0);
    private static final LocalDateTime MIDSUMMER_2017 = LocalDateTime.of(2017, 6, 24, 18, 0);

    @Test
    public void finnishLocaleIsFoundAndHasExpectedProperties() {
        Locale locale = finnishLocale();
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
    public void finnishTimeZoneIdIsFoundAndHasExpectedProperties() {
        ZoneId zoneId = finnishTimeZoneId();
        assertNotNull(zoneId);
        assertEquals("Eastern European Time", zoneId.getDisplayName(TextStyle.FULL, finnishLocale()));
        assertEquals("Eastern European Time", zoneId.getDisplayName(TextStyle.FULL_STANDALONE, finnishLocale()));
        assertEquals("EET", zoneId.getDisplayName(TextStyle.SHORT, finnishLocale()));
        assertEquals("EET", zoneId.getDisplayName(TextStyle.SHORT_STANDALONE, finnishLocale()));
        assertEquals("Europe/Helsinki", zoneId.getDisplayName(TextStyle.NARROW, finnishLocale()));
        assertEquals("EET", zoneId.getDisplayName(TextStyle.NARROW_STANDALONE, finnishLocale()));
        assertEquals("Europe/Helsinki", zoneId.getId());
        assertEquals(ZoneOffset.ofHours(2), zoneId.getRules().getOffset(INDEPENDENCE_DAY_2017));
        assertEquals(ZoneOffset.ofHours(3), zoneId.getRules().getOffset(MIDSUMMER_2017));
        assertFalse(zoneId.getRules().isDaylightSavings(INDEPENDENCE_DAY_2017.toInstant(ZoneOffset.ofHours(2))));
        assertTrue(zoneId.getRules().isDaylightSavings(MIDSUMMER_2017.toInstant(ZoneOffset.ofHours(2))));
        assertEquals(zoneId, zoneId.normalized());
    }

    @Test
    public void finnishTimeZoneIsFoundAndHasExpectedProperties() {
        TimeZone timeZone = FinnishLocaleUtil.finnishTimeZone();
        assertNotNull(timeZone);
        assertEquals("Europe/Helsinki", timeZone.getID());
        assertTrue(timeZone.observesDaylightTime());
        assertEquals(7200000, timeZone.getRawOffset());
        assertEquals(3600000, timeZone.getDSTSavings());
    }
}

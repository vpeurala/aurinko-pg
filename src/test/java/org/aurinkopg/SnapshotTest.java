package org.aurinkopg;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class SnapshotTest {
    @Test
    public void ordinaryNameIsOk() {
        new Snapshot("foo");
    }

    @Test
    public void nameWithMoreThan63BytesIsNotOk() {
        try {
            new Snapshot("asdfasfgsdfgsadfasfdgasdfkashdjfhajskdhfkjashdfkjhasdkjfhaskjdhfjkasdkfjhaksjdhfjakshdfkjhaskjdfhajskdhfkasjdfhkasjhdfjkasdkjfhaskdjfhajskdhfkjashdkfjhjaskdfhkasjdfhkahsjdjkfhasdf");
            fail("Snapshot creation should have failed.");
        } catch (IllegalArgumentException e) {
            assertEquals("Your snapshot name is too long. The maximum length is 63 bytes.", e.getMessage().substring(0, 63));
        }
    }

    @Test
    public void nullNameIsNotOk() {
        try {
            new Snapshot(null);
            fail("Snapshot creation should have failed.");
        } catch (NullPointerException e) {
            assertEquals("The name of a Snapshot cannot be null!", e.getMessage());
        }
    }

    @Test
    public void emptyNameIsNotOk() {
        try {
            new Snapshot("");
            fail("Snapshot creation should have failed.");
        } catch (IllegalArgumentException e) {
            assertEquals("You tried to use an empty string as a snapshot name.", e.getMessage());
        }
    }

    @Test
    public void specialCharactersAreNotOk() {
        try {
            new Snapshot("-");
            fail("Snapshot creation should have failed.");
        } catch (IllegalArgumentException e) {
            assertEquals("Your snapshot name '-' did not match the pattern of acceptable database names. See https://www.postgresql.org/docs/current/static/sql-syntax-lexical.html#SQL-SYNTAX-IDENTIFIERS for the rules.", e.getMessage());
        }
    }
}
